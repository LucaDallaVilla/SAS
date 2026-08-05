package catering.businesslogic.event;

import java.util.Objects;

/**
 * Represents the physical location where a specific catering service takes place.
 * This class maps to the Location entity in the Design Class Diagram (DCD).
 */
public class Location {

    private String venue;
    private String status;

    /**
     * Default constructor.
     * Useful for persistence frameworks (e.g., Hibernate, JPA) or serialization mechanisms.
     */
    public Location() {
    }

    /**
     * Parameterized constructor to initialize the venue.
     * This matches the explicit "+ Location(venue: String)" signature in the DCD.
     *
     * @param venue The name or the address of the location.
     */
    public Location(String venue) {
        this.venue = venue;
    }

    // ========================================================================
    // GETTERS & SETTERS
    // ========================================================================

    /**
     * Gets the venue (name or address) of the location.
     *
     * @return The venue string.
     */
    public String getVenue() {
        return venue;
    }

    /**
     * Sets the venue (name or address) of the location.
     *
     * @param venue The new venue string to set.
     */
    public void setVenue(String venue) {
        this.venue = venue;
    }

    /**
     * Gets the current status of the location (e.g., "confermata", "da verificare").
     *
     * @return The status string.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status of the location.
     *
     * @param status The new status string to set.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Location other = (Location) obj;
        return Objects.equals(venue, other.venue) &&
                Objects.equals(status, other.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(venue, status);
    }

    @Override
    public String toString() {
        return "Location [" +
                "venue='" + venue + '\'' +
                ", status='" + status + '\'' +
                "]";
    }
}