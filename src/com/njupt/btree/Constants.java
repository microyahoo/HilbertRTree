package com.njupt.btree;

public class Constants 
{
	/**
	 * BTree的阶<br>
	 * BTree中关键字个数为[ceil(m/2)-1,m-1]	<br>
	 * BTree中子树个数为[ceil(m/2),m]
	 */
	public static final int BTREE_ORDER = 3;	
	
	/**
	 * 非根节点中最小的关键字个数
	 */
	public static final int MIN_KEY_SIZE = (int) (Math.ceil(Constants.BTREE_ORDER / 2.0) - 1);
	
	/**
	 * 非根节点中最大的关键字个数
	 */
	public static final int MAX_KEY_SIZE = Constants.BTREE_ORDER - 1;
	
	/**
	 * 每个结点中最小的孩子个数
	 */
	public static final int MIN_CHILDREN_SIZE = (int) (Math.ceil(Constants.BTREE_ORDER / 2.0));
	
	/**
	 * 每个结点中最大的孩子个数
	 */
	public static final int MAX_CHILDREN_SIZE = Constants.BTREE_ORDER ;
	

}
