package org.cube.converter.model.element;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.cube.converter.util.element.Position3V;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Getter
@Setter
public class Parent {
    private String parent = "";

    private final String name;
    private final Position3V pivot;
    private final Position3V rotation;

    private final Map<Integer, Cube> cubes = new HashMap<>();

    @Override
    public Parent clone() {
        final Parent parent = new Parent(name, pivot.clone(), rotation.clone());
        for (final Map.Entry<Integer, Cube> entry : this.cubes.entrySet()) {
            parent.cubes.put(entry.getKey(), entry.getValue().clone());
        }
        parent.setParent(this.parent);

        return parent;
    }
}