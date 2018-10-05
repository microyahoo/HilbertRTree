package com.njupt.rtree;

public class Coordinate  {
	
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
	}
	
	public int[] getCoordinate() {
		return coordinate;
	}

	public int getDimension() {
		return dimension;
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
		
	}
	
}
