package catering.businesslogic.event;

import java.util.ArrayList;
import java.sql.Date;

/**
 * Represents a recurring series for event sheets in the CatERing system.
 * It manages the frequency, the end date of the recurrence, and the list of associated events.
 */
public class RecurringEventSheet {

    private int frequency;
    private Date finalDate;
    private ArrayList<EventSheet> events;

    /**
     * Constructs a new RecurringEventSheet.
     * Initializes the empty list of associated events to prevent NullPointerExceptions.
     *
     * @param frequency The recurrence frequency (e.g., number of days between events).
     * @param finalDate The final date until which the event recurrence is valid.
     */
    public RecurringEventSheet(int frequency, Date finalDate) {
        this.frequency = frequency;
        this.finalDate = finalDate;
        this.events = new ArrayList<>(); // Inizializzazione della collezione
    }

    /**
     * Gets the frequency of the recurrence.
     *
     * @return The recurrence frequency.
     */
    public int getFrequency() {
        return frequency;
    }

    /**
     * Sets the frequency of the recurrence.
     *
     * @param frequency The new recurrence frequency.
     */
    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    /**
     * Gets the final date of the recurrence series.
     *
     * @return The final date.
     */
    public Date getFinalDate() {
        return finalDate;
    }

    /**
     * Sets the final date of the recurrence series.
     *
     * @param finalDate The new final date.
     */
    public void setFinalDate(Date finalDate) {
        this.finalDate = finalDate;
    }

    /**
     * Gets the list of event sheets associated with this recurring series.
     *
     * @return The list of events.
     */
    public ArrayList<EventSheet> getEvents() {
        return events;
    }

    /**
     * Adds an EventSheet to the recurring series list.
     * Ensures that null objects are not added to the collection.
     *
     * @param eventSheet The event sheet to add.
     */
    public void addEvent(EventSheet eventSheet) {
        if (eventSheet != null) {
            this.events.add(eventSheet);
        }
    }
}