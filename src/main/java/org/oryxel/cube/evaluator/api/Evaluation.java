package org.oryxel.cube.evaluator.api;

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
public class Evaluation {



    /*
            IDENTIFIER: value from metadata, custom, ... like query.variant, query.property('hive:bed_sheet')
            NUMBER: just number
            OPERATION: add, subtract, multiply, divide, compare, ...
            ARRAY: just an array, like array.textures, array.geo, ....
     */
    public enum IdentifierType {
        IDENTIFIER, NUMBER, OPERATION, ARRAY;
    }

    /*
            COMPARE : example -> value ? 1 : 0;
            COLON: ":".. just that
            CURVED_BRACKETS = ( )
            BRACKETS = [ ]
     */
    public enum OperationType {
        SUBTRACT, ADD, REMOVE, MULTIPLY, COMPARE, COLON, CURVED_BRACKETS, BRACKETS;
    }

}
