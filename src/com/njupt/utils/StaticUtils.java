package com.njupt.utils;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.event.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.InetAddress;

/**
 * A collection of generally useful Swing utility methods.
 * 
 * Copyright: Copyright (c) 2004 Company: Superliminal Software
 * 
 * @author Melinda Green
 */
public class StaticUtils {
	// to disallow instantiation
	private StaticUtils() {
	}

	/**
	 * Adds a control hot key to the containing window of a component. In the
	 * case of buttons and menu items it also attaches the given action to the
	 * component itself.
	 * 
	 * @param key
	 *            one of the KeyEvent keyboard constants
	 * @param to
	 *            component to map to
	 * @param actionName
	 *            unique action name for the component's action map
	 * @param action
	 *            callback to notify when control key is pressed
	 */
	public static void addHotKey(int key, JComponent to, String actionName,
			Action action) {
		KeyStroke keystroke = KeyStroke.getKeyStroke(key,
				java.awt.event.InputEvent.CTRL_MASK);
		InputMap map = to.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		map.put(keystroke, actionName);
		to.getActionMap().put(actionName, action);
		if (to instanceof JMenuItem)
			((JMenuItem) to).setAccelerator(keystroke);
		if (to instanceof AbstractButton) // includes JMenuItem
			((AbstractButton) to).addActionListener(action);
	}

	/**
	 * Finds the top-level JFrame in the component tree containing a given
	 * component.
	 * 
	 * @param comp
	 *            leaf component to search up from
	 * @return the containing JFrame or null if none
	 */
	public static JFrame getTopFrame(Component comp) {
		if (comp == null)
			return null;
		while (comp.getParent() != null)
			comp = comp.getParent();
		if (comp instanceof JFrame)
			return (JFrame) comp;
		return null;
	}

	public static Window getActiveWindow() {
		return KeyboardFocusManager.getCurrentKeyboardFocusManager()
				.getActiveWindow();
	}

	public static void setWaitCursor() {
		if (getActiveWindow() != null)
			getActiveWindow().setCursor(
					Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}

	public static void setDefaultCursor() {
		if (getActiveWindow() != null)
			getActiveWindow().setCursor(
					Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	/**
	 * Different platforms use different mouse gestures as pop-up triggers. This
	 * class unifies them. Just implement the abstract popUp method to add your
	 * handler.
	 */
	public static abstract class PopperUpper extends MouseAdapter {
		// To work properly on all platforms, must check on mouse press as well
		// as release
		public void mousePressed(MouseEvent e) {
			if (e.isPopupTrigger())
				popUp(e);
		}

		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger())
				popUp(e);
		}

		protected abstract void popUp(MouseEvent e);
	}

	// simple Clipboard string routines

	public static void placeInClipboard(String str) {
		Toolkit.getDefaultToolkit().getSystemClipboard()
				.setContents(new StringSelection(str), null);
	}

	/**
	 * @return String contained in system clipboard if any and if accessible to
	 *         caller.
	 */
	public static String getFromClipboard() {
		try {
			return (String) Toolkit.getDefaultToolkit().getSystemClipboard()
					.getContents(null).getTransferData(DataFlavor.stringFlavor);
		} catch (UnsupportedFlavorException e) {
		} catch (IOException e) {
		}
		return null;
	}

	/**
	 * Replaces the contents of a container with a centered label. Useful for
	 * error messages or temporary messages like
	 * "Loading XXX View -- Please wait..." which later get replaced with the
	 * real view when the server data arives, etc.
	 */
	public static void showMessageLabel(String text, Container in) {
		JLabel label = new JLabel("<html><h2>" + text + "</h2><html>");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		in.removeAll();
		in.setLayout(new BorderLayout());
		in.add(label);
		in.validate();
	}

	/**
	 * Draws the given string into the given graphics with the area behind the
	 * string filled with a given background color.
	 */
	public static void fillString(String str, int x, int y, Color bg, Graphics g) {
		Rectangle2D strrect = g.getFontMetrics().getStringBounds(str, null);
		Color ocolor = g.getColor();
		g.setColor(bg);
		g.fillRect((int) (x + strrect.getX()), (int) (y + strrect.getY()),
				(int) (strrect.getWidth()), (int) (strrect.getHeight()));
		g.setColor(ocolor);
		g.drawString(str, x, y);
	}

	/**
	 * Sets the location of the given frame to be centered on the screen.
	 * Precondition: Frame must already have its size set either directly or via
	 * pack().
	 * 
	 * @param frame
	 *            the frame to center.
	 */
	public static void center(JFrame frame) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(
				Math.max(0, screenSize.width / 2 - frame.getWidth() / 2),
				Math.max(0, screenSize.height / 2 - frame.getHeight() / 2));
	}

