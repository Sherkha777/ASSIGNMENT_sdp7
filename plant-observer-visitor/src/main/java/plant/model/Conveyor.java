package plant.model;

import plant.visitor.Visitor;

public record Conveyor(String id, String name, double powerKw, double downtimeCostPerHour, double lengthM) implements Equipment {

    public Conveyor {
        Require.text(id, "id");
        Require.text(name, "name");
        Require.nonNegative(powerKw, "powerKw");
        Require.nonNegative(downtimeCostPerHour, "downtimeCostPerHour");
        Require.nonNegative(lengthM, "lengthM");
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitConveyor(this);
    }
}
