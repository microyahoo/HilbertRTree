package com.njupt.utils;

// WeakProxyReference.java

import java.lang.ref.*;
import java.lang.reflect.*;

//These imports are just for the test code used by the main method
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * DESCRIPTION
 * <p>
 * This class contains a static <code>create</code> method which creates a proxy
 * for a given object and adds it to a given object to a given container in such
 * a way that all the methods invoked on that proxy by the container will be
 * forwarded to the original object. It should work exactly as if the object had
 * been added directly to the container but with one important difference: No
 * strong reference to the object will be created in the process. This allows
 * the object <i>and any resources that object references</i> to be garbage
 * collected when no other parts of your application reference it. This helps
 * eliminate one of the most common sources of memory leaks usually involving
 * listener objects that keep their referenced windows, etc. from being garbage
 * collected <i>without requiring you to figure out when to remove those
 * listeners yourself</i>. In other words, it makes it possible to
 * "set and forget" your listeners.
 * <p>
 * 
 * The only caveat is that you will need to make sure that there is at least one
 * strong reference to your listener objects in your application, otherwise they
 * will now be immediately garbage collectable. That's because you can no longer
 * count on the container holding a strong reference to your listener. The
 * cleanest solution is to make the thing being referenced by the listener
 * (rather than the container which would have held the listener) keep any sort
 * of strong reference to that listener. This sort of circular reference will
 * keep both resource and listener alive until no other parts of your
 * application hold references to the resource.
 * <p>
 * 
 * EXAMPLE
 * 
 * <pre>
 * <code>
 * final JPanel my_panel = new JPanel(); // the expensive resource
 * ActionListener my_listener = new ActionListener() {
 *     public void ActionPerformed(ActionEvent ae) {
 *         ...code that directly affects "my_panel"...};
 *     }
 * });
 * JButton my_button = new JButton("Panel Controller");
 * // use the following instead of my_button.addActionListener(my_listener);
 * WeakProxyReference.create(my_button, my_listener, ActionListener.class);
 * my_panel.putClientProperty(new Object(), my_listener); // circular reference
 * </code>
 * </pre>
 * 
 * @author Melinda Green - Superliminal Software
 */
public class WeakProxyReference {
	private WeakReference reference_to_held; // weak reference to held object
	private Object proxyForHeld;
	private final static boolean DEBUG = true;

	/**
	 * Creates a weak proxy reference between two objects.
	 * 
	 * @param holder
	 *            is the object to which a weak proxy reference to the second
	 *            parameter is to be added.
	 * 
	 * @param held
	 *            is the object which holder is supposed to reference.
	 * 
	 * @param heldBaseClass
	 *            is the Class object for the <i>base class</i> of the held
	 *            object. IOW, the class of object the holder is expecting to
	 *            recieve. For example, if you want to add your own derived
	 *            action listener to a button, you must pass
	 *            ActionListener.class, <i>not</i> MyActionListener.class.
	 * 
	 * @param addMethodName
	 *            is the name of holder's method used to add objects of type
	 *            <code>heldBaseClass</code>.
	 * 
	 * @param removeMethodName
	 *            is the name of holder's method used to remove objects of type
	 *            <code>heldBaseClass</code>.
	 * 
	 * <br>
	 * <br>
	 *            <b>NOTE:</b> Because the object which you pass as the "held"
	 *            parameter will not be strongly held by the holder, you will
	 *            need to make sure you keep at least one reference to it. The
	 *            most natural place to do that is to store that reference is in
	 *            a private member variable of whatever object it operates on.
	 *            That sort of circular reference is good because both objects
	 *            will be garbage collected together when there are no external
	 *            references to either one. Or more prosaically, this makes them
	 *            bound by a mutual suicide pact.
	 */
	public static void create(Object holder, Object held, Class heldBaseClass,
			String addMethodName, String removeMethodName) {
		new WeakProxyReference(holder, held, heldBaseClass, addMethodName,
				removeMethodName);
	}

	/**
	 * Shorthand version of <code>create</code> method which assumes the add and
	 * remove method names are simply the heldBaseClass class name with the
	 * strings "add" and "remove" prepended. For example, since the JButton
	 * class has "addActionListener" and "removeActionListener" methods, you can
	 * create a WeakProxyReference from a JButton to an instance of your custom
	 * action listener like this: <br>
	 * <code>
	 * WeakProxyReference.create(myButton, myListener, ActionListener.class);
	 * </code><br>
	 * Be sure to see "NOTE" in the 5 argument version of this method.
	 */
	public static void create(Object holder, Object held, Class heldBaseClass) {
		String shortBaseName = heldBaseClass.getName().substring(
				heldBaseClass.getName().lastIndexOf('.') + 1);
		create(holder, held, heldBaseClass, "add" + shortBaseName, "remove"
				+ shortBaseName);
		if (DEBUG)
			System.out.println("Created a WeakProxyReference to a "
					+ shortBaseName);
	}

