package plant.app;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DemoAppTest {

    private String run(String... lines) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (PlantSystem system = new PlantSystem(SamplePlant.build(), PlantConfig.defaults());
             PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8)) {
            new DemoApp(system, new BufferedReader(new StringReader(String.join("\n", lines) + "\n")), out).run();
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void simulationShowsExactCountsOnTheDashboard() throws Exception {
        String output = run("1", "4", "500", "0");

        assertTrue(output.contains("TELEMETRY: 2000"));
        assertTrue(output.contains("PRODUCTION: 2000"));
        assertTrue(output.contains("LINE_SUMMARY: 2000"));
    }

    @Test
    void vibrationReadingProducesATicketForTheRightStation() throws Exception {
        String output = run("3", "S3", "R3", "9", "0");

        assertTrue(output.contains("ticket: station S3 faulty [R3] downtime cost/h 900.0"));
    }

    @Test
    void visitorsReportTheHandCalculatedNumbers() throws Exception {
        String output = run("5", "F1", "0");

        assertTrue(output.contains("energy kW:          97.0"));
        assertTrue(output.contains("downtime cost/h:    3250.0"));
    }

    @Test
    void oneShotPrinterPrintsTheFirstAlertThenLeaves() throws Exception {
        String output = run("7", "3", "S1", "R1", "9", "3", "S2", "P1", "9", "0");

        assertEquals(1, output.split("\\[printer\\]", -1).length - 1);
    }
}
