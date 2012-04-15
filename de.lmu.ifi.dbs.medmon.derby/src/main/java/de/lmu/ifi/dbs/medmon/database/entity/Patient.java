package de.lmu.ifi.dbs.medmon.database.entity;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createDirectory;
import static java.nio.file.Files.walkFileTree;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

@Entity
@NamedQueries({
		@NamedQuery(name = "Patient.findAll", query = "SELECT p FROM Patient p"),
		@NamedQuery(name = "Patient.findByFirstname", query = "SELECT p FROM Patient p WHERE p.firstname = :firstname"),
		@NamedQuery(name = "Patient.findByLastname", query = "SELECT p FROM Patient p WHERE p.lastname = :lastname"),
		@NamedQuery(name = "Patient.findByInsuranceId", query = "SELECT p FROM Patient p WHERE p.insuranceId = :insuranceId"),
		@NamedQuery(name = "Patient.findIdentical", query = "SELECT p FROM Patient p WHERE p.firstname = :firstname AND p.lastname = :lastname AND p.birth = :birth"),
		@NamedQuery(name = "Patient.likeName", query = "SELECT p FROM Patient p WHERE lower(p.lastname) LIKE :lastname OR lower(p.firstname) LIKE :firstname") })
public class Patient {

	public static final short	MALE	= 0;
	public static final short	FEMALE	= 1;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long				id;

	private String				firstname;

	private String				lastname;

	private short				gender;

	@Column(unique = true)
	private String				insuranceId;

	@Temporal(TemporalType.DATE)
	private Date				birth;

	@Lob
	private String				comment;

	private String				cluster;

	@Temporal(TemporalType.DATE)
	private Date				therapystart;

	@OneToMany(mappedBy = "patient", cascade = { CascadeType.REMOVE, CascadeType.REFRESH })
	private List<Therapy>		therapies;

	@OneToMany(mappedBy = "patient", cascade = { CascadeType.REMOVE, CascadeType.REFRESH })
	private List<Data>			data;

	transient boolean			remove;

	public Patient() {
	}

	public Patient(String firstname, String lastname, String insuranceId) {
		setFirstname(firstname);
		setLastname(lastname);
		setInsuranceId(insuranceId);
	}

	@PrePersist
	void prePersist() {
		try {
			Path path = toPath();
			Path root = createDirectories(path);
			createDirectory(root.resolve(Data.TRAIN));
			createDirectory(root.resolve(Data.RESULT));
			createDirectory(root.resolve(Data.RAW));
		} catch (IOException e) {
			e.printStackTrace();
			throw new Error("Error while persisting patient", e);
		}
	}

	@PreRemove
	void preRemove() {
		remove = true;
		Path path = toPath();
		try {
			walkFileTree(path, new SimpleFileVisitor<Path>() {
				
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * The directory on disc where all patient data is stored.
	 * Path can be influenced by preferences set in eclipse preference store.
	 * 
	 * @return Path to patient folder on disk
	 */
	public Path toPath() {
		IEclipsePreferences node = ConfigurationScope.INSTANCE.getNode("medmon");
		String folder = node.get("patient", null);

		// TODO throw new Error is not good. Try this one
		// http://www.java2s.com/Tutorial/Java/0355__JPA/EntityListenersPrePersist.htm
		if (folder == null)
			throw new Error("No patient folder defined in preference node " + node);

		DecimalFormat df = new DecimalFormat("000000000000");
		return Paths.get(folder, df.format(getId()));
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public short getGender() {
		return gender;
	}

	public void setGender(short gender) {
		this.gender = gender;
	}

	public String getInsuranceId() {
		return insuranceId;
	}

	public void setInsuranceId(String insuranceId) {
		this.insuranceId = insuranceId;
	}

	public Date getBirth() {
		return birth;
	}

	public void setBirth(Date birth) {
		this.birth = birth;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getCluster() {
		return cluster;
	}

	public void setCluster(String cluster) {
		this.cluster = cluster;
	}

	public Date getTherapystart() {
		return therapystart;
	}

	public void setTherapystart(Date therapystart) {
		this.therapystart = therapystart;
	}

	public List<Therapy> getTherapies() {
		return therapies;
	}

	protected void setTherapies(List<Therapy> therapies) {
		this.therapies = therapies;
	}

	public void addTherapy(Therapy therapy) {
		if(therapies == null)
			therapies = new ArrayList<>();
		therapies.add(therapy);
		if (!this.equals(therapy.getPatient()))
			therapy.setPatient(this);
	}

	public void removeTherapy(Therapy therapy) {
		therapies.remove(therapy);
	}

	public List<Data> getData() {
		return data;
	}

	protected void setData(List<Data> data) {
		this.data = data;
	}

	public boolean addData(Data d) {
		boolean success = data.add(d);
		if (!this.equals(d.getPatient()))
			d.setPatient(this);
		return success;
	}

	public void removeData(Data d) {
		data.remove(d);
	}

	@Override
	public String toString() {
		return getFirstname() + " " + getLastname();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((insuranceId == null) ? 0 : insuranceId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Patient other = (Patient) obj;
		if (insuranceId == null) {
			if (other.insuranceId != null)
				return false;
		} else if (!insuranceId.equals(other.insuranceId))
			return false;
		return true;
	}

}