	/**
	 * Constructor is private since there's nothing useful the user can do with
	 * an object of this type.
	 */
	private WeakProxyReference(final Object holder, Object held,
			final Class heldBaseClass, String addMethodName,
			final String removeMethodName) {
		reference_to_held = new WeakReference(held);
		// Define the call-through behavior used by the proxy object we're about
		// to create which dispatches all method calls to the original target
		// object if it still exists, otherwise remove the unneeded proxy.
		InvocationHandler handler = new InvocationHandler() {
			public Object invoke(Object proxy, Method method, Object[] args) {
				String meth_name = method.getName();
				if (DEBUG)
					System.out.println("invocation handler invoked for "
							+ method.getName());
				Object original_held_object = reference_to_held.get();
				if (original_held_object == null) {
					// The equals() method is a special case
					// because container remove methods called below use equals
					// to find the element to remove, and because the object in
					// the collection is really a proxy object and not the
					// original held object. Normally we'd dispatch even the
					// equals method but since the held object is already gone,
					// we need to make sure the container remove methods still
					// work.
					if (meth_name.equals("equals") && proxy == proxyForHeld)
						return new Boolean(args[0] instanceof Proxy);
					// Because the weak reference object returned null, we know
					// the original held object is now only weakly reachable
					// and is therefore garbage-collectable or even already
					// garbage collected. Just to be clean we take this
					// opportunity to remove the little proxy object from the
					// holder since its no longer useful and can also be garbage
					// collected.
					Method remover = getMethodFor(holder, removeMethodName,
							new Class[] { heldBaseClass });
					if (DEBUG)
						System.out.println("\tremoving stale proxy");
					doInvoke(remover, holder, new Object[] { proxyForHeld });
					if (DEBUG)
						System.out.println("\tdone removing stale proxy");
					return null; // nobody to dispatch to
				}
				Method held_object_method = getMethodFor(original_held_object,
						meth_name, method.getParameterTypes());
				return doInvoke(held_object_method, original_held_object, args);
			}
		};
		// Create the proxy
		proxyForHeld = Proxy.newProxyInstance(held.getClass().getClassLoader(),
				new Class[] { heldBaseClass }, handler);
		// Add the proxy to the holder by looking up its appropriate "add"
		// method and invoking it.
		Method adder = getMethodFor(holder, addMethodName,
				new Class[] { heldBaseClass });
		doInvoke(adder, holder, new Object[] { proxyForHeld });
	}

	/**
	 * Helper function for invoking a given method on a given object with
	 * exceptions caught and dealt with.
	 */
	private static Object doInvoke(Method method, Object obj, Object[] args) {
		try {
			return method.invoke(obj, args);
		} catch (IllegalAccessException e) {
			printException(e);
		} catch (InvocationTargetException e) {
			printException(e);
		} catch (NullPointerException e) {
			printException(e);
		}
		return null;
	}

	/**
	 * Helper function for getting a given method of a given object with
	 * exceptions caught and dealt with.
	 */
	private Method getMethodFor(Object obj, String method_name,
			Class parameterTypes[]) {
		try {
			return obj.getClass().getMethod(method_name, parameterTypes);
		} catch (NoSuchMethodException e) {
			printException(e);
		}
		return null;
	}

	private static void printException(Exception e) {
		System.err.println("WeakListener exception " + e);
	}

	/**
	 * A test method for WeakProxyReference. Call with no arguments to run a
	 * simple example, and with any extra argument to run a more complex
	 * example.
	 */
	private static void main(String args[]) {
		if (args.length == 0)
			twoWindowTest();
		else
			buttonSelectionTest();
	}

	private static void twoWindowTest() {
		// Make a window with a button that will control another window.
		JButton controller_button = new JButton(
				"Toggle other window's background");
		JFrame control_frame = new JFrame("Control Window");
		control_frame.getContentPane().add(controller_button);
		control_frame.setLocation(new Point(100, 100));
		control_frame.pack();
		control_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Create the window to be controlled by the first one.
		final JFrame controlled_window = new JFrame(
				"Resource which needs to be garbage collectable");
		final JTextArea instructions = new JTextArea(
				"This window represents an expensive resource you want to be "
						+ "garbage collectible after it's closed. Note that the JFrame is "
						+ "set up to 'dospose on close', so clicking the X in the upper "
						+ "right hand corner should do it. But first, click the toggle "
						+ "button in the other window a few times to see that it has a "
						+ "button listener which references this window. Then close this "
						+ "window. To see the console message that the resource has indeed "
						+ "been garbage collected depends upon when, if ever, your VM does "
						+ "garbage collection. You will at least need to compile this code "
						+ "with the DEBUG flag set to true to generate the print statements. "
						+ "With the JDK 1.3 Windows VM I found that the garbage collector "
						+ "will kick in if after I've closed this window, I maximize the "
						+ "control window and then restore it to normal. When I then click "
						+ "the toggle one more time I get the message showing that the "
						+ "garbage collecter has done it's thing.");
		instructions.setLineWrap(true);
		instructions.setWrapStyleWord(true);
		controlled_window.getContentPane().add(instructions);
		controlled_window.setSize(new Dimension(450, 250));
		controlled_window.setLocation(new Point(400, 100));
		controlled_window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		// Create the controler which will affect the controled window.
		ActionListener toggler = new ActionListener() {
			boolean yellow = false; // the current background color

			public void actionPerformed(ActionEvent ae) {
				System.out.println("colored window being toggled");
				yellow = !yellow;
				instructions.setBackground(yellow ? Color.yellow : Color.white);
				instructions.revalidate();
			}
		};

		// Rather than calling
		// controller_button.addActionListener(controlled_window.getController())
		// like we normally would, here's the magic call to set up
		// the weak proxy reference instead:
		WeakProxyReference.create(controller_button, toggler,
				ActionListener.class);

		// Unfortunately replacing one call with another is not quite enough.
		// Here's the important line that circumvents the one drawback of
		// replacing a strong reference to the listener with a weak one.
		// We create a strong reference from the CONTROLLED object to the
		// listener that controls it. IOW a circular reference chain that's
		// garbage collectible when there are no external references left to
		// either of them which is the case after the controlled window is
		// closed. After that, both the controlled window and the control
		// listener are subject to garbage collection.
		// Note that you don't need to use putClientProperty to do this.
		// ANY call that would result in a strong reference to the listener
		// from any object that's only referenced within the controlled
		// resource will do. For the common case of AWT component listeners,
		// the putClientProperty method is particularly useful as it provides
		// a way to associate a client object with *any* component. In this case
		// the toggler listener is associated with the instructions object
		// it affects. The object passed as the first argument is completely
		// arbitrary since any unreferenced object will do.
		instructions.putClientProperty(new Object(), toggler);

		// It's showtime folks!
		control_frame.setVisible(true);
		controlled_window.setVisible(true);
	}

