package es.uvigo.det.labredes.epon;

/**
 * This class extends Event class to represent the transmission of traffic reports from the ONUs to the OLT.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
public class TrafficReportEvent extends Event<ONU> {
    /**
     * Creates a new event representing the transmission of a traffic report from the specified ONU to the OLT.
     *
     * @param time   instant at which the ONU ends transmission of the traffic report 
     * @param onu    ONU that sends the traffic report
     * @param method name of the method that handles traffic reports
     */
    public TrafficReportEvent (double time, ONU onu, String method) {
	super(time, onu, method);
    }

    /**
     * Prints on standard output a message describing this traffic report event.
     */
    public void printEvent() {
	System.out.format("%.9f ONU %d TrafficReportEvent%n", time, handler.onu_id);
    }
}
