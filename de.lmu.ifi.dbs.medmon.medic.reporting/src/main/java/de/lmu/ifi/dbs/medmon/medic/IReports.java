package de.lmu.ifi.dbs.medmon.medic;

/**
 * Provides the resource id for all reports.
 * 
 * @author Nepomuk Seiler
 * @verison 0.1
 * @since 2012-04-13
 * 
 */
public interface IReports {
	
	/* ==================================== */
	/* ======= Report Resource Ids ======== */
	/* ==================================== */

	public static final String PATIENT_REPORT = "medmon.medic.patient";
	public static final String PATIENT_OVERVIEW = PATIENT_REPORT + ".overview";
	public static final String PATIENT_THERAPY_RESULT = PATIENT_REPORT + ".therapyResult";
	public static final String PATIENT_THERAPY = PATIENT_REPORT + ".therapy";
	
	/* ==================================== */
	/* ========= Report Parameter ========= */
	/* ==================================== */
	
	public static final String PATIENT = "patient";
	public static final String THERAPY = "therapy";
	public static final String THERAPY_RESULT = "therapyResult";
	
	/** Requires and Instances object with the TimeSeriesResults format. */
	public static final String TIMESERIES_RESULTS = "timeSeriesResults";
	
}
