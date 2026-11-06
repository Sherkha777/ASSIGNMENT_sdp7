package plant.visitor;

import org.junit.jupiter.api.Test;
import plant.app.SamplePlant;
import plant.model.Conveyor;
import plant.model.Equipment;
import plant.model.Factory;
import plant.model.Furnace;
import plant.model.Line;
import plant.model.PlantIndex;
import plant.model.Press;
import plant.model.Robot;
import plant.model.Sensor;
import plant.model.Station;
import plant.state.MachineStatus;
import plant.state.StatusBoard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VisitorTest {

    private final Factory plant = SamplePlant.build();
    private final PlantIndex index = new PlantIndex(plant);

    @Test
    void energyOfTheWholeFactoryIsTheSumOfAllLeaves() {
        assertEquals(97.0, plant.accept(new EnergyVisitor()), 1e-9);
    }

    @Test
    void energyOfOneLineAndOneStationMatchesHandCalculation() {
        Line body = plant.children().get(0);
        assertEquals(64.0, body.accept(new EnergyVisitor()), 1e-9);
        assertEquals(15.5, index.station("S1").accept(new EnergyVisitor()), 1e-9);
        assertEquals(48.5, index.station("S2").accept(new EnergyVisitor()), 1e-9);
    }

    @Test
    void downtimeCostMatchesHandCalculation() {
        assertEquals(3250.0, plant.accept(new DowntimeCostVisitor()), 1e-9);
        assertEquals(900.0, index.station("S3").accept(new DowntimeCostVisitor()), 1e-9);
    }

    @Test
    void inventoryAccumulatesCountsAcrossTheWholeTree() {
        Map<String, Integer> counts = plant.accept(new InventoryVisitor());

        assertEquals(Map.of("Factory", 1, "Line", 2, "Station", 4,
                "Robot", 4, "Conveyor", 3, "Sensor", 4, "Press", 1), counts);
    }

    @Test
    void aSecondInventoryRunStartsFromZero() {
        plant.accept(new InventoryVisitor());

        assertEquals(Map.of("Station", 1, "Robot", 2, "Sensor", 1), index.station("S3").accept(new InventoryVisitor()));
    }

    @Test
    void filterReturnsOnlyElementsThatAreNotOk() {
        StatusBoard board = new StatusBoard();
        board.set("R3", MachineStatus.FAULT);
        board.set("P1", MachineStatus.STOPPED);

        List<Equipment> faulty = plant.accept(new FaultyElementsFilter(board));

        assertEquals(List.of("P1", "R3"), faulty.stream().map(Equipment::id).sorted().toList());
    }

    @Test
    void filterOnASubtreeIgnoresFaultsOutsideIt() {
        StatusBoard board = new StatusBoard();
        board.set("R3", MachineStatus.FAULT);
        board.set("P1", MachineStatus.FAULT);

        List<Equipment> faulty = index.station("S3").accept(new FaultyElementsFilter(board));

        assertEquals(List.of("R3"), faulty.stream().map(Equipment::id).toList());
    }

    @Test
    void jsonExportOfOneStationHasCorrectNesting() {
        Station station = new Station("S9", "Test \"Cell\"", List.of(
                new Robot("R9", "Arm", 1.5, 10.0, 20.0),
                new Sensor("SN9", "Probe", 0.5, 0.0, "heat")));

        String json = station.accept(new JsonExportVisitor());

        assertEquals("{\"type\":\"Station\",\"id\":\"S9\",\"name\":\"Test \\\"Cell\\\"\",\"children\":["
                + "{\"type\":\"Robot\",\"id\":\"R9\",\"name\":\"Arm\",\"powerKw\":1.5,\"downtimeCostPerHour\":10.0,\"payloadKg\":20.0},"
                + "{\"type\":\"Sensor\",\"id\":\"SN9\",\"name\":\"Probe\",\"powerKw\":0.5,\"downtimeCostPerHour\":0.0,\"measures\":\"heat\"}"
                + "]}", json);
    }

    @Test
    void jsonExportOfTheFactoryNestsFourLevelsWithBalancedBrackets() {
        String json = plant.accept(new JsonExportVisitor());

        assertEquals(count(json, '{'), count(json, '}'));
        assertEquals(count(json, '['), count(json, ']'));
        assertEquals(1, occurrences(json, "\"type\":\"Factory\""));
        assertEquals(2, occurrences(json, "\"type\":\"Line\""));
        assertEquals(4, occurrences(json, "\"type\":\"Station\""));
        assertEquals(12, occurrences(json, "\"powerKw\""));
        assertEquals(true, json.indexOf("\"type\":\"Line\"") > json.indexOf("\"type\":\"Factory\""));
    }

    @Test
    void doubleDispatchCallsTheMethodOfTheRealElementType() {
        List<String> calls = new ArrayList<>();
        Visitor<Void> recorder = new Visitor<>() {
            public Void visitFactory(Factory f) { calls.add("factory"); return null; }
            public Void visitLine(Line l) { calls.add("line"); return null; }
            public Void visitStation(Station s) { calls.add("station"); return null; }
            public Void visitRobot(Robot r) { calls.add("robot"); return null; }
            public Void visitConveyor(Conveyor c) { calls.add("conveyor"); return null; }
            public Void visitSensor(Sensor s) { calls.add("sensor"); return null; }
            public Void visitPress(Press p) { calls.add("press"); return null; }
            public Void visitFurnace(Furnace f) { calls.add("furnace"); return null; }
        };
        Equipment asEquipment = new Press("P9", "Press", 1, 1, 1);
        Equipment other = new Robot("R9", "Arm", 1, 1, 1);

        asEquipment.accept(recorder);
        other.accept(recorder);
        plant.accept(recorder);

        assertEquals(List.of("press", "robot", "factory"), calls);
    }

    private static int count(String text, char c) {
        return (int) text.chars().filter(x -> x == c).count();
    }

    private static int occurrences(String text, String part) {
        int count = 0;
        for (int i = text.indexOf(part); i >= 0; i = text.indexOf(part, i + 1)) {
            count++;
        }
        return count;
    }
}
