package es.uvigo.det.labredes.epon;

import java.io.FileReader;
import java.io.BufferedReader;

/**
 * This class simulates the upstream channel of EPON (Ethernet Passive Optical Network) systems.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
public final class EponSimulator {
    /* Simulation parameters */
    /**
     * Length of the simulation (in seconds). Default = 1.
     */
    public static double simulation_len = 1;
    /**
     * Seed for the simulation. Default = 1.
     */
    public static long simulation_seed = 1;
    /**
     * If true a message for each simulated event is printed on standard output. Default = false.
     */
    public static boolean simulation_verbose = false;
    /**
     * Event handler.
     */
    public static EventList handler;

    /* EPON parameters */
    /**
     * Uplink capacity (in b/s). Default = 10000000000.
     */
    public static long uplink_capacity = 10000000000L;
    /**
     * Length of DBA cycle (in seconds). Default = 0.0015.
     */
    public static double dba_cycle = 1.5e-3;
    /**
     * Guard time required between the transmission slots of each pair of ONUs (in seconds). Default = 0.000001.
     */
    public static double dba_guard_time = 1e-6;
    /**
     * DBA sizing algorithm (fixed|fair|proportional|gated|limited|limitedExcess). Default = fixed.
     */
    public static String dba_algorithm = "fixed";
    /**
     * Number of ONUs. Default = 1.
     */
    public static int num_onus = 1;
    /**
     * If true ONUs can enter into a low power mode that makes their transmitter sleep when there is no traffic to transmit (doze mode). Default = true.
     */
    public static boolean onu_energy_aware = true;
    /**
     * Maximum number of packets that can be stored in each ONU upstream queue. Default = 0 (infinite capacity).
     */
    public static int onu_maximum_queue = 0;
    /**
     * Minimum number of packets in the upstream queue required to reactivate an ONU in the doze mode. If 0, a dynamic algorithm that adjusts the queue threshold with the goal of maintaining packet delay around a given target value is applied. Default = 10.
     */
    public static int onu_queue_threshold = 10;
    /**
     * Target delay value for the dynamic queue threshold algorithm (if onu_queue_threshold = 0). Default = 0.005.
     */
    public static double onu_dynamic_target_delay = 5e-3;
    /**
     * Parameter that controls the speed of adjustment to the target delay value (if onu_queue_threshold = 0). Default = 1.
     */
    public static int onu_dynamic_gamma = 1;
    /**
     * Time required to power up ONU transmitters (in seconds). Default = 0.002.
     */
    public static double onu_wakeup_len = 2e-3;
    /**
     * Upper limit on the time an ONU can remain continously in the doze mode (in seconds). Default = 0.05.
     */
    public static double onu_refresh_to = 50e-3;
    /**
     * Doze mode to active mode energy consumption ratio. Default = 0.3.
     */
    public static double onu_doze_mode_energy_ratio = 0.3;
    /**
     * Size of traffic report messages (512 bits).
     */
    public static final int REPORT_SIZE = 8 * 64;

    private EponSimulator() {}

    /**
     * Prints on standard error the specified message and exits.
     */
    public static void printError(String s) {
	System.err.println("ERROR: " + s);
	System.exit(1);
    }

    /**
     * Main method.
     * Usage: java EponSimulator [-n num_onus] [-l simulation_length (s)] [-s simulation_seed] [-t traffic_rate (b/s)] [-p packet_size (bytes)] [-g traffic_distribution (deterministic|poisson|pareto)] [-q queue_threshold (packets)] [-m maximum_queue_size (packets)] [-c uplink_capacity (b/s)] [-d dba_cycle (s)] [-a dba_algorithm (fixed|fair|proportional|gated|limited|limitedExcess)] [-w onu_wakeup (s)] [-r onu_refresh_timeout (s)] [-e onu_doze_mode_energy_ratio] [-f traffic_profiles] [-u] [-v]
     */
    public static void main(String[] args) {
	// Default traffic parameters
	long traffic_rate = 100000000; // in b/s
	int packet_size = 1500; // in bytes
	String traffic_distribution = "pareto";
	String traffic_profile = "";

	// Arguments parsing
	for (int i = 0; i < args.length; i++) {
	    if (args[i].equals("-n")) {
		try {
		    num_onus = Integer.parseInt(args[i+1]);
		} catch (NumberFormatException e) {
		    printError("Invalid number of onus!");
		}
		i++;
	    } else if (args[i].equals("-l")) {
		try {
		    simulation_len = Double.parseDouble(args[i+1]);
		} catch (NumberFormatException e) {
		    printError("Invalid simulation length!");
		}
		i++;
	    } else if (args[i].equals("-s")) {
		try {
		    simulation_seed = Integer.parseInt(args[i+1]);
		} catch (NumberFormatException e) {
		    printError("Invalid simulation seed!");
		}
		i++;
	    } else if (args[i].equals("-t")) {
		try {
		    traffic_rate = Long.parseLong(args[i+1]);
		} catch (NumberFormatException e) {
		    printError("Invalid traffic rate!");
		}
		i++;
	    } else if (args[i].equals("-p")) {
		try {
		    packet_size = Integer.parseInt(args[i+1]);
		} catch (NumberFormatException e) {
		    printError("Invalid packet size!");
		}
		i++;
	    } else if (args[i].equals("-g")) {
		if (args[i+1].equals("deterministic") || args[i+1].equals("poisson") || args[i+1].equals("pareto")) {
		    traffic_distribution = args[i+1];
		} else {
		    printError("Invalid traffic distribution!");
		}
		i++;
	    } else if (args[i].equals("-q")) {
		try {
		    onu_queue_threshold = Integer.parseInt(args[i+1]);
		} catch (NumberFormatException e) {
		    printError("Invalid queue threshold!");
		}
		i++;
	    } else if (args[i].equals("-m")) {
		try {
		    onu_maximum_queue = Integer.parseInt(args[i+1]);
		} catch (NumberFormatException e) {
		    printError("Invalid maximum queue size!");
		}
		i++;
	    } else if (args[i].equals("-c")) {
		try {
		    uplink_capacity = Long.parseLong(args[i+1]);
		} catch (NumberFormatException e) {
		    printError("Invalid uplink capacity!");
		}
		i++;
	    } else if (args[i].equals("-d")) {
		try {
		    dba_cycle = Double.parseDouble(args[i+1]);
		} catch (NumberFormatException e) {
		    printError("Invalid DBA cycle!");
		}
		i++;
	    } else if (args[i].equals("-a")) {
		if (args[i+1].equals("fixed") || args[i+1].equals("fair") || args[i+1].equals("proportional") || args[i+1].equals("gated") || args[i+1].equals("limited") || args[i+1].equals("limitedExcess")) {
		    dba_algorithm = args[i+1];
		} else {
		    printError("Invalid DBA algorithm!");
		}
		i++;
	    } else if (args[i].equals("-w")) {
		try {
		    onu_wakeup_len = Double.parseDouble(args[i+1]);
		} catch (NumberFormatException e) {
		    printError("Invalid ONU wakeup length!");
		}
		i++;
	    } else if (args[i].equals("-r")) {
		try {
		    onu_refresh_to = Double.parseDouble(args[i+1]);
		} catch (NumberFormatException e) {
		    printError("Invalid ONU refresh timeout!");
		}
		i++;
	    } else if (args[i].equals("-e")) {
		try {
		    onu_doze_mode_energy_ratio = Double.parseDouble(args[i+1]);
		} catch (NumberFormatException e) {
		    printError("Invalid ONU doze mode energy ratio!");
		}
		i++;
	    } else if (args[i].equals("-f")) {
		traffic_profile = args[i+1];
		i++;
	    } else if (args[i].equals("-u")) {
		onu_energy_aware = false;
	    } else if (args[i].equals("-v")) {
		simulation_verbose = true;
	    } else {
		printError("Unknown argument: " + args[i] + "\nUsage: java EponSimulator [-n num_onus] [-l simulation_length (s)] [-s simulation_seed] [-t traffic_rate (b/s)] [-p packet_size (bytes)] [-g traffic_distribution (deterministic|poisson|pareto)] [-q queue_threshold (packets)] [-m maximum_queue_size (packets)] [-c uplink_capacity (b/s)] [-d dba_cycle (s)] [-a dba_algorithm (fixed|fair|proportional|gated|limited|limitedExcess)] [-w onu_wakeup (s)] [-r onu_refresh_timeout (s)] [-e onu_doze_mode_energy_ratio] [-f traffic_profiles] [-u] [-v]");
	    }
	}

	// Event handler initialization
	handler = new EventList(simulation_len);

	// OLT initialization
	OLT olt = new OLT();

	// Traffic profiles initialization
	String[] onu_traffic_distribution = new String[num_onus];
	long[] onu_traffic_rate = new long[num_onus];
	int[] onu_packet_size = new int[num_onus];	
	if (traffic_profile.isEmpty()) {
	    for (int id = 0; id < num_onus; id++) {
		onu_traffic_distribution[id] = traffic_distribution;
		onu_traffic_rate[id] = traffic_rate;
		onu_packet_size[id] = 8 * packet_size;
	    }
	} else {
	    try {
		BufferedReader file = new BufferedReader(new FileReader(traffic_profile));
		for (int id = 0; id < num_onus; id++) {
		    try {
			String line = file.readLine();
			String[] line_fields = line.split("\\s+");
			onu_traffic_distribution[id] = line_fields[0];
			onu_traffic_rate[id] = Long.parseLong(line_fields[1]);
			onu_packet_size[id] = 8 * Integer.parseInt(line_fields[2]);
		    } catch (Exception e) {
			printError("Invalid traffic profile: Error in ONU " + id + "!");
		    }
		}
		file.close();
	    } catch (Exception e) {
		printError("Invalid traffic profile: File not found!");
	    }
	}

	// ONUs initialization
	TrafficGenerator tg = null;
	ONU[] onus = new ONU[num_onus];
        for (int id = 0; id < num_onus; id++) {
	    if (onu_traffic_distribution[id].equals("deterministic")) {
		tg = new DeterministicTrafficGenerator(onu_traffic_rate[id], onu_packet_size[id]);
	    } else if (onu_traffic_distribution[id].equals("poisson")) {
		tg = new PoissonTrafficGenerator(onu_traffic_rate[id], onu_packet_size[id]);
		((PoissonTrafficGenerator) tg).setSeed(simulation_seed + id);
	    } else if (onu_traffic_distribution[id].equals("pareto")) {
		tg = new ParetoTrafficGenerator(onu_traffic_rate[id], onu_packet_size[id]);
		((ParetoTrafficGenerator) tg).setSeed(simulation_seed + id);
	    } else {
		printError("Invalid traffic distribution for ONU " + id + "!");
	    }
            onus[id] = new ONU(id, olt, tg);
        }

	// Events processing
	Event event;
        while ((event = handler.getNextEvent(true)) != null) {
	    handler.handleEvent(event);
	}

	// ONUs statistics
	for (int id = 0; id < num_onus; id++) {
	    onus[id].printStatistics();
	}
    }
}