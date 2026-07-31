package catering.businesslogic.event;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
}
