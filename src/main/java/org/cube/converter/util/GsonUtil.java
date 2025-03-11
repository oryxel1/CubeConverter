package org.cube.converter.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
public final class GsonUtil {
    private GsonUtil() {}
    @Getter
    private final static Gson gson = new GsonBuilder().create();
}
