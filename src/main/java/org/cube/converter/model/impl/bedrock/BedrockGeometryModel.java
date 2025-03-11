package org.cube.converter.model.impl.bedrock;

import lombok.Getter;

import com.google.gson.JsonObject;
import org.cube.converter.converter.FormatConverter;
import org.cube.converter.model.GeneralModel;
import org.cube.converter.model.impl.java.JavaItemModel;
import org.cube.converter.parser.bedrock.geometry.BedrockGeometryParser;
import org.cube.converter.util.GsonUtil;
import org.cube.converter.util.element.Position2V;

import java.util.List;

@Getter
public final class BedrockGeometryModel extends GeneralModel {
    private final String identifier;

    public BedrockGeometryModel(final String identifier, final Position2V textureSize) {
        super(textureSize);
        this.identifier = identifier;
    }

    @Override
    public JsonObject compile() {
        return null;
    }

    public JavaItemModel toJavaItemModel(String texture, boolean workaround) {
        return FormatConverter.geometryToItemModel(texture, this, workaround);
    }

    public List<JavaItemModel> toMultipleJavaItemModel(String texture) {
        return FormatConverter.geometryToMultipleModels(texture, this);
    }

    public static List<BedrockGeometryModel> fromJson(String json) {
        return fromJson(GsonUtil.getGson().fromJson(json.trim(), JsonObject.class));
    }

    public static List<BedrockGeometryModel> fromJson(JsonObject object) {
        return BedrockGeometryParser.parse(object);
    }
}