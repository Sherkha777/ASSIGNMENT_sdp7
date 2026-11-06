package plant.model;

import plant.visitor.Visitor;

public interface PlantElement {

    String id();

    String name();

    <R> R accept(Visitor<R> visitor);
}
