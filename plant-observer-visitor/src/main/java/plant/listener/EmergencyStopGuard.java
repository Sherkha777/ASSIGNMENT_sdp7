package plant.listener;

import plant.event.EmergencyStopped;
import plant.event.PlantEvent;
import plant.event.TelemetryReading;
import plant.observer.EventPublisher;
import plant.observer.Observer;
import plant.state.MachineStatus;
import plant.state.StatusBoard;

public class EmergencyStopGuard extends EventPublisher<PlantEvent> implements Observer<PlantEvent> {

    private final StatusBoard board;
    private final double temperatureLimitC;

    public EmergencyStopGuard(StatusBoard board, double temperatureLimitC) {
        this.board = board;
        this.temperatureLimitC = temperatureLimitC;
    }

    @Override
    public void onEvent(PlantEvent event) {
        TelemetryReading reading = (TelemetryReading) event;
        if (reading.temperatureC() < temperatureLimitC) {
            return;
        }
        MachineStatus previous = board.set(reading.machineId(), MachineStatus.STOPPED);
        if (previous != MachineStatus.STOPPED) {
            publish(new EmergencyStopped(reading.stationId(), reading.machineId(), reading.temperatureC()));
        }
    }
}
