package plant.event;

import plant.observer.EventType;

public record LineSummary(String lineId, long good, long rejected) implements PlantEvent {

    public LineSummary {
        Require.text(lineId, "lineId");
        Require.nonNegative(good, "good");
        Require.nonNegative(rejected, "rejected");
    }

    @Override
    public EventType type() {
        return EventType.LINE_SUMMARY;
    }
}
