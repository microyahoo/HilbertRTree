package com.njupt.utils;

// JarLoader.java

import java.io.*;
import java.util.*;
import java.util.jar.*;

// for main test only
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * A JarLoader object is able to load all the jar files in a given
 * directory containing classes of a given type. It can also be set
 * to continuously track that directory for new jar files that are
 * added later. 
 * Listeners can be added to JarLoader objects which will be notified 
 * when the list of valid jar files in that directory changes.
 *<br>
 * Also contains a static method for dynamically loading a given jar file.
 *
 * @author Melinda Green - Superliminal Software http://www.superliminal.com
 */
public class JarLoader  {

    /**
     * When added as JarLoader listeners, objects implementing this interface
     * are informed whenever the contained list of loaded classes changes.
     */
    public interface ListListener {
        /**
         * Notification method implemented by jarLoader list listeners.
         * These implementations typically follow up with calls to 
         * <code>getLoadedClassNames()</code> and/or
         * <code>getClass(String name)</code> to discover the changes.
         */
        public void listChanged(JarLoader loader);
    }

    private Class target;
    final private String jarClassKey;
    private boolean watchForChanges;
    private HashMap name2class = new HashMap();
    private int last_jar_file_count = -1; //tracks when listeners need to be notified.
    private File directory;
    public File getDirectory() { return directory; }


    /**
     * Used to discover the names of all classes currently
     * loaded by this JarLoader instance.
     * @return an array of the names of all the 
     * classes currently loaded by this JarLoader.
     * Note, this list is suitable for display in a JComboBox.
     */
    public String[] getLoadedClassNames() {
        Object keys[] = null;
        keys = name2class.keySet().toArray(new String[0]);
        return (String[])keys;
    }

    /**
     * Retrives the Class object for a previously loaded class.
     * @param name is the name of a previously loaded class
     * presumably reported in a previous call to getLoadedClassNames.
     * @return the class object corrisponding to the loaded
     * class with the given name, or null if not found.
     */
    public Class getClass(String name) {
        return (Class)name2class.get(name);
    }
    
    /**
     * Creates a JarLoader to which loads from and possibly tracks jar
     * files of a given type in a directory. 
     * The loaded classes are available via <code>getClass(String name)</code>
     * where "name" was presumably gotten from <code>getLoadedClassNames()</code>.
     * @param jarClassKey is the key to look for in each jar's manifest file.
     * JarLoader will load one class from each jar file who's manifest file
     * contains a key/value attribute pair of the following form:
     * <jarClassKey>: <Class name>
     * <br>Example: <code>new JarLoader("My-Component", MyComponent.class, "plugins", false)</code><br>
     * will load all classes of type MyComponent identified by the 
     * "My-Component" key in each jar file's manifest file for each jar file
     * found in the "plugins" directory.
     * @param containing is a base class or interface which loaded classes 
     * must inherit from or implement in order to be loaded.
     * @param dir is the directory in which to look for jar files.
     * @param watchDir is a flag stating whether this JarLoader should
     * continuously monitor the given directory for changes in the list of
     * loadable jar files found there.
     */
    public JarLoader(String jarClassKey, Class containing, String dir, boolean watchDir) {
        directory = new File(dir);
        if(!directory.isDirectory())
            throw new java.security.InvalidParameterException(directory + " not a directory");
        this.jarClassKey = jarClassKey;
        target = containing;
        watchForChanges = watchDir;
        load(); //always loads jar files in given directory at least once.
        if(watchForChanges) {
            new Thread() {
                public void run() {
                    //System.out.println("Starting watcher");
                    while(true) {
                        try { Thread.sleep(1000); }
                        catch(InterruptedException ie) {}
                        load(); //reload jar files if directory changed
                    }
                }
            }.start();
        }
    }

