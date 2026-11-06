package plant.event;

import plant.observer.EventType;

public record AlertRaised(String stationId, String machineId, String reason) implements PlantEvent {

    public AlertRaised {
        Require.text(stationId, "stationId");
        Require.text(machineId, "machineId");
        Require.text(reason, "reason");
    }

    @Override
    public EventType type() {
        return EventType.ALERT;
    }
}
