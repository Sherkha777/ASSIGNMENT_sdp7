package plant.app;

public record PlantConfig(int producerThreads, double vibrationLimitMmS, double temperatureLimitC,
                          double idealUnitsPerMinute) {

    public PlantConfig {
        if (producerThreads < 1) {
            throw new IllegalArgumentException("producerThreads must be at least 1");
        }
        if (vibrationLimitMmS <= 0 || temperatureLimitC <= 0 || idealUnitsPerMinute <= 0) {
            throw new IllegalArgumentException("limits must be positive");
        }
    }

    public static PlantConfig defaults() {
        return new PlantConfig(4, 7.0, 120.0, 2.5);
    }
}
