/*
Grammatical Evolution in Java
Release: GEVA-v2.0.zip
Copyright (C) 2008 Michael O'Neill, Erik Hemberg, Anthony Brabazon, Conor Gilligan 
Contributors Patrick Middleburgh, Eliott Bartley, Jonathan Hugosson, Jeff Wrigh

Separate licences for asm, bsf, antlr, groovy, jscheme, commons-logging, jsci is included in the lib folder. 
Separate licence for rieps is included in src/com folder.

This licence refers to GEVA-v2.0.

This software is distributed under the terms of the GNU General Public License.


This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
/>.
*/

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.i3s.app.rdfminer.evolutionary.geva.Util.Structures;

import com.i3s.app.rdfminer.evolutionary.geva.Mapper.Symbol;

import java.util.HashSet;
import java.util.Hashtable;

/**
 * FIXME Make singelton
 * @author erikhemberg
 */
public class Operators<E> {

    private final Hashtable<String, Integer> operators = new Hashtable<>();
    private final HashSet<String> unaryOperators = new HashSet<>();

    public Operators() {
        operators.put("(", 10);
        operators.put("-", 20);
        operators.put("+", 20);
        operators.put("/", 30);
        operators.put("*", 30);
        operators.put("^", 50);//XOR
        operators.put("&", 40);//AND
        operators.put("|", 60);//OR
        operators.put("~", 61);//NOT
        operators.put("not",61);
        unaryOperators.add("~");
        unaryOperators.add("not");
    }

    public int priority(final String operator) {
        final int value;
        value = operators.getOrDefault(operator, -1);
        return value;
    }

    public boolean isUnary(final String operator) {
        final boolean value;
        if (unaryOperators.contains(operator)) {
            value = true;
        } else {
            value = false;
        }
        return value;
    }

    public boolean containsKey(final String s) {
        return operators.containsKey(s);
    }

    public int priority(final E operator) {
        final int value;
        if(operator instanceof Symbol) {
           value = this.priority(((Symbol)operator).getSymbolString());
        } else {
            if(operator instanceof String) {
                value = this.priority((String)operator);
                } else {
                throw new IllegalArgumentException("Bad type for:"+operator+" Must be String or Symbol. Is:"+operator.getClass());
            }
        }
        return value;
    }

    public boolean isUnary(final E operator) {
        final boolean value;
        if(operator instanceof Symbol) {
           value = this.isUnary(((Symbol)operator).getSymbolString());
        } else {
            if(operator instanceof String) {
                value = this.isUnary((String)operator);
            } else {
                throw new IllegalArgumentException("Bad type for:"+operator+" Must be String or Symbol. Is:"+operator.getClass());
            }
        }
        return value;
    }

    public boolean containsKey(final E operator) {
        final boolean value;
        if (operator instanceof Symbol) {
            value = this.containsKey(((Symbol) operator).getSymbolString());
        } else {
            if(operator instanceof String) {
                value = this.containsKey((String)operator);
            } else {
                throw new IllegalArgumentException("Bad type for:"+operator+" Must be String or Symbol. Is:"+operator.getClass());
            }
        }
        return value;
    }

}
