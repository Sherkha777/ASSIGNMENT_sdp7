package plant.listener;

import plant.event.LineSummary;
import plant.event.MaintenanceTicket;
import plant.event.PlantEvent;
import plant.event.TelemetryReading;
import plant.observer.EventType;
import plant.observer.Observer;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.LongAdder;

public class ScadaDashboard implements Observer<PlantEvent> {

    private final Map<EventType, LongAdder> counts = new EnumMap<>(EventType.class);
    private final Map<String, TelemetryReading> latestReading = new ConcurrentHashMap<>();
    private final Map<String, LineSummary> latestSummary = new ConcurrentHashMap<>();
    private final List<MaintenanceTicket> tickets = new CopyOnWriteArrayList<>();

    public ScadaDashboard() {
        for (EventType type : EventType.values()) {
            counts.put(type, new LongAdder());
        }
    }

    @Override
    public void onEvent(PlantEvent event) {
        counts.get(event.type()).increment();
        if (event instanceof TelemetryReading reading) {
            latestReading.put(reading.machineId(), reading);
        } else if (event instanceof LineSummary summary) {
            latestSummary.merge(summary.lineId(), summary, ScadaDashboard::larger);
        } else if (event instanceof MaintenanceTicket ticket) {
            tickets.add(ticket);
        }
    }

    public long count(EventType type) {
        return counts.get(type).sum();
    }

    public Optional<TelemetryReading> latestReading(String machineId) {
        return Optional.ofNullable(latestReading.get(machineId));
    }

    public Optional<LineSummary> lineSummary(String lineId) {
        return Optional.ofNullable(latestSummary.get(lineId));
    }

    public List<MaintenanceTicket> tickets() {
        return List.copyOf(tickets);
    }

    private static LineSummary larger(LineSummary current, LineSummary incoming) {
        return incoming.good() + incoming.rejected() >= current.good() + current.rejected() ? incoming : current;
    }
}
