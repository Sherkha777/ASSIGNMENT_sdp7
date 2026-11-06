package plant.model;

import plant.visitor.Visitor;

import java.util.List;

public record Line(String id, String name, List<Station> children) implements Container {

    public Line {
        Require.text(id, "id");
        Require.text(name, "name");
        children = List.copyOf(children);
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitLine(this);
    }
}
