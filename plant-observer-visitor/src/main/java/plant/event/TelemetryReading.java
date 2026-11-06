package plant.event;

import plant.observer.EventType;

public record TelemetryReading(String stationId, String machineId, double temperatureC, double vibrationMmS)
        implements PlantEvent {

    public TelemetryReading {
        Require.text(stationId, "stationId");
        Require.text(machineId, "machineId");
        Require.nonNegative(vibrationMmS, "vibrationMmS");
    }

    @Override
    public EventType type() {
        return EventType.TELEMETRY;
    }
}
