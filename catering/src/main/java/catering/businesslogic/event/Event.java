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
public class Event extends Order{
    private int id;
    private User chef;
    private String name;

    public Event() {};
    public Event(String name) {
        setServices(new ArrayList<>());
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    // Service management
    public void addService(Service service) {
        if (getServices() == null) {
            setServices(new ArrayList<>());
        }
        getServices().add(service);
    }

    public void removeService(Service service) {
        if (getServices() != null) {
            getServices().remove(service);
        }
    }

    public boolean containsService(Service service) {
        if (getServices() != null) {
            return getServices().contains(service);
        }
        return false;
    }

    // Database operations
    public void saveNewEvent() {
        String query = "INSERT INTO Events (name, date_start, date_end, chef_id) VALUES (?, ?, ?, ?)";

        Long startTimestamp = (getDateStart() != null) ? getDateStart().getTime() : null;
        Long endTimestamp = (getDateEnd() != null) ? getDateEnd().getTime() : null;

        PersistenceManager.executeUpdate(query, name, startTimestamp, endTimestamp, getChefId());

        // Get the ID of the newly inserted event
        id = PersistenceManager.getLastId();

    }

    public void updateEvent() {
        String query = "UPDATE Events SET name = ?, date_start = ?, date_end = ?, chef_id = ? WHERE id = ?";

        Long startTimestamp = (getDateStart() != null) ? getDateStart().getTime() : null;
        Long endTimestamp = (getDateEnd() != null) ? getDateEnd().getTime() : null;

        PersistenceManager.executeUpdate(query, name, startTimestamp, endTimestamp, getChefId(), id);

    }

    public boolean deleteEvent() {
        // Delete all services first
        for (Service service : getServices()) {
            service.deleteService();
        }
        getServices().clear();

        // Delete the event
        String query = "DELETE FROM Events WHERE id = ?";
        boolean success = PersistenceManager.executeUpdate(query, id) > 0;

        if (success) {

        }

        return success;
    }

    // Static load methods
    public static ArrayList<Event> loadAllEvents() {
        ArrayList<Event> events = new ArrayList<>();
        String query = "SELECT * FROM Events ORDER BY date_start DESC";

        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                Event e = new Event();
                e.id = rs.getInt("id");
                e.name = rs.getString("name");
                e.setDateStart(Date.valueOf(rs.getString("date_start")));
                e.setDateEnd(Date.valueOf(rs.getString("date_end")));
                e.setChef(User.load(rs.getInt("chef_id")));
                events.add(e);
            }
        });

        // Load services for each event
        for (Event e : events) {
            e.setServices(Service.loadServicesForEvent(e.id));
        }

        return events;
    }

    public static Event loadById(int id) {
        String query = "SELECT * FROM Events WHERE id = ?";
        return loadEventByQuery(query, id);
    }

    public static Event loadByName(String name) {
        String query = "SELECT * FROM Events WHERE name = ?";
        return loadEventByQuery(query, name);
    }

    private static Event loadEventByQuery(String query, Object param) {
        final Event[] eventHolder = new Event[1];
        final boolean[] eventFound = new boolean[1];

        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                eventFound[0] = true;

                Event e = new Event();

                e.id = rs.getInt("id");
                e.name = rs.getString("name");
                e.setDateStart(Date.valueOf(rs.getString("date_start")));
                e.setDateEnd(Date.valueOf(rs.getString("date_end")));

                try {
                    e.setChef(User.load(rs.getInt("chef_id")));
                } catch (Exception ex) {
                    e.setChef(null);
                }

                eventHolder[0] = e;
            }
        }, param);

        if (!eventFound[0]) {
            return null;
        }

        Event result = eventHolder[0];
        if (result != null) {
            try {
                result.setServices(Service.loadServicesForEvent(result.id));
            } catch (Exception ex) {
                result.setServices(new ArrayList<>());
            }
        }

        return result;
    }

    @Override
    public String toString() {
        return "Event [id=" + id + ", name=" + name + ", dateStart=" + getDateStart() +
                ", services=" + (getServices() != null ? getServices().size() : 0) + "]";
    }
}