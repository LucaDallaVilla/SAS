package catering.businesslogic.event;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import catering.businesslogic.UseCaseLogicException;
import catering.businesslogic.menu.Menu;
import catering.businesslogic.user.User;
import catering.persistence.PersistenceManager;

/**
 * Tests for {@link EventSheet}: in-memory aggregate behaviour and the static
 * loaders against the seeded SQLite database.
 */
class EventTest {

    @BeforeAll
    static void initializeDatabase() {
        PersistenceManager.initializeDatabase("database/catering_init_sqlite.sql");
    }

    @Nested
    class Aggregate {

        private EventSheet event;
        private Service first;
        private Service second;

        @BeforeEach
        void setUp() {
            event = new EventSheet("Test Event");
            first = new Service();
            first.setId(1);
            second = new Service();
            second.setId(2);
        }

        @Test
        void testName_SetInConstructor_IsReadable() {
            assertEquals("Test Event", event.getName());
        }

        @Test
        void testChef_SetExplicitly_IsReadableAndExposesId() {
            User chef = new User();
            chef.setId(7);

            event.setChef(chef);

            assertEquals(chef, event.getChef());
            assertEquals(7, event.getChefId());
        }

        @Test
        void testServices_FreshEvent_IsEmpty() {
            assertTrue(event.getServices().isEmpty());
        }

        @Test
        void testAddService_RecordsContainment() {
            event.addService(first);

            assertTrue(event.containsService(first));
            assertFalse(event.containsService(second));
        }

        @Test
        void testRemoveService_DropsOnlyTheTargetedService() {
            event.addService(first);
            event.addService(second);

            event.removeService(first);

            assertFalse(event.containsService(first));
            assertTrue(event.containsService(second));
        }

        @Test
        void testDates_SetExplicitly_AreReadable() {
            Date day = Date.valueOf("2024-05-29");

            event.setDateStart(day);
            event.setDateEnd(day);

            assertEquals(day, event.getDateStart());
            assertEquals(day, event.getDateEnd());
        }
    }

    @Nested
    class StaticLoaders {

        @Test
        void testLoadAllEvents_ReturnsSeededEvents() {
            List<EventSheet> events = EventSheet.loadAllEvents();

            assertNotNull(events);
            assertFalse(events.isEmpty(), "the seed script must populate at least one event");

            EventSheet sample = events.get(0);
            assertNotNull(sample.getName());
            assertNotNull(sample.getDateStart());
            assertNotNull(sample.getChef());
            assertNotNull(sample.getServices());
        }

        @Test
        void testLoadById_RoundTripsTheSameEvent() {
            EventSheet sample = EventSheet.loadAllEvents().get(0);

            EventSheet loaded = EventSheet.loadById(sample.getId());

            assertNotNull(loaded);
            assertEquals(sample.getId(), loaded.getId());
            assertEquals(sample.getName(), loaded.getName());
        }

        @Test
        void testLoadByName_FindsEventByExactName() {
            EventSheet sample = EventSheet.loadAllEvents().get(0);

            EventSheet loaded = EventSheet.loadByName(sample.getName());

            assertNotNull(loaded);
            assertEquals(sample.getName(), loaded.getName());
        }
    }

    // ========================================================================
    // NEW TEST CLASSES
    // ========================================================================

    @Nested
    class ClientTests {

        @Test
        void testEquals_SameFields_ReturnsTrue() {
            Client a = new Client("Mario", "Rossi", "333-1234567");
            Client b = new Client("Mario", "Rossi", "333-1234567");
            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        void testEquals_DifferentFields_ReturnsFalse() {
            Client a = new Client("Mario", "Rossi", "333-1234567");
            Client b = new Client("Luigi", "Bianchi", "333-9999999");
            assertNotEquals(a, b);
        }

        @Test
        void testToString_FormatsCorrectly() {
            Client c = new Client("Mario", "Rossi", "333-1234567");
            assertEquals("Mario Rossi - 333-1234567", c.toString());
        }
    }

    @Nested
    class LocationTests {

