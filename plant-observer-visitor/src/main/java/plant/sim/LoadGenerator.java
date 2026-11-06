package plant.sim;

import plant.event.ProductionCount;
import plant.event.TelemetryReading;
import plant.model.Equipment;
import plant.model.PlantIndex;
import plant.model.Station;
import plant.source.ProductionCounterSource;
import plant.source.TelemetrySource;

import java.util.ArrayList;
import java.util.List;

public final class LoadGenerator {

    public static final double CALM_TEMPERATURE_C = 70.0;
    public static final double CALM_VIBRATION_MMS = 1.0;
    public static final long GOOD_PER_COUNT = 9;
    public static final long REJECTED_PER_COUNT = 1;

    private final PlantIndex index;
    private final TelemetrySource telemetry;
    private final ProductionCounterSource counters;

    public LoadGenerator(PlantIndex index, TelemetrySource telemetry, ProductionCounterSource counters) {
        this.index = index;
        this.telemetry = telemetry;
        this.counters = counters;
    }

    public List<Runnable> calmProducers(int producers, int eventsEach) {
        List<Runnable> tasks = new ArrayList<>();
        for (int p = 0; p < producers; p++) {
            tasks.add(calmProducer(p, eventsEach));
        }
        return tasks;
    }

    private Runnable calmProducer(int producerIndex, int eventsEach) {
        return () -> {
            List<String> stationIds = index.stationIds();
            for (int i = 0; i < eventsEach; i++) {
                Station station = index.station(stationIds.get((producerIndex + i) % stationIds.size()));
                Equipment machine = station.children().get(i % station.children().size());
                telemetry.emit(new TelemetryReading(station.id(), machine.id(), CALM_TEMPERATURE_C, CALM_VIBRATION_MMS));
                counters.emit(new ProductionCount(station.id(), GOOD_PER_COUNT, REJECTED_PER_COUNT, 50, 60));
            }
        };
    }
}
