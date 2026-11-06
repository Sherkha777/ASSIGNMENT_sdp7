package plant.event;

import plant.observer.EventType;

import java.util.List;

public record MaintenanceTicket(String stationId, List<String> faultyElementIds, double downtimeCostPerHour,
                                String reason) implements PlantEvent {

    public MaintenanceTicket {
        Require.text(stationId, "stationId");
        Require.nonNegative(downtimeCostPerHour, "downtimeCostPerHour");
        Require.text(reason, "reason");
        faultyElementIds = List.copyOf(faultyElementIds);
    }

    @Override
    public EventType type() {
        return EventType.MAINTENANCE_TICKET;
    }
}
