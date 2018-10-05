package com.njupt.algorithm;

/******************************************************************************
 * Compilation: javac Hilbert.java Execution: java Hilbert N Dependencies:
 * StdDraw.java
 * 
 * Plot an order N Hilbert curve using two mutually recursive functions.
 * 
 * % java Hilbert 5
 * 
 * 
 *  The Hilbert Curve is based on the simple recursive pattern:
 *    ____       _   _       _   _
 *   |    |     | |_| |     | | | |
 *   |    | --> |_   _|  ~   _   _  
 *   |    |      _| |_       _| |_
 *   
 ******************************************************************************/

public class Hilbert {
	private Turtle turtle;

	public Hilbert(int N) {
		turtle = new Turtle(0.5, 0.5, 0.0);
		double max = Math.pow(2, N);
		turtle.setXscale(0, max);
		turtle.setYscale(0, max);
		hilbert(N);
	}

	// Hilbert curve
	private void hilbert(int n) {
		if (n == 0)
			return;
		turtle.turnLeft(90);
		treblih(n - 1);
		turtle.goForward(1.0);
		turtle.turnLeft(-90);
		hilbert(n - 1);
		turtle.goForward(1.0);
		hilbert(n - 1);
		turtle.turnLeft(-90);
		turtle.goForward(1.0);
		treblih(n - 1);
		turtle.turnLeft(90);
	}

	// evruc trebliH
	public void treblih(int n) {
		if (n == 0)
			return;
		turtle.turnLeft(-90);
		hilbert(n - 1);
		turtle.goForward(1.0);
		turtle.turnLeft(90);
		treblih(n - 1);
		turtle.goForward(1.0);
		treblih(n - 1);
		turtle.turnLeft(90);
		turtle.goForward(1.0);
		hilbert(n - 1);
		turtle.turnLeft(-90);
	}

	// plot a Hilber curve of order N
	public static void main(String[] args) {
		// int N = Integer.parseInt(args[0]);
		int N = 5;
		new Hilbert(N);
	}
}
