package com.njupt.utils;

/**
 * InfiniteBinaryTree
 *
 * <p> A little example program to show the power of the TreeModel interface.
 * Running it displays a binary tree with numbers on each node.
 * Every positive number can be found somewhere in the tree.
 * See if you can find the one labeled 1000!
 *
 * @author Melinda Green - Superliminal Software
 */
import javax.swing.*;
import javax.swing.tree.*;

public class InfiniteBinaryTree implements TreeModel {

    // these 5 lines contain all the logic
    public Object getRoot()  { return 1; }
    public boolean isLeaf(Object node) { return false; }
    public int getChildCount(Object parent)  { return 2; }
    public Object getChild(Object parent, int index) { return 2 * (Integer)parent + index; }
    public int getIndexOfChild(Object parent, Object child) { return (Integer)child % 2; }

    // stubbed out methods for example
    public void addTreeModelListener(javax.swing.event.TreeModelListener l) {}
    public void removeTreeModelListener(javax.swing.event.TreeModelListener l) {}
    public void valueForPathChanged(TreePath path, Object newValue) {}


    public static void main(String args[])  {
        JScrollPane scroller = new JScrollPane(new JTree(new InfiniteBinaryTree()));
        JFrame frame = new JFrame("Infinite Binary RTree");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(scroller);
        frame.setSize(new java.awt.Dimension(400, 400));
        frame.setVisible(true);
    }
}

