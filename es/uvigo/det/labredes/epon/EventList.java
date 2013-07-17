package es.uvigo.det.labredes.epon;

import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.*;

/**
 * This class implements a discrete sequence of events sorted by event time.
 *
 * @author Sergio Herreria-Alonso 
 * @version 1.0
 */
public class EventList {
    private List<Event> list;
    private double current_time;
    private double end_time;

    /**
     * Creates a new list of events.
     *
     * @param t the end of time
     */
    public EventList(double t) {
	list = new ArrayList<Event>();
	current_time = 0.0;
	end_time = t;
    }

    /**
     * Adds the specified event to this event list at the right position.
     *
     * @param event the Event to be added
     * @return true if the specified event is correctly added to this event list
     */
    public boolean addEvent(Event event) {
	if (event.time < current_time) {
	    EponSimulator.printError("Trying to add an event with an invalid event time!");
	}
	if (event.time > end_time) {
	    return false;
	}

	int i = 0;
	int list_size = list.size();
	while (i < list_size && event.compareTo(list.get(i)) <= 0) {
	    i++;
	}
	if (i == list_size) {
	    return list.add(event);
	}
	list.add(i,event);
	return true;
    }

    /**
     * Returns the time of the last event handled.
     *
     * @return the time of the last event handled
     */
    public double getCurrentTime() {
	return current_time;
    }

    /**
     * Returns the next event in this event list.
     *
     * @param remove if true the event is removed from the list
     * @return the next event in this event list or null if the list is empty
     */
    public Event getNextEvent(boolean remove) {
	Event event;
	try {
	    event = remove ? list.remove(0) : list.get(0);
	} catch (Exception e) {
	    event = null;
	}
	return event;
    }

    /**
     * Invokes the method that handles the specified event on the corresponding object.
     *
     * @param event the Event to be handled
     */
    public void handleEvent(Event event) {
	current_time = event.time;
	try {
	    Method handler_method = event.handler.getClass().getMethod(event.handler_method_name, event.getClass());
	    try {
		handler_method.invoke(event.handler, event);
	    } catch (Exception e) {
		EponSimulator.printError("Handler method invoke exception: " + event.handler_method_name + ": " + e.getMessage());
	    }
	} catch (Exception e) {
	    EponSimulator.printError("Handler method exception: " + e.getMessage());
	}
    }

    /**
     * Prints on standard output a message for each event contained in this event list.
     */
    public void printEvents() {
	Event event;
	for (int i = 0; i < list.size(); i++) {
	    list.get(i).printEvent();
	}
    }

    /**
     * Removes the specified event from this event list. If the list does not contain the event, it is unchanged.
     *
     * @param event the Event to be removed
     * @return true if this event list contained the specified event 
     */
    public boolean removeEvent(Event event) {
	return list.remove(event);
    }
}
