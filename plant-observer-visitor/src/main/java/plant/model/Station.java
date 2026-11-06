package plant.model;

import plant.visitor.Visitor;

import java.util.List;

public record Station(String id, String name, List<Equipment> children) implements Container {

    public Station {
        Require.text(id, "id");
        Require.text(name, "name");
        children = List.copyOf(children);
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitStation(this);
    }
}
