package com.njupt.rtree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Test2 
{
	public static void main(String[] args) throws Exception 
	{
		//LB数据集格式如下：
		//ID lx ly hx hy
		CachedPersistentPageFile file = new CachedPersistentPageFile("d:/RTree_work/RTree.dat", 10);
		RTree tree = new RTree(2, 0.4f, 5, file, Constants.RTREE_QUADRATIC);
//		RTree tree = new RTree(2, 0.4f, 5, Constants.RTREE_QUADRATIC, 2);
		BufferedReader reader = new BufferedReader(new FileReader(new File("d:\\RTree_work\\LB.txt")));
		String line ;
		int j = 0;
		while((line = reader.readLine()) != null)
		{
			String[] splits = line.split(" ");
			float lx = Float.parseFloat(splits[1]);
			float ly = Float.parseFloat(splits[2]);
			float hx = Float.parseFloat(splits[3]);
			float hy = Float.parseFloat(splits[4]);
			
			Point p1 = new Point(new float[]{ lx, ly });
			Point p2 = new Point(new float[]{ hx, hy });
			
			final Rectangle rectangle = new Rectangle(p1, p2);
			
			System.out.println("insert " + j + "th " + rectangle + "......");
			tree.insert(rectangle, -2);
			j++;
		}
		
		System.out.println(tree.file.readNode(0));
		
		
		//删除结点
//		System.out.println("---------------------------------");
//		System.out.println("Begin delete.");
//		
//		reader = new BufferedReader(new FileReader(new File("d:\\RTree_work\\LB.txt")));
//		while((line = reader.readLine()) != null)
//		{
//			String[] splits = line.split(" ");
//			float lx = Float.parseFloat(splits[1]);
//			float ly = Float.parseFloat(splits[2]);
//			float hx = Float.parseFloat(splits[3]);
//			float hy = Float.parseFloat(splits[4]);
//			
//			Point p1 = new Point(new float[]{lx,ly});
//			Point p2 = new Point(new float[]{hx,hy});
//			
//			final Rectangle rectangle = new Rectangle(p1, p2);
//			tree.delete(rectangle);
//		}
//		
//		reader.close();
//		System.out.println(tree.file.readNode(0));
//		System.out.println("---------------------------------");
//		System.out.println("Delete finished.");
	}
}
