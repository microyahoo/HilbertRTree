package com.njupt.hilbert.rtree;

import com.njupt.hilbert.rtree.HilbertRTNode;
import com.njupt.hilbert.rtree.Rectangle;

public interface INode 
{
	public HilbertRTNode getParent();
	public String getUniqueId();
	public int getLevel();
	public Rectangle getNodeRectangle();
	public boolean isLeaf();
    public boolean isRoot();
    public boolean isIndex();
}
