//******************************************************************************
//
// File:    Receive02.java
// Package: edu.rit.mp.test
// Unit:    Class edu.rit.mp.test.Receive02
//
// This Java source file is copyright (C) 2007 by Alan Kaminsky. All rights
// reserved. For further information, contact the author, Alan Kaminsky, at
// ark@cs.rit.edu.
//
// This Java source file is part of the Parallel Java Library ("PJ"). PJ is free
// software; you can redistribute it and/or modify it under the terms of the GNU
// General Public License as published by the Free Software Foundation; either
// version 3 of the License, or (at your option) any later version.
//
// PJ is distributed in the hope that it will be useful, but WITHOUT ANY
// WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
// A PARTICULAR PURPOSE. See the GNU General Public License for more details.
//
// Linking this library statically or dynamically with other modules is making a
// combined work based on this library. Thus, the terms and conditions of the
// GNU General Public License cover the whole combination.
//
// As a special exception, the copyright holders of this library give you
// permission to link this library with independent modules to produce an
// executable, regardless of the license terms of these independent modules, and
// to copy and distribute the resulting executable under terms of your choice,
// provided that you also meet, for each linked independent module, the terms
// and conditions of the license of that module. An independent module is a
// module which is not derived from or based on this library. If you modify this
// library, you may extend this exception to your version of the library, but
// you are not obligated to do so. If you do not wish to do so, delete this
// exception statement from your version.
//
// A copy of the GNU General Public License is provided in the file gpl.txt. You
// may also obtain a copy of the GNU General Public License on the World Wide
// Web at http://www.gnu.org/licenses/gpl.html.
//
//******************************************************************************

package com.njupt.hilbert.rtree;

import edu.rit.mp.ChannelGroup;
import edu.rit.mp.ObjectBuf;
import edu.rit.mp.Status;

import java.net.InetSocketAddress;

/**
 * Class Receive02 is a main program that receives MP messages from the
 * {@linkplain Send02} program. Each message consists of zero or more strings,
 * sent as serialized objects. The Receive02 program prints each message it
 * receives. The Receive02 program runs until killed externally.
 * <P>
 * Usage: java edu.rit.mp.test.Receive02 <I>tohost</I> <I>toport</I> <BR>
 * <I>tohost</I> = Host to which to send messages <BR>
 * <I>toport</I> = Port to which to send messages
 * 
 * @author Alan Kaminsky
 * @version 20-Dec-2007
 */
public class Receive02 {

	/**
	 * Prevent construction.
	 */
	private Receive02() {
	}

	/**
	 * Main routine.
	 */
	private void run(String[] args) throws Throwable {
		// Parse command line arguments.
		if (args.length != 2)
			usage();
		String tohost = args[0];
		int toport = Integer.parseInt(args[1]);

		// Set up item destination.
		Object[] objarray = new Object[1000];
		ObjectBuf dst = ObjectBuf.buffer(objarray);

		// Set up channel group.
		ChannelGroup channelgroup = new ChannelGroup(new InetSocketAddress(
				tohost, toport));
		channelgroup.startListening();

		// Receive messages.
		for (;;) {
			Status status = channelgroup.receive(null, null, dst);
			for (int i = 0; i < status.length; ++i) {
				if (i > 0)
					System.out.print(' ');
				System.out.print(objarray[i]);
			}
			System.out.println();
			System.out.println("Receive status:");
			System.out.println("Channel = " + status.channel);
			System.out.println("Tag     = " + status.tag);
			System.out.println("Length  = " + status.length);
			System.out.println();
		}
	}

	/**
	 * Main program.
	 */
	public static void main(String[] args) throws Throwable {
		new Receive02().run(args);
	}

	/**
	 * Print a usage message and exit.
	 */
	private static void usage() {
		System.err
				.println("Usage: java edu.rit.mp.test.Receive02 <tohost> <toport>");
		System.err.println("<tohost> = Host to which to send messages");
		System.err.println("<toport> = Port to which to send messages");
		System.exit(1);
	}

}
