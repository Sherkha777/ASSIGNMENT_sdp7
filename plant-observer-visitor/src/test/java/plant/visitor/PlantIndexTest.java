package plant.visitor;

import org.junit.jupiter.api.Test;
import plant.app.SamplePlant;
import plant.model.PlantIndex;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlantIndexTest {

    private final PlantIndex index = new PlantIndex(SamplePlant.build());

    @Test
    void findsStationsAndTheirLines() {
        assertEquals("Welding Cell", index.station("S1").name());
        assertEquals("L2", index.lineIdOf("S4"));
        assertEquals(List.of("S1", "S2", "S3", "S4"), index.stationIds());
    }

    @Test
    void unknownStationIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> index.station("S99"));
        assertThrows(IllegalArgumentException.class, () -> index.lineIdOf("S99"));
    }
}
