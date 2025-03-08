package org.cube.converter.model;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.cube.converter.model.element.Parent;
import org.cube.converter.util.element.Position2V;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Getter
@Setter
public abstract class GeneralModel {
    protected final List<Parent> parents = new ArrayList<>();
    protected final Position2V textureSize;

    public abstract JsonObject compile();
}