        @Test
        void testEquals_SameVenueAndStatus_ReturnsTrue() {
            Location a = new Location("Sala Grande");
            a.setStatus("confermata");
            Location b = new Location("Sala Grande");
            b.setStatus("confermata");
            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        void testEquals_DifferentVenue_ReturnsFalse() {
            Location a = new Location("Sala Grande");
            Location b = new Location("Sala Piccola");
            assertNotEquals(a, b);
        }

        @Test
        void testToString_ContainsVenueAndStatus() {
            Location loc = new Location("Giardino");
            loc.setStatus("da verificare");
            String s = loc.toString();
            assertTrue(s.contains("Giardino"));
            assertTrue(s.contains("da verificare"));
        }
    }

    @Nested
    class StaffTests {

        private Staff staff;

        @BeforeEach
        void setUp() {
            staff = new Staff("Cameriere", "Disponibile", "Servizio Sala");
        }

        @Test
        void testCheckAvailability_DateInList_ReturnsTrue() {
            Date d = Date.valueOf("2025-06-15");
            staff.addAvailableDate(d);
            assertTrue(staff.checkAvailability(d));
        }

        @Test
        void testCheckAvailability_DateNotInList_ReturnsFalse() {
            staff.addAvailableDate(Date.valueOf("2025-06-15"));
            assertFalse(staff.checkAvailability(Date.valueOf("2025-07-01")));
        }

        @Test
        void testCheckAvailability_NullDate_ReturnsFalse() {
            staff.addAvailableDate(Date.valueOf("2025-06-15"));
            assertFalse(staff.checkAvailability(null));
        }

        @Test
        void testCheckAvailability_EmptyList_ReturnsFalse() {
            assertFalse(staff.checkAvailability(Date.valueOf("2025-06-15")));
        }

        @Test
        void testAddAvailableDate_DuplicateNotAdded() {
            Date d = Date.valueOf("2025-06-15");
            staff.addAvailableDate(d);
            staff.addAvailableDate(d);
            assertEquals(1, staff.getAvailableDates().size());
        }

        @Test
        void testEquals_BothWithValidId_ComparesById() {
            Staff a = new Staff(10, "Cameriere", "Disponibile", "Sala");
            Staff b = new Staff(10, "Barman", "Occupato", "Bar");
            assertEquals(a, b, "Same ID should be equal regardless of other fields");
        }

        @Test
        void testEquals_NoId_ComparesByFields() {
            Staff a = new Staff("Cameriere", "Disponibile", "Sala");
            Staff b = new Staff("Cameriere", "Disponibile", "Sala");
            assertEquals(a, b);
        }
    }

    @Nested
    class FineConditionsTests {

        private FineConditions fc;

        @BeforeEach
        void setUp() {
            // maxDeltaParticipants=30, maxDaysNotice=7
            fc = new FineConditions(30, 7);
        }

        @Test
        void testCheckParticipantsVariation_BelowThreshold_ReturnsFalse() {
            assertFalse(fc.checkParticipantsVariation(10));
        }

        @Test
        void testCheckParticipantsVariation_AtThreshold_ReturnsFalse() {
            assertFalse(fc.checkParticipantsVariation(30));
        }

        @Test
        void testCheckParticipantsVariation_AboveThreshold_ReturnsTrue() {
            assertTrue(fc.checkParticipantsVariation(50));
        }

        @Test
        void testCheckDaysNotice_EventTomorrow_ReturnsTrue() {
            // 1 day from now is < 7, so returns true (fine required)
            Date tomorrow = Date.valueOf(
                    java.time.LocalDate.now().plusDays(1));
            assertTrue(fc.checkDaysNotice(tomorrow));
        }

        @Test
        void testCheckDaysNotice_EventFarAway_ReturnsFalse() {
            // 60 days from now is >= 7, so returns false (no fine)
            Date farAway = Date.valueOf(
                    java.time.LocalDate.now().plusDays(60));
            assertFalse(fc.checkDaysNotice(farAway));
        }
    }

    @Nested
    class ServiceDataTests {

