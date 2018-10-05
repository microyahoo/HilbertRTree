package com.njupt.hilbert.rtree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

public class Test2 
{
	public static void main(String[] args) throws Exception 
	{
		//LB数据集格式如下：
		//ID lx ly hx hy
		CachedPersistentPageFile file = new CachedPersistentPageFile("d:/RTree_work/HilbertRTree.dat", 10);
//		HilbertRTree tree = new HilbertRTree(2, 0.4f, 5, file, Constants.RTREE_QUADRATIC, 2);
		HilbertRTree tree = new HilbertRTree(2, 0.4f, 5, Constants.RTREE_QUADRATIC, 2);
		BufferedReader reader = new BufferedReader(new FileReader(new File("d:\\RTree_work\\32000.txt")));
		String line ;
		int capacity = tree.getNodeCapacity();
		int j = 0;
		long startTime = System.currentTimeMillis();
		while((line = reader.readLine()) != null)
		{
			String[] splits = line.split(" ");
			float lx = Float.parseFloat(splits[1]);
			float ly = Float.parseFloat(splits[2]);
			float hx = Float.parseFloat(splits[3]);
			float hy = Float.parseFloat(splits[4]);
			
			Point p1 = new Point(new float[]{ lx, ly });
			Point p2 = new Point(new float[]{ hx, hy });
			
			int[] c = new int[p1.getDimension()];
			for (int i = 0; i < p1.getDimension(); i++) {
				c[i] = (int) ((p1.getFloatCoordinate(i) + p2.getFloatCoordinate(i) ) / 2);
			}
			
			long hilbertValue = HilbertMethod.getHilbertValue(c[0], c[1]);
			
			final Rectangle rectangle = new Rectangle(p1, p2, hilbertValue);
			
//			System.out.println("insert " + j + "th " + rectangle + "......");//暂时注释
			
			tree.insert(rectangle, Constants.NOPAGE, Constants.HILBERT);
//			j++;
//			if (j % capacity == 0) {
//				System.out.println(tree.file.readNode(0));
//			}
			
			
//			if ((j % cap == 0 && j <= 1000) || j == 34 || j == 35 || j == 36 || j == 37 || j == 38 || j == 39) {
//			if (j < 50) {//暂时注释
//				List<HilbertRTNode> nodes = tree.traverseByLevel();
//				System.out.println("=================================begin===========================================");
//				for (int x = 0; x < nodes.size(); x++) {
//					System.out.println(nodes.get(x));
//				}
//				System.out.println("=================================end===========================================\n");
//			}
			
//			if (j == 1000)
//				break;
		}
		
//		System.out.println(tree.file.readNode(0));
		System.out.println("take " + (System.currentTimeMillis() - startTime) + " msec");
		
		
//		//删除结点
//		System.out.println("---------------------------------");
//		System.out.println("Begin delete.");
//		
//		reader = new BufferedReader(new FileReader(new File("d:\\RTree_work\\LB.txt")));
//		while((line = reader.readLine()) != null && j > 0)
//		{
//			String[] splits = line.split(" ");
//			float lx = Float.parseFloat(splits[1]);
//			float ly = Float.parseFloat(splits[2]);
//			float hx = Float.parseFloat(splits[3]);
//			float hy = Float.parseFloat(splits[4]);
//			
//			Point p1 = new Point(new float[]{ lx, ly });
//			Point p2 = new Point(new float[]{ hx, hy });
//			
//			int[] c = new int[p1.getDimension()];
//			for (int i = 0; i < p1.getDimension(); i++) {
//				c[i] = (int) ((p1.getFloatCoordinate(i) + p2.getFloatCoordinate(i) ) / 2);
//			}
//			long hilbertValue = HilbertMethod.getHilbertValue(c[0], c[1]);
//			
//			final Rectangle rectangle = new Rectangle(p1, p2, hilbertValue);
//			Rectangle testRectangle = new Rectangle(new Point(new float[] { 4451.0f, 5129.0f }), 
//					new Point(new float[] { 4481.0f, 5129.0f }));
//			if (rectangle.equals(testRectangle))
//				System.out.println("the two rectangles are equal.");
//			System.out.println("delete " + j + "th " + rectangle + "......");
//			tree.delete(rectangle, Constants.HILBERT);
//			 
//			if (j < 50) {
//				List<HilbertRTNode> nodes = tree.traverseByLevel();
//				System.out.println("=================================delete begin===========================================");
//				for (int x = 0; x < nodes.size(); x++) {
//					System.out.println(nodes.get(x));
//				}
//				System.out.println("=================================delete end===========================================\n");
//			}
//			j--;
//		}
//		
//		reader.close();
//		System.out.println(tree.file.readNode(0));
//		System.out.println("---------------------------------");
//		System.out.println("Delete finished.");
	}
}
