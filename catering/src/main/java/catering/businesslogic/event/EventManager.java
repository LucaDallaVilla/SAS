package catering.businesslogic.event;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;

import catering.businesslogic.UseCaseLogicException;
import catering.businesslogic.menu.Menu;

/**
 * EventManager handles all operations related to events and services in the
 * CatERing system.
 * It manages event creation, modification, and deletion, as well as service
 * management and menu assignments for services.
 */
public class EventManager {
    private final ArrayList<EventReceiver> eventReceivers;
    private EventSheet currentEvent;

    /**
     * Constructor initializes the event receivers list
     */
    public EventManager() {
        currentEvent = null;
        eventReceivers = new ArrayList<>();
    }

    /**
     * Adds an event receiver to be notified of events changes
     *
     * @param receiver The event receiver to add
     */
    public void addEventReceiver(EventReceiver receiver) {
        if (receiver != null && !eventReceivers.contains(receiver)) {
            eventReceivers.add(receiver);
        }
    }

    /**
     * Removes an event receiver
     *
     * @param receiver The event receiver to remove
     */
    public void removeEventReceiver(EventReceiver receiver) {
        eventReceivers.remove(receiver);
    }

    /**
     * Gets all events in the system
     *
     * @return List of all events
     */
    public ArrayList<EventSheet> getEvents() {
        return EventSheet.loadAllEvents();
    }

    /**
     * Gets the selected event
     *
     * @return Selected event or null if none selected
     */
    public EventSheet getCurrentEvent() {
        return currentEvent;
    }

    /**
     * Sets the selected event
     *
     * @param event Event to select
     */
    public void setCurrentEvent(EventSheet event) {
        this.currentEvent = event;
    }

