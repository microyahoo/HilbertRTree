package com.njupt.utils;

// Inlay.java

import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * An Inlay object is a container similar to javax.swing.Box in that it
 * lays out added components along a given axis. What it adds are visual
 * borders, alternating color backgrounds for nested Inlay components,
 * a selection model, and a tree traversal method.
 *
 * The selection model works as follows: Each mouse click event sent to an
 * Inlay component normally causes it to swap its background color with a
 * highlighting color. If the selected instance is transparent and has another 
 * Inlay instance as its parent, then given mouse events are dispatched 
 * directly to that parent. The purpose of dispatching mouse click events to 
 * the parent is to allow transparent Inlay instances to be used as convienient
 * containers to position visual components within a parent, and have them all 
 * behave as if they were simply part of that parent's display.
 *
 * Note: Users should not use any JPanel editing methods other than the add
 * and remove methods defined here, otherwise it may display incorrectly.
 *
 * @author Melinda Green
 */
public class Inlay extends JPanel {
    private static final int PAD_PIXELS = 10;
    private static final Color HIGHLIGHT_COLOR = Color.yellow.darker();
    private static final Color backgroundColors[] = {
        Color.white,
        Color.gray,
    };
    private int layoutDirection;
    private boolean isHighlighted = false;
    private boolean highlightable = true;
    private boolean contrastWithParent = true;
    private int normalBackground = 0;

    /** the list of highlight listeners */
    private HashSet highlightListeners = new HashSet();

    /** returns highlighted state. */
    public boolean isHighlighted() { return isHighlighted; }
    
    public void setHighlightable(boolean highlightable) { this.highlightable = highlightable; }

    /** returns layout direction. one of the javax.swing.BoxLayout constants */
    public int getLayoutDirection() { return layoutDirection; }

    public String toString() { return "Inlay"; }

