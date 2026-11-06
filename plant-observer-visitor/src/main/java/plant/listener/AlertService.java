package plant.listener;

import plant.event.AlertRaised;
import plant.event.MaintenanceTicket;
import plant.event.PlantEvent;
import plant.model.Equipment;
import plant.model.PlantIndex;
import plant.model.Station;
import plant.observer.EventPublisher;
import plant.observer.Observer;
import plant.state.StatusBoard;
import plant.visitor.DowntimeCostVisitor;
import plant.visitor.FaultyElementsFilter;

import java.util.List;

public class AlertService extends EventPublisher<PlantEvent> implements Observer<PlantEvent> {

    private final PlantIndex index;
    private final StatusBoard board;

    public AlertService(PlantIndex index, StatusBoard board) {
        this.index = index;
        this.board = board;
    }

    @Override
    public void onEvent(PlantEvent event) {
        AlertRaised alert = (AlertRaised) event;
        Station station = index.station(alert.stationId());
        List<Equipment> faulty = station.accept(new FaultyElementsFilter(board));
        double costPerHour = station.accept(new DowntimeCostVisitor());
        publish(new MaintenanceTicket(station.id(), faulty.stream().map(Equipment::id).toList(),
                costPerHour, alert.reason()));
    }
}
