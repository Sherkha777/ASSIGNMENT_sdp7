package plant.event;

import plant.observer.EventType;

public record EmergencyStopped(String stationId, String machineId, double temperatureC) implements PlantEvent {

    public EmergencyStopped {
        Require.text(stationId, "stationId");
        Require.text(machineId, "machineId");
    }

    @Override
    public EventType type() {
        return EventType.EMERGENCY_STOP;
    }
}
