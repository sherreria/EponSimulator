package es.uvigo.det.labredes.epon;

/**
 * This class extends Event class to represent the transmission of one packet from the ONUs to the OLT.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
public class PacketTransmissionEvent extends Event<ONU> {
    /**
     * The size of the transmitted packet.
     */
    public int packet_size;

    /**
     * Creates a new event representing the transmission of one packet from the specified ONU to the OLT.
     *
     * @param time   instant at which the ONU ends packet transmission
     * @param onu    onu that transmits the packet
     * @param method name of the method that handles this packet transmission
     * @param psize  size of the transmitted packet
     */
    public PacketTransmissionEvent (double time, ONU onu, String method, int psize) {
	super(time, onu, method);
	packet_size = psize;
    }

    /**
     * Prints on standard output a message describing this packet transmission event.
     */
    public void printEvent() {
	System.out.format("%.9f ONU %d PacketTransmissionEvent %d %d %n", time, handler.onu_id, packet_size, handler.getQueueSize());
    }
}