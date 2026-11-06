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

import java.util.ArrayList;
import java.util.List;

public class JsonExportVisitor implements Visitor<String> {

    @Override
    public String visitFactory(Factory factory) {
        return container("Factory", factory);
    }

    @Override
    public String visitLine(Line line) {
        return container("Line", line);
    }

    @Override
    public String visitStation(Station station) {
        return container("Station", station);
    }

    @Override
    public String visitRobot(Robot robot) {
        return leaf("Robot", robot, number("payloadKg", robot.payloadKg()));
    }

    @Override
    public String visitConveyor(Conveyor conveyor) {
        return leaf("Conveyor", conveyor, number("lengthM", conveyor.lengthM()));
    }

    @Override
    public String visitSensor(Sensor sensor) {
        return leaf("Sensor", sensor, text("measures", sensor.measures()));
    }

    @Override
    public String visitPress(Press press) {
        return leaf("Press", press, number("tonnage", press.tonnage()));
    }

    @Override
    public String visitFurnace(Furnace furnace) {
        return leaf("Furnace", furnace, number("setpointC", furnace.setpointC()));
    }

    private String container(String type, Container container) {
        List<String> rendered = new ArrayList<>();
        for (PlantElement child : container.children()) {
            rendered.add(child.accept(this));
        }
        return "{" + identity(type, container) + ",\"children\":[" + String.join(",", rendered) + "]}";
    }

    private String leaf(String type, Equipment equipment, String specific) {
        return "{" + identity(type, equipment)
                + "," + number("powerKw", equipment.powerKw())
                + "," + number("downtimeCostPerHour", equipment.downtimeCostPerHour())
                + "," + specific + "}";
    }

    private String identity(String type, PlantElement element) {
        return text("type", type) + "," + text("id", element.id()) + "," + text("name", element.name());
    }

    private String number(String key, double value) {
        return "\"" + key + "\":" + value;
    }

    private String text(String key, String value) {
        return "\"" + key + "\":\"" + escape(value) + "\"";
    }

    private String escape(String value) {
        StringBuilder escaped = new StringBuilder();
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\n' -> escaped.append("\\n");
                case '\t' -> escaped.append("\\t");
                case '\r' -> escaped.append("\\r");
                default -> {
                    if (c < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) c));
                    } else {
                        escaped.append(c);
                    }
                }
            }
        }
        return escaped.toString();
    }
}
