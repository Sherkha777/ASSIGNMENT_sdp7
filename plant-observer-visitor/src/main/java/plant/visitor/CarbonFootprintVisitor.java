package plant.visitor;

import plant.model.Equipment;

public class CarbonFootprintVisitor extends FoldVisitor<Double> {

    private final double kgCo2PerKwh;

    public CarbonFootprintVisitor(double kgCo2PerKwh) {
        if (kgCo2PerKwh < 0) {
            throw new IllegalArgumentException("kgCo2PerKwh must not be negative");
        }
        this.kgCo2PerKwh = kgCo2PerKwh;
    }

    @Override
    protected Double identity() {
        return 0.0;
    }

    @Override
    protected Double combine(Double left, Double right) {
        return left + right;
    }

    @Override
    protected Double leaf(Equipment equipment) {
        return equipment.powerKw() * kgCo2PerKwh;
    }
}
