package de.lmu.ifi.dbs.medmon.medic.ui.wizard;

public abstract class ImportWizardOptions {

	public static int	NO_OPTION		= 0;
	public static int	IMPORT_RAW		= 1 << 0;
	public static int	IMPORT_TRAINING	= 1 << 1;
	public static int	SOURCE_SENSOR	= 1 << 2;
	public static int	SOURCE_FILE		= 1 << 3;
	public static int	TYPE_CSV		= 1 << 4;
	public static int	TYPE_ARFF		= 1 << 5;

}
