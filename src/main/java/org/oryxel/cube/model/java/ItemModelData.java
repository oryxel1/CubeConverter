package org.oryxel.cube.model.java;

import org.oryxel.cube.model.java.other.Element;
import org.oryxel.cube.model.java.other.Group;

import java.util.ArrayList;
import java.util.List;

/*
 * This file is part of CubeConverter - https://github.com/Oryxel/CubeConverter
 * Copyright (C) 2023-2024 Oryxel and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class ItemModelData {

    private final String textures;
    private final int textureWidth, textureHeight;
    private double[] positionOffset = new double[3], offsetFromCenter = new double[3];
    private double[] rotation = new double[3];
    private double scale = 1;

    private final List<Group> groups = new ArrayList<>();
    private final List<Element> elements = new ArrayList<>();

    public ItemModelData(String textures, int textureWidth, int textureHeight) {
        this.textures = textures;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    public String textures() {
        return textures;
    }

    public int textureWidth() {
        return textureWidth;
    }

    public int textureHeight() {
        return textureHeight;
    }

    public double[] rotation() {
        return rotation;
    }

    public void rotation(double[] rotation) {
        this.rotation = rotation;
    }

    public double[] positionOffset() {
        return positionOffset;
    }

    public void positionOffset(double[] positionOffset) {
        this.positionOffset = positionOffset;
    }

    public void offsetFromCenter(double[] offsetFromCenter) {
        this.offsetFromCenter = offsetFromCenter;
    }

    public double[] offsetFromCenter() {
        return offsetFromCenter;
    }

    public double scale() {
        return scale;
    }

    public void scale(double scale) {
        this.scale = scale;
    }

    public List<Group> groups() {
        return groups;
    }

    public List<Element> elements() {
        return elements;
    }

}
