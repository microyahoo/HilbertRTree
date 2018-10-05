package com.njupt.rtree;

import java.io.Serializable;

/**
 * @ClassName Point 
 * @Description n维空间中的点，所有的维度被存储在一个float数组中
 */
public class Point implements Cloneable, Serializable
{
	private static final long serialVersionUID = 3810775231553230680L;
	
	private float[] data;
	
	public Point(float[] data)
	{
		if(data == null)
		{
			throw new IllegalArgumentException("Coordinates cannot be null.");
		}
		if(data.length < 2)
		{
			throw new IllegalArgumentException("Point dimension should be greater than 1.");
		}
		
		this.data = new float[data.length];
		System.arraycopy(data, 0, this.data, 0, data.length);
	}
	
	public Point(int[] data)
	{
		if(data == null)
		{
			throw new IllegalArgumentException("Coordinates cannot be null.");
		}
		if(data.length < 2)
		{
			throw new IllegalArgumentException("Point dimension should be greater than 1.");
		}
		
		this.data = new float[data.length];
		for(int i = 0 ; i < data.length ; i ++)
		{
			this.data[i] = data[i];
		}
	}
	
	@Override
	public Object clone()
	{
		float[] copy = new float[data.length];
		System.arraycopy(data, 0, copy, 0, data.length);
		return new Point(copy);
	}
	
	@Override
	public String toString() 
	{
		StringBuffer sBuffer = new StringBuffer("(");
		
		for(int i = 0 ; i < data.length - 1 ; i ++)
		{
			sBuffer.append(data[i]).append(",");
		}
		
		sBuffer.append(data[data.length - 1]).append(")");
		
		return sBuffer.toString();
	}
	
	public static void main(String[] args) 
	{
		float[] test = {1.2f,2f,34f};
		Point point1 = new Point(test);
		System.out.println(point1);
		
		int[] test2 = {1,2,3,4};
		point1 = new Point(test2);
		System.out.println(point1);
	}

	/**
	 * @return 返回Point的维度
	 */
	public int getDimension() 
	{
		return data.length;
	}

	/**
	 * @param index
	 * @return 返回Point坐标第i位的float值
	 */
	public float getFloatCoordinate(int index) 
	{
		return data[index];
	}
	
	/**
	 * @param index
	 * @return 返回Point坐标第i位的int值
	 */
	public int getIntCoordinate(int index)
	{
		return (int) data[index];
	}
	
	@Override
	public boolean equals(Object obj) 
	{
		if(obj instanceof Point)
		{
			Point point = (Point) obj;
			
			if(point.getDimension() != getDimension())
				throw new IllegalArgumentException("Points must be of equal dimensions to be compared.");
			
			for(int i = 0; i < getDimension(); i ++)
			{
				if(getFloatCoordinate(i) != point.getFloatCoordinate(i))
					return false;
			}
		}
		
		if(! (obj instanceof Point))
			return false;
		
		return true;
	}
}
