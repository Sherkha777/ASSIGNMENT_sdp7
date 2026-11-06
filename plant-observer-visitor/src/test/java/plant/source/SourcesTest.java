package plant.source;

import org.junit.jupiter.api.Test;
import plant.event.PlantEvent;
import plant.event.ProductionCount;
import plant.event.TelemetryReading;
import plant.observer.EventType;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SourcesTest {

    @Test
    void twoIndependentSourcesNotifyTheirOwnSubscribersOnly() {
        TelemetrySource telemetry = new TelemetrySource();
        ProductionCounterSource counters = new ProductionCounterSource();
        List<PlantEvent> fromTelemetry = new ArrayList<>();
        List<PlantEvent> fromCounters = new ArrayList<>();
        telemetry.subscribe(EventType.TELEMETRY, fromTelemetry::add);
        counters.subscribe(EventType.PRODUCTION, fromCounters::add);

        TelemetryReading reading = new TelemetryReading("S1", "R1", 60, 1);
        ProductionCount count = new ProductionCount("S1", 5, 1, 30, 60);
        telemetry.emit(reading);
        counters.emit(count);

        assertEquals(List.of(reading), fromTelemetry);
        assertEquals(List.of(count), fromCounters);
    }
}