	/**
	 * Utility class that initializes a meduim sized, screen-centered,
	 * exit-on-close JFrame. Mostly useful for simple example main programs.
	 */
	public static class QuickFrame extends JFrame {
		public QuickFrame(String title) {
			super(title);
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setSize(640, 480);
			center(this);
		}

		public QuickFrame(String title, Component content) {
			this(title);
			getContentPane().add(content);
		}
	}

	/**
	 * Compares a screen rectangle to the current graphics screens.
	 * 
	 * @param rect
	 *            represents the bounds of a window or other region in screen
	 *            space.
	 * @return true if the given rectangle is completely contained by one of the
	 *         current screens, false otherwise.
	 */
	public static boolean isOnScreen(Rectangle rect) {
		for (GraphicsDevice screenDevice : GraphicsEnvironment
				.getLocalGraphicsEnvironment().getScreenDevices())
			if (screenDevice.getDefaultConfiguration().getBounds()
					.contains(rect))
				return true;
		return false;
	}

	public static Color slightlyDifferentColor(Color from) {
		float rgb[] = new float[3];
		from.getColorComponents(rgb);
		float offset = -.05f;
		if (rgb[0] < .5 && rgb[1] < .5 && rgb[2] < .5)
			offset *= -1;
		for (int i = 0; i < 3; i++) {
			rgb[i] += offset;
			rgb[i] = Math.min(rgb[i], 1);
			rgb[i] = Math.max(rgb[i], 0);
		}
		return new Color(rgb[0], rgb[1], rgb[2]);
	}

	/**
	 * Description: A JTable with alternating row background colors. half the
	 * rows are the component's natural background color, and the others are
	 * either darkened or lightened versions of that color depending on the
	 * maximum color component.
	 */
	public class ZebraTable extends JTable {
		public ZebraTable() {
			super();
		}

		public ZebraTable(TableModel model) {
			super(model);
		}

		private final Color altered = StaticUtils
				.slightlyDifferentColor(getBackground());

		public Component prepareRenderer(TableCellRenderer renderer, int row,
				int col) {
			Component c = super.prepareRenderer(renderer, row, col);
			Color bg = getBackground();
			c.setBackground(row % 2 == 0 && !isCellSelected(row, col) ? altered
					: bg);
			if (isCellSelected(row, col))
				c.setBackground(getSelectionBackground());
			return c;
		}
	}

