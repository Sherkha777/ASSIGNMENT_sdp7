package plant.listener;

import plant.event.PlantEvent;
import plant.event.ProductionCount;
import plant.observer.Observer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OeeTracker implements Observer<PlantEvent> {

    private record Totals(long good, long rejected, long runMinutes, long plannedMinutes) {
        Totals plus(Totals other) {
            return new Totals(good + other.good, rejected + other.rejected,
                    runMinutes + other.runMinutes, plannedMinutes + other.plannedMinutes);
        }
    }

    private final Map<String, Totals> totalsByStation = new ConcurrentHashMap<>();
    private final double idealUnitsPerMinute;

    public OeeTracker(double idealUnitsPerMinute) {
        if (idealUnitsPerMinute <= 0) {
            throw new IllegalArgumentException("idealUnitsPerMinute must be positive");
        }
        this.idealUnitsPerMinute = idealUnitsPerMinute;
    }

    @Override
    public void onEvent(PlantEvent event) {
        ProductionCount count = (ProductionCount) event;
        totalsByStation.merge(count.stationId(),
                new Totals(count.good(), count.rejected(), count.runMinutes(), count.plannedMinutes()),
                Totals::plus);
    }

    public double oee(String stationId) {
        Totals totals = totalsByStation.get(stationId);
        if (totals == null || totals.runMinutes() == 0 || totals.good() + totals.rejected() == 0) {
            return 0.0;
        }
        double produced = totals.good() + totals.rejected();
        double availability = (double) totals.runMinutes() / totals.plannedMinutes();
        double performance = Math.min(1.0, produced / (totals.runMinutes() * idealUnitsPerMinute));
        double quality = totals.good() / produced;
        return availability * performance * quality;
    }

    public long goodUnits(String stationId) {
        Totals totals = totalsByStation.get(stationId);
        return totals == null ? 0 : totals.good();
    }
}
