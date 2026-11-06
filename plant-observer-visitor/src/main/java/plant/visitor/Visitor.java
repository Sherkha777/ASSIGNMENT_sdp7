package plant.visitor;

import plant.model.Conveyor;
import plant.model.Factory;
import plant.model.Furnace;
import plant.model.Line;
import plant.model.Press;
import plant.model.Robot;
import plant.model.Sensor;
import plant.model.Station;

public interface Visitor<R> {

    R visitFactory(Factory factory);

    R visitLine(Line line);

    R visitStation(Station station);

    R visitRobot(Robot robot);

    R visitConveyor(Conveyor conveyor);

    R visitSensor(Sensor sensor);

    R visitPress(Press press);

    R visitFurnace(Furnace furnace);
}
