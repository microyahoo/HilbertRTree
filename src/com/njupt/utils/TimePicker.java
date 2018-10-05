package com.njupt.utils;

import javax.swing.*;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Calendar;
import java.util.Arrays;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.text.DateFormat;

/**
 * Presents two or more selectable dates arrayed on a timeline.<br>
 * 
 * Created Apr 28, 2006
 *
 * @author Melinda Green
 */
public class TimePicker extends JPanel {
    private final static String MONSTRS[] = {
        "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };
    private final static int PAD=30, BW=20, DAYTIC=8;
    private final static String FAMILY = "Sans Serif";
    private final static Font
        NONTH_FONT = new Font(FAMILY, Font.BOLD,  14),
        DAY_FONT   = new Font(FAMILY, Font.BOLD,  12),
        HOUR_FONT  = new Font(FAMILY, Font.PLAIN, 10);
    private Date[] dates;
    private JRadioButton butts[];

    public static interface SelectionListener {
        public void selectionChanged(int selection);
    }

    /**
     * Convenience constructor assumes no initial selection or selection listener.
     */
    public TimePicker(Date[] dates) {
        this(dates, -1, null);
    }

    public Dimension getMinimumSize() {
        return new Dimension(2*PAD, 2*PAD);
    }

    /**
     * @param dates must have 2 or more entries sorted from newest to oldest.
     * @param initialSelection index of date to initially select or -1 if none.
     * @param dl optional callback object notified whenever selection changes.
     */
    public TimePicker(Date[] dates, int initialSelection, final SelectionListener dl) {
        this.dates = dates;
        this.setPreferredSize(new Dimension(300, 70));
        this.setLayout(null); // because we will position the buttons explicitly
        ActionListener selectionWatcher = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(dl != null)
                    dl.selectionChanged(getSelection());
            }
        };
        ButtonGroup group = new ButtonGroup();
        butts = new JRadioButton[dates.length];
        for(int i=0; i<butts.length; i++) {
            JRadioButton butt = new JRadioButton();
            butts[i] = butt;
            butt.setSize(BW, BW);
            butt.setOpaque(false);
            butt.setToolTipText(dates[i].toString());
            group.add(butt);
            butt.addActionListener(selectionWatcher);
            butt.setSelected(i == initialSelection);
            this.add(butt);
        }
    }

    /**
     * @return the index of the currently selected date or -1 if none.
     */
    public int getSelection() {
        for(int i=0; i<butts.length; i++)
            if(butts[i].isSelected())
                return i;
        return -1;
    }

    /**
     * Positions all the radio buttons.
     */
    public void doLayout() {
        int w = getWidth(), h = getHeight();
        float spanInMillis = dates[0].getTime() - dates[dates.length-1].getTime();
        float pixelsPerMilli = (w-2*PAD) / spanInMillis;
        // first arrange them all in a line ignoring overlaps
        for(int i=0; i<butts.length; i++) {
            float offset = (dates[0].getTime() - dates[i].getTime()) * pixelsPerMilli;
            butts[i].setLocation((int)(w-PAD-offset-BW/2), h-2*PAD);
        }
        // next, make a pass looking for overlaps and adjust vertically when that fixes them, otherwise too bad.
        int availableVerticalSpace = butts[0].getY() + butts[0].getHeight();
        int minx[] = new int[availableVerticalSpace/BW]; // leftmost edges for each row.
        Arrays.fill(minx, Integer.MAX_VALUE);
        for (JRadioButton cur : butts) { // place each button
            int rightEdge = cur.getX() + cur.getWidth();
            for (int row = 0; row < minx.length; row++) { // counting up from base row
                if (rightEdge < minx[row]) {
                    minx[row] = cur.getX(); // set the new minimum for this row
                    cur.setLocation(cur.getX(), cur.getY() - row * BW);
                    break; // go to positioning next button
                }
            }
        }
    }

    public void paintComponent(Graphics g) {
        Font origFont = g.getFont();
        int w = getWidth(), h = getHeight();
        g.setColor(getBackground());
        g.fillRect(0, 0, w, h);
        float spanInMillis = dates[0].getTime() - dates[dates.length-1].getTime();
        float pixelsPerMilli = (w-2*PAD) / spanInMillis;
        GregorianCalendar monthStart = round(dates[0], Calendar.MONTH);
        monthStart.add(Calendar.MONDAY, 1);
        int xend = w;
        Color bg = getBackground();
        Color fg = Color.black;
        Color other = StaticUtils.slightlyDifferentColor(bg);
        g.setFont(NONTH_FONT);
        boolean even = true;
        while(xend > 0) {
            g.setColor(other);
            even = !even;
            float offset = (dates[0].getTime() - monthStart.getTimeInMillis()) * pixelsPerMilli;
            int xst = (int)(w-PAD-offset);
            if(even)
                g.fillRect(xst,0,xend-xst,h);
            g.setColor(fg);
            g.drawString(MONSTRS[monthStart.get(Calendar.MONTH)], xst+3, h-3);
            xend = xst;
            monthStart.add(Calendar.MONDAY, -1);
        }
        g.setColor(fg);
        // vertical line showing "now"
        //int now = w-PAD-(int)((dates[0].getTime() - System.currentTimeMillis()) * pixelsPerMilli);
        //g.drawLine(now, h-PAD, now, 20);
        //g.drawString("now", now-width("now",g)/2, g.getFontMetrics().getAscent());
        // horizontal time axis
        g.drawLine(0, h-PAD, w, h-PAD);
        // for short time spans, draw tick marks for days and possibly hours.
        float pixelsPerMinute = pixelsPerMilli * 1000 * 60;
        float pixelsPerHour = pixelsPerMinute * 60;
        float pixelsPerDay = pixelsPerHour * 24;
        if(pixelsPerDay > 0) {
            GregorianCalendar midnight = round(dates[dates.length-1], Calendar.DAY_OF_MONTH);
            GregorianCalendar end = new GregorianCalendar();
            end.setTime(dates[0]);
            while(midnight.before(end)) {
                int daytic = PAD+(int)((midnight.getTimeInMillis() - dates[dates.length-1].getTime()) * pixelsPerMilli);
                g.fillRect(daytic, h-PAD-DAYTIC, 2, DAYTIC);
                if(pixelsPerDay > 18) { // enough room so day-of-month numbers don't overlap?
                    String daystr = ""+midnight.get(Calendar.DAY_OF_MONTH);
                    if(pixelsPerDay > 35) // enough room for month number too?
                        daystr = (midnight.get(Calendar.MONTH)+1) + "/" + daystr;
                    g.setFont(DAY_FONT);
                    g.drawString(daystr, daytic-width(daystr,g)/2, h-PAD+12);
                    g.setFont(HOUR_FONT);
                    if(pixelsPerHour > 5) { // enough room for hour data too?
                        for(int hour=1; hour<24; hour++) {
                            int hx = (int)(daytic+hour*pixelsPerHour+.5);
                            if(pixelsPerHour > 18) { // enough room so hour-of-day numbers don't overlap?
                                String hourstr = ""+hour;
                                g.drawString(hourstr, hx-width(hourstr,g)/2, h-PAD-2);
                                if(pixelsPerMinute > 5) { // enough room for minute data too?
                                    for(int minute=1; minute<60; minute++) {
                                        int mx = (int)(hx+minute*pixelsPerMinute+.5);
                                        String minutestr = ""+minute;
                                        if(pixelsPerMinute > 20) { // enough room so minute numbers don't overlap?
                                            if(pixelsPerMinute > 35)
                                                minutestr = hourstr + ':' + minutestr; // yes, and room for hour too
                                            g.drawString(minutestr, mx-width(minutestr,g)/2, h-PAD-2);
                                        }
                                        else // no, only enough room for a minute tic mark
                                            g.drawLine(mx, h-PAD, mx, h-PAD-DAYTIC/3);
                                    }
                                }
                            }
                            else // no, only enough room for an hour tic mark
                                g.drawLine(hx, h-PAD, hx, h-PAD-DAYTIC/2);
                        }
                    }
                }
                midnight.add(Calendar.DAY_OF_MONTH, 1);
            }
            // paint labels in the lower corners with the dates of the first and last dates in the range
            g.setFont(HOUR_FONT); // because it's a good small font
            DateFormat df = DateFormat.getDateInstance();
            StaticUtils.fillString(df.format(dates[dates.length-1]), 3, h-3, bg, g);
            String date0 = df.format(dates[0]);
            Rectangle2D strrect = g.getFontMetrics().getStringBounds(date0, null);
            StaticUtils.fillString(date0, w-(int)strrect.getWidth()-3, h-3, bg, g);

            g.setFont(origFont);
        }
    }

    private static int width(String str, Graphics g) {
        return (int)(g.getFontMetrics().getStringBounds(str, null).getWidth()+.5);
    }

    /**
     * Rounds a given date to a specified unit.
     * @param date date to round
     * @param unit one of Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND.
     * @return date rounded to the nearest specified unit.
     */
    public static GregorianCalendar round(Date date, int unit) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        switch(unit) {
            case Calendar.YEAR: return
                new GregorianCalendar(cal.get(Calendar.YEAR), 0, 1);
            case Calendar.MONTH: return
                new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), 1);
            case Calendar.DAY_OF_MONTH: return
                new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            case Calendar.HOUR_OF_DAY: return
                new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY), 0);
            case Calendar.MINUTE: return
                new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
            case Calendar.SECOND: return
                new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), 0);
        }
        return null;
    }

    /**
     * A simple example test program.
     */
    public static void main(String[] args) {
        JFrame frame = new StaticUtils.QuickFrame("TimePicker Test");
        frame.setSize(600, 154);
        long now = System.currentTimeMillis();
        long daylen = 1000 * 60 * 60 * 24;
        frame.add(new TimePicker(
            //new Date[] {new Date(now), new Date(now-daylen/4), new Date(now-daylen*2)},
            new Date[] {
                new Date(now), new Date(now-daylen/4), new Date(now-daylen/3), new Date(now-daylen/2), new Date(now-daylen*2),
                new Date(now-daylen*8), new Date(now-daylen*8-daylen/2), new Date(now-daylen*8-daylen),
                new Date(now-daylen*20)
            },
            0,
            new TimePicker.SelectionListener() {
                public void selectionChanged(int selection) {
                    System.out.println("Selected date " + selection);
                }
            }
        ));
        frame.setVisible(true);
    }
}

