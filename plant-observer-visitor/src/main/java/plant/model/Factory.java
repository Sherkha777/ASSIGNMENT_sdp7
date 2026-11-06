package plant.model;

import plant.visitor.Visitor;

import java.util.List;

public record Factory(String id, String name, List<Line> children) implements Container {

    public Factory {
        Require.text(id, "id");
        Require.text(name, "name");
        children = List.copyOf(children);
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitFactory(this);
    }
}