    /**
     * Creates a new event with the given details
     *
     * @param dateStart       Start date
     * @param dateEnd         End date
     * @param numParticipants Number of participants
     * @param services        List of services for the event
     * @return The newly created event, or null if an error occurs
     */
    public EventSheet fillEventSheet(Date dateStart, Date dateEnd, int numParticipants, ArrayList<ServiceData> services) {
        try {
            EventSheet event = new EventSheet(dateStart, dateEnd, numParticipants, services);

            // Notify all receivers (EventPersistence will persist)
            notifyEventCreated(event);

            // Set as selected event
            this.currentEvent = event;

            return event;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Selects the specified event as the current event
     *
     * @param event The event to select
     */
    public void selectEvent(EventSheet event) {
        this.currentEvent = event;
    }

    /**
     * Creates a new service for the current event
     *
     * @param name      Name of the service
     * @param date      Date of the service
     * @param timeStart Start time
     * @param timeEnd   End time
     * @param location  Location of the service
     * @return The newly created service, or null if an error occurs
     * @throws UseCaseLogicException if no event is currently selected
     */
    public Service createService(String name, Date date, Time timeStart, Time timeEnd, String location)
            throws UseCaseLogicException {
        if (currentEvent == null) {
            String msg = "Cannot create service: no event selected";
            throw new UseCaseLogicException(msg);
        }

        try {
            Service service = new Service();
            service.setName(name);
            service.setDate(date);
            service.setTimeStart(timeStart);
            service.setTimeEnd(timeEnd);
            service.setLocation(location);

            // Notify all receivers (EventPersistence will persist)
            notifyServiceCreated(service);

            // Add to event and set as current service
            currentEvent.addService(service);
            return service;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Modifies an existing event
     *
     * @param eventId ID of the event to modify
     * @param name    New name for the event
     * @param date    New date for the event
     * @return true if modified successfully, false otherwise
     */
    public boolean editEvent(int eventId, String name, Date date) {
        EventSheet event = EventSheet.loadById(eventId);
        if (event != null) {
            event.setName(name);
            event.setDateStart(date);

            // Notify all receivers
            notifyEventModified(event);

            // Update selected event if it's the same one
            if (currentEvent != null && currentEvent.getId() == eventId) {
                this.currentEvent = event;
            }

            // TODO: scrivi le condizioni per applicare la penale
            return false;
        }

        return false;
    }

    /**
     * Deletes a service from the specified event
     *
     * @param currentEvent The event containing the service
     * @param service      The service to delete
     * @return true if deleted successfully, false otherwise
     */
    public boolean deleteService(EventSheet currentEvent, Service service) {
        try {
            if (currentEvent == null) {
                return false;
            }

            if (service == null) {
                return false;
            }

            currentEvent.removeService(service);

            // Notify all receivers (EventPersistence will delete from DB)
            notifyServiceDeleted(service);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Deletes an event and all its associated services
     *
     * @param eventId ID of the event to delete
     * @return true if deleted successfully, false otherwise
     */
    public boolean deleteEvent(int eventId) {
        try {
            EventSheet eventToDelete = EventSheet.loadById(eventId);
            if (eventToDelete == null) {
                return false;
            }

            // Clear references if this was the selected event
            if (currentEvent != null && currentEvent.getId() == eventId) {
                currentEvent = null;
            }

            // Notify all receivers (EventPersistence will delete from DB)
            notifyEventDeleted(eventToDelete);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Assigns a menu to the specified service
     *
     * @param service The service to which the menu will be assigned
     * @param menu    The menu to assign
     * @throws UseCaseLogicException if no event or service is selected
     */
    public void assignMenu(Service service, Menu menu) throws UseCaseLogicException {
        if (currentEvent == null) {
            String msg = "Cannot assign menu: no event selected";
            throw new UseCaseLogicException(msg);
        }

        if (service == null) {
            String msg = "Cannot assign menu: no service selected";
            throw new UseCaseLogicException(msg);
        }

        service.setMenu(menu);

        // Notify all receivers (EventPersistence will persist)
        notifyMenuAssigned(service, menu);
    }

    /**
     * Removes the menu from the specified service
     *
     * @param service The service from which to remove the menu
     * @return true if removed successfully, false if the service is null
     */
    public boolean removeMenu(Service service) {
        if (service == null) {
            return false;
        }

        service.removeMenu();

        // Notify all receivers
        notifyMenuRemoved(service);

        return true;
    }

    // Notification methods to avoid code duplication

    /**
     * Notifies all receivers that an event has been created
     *
     * @param event The created event
     */
    private void notifyEventCreated(EventSheet event) {
        for (EventReceiver receiver : eventReceivers) {
            receiver.updateEventCreated(event);
        }
    }

    /**
     * Notifies all receivers that an event has been modified
     *
     * @param event The modified event
     */
    private void notifyEventModified(EventSheet event) {
        for (EventReceiver receiver : eventReceivers) {
            receiver.updateEventModified(event);
        }
    }

    /**
     * Notifies all receivers that an event has been deleted
     *
     * @param event The deleted event
     */
    private void notifyEventDeleted(EventSheet event) {
        for (EventReceiver receiver : eventReceivers) {
            receiver.updateEventDeleted(event);
        }
    }

    /**
     * Notifies all receivers that a service has been created
     *
     * @param service The created service
     */
    private void notifyServiceCreated(Service service) {
        for (EventReceiver receiver : eventReceivers) {
            receiver.updateServiceCreated(currentEvent, service);
        }
    }

    /**
     * Notifies all receivers that a service has been modified
     *
     * @param service The modified service
     */
    private void notifyServiceModified(Service service) {
        for (EventReceiver receiver : eventReceivers) {
            receiver.updateServiceModified(service);
        }
    }

    /**
     * Notifies all receivers that a service has been deleted
     *
     * @param service The deleted service
     */
    private void notifyServiceDeleted(Service service) {
        for (EventReceiver receiver : eventReceivers) {
            receiver.updateServiceDeleted(service);
        }
    }

    /**
     * Notifies all receivers that a menu has been assigned to a service
     *
     * @param service The service to which the menu was assigned
     * @param menu    The assigned menu
     */
    private void notifyMenuAssigned(Service service, Menu menu) {
        for (EventReceiver receiver : eventReceivers) {
            receiver.updateMenuAssigned(service, menu);
        }
    }

    /**
     * Notifies all receivers that a menu has been removed from a service
     *
     * @param service The service from which the menu was removed
     */
    private void notifyMenuRemoved(Service service) {
        for (EventReceiver receiver : eventReceivers) {
            receiver.updateMenuRemoved(service);
        }
    }
}