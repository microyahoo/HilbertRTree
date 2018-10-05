//package com.njupt.hilbert.rtree;
///* hilbert.java -- hilbert curve
// *
// * Author: Eric Laroche
// *
// *   This program is distributed in the hope that it will be useful,
// *   but WITHOUT ANY WARRANTY; without even the implied warranty of
// *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
// *
// * The Hilbert Curve is based on the simple recursive pattern:
// *    ____       _   _       _   _
// *   |    |     | |_| |     | | | |
// *   |    | --> |_   _|  ~   _   _  
// *   |    |      _| |_       _| |_
// */
//
//import java.applet.*;
//import java.awt.*;
//
///** hilbert -- hilbert curve.
// * The Hilbert Curve is based on a simple recursive pattern.
// * The turtle class is used to draw the curve.
// *
// * @author Eric Laroche
// * @version @(#) $Id: hilbert.java,v 1.2 1997/04/02 22:08:38 laroche Exp $
// */
//public class hilbert extends Applet implements Runnable
//{
//	/** version string */
//	private static final String version =
//		"@(#) $Id: hilbert.java,v 1.2 1997/04/02 22:08:38 laroche Exp $";
//	/** applet usage strings */
//	private static final String[][] usage = {
//		{ "depth", "int", "(maximal) recursion depth" },
//		{ "delay", "long", "delay in milliseconds" } };
//
//	/** maximal recursion depth and default value */
//	private int maxdepth = 4;
//	/** current recursion depth */
//	private int depth = 0;
//	/** paint delay in milliseconds and default value */
//	private long delay = 2000;
//	/** applet thread */
//	private Thread t = null;
//
//	/** main -- entry point for standalone application
//	 */
//	public static void main( String[] args )
//	{
//		System.err.println(
//			"Sorry, you must run the thing as applet." );
//	}
//
//	/** init -- called after the constructor and before start().
//	 * init sets the background color and reads in the applet parameters.
//	 */
//	public void init( )
//	{
//		setBackground( Color.white );
//		String parm = null;
//		parm = getParameter( "depth" );
//		if ( parm != null )
//		{
//			maxdepth = Integer.valueOf( parm ).intValue( );
//			if ( maxdepth < 0 )
//				throw new IllegalArgumentException( );
//			depth = maxdepth;
//			/* we don't cycle if depth is given and delay isn't */
//			delay = 0;
//		}
//		parm = getParameter( "delay" );
//		if ( parm != null )
//		{
//			delay = Long.valueOf( parm ).longValue( );
//			if ( delay < 0 )
//				throw new IllegalArgumentException( );
//			depth = 0;
//		}
//	}
//
//	/** start -- called by the browser
//	 */
//	public void start( )
//	{
//		if ( delay == 0 )
//			return;
//		if ( t == null )
//		{
//			t = new Thread( this );
//			t.start( );
//		}
//	}
//
//	/** stop -- called by the browser
//	 */
//	public void stop( )
//	{
//		if ( t != null )
//		{
//			t.stop( );
//			t = null;
//		}
//	}
//
//	/** getAppletInfo -- called by the browser.
//	 * getAppletInfo returns info on this applet.
//	 */
//	public String getAppletInfo( )
//	{
//		return version;
//	}
//
//	/** getParameterInfo -- called by the browser
//	 * getParameterInfo returns info on the parameters of this applet.
//	 */
//	public String[][] getParameterInfo( )
//	{
//		return usage;
//	}
//
//	/** run -- thread action, called by start()
//	 */
//	public void run( )
//	{
//		while ( t != null )
//		{
//			repaint( );
//			try {
//				Thread.sleep( delay ); }
//			catch ( InterruptedException e ) {
//				break;
//			}
//
//			depth++;
//			if ( depth > maxdepth )
//				depth = 0;
//		}
//	}
//
//	/** paint -- paint the applet component.
//	 * A new turtle is created and positioned so that the hilbert curve
//	 * gets centered.
//	 */
//	public void paint( Graphics g )
//	{
//		Dimension d = size( );
//		turtle t = new turtle( g, d );
//
//		int len = Math.min( d.width, d.height ) - 1;
//		/* need 1 part for depth 0, 3 (==1*2+1) for 1, 7 for 2, etc. */
//		int partsneeded = ( 1 << depth + 1 ) - 1;
//		int step = len / partsneeded;
//		if ( step == 0 )
//			return;
//		/* center the curve */
//		Point offset = new Point(
//			Math.max( ( d.width - 1 - step * partsneeded ) / 2, 0 ),
//			Math.max( ( d.height - 1 - step * partsneeded ) / 2, 0 ) );
//		t.setlocation( offset );
//
//		curve( t, depth, step, false );
//	}
//
//	/** curve -- recursive algorithm to draw a hilbert curve.
//	 * No pattern is used to produce the recursion.
//	 */
//	private static void curve( turtle t,
//		int depth, int step, boolean left )
//	{
//		depth--;
//		t.turnleft( ! left );
//		if ( depth >= 0 )
//			curve( t, depth, step, ! left );
//		t.advance( step );
//		t.turnleft( left );
//		if ( depth >= 0 )
//			curve( t, depth, step, left );
//		t.advance( step );
//		if ( depth >= 0 )
//			curve( t, depth, step, left );
//		t.turnleft( left );
//		t.advance( step );
//		if ( depth >= 0 )
//			curve( t, depth, step, ! left );
//		t.turnleft( ! left );
//	}
//}
