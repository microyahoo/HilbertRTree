package com.njupt.hilbert.rtree;

import java.io.Serializable;

/**
 * 外包矩形<p>
 * 实现Cloneable和Comparable接口，可以根据Hilbert值进行比较排序
 * @ClassName Rectangle 
 * @Description 
 */
public class Rectangle implements Cloneable, Comparable<Rectangle>, Serializable
{
	private static final long serialVersionUID = 4694528623116091746L;

	/**
	 * 左下角坐标
	 */
	private Point low;
	
	/**
	 * 右上角坐标
	 */
	private Point high;
	
	/**
	 * 坐标点的描述，即关键字
	 */
	private String description;
	
	/**
	 * Using the Hilbert value of the inserted data rectangle as the primary key.
	 */
	private long hilbertValue;
	
	public Rectangle(Point p1, Point p2)
	{
		if(p1 == null || p2 == null)
		{
			throw new IllegalArgumentException("Points cannot be null.");
		}
		if(p1.getDimension() != p2.getDimension())
		{
			throw new IllegalArgumentException("Points must be of same dimension.");
		}
		//先左下角后右上角
		for(int i = 0; i < p1.getDimension(); i ++)
		{
			if(p1.getFloatCoordinate(i) > p2.getFloatCoordinate(i))
			{
				throw new IllegalArgumentException("坐标点为先左下角后右上角");
			}
		}
		low = (Point) p1.clone();
		high = (Point) p2.clone();
		
	}
	
	public Rectangle(Point p1, Point p2, long hilbertValue) {
		this(p1, p2);
		this.hilbertValue = hilbertValue;
	}
	
	public Rectangle(Point p1, Point p2, String description)
	{
		this(p1, p2);
		this.description = description;
	}

	/**
	 * 返回Rectangle左下角的Point
	 * @return Point
	 */
	public Point getLow() 
	{
		return (Point) low.clone();
	}

	/**
	 * 返回Rectangle右上角的Point
	 * @return Point
	 */
	public Point getHigh() 
	{
		return high;
	}
	
	public void setHilbertValue(long hilbertValue) {
		this.hilbertValue = hilbertValue;
	}

	public long getHilbertValue() {
		return hilbertValue;
	}

	/**
	 * @param rectangle
	 * @return 包围两个Rectangle的最小Rectangle
	 */
	public Rectangle getUnionRectangle(Rectangle rectangle)
	{
		if(rectangle == null)
			throw new IllegalArgumentException("Rectangle cannot be null.");
		
		if(rectangle.getDimension() != getDimension())
		{
			throw new IllegalArgumentException("Rectangle must be of same dimension.");
		}
		
		float[] min = new float[getDimension()];
		float[] max = new float[getDimension()];
		
		for(int i = 0; i < getDimension(); i++)
		{
			min[i] = Math.min(low.getFloatCoordinate(i), rectangle.low.getFloatCoordinate(i));
			max[i] = Math.max(high.getFloatCoordinate(i), rectangle.high.getFloatCoordinate(i));
		}
		
		Rectangle ret = new Rectangle(new Point(min), new Point(max));
//		ret.hilbertValue = ((hilbertValue >= rectangle.hilbertValue) ? hilbertValue : rectangle.hilbertValue);
		
		return ret;
	}
	
	/**
	 * @return 返回Rectangle的面积
	 */
	public float getArea()
	{
		float area = 1;
		for(int i = 0; i < getDimension(); i ++)
		{
			area *= high.getFloatCoordinate(i) - low.getFloatCoordinate(i);
		}
		
		return area;
	}
	
	/**
	 * @param rectangles
	 * @return 包围一系列Rectangle的最小Rectangle
	 */
	public static Rectangle getUnionRectangle(Rectangle[] rectangles)
	{
		if(rectangles == null || rectangles.length == 0)
			throw new IllegalArgumentException("Rectangle array is empty.");
		
		Rectangle r0 = (Rectangle) rectangles[0].clone();
//		long lhv = r0.hilbertValue;
		for(int i = 1; i < rectangles.length; i ++)
		{
//			if (lhv < rectangles[i].hilbertValue)
//				lhv = rectangles[i].hilbertValue;
			r0 = r0.getUnionRectangle(rectangles[i]);
		}
		r0.hilbertValue = rectangles[rectangles.length - 1].hilbertValue;
		
		return r0;
	}
	
	@Override
	protected Object clone()
	{
		Point p1 = (Point) low.clone();
		Point p2 = (Point) high.clone();
		return new Rectangle(p1, p2, hilbertValue);
	}
	
