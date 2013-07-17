package es.uvigo.det.labredes.epon;

/**
 * This class extends Event class to represent the arrival of a new packet at the ONUs.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
public class PacketArrivalEvent extends Event<ONU> {
    /**
     * The size of the arriving packet.
     */
    public int packet_size;

    /**
     * Creates a new event representing the arrival of a new packet at the specified ONU.
     *
     * @param time   instant at which the new packet arrives at the ONU
     * @param onu    onu that receives the new packet
     * @param method name of the method that handles this packet arrival
     * @param psize  size of the arriving packet
     */
    public PacketArrivalEvent (double time, ONU onu, String method, int psize) {
	super(time, onu, method);
	packet_size = psize;
    }

    /**
     * Prints on standard output a message describing this packet arrival event.
     */
    public void printEvent() {
	System.out.format("%.9f ONU %d PacketArrivalEvent %d %d %n", time, handler.onu_id, packet_size, handler.getQueueSize());
    }
}
