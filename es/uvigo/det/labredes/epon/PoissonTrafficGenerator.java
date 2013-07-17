package es.uvigo.det.labredes.epon;

import java.util.Random;

/**
 * This class extends TrafficGenerator class to simulate Poisson traffic.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
public class PoissonTrafficGenerator extends TrafficGenerator {
    private Random rng;

    /**
     * Creates a new Poisson traffic generator.
     *
     * @param brate bit rate (in b/s)
     * @param psize size of arriving packets (in bits)
     */
    public PoissonTrafficGenerator(long brate, int psize) {
	super(brate, psize);
	rng = new Random();
    }

    /**
     * Sets the seed of this Poisson traffic generator.
     *
     * @param seed initial seed
     */
    public void setSeed(long seed) {
	rng.setSeed(seed);
    }

    /**
     * Returns the instant at which the next packet arrives.
     *
     * @return instant at which the next packet arrives (in seconds)
     */
    public double getNextArrival() {
	double rand = rng.nextDouble();
	arrival_time += -1.0 * Math.log(rand) / packet_rate;
	return arrival_time;
    }    
}