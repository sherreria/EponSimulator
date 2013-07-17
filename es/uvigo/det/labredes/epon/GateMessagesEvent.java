package es.uvigo.det.labredes.epon;

/**
 * This class extends Event class to represent the transmission of gate messages from the OLT to the ONUs.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
public class GateMessagesEvent extends Event<OLT> {
    /**
     * Creates a new event representing the transmission of gate messages from the specified OLT to the ONUs.
     *
     * @param time   instant at which all the ONUs have received their gate message
     * @param olt    OLT that sends gate messages
     * @param method name of the method that handles gate messages
     */
    public GateMessagesEvent (double time, OLT olt, String method) {
	super(time, olt, method);
    }
    
    /**
     * Prints on standard output a message describing this gate messages event.
     */
    public void printEvent() {
	System.out.format("%.9f OLT GateMessagesEvent%n", time);
    }
}