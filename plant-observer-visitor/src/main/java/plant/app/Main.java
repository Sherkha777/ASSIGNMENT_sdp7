package plant.app;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Main {

    public static void main(String[] args) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        try (PlantSystem system = new PlantSystem(SamplePlant.build(), PlantConfig.defaults())) {
            new DemoApp(system, in, System.out).run();
        }
    }
}