        @Test
        void testAccessors_ReturnConstructorValues() {
            Date d = Date.valueOf("2025-06-15");
            Time ts = Time.valueOf("12:00:00");
            Time te = Time.valueOf("15:00:00");
            ServiceData sd = new ServiceData("Pranzo", d, ts, te);

            assertEquals("Pranzo", sd.type());
            assertEquals(d, sd.date());
            assertEquals(ts, sd.timeStart());
            assertEquals(te, sd.timeEnd());
        }

        @Test
        void testEquals_SameValues_ReturnsTrue() {
            Date d = Date.valueOf("2025-06-15");
            Time ts = Time.valueOf("12:00:00");
            Time te = Time.valueOf("15:00:00");
            ServiceData a = new ServiceData("Pranzo", d, ts, te);
            ServiceData b = new ServiceData("Pranzo", d, ts, te);
            assertEquals(a, b);
        }
    }

    @Nested
    class NoteTests {

        @Test
        void testConstructor_NullMenuList_CreatesEmptyList() {
            Note note = new Note(new EventSheet("E"), null);
            assertNotNull(note.getMenus());
            assertTrue(note.getMenus().isEmpty());
        }

        @Test
        void testAddMenu_NullMenu_NotAdded() {
            Note note = new Note();
            note.addMenu(null);
            assertTrue(note.getMenus().isEmpty());
        }

        @Test
        void testAddMenu_DuplicateMenu_NotAdded() {
            User owner = new User("chef");
            owner.setId(1);
            owner.addRole(User.Role.CHEF);
            Menu menu = new Menu(owner, "Test Menu");

            Note note = new Note();
            note.addMenu(menu);
            note.addMenu(menu);
            assertEquals(1, note.getMenus().size());
        }

        @Test
        void testRemoveMenu_PresentMenu_Removed() {
            User owner = new User("chef");
            owner.setId(1);
            owner.addRole(User.Role.CHEF);
            Menu menu = new Menu(owner, "Test Menu");

            Note note = new Note();
            note.addMenu(menu);
            note.removeMenu(menu);
            assertTrue(note.getMenus().isEmpty());
        }

        @Test
        void testSetMenus_Null_CreatesEmptyList() {
            Note note = new Note();
            note.setMenus(null);
            assertNotNull(note.getMenus());
            assertTrue(note.getMenus().isEmpty());
        }
    }

    @Nested
    class RecurringEventSheetTests {

        @Test
        void testConstructor_InitializesFieldsCorrectly() {
            Date finalDate = Date.valueOf("2025-12-31");
            RecurringEventSheet rec = new RecurringEventSheet(7, finalDate);

            assertEquals(7, rec.getFrequency());
            assertEquals(finalDate, rec.getFinalDate());
            assertNotNull(rec.getEvents());
            assertTrue(rec.getEvents().isEmpty());
        }

        @Test
        void testAddEvent_ValidEvent_IsAdded() {
            RecurringEventSheet rec = new RecurringEventSheet(7, Date.valueOf("2025-12-31"));
            EventSheet event = new EventSheet("Recurring");
            rec.addEvent(event);
            assertEquals(1, rec.getEvents().size());
            assertSame(event, rec.getEvents().get(0));
        }

        @Test
        void testAddEvent_NullEvent_NotAdded() {
            RecurringEventSheet rec = new RecurringEventSheet(7, Date.valueOf("2025-12-31"));
            rec.addEvent(null);
            assertTrue(rec.getEvents().isEmpty());
        }
    }

    @Nested
    class ServiceTests {

        @Test
        void testConstructorFromServiceData_PopulatesFields() {
            Date d = Date.valueOf("2025-06-15");
            Time ts = Time.valueOf("12:00:00");
            Time te = Time.valueOf("15:00:00");
            ServiceData sd = new ServiceData("Pranzo", d, ts, te);

            Service svc = new Service(sd);

            assertEquals("Pranzo", svc.getType());
            assertEquals(d, svc.getDate());
            assertEquals(ts, svc.getTimeStart());
            assertEquals(te, svc.getTimeEnd());
        }

