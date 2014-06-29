/*
 * Copyright 2012 Anchialas.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kenai.redminenb.util;

import java.util.Arrays;

/**
 * Collected methods which allow easy implementation of {@link Object#equals}.
 * <p>
 * Example use case in a class called Car:
 * <pre>
 * public boolean equals(Object obj){
 *    if (this == obj) return true;
 *    if (!(obj instanceof Car)) return false;
 *    Car other = (Car)obj;
 *    return Is.equals(this.name, other.name) &&
 *           Is.equals(this.numDoors, other.numDoors) &&
 *           Is.equals(this.gasMileage, other.gasMileage) &&
 *           Is.equals(this.color, other.color) &&
 *           Arrays.equals(this.maintenanceChecks, other.maintenanceChecks); // array!
 * }
 * </pre>
 *
 * <em>Arrays are not handled by this class</em>:
 * The {@link Arrays#equals} methods should be used for array fields!
 * <p>
 * This is a noninstantiable utility class.
 * 
 * @author Anchialas <anchialas@gmail.com>
 */
public final class Is {

    private Is() {
        // default constructor suppressed for non-instantiability
    }

    /**
     * Compares the two specified <code>boolean</code> values.
     *
     * @param   a        the first <code>boolean</code> to compare
     * @param   b        the second <code>boolean</code> to compare
     * @return  <code>true</code> if both booleans are equal;
     *          <code>false</code> otherwise.
     */
    public static boolean equals(boolean a, boolean b) {
        return a == b;
    }

    /**
     * Compares the two specified <code>char</code> values.
     *
     * @param   a        the first <code>char</code> to compare
     * @param   b        the second <code>char</code> to compare
     * @return  <code>true</code> if both characters are equal;
     *          <code>false</code> otherwise.
     */
    public static boolean equals(char a, char b) {
        return a == b;
    }

    /**
     * Compares the two specified <code>long</code> values.
     * <p>
     * Note that byte, short, and int are handled also by this method, through implicit conversion.
     *
     * @param   a        the first <code>long</code> to compare
     * @param   b        the second <code>long</code> to compare
     * @return  <code>true</code> if <code>a</code> is numerically equal to <code>b</code>;
     *          <code>false</code> otherwise.
     */
    public static boolean equals(long a, long b) {
        return a == b;
    }

    /**
     * Compares the two specified <code>float</code> values. This call is equal to:
     * <pre>
     *    Float.compare(a,b) == 0;
     * </pre>
     *
     * @param   a        the first <code>float</code> to compare
     * @param   b        the second <code>float</code> to compare
     * @return  <code>true</code> if <code>a</code> is numerically equal to <code>b</code>;
     *          <code>false</code> otherwise.
     * @see Float#compare
     */
    public static boolean equals(float a, float b) {
        return Float.compare(a, b) == 0;
    }

    /**
     * Compares the two specified <code>double</code> values. This call is equal to:
     * <pre>
     *    Double.compare(a,b) == 0;
     * </pre>
     *
     * @param   a        the first <code>double</code> to compare
     * @param   b        the second <code>double</code> to compare
     * @return  <code>true</code> if <code>a</code> is numerically equal to <code>b</code>;
     *          <code>false</code> otherwise.
     * @see Double#compare
     */
    public static boolean equals(double a, double b) {
        return Double.compare(a, b) == 0;
    }

    /**
     * Compares the two Objects for equality (Objects can be possibly <code>null</code>).
     * <p>
     * Includes type-safe enumerations and collections, but does not include
     * arrays. See class comment.
     *
     * @param   a        the first <code>Object</code> to compare
     * @param   b        the second <code>Object</code> to compare
     * @return  true if the both Objects are equal, or both {@code null}
     * @see Object#equals
     * @see Arrays#equals
     */
    public static boolean equals(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
    }

}
