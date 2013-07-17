package es.uvigo.det.labredes.epon;

/**
 * This class extends Event class to represent the drop of a new arriving packet at the ONUs.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
public class PacketDropEvent extends Event<ONU> {
    /**
     * The size of the packet discarded.
     */
    public int packet_size;

    /**
     * Creates a new event representing the drop of a new arriving packet at the specified ONU.
     *
     * @param time   instant at which the new arriving packet is discarded at the ONU
     * @param onu    onu that discards the new packet
     * @param method name of the method that handles this packet drop
     * @param psize  size of the packet discarded
     */
    public PacketDropEvent (double time, ONU onu, String method, int psize) {
	super(time, onu, method);
	packet_size = psize;
    }

    /**
     * Prints on standard output a message describing this packet drop event.
     */
    public void printEvent() {
	System.out.format("%.9f ONU %d PacketDropEvent %d %d %n", time, handler.onu_id, packet_size, handler.getQueueSize());
    }
}
