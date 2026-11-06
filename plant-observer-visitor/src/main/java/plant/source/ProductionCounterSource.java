package plant.source;

import plant.event.PlantEvent;
import plant.event.ProductionCount;
import plant.observer.EventPublisher;

public class ProductionCounterSource extends EventPublisher<PlantEvent> {

    public void emit(ProductionCount count) {
        publish(count);
    }
}
