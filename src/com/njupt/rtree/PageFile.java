package com.njupt.rtree;

import org.apache.log4j.Logger;


/**
 * 每个结点必须存储在唯一的page，根节点总是存储在page 0.
 * 
 */
public abstract class PageFile {

	/**
	 * 一个结点能拥有的最多条目个数，即fan-out<br>
	 * branchingFactor的大小与disk block或file system page的大小一致
	 */
	private int branchingFactor;

	private static Logger logger = Logger.getLogger(PageFile.class);
	
	protected RTree tree = null;

	/**
	 * 维度
	 */
	protected int dimension = -1;

	/**
	 * 结点填充因子，0-0.5
	 */
	protected float fillFactor = -1;

	/**
	 * 结点容量
	 */
	protected int nodeCapacity = -1;

	/**
	 * 一个结点以字节来存储，计算公式如下： [nodeCapacity * (sizeof(Rectangle) + sizeof(Branch))]
	 * + parent + level + usedSpace = {nodeCapacity * [(2 * dimension *
	 * sizeof(float)) + sizeof(int)]} + sizeof(int) + sizeof(int) + sizeof(int)
	 * 
	 */
	protected int pageSize = -1;

	/**
	 * 树类型
	 */
	protected int treeType = -1;
	
	/**
	 * @param page
	 * @return 返回请求page中存储的node
	 * @throws PageFaultError
	 */
	protected abstract RTNode readNode(int page) throws PageFaultError;

	/**
	 * @param node
	 * @return 将node写入第一个可用的page中，并返回此page
	 * @throws PageFaultError
	 */
	protected abstract int writeNode(RTNode node) throws PageFaultError;

	/**
	 * @param page
	 * @return 标记指定的page为空
	 * @throws PageFaultError
	 */
	protected abstract RTNode deletePage(int page) throws PageFaultError;

	/**
	 * PageFile初始化，为其中的属性赋值
	 * 
	 * @param tree
	 * @param dimension
	 * @param fillFactor
	 * @param capacity
	 * @param treeType
	 */
	protected void initialize(RTree tree, int dimension, float fillFactor,
			int capacity, int treeType) {
		logger.info("initializing...");
		this.dimension = dimension;
		this.fillFactor = fillFactor;
		this.nodeCapacity = capacity;
		this.treeType = treeType;
		this.tree = tree;

		this.pageSize = capacity * (8 * dimension + 4) + 12;
	}

	protected void finalize() throws Throwable {
		super.finalize();
	}
}
