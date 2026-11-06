package plant.state;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StatusBoard {

    private final Map<String, MachineStatus> statuses = new ConcurrentHashMap<>();

    public MachineStatus status(String machineId) {
        return statuses.getOrDefault(machineId, MachineStatus.OK);
    }

    public MachineStatus set(String machineId, MachineStatus status) {
        MachineStatus previous = statuses.put(machineId, status);
        return previous == null ? MachineStatus.OK : previous;
    }

    public boolean tryChange(String machineId, MachineStatus expected, MachineStatus next) {
        boolean[] changed = {false};
        statuses.compute(machineId, (id, current) -> {
            MachineStatus now = current == null ? MachineStatus.OK : current;
            if (now == expected) {
                changed[0] = true;
                return next;
            }
            return current;
        });
        return changed[0];
    }
}
