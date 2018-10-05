package com.njupt.utils;



// SelectionSetListener.java


/**
 * used by the SelectionSet class to notify listeners of
 * set membership changes.
 *
 * @author Melinda Green
 */
public interface SelectionSetListener {
    public void selectionSetChanged(SelectionSet set, Object source);
}

