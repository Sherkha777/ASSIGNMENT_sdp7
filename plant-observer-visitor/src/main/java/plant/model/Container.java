package plant.model;

import java.util.List;

public interface Container extends PlantElement {

    List<? extends PlantElement> children();
}
