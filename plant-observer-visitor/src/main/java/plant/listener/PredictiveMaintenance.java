package plant.listener;

import plant.event.AlertRaised;
import plant.event.PlantEvent;
import plant.event.TelemetryReading;
import plant.observer.EventPublisher;
import plant.observer.Observer;
import plant.state.MachineStatus;
import plant.state.StatusBoard;

public class PredictiveMaintenance extends EventPublisher<PlantEvent> implements Observer<PlantEvent> {

    private final StatusBoard board;
    private final double vibrationLimitMmS;

    public PredictiveMaintenance(StatusBoard board, double vibrationLimitMmS) {
        this.board = board;
        this.vibrationLimitMmS = vibrationLimitMmS;
    }

    @Override
    public void onEvent(PlantEvent event) {
        TelemetryReading reading = (TelemetryReading) event;
        boolean overLimit = reading.vibrationMmS() >= vibrationLimitMmS;
        if (overLimit && board.tryChange(reading.machineId(), MachineStatus.OK, MachineStatus.FAULT)) {
            publish(new AlertRaised(reading.stationId(), reading.machineId(),
                    "vibration " + reading.vibrationMmS() + " mm/s reached limit " + vibrationLimitMmS));
        }
    }
}