    /**
     * The directory version of <code>load(File...)</code> which loads all conforming
     * Classes found in the jar files of the directory with which this JarLoader
     * instance was created.
     */
    private void load() {
        File jar_files[] = directory.listFiles(new FileFilter()  {
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".jar");
            }
        });
        if(jar_files.length == last_jar_file_count)
            return; //nothing to do so go back to sleep
        name2class.clear();
        //System.out.println("found " + jar_files.length + " jar files");
        last_jar_file_count = jar_files.length; // remember for next time
        for(int i=0; i<jar_files.length; i++)  {
            Class loaded_class = load(jar_files[i], jarClassKey, name2class);
            if(loaded_class!=null && target.isAssignableFrom(loaded_class)) {                  
                name2class.put(loaded_class.getName(), loaded_class);
            }
        }
        fireListChanged();
    }


    /**
     * The workhorse of JarLoader which performs the actual dynamic loading
     * of a single jar file.
     * @param jarFile is a java File object representing the jar file to load.
     * @param jarClassKey is the key to look for in each jar's manifest file.
     * @param name2class is an optional HashMap which, if supplied, will return
     * null if the class in the given jar file has the same name as a String
     * key in the HashMap. In all cases the given HashMap is left unchanged.
     * @return The Class loaded from the jar file or null on error.
     */
     public static Class load(File jarFile, String jarClassKey, HashMap name2class) {
        JarFile the_jar = null;
        try {
            final JarFile ajar = new JarFile(jarFile);
            the_jar = ajar; // so it can be closed regardless of exceptions
            Manifest manifest = ajar.getManifest();
            Map map = manifest.getEntries();
            Attributes att = manifest.getMainAttributes();
            final String loaded_class_name = att.getValue(jarClassKey);
            if(loaded_class_name == null) {
                System.out.println("can't find class to load in manifest at the key: " +
                    jarClassKey);
                return null;
            }
            if(name2class!=null && name2class.get(loaded_class_name) != null)
                return (Class)name2class.get(loaded_class_name); // already have this one
            //System.out.println("loading " + loaded_class_name);
            Class loaded_class = new ClassLoader()  {
                public Class findClass(String name) {
                    JarEntry loaded_class_entry = ajar.getJarEntry(name);
                    if(loaded_class_entry == null)
                        return(null); 
                    try {  
                        InputStream is = ajar.getInputStream(loaded_class_entry);
                        int available = is.available();
                        byte data[] = new byte[available];
                        is.read(data);
                        return defineClass(name, data, 0, data.length);
                    }
                    catch(IOException ioe)  {
                        System.out.println("Exception: " + ioe);
                        return(null);
                    }
                }
            }.loadClass(loaded_class_name);
            return loaded_class;
        }
        catch(Exception e) {
            System.out.println("Exception: " + e);
            return null;
        }
        finally { //insures jar file is always closed regardless of exceptions
            if(the_jar != null) {
                try { the_jar.close(); }
                catch(IOException ioe) {}
            }
        }
    } // end load(File...)

    //Listener handling methods
    private Vector listListeners = new Vector();
    public void removeListListener(JarLoader.ListListener ll) { listListeners.remove(ll); }
    private void fireListChanged() {
        for(Enumeration e=listListeners.elements(); e.hasMoreElements(); ) {
            ((ListListener)e.nextElement()).listChanged(this);
        }
    }
    /**
     * Adds a listener to be notified when conforming jar files are added
     * or removed from the given directory.
     * The given listeners are notified immediately in order to let them
     * do any initializations involving the currently loaded classes.
     */
    public void addListListener(JarLoader.ListListener ll) {
        listListeners.add(ll);
        ll.listChanged(this);
    }


    //EVERYTHING FROM HERE DOWN IN THIS FILE IS FOR TESTING ONLY
    //
    //INSTRUCTIONS:
    //In order to run this test you will need to compile this file and then
    //create two jar files for the two concrete "loadable" classes
    //defined below. To do that, you can use Sun's "jar" tool to package
    //each loadable class into a jar file. To make that simple, extract
    //the following text into the named files but without indentation:
    //
    //LoadableEntry1.txt:
    //    Manifest-Version: 1.0
    //    Loadable-Class: JarLoader$TestLoadable1
    //    
    //LoadableEntry1.txt:
    //    Manifest-Version: 1.0
    //    Loadable-Class: JarLoader$TestLoadable2
    //    
    //Note the extra blank line at the end of each file (three lines total).
    //Next, create a file named maketest.bat" (for Windows) containing
    //the following command:
    //    c:/jdk1.3/bin/jar cfm test1.jar LoadableEntry1.txt *TestLoadable1.class
    //    c:/jdk1.3/bin/jar cfm test2.jar LoadableEntry2.txt *TestLoadable2.class
    //With your correct path to the "jar" program.
    //That batch file will create proper jar files which you can move in and
    //out of the program directory while running the test program resulting
    //in controll buttons being created and deleted in response.
    //You can then rerun the batch file if you change the names of your
    //loadable classes or make other changes that affect the jar files.

    /**
     * A loadable test base class.
     * Jar files containing classes derived from this base class.
     * will be loaded by the main method.
     */
    private interface TestLoadableBase {
        public void print();
    }
    /**
     * An example loadable test class.
     * NOTE: Since this one happens to be an inner class,
     * it's important that it be static. 
     * Otherwise it would not be instantiatiable outside of a parent instance.
     */
    private static class TestLoadable1 implements TestLoadableBase {
        public TestLoadable1() {} //empty constructor required for class to be accessible
        public void print() { System.out.println("print called on a TestLoadable1"); }
    }
    /**
     * Another example loadable test class.
     */
    private static class TestLoadable2 implements TestLoadableBase {
        public TestLoadable2() {} //empty constructor required for class to be accessible
        public void print() { System.out.println("print called on a TestLoadable2"); }
    }

    /**
     * A test application for JarLoader which loads and monitors the jar
     * files in the current directory. A GUI is presented with interactive
     * objects dynamically loaded jar files. Adding and removing those jar files
     * from the current directory while the program is running will create and 
     * delete those GUI representations.
     * @param args unused.
     */
    public static void main(String args[]) {
        final int WIN_WIDTH = 500, WIN_HEIGHT = 300, BUTT_WIDTH = 200, BUTT_HEIGHT = 30;
        final HashMap butt2position = new HashMap();
        JarLoader the_loader = new JarLoader("Loadable-Class", TestLoadableBase.class, ".", true);
        final JPanel class_list = new JPanel(null);
        class_list.setSize(new Dimension(WIN_WIDTH, WIN_HEIGHT));
        JarLoader.ListListener loader_listener = new ListListener()  {
            public void listChanged(final JarLoader loader) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        //System.out.println("List changed");
                        class_list.removeAll();
                        String available_classes[] = loader.getLoadedClassNames();
                        for(int i=0; i<available_classes.length; i++)  {
                            //Note that all final variables in this block are 
                            //local to the button being constructed.
                            final String class_name = available_classes[i];
                            final JButton my_butt = new JButton(class_name);
                            final Point bounds_start = new Point();
                            final Point drag_start = new Point();
                            Point pos = (Point)butt2position.get(class_name);
                            if(pos == null) {
                                pos = new Point(
                                    (int)(Math.random() * (WIN_WIDTH  - BUTT_WIDTH)), 
                                    (int)(Math.random() * (WIN_HEIGHT - BUTT_HEIGHT)));
                                butt2position.put(class_name, pos); // so visible don't move when others are added or removed
                            }
                            my_butt.setBounds(pos.x, pos.y, BUTT_WIDTH, BUTT_HEIGHT);
                            my_butt.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent ae) {
                                    Class loadable_class = loader.getClass(class_name);
                                    try {
                                        TestLoadableBase loadable = (TestLoadableBase)loadable_class.newInstance();
                                        loadable.print();
                                    }
                                    catch(Exception e) {System.out.println("Exception: " + e);}
                                }
                            });
                            my_butt.addMouseListener(new MouseAdapter() {
                                public void mousePressed(MouseEvent me) {
                                    drag_start.x = me.getX();
                                    drag_start.y = me.getY();
                                    bounds_start.x = my_butt.getBounds().x;
                                    bounds_start.y = my_butt.getBounds().y;
                                }                                
                            });
                            my_butt.addMouseMotionListener(new MouseMotionAdapter() {
                                public void mouseDragged(MouseEvent me) {
                                    //NOTE: I can't figure out why the reported positions on
                                    //the next line are wrong and get worse the more you drag!
                                    //System.out.println("MouseEvent position = " + me.getPoint());
                                    int xdif = me.getX() - drag_start.x;
                                    int ydif = me.getY() - drag_start.y;
                                    Point pos = (Point)butt2position.get(class_name);
                                    pos.x = bounds_start.x + xdif;
                                    pos.y = bounds_start.y + ydif;
                                    my_butt.setBounds(pos.x, pos.y, BUTT_WIDTH, BUTT_HEIGHT);
                                }
                            });
                            class_list.add(my_butt);
                        }
                        class_list.repaint();
                    }
                });
            }
        };
        the_loader.addListListener(loader_listener);
        JPanel content = new JPanel();
        content.setLayout(new BorderLayout());
        JTextArea info = new JTextArea(
            "The following combo box contains a list of all currently loaded " +
            "classes of type 'TestLoadableBase' from the jar files found in this directory. " +
            "Adding and removing TestLoadableBase jar files will cause the contents " +
            "of the combo box to change immediately to reflect the available " +
            "jar files. \n\n" +
            "Selecting a TestLoadable class in the combo box causes an instance " +
            "of that class to be created and its 'print' method to be called.\n\n" +
            "Drag the buttons around to adjust their positions."
        );
        info.setLineWrap(true);
        info.setWrapStyleWord(true);
        content.add("North", info);
        content.add("Center", class_list);
        JFrame frame = new JFrame("JarLoader Test");
        frame.getContentPane().add(content);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(new Dimension(500, 500));
        frame.show();
    } //end main

}

