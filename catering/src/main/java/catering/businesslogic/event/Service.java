package catering.businesslogic.event;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import catering.businesslogic.menu.Menu;
import catering.businesslogic.menu.MenuItem;
import catering.persistence.PersistenceManager;
import catering.persistence.ResultHandler;

/**
 * Represents a service in an event in the catering system.
 */
public class Service {
    private int id;
    private String name;
    private Date date;
    private Time timeStart;
    private Time timeEnd;
    private String location;
    private Menu menu;
    private String type;
    private String status;
    private EventSheet event; // Riferimento diretto all'evento (associazione "prevede")
    private int eventId; // Mantenuto per agevolare il caricamento dal DB prima di risolvere la referenza
    private List<Staff> staffList; // Riferimento al personale (associazione "works for")

    public Service() {
        this.staffList = new ArrayList<>();
    }

    public Service(String name) {
        this();
        this.name = name;
    }

    public Service(ServiceData data) {
        this.type = data.type();
        this.date = data.date();
        this.timeStart = data.timeStart();
        this.timeEnd = data.timeEnd();
    }

    // ========================================================================
    // GETTERS & SETTERS
    // ========================================================================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Time getTimeStart() {
        return timeStart;
    }

    public void setTimeStart(Time timeStart) {
        this.timeStart = timeStart;
    }

    public Time getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(Time timeEnd) {
        this.timeEnd = timeEnd;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public EventSheet getEvent() {
        return event;
    }

    public void setEvent(EventSheet event) {
        this.event = event;
        if (event != null) {
            this.eventId = event.getId();
        }
    }

    public int getEventId() {
        return (event != null) ? event.getId() : eventId;
    }

    public List<Staff> getStaffList() {
        return staffList;
    }

    public int getMenuId() {
        return (menu != null) ? menu.getId() : 0;
    }

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }

    public ArrayList<MenuItem> getMenuItems() {
        if (this.menu == null) {
            return new ArrayList<>();
        }
        return this.menu.getItems();
    }

    // ========================================================================
    // DOMAIN LOGIC METHODS (dal DCD)
    // ========================================================================

    /**
     * Modifica lo stato corrente del servizio.
     * @param status Il nuovo stato del servizio
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Aggiorna la location del servizio corrente.
     * @param venue Il luogo/sala dell'evento
     */
    public void createLocation(String venue) {
        this.location = venue;
    }

    /**
     * Aggiunge una lista di personale alla lista preesistente.
     * @param staff Lista di Staff da assegnare al servizio
     */
    public void addStaff(List<Staff> staff) {
        if (this.staffList == null) {
            this.staffList = new ArrayList<>();
        }
        if (staff != null) {
            this.staffList.addAll(staff);
        }
    }

    /**
     * Aggiorna i dati del servizio basandosi su un'altra istanza.
     * @param newService Il servizio con i nuovi dati
     */
    public void edit(Service newService) {
        if (newService == null) return;

        this.name = newService.getName();
        this.date = newService.getDate();
        this.timeStart = newService.getTimeStart();
        this.timeEnd = newService.getTimeEnd();
        this.location = newService.getLocation();
        this.type = newService.getType();
        // Lo stato non viene modificato qui, poiché segue un ciclo di vita specifico
    }

    // ========================================================================
    // MENU MANAGEMENT
    // ========================================================================

    public void approveMenu() {
        if (this.menu == null)
            return;

        String query = "UPDATE Services SET approved_menu_id = ? WHERE id = ?";
        PersistenceManager.executeUpdate(query, this.menu.getId(), this.getId());
    }

    public void removeMenu() {
        this.menu = null;
    }

    public void assignMenuToService(Menu menu) {
        this.setMenu(menu);

        String query = "UPDATE Services SET approved_menu_id = ? WHERE id = ?";
        PersistenceManager.executeUpdate(query, menu.getId(), this.getId());
    }

    public void removeMenuFromService() {
        this.removeMenu();

        String query = "UPDATE Services SET approved_menu_id = 0 WHERE id = ?";
        PersistenceManager.executeUpdate(query, this.getId());
    }

    // ========================================================================
    // DATABASE OPERATIONS (Persistence)
    // ========================================================================

    public void saveNewService() {
        String query = "INSERT INTO Services (event_id, name, service_date, time_start, time_end, location, type, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        Long dateTimestamp = (this.getDate() != null) ? this.getDate().getTime() : null;

        PersistenceManager.executeUpdate(query,
                this.getEventId(),
                this.getName(),
                dateTimestamp,
                this.getTimeStart(),
                this.getTimeEnd(),
                this.getLocation(),
                this.getType(),
                this.getStatus());

        this.setId(PersistenceManager.getLastId());
    }

    public void updateService() {
        String query = "UPDATE Services SET name = ?, service_date = ?, time_start = ?, time_end = ?, location = ?, type = ?, status = ? WHERE id = ?";

        Long dateTimestamp = (this.getDate() != null) ? this.getDate().getTime() : null;

        PersistenceManager.executeUpdate(query,
                this.getName(),
                dateTimestamp,
                this.getTimeStart(),
                this.getTimeEnd(),
                this.getLocation(),
                this.getType(),
                this.getStatus(),
                this.getId());
    }

    public boolean deleteService() {
        String query = "DELETE FROM Services WHERE id = ?";
        return PersistenceManager.executeUpdate(query, this.getId()) > 0;
    }

    public static ArrayList<Service> loadServicesForEvent(int eventId) {
        ArrayList<Service> services = new ArrayList<>();
        String query = "SELECT * FROM Services WHERE event_id = ? ORDER BY service_date, time_start";

        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                Service s = new Service();
                s.id = rs.getInt("id");
                s.name = rs.getString("name");
                // s.type = rs.getString("type");
                // s.status = rs.getString("status");

                try {
                    s.date = Date.valueOf(rs.getString("service_date"));
                    s.timeStart = Time.valueOf(rs.getString("time_start"));
                    s.timeEnd = Time.valueOf(rs.getString("time_end"));
                } catch (IllegalArgumentException ex) {
                    // Ignore parsing errors
                }

                s.location = rs.getString("location");
                s.eventId = rs.getInt("event_id");

                int menuId = rs.getInt("approved_menu_id");
                if (menuId > 0)
                    s.menu = Menu.load(menuId);

                services.add(s);
            }
        }, eventId);

        return services;
    }

    public static Service loadById(int id) {
        String query = "SELECT * FROM Services WHERE id = ?";
        return loadServiceByQuery(query, id);
    }

    public static Service loadByName(String name) {
        String query = "SELECT * FROM Services WHERE name = ?";
        return loadServiceByQuery(query, name);
    }

    private static Service loadServiceByQuery(String query, Object param) {
        final Service[] serviceHolder = new Service[1];
        final boolean[] serviceFound = new boolean[1];
        serviceFound[0] = false;

        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                serviceFound[0] = true;

                Service s = new Service();
                s.id = rs.getInt("id");
                s.name = rs.getString("name");

                try {
                    String dateStr = rs.getString("service_date");
                    String startTimeStr = rs.getString("time_start");
                    String endTimeStr = rs.getString("time_end");

                    if (dateStr != null && !dateStr.isEmpty()) {
                        s.date = Date.valueOf(dateStr);
                    }
                    if (startTimeStr != null && !startTimeStr.isEmpty()) {
                        s.timeStart = Time.valueOf(startTimeStr);
                    }
                    if (endTimeStr != null && !endTimeStr.isEmpty()) {
                        s.timeEnd = Time.valueOf(endTimeStr);
                    }
                } catch (IllegalArgumentException ex) {
                }

                s.location = rs.getString("location");
                s.eventId = rs.getInt("event_id");

                int menuId = rs.getInt("approved_menu_id");
                if (menuId > 0) {
                    try {
                        s.menu = Menu.load(menuId);
                    } catch (Exception e) {
                    }
                }

                serviceHolder[0] = s;
            }
        }, param);

        return serviceFound[0] ? serviceHolder[0] : null;
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    @Override
    public String toString() {
        return "Service [id=" + id + ", name=" + name + ", type=" + type + ", status=" + status +
                ", date=" + date + ", location=" + location + ", menu=" + (menu != null ? menu.getTitle() : "none") + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        Service other = (Service) obj;

        // Compare by ID if both are valid
        if (this.id > 0 && other.id > 0) {
            return this.id == other.id;
        }

        boolean nameMatch = (this.name == null && other.name == null) ||
                (this.name != null && this.name.equals(other.name));
        if (!nameMatch) return false;

        boolean dateMatch = (this.date == null && other.date == null) ||
                (this.date != null && this.date.equals(other.date));
        if (!dateMatch) return false;

        boolean timeStartMatch = (this.timeStart == null && other.timeStart == null) ||
                (this.timeStart != null && this.timeStart.equals(other.timeStart));
        if (!timeStartMatch) return false;

        boolean timeEndMatch = (this.timeEnd == null && other.timeEnd == null) ||
                (this.timeEnd != null && this.timeEnd.equals(other.timeEnd));
        if (!timeEndMatch) return false;

        boolean locationMatch = (this.location == null && other.location == null) ||
                (this.location != null && this.location.equals(other.location));
        if (!locationMatch) return false;

        /*
        boolean typeMatch = (this.type == null && other.type == null) ||
                (this.type != null && this.type.equals(other.type));
        if (!typeMatch) return false;

        boolean statusMatch = (this.status == null && other.status == null) ||
                (this.status != null && this.status.equals(other.status));
        if (!statusMatch) return false;
        */

        boolean menuMatch = (this.menu == null && other.menu == null) ||
                (this.menu != null && this.menu.equals(other.menu));
        if (!menuMatch) return false;

        if (this.getEventId() > 0 && other.getEventId() > 0) {
            return this.getEventId() == other.getEventId();
        }

        return true;
    }
}