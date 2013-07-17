package es.uvigo.det.labredes.epon;

import java.util.Map;
import java.util.HashMap;

/**
 * This class simulates the ONUs of the EPON. 
 * The simulated ONUs can enter into a low power mode that makes their transmitter sleep when there is no traffic to transmit (doze mode).
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
public class ONU {
    /**
     * The ONU identifier.
     */
    public int onu_id;
    /**
     * The OLT of the EPON.
     */
    private OLT epon_olt;
    /**
     * The ONU traffic generator.
     */
    private TrafficGenerator traffic_generator;
    /**
     * The ONU upstream queue.
     */
    private EventList upstream_queue;
    /**
     * The amount of data stored in the ONU upstream queue.
     */
    private int current_qsize;
    /**
     * The maximum amount of data that can be stored in the ONU upstream queue.
     */
    private int maximum_qsize;
    /**
     * The minimum amount of data stored in the upstream queue required to active the ONU.
     */
    private int queue_threshold;
    /**
     * The amount of data that the ONU can transmit in the current DBA cycle.
     */
    private int available_tsize;
    /**
     * The ONU state.
     */
    private OnuState state;
    /**
     * The next refresh timeout event.
     */
    private Event refresh_to_event;
    /**
     * The time required to transmit the traffic report from the ONU to the OLT.
     */
    private double traffic_report_transmission_time;

    // Statistics variables
    private double last_state_transition_time;
    private Map<OnuState, Double> time_in_states;
    private int packets_received, packets_sent, packets_dropped;
    private double packets_delay;

    private int current_dba_packets_sent;
    private double current_dba_packets_delay;

    /**
     * Creates a new ONU with the specified identifier. 
     * The ONU is connected to the specified OLT. 
     * The ONU upstream traffic is simulated with the specified traffic generator.
     *
     * @param id  the ONU identifier
     * @param olt the OLT of the EPON
     * @param tg  the traffic generator
     */
    public ONU(int id, OLT olt, TrafficGenerator tg) {
        onu_id = id;
        epon_olt = olt;
        epon_olt.registerONU(id, this);
	traffic_generator = tg;
	upstream_queue = new EventList(EponSimulator.simulation_len);
	current_qsize = 0;
	maximum_qsize = tg.packet_size * EponSimulator.onu_maximum_queue;
	queue_threshold = tg.packet_size * EponSimulator.onu_queue_threshold;
	available_tsize = EponSimulator.REPORT_SIZE;
	traffic_report_transmission_time = (double) EponSimulator.REPORT_SIZE / EponSimulator.uplink_capacity;

	last_state_transition_time = 0.0;
	time_in_states = new HashMap<OnuState, Double>();
	for (OnuState st : OnuState.values()) {
	    time_in_states.put(st, 0.0);
	}
	packets_received = packets_sent = packets_dropped = 0;
	packets_delay = 0.0;

	state = EponSimulator.onu_energy_aware ? OnuState.OFF : OnuState.ON;
	EponSimulator.handler.addEvent(new StateTransitionEvent (0.0, this, "handleStateTransitionEvent", state));
	EponSimulator.handler.addEvent(new PacketArrivalEvent (traffic_generator.getNextArrival(), this, "handlePacketArrivalEvent", traffic_generator.packet_size));
	if (state == OnuState.ON) {
	    double first_transmission_slot_time = onu_id * EponSimulator.dba_cycle / EponSimulator.num_onus;
	    EponSimulator.handler.addEvent(new TransmissionSlotEvent (first_transmission_slot_time, this, "handleTransmissionSlotEvent", EponSimulator.REPORT_SIZE));
	}
    }

    /**
     * Returns the current size of the upstream queue of this ONU.
     *
     * @return the current size of the upstream queue of this ONU
     */
    public int getQueueSize() {
	return current_qsize;
    }

    /**
     * Handles the specified transmission slot event.
     *
     * @param event the TransmissionSlotEvent to be handled
     */
    public void handleTransmissionSlotEvent(TransmissionSlotEvent event) {
	available_tsize = event.data_amount;
	if (EponSimulator.simulation_verbose) {
	    event.printEvent();
	}

	if (state == OnuState.ON) {
	    PacketArrivalEvent next_packet_to_transmit = (PacketArrivalEvent) (upstream_queue.getNextEvent(false));
	    int next_packet_size = next_packet_to_transmit == null ? 0 : next_packet_to_transmit.packet_size;
	    if (next_packet_size > 0 && next_packet_size <= available_tsize - EponSimulator.REPORT_SIZE) {
		double next_packet_transmission_time = event.time + (double) next_packet_size / EponSimulator.uplink_capacity;
		EponSimulator.handler.addEvent(new PacketTransmissionEvent (next_packet_transmission_time, this, "handlePacketTransmissionEvent", next_packet_size));
	    } else if (EponSimulator.REPORT_SIZE <= available_tsize) {
		double next_traffic_report_time = event.time + traffic_report_transmission_time;	    
		EponSimulator.handler.addEvent(new TrafficReportEvent (next_traffic_report_time, this, "handleTrafficReportEvent"));
	    }
	}
    }

    /**
     * Handles the specified traffic report event.
     *
     * @param event the TrafficReportEvent to be handled
     */
    public void handleTrafficReportEvent(TrafficReportEvent event) {
	epon_olt.registerTrafficReport(new Report(onu_id, current_qsize));
	available_tsize -= EponSimulator.REPORT_SIZE;
	if (available_tsize < 0) {
	    EponSimulator.printError("Trying to handle an invalid traffic report!");
	}
	if (EponSimulator.simulation_verbose) {
	    event.printEvent();
	}

	if (EponSimulator.onu_energy_aware && state == OnuState.ON && current_qsize == 0) {
	    EponSimulator.handler.addEvent(new StateTransitionEvent (event.time, this, "handleStateTransitionEvent", OnuState.OFF));
	}
    }

    /**
     * Handles the specified packet arrival event.
     *
     * @param event the PacketArrivalEvent to be handled
     */
    public void handlePacketArrivalEvent(PacketArrivalEvent event) {
	packets_received++;
	if (maximum_qsize == 0 || current_qsize + event.packet_size <= maximum_qsize) {
	    current_qsize += event.packet_size;
	    upstream_queue.addEvent(event);
	    if (EponSimulator.simulation_verbose) {
		event.printEvent();
	    }
	    if (state == OnuState.OFF && current_qsize >= queue_threshold) {
		OnuState nextState = EponSimulator.dba_algorithm.equals("gated") || EponSimulator.dba_algorithm.equals("limited") || EponSimulator.dba_algorithm.equals("limitedExcess") ? OnuState.TRANSITION_TO_ON : OnuState.OFF_WAIT;
		EponSimulator.handler.addEvent(new StateTransitionEvent (event.time, this, "handleStateTransitionEvent", nextState));
	    }
	} else {
	    EponSimulator.handler.addEvent(new PacketDropEvent (event.time, this, "handlePacketDropEvent", event.packet_size));
	}

	double next_packet_arrival_time = traffic_generator.getNextArrival();
	EponSimulator.handler.addEvent(new PacketArrivalEvent (next_packet_arrival_time, this, "handlePacketArrivalEvent", traffic_generator.packet_size));
    }

    /**
     * Handles the specified packet drop event.
     *
     * @param event the PacketDropEvent to be handled
     */
    public void handlePacketDropEvent(PacketDropEvent event) {
	packets_dropped++;
	if (EponSimulator.simulation_verbose) {
	    event.printEvent();
	}
    }

    /**
     * Handles the specified packet transmission event.
     *
     * @param event the PacketTransmissionEvent to be handled
     */
    public void handlePacketTransmissionEvent(PacketTransmissionEvent event) {
	current_qsize -= event.packet_size;
	available_tsize -= event.packet_size;
	if (current_qsize < 0 || available_tsize < 0) {
	    EponSimulator.printError("Trying to handle an invalid packet transmission!");
	}
	packets_sent++;
	PacketArrivalEvent packet_transmitted = (PacketArrivalEvent) (upstream_queue.getNextEvent(true));
	packets_delay += event.time - packet_transmitted.time;
	if (EponSimulator.onu_queue_threshold == 0) {
	    current_dba_packets_sent++;
	    current_dba_packets_delay += event.time - packet_transmitted.time;
	}

	if (EponSimulator.simulation_verbose) {
	    event.printEvent();
	}

	PacketArrivalEvent next_packet_to_transmit = (PacketArrivalEvent) (upstream_queue.getNextEvent(false));
	int next_packet_size = next_packet_to_transmit == null ? 0 : next_packet_to_transmit.packet_size;
	if (next_packet_size > 0 && next_packet_size <= available_tsize - EponSimulator.REPORT_SIZE) {
	    double next_packet_transmission_time = event.time + (double) next_packet_size / EponSimulator.uplink_capacity;
	    EponSimulator.handler.addEvent(new PacketTransmissionEvent (next_packet_transmission_time, this, "handlePacketTransmissionEvent", next_packet_size));
	} else if (EponSimulator.REPORT_SIZE <= available_tsize) {
	    double next_traffic_report_time = event.time + traffic_report_transmission_time;	    
	    EponSimulator.handler.addEvent(new TrafficReportEvent (next_traffic_report_time, this, "handleTrafficReportEvent"));
	} 
    }

    /**
     * Handles the specified state transition event.
     *
     * @param event the StateTransitionEvent to be handled
     */
    public void handleStateTransitionEvent(StateTransitionEvent event) {
	if (event.new_state == OnuState.OFF_WAIT) {
	    double ton_dba = Math.ceil((event.time + EponSimulator.onu_wakeup_len) / EponSimulator.dba_cycle);
	    double next_state_transition_time = ton_dba * EponSimulator.dba_cycle - EponSimulator.onu_wakeup_len;
	    EponSimulator.handler.addEvent(new StateTransitionEvent (next_state_transition_time, this, "handleStateTransitionEvent", OnuState.TRANSITION_TO_ON));
	} else if (event.new_state == OnuState.TRANSITION_TO_ON) {
	    EponSimulator.handler.removeEvent(refresh_to_event);
	    double next_state_transition_time = event.time + EponSimulator.onu_wakeup_len;
	    EponSimulator.handler.addEvent(new StateTransitionEvent (next_state_transition_time, this, "handleStateTransitionEvent", OnuState.ON));
	} else if (event.new_state == OnuState.OFF) {
	    double next_refresh_to_time;
	    if (EponSimulator.dba_algorithm.equals("gated") || EponSimulator.dba_algorithm.equals("limited") || EponSimulator.dba_algorithm.equals("limitedExcess")) {
		next_refresh_to_time = event.time + EponSimulator.onu_refresh_to;
	    } else {
		double refresh_to_dba = Math.floor((event.time + EponSimulator.onu_refresh_to) / EponSimulator.dba_cycle);
		next_refresh_to_time = refresh_to_dba * EponSimulator.dba_cycle - EponSimulator.onu_wakeup_len;
	    }
	    refresh_to_event = new StateTransitionEvent (next_refresh_to_time, this, "handleStateTransitionEvent", OnuState.TRANSITION_TO_ON);
	    EponSimulator.handler.addEvent(refresh_to_event);
	    if (EponSimulator.onu_queue_threshold == 0) {
		double current_dba_avg_packets_delay = current_dba_packets_delay / current_dba_packets_sent;
		if (current_dba_avg_packets_delay > EponSimulator.onu_dynamic_target_delay) {
		    queue_threshold -= EponSimulator.onu_dynamic_gamma * traffic_generator.packet_size;
		    if (queue_threshold < traffic_generator.packet_size) {
			queue_threshold = traffic_generator.packet_size;
		    }
		} else {
		    queue_threshold += EponSimulator.onu_dynamic_gamma * traffic_generator.packet_size;
		}
	    }
	} else if (event.new_state == OnuState.ON && EponSimulator.onu_queue_threshold == 0) {
	    current_dba_packets_sent = 0;
	    current_dba_packets_delay = 0.0;
	}

	time_in_states.put(state, time_in_states.get(state) + event.time - last_state_transition_time);
	state = event.new_state;
	last_state_transition_time = event.time;
	if (EponSimulator.simulation_verbose) {
	    event.printEvent();
	}
    }

    /**
     * Prints on standard output a summary of this ONU statistics.
     */
    public void printStatistics() {
	System.out.format("ONU %d STATISTICS %n", onu_id);
	System.out.format("ONU %d Packets received: %d %n", onu_id, packets_received);
	System.out.format("ONU %d Packets sent: %d %n", onu_id, packets_sent);
	System.out.format("ONU %d Packets dropped: %d %n", onu_id, packets_dropped);
	if (packets_sent > 0) {
	    System.out.format("ONU %d Average packet delay: %.9f %n", onu_id, packets_delay / packets_sent);
	}
	time_in_states.put(state, time_in_states.get(state) + EponSimulator.simulation_len - last_state_transition_time);
	for (OnuState st : OnuState.values()) {
	    System.out.format("ONU %d Time in state %s: %.9f %n", onu_id, st, time_in_states.get(st));
	}
	double time_on = time_in_states.get(OnuState.TRANSITION_TO_ON) + time_in_states.get(OnuState.ON);
	double time_off = time_in_states.get(OnuState.OFF) + time_in_states.get(OnuState.OFF_WAIT);
	double energy_consumption = (time_on + EponSimulator.onu_doze_mode_energy_ratio * time_off) / EponSimulator.simulation_len;
	System.out.format("ONU %d Energy consumption: %.9f %n", onu_id, energy_consumption);
    }
}