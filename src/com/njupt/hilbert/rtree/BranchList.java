package com.njupt.hilbert.rtree;

import com.njupt.hilbert.rtree.HilbertRTNode;

public class BranchList 
{
	public HilbertRTNode node;
	
	/**
	 * 点Point到node所包围的Rectangle的最小最大距离
	 */
	public float minMaxDist;

	/**
	 * 点Point到node所包围的Rectangle的最小距离
	 */
	public float minDist;
	
	public BranchList(HilbertRTNode node, float minDist, float minMaxDist) 
	{
		this.node = node;
		this.minDist = minDist;
		this.minMaxDist = minMaxDist;
	}
}
