package com.njupt.rtree;

import com.njupt.rtree.RTNode;
import com.njupt.rtree.Rectangle;

public interface INode 
{
	public RTNode getParent();
	public String getUniqueId();
	public int getLevel();
	public Rectangle getNodeRectangle();
	public boolean isLeaf();
    public boolean isRoot();
    public boolean isIndex();
}
