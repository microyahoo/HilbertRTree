package com.njupt.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * PasswordChanger is a simple dialog that allows users to enter their old password
 * and to reequest a new one. An abstract callback method is used to validate or reject
 * user entries.
 * Copyright:    Copyright (c) 2004
 * Company:      Superliminal Software
 *
 * @author Melinda Green
 *         Date: Aug 2, 2004
 */
public abstract class PasswordChanger extends JDialog {
    /**
     * Creates and displays a PasswordChanger object.
     * NOTE: Callers should not call setVisible() on these objects
     * and should throw away any instances once their acceptPasswordChange callback returns 'true'.
     * @param owner same as JDialog constuctor
     * @param uname user name used in title
     */
    public PasswordChanger(JFrame owner, String uname) {
        super(owner, "Changing Password For " + uname, true);
        JPanel typeins = new JPanel(new GridLayout(3, 2));
        final JPasswordField
            old  = new JPasswordField(14),
            try1 = new JPasswordField(14),
            try2 = new JPasswordField(14);
        typeins.add(new JLabel("Old Password: "));
        typeins.add(old);
        typeins.add(new JLabel("New Password: "));
        typeins.add(try1);
        typeins.add(new JLabel("Retype Password: "));
        typeins.add(try2);
        final JButton changeit = new JButton("Change Password");
        changeit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(acceptPasswordChange(new String(old.getPassword()), new String(try1.getPassword()))) {
                    JOptionPane.showMessageDialog(PasswordChanger.this, "Password Changed");
                    setVisible(false);
                }
                else
                    JOptionPane.showMessageDialog(PasswordChanger.this, "Invalid Password");
            }
        });
        // allow for <enter> key in text fields to click button when enabled
        KeyListener quickchange = new KeyAdapter() {
            public void keyTyped(KeyEvent ke) {
                if(ke.getKeyChar() == KeyEvent.VK_ENTER && changeit.isEnabled()) {
                    changeit.doClick();
                    changeit.requestFocus();
                }
            }
        };
        old.addKeyListener(quickchange);
        try1.addKeyListener(quickchange);
        try2.addKeyListener(quickchange);
        // enforce that the 'changeit' button is only enabled when valid data is ready.
        new ComponentDependencyHandler(old, try1, try2) {
            public void dependencyNotification() {
                String
                    op = new String(old.getPassword()),
                    t1  = new String(try1.getPassword()),
                    t2  = new String(try2.getPassword());
                changeit.setEnabled(t1.equalsIgnoreCase(t2) && !t1.equalsIgnoreCase(op));
            }
        };
        // create the final layout and show the dialog
        Container content = getContentPane();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(typeins);
        content.add(changeit);
        pack();
        if(owner != null)
            setLocation(owner.getX()+50, owner.getY()+50);
        setVisible(true);
    } // end PasswordChanger()

    /**
     * Implement this method to approve or reject a new password.
     * @param oldpswd what the user declares to be their current password.
     * @param newpswd user's equested new password.
     * @return true if old password entered matches current system value, and new password is acceptible.
     */
    protected abstract boolean acceptPasswordChange(String oldpswd, String newpswd);

    /**
     * a simple example main method that presents a PasswordChanger that accepts a new password of "pass"
     */
    public static void main(String[] args) {
        new PasswordChanger(null, "Joe") {
            protected boolean acceptPasswordChange(String oldpswd, String newpswd) {
                return "pass".equalsIgnoreCase(newpswd);
            }
        };
    }
}

