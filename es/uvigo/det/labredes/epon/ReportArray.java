package es.uvigo.det.labredes.epon;

import java.util.Arrays;
import java.util.Collections;

/**
 * This class implements an array of traffic reports.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
public class ReportArray {
    /**
     * The array of traffic reports
     */
    private Report[] report_array;
    /**
     * The number of active ONUs.
     */
    public int num_active_onus;
    /**
     * The overall amount of data stored in the upstream queues of all ONUs.
     */
    public int overall_qsize;
    /**
     * The overall amount of data that all ONUs can transmit in the next DBA cycle.
     */
    public int overall_tsize;

    /**
     * Creates a new array of traffic reports.
     */
    public ReportArray() {
	report_array = new Report[EponSimulator.num_onus];
	overall_qsize = overall_tsize = 0;
	num_active_onus = 0;
    }

    /**
     * Adds the specified report to this report array at the right position.
     *
     * @param report the Report to be added
     * @return true if the report is correctly added to this report array
     */
    public boolean addReport(Report report) {
	if (report.onu_id >= 0 && report.onu_id < EponSimulator.num_onus) {
	    if (report.onu_qsize > 0) {
		overall_qsize += report.onu_qsize;
		num_active_onus++;
	    }
	    if (report.onu_tsize > 0) {
		overall_tsize += report.onu_tsize;
	    }
	    report_array[report.onu_id] = report;
	    return true;
	}
	return false;
    }

    /**
     * Removes all the reports from this report array.
     */
    public void clear() {
	overall_qsize = overall_tsize = 0;
	num_active_onus = 0;
	Arrays.fill(report_array, null);
    }

    /**
     * Returns the report at the specified position in this report array.
     *
     * @param pos the specified position
     * @return the report at the specified position in this report array
     */
    public Report getReport(int pos) {
	return report_array[pos];
    }

    /**
     * Prints on standard output a message for each report contained in this report array.
     */
    public void printReports() {
	Report report;
	for (int i = 0; i < EponSimulator.num_onus; i++) {
	    report = report_array[i];
	    if (report != null) {
		report.printReport();
	    }
	}
	System.out.println("OVERALL ONUs qsize=" + overall_qsize + " tsize=" + overall_tsize + " active=" + num_active_onus);
    }

    /**
     * Sorts this report array by the specified report field.
     *
     * @param sort_by       the report field to sort by
     * @param reverse_order if true sorts the array in reverse order
     */
    public void sortReports(String sort_by, boolean reverse_order) {
	Report.SORT_BY = sort_by;
	Arrays.sort(report_array, reverse_order ? Collections.reverseOrder() : null);
    }
}
