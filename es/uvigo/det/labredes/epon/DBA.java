package es.uvigo.det.labredes.epon;

/**
 * This class implements DBA algorithms.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
public final class DBA {

    private DBA() {}

    /**
     * Shares the available bandwidth among all active ONUs in a fair manner.
     *
     * @param dba_size maximum amount of data that can be sent from all the ONUs to the OLT in a DBA cycle
     * @param report_array the report array to be handled
     */
    public static void fairAllocation(long dba_size, ReportArray report_array) {
	long remaining_dba_size = dba_size - (EponSimulator.num_onus - report_array.num_active_onus) * EponSimulator.REPORT_SIZE;
	int fair_sharing = report_array.num_active_onus > 0 ? (int) (remaining_dba_size / report_array.num_active_onus) : 0;
	Report report;
	for (int id = 0; id < EponSimulator.num_onus; id++) {
	    report = report_array.getReport(id);
	    if (report != null) {
		report.onu_tsize = report.onu_qsize > 0 ? fair_sharing : EponSimulator.REPORT_SIZE;
		report_array.overall_tsize += report.onu_tsize;
	    } else {
		report = new Report(id, 0);
		report.onu_tsize = EponSimulator.REPORT_SIZE;
		report_array.addReport(report);
	    }
	}
    }

    /**
     * Always allocates the same amount of bandwidth to each ONU.
     *
     * @param dba_size maximum amount of data that can be sent from all the ONUs to the OLT in a DBA cycle
     * @param report_array the report array to be handled
     */
    public static void fixedAllocation(long dba_size, ReportArray report_array) {
	int fixed_sharing = (int) (dba_size / EponSimulator.num_onus);
	Report report;
	for (int id = 0; id < EponSimulator.num_onus; id++) {
	    report = report_array.getReport(id);
	    if (report != null) {
		report.onu_tsize = fixed_sharing;
		report_array.overall_tsize += report.onu_tsize;
	    } else {
		report = new Report(id, 0);
		report.onu_tsize = fixed_sharing;
		report_array.addReport(report);
	    }
	}
    }

    /**
     * Allocates to each ONU just the bandwidth requested.
     *
     * @param report_array the report array to be handled
     */
    public static void gatedAllocation(ReportArray report_array) {
	Report report;
	for (int id = 0; id < EponSimulator.num_onus; id++) {
	    report = report_array.getReport(id);
	    if (report != null) {
		report.onu_tsize = report.onu_qsize + EponSimulator.REPORT_SIZE;
		report_array.overall_tsize += report.onu_tsize;
	    } else {
		report = new Report(id, 0);
		report.onu_tsize = EponSimulator.REPORT_SIZE;
		report_array.addReport(report);
	    }
	}
    }

    /**
     * Allocates to each ONU the bandwidth requested as long as it does not exceed the fixed limit.
     *
     * @param dba_size maximum amount of data that can be sent from all the ONUs to the OLT in a DBA cycle
     * @param report_array the report array to be handled
     */
    public static void limitedAllocation(long dba_size, ReportArray report_array) {
	//int limited_sharing = (int) (dba_size / EponSimulator.num_onus);
	int limited_sharing = 120000 + EponSimulator.REPORT_SIZE;
	Report report;
	for (int id = 0; id < EponSimulator.num_onus; id++) {
	    report = report_array.getReport(id);
	    if (report != null) {
		report.onu_tsize = report.onu_qsize + EponSimulator.REPORT_SIZE > limited_sharing ? limited_sharing : report.onu_qsize + EponSimulator.REPORT_SIZE;
		report_array.overall_tsize += report.onu_tsize;
	    } else {
		report = new Report(id, 0);
		report.onu_tsize = EponSimulator.REPORT_SIZE;
		report_array.addReport(report);
	    }
	}
    }

    /**
     * Allocates to each ONU the bandwidth requested as long as it does not exceed the fixed limit but 
     the excess bandwidth not used by underloaded ONUs is fairly distributed among overloaded ONUs.
     *
     * @param dba_size maximum amount of data that can be sent from all the ONUs to the OLT in a DBA cycle
     * @param report_array the report array to be handled
     */
    public static void limitedExcessDistributionAllocation(long dba_size, ReportArray report_array) {
	//int limited_sharing = (int) (dba_size / EponSimulator.num_onus);
	int limited_sharing = 120000 + EponSimulator.REPORT_SIZE;
	int overall_excess = 0;
	int num_overloaded_onus = 0;
	Report report;
	for (int id = 0; id < EponSimulator.num_onus; id++) {
	    report = report_array.getReport(id);
	    if (report != null) {
		if (report.onu_qsize + EponSimulator.REPORT_SIZE < limited_sharing) {
		    report.onu_tsize = report.onu_qsize + EponSimulator.REPORT_SIZE;
		    overall_excess += limited_sharing - report.onu_tsize;
		} else {
		    report.onu_tsize = limited_sharing;
		    num_overloaded_onus++;
		}
		report_array.overall_tsize += report.onu_tsize;
	    } else {
		report = new Report(id, 0);
		report.onu_tsize = EponSimulator.REPORT_SIZE;
		report_array.addReport(report);
		overall_excess += limited_sharing - report.onu_tsize;
	    }
	}
	if (num_overloaded_onus > 0 && overall_excess > 0) {
	    int excess_sharing = limited_sharing + overall_excess / num_overloaded_onus;
	    for (int id = 0; id < EponSimulator.num_onus; id++) {
		report = report_array.getReport(id);
		if (report.onu_tsize == limited_sharing) {
		    report.onu_tsize = report.onu_qsize + EponSimulator.REPORT_SIZE > excess_sharing ? excess_sharing : report.onu_qsize + EponSimulator.REPORT_SIZE;
		    report_array.overall_tsize += report.onu_tsize - limited_sharing;
		}
	    }
	}
    }
    
    /**
     * Shares the available bandwidth among all active ONUs according to their bandwidth requests in a proportional manner.
     *
     * @param dba_size maximum amount of data that can be sent from all the ONUs to the OLT in a DBA cycle
     * @param report_array the report array to be handled
     */
    public static void proportionalAllocation(long dba_size, ReportArray report_array) {
	long remaining_dba_size = dba_size - (EponSimulator.num_onus - report_array.num_active_onus) * EponSimulator.REPORT_SIZE;
	Report report;
	for (int id = 0; id < EponSimulator.num_onus; id++) {
	    report = report_array.getReport(id);
	    if (report != null) {
		report.onu_tsize = (int) Math.round((double) report.onu_qsize * remaining_dba_size / report_array.overall_qsize);
		if (report.onu_tsize < EponSimulator.REPORT_SIZE) {
		    report.onu_tsize = EponSimulator.REPORT_SIZE;
		}
		report_array.overall_tsize += report.onu_tsize;
	    } else {
		report = new Report(id, 0);
		report.onu_tsize = EponSimulator.REPORT_SIZE;
		report_array.addReport(report);
	    }
	}
    }
}
