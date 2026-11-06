package plant.observer;

import org.junit.jupiter.api.Test;
import plant.event.MaintenanceTicket;
import plant.event.ProductionCount;
import plant.event.TelemetryReading;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EventRecordsTest {

    @Test
    void eventsKnowTheirType() {
        assertEquals(EventType.TELEMETRY, new TelemetryReading("S1", "R1", 50, 1).type());
        assertEquals(EventType.PRODUCTION, new ProductionCount("S1", 1, 0, 1, 2).type());
    }

    @Test
    void invalidEventsAreRejectedAtConstruction() {
        assertThrows(IllegalArgumentException.class, () -> new TelemetryReading(" ", "R1", 50, 1));
        assertThrows(IllegalArgumentException.class, () -> new TelemetryReading("S1", "R1", 50, -1));
        assertThrows(IllegalArgumentException.class, () -> new ProductionCount("S1", 1, 0, 90, 60));
    }

    @Test
    void ticketKeepsItsOwnCopyOfTheFaultyList() {
        List<String> ids = new ArrayList<>(List.of("R1"));
        MaintenanceTicket ticket = new MaintenanceTicket("S1", ids, 10.0, "reason");

        ids.add("R2");

        assertEquals(List.of("R1"), ticket.faultyElementIds());
        assertThrows(UnsupportedOperationException.class, () -> ticket.faultyElementIds().add("R3"));
    }
}