    /**
     * Constructs an Inlay object which lays out its children in the
     * given direction. Note: it is an error to attempt to change the
     * layout manager on Inlay objects.
     * 
     * @param direction is one of the BoxLayout direction constants,
     * either BoxLayout.X_AXIS or BoxLayout.Y_AXIS.
     * @param constrastParent specifies whether the instance should attempt to
     * match the background color of any parent Inlay, or to contrast with it.
     * @param borderWidth  specifies the width  of the internal border padding in pixels.
     * @param borderHeight specifies the height of the internal border padding in pixels.
     */
    public Inlay(int direction, boolean contrastParent, int borderWidth, int borderHeight) {
        layoutDirection = direction;
        contrastWithParent = contrastParent;
        super.setLayout(new BoxLayout(this, layoutDirection));
        setBorder(new EmptyBorder(borderHeight, borderWidth, borderHeight, borderWidth));
        setBackground(backgroundColors[normalBackground]);
        setAlignmentX(Component.CENTER_ALIGNMENT);
        setAlignmentY(Component.CENTER_ALIGNMENT);
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                if( ! highlightable)
                    return;
                Container parent = getParent();
                // always just toggle when not transparent
                // or when transparent but no containing Inlay parent to dispatch to.
                if (isOpaque() || (! (parent instanceof Inlay))) {
                    toggleHighlighted();
                    return;
                }
                // this message is not for us. translate into parent's
                // coordinate system and dispatch the event to it.
                // not sure if "source" field needs to change too.
                Point loc_in_parent = getLocation();
                me.translatePoint(loc_in_parent.x, loc_in_parent.y);
                parent.dispatchEvent(me);
            }
        });
    }

    /**
     * Same as the four argument constructor but assumes the default
     * border width.
     */
    public Inlay(int direction, boolean contrastParent) {
        this(direction, contrastParent, PAD_PIXELS, PAD_PIXELS);
    }
    
    /**
     * Same as the two argument constructor but assumes contrasting background.
     */
    public Inlay(int direction) {
        this(direction, true);
    }  

    /**
     * just calls super.paint(g) and when highlighted, 
     * draws a one pixel wide border in the normal background color
     * so this instance can be distinguished from an also highlighted
     * parent.
     */
    public void paint(Graphics g) {
        super.paint(g);
        if( ! isHighlighted)
            return;
        g.setColor(backgroundColors[normalBackground]);
        g.drawRect(0, 0, getSize().width-1, getSize().height-1);
    }
    
    /**
     * Called from parent Inlay containers to inform children of a change
     * in the parent's background color. this is needed for children who
     * must maintain a contrasting color with that of their parent.
     */
    private void parentBackgroundColorIs(int parentBackground) {
        // first, save our new color
        normalBackground = contrastWithParent ? oppositeColor(parentBackground) : parentBackground;
        if( ! isHighlighted) // tell swing
            setBackground(backgroundColors[normalBackground]);
        // finaly, tell the kids
        Component kids[] = getComponents();
        for(int i=0; i<kids.length; i++) {
            if(kids[i] instanceof Inlay)
                ((Inlay)kids[i]).parentBackgroundColorIs(normalBackground);
        }
    }

    private static int oppositeColor(int color) {
        return color == 0 ? 1 : 0;
    }


    //////////////////////
    // EDITING CONTROLS //
    ////////////////////// 

    /**
     * Overrides the protected method called by all add methods.
     * NOTE: For each component added after the first one, this
     * implementation adds two objects for each one given.
     * This could screw up code which counts, changes background color,
     * or removes elements it didn't add, etc.
     * Perhaps this should be a subclass of Container rather than JPanel
     * which would deligate to an internal JPanel? Tricky to do solidly
     * either way.
     */
    protected void addImpl(Component comp, Object constraints, int index) {
        if(comp instanceof JComponent)
            ((JComponent)comp).setAlignmentX(Component.CENTER_ALIGNMENT);
        if(getComponentCount() > 0)
            super.addImpl(space(), null, index); // adds spacer between previous
        super.addImpl(comp, constraints, index); // adds given component
        if(comp instanceof Inlay)  { // set child's color to be opposite of ours
            Inlay inlay = (Inlay)comp;
            inlay.parentBackgroundColorIs(normalBackground);
            inlay.addHighlightListener(nanny); // nanny reports child highlight events
            inlay.fireHighlightChanged(this); // workaround for swing bug?
        }
        invalidate();
    }

    /**
     * Don't know why java.awt.Container doesn't include this useful method,
     * so its implemented here.
     */
    public int indexOf(Component comp) {
        Component kids[] = getComponents();
        for(int i=0; i<kids.length; i++) {
            if(kids[i] == comp)
                return i;
        }
        return -1;
    }

    /**
     * returns the number of Inlay children.
     */
    public int getKidCount() {
        Component kids[] = getComponents();
        int kidCount = 0;
        for(int i=0; i<kids.length; i++) {
            if(kids[i] instanceof Inlay)
                kidCount++;
        }
        return kidCount;      
    }

    /**
     * returns an array containing all the Inlay children.
     */
    public Inlay[] getKids() {
        Component kids[] = (Component[])getComponents().clone();
        Inlay inlays[] = new Inlay[getKidCount()];
        int nkids = 0;
        for(int i=0; i<kids.length; i++)
            if(kids[i] instanceof Inlay)
                inlays[nkids++] = (Inlay)kids[i];
        return inlays;
    }

    /**
     * overrides the remove methods to account for spacers added.
     */
    public void remove(Component comp) {
        remove(indexOf(comp));
    }
    /**
     * removes the component at the given index 
     * as well as any associated spacer.
     */
    public void remove(int index) {
        super.remove(index);
        if(index > 0)
            super.remove(index-1); // remove preceeding spacer
        else // given index == 0
            if(getComponentCount() > 1) // there are following components
                super.remove(0); // remove the spacer that *was* following (but now at 0)
    }



    ///////////////////////////
    // HIGHLIGHTING CONTROLS //
    /////////////////////////// 

    /**
     * Listeners notified on highlight changes.
     */
    public interface HighlightListener {
        /**
         * called when the highlighting state is changed on the Inlay
         * to which a listener is added. The method is also called if 
         * the highlightin state of any of its descendants changes.
         * In all cases, the given Inlay is the one who's state has changed.
         * Just be aware that it may not be the same Inlay to which the
         * listener was added.
         */
        public void highlightChanged(Inlay whose);
        /**
         * returning <code>true</code> causes attached Inlay instances to
         * report highlighting events of all nested Inlay instances as well
         * as those of the attached Inlay. returning <code>false</code>
         * causes notification of highlighting events of the attached Inlay
         * instances only.
         */
        public boolean doRecurse();
    }

    /**
     * Swaps the background color between normal color and highlight color.
     * @param doNotify specifies whether attached HighlightListeners should
     * be notified of the change.
     */
    protected void toggleHighlighted(boolean doNotify) {
        setHighlighted(!isHighlighted, doNotify);
    }

    /**
     * Same as the two argument version but with listeners always notified.
     */
    public void toggleHighlighted() {
        toggleHighlighted(true);
    }

    /**
     * Sets the highlighted state.
     * @param doNotify specifies whether attached HighlightListeners should
     * be notified if the highlighted state changes.
     */
    public void setHighlighted(boolean on, boolean doNotify) {
        if (isHighlighted == on) {
            //System.out.println("highlighting already " + (on ? "on" : "off"));
            return;
        }
        isHighlighted = on;
        setBackground(isHighlighted ? HIGHLIGHT_COLOR : backgroundColors[normalBackground]);
        if(doNotify)
            fireHighlightChanged(this);
    }

    /**
     * Same as the two argument version but with listeners always notified.
     */
    public void setHighlighted(boolean on) {
        setHighlighted(on, true);
    }

    /**
     * adds a listener which will be called any time the highlighting state
     * is changed on the called instance <i>or any of its descendants</i>.
     */
    public void addHighlightListener(HighlightListener hl) {
        highlightListeners.add(hl);
    }

    /**
     * partial implementation of HighlightListener which always recurses.
     */
    public static abstract class HighlightAdapter implements HighlightListener {
        public boolean doRecurse() { return true; }
    }

    /**
     * removes a previously added highlight listener.
     */
    public void removeHighlightListener(HighlightListener hl) {
        highlightListeners.remove(hl); // remove user's listener
        Inlay kids[] = getKids();
        for(int i=0; i<kids.length; i++) // make nanny stop watching the kids
            kids[i].highlightListeners.remove(nanny); // done forcefully. oh well.
    }

    private void fireHighlightChanged(Inlay whose) {
        for(Iterator it = highlightListeners.iterator(); it.hasNext(); ) {
            HighlightListener listener = (HighlightListener)it.next();
            if(whose == this || listener.doRecurse())
                listener.highlightChanged(whose);
        }
    }

    /**
     * returns an array containing all the highlighted Inlay components
     * from the called instance, downward, optionally including all
     * highlighted descendants.
     * @param recurse specifies whether to include descendants other than
     * just the direct children.
     */
    public Inlay[] getHighlighted(boolean recurse) {
        final ArrayList highlighted = new ArrayList();
        walkTree(new TreeWalker() {
            public void visit(Inlay node, int level) {
                if(node.isHighlighted())
                    highlighted.add(node);
            }
        }, recurse);
        return (Inlay[])highlighted.toArray(new Inlay[0]);
    }

    /**
     * used internally to listen to child Inlays and report their changes
     * to listeners of their parent (i.e. listeners to "this").
     */
    private HighlightListener nanny = new HighlightAdapter() {
        public void highlightChanged(Inlay whose) {
            fireHighlightChanged(whose);
        }
    };


    /////////////////////
    // UTILITY METHODS //
    ///////////////////// 

    /**
     * returns a component which attempts to spread to fill available space
     * along the horizontal axis, and takes up a finite space (thickness)
     * along the vertical axis. Essentially a combinition of a strut
     * in one direction and glue in the other. 
     * Note: do not use in scroll panels or they will expand indefinitely.
     * @param thickness gives the size of the component's "strutness"
     * along the vertical axis.
     */
    public static Component createHorizontalSpreaderBar(int thickness) {
        return new Box.Filler(
                new Dimension(0, thickness), // minimum size
                new Dimension(0, thickness), // preferred size
                new Dimension(Short.MAX_VALUE, thickness) { // maximum size
                    public String toString() { return "horizontal spreader"; }
                });
    }

    /**
     * returns a component which attempts to spread to fill available space
     * along the vertical axis, and takes up a finite space (thickness)
     * along the horizontal axis. Essentially a combinition of a strut
     * in one direction and glue in the other.
     * Note: do not use in scroll panels or they will expand indefinitely.
     * @param thickness gives the size of the component's "strutness"
     * along the horizontal axis.
     */
    public static Component createVerticalSpreaderBar(int thickness) {
        return new Box.Filler(
                new Dimension(thickness, 0), // minimum size
                new Dimension(thickness, 0), // preferred size
                new Dimension(thickness, Short.MAX_VALUE) { // maximum size
                    public String toString() { return "vertical spreader"; }
                });
    }

    /**
     * like HorizontalSpreaderBar but which has an unlimited preferred size
     * along the horizontal axis. In this sense it's very similar to
     * glue but stronger since glue only has an unlimited maximum size
     * whereas this one has an unlimited <i>preferred</i> size.
     * When competing with normal glue, super glue wins. That makes it possible
     * to use super glue to "take up the slack" in areas where normal glue
     * has caused components to expand too far into unoccupied space.
     * Note: do not use in scroll panels or they will expand indefinitely.
     */
    public static Component createHorizontalSuperGlue() {
        return new Box.Filler(
                new Dimension(0, 0), // minimum size
                new Dimension(Short.MAX_VALUE, 0), // preferred size
                new Dimension(Short.MAX_VALUE, 0) { // maximum size
                    public String toString() { return "horizontal superglue"; }
                });
    }

    /**
     * like VerticalSpreaderBar but which has an unlimited preferred size
     * along the vertical axis. In this sense it's very similar to
     * glue but stronger since glue only has an unlimited maximum size
     * whereas this one has an unlimited <i>preferred</i> size.
     * When competing with normal glue, super glue wins. That makes it possible
     * to use super glue to "take up the slack" in areas where normal glue
     * has caused components to expand too far into unoccupied space.
     * Note: do not use in scroll panels or they will expand indefinitely.
     */
    public static Component createVerticalSuperGlue() {
        return new Box.Filler(
                new Dimension(0, 0), // minimum size
                new Dimension(0, Short.MAX_VALUE), // preferred size
                new Dimension(0, Short.MAX_VALUE) { // maximum size
                    public String toString() { return "vertical superglue"; }
                });
    }

    /**
     * returns a component which takes up PAD_PIXELS in the layout direction.
     */
    private Component space() {
        // Implementation uses a spreader bar rather than a simple strut or
        // rigid area in order to expand the containers to fill available space
        // in the non layout direction. If that is later decided to not be 
        // desired, then simply use the commented out use of rigid area below.
        return layoutDirection == BoxLayout.X_AXIS ?
            Inlay.createVerticalSpreaderBar(PAD_PIXELS) :
            Inlay.createHorizontalSpreaderBar(PAD_PIXELS);
        //return Box.createRigidArea(new Dimension(PAD_PIXELS, PAD_PIXELS));
    }

    /**
     * static utility tells whether all given Inlays have the same Inlay parent.
     */
     public static boolean haveCommonInlayParent(Inlay inlays[]) {
        if(inlays.length < 2)
            return true;
        if( ! (inlays[0].getParent() instanceof Inlay))
            return false;
        Inlay parent0 = (Inlay)inlays[0].getParent();
        for(int i=0; i<inlays.length; i++) {
            if(inlays[i].getParent() != parent0)
                return false; // not all highlighted inlays have a common parent
        }
        return true;
    }


    ////////////////////
    // TREE TRAVERSAL //
    ////////////////////

    /**
     * for use by walkTree method.
     * objects implementing this interface and passed to walkTree
     * will have their visit method called for each Inlay in a
     * (possibly nested) Inlay tree. every Inlay node encountered
     * will be visited in depth-first order.
     */
    public interface TreeWalker {
        /**
         * callback method which reports an Inlay instance in a tree.
         * @param node is the Inlay being reported.
         * @param level is the depth of the node being reported
         * where zero denotes the root (i.e. the original Inlay instance
         * the walkTree method was invoked on.)
         */
        public void visit(Inlay node, int level);
    }

    /**
     * walks this Inlay object and all its Inlay children.
     * the walker's visit method is called for each Inlay node
     * in the tree rooted at the instance this method is called on.
     * nodes are visited in depth-first order. The walker object
     * may safely edit the tree during traversal, but callers should
     * be aware that changes to the node containing a node that is
     * currently being walked will not be noticed by the current 
     * walkTree invocation, though changes to its descendants will.
     */
    public void walkTree(TreeWalker walker, boolean recurse) {
        walkTree(walker, recurse, 0);
    }
    /**
     * convienience version which always recurses.
     */
    public void walkTree(TreeWalker walker) {
        walkTree(walker, true, 0);
    }
    /**
     * the private recursive version called from the public version
     * to initiate tree traversal.
     * the current Inlay plus each of its immediate children are always 
     * visited, but if recurse==true, all children's descenedants are
     * also visited.
     */
    private void walkTree(TreeWalker walker, boolean recurse, int level) {
        walker.visit(this, level);
        Inlay kids[] = getKids();
        for(int i=0; i<kids.length; i++)
            kids[i].walkTree(walker, recurse, level+1);
    }    


    /**
     * A simple example of using Inlays.
     */
    private static void main(String args[]) {
        Inlay mainpanel = new Inlay(BoxLayout.Y_AXIS);
        Inlay subpanel = new Inlay(BoxLayout.Y_AXIS);
        subpanel.add(new JComboBox());
        subpanel.add(new JComboBox());
        mainpanel.add(subpanel);
        JPanel tmp = new JPanel();
        tmp.setOpaque(false);
        tmp.add(new JComboBox(new String[] { "AND", "OR", }));
        tmp.setMaximumSize(new Dimension(8888, 30));
        mainpanel.add(tmp);
        JLabel text = new JLabel("Click Me To Toggle Selection");
        Inlay labelinlay = new Inlay(BoxLayout.Y_AXIS);
        text.setForeground(Color.black);
        labelinlay.add(text);
        mainpanel.add(labelinlay);
        mainpanel.addHighlightListener(new Inlay.HighlightAdapter() {
            public void highlightChanged(Inlay whose) {
                System.out.println("Inlay with " + whose.getKidCount() + " sub Inlays");
            }
        });
        JFrame frame = new JFrame("Inlay Example");
        frame.getContentPane().add(mainpanel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

} // end class Inlay

