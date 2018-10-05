package com.njupt.hilbert.rtree;

//import org.apache.log4j.Logger;

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

//	private static Logger logger = Logger.getLogger(PageFile.class);
	
	protected HilbertRTree tree = null;

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
	 * 一个结点以字节来存储，计算公式如下： [nodeCapacity * (sizeof(Rectangle) + sizeof(Branch) + sizeof(LHV)]
	 * + parent + level + usedSpace + LHV = {nodeCapacity * [(2 * dimension *
	 * sizeof(float)) + sizeof(int)]} + sizeof(int) + sizeof(int) + sizeof(int) + sizeof(long)
	 * 
	 */
	protected int pageSize = -1;

	/**
	 * 树类型
	 */
	protected int treeType = -1;
	
	/**
	 * order of the splitting policy<p>
	 * 
	 * A plain R-tree splits a node on overflow, turning 1 node to 2.
	 * We call this policy a 1-to-2 splitting policy.<br>
	 * 
	 * In general, we can have an s-to-(s+1) splitting policy, we refer to
	 * s as the order of the splitting policy.
	 */
	protected int splitPolicy = 1;;
	
	/**
	 *  Largest Hilbert Value
	 */
	protected int hilbertValue;

	/**
	 * @param page
	 * @return 返回请求page中存储的node
	 * @throws PageFaultError
	 */
	protected abstract HilbertRTNode readNode(int page) throws PageFaultError;

	/**
	 * @param node
	 * @return 将node写入第一个可用的page中，并返回此page
	 * @throws PageFaultError
	 */
	protected abstract int writeNode(HilbertRTNode node) throws PageFaultError;

	/**
	 * @param page
	 * @return 标记指定的page为空
	 * @throws PageFaultError
	 */
	protected abstract HilbertRTNode deletePage(int page) throws PageFaultError;

	/**
	 * PageFile初始化，为其中的属性赋值
	 * 
	 * @param tree
	 * @param dimension
	 * @param fillFactor
	 * @param capacity
	 * @param treeType
	 * @param splitPolicy
	 * 		order of the splitting policy<br>
	 * 		A plain R-tree splits a node on overflow, turning 1 node to 2.
	 * 		We call this policy a 1-to-2 splitting policy.In general, 
	 * 		we can have an s-to-(s+1) splitting policy, we refer to
	 * 		s as the order of the splitting policy.
	 */
	protected void initialize(HilbertRTree tree, int dimension, float fillFactor,
			int capacity, int treeType, int splitPolicy) {
//		logger.info("initializing...");
		this.dimension = dimension;
		this.fillFactor = fillFactor;
		this.nodeCapacity = capacity;
		this.treeType = treeType;
		this.tree = tree;
		this.splitPolicy = splitPolicy;

		this.pageSize = capacity * (8 * dimension + 4 + 8) + 4 * 3 + 8;
	}

	protected void finalize() throws Throwable {
		super.finalize();
	}
}
