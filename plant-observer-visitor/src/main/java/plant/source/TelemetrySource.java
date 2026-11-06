package plant.source;

import plant.event.PlantEvent;
import plant.event.TelemetryReading;
import plant.observer.EventPublisher;

public class TelemetrySource extends EventPublisher<PlantEvent> {

    public void emit(TelemetryReading reading) {
        publish(reading);
    }
}
