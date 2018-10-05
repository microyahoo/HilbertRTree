package com.njupt.rtree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class Test3 
{
	public static void main(String[] args) throws Exception 
	{
		//LB数据集格式如下：
		//ID lx ly hx hy
		CachedPersistentPageFile file = new CachedPersistentPageFile("D:\\RTree_work\\rtree.dat", 10);
		RTree tree = new RTree(2, 0.4f, 5, file, Constants.RTREE_QUADRATIC);
//		RTree tree = new RTree(2, 0.4f, 5, Constants.RTREE_QUADRATIC);
		BufferedReader reader = new BufferedReader(new FileReader(new File("D:\\RTree_work\\LB.txt")));
		String line ;
		int j = 0;
		
		Map<Integer, String> description = new HashMap<Integer, String>();
		File _20news = new File("D:\\RTree_work\\dataset\\20news");
		File[] listFiles = _20news.listFiles();
		for(int i = 0; i < listFiles.length; i ++)
		{
			description.put(i + 1, readFromFile(listFiles[i]));
		}
		
		int size = description.size();
		Random random = new Random(size);
		
		while((line = reader.readLine()) != null)
		{
			String[] splits = line.split(" ");
			float lx = Float.parseFloat(splits[1]);
			float ly = Float.parseFloat(splits[2]);
			float hx = Float.parseFloat(splits[3]);
			float hy = Float.parseFloat(splits[4]);
			
			Point p1 = new Point(new float[]{lx, ly});
			Point p2 = new Point(new float[]{hx, hy});
			String desc = description.get(random.nextInt(size));
			while(desc == null)
			{
				desc = description.get(random.nextInt(size));
			}
			
//			final Rectangle rectangle = new Rectangle(p1, p2);
			final Rectangle rectangle = new Rectangle(p1, p2, desc);
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
	
	public static String readFromFile(final File file) throws IOException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader
				(new FileInputStream(file)));
		StringBuilder sb = new StringBuilder();
		String line;
		while((line = reader.readLine()) != null)
		{
			sb.append(line).append("\n");
		}
		
		reader.close();
		
		return sb.toString();
	}
}
