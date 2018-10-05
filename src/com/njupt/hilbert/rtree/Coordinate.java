package com.njupt.hilbert.rtree;

public class Coordinate implements Comparable<Coordinate> {
	/**
	 * The Hilbert value of a rectangle is defined as the Hilbert value of its center.
	 */
	private long hilbertValue;
	
	//针对高维坐标点
	/**
	 * Rectangle中心点的坐标
	 */
	private int[] coordinate;
	
	/**
	 * 维度默认为2
	 */
	private int dimension = 2;
	
	/**
	 * 针对高维坐标点
	 * @param coord
	 * @param hilbert
	 */
	public Coordinate(int[] coord, long hilbert) {
		int dim = coord.length;
		if (dim < 2) {
			throw new RuntimeException("坐标维度小于2！");
		}
		coordinate = coord;
		this.dimension = dim;
		hilbertValue = hilbert;
	}
	
	public int[] getCoordinate() {
		return coordinate;
	}

	public int getDimension() {
		return dimension;
	}

	public long getHilbert() {
		return hilbertValue;
	}
	
	@Override
	public int compareTo(Coordinate o) {
		return Long.compare(this.hilbertValue, o.getHilbert());
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		for (int i = 0; i < dimension; i++) {
			sb.append(coordinate[i]);
			sb.append(", ");
		}
		int index = sb.lastIndexOf(", ");
		sb.replace(index, index + 2, "");
		sb.append(")");
		return sb.toString();
	}
	
	public static void main(String[] args) {
		Coordinate coordinate = new Coordinate(new int[] {1, 2, 3, 4,5, 6}, 
				HilbertMethod.getHilbertValue(1, 2));
		System.out.println(coordinate);
	}
	
}
