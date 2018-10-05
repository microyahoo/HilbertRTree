package com.njupt.cluster;
/**
 * 这个类用于计算距离的。。
 *
 */
public class Distance {
	int dest;// 目的
	int source;// 源
	double dist;// 欧式距离

	public int getDest() {
		return dest;
	}

	public void setDest(int dest) {
		this.dest = dest;
	}

	public int getSource() {
		return source;
	}

	public void setSource(int source) {
		this.source = source;
	}

	public double getDist() {
		return dist;
	}

	public void setDist(double dist) {
		this.dist = dist;
	}
	/**
	 * 计算源和目的的距离
	 * @param dest 目的武将
	 * @param source 源武将
	 * @param dist 两者间的距离
	 */
	public Distance(int dest, int source, double dist) {
		this.dest = dest;
		this.source = source;
		this.dist = dist;
	}

	public Distance() {
	}

}
