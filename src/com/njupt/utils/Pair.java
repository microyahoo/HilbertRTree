package com.njupt.utils;

/**
 * Container for two objects.
 * Similar to the Pair class in the C++ STL libraries. <br>
 *
 * Created Jul 17, 2006
 * @author Melinda Green
 */
public class Pair<T1, T2> {
    public T1 o1;
    public T2 o2;
    public Pair(T1 o1, T2 o2) { this.o1 = o1; this.o2 = o2; }

    public int hashCode() {
        int code = 0;
        if(o1 != null)
            code = o1.hashCode();
        if(o2 != null)
            code = code/2 + o2.hashCode()/2;
        return code;
    }

    public static boolean same(Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }

    public boolean equals(Object obj) {
        if( ! (obj instanceof Pair))
            return false;
        Pair p = (Pair)obj;
        return same(p.o1, this.o1) && same(p.o2, this.o2);
    }

    public String toString() {
        return "Pair{"+o1+", "+o2+"}";
    }

    /**
     * Simple example test program.
     */
    public static void main(String[] args) {
        Pair<String, String>
            p1 = new Pair<String, String>("a", "b"),
            p2 = new Pair<String, String>("a", null),
            p3 = new Pair<String, String>("a", "b"),
            p4 = new Pair<String, String>(null, null);
        System.out.println(p1.equals(new Pair<Integer, Integer>(1, 2)) + " should be false");
        System.out.println(p4.equals(p2) + " should be false");
        System.out.println(p2.equals(p4) + " should be false");
        System.out.println(p1.equals(p3) + " should be true");
        System.out.println(p4.equals(p4) + " should be true");
    }

}

