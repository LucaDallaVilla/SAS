package catering.businesslogic.event;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import catering.businesslogic.menu.Menu;
import catering.businesslogic.user.User;
import catering.persistence.PersistenceManager;
import catering.persistence.ResultHandler;

/**
 * Represents the Event Sheet (Scheda Evento) in the CatERing system.
 * Acts as the Aggregate Root for the Event domain, managing state, services, and assignments.
 */
public class EventSheet {

    private int id;
    private String name;
    private Date dateStart;
    private Date dateEnd;
    private int numParticipants;
    private EventStatus status;
    private User chef;

    private ArrayList<Service> services;
    private ArrayList<Note> notes;
    private RecurringEventSheet recurringSeries;

    /**
     * Default constructor for persistence layer.
     */
    public EventSheet() {
        this.services = new ArrayList<>();
        this.notes = new ArrayList<>();
        this.status = EventStatus.SCHEDA_SALVATA;
    }

    public EventSheet(String name) {
        this();
        this.name = name;
    }

    /**
     * Constructor for creating a new standard EventSheet.
     *
     * @param dateStart       Start date of the event
     * @param dateEnd         End date of the event
     * @param numParticipants Number of expected participants
     * @param servicesData    Initial data for the services requested
     */
    public EventSheet(Date dateStart, Date dateEnd, int numParticipants, ArrayList<ServiceData> servicesData) {
        this();
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.numParticipants = numParticipants;
        this.status = EventStatus.SCHEDA_SALVATA; // Stato in base ai contratti operativi

        if (servicesData != null) {
            for (ServiceData serviceData : servicesData) {
                // Instantiates a new Service based on ServiceData (as per DCD)
                this.services.add(new Service(serviceData));
            }
        }
    }

    /**
     * Constructor for creating a new recurring EventSheet.
     *
     * @param dateStart       Start date of the event
     * @param dateEnd         End date of the event
     * @param numParticipants Number of expected participants
     * @param servicesData    Initial data for the services requested
     * @param res             The RecurringEventSheet defining the series
     */
    public EventSheet(Date dateStart, Date dateEnd, int numParticipants, ArrayList<ServiceData> servicesData, RecurringEventSheet res) {
        this(dateStart, dateEnd, numParticipants, servicesData);
        this.recurringSeries = res;
    }

    // ========================================================================
    // GETTERS & SETTERS
    // ========================================================================

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDateStart() {
        return dateStart;
    }

    public void setDateStart(Date dateStart) {
        this.dateStart = dateStart;
    }

    public Date getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(Date dateEnd) {
        this.dateEnd = dateEnd;
    }

    public int getNumParticipants() {
        return numParticipants;
    }