	@Override
	public String toString() 
	{
//		if(description == null)
//			return "Rectangle Low:" + low + "\tHigh:" + high + "\tHilbert value: " + hilbertValue + "\n";
		return "Rectangle Low:" + low + "\tHigh:" + high + "\tHilbert value: " + hilbertValue /* + "\tCenter: " + getCenter() + "\n"*/;
//		return "Rectangle Low:" + low + " High:" + high + "\n" + description ;
	}
	
	public String toStr() {
		if(description == null)
			return "Rectangle Low:" + low + "\tHigh:" + high + "\tHilbert value: " + hilbertValue;
		return "Rectangle Low:" + low + "\tHigh:" + high + "\tHilbert value: " + hilbertValue + "\n" + description ;
	}

	/**
	 * 两个Rectangle相交的面积
	 * @param rectangle Rectangle
	 * @return float
	 */
	public float intersectingArea(Rectangle rectangle) 
	{
		if(! isIntersection(rectangle))
		{
			return 0;
		}
		
		float ret = 1;
		for(int i = 0; i < rectangle.getDimension(); i ++)
		{
			float l1 = this.low.getFloatCoordinate(i);
			float h1 = this.high.getFloatCoordinate(i);
			float l2 = rectangle.low.getFloatCoordinate(i);
			float h2 = rectangle.high.getFloatCoordinate(i);
			
			//rectangle1在rectangle2的左边
			if(l1 <= l2 && h1 <= h2)
			{
				ret *= (h1 - l1) - (l2 - l1);
			}else if(l1 >= l2 && h1 >= h2)
			//rectangle1在rectangle2的右边
			{
				ret *= (h2 - l2) - (l1 - l2);
			}else if(l1 >= l2 && h1 <= h2)			
			//rectangle1在rectangle2里面
			{
				ret *= h1 - l1;
			}else if(l1 <= l2 && h1 >= h2)	
			//rectangle1包含rectangle2
			{
				ret *= h2 - l2;
			}
		}
		return ret;
	}
	
