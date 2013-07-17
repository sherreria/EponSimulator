package es.uvigo.det.labredes.epon;

/**
 * This class extends Event class to represent state transitions at the ONUs.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
public class StateTransitionEvent extends Event<ONU> {
    /**
     * The new state of the ONU.
     */
    public OnuState new_state;

    /**
     * Creates a new event representing a state transition at the specified ONU.
     *
     * @param time   instant at which the ONU changes its state
     * @param onu    onu that changes its state
     * @param method name of the method that handles this state transition
     * @param state  new state of the ONU
     */
    public StateTransitionEvent (double time, ONU onu, String method, OnuState state) {
	super(time, onu, method);
	new_state = state;
    }

    /**
     * Prints on standard output a message describing this state transition event.
     */
    public void printEvent() {
	System.out.format("%.9f ONU %d StateTransitionEvent %s %n", time, handler.onu_id, new_state);
    }
}