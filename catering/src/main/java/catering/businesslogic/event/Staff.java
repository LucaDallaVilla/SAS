package catering.businesslogic.event;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a staff member in the CatERing system.
 * Staff members (e.g., waiters, chefs, sommeliers) can be assigned to services.
 */
public class Staff {

    private int id;
    private String type;
    private List<Date> availableDates;
    private String status;
    private String role;

    /**
     * Default constructor.
     * Initializes the availableDates list to prevent NullPointerExceptions.
     */
    public Staff() {
        this.availableDates = new ArrayList<>();
    }

    /**
     * Parameterized constructor to initialize the main fields of a Staff member.
     *
     * @param type   The type/category of the staff member (e.g., "Cameriere", "Barman").
     * @param status The current status (e.g., "Disponibile", "Occupato").
     * @param role   The specific role assigned for a service.
     */
    public Staff(String type, String status, String role) {
        this();
        this.type = type;
        this.status = status;
        this.role = role;
    }

    /**
     * Parameterized constructor including the ID for persistence retrieval.
     *
     * @param id     The unique identifier of the staff member.
     * @param type   The type/category of the staff member.
     * @param status The current status.
     * @param role   The specific role assigned.
     */
    public Staff(int id, String type, String status, String role) {
        this(type, status, role);
        this.id = id;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Date> getAvailableDates() {
        return availableDates;
    }

    public void setAvailableDates(List<Date> availableDates) {
        this.availableDates = availableDates != null ? availableDates : new ArrayList<>();
    }

    public String getStatus() {
        return status;
    }

    public String getRole() {
        return role;
    }

    // ========================================================================
    // DOMAIN LOGIC METHODS (from DCD)
    // ========================================================================

    /**
     * Checks if the staff member is available on a specific date.
     *
     * @param date The date to verify against the availableDates list.
     * @return true if the staff member is available on the specified date, false otherwise.
     */
    public boolean checkAvailability(Date date) {
        if (date == null || this.availableDates == null || this.availableDates.isEmpty()) {
            return false;
        }
        return this.availableDates.contains(date);
    }

    /**
     * Updates the status of the staff member.
     *
     * @param newStatus The new status to be set.
     */
    public void setStatus(String newStatus) {
        this.status = newStatus;
    }

    /**
     * Assigns or modifies the specific role of the staff member for a service.
     *
     * @param newRole The new role to be assigned.
     */
    public void setRole(String newRole) {
        this.role = newRole;
    }

    /**
     * Utility method to add an available date for the staff member.
     *
     * @param date The date to add to the availability list.
     */
    public void addAvailableDate(Date date) {
        if (date != null && !this.availableDates.contains(date)) {
            this.availableDates.add(date);
        }
    }

    // ========================================================================
    // UTILITY METHODS (equals, hashCode, toString)
    // ========================================================================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Staff other = (Staff) obj;

        // If both have valid IDs, compare by ID (Primary Key)
        if (this.id > 0 && other.id > 0) {
            return this.id == other.id;
        }

        // Fallback: compare by properties
        return Objects.equals(type, other.type) &&
                Objects.equals(status, other.status) &&
                Objects.equals(role, other.role);
    }

    @Override
    public int hashCode() {
        if (this.id > 0) {
            return Objects.hash(id);
        }
        return Objects.hash(type, status, role);
    }

    @Override
    public String toString() {
        return "Staff [" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", status='" + status + '\'' +
                ", role='" + role + '\'' +
                ", availableDatesCount=" + (availableDates != null ? availableDates.size() : 0) +
                "]";
    }
}