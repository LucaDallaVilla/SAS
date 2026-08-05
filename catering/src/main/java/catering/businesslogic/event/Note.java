package catering.businesslogic.event;

import java.util.ArrayList;
import java.util.List;

import catering.businesslogic.menu.Menu;

/**
 * Represents a note or annotation associated with an event sheet in the CatERing system[cite: 4, 5].
 * It is used to pin or suggest menus for a specific event[cite: 2].
 * This class maps to the Note entity in the Design Class Diagram (DCD)[cite: 2].
 */
public class Note {

    private EventSheet event;
    private List<Menu> menus;

    /**
     * Default constructor.
     * Initializes the menus list to prevent NullPointerExceptions.
     */
    public Note() {
        this.menus = new ArrayList<>();
    }

    /**
     * Parameterized constructor to initialize the note with an event and a list of menus.
     * This matches the explicit "+ Note(e: EventSheet, menus: List<Menu>)" signature defined in the DCD[cite: 2].
     *
     * @param event The event sheet to which this note belongs.
     * @param menus The list of menus to associate with this note.
     */
    public Note(EventSheet event, List<Menu> menus) {
        this.event = event;
        // Inizializzazione sicura: se la lista passata è nulla, crea una nuova ArrayList vuota
        this.menus = (menus != null) ? new ArrayList<>(menus) : new ArrayList<>();
    }

    // ========================================================================
    // GETTERS & SETTERS
    // ========================================================================

    /**
     * Gets the event sheet associated with this note[cite: 2].
     *
     * @return The associated EventSheet.
     */
    public EventSheet getEvent() {
        return event;
    }

    /**
     * Sets the event sheet for this note[cite: 2].
     *
     * @param event The EventSheet to set.
     */
    public void setEvent(EventSheet event) {
        this.event = event;
    }

    /**
     * Gets the list of menus pinned in this note[cite: 2].
     *
     * @return The list of associated menus.
     */
    public List<Menu> getMenus() {
        return menus;
    }

    /**
     * Sets the list of menus for this note[cite: 2].
     *
     * @param menus The new list of menus. If null is passed, an empty list is created.
     */
    public void setMenus(List<Menu> menus) {
        this.menus = (menus != null) ? new ArrayList<>(menus) : new ArrayList<>();
    }

    // ========================================================================
    // UTILITY METHODS FOR COLLECTIONS
    // ========================================================================

    /**
     * Adds a single menu to the note's list of pinned menus.
     *
     * @param m The menu to add.
     */
    public void addMenu(Menu m) {
        if (m != null && !this.menus.contains(m)) {
            this.menus.add(m);
        }
    }

    /**
     * Removes a single menu from the note's list of pinned menus.
     *
     * @param m The menu to remove.
     */
    public void removeMenu(Menu m) {
        if (m != null) {
            this.menus.remove(m);
        }
    }

    // ========================================================================
    // OVERRIDES
    // ========================================================================

    @Override
    public String toString() {
        return "Note [" +
                "event=" + (event != null ? event.getId() : "null") +
                ", menusCount=" + (menus != null ? menus.size() : 0) +
                "]";
    }
}