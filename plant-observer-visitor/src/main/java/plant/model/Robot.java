package plant.model;

import plant.visitor.Visitor;

public record Robot(String id, String name, double powerKw, double downtimeCostPerHour, double payloadKg) implements Equipment {

    public Robot {
        Require.text(id, "id");
        Require.text(name, "name");
        Require.nonNegative(powerKw, "powerKw");
        Require.nonNegative(downtimeCostPerHour, "downtimeCostPerHour");
        Require.nonNegative(payloadKg, "payloadKg");
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitRobot(this);
    }
}
