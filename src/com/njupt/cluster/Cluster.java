package com.njupt.cluster;

import java.util.ArrayList;

public class Cluster {
	private int center;// 聚类中心武将的id
	private ArrayList<General> ofCluster = new ArrayList<General>();// 属于这个聚类的武将的集合

	public int getCenter() {
		return center;
	}

	public void setCenter(int center) {
		this.center = center;
	}

	public ArrayList<General> getOfCluster() {
		return ofCluster;
	}

	public void setOfCluster(ArrayList<General> ofCluster) {
		this.ofCluster = ofCluster;
	}

	public void addGeneral(General general) {
		if (!(this.ofCluster.contains(general)))
			this.ofCluster.add(general);
	}
}
