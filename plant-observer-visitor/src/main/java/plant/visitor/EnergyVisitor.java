package plant.visitor;

import plant.model.Equipment;

public class EnergyVisitor extends FoldVisitor<Double> {

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
        return equipment.powerKw();
    }
}
