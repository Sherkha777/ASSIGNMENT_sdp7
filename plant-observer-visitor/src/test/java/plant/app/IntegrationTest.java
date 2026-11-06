package plant.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import plant.event.MaintenanceTicket;
import plant.event.PlantEvent;
import plant.event.ProductionCount;
import plant.event.TelemetryReading;
import plant.observer.EventType;
import plant.observer.Observer;
import plant.state.MachineStatus;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IntegrationTest {

    private PlantSystem system;

    @BeforeEach
    void setUp() {
        system = new PlantSystem(SamplePlant.build(), PlantConfig.defaults());
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        system.close();
    }

    @Test
    void vibrationAlertTriggersTheVisitorsOnTheRelatedStationOnly() {
        system.telemetry().emit(new TelemetryReading("S1", "R1", 70, 9.0));
        system.telemetry().emit(new TelemetryReading("S3", "R3", 70, 9.0));

        List<MaintenanceTicket> tickets = system.dashboard().tickets();

        assertEquals(2, tickets.size());
        assertEquals("S1", tickets.get(0).stationId());
        assertEquals(List.of("R1"), tickets.get(0).faultyElementIds());
        assertEquals(700.0, tickets.get(0).downtimeCostPerHour(), 1e-9);
        assertEquals("S3", tickets.get(1).stationId());
        assertEquals(List.of("R3"), tickets.get(1).faultyElementIds());
        assertEquals(900.0, tickets.get(1).downtimeCostPerHour(), 1e-9);
    }

    @Test
    void theTicketEventIsPublishedBackIntoTheSystemAndReachesTheDashboard() {
        system.telemetry().emit(new TelemetryReading("S2", "P1", 70, 8.0));

        assertEquals(1, system.dashboard().count(EventType.ALERT));
        assertEquals(1, system.dashboard().count(EventType.MAINTENANCE_TICKET));
        assertEquals(MachineStatus.FAULT, system.board().status("P1"));
    }

    @Test
    void overheatingReachesTheDashboardAsAnEmergencyStop() {
        system.telemetry().emit(new TelemetryReading("S2", "P1", 130, 1.0));

        assertEquals(1, system.dashboard().count(EventType.EMERGENCY_STOP));
        assertEquals(MachineStatus.STOPPED, system.board().status("P1"));
    }

    @Test
    void productionFlowsThroughTheChainOfObservers() {
        system.counters().emit(new ProductionCount("S1", 9, 1, 50, 60));
        system.counters().emit(new ProductionCount("S2", 9, 1, 50, 60));

        assertEquals(2, system.dashboard().count(EventType.PRODUCTION));
        assertEquals(2, system.dashboard().count(EventType.LINE_SUMMARY));
        assertEquals(18, system.dashboard().lineSummary("L1").orElseThrow().good());
        assertEquals(9, system.oee().goodUnits("S1"));
    }

    @Test
    void aListenerCanJoinAndLeaveAtRuntime() {
        AtomicInteger seen = new AtomicInteger();
        Observer<PlantEvent> temporary = e -> seen.incrementAndGet();

        system.telemetry().subscribe(EventType.TELEMETRY, temporary);
        system.telemetry().emit(new TelemetryReading("S1", "R1", 70, 1));
        system.telemetry().unsubscribe(EventType.TELEMETRY, temporary);
        system.telemetry().emit(new TelemetryReading("S1", "R1", 70, 1));

        assertEquals(1, seen.get());
        assertEquals(2, system.dashboard().count(EventType.TELEMETRY));
    }
}
