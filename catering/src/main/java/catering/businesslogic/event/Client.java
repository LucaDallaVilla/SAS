package catering.businesslogic.event;

import java.util.Objects;

/**
 * Represents a client in the CatERing system.
 * The client is the person who requests and commissions an event[cite: 5].
 * This class maps to the Client entity in the Design Class Diagram (DCD)[cite: 2].
 */
public class Client {

    private String firstName;
    private String lastName;
    private String phoneNumber;

    /**
     * Default constructor.
     * Provided for compatibility with persistence frameworks (e.g., Hibernate/JPA)
     * or JSON serialization/deserialization mechanisms.
     */
    public Client() {}

    /**
     * Parameterized constructor to initialize the client's details.
     * This matches the explicit "+ Client(firstName: String, lastName: String, phoneNumber: String)"
     * signature defined in the DCD[cite: 2].
     *
     * @param firstName   The first name of the client.
     * @param lastName    The last name of the client.
     * @param phoneNumber The contact phone number of the client.
     */
    public Client(String firstName, String lastName, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
    }

    // ========================================================================
    // GETTERS & SETTERS
    // ========================================================================

    /**
     * Gets the first name of the client[cite: 2].
     *
     * @return The client's first name.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the first name of the client[cite: 2].
     *
     * @param firstName The first name to set.
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Gets the last name of the client[cite: 2].
     *
     * @return The client's last name.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the last name of the client[cite: 2].
     *
     * @param lastName The last name to set.
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Gets the phone number of the client[cite: 2].
     *
     * @return The client's phone number.
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Sets the phone number of the client[cite: 2].
     *
     * @param phoneNumber The phone number to set.
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Client other = (Client) obj;
        return Objects.equals(firstName, other.firstName) &&
                Objects.equals(lastName, other.lastName) &&
                Objects.equals(phoneNumber, other.phoneNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, phoneNumber);
    }

    @Override
    public String toString() {
        return firstName + " " + lastName + " - " + phoneNumber;
    }
}