package com.njupt.utils;

import java.net.*;
import java.awt.*;
import java.io.File;
import javax.swing.*;

/**
 * Utilities for getting Icons and related resources.
 * Icon file names are assumed to be rooted in a folder named "resources"
 * expected to be found in the classpath.<br>
 * Copyright (c) 2004, Superliminal Software
 * @author Melinda Green
 */
public class IconUtils {
    /**
     * @param name is either a fully qualified url path or a relative file path.
     * @return URL version of name if a fully qualified url path otherwise a url
     * relative to name with 'resources/' prepended and expected to be found in
     * the classpath.
     */
    public static URL getResource(String name) {
        if(name == null)
            return null;
        URL url = null;
        try {
            if(name.indexOf(':') == -1) {
                String path = "resources" + File.separatorChar + name; // getResource always looks for forward slash separators.
                url = IconUtils.class.getClassLoader().getResource(path);
            }
            else
                url = new URL(name);
        }
        catch(Exception e){
            System.err.println("IconUtils.getResource: can't load resource: " + name);
        }
        return url;
    }

    /**
     * Constructs an HTML string suitable for Swing labels and other components
     * containing an image followed by string label.
     * @param iconpath HTML "src" URL
     * @param label text to display after the image
     * @return HTML string with icon and label
     */
    public static String imageString(URL iconpath, String label) {
        return "<html><img src=\"" + iconpath + "\">&nbsp;&nbsp;" + label + "</html>";
    }

    public static Icon getIcon(String fname) {
        URL iurl = getResource(fname);
        if(iurl == null)
            return new LED(Color.red); // an error indication
        return new ImageIcon(iurl);
    }

    public static void tryToSetIcon(String name, AbstractButton into) {
        URL iurl = getResource(name);
        if(iurl != null)
            into.setIcon(new ImageIcon(iurl));
    }

    public static void tryToSetIcon(String name, JFrame into) {
        URL iurl = getResource(name);
        if(iurl != null)
            into.setIconImage(new ImageIcon(iurl).getImage());
    }

    public static void tryToSetIcon(String name, JLabel into) {
        URL iurl = getResource(name);
        if(iurl != null)
            into.setIcon(new ImageIcon(iurl));
    }

    /**
     * A small 3D light for control panels, etc.
     * May eventually contain methods to blink, etc.
     */
    public static class LED implements Icon {
        private Color color;
        private int HEIGHT = 10;
        private int WIDTH = 10;
        public int getIconWidth() { return WIDTH; }
        public int getIconHeight() { return HEIGHT; }

        public LED(Color color) {
            setColor(color);
        }

        /**
         * Create a rectangle of the color, height and width desired
         * @param color
         * @param height
         * @param width
         */
        public LED(Color color, int height, int width) {
            HEIGHT = height;
            WIDTH = width;
            setColor(color);
        }

        public void setColor(Color color) {
            this.color = color;
        }

        public void paintIcon(Component comp, Graphics g, int x, int y) {
            g.setColor(color);
            g.fill3DRect(x, y, getIconWidth(), getIconHeight(), true);
        }
    }

    private IconUtils() {} // disallows construction of utility class
}

