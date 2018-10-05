package com.njupt.utils;

// StackMethodExtractor.java

import java.io.*;

/**
 * Utility class with one public static method which returns the string name of
 * of a caller in the stack a given number of calls above the caller of that method.
 * It is very useful as a debugging aid. Many people like to use print statements as a
 * debugging aid. I generally don't, but on occasion it's useful to use a debugging
 * print statement as an initial stub implementation for a set of class or interface
 * methods. For example: <br><pre>
 *
 *     class MyClass implements SomeInterface {
 *         public void method1 {
 *             System.out.println("method1 called");
 *         }
 *         public void method2 {
 *             System.out.println("method2 called");
 *         }
 *         public void method3 {
 *             System.out.println("method2 called");
 *         }
 *         public void methodN {
 *             System.out.println("methodN called");
 *         }
 *     }
 * </pre></br>
 *
 * Pretty tedious, right? It's also error prone. Notice the deliberate cut-n-paste
 * error where interfaceImplemntation3 wrongly reports that implementation2 was called.
 * The solution using StackMethodExtractor is simply this: <br><pre>
 *
 *     class MyClass implements SomeInterface {
 *         public void method1 {
 *             System.out.println(StackMethodExtractor.getCaller(0) + " called");
 *         }
 *         public void method2 {
 *             System.out.println(StackMethodExtractor.getCaller(0) + " called");
 *         }
 *         public void method3 {
 *             System.out.println(StackMethodExtractor.getCaller(0) + " called");
 *         }
 *         public void methodN {
 *             System.out.println(StackMethodExtractor.getCaller(0) + " called");
 *         }
 *     }
 * </pre></br>
 * 
 * This way all the initial implementations are identical. Easy to generate; totally
 * foolproof. Calling with an offset greater than zero returns method names that many
 * stack frames above your calling method. A somewhat more elegant means for seeing
 * where your code is being called from than <code>Thread.dumpStack()</code>.
 *
 * This is a refactored implementation of a more complicated utility by Ashutosh Marhari
 * published in Java Developer's Journal November 2001 volume 6 issue 11 page 56.
 * Note: the original source was supposed to be found at 
 * http://www.sys-con.com/java/source2.cfm?volume=06&issue=11
 * but seems to be missing. Ashutosh's email address is ashutosh@nahari.com
 *
 * @author Melinda Green - http://www.superliminal.com
 */
public class StackMethodExtractor {

    /**
     * A utility method which extracts the full name of a method on the stack
     * above the calling method.
     * @param callerID is the index of the method that many stack frames above the
     * calling method. The value 0 returns the name of the calling method itself, 
     * 1 returns its immediate caller, 2 the caller of that caller, etc.
     * @return full name of the method calling this one a given number of stack
     * frames above that one.
     */
    public static String getCaller(int callerID) {
        int stack_base = callerID + 2; // +1 to ignore "Thorwable" line, +1 to ignore this method
        StringWriter sw = new StringWriter();
        (new Throwable()).printStackTrace(new PrintWriter(sw));
        String trace = sw.toString();
        int linestart = -1;
        for(int i=0; i<stack_base; i++)
            linestart = trace.indexOf("\n", linestart+1);
        return trace.substring(linestart+5, trace.indexOf("(", linestart+5));
    }

    private static int countChars(char c, String str) {
        int count = 0;
        for(int i=0; i<str.length(); i++)
            if(str.charAt(i) == c)
                count++;
        return count;    
    }

    /**
     * A simple example using StackMethodExtractor to print the name of the
     * VM method which is calling main.
     */
    public static void main(String args[]) {
        System.out.println("main was called from: " + StackMethodExtractor.getCaller(1));
    }
    
}

