package plant.visitor;

import plant.model.Container;
import plant.model.Equipment;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class InventoryVisitor extends WalkVisitor<Map<String, Integer>> {

    private final Map<String, Integer> counts = new TreeMap<>();

    @Override
    protected void onContainer(Container container) {
        count(container.getClass());
    }

    @Override
    protected void onLeaf(Equipment equipment) {
        count(equipment.getClass());
    }

    @Override
    protected Map<String, Integer> result() {
        return Collections.unmodifiableMap(new TreeMap<>(counts));
    }

    private void count(Class<?> kind) {
        counts.merge(kind.getSimpleName(), 1, Integer::sum);
    }
}
