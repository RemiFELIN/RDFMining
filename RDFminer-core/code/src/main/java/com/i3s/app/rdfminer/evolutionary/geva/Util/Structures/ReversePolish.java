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

import com.i3s.app.rdfminer.evolutionary.geva.Individuals.Phenotype;
import com.i3s.app.rdfminer.evolutionary.geva.Mapper.Symbol;
import com.i3s.app.rdfminer.evolutionary.geva.Util.Enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class ReversePolish {

    private BinaryNode<Symbol> root;
    private ArrayList<Symbol> postfix = new ArrayList<>();
    private ArrayList<Symbol> tokens;
    private int global_cnt;
    private Operators operators;

    public ReversePolish() {
        this.operators = new Operators<Symbol>();
    }

    public ReversePolish(String s) {
        this.setTokens(s);
        this.operators = new Operators<Symbol>();
    }

    public void clear() {
        this.root = null;
        this.postfix.clear();
        if(tokens != null) {
            this.tokens.clear();
        }
        this.global_cnt = 0;
    }

    public void setTokens(String s) {
        tokens = new ArrayList<Symbol>();
        List<String> als = Arrays.asList(s.split("\\s+"));
        for(int i = 0; i<als.size(); i++) {
            tokens.add(new Symbol(als.get(i), Enums.SymbolType.TSymbol));
        }
    }

    public void setTokens(ArrayList<Symbol> tokens) {
        this.tokens = tokens;
    }

    public ArrayList<Symbol> getTokens() {
        return tokens;
    }

    public BinaryNode<Symbol> getRoot() {
        return root;
    }

    public ArrayList<Symbol> getPostfix() {
        return postfix;
    }

    public Phenotype toPrefixPhenotype(Phenotype phenotype) {
        this.setTokens((ArrayList<Symbol>)phenotype);
        this.toReversePolish();
        this.treeFromPostfix();
        final Phenotype prefixPhenotype = new Phenotype();
        this.treeToPrefixPhenotype(prefixPhenotype, this.root);
        return prefixPhenotype;
    }

    public void treeFromPostfix() {
        this.global_cnt = postfix.size() - 1;
        this.root = this.buildTreeFromPostfix();
    }

    public BinaryNode<Symbol> buildTreeFromPostfix() {
        BinaryNode<Symbol> new_node = null;
        if (this.global_cnt > -1) {
            new_node = new BinaryNode<Symbol>();
            final Symbol s = this.postfix.get(this.global_cnt);
            this.global_cnt--;
            if (!operators.containsKey(s)) {
                new_node.setValue(s);
                return new_node;
            }
            new_node.setLeft(buildTreeFromPostfix());
            if (!operators.isUnary(s)) {
                new_node.setRight(buildTreeFromPostfix());
            }
            new_node.setValue(s);
        }
        return new_node;
    }

    /**
     * The method toReversePolish converts the infix expression into a
     * reverse Polish string.
     * @return Return the reverse Polish string.
     */
    public String toReversePolish() {
        Symbol nextToken;
        Symbol operator;
        Stack<Symbol> siding = new Stack<>();

        for (int index = 0; index < tokens.size(); index++) {
            nextToken = tokens.get(index);
            if (nextToken.equals(")")) {
                operator = siding.pop();
                while (!operator.equals("(")) {
                    postfix.add(operator);
                    operator = siding.pop();
                }
            } else if (nextToken.equals("(")) {
                siding.push(nextToken);
            } else if (operators.containsKey(nextToken) && !nextToken.equals("(")) {
                while (!siding.empty() && operators.priority(nextToken) <= operators.priority(siding.peek())) {
                    operator = siding.pop();
                    postfix.add(operator);
                }
                siding.push(nextToken);
            } else {
                postfix.add(nextToken);
            }
        }
        while (!siding.empty()) {//Stack not empty
            postfix.add(siding.pop());
        }
        return postfix.toString();
    }

    /**
     * Traverses a binary tree depth first
     * @param node node to travers
     * @return string value of node
     */
    public String toPrefixString(final BinaryNode<Symbol> node) {
        final StringBuffer sb = new StringBuffer();
        if (node != null) {
            sb.append(node.getValue());
            sb.append(" ");
            sb.append(toPrefixString(node.getLeft()));
            if (!operators.isUnary(node.getValue())) {
                sb.append(toPrefixString(node.getRight()));
            }
        }
        return sb.toString();
    }

    private void treeToPrefixPhenotype(final Phenotype phenotype, final BinaryNode<Symbol> node) {
        if (node != null) {
            phenotype.add((Symbol)node.getValue());
            treeToPrefixPhenotype(phenotype, node.getLeft());
            if (!operators.isUnary(node.getValue())) {
                treeToPrefixPhenotype(phenotype, node.getRight());
            }
        }
    }
}
