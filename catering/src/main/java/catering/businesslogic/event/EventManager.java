package catering.businesslogic.event;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import catering.businesslogic.UseCaseLogicException;
import catering.businesslogic.menu.Menu;
import catering.businesslogic.user.User;

/**
 * EventManager handles all operations related to events and services in the
 * CatERing system.
 * It manages event creation, modification, and deletion, as well as service
 * management and menu assignments for services.
 */
public class EventManager {
    private final ArrayList<EventReceiver> eventReceivers;
    private EventSheet currentEvent;
    private FineConditions fineConditions;

    /**
     * Constructor initializes the event receivers list
     */
    public EventManager() {
        currentEvent = null;
        eventReceivers = new ArrayList<>();
        fineConditions = new FineConditions(30, 7);
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
     * Creates a new event with the given details
     *
     * @param dateStart       Start date
     * @param dateEnd         End date
     * @param numParticipants Number of participants
     * @param services        List of services for the event
     * @return The newly created event, or null if an error occurs
     */
    public void fillRecurringEventSheet(Date dateStart, Date dateEnd, int numParticipants, ArrayList<ServiceData> services, int frequency, Date finalDate) {
        try {
            LocalDate startDate = dateStart.toLocalDate();
            LocalDate dateFinal = finalDate.toLocalDate();

            RecurringEventSheet res = new RecurringEventSheet(frequency, finalDate);

            for(int i=0; i<(ChronoUnit.DAYS.between(startDate, dateFinal))/frequency; i++){
                EventSheet event = new EventSheet(dateStart, dateEnd, numParticipants, services, res);

                // Notify all receivers (EventPersistence will persist)
                notifyEventCreated(event);

                // Set as selected event
                this.currentEvent = event;
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void assignChef(User chef)throws UseCaseLogicException{
        if (currentEvent == null) {
            String msg = "Cannot assign menu: no event selected";
            throw new UseCaseLogicException(msg);
        }
        currentEvent.setChef(chef);
    }

    public void bookStaff(List<Staff> staff)throws UseCaseLogicException{
        if (currentEvent == null) {
            String msg = "Cannot assign menu: no event selected";
            throw new UseCaseLogicException(msg);
        }
        currentEvent.bookStaff(staff);
    }

    public void assignStaff(List<Staff> staff)throws UseCaseLogicException{
        if (currentEvent == null) {
            String msg = "Cannot assign menu: no event selected";
            throw new UseCaseLogicException(msg);
        }
        currentEvent.assignStaff(staff);
    }

    /**
     * Selects the specified event as the current event
     *
     * @param event The event to select
     */
    public void selectEvent(EventSheet event) throws UseCaseLogicException{
        if (currentEvent == null) {
            String msg = "Cannot assign menu: no event selected";
            throw new UseCaseLogicException(msg);
        }
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
     * Modifies an existing event respecting contract pre-conditions and post-conditions.
     *
     * @param eventId ID of the event to modify
     * @param newEventSheet The new data for the event
     * @return true if a penalty is required due to late modifications, false otherwise
     * @throws UseCaseLogicException if preconditions are not met
     */
    public boolean editEvent(int eventId, EventSheet newEventSheet) throws UseCaseLogicException {
        if (newEventSheet == null) {
            return false;
        }

        EventSheet event = EventSheet.loadById(eventId);
        if (event == null) {
            throw new UseCaseLogicException("L'evento specificato non esiste.");
        }

        String status = event.getStatus();
        if (status == null) {
            throw new UseCaseLogicException("Stato dell'evento risulta nullo.");
        }

        if (status.equals("evento chiuso")) {
            throw new UseCaseLogicException("Stato dell'evento non valido per la modifica. Lo stato deve essere 'In compilazione', 'Scheda salvata', 'Chef assegnato' o 'In corso'.");
        }

        // Pre-condizioni: verifica l'esistenza dei servizi richiesti
        if (event.getServices() == null || newEventSheet.getServices() == null) {
            throw new UseCaseLogicException("I servizi previsti dalla scheda evento devono essere presenti e validi.");
        }

        // Post-condizioni: calcolo necessità di una penale se "In corso" o in base a variazioni
        boolean requiresPenalty = false;
        if (status.equals("In corso")) {
            requiresPenalty = true;
        } else {
            int deltaParticipants = 0;
            if (event.getNumParticipants() > 0) {
                deltaParticipants = Math.abs(newEventSheet.getNumParticipants() - event.getNumParticipants()) * 100 / event.getNumParticipants();
            }
            // Controllo standard dei criteri di penale se lo stato originario non era forzatamente già "In corso"
            requiresPenalty = fineConditions.checkDaysNotice(event.getDateStart()) || fineConditions.checkParticipantsVariation(deltaParticipants);
        }

        // Applicazione modifiche
        event.edit(newEventSheet);

        // Notifica ai receiver
        notifyEventModified(event);

        // Aggiorna la referenza locale se l'evento è quello correntemente selezionato
        if (currentEvent != null && currentEvent.getId() == eventId) {
            this.currentEvent = event;
        }

        return requiresPenalty;
    }

    /**
     * Modifies an existing recurring event
     *
     * @param eventId ID of the event to modify
     * @param newEventSheet    New data for the event
     * @param singleEvent Boolean flag for single event vs entire series
     * @return true if penalty applied, false otherwise
     */
    public boolean editRecurringEvent(int eventId, EventSheet newEventSheet, boolean singleEvent) {
        if(newEventSheet == null){return false;}

        EventSheet event = EventSheet.loadById(eventId);
        if(event == null) {return false;}

        RecurringEventSheet recur = event.getRecurringSeries();
        if(recur == null) {return false;}

        if(!singleEvent){
            boolean fine = false;
            for(EventSheet e : recur.getEvents()){
                if (e != null) {

                    if(fine == false){
                        int deltaParticipants = Math.abs(newEventSheet.getNumParticipants()-e.getNumParticipants())/(newEventSheet.getNumParticipants()*100);
                        fine = fineConditions.checkDaysNotice(e.getDateStart()) || fineConditions.checkParticipantsVariation(deltaParticipants);
                    }
                    e.edit(newEventSheet);

                    // Notify all receivers
                    notifyEventModified(event);

                    // Update selected event if it's the same one
                    if (currentEvent != null && currentEvent.getId() == eventId) {
                        this.currentEvent = event;
                    }
                }
            }
            return fine;
        }else{
            int deltaParticipants = Math.abs(newEventSheet.getNumParticipants()-event.getNumParticipants())/(newEventSheet.getNumParticipants()*100);
            boolean fine = fineConditions.checkDaysNotice(event.getDateStart()) || fineConditions.checkParticipantsVariation(deltaParticipants);
            event.edit(newEventSheet);

            // Notify all receivers
            notifyEventModified(event);

            // Update selected event if it's the same one
            if (currentEvent != null && currentEvent.getId() == eventId) {
                this.currentEvent = event;
            }

            return fine;
        }
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
     * @return true if penalty applied, false otherwise
     */
    public boolean deleteEvent(int eventId) {
        try {
            EventSheet eventToDelete = EventSheet.loadById(eventId);
            if (eventToDelete == null) {
                return false;
            }

            // Notify all receivers (EventPersistence will delete from DB)
            notifyEventDeleted(eventToDelete);

            boolean fine = fineConditions.checkDaysNotice(eventToDelete.getDateStart());
            // Clear references if this was the selected event
            eventToDelete = null;

            return fine;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean deleteRecurringEvent(int eventId, boolean singleEvent){
        try {
            RecurringEventSheet rec = EventSheet.loadById(eventId).getRecurringSeries();
            boolean fine = false;
            if(!singleEvent){
                for(int i=0; i<rec.getEvents().size(); i++){
                    EventSheet eventToDelete = rec.getEvents().get(i);
                    if (eventToDelete == null) {
                        return false;
                    }

                    if(!fine){fine = fineConditions.checkDaysNotice(eventToDelete.getDateStart());}

                    // Notify all receivers (EventPersistence will delete from DB)
                    notifyEventDeleted(eventToDelete);

                    // Clear references if this was the selected event
                    eventToDelete = null;
                }
                return fine;
            }else{
                EventSheet eventToDelete = EventSheet.loadById(eventId);
                if (eventToDelete == null) {
                    return false;
                }

                // Notify all receivers (EventPersistence will delete from DB)
                notifyEventDeleted(eventToDelete);

                fine = fineConditions.checkDaysNotice(eventToDelete.getDateStart());

                // Clear references if this was the selected event
                eventToDelete = null;

                return fine;
            }

        } catch (Exception e) {
            return false;
        }
    }

    public boolean cancelEvent(int eventId){
        try {
            EventSheet eventToCancel = EventSheet.loadById(eventId);
            if (eventToCancel == null) {
                return false;
            }

            // Clear references if this was the selected event
            eventToCancel.setStatus("cancelled");

            eventToCancel.cancelServices();

            return fineConditions.checkDaysNotice(eventToCancel.getDateStart());
        } catch (Exception e) {
            return false;
        }
    }

    public boolean cancelRecurringEvent(int eventId, boolean singleEvent){
        try {
            RecurringEventSheet rec = EventSheet.loadById(eventId).getRecurringSeries();
            if(!singleEvent){
                EventSheet eventToCancel;
                boolean fine = false;
                for(int i=0; i<rec.getEvents().size(); i++){
                    eventToCancel = rec.getEvents().get(i);
                    if (eventToCancel == null) {
                        return false;
                    }

                    // Clear references if this was the selected event
                    eventToCancel.setStatus("cancelled");

                    eventToCancel.cancelServices();
                    if(!fine){fine = fineConditions.checkDaysNotice(eventToCancel.getDateStart());}
                }
                return fine;
            }else{
                EventSheet eventToCancel = EventSheet.loadById(eventId);
                if (eventToCancel == null) {
                    return false;
                }

                // Clear references if this was the selected event
                eventToCancel.setStatus("cancelled");

                eventToCancel.cancelServices();

                return fineConditions.checkDaysNotice(eventToCancel.getDateStart());
            }
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
    public void approveMenu(Service service, Menu menu) throws UseCaseLogicException {
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

    /**
     * Proposes a menu modification and notifies the Chef, altering the event state.
     *
     * @param menu    The suggested menu
     * @param service The service involved
     * @throws UseCaseLogicException se i requisiti e le pre-condizioni non sono soddisfatti.
     */
    public void suggestNewMenu(Menu menu, Service service) throws UseCaseLogicException {
        // Pre-condizione: l'evento corrente deve essere valido e selezionato
        if (currentEvent == null) {
            throw new UseCaseLogicException("Nessun evento selezionato. Operazione non permessa.");
        }

        // Pre-condizione: verifica parametri in ingresso
        if (menu == null || service == null) {
            throw new UseCaseLogicException("Parametri non validi: menu e servizio devono esistere.");
        }

        String status = currentEvent.getStatus();
        if (status == null) {
            status = "";
        }

        // Pre-condizione: lo stato dell'evento deve essere 'Personale prenotato'
        if (!status.equals("Personale prenotato")) {
            throw new UseCaseLogicException("Impossibile proporre un nuovo menu: lo stato dell'evento non è 'Personale prenotato'.");
        }

        // Post-condizione 1: notifica di proposta allo chef (qui simulata logica di sistema)
        System.out.println("SISTEMA - Inviata notifica di proposta di modifica menu allo Chef per il servizio: " + service.getName());

        // Post-condizione 2: sovrascrittura dello stato su "Evento in corso"
        currentEvent.setStatus("Evento in corso");

        // Notifica ai listener l'avvenuta modifica di stato sull'evento
        notifyEventModified(currentEvent);
    }

    public void prepareLocation(String venue, Service service) throws UseCaseLogicException{
        if (currentEvent == null) {
            String msg = "Cannot assign menu: no event selected";
            throw new UseCaseLogicException(msg);
        }
        currentEvent.setServiceLocation(venue, service);
    }

    public void pinEventAndMenus(List<Menu> menus) throws UseCaseLogicException{
        if (currentEvent == null) {
            String msg = "Cannot assign menu: no event selected";
            throw new UseCaseLogicException(msg);
        }
        currentEvent.addNote(menus);
    }

    public void endEvent() throws UseCaseLogicException{
        if (currentEvent == null) {
            String msg = "Cannot assign menu: no event selected";
            throw new UseCaseLogicException(msg);
        }
        currentEvent.setStatus("evento chiuso");
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