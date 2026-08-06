package catering.businesslogic.event;

import catering.businesslogic.menu.Menu;
import catering.businesslogic.user.User;

/**
 * Interface for receiving event-related notifications.
 * Implemented by classes that need to respond to event changes.
 */
public interface EventReceiver {

    void updateEventCreated(EventSheet event);

    void updateEventModified(EventSheet event);

    void updateEventDeleted(EventSheet event);

    void updateServiceCreated(EventSheet event, Service service);

    void updateServiceModified(Service service);

    void updateServiceDeleted(Service service);

    void updateMenuAssigned(Service service, Menu menu);

    void updateMenuRemoved(Service service);

    void updatePersonalePrenotato(Service s, User p);

    void updateCuocoAssegnato(Service s, User c);

    void updateRuoloAssegnato(Service s, User p, String ruolo);
}