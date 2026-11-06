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

public abstract class FoldVisitor<R> implements Visitor<R> {

    protected abstract R identity();

    protected abstract R combine(R left, R right);

    protected abstract R leaf(Equipment equipment);

    @Override
    public final R visitFactory(Factory factory) {
        return fold(factory);
    }

    @Override
    public final R visitLine(Line line) {
        return fold(line);
    }

    @Override
    public final R visitStation(Station station) {
        return fold(station);
    }

    @Override
    public final R visitRobot(Robot robot) {
        return leaf(robot);
    }

    @Override
    public final R visitConveyor(Conveyor conveyor) {
        return leaf(conveyor);
    }

    @Override
    public final R visitSensor(Sensor sensor) {
        return leaf(sensor);
    }

    @Override
    public final R visitPress(Press press) {
        return leaf(press);
    }

    @Override
    public final R visitFurnace(Furnace furnace) {
        return leaf(furnace);
    }

    private R fold(Container container) {
        R accumulated = identity();
        for (PlantElement child : container.children()) {
            accumulated = combine(accumulated, child.accept(this));
        }
        return accumulated;
    }
}