        @Test
        void testEdit_OverwritesFieldsButNotStatus() {
            Service original = new Service("Original");
            original.setStatus("attivo");
            original.setLocation("Sala A");

            Service updated = new Service("Updated");
            updated.setLocation("Sala B");
            updated.setDate(Date.valueOf("2025-07-01"));

            original.edit(updated);

            assertEquals("Updated", original.getName());
            assertEquals("Sala B", original.getLocation());
            assertEquals(Date.valueOf("2025-07-01"), original.getDate());
            assertEquals("attivo", original.getStatus(),
                    "edit() must not overwrite the status");
        }

        @Test
        void testRemoveMenu_SetsMenuToNull() {
            Service svc = new Service("Svc");
            User owner = new User("chef");
            owner.setId(1);
            owner.addRole(User.Role.CHEF);
            svc.setMenu(new Menu(owner, "Menu1"));
            assertNotNull(svc.getMenu());

            svc.removeMenu();

            assertNull(svc.getMenu());
        }

        @Test
        void testGetMenuItems_NullMenu_ReturnsEmptyList() {
            Service svc = new Service("Svc");
            assertNotNull(svc.getMenuItems());
            assertTrue(svc.getMenuItems().isEmpty());
        }

        @Test
        void testCreateLocation_SetsLocationField() {
            Service svc = new Service("Svc");
            svc.createLocation("Giardino");
            assertEquals("Giardino", svc.getLocation());
        }

        @Test
        void testAddStaff_NullList_DoesNotThrow() {
            Service svc = new Service("Svc");
            assertDoesNotThrow(() -> svc.addStaff(null));
            assertTrue(svc.getStaffList().isEmpty());
        }
    }

    @Nested
    class EventSheetExtendedTests {

        private EventSheet event;

        @BeforeEach
        void setUp() {
            event = new EventSheet("Extended Test");
        }

        @Test
        void testSetChef_StatusSchedaSalvata_AdvancesToChefAssegnato() {
            event.setStatus(EventStatus.SCHEDA_SALVATA);
            User chef = new User("chef");
            chef.setId(5);

            event.setChef(chef);

            assertEquals(EventStatus.CHEF_ASSEGNATO, event.getStatus());
        }

        @Test
        void testCancelServices_SetsAllServiceStatusToCancellato() {
            Service s1 = new Service("S1");
            s1.setStatus("attivo");
            Service s2 = new Service("S2");
            s2.setStatus("attivo");
            event.addService(s1);
            event.addService(s2);

            event.cancelServices();

            assertEquals("cancellato", s1.getStatus());
            assertEquals("cancellato", s2.getStatus());
        }

        @Test
        void testEdit_NullArgument_NoOp() {
            event.setName("Original");
            event.edit(null);
            assertEquals("Original", event.getName());
        }

        @Test
        void testEdit_UpdatesOnlyNonNullPositiveFields() {
            event.setDateStart(Date.valueOf("2025-01-01"));
            event.setNumParticipants(100);

            EventSheet update = new EventSheet();
            update.setName("New Name");
            // dateStart is null in update, numParticipants is 0

            event.edit(update);

            assertEquals("New Name", event.getName());
            // dateStart should be overwritten to null since update.getDateStart() is null
            // but numParticipants should stay 100 since update has 0
            assertEquals(100, event.getNumParticipants());
        }

        @Test
        void testAddNote_CreatesAndAddsNote() {
            User owner = new User("chef");
            owner.setId(1);
            owner.addRole(User.Role.CHEF);
            List<Menu> menus = List.of(new Menu(owner, "M1"));

            event.addNote(menus);

            assertNotNull(event.getNotes());
            assertEquals(1, event.getNotes().size());
        }

        @Test
        void testBookStaff_SetsStatusToPersonalePrenotato() {
            event.bookStaff(new ArrayList<>());
            assertEquals(EventStatus.PERSONALE_PRENOTATO, event.getStatus());
        }

        @Test
        void testSetServiceLocation_ServiceNotInEvent_ThrowsIllegalArgumentException() {
            Service outsider = new Service("Outside");
            assertThrows(IllegalArgumentException.class,
                    () -> event.setServiceLocation("Sala", outsider));
        }