	/**
	 * Used to persist tab selection changes as user preferences. This must only
	 * be called <i>after</i> all the tabs have been added otherwise internal
	 * calls to setSelectedTab will cause the user preferences to be
	 * overwritten.
	 * 
	 * @param tabs
	 *            the component to initialize and track.
	 * @param key
	 *            name of the property to set/get.
	 */
	public static void manageTabSelections(final JTabbedPane tabs,
			final String key) {
		// First, set initially selected tab if any.
		String lastSelected = PropertyManager.top.getProperty(key);
		if (lastSelected != null) {
			int i = tabs.indexOfTab(lastSelected);
			if (i >= 0)
				tabs.setSelectedIndex(i);
		}
		// Now, add change listener that tracks user selections.
		// This must be done *after* making the initial selection so as not to
		// pick up that event.
		tabs.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (!tabs.isShowing())
					return;
				String ss = tabs.getTitleAt(tabs.getSelectedIndex());
				PropertyManager.userprefs.setProperty(key, ss);
			}
		});
	}

	/**
	 * Selection utility in the style of the JOptionPane.showXxxDialog methods.
	 * Given a JTree, presents an option dialog presenting the tree allowing
	 * users to select a node.
	 * 
	 * @param tree
	 *            is the tree to display
	 * @param parent
	 *            is the component to anchor the diaglog to
	 * @return the path of the selected tree node or null if cancelled.
	 */
	public static TreePath showTreeNodeChooser(JTree tree, String title,
			Component parent) {
		final String OK = "OK", CANCEL = "Cancel";
		final JButton ok_butt = new JButton(OK), cancel_butt = new JButton(
				CANCEL);
		final TreePath selected[] = new TreePath[] { tree
				.getLeadSelectionPath() }; // only an array so it can be final,
											// yet mutable
		ok_butt.setEnabled(selected[0] != null);
		final JOptionPane option_pane = new JOptionPane(new JScrollPane(tree),
				JOptionPane.QUESTION_MESSAGE, JOptionPane.DEFAULT_OPTION, null,
				new Object[] { ok_butt, cancel_butt });
		ok_butt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				option_pane.setValue(OK);
			}
		});
		cancel_butt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				option_pane.setValue(CANCEL);
				selected[0] = null;
			}
		});
		TreeSelectionListener tsl = new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				selected[0] = e.getNewLeadSelectionPath();
				ok_butt.setEnabled(selected[0] != null);
			}
		};
		JDialog dialog = option_pane.createDialog(parent, title);
		tree.addTreeSelectionListener(tsl); // to start monitoring user tree
											// selections
		dialog.setVisible(true); // present modal tree dialog to user
		tree.removeTreeSelectionListener(tsl); // don't want to clutter caller's
												// tree with listeners
		return OK.equals(option_pane.getValue()) ? selected[0] : null;
	}

	/**
	 * Converts an IP address into a numeric value suitable for sorting via
	 * comparators.
	 */
	public static long ip2long(InetAddress ip) {
		byte[] addr = ip.getAddress();
		long val = 0;
		for (int i = 0; i < addr.length; i++) {
			long byteval = (0x000000FF & ((int) addr[addr.length - i - 1]));
			val |= ((byteval << 8 * i) & ~0L << 8 * i);
		}
		return val;
	}

	/**
	 * Presents a warning message to the user in a modal dialog along with a
	 * standard "don't show this again" checkbox. Subsequent calls will simply
	 * do nothing any time after the user checks the check box and closes the
	 * dialog.
	 * 
	 * @param parent
	 *            is a parent component to attach to.
	 * @param skipKey
	 *            is a string key used to find in the PropertyManager whether to
	 *            skip showing this message. Also used as the user preference
	 *            key to set when/if the user ever checks the check box.
	 * @param notice
	 *            is the warning message to present.
	 */
	public static void conditionalWarning(final String notice,
			final String skipKey, Component parent) {
		class NotifyPanel extends JPanel {
			public NotifyPanel() {
				final JCheckBox enough = new JCheckBox(
						"Don't show this message again",
						PropertyManager.getBoolean(skipKey, false));
				enough.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						PropertyManager.userprefs.setProperty(skipKey, ""
								+ enough.isSelected()); // save pref change.
					}
				});
				setLayout(new BorderLayout());
				add(new JLabel("<html><font size=+1>" + notice
						+ "</font></html>"), BorderLayout.CENTER);
				add(enough, BorderLayout.SOUTH);
			}
		}
		if (!PropertyManager.getBoolean(skipKey, false))
			JOptionPane.showMessageDialog(parent, new NotifyPanel(), "Warning",
					JOptionPane.WARNING_MESSAGE);
	}

	/**
	 * Same as 3-argument version but uses the active window as the parent.
	 */
	public static void conditionalWarning(final String notice,
			final String skipKey) {
		conditionalWarning(notice, skipKey, getActiveWindow());
	}

	/**
	 * A mouse listener that when added to JTables, JTrees, or JLists, causes
	 * right-clicks on rows or nodes to select them just like a left click
	 * would. For more information on this issue see:
	 * http://forums.java.net/jive/thread.jspa?messageID=107674
	 */
	public static class RightClickSelector extends MouseAdapter {
		public void mousePressed(MouseEvent e) {
			if (e.getButton() != MouseEvent.BUTTON3)
				return;
			Object src = e.getSource();
			if (src instanceof JTree) {
				JTree tree = (JTree) src;
				int clickedRow = tree.getRowForLocation(e.getX(), e.getY());
				if (!tree.isRowSelected(clickedRow))
					tree.setSelectionRow(clickedRow);
			} else if (src instanceof JTable) {
				JTable table = (JTable) src;
				int clickedRow = table.rowAtPoint(e.getPoint());
				if (!table.isRowSelected(clickedRow))
					table.getSelectionModel().setSelectionInterval(clickedRow,
							clickedRow);
			} else if (src instanceof JList) {
				JList list = (JList) src;
				int clickedRow = list.locationToIndex(e.getPoint());
				if (!list.isSelectedIndex(clickedRow))
					list.setSelectedIndex(clickedRow);
			}
		}
	}

	public static void main(String[] args) {
		conditionalWarning("You haven't yet chosen to ignore this warning.",
				"showtestwarning");
	}
}
