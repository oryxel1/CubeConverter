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
}