        @Test
        void testSetServiceLocation_ServiceInEvent_SetsLocation() {
            Service svc = new Service("Inside");
            event.addService(svc);

            event.setServiceLocation("Terrazza", svc);

            assertEquals("Terrazza", svc.getLocation());
        }

        @Test
        void testAddService_NullService_NotAdded() {
            event.addService(null);
            assertTrue(event.getServices().isEmpty());
        }

        @Test
        void testAddService_DuplicateService_NotAdded() {
            Service svc = new Service();
            svc.setId(99);
            event.addService(svc);
            event.addService(svc);
            assertEquals(1, event.getServices().size());
        }

        @Test
        void testConstructor_WithServiceData_CreatesServicesAndSetsStatus() {
            ArrayList<ServiceData> data = new ArrayList<>();
            data.add(new ServiceData("Pranzo", Date.valueOf("2025-06-15"),
                    Time.valueOf("12:00:00"), Time.valueOf("15:00:00")));
            data.add(new ServiceData("Cena", Date.valueOf("2025-06-15"),
                    Time.valueOf("19:00:00"), Time.valueOf("23:00:00")));

            EventSheet es = new EventSheet(Date.valueOf("2025-06-15"),
                    Date.valueOf("2025-06-16"), 50, data);

            assertEquals(EventStatus.SCHEDA_SALVATA, es.getStatus());
            assertEquals(2, es.getServices().size());
            assertEquals(50, es.getNumParticipants());
        }

        @Test
        void testGetChefId_NoChef_ReturnsZero() {
            assertEquals(0, event.getChefId());
        }
    }

    @Nested
    class EventManagerTests {

        private EventManager manager;

        @BeforeEach
        void setUp() {
            manager = new EventManager();
        }

        @Test
        void testCreateService_NoCurrentEvent_ThrowsUseCaseLogicException() {
            assertThrows(UseCaseLogicException.class,
                    () -> manager.createService("S", Date.valueOf("2025-06-15"),
                            Time.valueOf("12:00:00"), Time.valueOf("15:00:00"), "Sala"));
        }

        @Test
        void testApproveMenu_NoCurrentEvent_ThrowsUseCaseLogicException() {
            assertThrows(UseCaseLogicException.class,
                    () -> manager.approveMenu(new Service("S"), null));
        }

        @Test
        void testApproveMenu_NullService_ThrowsUseCaseLogicException() {
            // First set a current event so we pass the first check
            EventSheet event = new EventSheet("E");
            manager.setCurrentEvent(event);

            assertThrows(UseCaseLogicException.class,
                    () -> manager.approveMenu(null, null));
        }

        @Test
        void testRemoveMenu_NullService_ReturnsFalse() {
            assertFalse(manager.removeMenu(null));
        }

        @Test
        void testRemoveMenu_ValidService_ReturnsTrue() {
            Service svc = new Service("S");
            User owner = new User("chef");
            owner.setId(1);
            owner.addRole(User.Role.CHEF);
            svc.setMenu(new Menu(owner, "M"));

            assertTrue(manager.removeMenu(svc));
            assertNull(svc.getMenu());
        }

        @Test
        void testDeleteService_NullEvent_ReturnsFalse() {
            assertFalse(manager.deleteService(null, new Service("S")));
        }

        @Test
        void testDeleteService_NullService_ReturnsFalse() {
            assertFalse(manager.deleteService(new EventSheet("E"), null));
        }

        @Test
        void testDeleteService_ValidInputs_ReturnsTrue() {
            EventSheet event = new EventSheet("E");
            Service svc = new Service("S");
            event.addService(svc);

            assertTrue(manager.deleteService(event, svc));
            assertFalse(event.containsService(svc));
        }

        @Test
        void testAssignChef_NoCurrentEvent_ThrowsUseCaseLogicException() {
            assertThrows(UseCaseLogicException.class,
                    () -> manager.assignChef(new User("chef")));
        }

