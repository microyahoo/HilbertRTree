package com.njupt.utils;

import java.io.*;
import java.net.*;

/**
 * Title:      Safe Object Streams
 * Copyright:   Copyright (c) 2004
 * Company:  Superliminal Software
 * @author Melinda Green
 * @version 1.0
 *
 * Description:
 * A helper object that safely creates pairs of ObjectInput/OutputStream
 * objects. This requires a tricky bit of client/server handshaking
 * to set up. The purpose of this class is to make this a safe and easy process.
 * Note: when used on one end of a Socket, the other end must also.
 *
 * The reason this is tricky is
 * because threads will easily deadlock whenever an ObjectInputStream is constructed
 * before the corresponding ObjectOutputStream is created at the other end
 * of the socket. one difficult question is how the client and server can signal
 * each other over their shared socket that they've created their
 * ObjectOutputStreams without corrupting the socket's ability to send objects.
 * That is handled that by using PushbackInputStreams to perform blocking reads
 * to wait for the other's signal, and then unreading the data and then using
 * the pristine PushbackInputStream as the ObjectInputStream's data source.
 */
public class SOS {
    private ObjectOutputStream m_oos;
    private InputStream m_is;
    private ObjectInputStream m_ois=null;

    /**
     * Creates a Safe Object Streams object.
     * @param soc one end of a socket to transmit objects.
     * note: the other end of the socket must be similarly treated.
     */
    public SOS(Socket soc) {
        try {
            OutputStream os = soc.getOutputStream();
            m_oos = new ObjectOutputStream(os);
            m_oos.flush();
            InputStream is = soc.getInputStream();
            PushbackInputStream pis = new PushbackInputStream(is);
            int got = pis.read(); // blocking read in order to wait until ok-to-read signal received from other end
            pis.unread(got);
            m_is = pis;
        }
        catch(Exception e) {
            System.err.println("SOS exception " + e);
        }
        // should now be safe to construct ObjectInputStream without risk of deadlock
    }

    public ObjectOutputStream getOutputStream() {
        return m_oos;
    }

    public ObjectInputStream getInputStream() {
        // it must now be safe to construct object input stream because constructor must have
        // unblocked after it connected to the other end.
        if(m_ois != null)
            return m_ois;
        try {
            m_ois = new ObjectInputStream(m_is);
        }
        catch(Exception e) {
            System.err.println("SOS exception " + e);
        }
        return m_ois;
    }

    //
    // EVERYTHING FROM HERE DOWN IS TEST CODE
    //

    private static final int TEST_PORT = 7777;

    private static void startServer() {
        try {
            final ServerSocket ss = new ServerSocket(TEST_PORT);
            new Thread() {
                public void run() {
                    while(true) {
                        try {
                            SOS sos = new SOS(ss.accept());
                            ObjectInputStream ois = sos.getInputStream();
                            Object obj = ois.readObject();
                            System.out.println("SOS test server read obj from SOS: " + obj);
                        }
                        catch(Exception e) {
                            System.err.println("server exception " + e);
                        }
                    }
                }
            }.start();
        }
        catch(Exception e) {
            System.err.println("server exception " + e);
        }
    }

    /**
     * the simplest possible example client/server program that tests SOS objects.
     */
    public static void main(String args[]) {
        Object to_send = "THIS IS A MESSAGE FROM CLIENT TO SERVER"; // could be any serializable object
        try {
            startServer();
            Socket s = new Socket("localhost", TEST_PORT);
            System.out.println("cliet writing to SOS: " + to_send);
            SOS sos = new SOS(s);
            sos.getOutputStream().writeObject(to_send);
        }
        catch(Exception e) {
            System.err.println("client exception " + e);
        }
    }
}

