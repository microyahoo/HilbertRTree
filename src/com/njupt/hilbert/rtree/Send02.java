//******************************************************************************
//
// File:    Send02.java
// Package: edu.rit.mp.test
// Unit:    Class edu.rit.mp.test.Send02
//
// This Java source file is copyright (C) 2006 by Alan Kaminsky. All rights
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

import edu.rit.mp.Channel;
import edu.rit.mp.ChannelGroup;
import edu.rit.mp.ObjectBuf;

import edu.rit.util.Range;

import java.net.InetSocketAddress;

/**
 * Class Send02 is a main program that sends an MP message to the
 * {@linkplain Receive02} program. The message consists of zero or more strings
 * from the command line, sent as serialized objects.
 * <P>
 * Usage: java edu.rit.mp.test.Send02 <I>tohost</I> <I>toport</I> [
 * <I>string</I> . . . ] <BR>
 * <I>tohost</I> = Host to which to send messages <BR>
 * <I>toport</I> = Port to which to send messages <BR>
 * <I>string</I> = String to send (zero or more)
 * 
 * @author Alan Kaminsky
 * @version 09-Mar-2006
 */
public class Send02 {

	/**
	 * Prevent construction.
	 */
	private Send02() {
	}

	/**
	 * Main routine.
	 */
	private void run(String[] args) throws Throwable {
		// Parse command line arguments.
		if (args.length < 2)
			usage();
		String tohost = args[0];
		int toport = Integer.parseInt(args[1]);

		Point p1 = new Point(new float[] { 5965, 4744 });
		Point p2 = new Point(new float[] { 5966, 4879 });
		Rectangle rectangle = new Rectangle(p1, p2, 12345);
		// Set up item source.
		ObjectBuf<Rectangle> src = ObjectBuf.buffer(rectangle);
		// ObjectBuf.sliceBuffer (args, new Range (2, args.length-1));

		// Set up channel group.
		ChannelGroup channelgroup = new ChannelGroup();

		// Set up a connection to the far end.
		Channel channel = channelgroup.connect(new InetSocketAddress(tohost,
				toport));

		// Send message.
		channelgroup.send(channel, src);
	}

	/**
	 * Main program.
	 */
	public static void main(String[] args) throws Throwable {
		new Send02().run(args);
	}

	/**
	 * Print a usage message and exit.
	 */
	private static void usage() {
		System.err
				.println("Usage: java edu.rit.mp.test.Send02 <tohost> <toport> [ <string> ... ]");
		System.err.println("<tohost> = Host to which to send messages");
		System.err.println("<toport> = Port to which to send messages");
		System.err.println("<string> = String to send (zero or more)");
		System.exit(1);
	}

}
