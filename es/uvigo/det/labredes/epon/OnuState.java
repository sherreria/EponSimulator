package es.uvigo.det.labredes.epon;

/**
 * This type enums all the possible states of energy-aware ONUs.
 */
public enum OnuState {
    /**
     * The ONU transmitter is idle (doze mode).
     */
    OFF, 
	/**
	 * The ONU transmitter remains idle until it can be properly powered up (doze mode).
	 */
	OFF_WAIT, 
	/**
	 * The ONU transmitter is being powered up (active mode).
	 */
	TRANSITION_TO_ON, 
	/**
	 * The ONU transmitter is fully operative (active mode).
	 */
	ON;
}
