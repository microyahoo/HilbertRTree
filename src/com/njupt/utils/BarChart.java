package com.njupt.utils;

// BarChart.java

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


/**
 * Generic bar chart class with full selection set support.
 * 
 * @author Melinda Green
 */
public class BarChart extends JPanel {
    /**
     * implemented by data objects to be charted.
     */
    public interface BarValue {
        /** specifies the hight (data value) of this bar. */
        public float getBarValue();
        /** optional summary text suitable for tool-tip or other display.*/
        public String getInfoText();
    } 
    public final static int
        SORT_NONE       = 0,
        SORT_ASCENDING  = 1,
        SORT_DESCENDING = 2;
    public final static int
        LINEAR_SCALE = 0,
        LOG_SCALE    = 1;
    private static final int 
        CHART_LEFT_PAD   = 50,
        CHART_RIGHT_PAD  = 25,
        CHART_TOP_PAD    = 50,
        CHART_BOTTOM_PAD = 50,
        CHART_MIN_DIM = 80,
        MAX_TIC_LABELS = 10, // greatest number of axis tic labels
        TIC_LENGTH = 6, // length of axis tic marks
        MIN_BOX_WIDTH_FOR_SEPERATOR = 8; // looks crummy if too small
    private final static String NO_VALUES_MSG = "No Bar Values Set";
    private static final Color NORMAL_COLOR = Color.blue.darker();
    private static final Color HIGHLIGHT_COLOR = Color.yellow;
    private String xAxisLabel, yAxisLabel;
    private int xScaleType, yScaleType; // log or linear
    private SelectionSet selections;
    private BarValue barValues[] = null;
    private float highVal;
    private int anchorSelectionIndex = 0; // base index for range selections
    private final Rectangle tmpRect = new Rectangle(); // scratch space

