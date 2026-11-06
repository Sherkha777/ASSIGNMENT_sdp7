package plant.model;

import plant.visitor.Visitor;

public record Sensor(String id, String name, double powerKw, double downtimeCostPerHour, String measures)
        implements Equipment {

    public Sensor {
        Require.text(id, "id");
        Require.text(name, "name");
        Require.nonNegative(powerKw, "powerKw");
        Require.nonNegative(downtimeCostPerHour, "downtimeCostPerHour");
        Require.text(measures, "measures");
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitSensor(this);
    }
}
