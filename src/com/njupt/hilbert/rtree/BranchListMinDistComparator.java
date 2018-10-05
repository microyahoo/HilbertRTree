package com.njupt.hilbert.rtree;

import java.util.Comparator;

public class BranchListMinDistComparator implements Comparator<BranchList>
{

	@Override
	public int compare(BranchList o1, BranchList o2) 
	{
		float f = o1.minDist - o2.minDist;
		if(f > 0)
			return 1;
		else if(f < 0)
			return -1;
		return 0;
	}

}
