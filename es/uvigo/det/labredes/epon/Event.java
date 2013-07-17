package es.uvigo.det.labredes.epon;

/**
 * This class implements the events for the EPON simulator.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
abstract public class Event<T> implements Comparable {
    /**
     * The object responsible for handling the event.
     */
    public T handler;
    /**
     * The name of the method that handles the event.
     */
    public String handler_method_name;
    /**
     * The instant at which the event occurs.
     */
    public double time;

    /**
     * Creates a new event ocurring at the specified time.
     *
     * @param time   instant at which this event occurs
     * @param object object responsible for handling this event
     * @param method name of the method that handles this event
     */
    public Event (double time, T object, String method) {
	this.time = time;
	handler = object;
	handler_method_name = method;
    }

    /**
     * Compares two events based on the instant at which each event occurs.
     *
     * @param event the Event to be compared
     * @return the value 0 if both the argument event and this event occur at the same instant; a value less than 0 if this event is later than the event argument; and a value greater than 0 if this event is earlier than the event argument
     */
    public int compareTo(Object event) {
        return (int) (10e9 * (((Event) event).time - this.time));
    }

    /**
     * Prints on standard output a message describing this event.
     */
    abstract public void printEvent();
}
