package plant.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PlantIndex {

    private final Map<String, Station> stations;
    private final Map<String, String> lineOfStation;
    private final List<String> orderedStationIds;

    public PlantIndex(Factory factory) {
        Map<String, Station> stationsById = new LinkedHashMap<>();
        Map<String, String> linesByStation = new LinkedHashMap<>();
        for (Line line : factory.children()) {
            for (Station station : line.children()) {
                stationsById.put(station.id(), station);
                linesByStation.put(station.id(), line.id());
            }
        }
        this.stations = Map.copyOf(stationsById);
        this.lineOfStation = Map.copyOf(linesByStation);
        this.orderedStationIds = List.copyOf(stationsById.keySet());
    }

    public Station station(String stationId) {
        Station station = stations.get(stationId);
        if (station == null) {
            throw new IllegalArgumentException("unknown station " + stationId);
        }
        return station;
    }

    public String lineIdOf(String stationId) {
        String lineId = lineOfStation.get(stationId);
        if (lineId == null) {
            throw new IllegalArgumentException("unknown station " + stationId);
        }
        return lineId;
    }

    public List<String> stationIds() {
        return orderedStationIds;
    }
}
