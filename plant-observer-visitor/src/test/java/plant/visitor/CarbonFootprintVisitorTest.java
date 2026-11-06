package plant.visitor;

import org.junit.jupiter.api.Test;
import plant.app.SamplePlant;
import plant.model.Factory;
import plant.model.PlantIndex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CarbonFootprintVisitorTest {

    private final Factory plant = SamplePlant.build();

    @Test
    void factoryFootprintIsEnergyTimesTheEmissionFactor() {
        assertEquals(38.8, plant.accept(new CarbonFootprintVisitor(0.4)), 1e-9);
    }

    @Test
    void stationFootprintMatchesHandCalculation() {
        assertEquals(6.2, new PlantIndex(plant).station("S1").accept(new CarbonFootprintVisitor(0.4)), 1e-9);
    }

    @Test
    void negativeFactorIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new CarbonFootprintVisitor(-1));
    }
}
