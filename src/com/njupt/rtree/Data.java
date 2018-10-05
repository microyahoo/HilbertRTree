package com.njupt.rtree;

public class Data 
{
	public Rectangle mbr;
	
	/**
	 * 父节点
	 */
	public RTNode parent;
	
	/**
	 * 在结点中的位置
	 */
	public int position;
	
	public float minDist;
	
	public Data(Rectangle mbr, float minDist, int position)
	{
		this.mbr = mbr;
		this.minDist = minDist;
		this.position = position;
	}
	
	public Data(Rectangle mbr, int position) 
	{
		this.mbr = mbr;
		this.position = position;
	}
	
	
	public Data(Rectangle mbr, RTNode parent, int position) 
	{
		this.mbr = mbr;
		this.parent = parent;
		this.position = position;
	}
	
	@Override
	public String toString() 
	{
		return mbr.toString() + "-->Position:" + position + " -->minDist:" + minDist + "\n";
	}
}
