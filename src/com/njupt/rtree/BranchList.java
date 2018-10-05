package com.njupt.rtree;

import com.njupt.rtree.RTNode;

public class BranchList 
{
	public RTNode node;
	
	/**
	 * 点Point到node所包围的Rectangle的最小最大距离
	 */
	public float minMaxDist;

	/**
	 * 点Point到node所包围的Rectangle的最小距离
	 */
	public float minDist;
	
//	public BranchList(){
//		
//	}

	public BranchList(RTNode node, float minDist, float minMaxDist) 
	{
		this.node = node;
		this.minDist = minDist;
		this.minMaxDist = minMaxDist;
	}
	
	
}
