package com.njupt.hilbert.rtree;

import java.io.Serializable;

public class Data implements Serializable, Comparable<Data>
{
	private static final long serialVersionUID = 7481061049260206108L;

	public Rectangle mbr;
	
	/**
	 * 父节点
	 */
	public HilbertRTNode parent;
	
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
	
	
	public Data(Rectangle mbr, HilbertRTNode parent, int position) 
	{
		this.mbr = mbr;
		this.parent = parent;
		this.position = position;
	}
	
	@Override
	public String toString() 
	{
		return mbr.toString() + " --> Position:" + position + " --> minDist:" + minDist + "\n";
	}

	@Override
	public int compareTo(Data o) {
		return Float.compare(minDist, o.minDist);
	}
}
