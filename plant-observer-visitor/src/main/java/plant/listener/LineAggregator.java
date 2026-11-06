package plant.listener;

import plant.event.LineSummary;
import plant.event.PlantEvent;
import plant.event.ProductionCount;
import plant.model.PlantIndex;
import plant.observer.EventPublisher;
import plant.observer.Observer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LineAggregator extends EventPublisher<PlantEvent> implements Observer<PlantEvent> {

    private final PlantIndex index;
    private final Map<String, LineSummary> totalsByLine = new ConcurrentHashMap<>();

    public LineAggregator(PlantIndex index) {
        this.index = index;
    }

    @Override
    public void onEvent(PlantEvent event) {
        ProductionCount count = (ProductionCount) event;
        String lineId = index.lineIdOf(count.stationId());
        LineSummary updated = totalsByLine.merge(lineId,
                new LineSummary(lineId, count.good(), count.rejected()),
                (current, added) -> new LineSummary(lineId, current.good() + added.good(),
                        current.rejected() + added.rejected()));
        publish(updated);
    }
}
