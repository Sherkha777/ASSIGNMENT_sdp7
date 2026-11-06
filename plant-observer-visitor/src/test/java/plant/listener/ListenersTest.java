package plant.listener;

import org.junit.jupiter.api.Test;
import plant.app.SamplePlant;
import plant.event.AlertRaised;
import plant.event.EmergencyStopped;
import plant.event.LineSummary;
import plant.event.PlantEvent;
import plant.event.ProductionCount;
import plant.event.TelemetryReading;
import plant.model.PlantIndex;
import plant.observer.EventType;
import plant.state.MachineStatus;
import plant.state.StatusBoard;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListenersTest {

    private final StatusBoard board = new StatusBoard();

    @Test
    void vibrationBelowTheLimitRaisesNoAlert() {
        PredictiveMaintenance maintenance = new PredictiveMaintenance(board, 7.0);
        List<PlantEvent> alerts = new ArrayList<>();
        maintenance.subscribe(EventType.ALERT, alerts::add);

        maintenance.onEvent(new TelemetryReading("S1", "R1", 60, 6.9));

        assertTrue(alerts.isEmpty());
        assertEquals(MachineStatus.OK, board.status("R1"));
    }

    @Test
    void vibrationAtTheLimitRaisesExactlyOneAlertAndMarksTheMachineFaulty() {
        PredictiveMaintenance maintenance = new PredictiveMaintenance(board, 7.0);
        List<PlantEvent> alerts = new ArrayList<>();
        maintenance.subscribe(EventType.ALERT, alerts::add);

        maintenance.onEvent(new TelemetryReading("S1", "R1", 60, 7.0));
        maintenance.onEvent(new TelemetryReading("S1", "R1", 60, 9.5));

        assertEquals(1, alerts.size());
        assertEquals("R1", ((AlertRaised) alerts.get(0)).machineId());
        assertEquals(MachineStatus.FAULT, board.status("R1"));
    }

    @Test
    void overheatingStopsTheMachineOnceAndPublishesOneEmergencyEvent() {
        EmergencyStopGuard guard = new EmergencyStopGuard(board, 120.0);
        List<PlantEvent> stops = new ArrayList<>();
        guard.subscribe(EventType.EMERGENCY_STOP, stops::add);

        guard.onEvent(new TelemetryReading("S2", "P1", 119.9, 1));
        assertTrue(stops.isEmpty());
        guard.onEvent(new TelemetryReading("S2", "P1", 120.0, 1));
        guard.onEvent(new TelemetryReading("S2", "P1", 150.0, 1));

        assertEquals(1, stops.size());
        assertEquals("P1", ((EmergencyStopped) stops.get(0)).machineId());
        assertEquals(MachineStatus.STOPPED, board.status("P1"));
    }

    @Test
    void oeeMatchesTheHandCalculation() {
        OeeTracker oee = new OeeTracker(2.5);

        oee.onEvent(new ProductionCount("S1", 90, 10, 45, 60));
        oee.onEvent(new ProductionCount("S1", 80, 20, 55, 60));

        assertEquals(170, oee.goodUnits("S1"));
        assertEquals(0.8333333 * 0.8 * 0.85, oee.oee("S1"), 1e-6);
    }

    @Test
    void oeeIsZeroWithoutData() {
        assertEquals(0.0, new OeeTracker(2.5).oee("S1"), 0.0);
        assertThrows(IllegalArgumentException.class, () -> new OeeTracker(0));
    }

    @Test
    void aggregatorRepublishesCumulativeLineTotalsAsANewEvent() {
        LineAggregator aggregator = new LineAggregator(new PlantIndex(SamplePlant.build()));
        List<PlantEvent> summaries = new ArrayList<>();
        aggregator.subscribe(EventType.LINE_SUMMARY, summaries::add);

        aggregator.onEvent(new ProductionCount("S1", 9, 1, 50, 60));
        aggregator.onEvent(new ProductionCount("S2", 18, 2, 50, 60));
        aggregator.onEvent(new ProductionCount("S3", 5, 0, 50, 60));

        assertEquals(List.of(new LineSummary("L1", 9, 1), new LineSummary("L1", 27, 3),
                new LineSummary("L2", 5, 0)), summaries);
    }

    @Test
    void dashboardKeepsTheLargestSummaryEvenIfAnOlderOneArrivesLate() {
        ScadaDashboard dashboard = new ScadaDashboard();

        dashboard.onEvent(new LineSummary("L1", 20, 2));
        dashboard.onEvent(new LineSummary("L1", 10, 1));

        assertEquals(20, dashboard.lineSummary("L1").orElseThrow().good());
    }
}