        @Test
        void testEndEvent_NoCurrentEvent_ThrowsUseCaseLogicException() {
            assertThrows(UseCaseLogicException.class,
                    () -> manager.endEvent());
        }

        @Test
        void testSuggestNewMenu_WrongStatus_ThrowsUseCaseLogicException() {
            EventSheet event = new EventSheet("E");
            event.setStatus(EventStatus.SCHEDA_SALVATA);
            manager.setCurrentEvent(event);

            User owner = new User("chef");
            owner.setId(1);
            owner.addRole(User.Role.CHEF);

            assertThrows(UseCaseLogicException.class,
                    () -> manager.suggestNewMenu(new Menu(owner, "M"), new Service("S")));
        }

        @Test
        void testSuggestNewMenu_CorrectStatus_ChangesToEventoInCorso() throws UseCaseLogicException {
            EventSheet event = new EventSheet("E");
            event.setStatus(EventStatus.PERSONALE_PRENOTATO);
            manager.setCurrentEvent(event);

            User owner = new User("chef");
            owner.setId(1);
            owner.addRole(User.Role.CHEF);

            manager.suggestNewMenu(new Menu(owner, "M"), new Service("S"));

            assertEquals(EventStatus.IN_CORSO, event.getStatus());
        }

        @Test
        void testFillEventSheet_CreatesEventAndSetsAsCurrent() {
            ArrayList<ServiceData> data = new ArrayList<>();
            data.add(new ServiceData("Pranzo", Date.valueOf("2025-06-15"),
                    Time.valueOf("12:00:00"), Time.valueOf("15:00:00")));

            EventSheet created = manager.fillEventSheet(
                    Date.valueOf("2025-06-15"), Date.valueOf("2025-06-16"),
                    50, data);

            assertNotNull(created);
            assertSame(created, manager.getCurrentEvent());
        }

        @Test
        void testEventReceiverNotification_FillEventSheet_NotifiesCreated() {
            AtomicInteger createdCount = new AtomicInteger(0);

            EventReceiver stubReceiver = new EventReceiver() {
                @Override public void updateEventCreated(EventSheet event) {
                    createdCount.incrementAndGet();
                }
                @Override public void updateEventModified(EventSheet event) {}
                @Override public void updateEventDeleted(EventSheet event) {}
                @Override public void updateServiceCreated(EventSheet event, Service service) {}
                @Override public void updateServiceModified(Service service) {}
                @Override public void updateServiceDeleted(Service service) {}
                @Override public void updateMenuAssigned(Service service, Menu menu) {}
                @Override public void updateMenuRemoved(Service service) {}
                @Override public void updatePersonalePrenotato(Service s, User p) {}
                @Override public void updateCuocoAssegnato(Service s, User c) {}
                @Override public void updateRuoloAssegnato(Service s, User p, String ruolo) {}
            };

            manager.addEventReceiver(stubReceiver);

            manager.fillEventSheet(Date.valueOf("2025-06-15"),
                    Date.valueOf("2025-06-16"), 50, new ArrayList<>());

            assertEquals(1, createdCount.get(),
                    "EventReceiver.updateEventCreated must be called once");
        }

        @Test
        void testAddEventReceiver_NullReceiver_NotAdded() {
            // Should not throw and internal list should remain functional
            assertDoesNotThrow(() -> manager.addEventReceiver(null));
            // Verify the manager still works (fillEventSheet doesn't NPE)
            assertDoesNotThrow(() -> manager.fillEventSheet(
                    Date.valueOf("2025-06-15"), Date.valueOf("2025-06-16"),
                    50, new ArrayList<>()));
        }

        @Test
        void testPrepareLocation_NoCurrentEvent_ThrowsUseCaseLogicException() {
            assertThrows(UseCaseLogicException.class,
                    () -> manager.prepareLocation("Sala", new Service("S")));
        }

        @Test
        void testPinEventAndMenus_NoCurrentEvent_ThrowsUseCaseLogicException() {
            assertThrows(UseCaseLogicException.class,
                    () -> manager.pinEventAndMenus(new ArrayList<>()));
        }
    }
}
