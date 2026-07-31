package catering.businesslogic.event;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import catering.businesslogic.user.User;
import catering.persistence.PersistenceManager;
import catering.persistence.ResultHandler;

/**
 * Represents an event in the catering system.
 */
public class EventSheet {
    private static int id = 0;
    private Date dateStart;
    private Date dateEnd;
    private User chef;
    private ArrayList<Service> services;
    private int numParticipants;
    private String status;


    public EventSheet(Date dateStart, Date dateEnd, int numParticipants, ArrayList<ServiceData> services) {
        this.services = new ArrayList<>();
        this.dateEnd = dateEnd;
        this.dateStart = dateStart;
        this.numParticipants = numParticipants;
        this.status = "saved";

        // increments the static shared variable 'id'
        id++;

        // creates services from input array
        for (ServiceData service : services) {
            this.services.add(new Service()); // TODO: inserisci i parametri corretti
        }
    }

    public int getNumParticipants() {
        return numParticipants;
    }

    public void setNumParticipants(int numParticipants) {
        this.numParticipants = numParticipants;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Basic getters and setters
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

    public User getChef() {
        return chef;
    }

    public int getChefId() {
        return chef != null ? chef.getId() : 0;
    }

    public void setChef(User chef) {
        this.chef = chef;
    }

    public void setChefId(int chefId) {
        this.chef = User.load(chefId);
    }

    public ArrayList<Service> getServices() {
        return services;
    }

    public void setServices(ArrayList<ServiceData> services) {

    }

    // Service management
    public void addService(Service service) {
        if (services == null) {
            services = new ArrayList<>();
        }
        services.add(service);
    }

    public void removeService(Service service) {
        if (services != null) {
            services.remove(service);
        }
    }

    public boolean containsService(Service service) {
        if (services != null) {
            return services.contains(service);
        }
        return false;
    }

    // Database operations
    public void saveNewEvent() {
        String query = "INSERT INTO Events (name, date_start, date_end, chef_id) VALUES (?, ?, ?, ?)";

        Long startTimestamp = (dateStart != null) ? dateStart.getTime() : null;
        Long endTimestamp = (dateEnd != null) ? dateEnd.getTime() : null;

        PersistenceManager.executeUpdate(query, name, startTimestamp, endTimestamp, getChefId());

        // Get the ID of the newly inserted event
        id = PersistenceManager.getLastId();

    }

    public void updateEvent() {
        String query = "UPDATE Events SET name = ?, date_start = ?, date_end = ?, chef_id = ? WHERE id = ?";

        Long startTimestamp = (dateStart != null) ? dateStart.getTime() : null;
        Long endTimestamp = (dateEnd != null) ? dateEnd.getTime() : null;

        PersistenceManager.executeUpdate(query, name, startTimestamp, endTimestamp, getChefId(), id);

    }

    public boolean deleteEvent() {
        // Delete all services first
        for (Service service : services) {
            service.deleteService();
        }
        services.clear();

        // Delete the event
        String query = "DELETE FROM Events WHERE id = ?";
        boolean success = PersistenceManager.executeUpdate(query, id) > 0;

        if (success) {
        }

        return success;
    }

    // Static load methods
    public static ArrayList<EventSheet> loadAllEvents() {
        ArrayList<EventSheet> events = new ArrayList<>();
        String query = "SELECT * FROM Events ORDER BY date_start DESC";

        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                EventSheet e = new EventSheet();
                e.id = rs.getInt("id");
                e.name = rs.getString("name");
                e.dateStart = Date.valueOf(rs.getString("date_start"));
                e.dateEnd = Date.valueOf(rs.getString("date_end"));
                e.chef = User.load(rs.getInt("chef_id"));
                events.add(e);
            }
        });

        // Load services for each event
        for (EventSheet e : events) {
            e.services = Service.loadServicesForEvent(e.id);
        }

        return events;
    }

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
                e.dateStart = Date.valueOf(rs.getString("date_start"));
                e.dateEnd = Date.valueOf(rs.getString("date_end"));

                try {
                    e.chef = User.load(rs.getInt("chef_id"));
                } catch (Exception ex) {
                    e.chef = null;
                }

                eventHolder[0] = e;
            }
        }, param);

        if (!eventFound[0]) {
            return null;
        }

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
        return "Event [id=" + id + ", name=" + name + ", dateStart=" + dateStart +
                ", services=" + (services != null ? services.size() : 0) + "]";
    }
}