package plant.model;

import plant.visitor.Visitor;

public record Press(String id, String name, double powerKw, double downtimeCostPerHour, double tonnage) implements Equipment {

    public Press {
        Require.text(id, "id");
        Require.text(name, "name");
        Require.nonNegative(powerKw, "powerKw");
        Require.nonNegative(downtimeCostPerHour, "downtimeCostPerHour");
        Require.nonNegative(tonnage, "tonnage");
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitPress(this);
    }
}
