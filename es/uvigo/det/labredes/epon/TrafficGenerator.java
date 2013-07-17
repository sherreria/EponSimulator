package es.uvigo.det.labredes.epon;

/**
 * This class simulates the arrival of a stream of packets.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
abstract public class TrafficGenerator {
    /**
     * The bit rate (in b/s).
     */
    public long bit_rate;
    /**
     * The size of arriving packets (in bits).
     */
    public int packet_size;
    /**
     * The packet rate (in packets/s).
     */
    public double packet_rate;
    /**
     * The instant at which the last packet arrived (in seconds).
     */
    public double arrival_time;

    /**
     * Creates a new random traffic generator.
     *
     * @param brate bit rate (in b/s)
     * @param psize size of arriving packets (in bits)
     */
    public TrafficGenerator(long brate, int psize) {
	bit_rate = brate;
	packet_size = psize;
	packet_rate = (double) bit_rate / packet_size;
	arrival_time = 0.0;
    }

    /**
     * Sets the size of arriving packets.
     *
     * @param psize size of arriving packets (in bits)
     */
    public void setPacketSize(int psize) {
	packet_size = psize;
	packet_rate = (double) bit_rate / packet_size;
    }

    /**
     * Sets the bit rate of this random traffic generator.
     *
     * @param brate bit rate (in b/s)
     */
    public void setRate(long brate) {
	bit_rate = brate;
	packet_rate = (double) bit_rate / packet_size;
    }

    /**
     * Returns the instant at which the next packet arrives.
     *
     * @return instant at which the next packet arrives (in seconds)
     */
    abstract public double getNextArrival();
}