    /**
     * constructs a BarChart panel.
     * @param xAxisLabel - String to draw on x axis - optional.
     * @param yAxisLabel - String to draw on y axis - optional.
     * @param logScale determines whether to draw x axis on log or linear scale.
     * @param sel is an optional SelectionSet. the bar chart class always
     * maintains a SelectionSet object which it updates on UI selections.
     * callers may call getSelectionSet to monitor BarChart selections and
     * to make selection changes which the BarChart will respond to. if
     * a non-null SelectionSet parameter is provided here, the BarChart will
     * listen to and modify the given one instead.
     */
    public BarChart(String xAxisLabel, String yAxisLabel, 
                    int xAxisScaleType, int yAxisScaleType, SelectionSet sel)
    {
        this.xAxisLabel = xAxisLabel;
        this.yAxisLabel = yAxisLabel;
        this.selections = sel == null ? new SelectionSet(BarValue.class) : sel;
        setScaleType(Axis.X_AXIS, xAxisScaleType);
        setScaleType(Axis.Y_AXIS, yAxisScaleType);
        setMinimumSize(new Dimension(
            CHART_MIN_DIM + CHART_LEFT_PAD + CHART_RIGHT_PAD,
            CHART_MIN_DIM + CHART_TOP_PAD  + CHART_BOTTOM_PAD));
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) {            
                int b = barAt(me.getPoint().x, me.getPoint().y);
                if(b < 0) {
                    if(!me.isControlDown())
                        selections.clear(BarChart.this);
                    repaint();
                    return;
                }
                BarValue selectedBar = barValues[b];
                selections.beginEditing(BarChart.this, false);
                if(me.isShiftDown())
                    selectRange(b, anchorSelectionIndex);
                else if(me.isControlDown())
                    selections.toggle(selectedBar, BarChart.this);
                else
                    selections.setElements(selectedBar, BarChart.this);
                if(!me.isShiftDown())
                    anchorSelectionIndex = b;
                selections.endEditing(BarChart.this);
                repaint();
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent me) {
                selectRange(barAt(me.getPoint().x), anchorSelectionIndex);
                repaint();
            }   
        });
    } // end constructor

    /**
     * provides the data to plot.
     * @param barValues is an array of items each of which can provide one
     * data value. i.e. the height of a single bar.
     * @param sortType describes how the data bars are to be sorted.
     * choices are:<br><pre>
     *    SORT_NONE
     *    SORT_ASCENDING
     *    SORT_DESCENDING
     * </pre>
     */
    public void setBarValues(BarValue barValues[], final int sortType) {
        this.barValues = new BarValue[barValues.length];
        System.arraycopy(barValues, 0, this.barValues, 0, barValues.length);
        highVal = Float.MIN_VALUE;
        for(int i=0; i<barValues.length; i++)
            highVal = Math.max(highVal, barValues[i].getBarValue());
        if (sortType != SORT_NONE) {
            Arrays.sort(this.barValues, new Comparator() {
                public int compare(Object a, Object b) {
                    float val1 = ((BarValue)a).getBarValue();
                    float val2 = ((BarValue)b).getBarValue();
                    if (sortType == SORT_ASCENDING) {
                        float tmp = val1;
                        val1 = val2;
                        val2 = tmp;
                    }
                    return val2 - val1 > 0 ? 1 : val2 == val1 ? 0 : -1; // NOTE: reverse numeric order
                }
            });
        }
        repaint();
    } // end setBarValues
    
    /**
     * specifies whether drawing should now use a log or linear scale
     * along the x axis.
     */
    public void setScaleType(int axis, int type) {
        if( ! (type == LOG_SCALE || type == LINEAR_SCALE))
            throw new IllegalArgumentException("BarChart.setScaleType: bad scale type " + type);
        if(axis == Axis.X_AXIS)
            xScaleType = type;
        else
            yScaleType = type;
        repaint();
    }

    /**
     * returns the selection set object being used.
     * this is either the one provided to the constuctor,
     * generated internally otherwise.
     */
    public SelectionSet getSelectionSet() {
        return selections;
    }

    private void selectRange(int b1, int b2) {
        if(b1 < 0 || b2 < 0)
            return;
        selections.clear(BarChart.this);
        int range_start = Math.min(b1, b2);
        int range_end = Math.max(b1, b2);
        for(int i=range_start; i<=range_end; i++)
            selections.addElement(barValues[i], BarChart.this);
    }
        
    private void computeBar(int barID, Rectangle rect) {
        int chart_width  = getWidth()  - (CHART_LEFT_PAD+CHART_RIGHT_PAD);
        int chart_height = getHeight() - (CHART_TOP_PAD+CHART_BOTTOM_PAD);
        int chart_right  = CHART_LEFT_PAD + chart_width;
        int chart_bottom = CHART_TOP_PAD + chart_height;
        rect.height = Math.round(barValues[barID].getBarValue() / highVal * chart_height);
        rect.y = CHART_TOP_PAD + (chart_height - rect.height);
        if (xScaleType==LINEAR_SCALE) {
            rect.width = Math.round(chart_width / (float)barValues.length);
            rect.x = CHART_LEFT_PAD + barID * rect.width;
        }
        else {
            rect.x = 0;
            if (barID > 0)
                rect.x = Axis.plotValue(barID, 
                                .5f, barValues.length, // value range
                                CHART_LEFT_PAD, getWidth()-CHART_RIGHT_PAD, // screen range
                                true, getHeight());
            int next = Axis.plotValue(barID+1, 
                                .5f, barValues.length, // value range
                                CHART_LEFT_PAD, getWidth()-CHART_RIGHT_PAD, // screen range
                                true, getHeight());
            rect.width = next - rect.x;
            rect.x += CHART_LEFT_PAD;
        }
    }
                
    public void paint(Graphics g) {
        super.paint(g);
        Point center = new Point(getWidth()/2, getHeight()/2);
        if(g instanceof Graphics2D)
            center.x -= stringWidth(NO_VALUES_MSG, g) / 2;
        if (barValues == null || barValues.length == 0) {
            g.drawString(NO_VALUES_MSG, center.x, center.y);
            return;
        }
        // draw the data boxes
        int lastxend = 0;
        int chart_width = getWidth() - (CHART_LEFT_PAD+CHART_RIGHT_PAD);
        boolean drawBoxSeperators = chart_width / (float)barValues.length > MIN_BOX_WIDTH_FOR_SEPERATOR;
        for(int i=0; i<barValues.length; i++) {
            g.setColor(selections.contains(barValues[i]) ? HIGHLIGHT_COLOR : NORMAL_COLOR);
            computeBar(i, tmpRect);
            if(i == 0)
                lastxend = tmpRect.x + tmpRect.width;
            else {
                if (tmpRect.x > lastxend) {
                    int diff = tmpRect.x - lastxend;
                    tmpRect.x = lastxend;
                    tmpRect.width += diff;
                }
            }
            lastxend = tmpRect.x+tmpRect.width; 
            //System.out.println(tmpRect.x + "," + tmpRect.y + " w=" + tmpRect.width + " h=" + tmpRect.height + " lastxend=" + lastxend);
            g.fillRect(tmpRect.x, tmpRect.y, tmpRect.width, tmpRect.height);
            g.setColor(Color.gray);
            if(drawBoxSeperators && i > 0) // draw a line between each box pair
                g.drawLine(tmpRect.x, tmpRect.y, tmpRect.x, tmpRect.y+tmpRect.height);
        }
        // draw the axes
        g.setColor(Color.black);
        Axis.drawAxis(Axis.X_AXIS, MAX_TIC_LABELS, TIC_LENGTH,
                .5f, barValues.length, // value range
                CHART_LEFT_PAD, getWidth()-CHART_RIGHT_PAD, // screen range
                CHART_BOTTOM_PAD, xScaleType==LOG_SCALE, getHeight(), g);
        Axis.drawAxis(Axis.Y_AXIS, MAX_TIC_LABELS, TIC_LENGTH,
                0, highVal, // value range
                CHART_BOTTOM_PAD, getHeight()-CHART_TOP_PAD, // screen range
                CHART_LEFT_PAD, yScaleType==LOG_SCALE, getHeight(), g);
        Font bold = g.getFont().deriveFont(Font.BOLD);
        g.setFont(bold);
        if (xAxisLabel != null) {
            g.drawString(xAxisLabel, getWidth()-stringWidth(xAxisLabel, g)-20, getHeight()-10);
        }
        if (yAxisLabel != null) {
            g.drawString(yAxisLabel, CHART_LEFT_PAD-40, 25);
        }
    } // end paint

    private int barAt(int x, int y) {
        if(barValues == null)
            return -1;
        for(int i=0; i<barValues.length; i++) {
            computeBar(i, tmpRect);
            if(rectContainsPoint(tmpRect, x, y))
                return i;
        }
        return -1;
    }
    
    private static boolean rectContainsPoint(Rectangle rect, int x, int y) {
        return 
            rect.x <= x && x <= rect.x+rect.width  &&
            rect.y <= y && y <= rect.y+rect.height;
    }

    private int barAt(int x) {
        if(barValues == null)
            return -1;
        for(int i=0; i<barValues.length; i++) {
            computeBar(i, tmpRect);
            if(tmpRect.x <= x && x <= tmpRect.x+tmpRect.width)
                return i;
        }
        return -1;
    }

    /**
     * handy little utility for determining the length in pixels the
     * given string will use if drawn into the given Graphics object.
     */
    public static int stringWidth(String str, Graphics g) {
        if(g instanceof Graphics2D)
            return (int)(g.getFont().getStringBounds(str, ((Graphics2D)g).getFontRenderContext()).getWidth()+.5);
        else
            return g.getFontMetrics().stringWidth(str);
    }


     //
     // TEST CODE FROM HERE DOWN
     //
 
    /**
     * an example data class
     */
    private static class TestDatum implements BarChart.BarValue {
        private int id;
        private int dataValue;
        public TestDatum(int id, int value) {
            this.id = id;
            this.dataValue = value;
        }
        public int getID() { return id; }
        public int getDataValue() { return dataValue; }
        /**
         * implementation of the BarValue interface 
         */
        public float getBarValue() { return getDataValue(); }
        public String getInfoText() { return "id # " + id + ", " + dataValue + " value"; }
    }
        
    /**
     * example data. unsorted but will have BarChart perform the sorting.
     */
    private final static TestDatum testSamples[] = new TestDatum[] {
        new TestDatum(12, 2020),
        new TestDatum(88, 2300),
        new TestDatum(43, 3001),
        new TestDatum(81, 2405),
        new TestDatum(10, 2069),
        new TestDatum( 2, 2054),
        new TestDatum(74, 2339),
        new TestDatum(56, 2020),
        new TestDatum(57, 2021),
        new TestDatum(58, 2023),
        new TestDatum(59, 2022),
        new TestDatum( 4, 4700),
        new TestDatum(98, 3100),
        new TestDatum(90, 3454),
        new TestDatum(33, 2560),
        new TestDatum(99, 2299),
        new TestDatum(78, 2020),
        new TestDatum(65, 2020),
    };
    
    /**
     * an example main method demonstrating use of the BarChart class.
     */
    public static void main(String args[]) {
        SelectionSet selections = new SelectionSet(TestDatum.class);
        selections.addSelectionSetListener(new SelectionSetListener() {
            public void selectionSetChanged(SelectionSet set, Object source) {
                System.out.println("main: " + set.getNumElements() + " selected "); 
            }
        });
        BarChart chart = new BarChart("Number of Samples", "Data Value", 
                    BarChart.LOG_SCALE, BarChart.LINEAR_SCALE, selections);
        chart.setBarValues(testSamples, BarChart.SORT_DESCENDING);
        JFrame frame = new JFrame("Bar Chart Test");
        frame.getContentPane().add(chart);
        frame.setSize(new Dimension(500, 500));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
    
} // end class BarChart


