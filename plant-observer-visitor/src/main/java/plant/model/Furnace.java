package plant.model;

import plant.visitor.Visitor;

public record Furnace(String id, String name, double powerKw, double downtimeCostPerHour, double setpointC)
        implements Equipment {

    public Furnace {
        Require.text(id, "id");
        Require.text(name, "name");
        Require.nonNegative(powerKw, "powerKw");
        Require.nonNegative(downtimeCostPerHour, "downtimeCostPerHour");
        Require.nonNegative(setpointC, "setpointC");
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitFurnace(this);
    }
}