	/**
	 * A somewhat more complex example. In this case the resource and the
	 * controller live in the same panel. The "remove" button allows you to
	 * remove the resource--in this case a colored label--which then becomes
	 * garbage collectible.
	 * 
	 * The main functional difference between this example and the previous one
	 * is in the way the listener is kept alive by the thing it's affecting. In
	 * the previous example the hard reference was created by explicitly adding
	 * the listener as a client property of an object within the window it
	 * affects. In this case that reference is implicit in the fact that the
	 * listener lives as a member variable within the class it affects. Either
	 * way works fine. Just choose from these or other ways of making the
	 * association you need depending upon what's most natural in each case
	 * where you want to use a WeakProxyReference.
	 */
	private static void buttonSelectionTest() {
		// here's an inner class that represents a JLabel with toggleable color.
		// the toggle() method switches between two different background colors.
		class ColoredLabel extends JLabel {
			private boolean green = true; // current background color
			// NOTE: The line below creates and stores the action listener
			// inside the ColoredLabel class. The fact that it is stored here
			// is what keeps the listener from being garbage collected too soon.
			// It's essentially a hard reference to that listener that won't
			// be garbage collected until its containing ColoredLabel instance
			// is also collectible. That happens as soon as the label is removed
			// from it's containing panel by clicking the "remover" button.
			private ActionListener toggler = new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					toggle();
				}
			};

			public ActionListener getController() {
				return toggler;
			}

			private ColoredLabel(String title) {
				super(title);
				setFont(new Font("Courier", Font.BOLD, 30));
				toggle(); // to set original background color
			}

			private void toggle() {
				System.out.println("colored window being toggled");
				green = !green;
				setForeground(green ? Color.green : Color.blue);
				repaint();
				System.gc();
			}
		}
		final ColoredLabel text = new ColoredLabel("Resource");
		JButton control_button = new JButton("Toggle text field");
		JFrame control_frame = new JFrame("WeakProxyReference Test");
		final JButton remover = new JButton("Remove Resources");
		final JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		remover.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				panel.remove(text);
				panel.remove(remover);
				// text label and remover button are now garbage collectable
				panel.repaint();
			}
		});
		control_frame.getContentPane().add(panel);
		control_button.setAlignmentX(0.5f);
		panel.add(control_button);
		text.setAlignmentX(0.5f);
		panel.add(text);
		remover.setAlignmentX(0.5f);
		panel.add(remover);
		JTextArea instructions = new JTextArea(
				"The toggle button controls the color of the 'Resource' label. "
						+ "That label is acting as some expensive resource that you don't "
						+ "want kept alive just because by the control button has a "
						+ "listener which affects it. The 'remove' button removes both "
						+ "the resource label and the remove button itself, both of which "
						+ "then become garbage collectable. "
						+ "To see the console message that the resource has indeed been "
						+ "garbage collected, you need to compile this code with the DEBUG "
						+ "flag set to 'true' and you may need to maximize the window,  "
						+ "restore it to normal, and hit the toggle button once more "
						+ "before you see that the garbage collecter has done it's thing.");
		instructions.setLineWrap(true);
		instructions.setWrapStyleWord(true);
		panel.add(instructions);
		control_frame.setLocation(new Point(100, 100));
		control_frame.setSize(new Dimension(300, 300));
		control_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// Rather than calling
		// control_button.addActionListener(text.getController())
		// like we normally would, here's the magic call to set up
		// the weak proxy reference to the button listener
		WeakProxyReference.create(control_button, text.getController(),
				ActionListener.class);
		control_frame.setVisible(true);
	}

}
