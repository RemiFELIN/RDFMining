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
 * TreeNode.java
 *
 * Created on 16 October 2006, 16:45
 *
 */

package com.i3s.app.rdfminer.evolutionary.geva.Util.Structures;

import com.i3s.app.rdfminer.evolutionary.geva.Mapper.Rule;

import java.util.ArrayList;

/**
 * Node for use in the nimble tree structure. Has a parent and data. Is an array list
 *
 * @author EHemberg
 * @author Eoin Murphy
 */
public class TreeNode<E> extends ArrayList<TreeNode<E>> {

    private TreeNode<E> parent;
    private E data;
    private int depth;
    private int id;
    private int info;

    /**
     * Creates a new instance of TreeNode
     */
    public TreeNode() {
        super();
        info = 0;
    }

    /**
     * Create node with parent and data
     *
     * @param parent node parent
     * @param data   node data
     */
    public TreeNode(TreeNode<E> parent, E data) {
        super();
        this.parent = parent;
        this.data = data;
        info = 0;
        id = 0;
    }

    /**
     * Copy constructor
     *
     * @param copy node to copy
     */
    public TreeNode(TreeNode<E> copy) {
        this(copy, copy.getParent());
    }

    public TreeNode(TreeNode<E> copy, TreeNode<E> parent) {
        super();
        this.parent = parent;
        this.data = copy.data;
        this.info = copy.info;
        id = copy.id;
        for (TreeNode<E> aCopy : copy) {
            this.add(new TreeNode<E>(aCopy, this));
        }
    }

    /**
     * Adds a child node to this node.
     * Sets the childs parent and depth
     *
     * @param child the child node to add
     */
    @Override
    public boolean add(TreeNode<E> child) {
        child.setParent(this);
        child.setDepth(getDepth() + 1);
        return super.add(child);
    }

    /**
     * Get parent node
     *
     * @return parent node
     */
    public TreeNode<E> getParent() {
        return this.parent;
    }

    /**
     * Set parent node
     *
     * @param tn parent node
     */
    public void setParent(TreeNode<E> tn) {
        parent = tn;
        setDepth(parent.getDepth() + 1);
    }

    /**
     * Get data in node
     *
     * @return data
     */
    public E getData() {
        return data;
    }

    /**
     * Set data in node
     *
     * @param data node data
     */
    public void setData(E data) {
        this.data = data;
    }

    public int getInfo() {
        return info;
    }

    public void setInfo(int info) {
        this.info = info;
    }

    public int getID() {
        return id;
    }

    public void setID(int id) {
        this.id = id;
    }


    /**
     * Set depth of this node and it's children
     *
     * @param i depth
     */
    public void setDepth(int i) {
        depth = i;
        for (int n = 0; n < size(); n++) {
            TreeNode<E> node = get(n);
            node.setDepth(i + 1);
        }
    }

    public int getMaxDepth() {
        int max = depth;
        for (TreeNode<E> node : this)
            max = Math.max(max, node.getMaxDepth());
        return max;
    }

    /**
     * Get depth of this node in the tree
     *
     * @return node's depth
     */
    public int getDepth() {
        return this.depth;
    }

    /**
     * Get the last node
     *
     * @return the last node
     */
    public TreeNode<E> getEnd() {
        return this.get(this.size() - 1);
    }

    /**
     * Collapse the node to a string
     *
     * @return string representation
     */
    public String collapse() {
        StringBuffer s = new StringBuffer();
        s.append(this.getData());
        if (this.getParent() != null) {
            s.append(this.getParent().collapse());
        }
        for (TreeNode<E> e : this) {
            s.append(e.getData().toString());
            s.append(e.getParent().collapse());
        }
        return s.toString();
    }


    /**
     * Build the string using a textural tree-view
     *
     * @return String
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        toString(builder, "");
        return builder.toString();
    }

    /**
     * Recursively build the string using a textual tree-view. Each recursion
     * passes an <var>indent</var> to append to the start of each line. If
     * indent contains the value "\1", it is output as "|_" and then replaced
     * with the value "| ", if indent contains the value "\2", it is output as
     * "|_" and then replaced with "  ". Therefore, each parent that has
     * siblings following it should add "\1" to the indent, and each parent
     * that has no following siblings add "\2", this way, the branch for
     * parents with following siblings (for all grand-children) will output
     * "| " indicating more to follow, where parents with no following siblings
     * would output "  " indicating no more to follow
     *
     * @param builder The string builder to append the string
     * @param indent  The string used to start each line
     * @return true if a new-line was added after the last entry, else false
     */
    private boolean toString(StringBuilder builder, String indent) {

        builder.append(indent.replaceAll("\1", "|_").replaceAll("\2", "|_"));
        builder.append(getData());
        //         if(codonIndex != -1)
        //         {   builder.append(" (");
        //             builder.append(codonIndex);
        //             /*builder.append(":");
        //             builder.append(codonValue);
        //             builder.append(":");
        //             builder.append(codonChose);
        //             */
        //             builder.append(")");
        //         }
        if (super.size() != 0) {
            builder.append("\n");
            for (int i = 0; i < super.size(); i++)
                if (!super.get(i).toString
                        (builder,
                                indent.replaceAll("\1", "| ").replaceAll("\2", "  ")
                                        + (i < super.size() - 1 ? "\1" : "\2")
                        ))
                    if (i < super.size() - 1)
                        builder.append("\n");
                    else
                        return false;
            return true;
        }
        return false;

    }

    public static void main(String[] args) {
        TreeNode<Rule> tn = new TreeNode<>();
        TreeNode<Rule> tn2 = new TreeNode<>();
        TreeNode<Rule> tn3;
        Rule c = new Rule();
        c.setMinimumDepth(1);
        tn.setParent(tn);
        tn.setData(c);
        tn.add(tn2);
        tn2.setParent(tn);
        tn2.setData(new Rule(c));
        tn2.getData().setMinimumDepth(2);
        tn3 = new TreeNode<Rule>(tn2);
        System.out.println(tn.getParent().getData().getMinimumDepth());
        System.out.println(tn2.getData().getMinimumDepth());
        System.out.println(tn3.getParent().getData().getMinimumDepth());
        tn2.getData().setMinimumDepth(4);
        tn3.setParent(tn2);
        System.out.println(tn.getParent().getData().getMinimumDepth());
        System.out.println(tn2.getData().getMinimumDepth());
        System.out.println(tn3.getParent().getData().getMinimumDepth());

    }

}
