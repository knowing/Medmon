package de.lmu.ifi.dbs.medmon.medic.ui.util;


public class MedicUtil {
	//TODO Move medic.ui MedicUtil to medic.core
/*
	public static List<PatientClusterAdapter> loadClusterUnits(Patient patient) {
		List<ClusterUnit> units = loadClusterUnits(getClusterUnitFolder(patient));

		List<PatientClusterAdapter> returns = new ArrayList<PatientClusterAdapter>();
		for (ClusterUnit unit : units)
			returns.add(new PatientClusterAdapter(patient, unit));

		return returns;
	}

	public static List<ClusterUnit> loadClusterUnits(String path) {
		List<ClusterUnit> returns = new ArrayList<ClusterUnit>();
		File root = new File(path);
		if (!root.exists())
			return returns;
		try {
			JAXBContext context = JAXBContext.newInstance(ClusterUnit.class);
			Unmarshaller um = context.createUnmarshaller();

			// Only one element
			if (root.isFile()) {
				ClusterUnit cu = (ClusterUnit) um.unmarshal(root);
				return Collections.singletonList(cu);
			}

			File[] files = root.listFiles(new XMLFileFilter());
			for (File file : files) {
				ClusterUnit cu = (ClusterUnit) um.unmarshal(file);
				returns.add(cu);
			}

		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return returns;
	}

	public static ClusterUnit loadClusterUnit(Patient patient, final String cluster) {
		String root = getClusterUnitFolder(patient);
		File file = new File(root);
		File[] files = file.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.equals(cluster + ".xml");
			}

		});
		try {
			JAXBContext context = JAXBContext.newInstance(ClusterUnit.class);
			Unmarshaller um = context.createUnmarshaller();
			return (ClusterUnit) um.unmarshal(files[0]);

		} catch (JAXBException e) {
			e.printStackTrace();
		}

		return null;
	}*/
}
