package es.uvigo.det.labredes.epon;

/**
 * This class simulates the OLT of the EPON.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
public class OLT {
    /**
     * Array containing the ONUs managed by the OLT.
     */
    private ONU[] epon_onus;
    /**
     * Array containing the traffic reports sent from the ONUs to the OLT.
     */
    private ReportArray report_array;
    /**
     * The maximum amount of data that can be sent from all the ONUs to the OLT in a DBA cycle (in bits).
     */
    private long dba_size;
    
    /**
     * Creates a new OLT.
     */
    public OLT() {
	epon_onus = new ONU[EponSimulator.num_onus];
	report_array = new ReportArray();
	dba_size = (long) Math.floor((EponSimulator.dba_cycle - EponSimulator.dba_guard_time * EponSimulator.num_onus) * EponSimulator.uplink_capacity);

	EponSimulator.handler.addEvent(new GateMessagesEvent (EponSimulator.dba_cycle, this, "handleGateMessagesEvent"));
    }

    /**
     * Handles the specified gate messages event.
     *
     * @param event the GateMessagesEvent to be handled
     */
    public void handleGateMessagesEvent(GateMessagesEvent event) {
	if (EponSimulator.dba_algorithm.equals("fair")) {
	    DBA.fairAllocation(dba_size, report_array);
	} else if (EponSimulator.dba_algorithm.equals("proportional")) {
	    DBA.proportionalAllocation(dba_size, report_array);
	} else if (EponSimulator.dba_algorithm.equals("gated")) {
	    DBA.gatedAllocation(report_array);
	} else if (EponSimulator.dba_algorithm.equals("limited")) {
	    DBA.limitedAllocation(dba_size, report_array);
	} else if (EponSimulator.dba_algorithm.equals("limitedExcess")) {
	    DBA.limitedExcessDistributionAllocation(dba_size, report_array);
	} else {
	    DBA.fixedAllocation(dba_size, report_array);    
	}

	if (EponSimulator.dba_algorithm.equals("gated") || EponSimulator.dba_algorithm.equals("limited") || EponSimulator.dba_algorithm.equals("limitedExcess")) {
	    EponSimulator.dba_cycle = EponSimulator.num_onus * EponSimulator.dba_guard_time + (double) report_array.overall_tsize / EponSimulator.uplink_capacity;
	    System.out.println("New dba_cycle: " + EponSimulator.dba_cycle);
	}

	report_array.sortReports("onu_tsize", false);

	double next_transmission_slot_event = event.time;
	Report report;
	for (int i = 0; i < EponSimulator.num_onus; i++) {
	    report = report_array.getReport(i);
	    EponSimulator.handler.addEvent(new TransmissionSlotEvent (next_transmission_slot_event, epon_onus[report.onu_id], "handleTransmissionSlotEvent", report.onu_tsize));
	    next_transmission_slot_event += (double) report.onu_tsize / EponSimulator.uplink_capacity + EponSimulator.dba_guard_time;
	}
	
	if (EponSimulator.simulation_verbose) {
	    event.printEvent();
	    report_array.printReports();
	}
	report_array.clear();
	
	double next_gate_messages_time = event.time + EponSimulator.dba_cycle;
	EponSimulator.handler.addEvent(new GateMessagesEvent (next_gate_messages_time, this, "handleGateMessagesEvent"));
    }

    /**
     * Registers the ONU with the specified identifier in this OLT.
     *
     * @param id the ONU identifier
     * @param onu the ONU to be registered
     */
    public void registerONU(int id, ONU onu) {
	epon_onus[id] = onu;
    }

    /**
     * Registers the specified traffic report in this OLT.
     *
     * @param report the traffic report to be registered
     */
    public void registerTrafficReport(Report report) {
	report_array.addReport(report);
    }
}
