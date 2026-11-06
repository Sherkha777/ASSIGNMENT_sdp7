package plant.app;

import plant.model.Conveyor;
import plant.model.Factory;
import plant.model.Line;
import plant.model.Press;
import plant.model.Robot;
import plant.model.Sensor;
import plant.model.Station;

import java.util.List;

public final class SamplePlant {

    private SamplePlant() {
    }

    public static Factory build() {
        Station welding = new Station("S1", "Welding Cell", List.of(
                new Robot("R1", "Weld Robot", 12.0, 500.0, 150.0),
                new Conveyor("C1", "Feed Belt", 3.0, 200.0, 12.0),
                new Sensor("SN1", "Weld Vibration", 0.5, 0.0, "vibration")));
        Station pressing = new Station("S2", "Pressing Cell", List.of(
                new Press("P1", "Body Press", 45.0, 900.0, 800.0),
                new Sensor("SN2", "Press Temperature", 0.5, 0.0, "temperature"),
                new Conveyor("C2", "Exit Belt", 3.0, 200.0, 8.0)));
        Station spraying = new Station("S3", "Spray Cell", List.of(
                new Robot("R2", "Spray Robot A", 10.0, 450.0, 80.0),
                new Robot("R3", "Spray Robot B", 10.0, 450.0, 80.0),
                new Sensor("SN3", "Booth Humidity", 0.5, 0.0, "humidity")));
        Station packing = new Station("S4", "Packing Cell", List.of(
                new Conveyor("C3", "Pack Belt", 4.0, 250.0, 20.0),
                new Robot("R4", "Pack Robot", 8.0, 300.0, 40.0),
                new Sensor("SN4", "Pack Counter", 0.5, 0.0, "count")));
        Line body = new Line("L1", "Body Line", List.of(welding, pressing));
        Line paint = new Line("L2", "Paint Line", List.of(spraying, packing));
        return new Factory("F1", "Astana Works", List.of(body, paint));
    }
}
