package plant.visitor;

import org.junit.jupiter.api.Test;
import plant.model.Furnace;
import plant.model.Station;
import plant.model.Robot;
import plant.state.MachineStatus;
import plant.state.StatusBoard;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FurnaceTest {

    private final Station station = new Station("S9", "Heat Cell", List.of(
            new Furnace("FU1", "Annealing Furnace", 80.0, 1200.0, 850.0),
            new Robot("R9", "Loader", 5.0, 100.0, 30.0)));

    @Test
    void existingVisitorsHandleTheNewElementThroughTheirBaseClass() {
        assertEquals(85.0, station.accept(new EnergyVisitor()), 1e-9);
        assertEquals(1300.0, station.accept(new DowntimeCostVisitor()), 1e-9);
        assertEquals(34.0, station.accept(new CarbonFootprintVisitor(0.4)), 1e-9);
        assertEquals(Map.of("Station", 1, "Furnace", 1, "Robot", 1), station.accept(new InventoryVisitor()));
    }

    @Test
    void filterFindsAFaultyFurnace() {
        StatusBoard board = new StatusBoard();
        board.set("FU1", MachineStatus.FAULT);

        assertEquals("FU1", station.accept(new FaultyElementsFilter(board)).get(0).id());
    }

    @Test
    void jsonExportWritesTheFurnaceSpecificField() {
        String json = station.accept(new JsonExportVisitor());

        assertEquals(true, json.contains("{\"type\":\"Furnace\",\"id\":\"FU1\",\"name\":\"Annealing Furnace\","
                + "\"powerKw\":80.0,\"downtimeCostPerHour\":1200.0,\"setpointC\":850.0}"));
    }
}