	/**
	 * @param rectangle
	 * @return 判断两个Rectangle是否相交
	 */
	public boolean isIntersection(Rectangle rectangle)
	{
		if(rectangle == null)
			throw new IllegalArgumentException("Rectangle cannot be null.");
		
		if(rectangle.getDimension() != getDimension())
		{
			throw new IllegalArgumentException("Rectangle cannot be null.");
		}
		
		
		for(int i = 0; i < getDimension(); i ++)
		{
			if(low.getFloatCoordinate(i) > rectangle.high.getFloatCoordinate(i) ||
					high.getFloatCoordinate(i) < rectangle.low.getFloatCoordinate(i))
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * @return 返回Rectangle的维度
	 */
	public int getDimension() 
	{
		return low.getDimension();
	}

	/**
	 * 判断@rectangle是否被包围
	 * @param rectangle
	 * @return
	 */
	public boolean enclosure(Rectangle rectangle) 
	{
		if(rectangle == null)
			throw new IllegalArgumentException("Rectangle cannot be null.");
		
		if(rectangle.getDimension() != getDimension())
			throw new IllegalArgumentException("Rectangle dimension is different from current dimension.");
		
		for(int i = 0; i < getDimension(); i ++)
		{
			if(rectangle.low.getFloatCoordinate(i) < low.getFloatCoordinate(i) ||
					rectangle.high.getFloatCoordinate(i) > high.getFloatCoordinate(i))
				return false;
		}
		return true;
	}
	
	@Override
	public boolean equals(Object obj) 
	{
		if(obj instanceof Rectangle)
		{
			Rectangle rectangle = (Rectangle) obj;
			if(low.equals(rectangle.getLow()) && high.equals(rectangle.getHigh()))
				return true;
		}
		return false;
	}

	/**
	 * MINDIST距离<p>
	 * 1.查询点p在R内或R的边界上则MINDIST=0<br>
     * 2.查询点p在R外，若最短距离距离（p到R的边）存在，则MINDIST=p到R的边的最短距离， 否则，MINDIST=p到R的顶点的最短最短距离。<p>
	 * MINDIST(P,R):  the shortest distance from P to R
	 * @param point
	 * @return 返回Point到Rectangle的最小距离，即MINDIST距离
	 */
	public float getMinDist(Point point) 
	{
		if(point == null)
			throw new IllegalArgumentException("Point cannot be null.");
		if(point.getDimension() != getDimension())
			throw new IllegalArgumentException("Point dimension is different from Rectangle dimension.");
		
		float ret = 0;
		for(int i = 0; i < getDimension(); i ++)
		{
			float p = point.getFloatCoordinate(i);
			float l = low.getFloatCoordinate(i);
			float h = high.getFloatCoordinate(i);
			float r;
			
			if(p < l)
				r = l;
			else if(p > h)
				r = h;
			else 
				r = p;
			
			ret += Math.pow(Math.abs(p - r), 2);
		}
		
		return ret;
		
	}
	
	/**
	 * MINMAXDIST距离<p>
	 * 1.找出与第k轴垂直的并且离查询点p最近的面，记为H<br>
     * 2.选择从查询点p到面H中距离最远的那个点，记为a<br>
     * 3.计算查询点p到点a的距离,记为dk<br>
     * 4.对每个坐标轴重复步骤1-步骤3，记计算所得距离为d1，d2，...dk<br>
     * 5.从所有计算所得距离中选出最小的那一个，即MINMAXDIST<p>
	 * MINMAXDIST(P,R)：the minimum over all dimensions distance from P  to the furthest point of the closest face of the R<p>
	 * The important of MINMAXDIST(P,M) is that it computes the smallest distance between point P
	 * and MBR M that gurantees the finding of an object in M at a Enclidean distance less than or equal to MINMAXDIST(P,M).
	 * @param point
	 * @return 返回Point到Rectangle的最小最大距离，即MINMAXDIST距离
	 */
	public float getMinMaxDist(Point point)
	{
		if (point == null)
			throw new IllegalArgumentException("Point cannot be null.");
		if (point.getDimension() != getDimension())
			throw new IllegalArgumentException("Point dimension is different from Rectangle dimension.");
		
		float ret = Float.POSITIVE_INFINITY;
		for (int k = 0; k < getDimension(); k ++)
		{
			float p = point.getFloatCoordinate(k);
			float s = low.getFloatCoordinate(k);
			float t = high.getFloatCoordinate(k);
			float rm;
			
			if (p <= (s + t)/2.0)
				rm = s;
			else
				rm = t;
			
			float sum = 0;
			for (int i = 0; i < getDimension(); i ++)
			{
				if (i != k)
				{
					float p_ = point.getFloatCoordinate(i);
					float s_ = low.getFloatCoordinate(i);
					float t_ = high.getFloatCoordinate(i);
					
					float rM;
					
					if (p_ >= (s_ + t_) / 2.0)
						rM = s_;
					else
						rM = t_;
					
					sum += Math.pow(Math.abs(p_ - rM), 2);
				}
			}
			
			sum += Math.pow(Math.abs(p - rm), 2);
			
			if (sum < ret)
			{
				ret = sum;
			}
//			System.out.println("sum = " + sum);
			
		}
		
		return ret;
	}

	@Override
	public int compareTo(Rectangle o) {
		return Long.compare(hilbertValue, o.getHilbertValue());
	}
	
	public static void main(String[] args) 
	{
//		float[] f1 = {1.3f,2.4f};
//		float[] f2 = {3.4f,4.5f};
//		Point p1 = new Point(f1);
//		Point p2 = new Point(f2);
//		Rectangle rectangle = new Rectangle(p1, p2);
//		System.out.println(rectangle);
//		Point point = rectangle.getHigh();
//		point = p1;
//		System.out.println(rectangle);
		
//		float[] f_1 = {0f,0f};
//		float[] f_2 = {2f,2f};
//		float[] f_3 = {3f,3f};
//		float[] f_4 = {2.5f,2.5f};
//		float[] f_5 = {1.5f,1.5f};
//		p1 = new Point(f_1);
//		p2 = new Point(f_2);
//		Point p3 = new Point(f_3);
//		Point p4 = new Point(f_4);
//		Point p5 = new Point(f_5);
//		Rectangle re1 = new Rectangle(p1, p2);
//		Rectangle re2 = new Rectangle(p2, p3);
//		Rectangle re3 = new Rectangle(p4, p3);
//		Rectangle re4 = new Rectangle(p3, p4);
//		Rectangle re5 = new Rectangle(p5, p4);
//		System.out.println(re1.isIntersection(re2));
//		System.out.println(re1.isIntersection(re3));
//		System.out.println(re1.intersectingArea(re2));
//		System.out.println(re1.intersectingArea(re5));
		
//		System.out.println("****************************************");
//		Point point = new Point(new float[]{0,3,0});
//		Point po1 = new Point(new float[]{4,2,4});
//		Point po2 = new Point(new float[]{13,8,12});
//		Rectangle rec = new Rectangle(po1, po2);
//		System.out.println(rec.getMinDist(point));
//		System.out.println(rec.getMinMaxDist(point));
//		System.out.println("****************************************");
		
		int[] s = { 4, 2, 4 };
		int[] t = { 13, 8, 12 };
//		int[] p = { 0, 3, 0 };
		int[] p = { 8, 0, 0 };
		Point point = new Point(p);
		Rectangle rec = new Rectangle(new Point(s), new Point(t));
		System.out.println(rec.getMinDist(point));
		System.out.println(rec.getMinMaxDist(point));
		
	}

}
