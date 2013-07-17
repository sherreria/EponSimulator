package es.uvigo.det.labredes.epon;

/**
 * This class extends TrafficGenerator class to simulate deterministic traffic.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
public class DeterministicTrafficGenerator extends TrafficGenerator {
    /**
     * Creates a new deterministic traffic generator.
     *
     * @param brate bit rate (in b/s)
     * @param psize size of arriving packets (in bits)
     */
    public DeterministicTrafficGenerator(long brate, int psize) {
	super(brate, psize);
    }

    /**
     * Returns the instant at which the next packet arrives.
     *
     * @return instant at which the next packet arrives (in seconds)
     */
    public double getNextArrival() {
	arrival_time += 1.0 / packet_rate;
	return arrival_time;
    }    
}