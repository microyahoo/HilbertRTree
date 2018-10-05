package com.njupt.algorithm;
/******************************************************************************
 *  Compilation:  javac SingleHilbert.java
 *  Execution:    java SingleHilbert N
 *  Dependencies: StdDraw.java
 *  
 *  Plot an order N Hilbert curve using a singly recursive function.
 *
 *  % java SingleHilbert 5
 *
 *
 ******************************************************************************/


public class SingleHilbert {
    private Turtle turtle;

    public SingleHilbert(int N) {
        turtle = new Turtle(0.5, 0.5, 0.0);
        double max = Math.pow(2, N);
        turtle.setXscale(0, max);
        turtle.setYscale(0, max);
        hilbert(N, +1);
    }

    public void hilbert(int n, int parity) {
        if (n == 0) return;
        turtle.turnLeft(parity * 90);
        hilbert(n-1, -parity);
        turtle.goForward(1.0);
        turtle.turnLeft(-parity * 90);
        hilbert(n-1, +parity);
        turtle.goForward(1.0);
        hilbert(n-1, +parity);
        turtle.turnLeft(-parity * 90);
        turtle.goForward(1.0);
        hilbert(n-1, -parity);
        turtle.turnLeft(parity * 90);
    }
    
    public static void main(String[] args) {
//        int N = Integer.parseInt(args[0]);
    	int N = 6;
        new SingleHilbert(N);
    }
}
