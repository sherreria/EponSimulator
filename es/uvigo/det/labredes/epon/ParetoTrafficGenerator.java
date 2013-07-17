package es.uvigo.det.labredes.epon;

import java.util.Random;

/**
 * This class extends TrafficGenerator class to simulate Pareto traffic.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
public class ParetoTrafficGenerator extends TrafficGenerator {
    private Random rng;
    private double alpha;

    /**
     * Creates a new Pareto traffic generator.
     *
     * @param brate bit rate (in b/s)
     * @param psize size of arriving packets (in bits)
     */
    public ParetoTrafficGenerator(long brate, int psize) {
	super(brate, psize);
	alpha = 2.5;
	rng = new Random();
    }

    /**
     * Sets the shape parameter (alpha) of this Pareto traffic generator.
     *
     * @param a value for the shape parameter (alpha)
     */
    public void setAlpha(double a) {
	alpha = a;
    }

    /**
     * Sets the seed of this Pareto traffic generator.
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
	double xm = (alpha - 1) / alpha / packet_rate;
	double rand = rng.nextDouble();
	arrival_time += xm / Math.pow(rand, 1 / alpha);	
	return arrival_time;
    }    
}