package de.lmu.ifi.dbs.medmon.database.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "SENSOR")
@NamedQueries({ @NamedQuery(name = "Sensor.findAll", query = "SELECT s FROM Sensor s"),
		@NamedQuery(name = "Sensor.findBySensorId", query = "SELECT s FROM Sensor s WHERE s.sensorId = :sensorId") })
public class Sensor {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private String		id;

	@Column(unique = true)
	private String		sensorId;

	@Column
	private String		name;

	@Column
	private String		version;

	@Column
	private String		defaultpath;

	@Column
	private String		filePrefix;

	@OneToMany(mappedBy = "sensor")
	private Set<Data>	data	= new HashSet<Data>();

	protected Sensor() {
	}

	public Sensor(String name, String sensorId, String version) {
		this.sensorId = sensorId;
		this.name = name;
		this.version = version;
	}

	public String getId() {
		return id;
	}

	public String getSensorId() {
		return sensorId;
	}

	public void setSensorId(String sensorId) {
		this.sensorId = sensorId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDefaultpath() {
		return defaultpath;
	}

	public void setDefaultpath(String defaultpath) {
		this.defaultpath = defaultpath;
	}

	public Set<Data> getData() {
		return data;
	}

	public void setData(Set<Data> data) {
		this.data = data;
	}

	public String getFilePrefix() {
		return filePrefix;
	}

	public void setFilePrefix(String filePrefix) {
		this.filePrefix = filePrefix;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		Sensor other = (Sensor) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
