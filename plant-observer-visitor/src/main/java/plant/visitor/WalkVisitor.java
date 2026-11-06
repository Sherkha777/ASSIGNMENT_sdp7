package plant.visitor;

import plant.model.Conveyor;
import plant.model.Container;
import plant.model.Equipment;
import plant.model.Factory;
import plant.model.Furnace;
import plant.model.Line;
import plant.model.PlantElement;
import plant.model.Press;
import plant.model.Robot;
import plant.model.Sensor;
import plant.model.Station;

public abstract class WalkVisitor<R> implements Visitor<R> {

    protected abstract void onLeaf(Equipment equipment);

    protected void onContainer(Container container) {
    }

    protected abstract R result();

    @Override
    public final R visitFactory(Factory factory) {
        return walk(factory);
    }

    @Override
    public final R visitLine(Line line) {
        return walk(line);
    }

    @Override
    public final R visitStation(Station station) {
        return walk(station);
    }

    @Override
    public final R visitRobot(Robot robot) {
        return visitLeaf(robot);
    }

    @Override
    public final R visitConveyor(Conveyor conveyor) {
        return visitLeaf(conveyor);
    }

    @Override
    public final R visitSensor(Sensor sensor) {
        return visitLeaf(sensor);
    }

    @Override
    public final R visitPress(Press press) {
        return visitLeaf(press);
    }

    @Override
    public final R visitFurnace(Furnace furnace) {
        return visitLeaf(furnace);
    }

    private R visitLeaf(Equipment equipment) {
        onLeaf(equipment);
        return result();
    }

    private R walk(Container container) {
        onContainer(container);
        for (PlantElement child : container.children()) {
            child.accept(this);
        }
        return result();
    }
}
