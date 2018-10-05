package com.njupt.btree;

public class TestBTreeCLU {
	public static void main(String[] args) throws Exception {
		long startTime = System.currentTimeMillis();
		BTreeClu.run(args);
		System.out.println(BTreeClu.getBTree());
		System.out.println("take " + (System.currentTimeMillis() - startTime) + " msec");
		System.exit(0);
	}
}
