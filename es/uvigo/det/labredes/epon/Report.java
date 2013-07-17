package es.uvigo.det.labredes.epon;

/**
 * This class implements traffic reports sent from the ONUs to the OLT.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
public class Report implements Comparable {
    /**
     * The ONU identifier.
     */
    public int onu_id;
    /**
     * The amount of data stored in the ONU upstream queue.
     */
    public int onu_qsize;
    /**
     * The amount of data that the ONU can transmit in the next DBA cycle.
     */
    public int onu_tsize;
    /**
     * The report field used to compare reports.
     */
    public static String SORT_BY;

    /**
     * Creates a new traffic report.
     *
     * @param id    ONU identifier
     * @param qsize amount of data stored in the upstream queue of the ONU
     */
    public Report (int id, int qsize) {
	onu_id = id;
	onu_qsize = qsize;
	onu_tsize = 0;
    }

    /**
     * Compares two reports based on the amount of data stored in the upstream queue of each ONU (if the static field SORT_BY equals "onu_qsize") or on the amount of data that each ONU can transmit in the next DBA cycle (if the static field SORT_BY equals "onu_tsize"). Otherwise, the reports are compared based on each ONU identifier.
     *
     * @param report the Report to be compared
     * @return the value 0 if both the argument report and this report involve the same amount of data stored in the upstream queue; a value less than 0 if this report involves less amount of stored data than the report argument; and a value greater than 0 if this report involves more amount of stored data than the report argument
     */
    public int compareTo(Object report) {
	if (SORT_BY.equals("onu_qsize")) {
	    return this.onu_qsize - ((Report) report).onu_qsize;
	} else if (SORT_BY.equals("onu_tsize")) {
	    return this.onu_tsize - ((Report) report).onu_tsize;
	} else {
	    return this.onu_id - ((Report) report).onu_id;
	}
    }

    /**
     * Prints on standard output a message describing this traffic report.
     */
    public void printReport() {
	System.out.println("REPORT ONU " + onu_id + " qsize=" + onu_qsize + " tsize=" + onu_tsize);
    }
}
