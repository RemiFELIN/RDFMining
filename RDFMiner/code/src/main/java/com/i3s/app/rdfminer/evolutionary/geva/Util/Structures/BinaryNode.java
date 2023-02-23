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

package com.i3s.app.rdfminer.evolutionary.geva.Util.Structures;

public class BinaryNode<T> {

    private BinaryNode<T> left;
    private BinaryNode<T> right;
    private T value;

    public BinaryNode(T value) {
	this.value = value;
    }

    public BinaryNode() {
    }

    public T getValue() {
	return this.value;
    }

    public void setValue(T value) {
	this.value = value;
    }

    public BinaryNode<T> getLeft() {
	return this.left;
    }

    public void setLeft(BinaryNode<T> left) {
	this.left = left;
    }

    public BinaryNode<T> getRight() {
	return this.right;
    }

    public void setRight(BinaryNode<T> right) {
	this.right = right;
    }

}