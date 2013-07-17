package es.uvigo.det.labredes.epon;

/**
 * This class extends Event class to represent the beginning of the transmission slots from the ONUs to the OLT.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
public class TransmissionSlotEvent extends Event<ONU> {
    /**
     * The maximum amount of data allowed to transmit in the transmission slot.
     */
    public int data_amount;

    /**
     * Creates a new event representing the beginning of a transmission slot from the specified ONU to the OLT.
     *
     * @param time   instant at which the transmission slot starts
     * @param onu    onu that handles the transmission slot
     * @param method name of the method that handles the transmission slot
     * @param amount maximum amount of data allowed to transmit in the transmission slot
     */
    public TransmissionSlotEvent (double time, ONU onu, String method, int amount) {
	super(time, onu, method);
	data_amount = amount;
    }

    /**
     * Prints on standard output a message describing this transmission slot event.
     */
    public void printEvent() {
	System.out.format("%.9f ONU %d TransmissionSlotEvent %d %d %n", time, handler.onu_id, data_amount, handler.getQueueSize());
    }
}