    public void setNumParticipants(int numParticipants) {
        this.numParticipants = numParticipants;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public User getChef() {
        return chef;
    }

    public int getChefId() {
        return chef != null ? chef.getId() : 0;
    }

    public RecurringEventSheet getRecurringSeries() {
        return recurringSeries;
    }

    public void setRecurringSeries(RecurringEventSheet recurringSeries) {
        this.recurringSeries = recurringSeries;
    }

    public ArrayList<Service> getServices() {
        return services;
    }

    public ArrayList<Note> getNotes() {
        return notes;
    }

    // ========================================================================
    // DOMAIN LOGIC METHODS
    // ========================================================================

    /**
     * Assigns a Chef to the event and updates the event status.
     *
     * @param chef The user with Chef role to be assigned.
     */
    public void setChef(User chef) {
        this.chef = chef;
        // Se lo stato è "Scheda salvata", avanza a "Chef assegnato" come da System Sequence
        if (this.status != null && this.status == EventStatus.SCHEDA_SALVATA) {
            this.status = EventStatus.CHEF_ASSEGNATO;
        }
    }

    /**
     * Sets the location for a specific service within this event.
     *
     * @param venue   The location string
     * @param service The service to update
     */
    public void setServiceLocation(String venue, Service service) {
        if (this.services != null && this.services.contains(service)) {
            service.createLocation(venue); // DCD indica createLocation(venue: String) su Service
        } else {
            throw new IllegalArgumentException("Il servizio specificato non appartiene a questo evento.");
        }
    }

    public boolean containsService(Service service) {
        if (services != null) {
            return services.contains(service);
        }
        return false;
    }

    /**
     * Adds a new service to the event.
     *
     * @param service The service to add
     */
    public void addService(Service service) {
        if (this.services == null) {
            this.services = new ArrayList<>();
        }
        if (service != null && !this.services.contains(service)) {
            this.services.add(service);
        }
    }

    /**
     * Removes an existing service from the event.
     *
     * @param service The service to remove
     */
    public void removeService(Service service) {
        if (this.services != null && service != null) {
            this.services.remove(service);
        }
    }

    /**
     * Cancels all services associated with this event.
     * Used mainly when the event itself gets cancelled.
     */
    public void cancelServices() {
        if (this.services != null) {
            for (Service s : this.services) {
                s.setStatus("cancellato");
            }
        }
    }

    /**
     * Updates the fields of this event using data from another EventSheet.
     *
     * @param newEventSheet The EventSheet containing updated information.
     */
    public void edit(EventSheet newEventSheet) {
        if (newEventSheet == null) return;

        if (newEventSheet.getName() != null) {
            this.name = newEventSheet.getName();
        }
        if (newEventSheet.getDateStart() != null) {
            this.dateStart = newEventSheet.getDateStart();
        }
        if (newEventSheet.getDateEnd() != null) {
            this.dateEnd = newEventSheet.getDateEnd();
        }
        if (newEventSheet.getNumParticipants() > 0) {
            this.numParticipants = newEventSheet.getNumParticipants();
        }

        // Aggiorna i servizi individualmente come da DSD (senza sostituire l'intera lista)
        if (newEventSheet.getServices() != null && this.services != null) {
            int max = Math.min(this.services.size(), newEventSheet.getServices().size());
            for (int i = 0; i < max; i++) {
                this.services.get(i).edit(newEventSheet.getServices().get(i));
            }
        }
    }

    /**
     * Adds pinning notes and menus to the closed/active event.
     *
     * @param menus The list of menus to attach to the note.
     */
    public void addNote(List<Menu> menus) {
        if (this.notes == null) {
            this.notes = new ArrayList<>();
        }
        // Il DCD prevede la creazione di Note passandogli EventSheet e la lista dei Menu
        Note note = new Note(this, menus);
        this.notes.add(note);
    }

    // Stub per l'eventuale chiamata da EventManager
    public void bookStaff(List<Staff> staff) {
        // Implementazione specifica della prenotazione personale...
        this.status = EventStatus.PERSONALE_PRENOTATO;
    }

    // Stub per l'eventuale chiamata da EventManager
    public void assignStaff(List<Staff> staff) {
        // Implementazione specifica dell'assegnazione definitiva...
    }

    // ========================================================================
    // PERSISTENCE (Static Loaders & Instance Savers)
    // ========================================================================

    public void saveNewEvent() {
        String query = "INSERT INTO Events (name, date_start, date_end, num_participants, status, chef_id) VALUES (?, ?, ?, ?, ?, ?)";
        Long startTimestamp = (dateStart != null) ? dateStart.getTime() : null;
        Long endTimestamp = (dateEnd != null) ? dateEnd.getTime() : null;

        PersistenceManager.executeUpdate(query, name, startTimestamp, endTimestamp, numParticipants, (status != null ? status.getStringValue() : null), getChefId());
        id = PersistenceManager.getLastId();
    }

    public void updateEvent() {
        String query = "UPDATE Events SET name = ?, date_start = ?, date_end = ?, num_participants = ?, status = ?, chef_id = ? WHERE id = ?";
        Long startTimestamp = (dateStart != null) ? dateStart.getTime() : null;
        Long endTimestamp = (dateEnd != null) ? dateEnd.getTime() : null;

        PersistenceManager.executeUpdate(query, name, startTimestamp, endTimestamp, numParticipants, (status != null ? status.getStringValue() : null), getChefId(), id);
    }

    public boolean deleteEvent() {
        if (this.services != null) {
            for (Service service : services) {
                service.deleteService();
            }
            services.clear();
        }
        String query = "DELETE FROM Events WHERE id = ?";
        return PersistenceManager.executeUpdate(query, id) > 0;
    }

    /**
     * Loads all events from the persistence layer.
     *
     * @return A list of all loaded EventSheets
     */
    public static ArrayList<EventSheet> loadAllEvents() {
        ArrayList<EventSheet> events = new ArrayList<>();
        String query = "SELECT * FROM Events ORDER BY date_start DESC";

        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                EventSheet e = new EventSheet();
                e.id = rs.getInt("id");
                e.name = rs.getString("name");

                String startStr = rs.getString("date_start");
                if(startStr != null) e.dateStart = Date.valueOf(startStr);

                String endStr = rs.getString("date_end");
                if(endStr != null) e.dateEnd = Date.valueOf(endStr);

                e.numParticipants = rs.getInt("num_participants");
                e.setStatus(EventStatus.SCHEDA_SALVATA);

                try {
                    e.chef = User.load(rs.getInt("chef_id"));
                } catch (Exception ex) {
                    e.chef = null;
                }
                events.add(e);
            }
        });

        // Simula il caricamento dei servizi in base al DB o mock se assente
        for (EventSheet e : events) {
            try {
                // e.services = Service.loadServicesForEvent(e.id);
            } catch (Exception ignored) { }
        }

        return events;
    }

    /**
     * Loads a specific event by its ID.
     *
     * @param id The ID of the event to retrieve
     * @return The EventSheet instance or null if not found
     */
    public static EventSheet loadById(int id) {
        String query = "SELECT * FROM Events WHERE id = ?";
        return loadEventByQuery(query, id);
    }

    public static EventSheet loadByName(String name) {
        String query = "SELECT * FROM Events WHERE name = ?";
        return loadEventByQuery(query, name);
    }

    private static EventSheet loadEventByQuery(String query, Object param) {
        final EventSheet[] eventHolder = new EventSheet[1];
        final boolean[] eventFound = new boolean[1];

        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                eventFound[0] = true;
                EventSheet e = new EventSheet();

                e.id = rs.getInt("id");
                e.name = rs.getString("name");

                String startStr = rs.getString("date_start");
                if(startStr != null) e.dateStart = Date.valueOf(startStr);

                String endStr = rs.getString("date_end");
                if(endStr != null) e.dateEnd = Date.valueOf(endStr);

                e.numParticipants = rs.getInt("num_participants");
                // e.status = rs.getString("status");

                try {
                    e.chef = User.load(rs.getInt("chef_id"));
                } catch (Exception ex) {
                    e.chef = null;
                }
                eventHolder[0] = e;
            }
        }, param);

        if (!eventFound[0]) return null;

        EventSheet result = eventHolder[0];
        if (result != null) {
            try {
                result.services = Service.loadServicesForEvent(result.id);
            } catch (Exception ex) {
                result.services = new ArrayList<>();
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "EventSheet [id=" + id + ", name=" + name + ", status=" + (status != null ? status.getStringValue() : "null") +
                ", dateStart=" + dateStart + ", participants=" + numParticipants +
                ", servicesCount=" + (services != null ? services.size() : 0) + "]";
    }
}