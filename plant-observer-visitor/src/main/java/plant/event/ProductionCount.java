package plant.event;

import plant.observer.EventType;

public record ProductionCount(String stationId, long good, long rejected, long runMinutes, long plannedMinutes)
        implements PlantEvent {

    public ProductionCount {
        Require.text(stationId, "stationId");
        Require.nonNegative(good, "good");
        Require.nonNegative(rejected, "rejected");
        Require.nonNegative(runMinutes, "runMinutes");
        if (plannedMinutes <= 0 || runMinutes > plannedMinutes) {
            throw new IllegalArgumentException("plannedMinutes must be positive and not below runMinutes");
        }
    }

    @Override
    public EventType type() {
        return EventType.PRODUCTION;
    }
}
