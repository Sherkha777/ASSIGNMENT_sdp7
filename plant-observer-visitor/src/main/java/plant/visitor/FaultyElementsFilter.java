package plant.visitor;

import plant.model.Equipment;
import plant.state.MachineStatus;
import plant.state.StatusBoard;

import java.util.ArrayList;
import java.util.List;

public class FaultyElementsFilter extends WalkVisitor<List<Equipment>> {

    private final StatusBoard board;
    private final List<Equipment> faulty = new ArrayList<>();

    public FaultyElementsFilter(StatusBoard board) {
        this.board = board;
    }

    @Override
    protected void onLeaf(Equipment equipment) {
        if (board.status(equipment.id()) != MachineStatus.OK) {
            faulty.add(equipment);
        }
    }

    @Override
    protected List<Equipment> result() {
        return List.copyOf(faulty);
    }
}
