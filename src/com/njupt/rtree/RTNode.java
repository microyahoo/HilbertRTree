package com.njupt.rtree;

import java.util.List;

/**
 * @ClassName RTNode
 * @Description
 */
public abstract class RTNode implements INode {
	/**
	 * 结点所在的树
	 */
	protected transient RTree tree;

	/**
	 * 结点所在的层，叶子结点所在的层为0
	 */
	protected int level;

	/**
	 * 相当于条目
	 */
	public Rectangle[] datas;

	/**
	 * 结点已用的空间
	 */
	protected int usedSpace;

	/**
	 * 
	 */
	public int[] branches;

	/**
	 * 此node被存储的pageNumber
	 */
	protected int pageNumber;

	/**
	 * 此结点的父节点被存储的pageNumber
	 */
	protected int parent;
	
	protected RTNode(RTree tree, int parent, int pageNumber, int level) {
		this.parent = parent;
		this.tree = tree;
		this.pageNumber = pageNumber;
		this.level = level;
		datas = new Rectangle[tree.getNodeCapacity() + 1];
		branches = new int[tree.getNodeCapacity() + 1];
		usedSpace = 0;
	}


	/**
	 * @return 结点所在的层
	 */
	@Override
	public int getLevel() {
		return level;
	}

	/**
	 * @return 返回父节点
	 */
	@Override
	public RTNode getParent() {
		if (isRoot()) {
			return null;
		} else {
			return tree.file.readNode(parent);
		}
	}

	/**
	 * @return Returns a unique id for this node. The page number is unique for every node.
	 */
	@Override
	public String getUniqueId() {
		return Integer.toBinaryString(pageNumber);
	}
	
	protected void addData(Rectangle rectangle, int page) {
		if (usedSpace == tree.getNodeCapacity()) {
			throw new IllegalArgumentException("Node is full.");
		}
		datas[usedSpace] = rectangle;
		branches[usedSpace] = page;
		usedSpace++;
	}
	
	/**
     * Adds a child node into this node.
     * This function does not save the node into persistent storage. 
     * It is used for bulk loading a node whith data. The user must 
     * make sure that she saves the node into persistent storage, after
     * calling this function.
     *
     * @param node The new node to insert as a child of the current node.
     *
     */
	public void addData(RTNode node) {
		addData(node.getNodeRectangle(), node.pageNumber);
	}

	/**
	 * 删除结点中的第i个条目
	 * 
	 * @param i
	 */
	protected void deleteData(int i) {
		if (datas[i + 1] != null) {
			System.arraycopy(datas, i + 1, datas, i, usedSpace - i - 1);
			System.arraycopy(branches, i + 1, branches, i, usedSpace - i - 1);
			datas[usedSpace - 1] = null;
			branches[usedSpace - 1] = 0;
		} else {
			datas[i] = null;
			branches[i] = 0;
		}
//		if (datas[i].getHilbertValue() >= LHV) {
			//TODO
//		}
		usedSpace--;
	}

	/**
	 * 树的压缩。叶节点L中刚刚删除了一个条目。如果这个节点的条目数太少，则删除该结点，
	 * 同时将这些条目重定位到其他节点中。如果有必要，要逐级向上进行这种删除。 调整向上传递的路径上的所有外廓矩形，使其尽可能小，直到根节点。
	 * <p>
	 * <b>步骤CT1：</b>初始化——记N=L，定义Q为删除节点的集合，初始化的时候将此数组置空。<br>
	 * <b>步骤CT2：</b>查找父条目，注意是父条目，不是父节点——如果N是根节点，转到步骤CT6。
	 * 如果N不是根节点，记P为N的父节点，并记En为P中代表N的那个条目。<br>
	 * <b>步骤CT3：</b>删除下溢结点——如果N中的条目数小于m，意味着节点N下溢，此时应当将En从P中移除，并将N加入Q。<br>
	 * <b>步骤CT4：</b>调整外廓矩形——如果N没有被删除，则调整En的外廓矩形EnI，使其尽量变小、恰好包含N中的所有条目。<br>
	 * <b>步骤CT5：</b>向上一层——令N=P，返回步骤CT2重新执行。<br>
	 * <b>步骤CT6：</b>重新插入孤立条目——对Q中所有节点的所有条目执行重新插入。叶节点中的条目
	 * 使用算法Insert重新插入到树的叶节点中；较高层节点中的条目必须插入到树的较高位置上。
	 * 这是为了保证这些较高层节点下的子树的叶子节点、与其他叶子节点能够放置在同一层上。<br>
	 * --------------------------------------------------------------------
	 * <p>
	 * 叶节点L中刚刚删除了一个条目，如果这个结点的条目数太少而下溢，则删除该结点，同时将该结点中剩余的条目重定位到其他结点中。
	 * 如果有必要，要逐级向上进行这种删除，调整向上传递的路径上的所有外包矩形，使其尽可能小，直到根节点。
	 * 
	 * @param list
	 *            存储删除结点中剩余条目
	 */
	protected void condenseTree(List<RTNode> list) {
		if (isRoot()) {
			// 根节点只有一个条目了，即只有左孩子或者右孩子
			if (!isLeaf() && usedSpace == 1) {
				RTNode n = tree.file.readNode(branches[0]);
				tree.file.deletePage(n.pageNumber);
				n.pageNumber = 0;
				n.parent = Constants.NIL;//?
				tree.file.writeNode(n);//将孩子写入
				if (!n.isLeaf()) {
					for (int i = 0; i < n.usedSpace; i++) {
						RTNode m = ((RTDirNode)n).getChild(i);
						m.parent = 0;//?
						tree.file.writeNode(m);//parent属性有变化重新写入
					}
				}
			}
		} else {
			RTNode p = getParent();
			int e;
			//在父节点中找到此结点的条目
			for (e = 0; e < p.usedSpace; e++) {
				if (pageNumber == p.branches[e])
					break;
			}
			
			int min = Math.round(tree.getNodeCapacity()
					* tree.getFillFactor());
			if (usedSpace < min) {
				p.deleteData(e);
				list.add(this);// 之前已经把数据删除了
			} else {
				p.datas[e] = getNodeRectangle();
			}
			tree.file.writeNode(p);
			p.condenseTree(list);
		}
	}

	/**
	 * <b>分裂结点的平方算法</b>
	 * <p>
	 * 1、为两个组选择第一个条目--调用算法pickSeeds()来为两个组选择第一个元素，分别把选中的两个条目分配到两个组当中。<br>
	 * 2、检查是否已经分配完毕，如果一个组中的条目太少，为避免下溢，将剩余的所有条目全部分配到这个组中，算法终止<br>
	 * 3、调用pickNext来选择下一个进行分配的条目--计算把每个条目加入每个组之后面积的增量，选择两个组面积增量差最大的条目索引,
	 * 	    如果面积增量相等则选择面积较小的组，若面积也相等则选择条目数更少的组<br>
	 * 
	 * @param rectangle
	 *            导致分裂的溢出Rectangle
	 * @param page 
	 * 			      引起分裂的孩子结点被存储的page，如果分裂发生在叶子结点则为-1,
	 * @return 两个组中的条目的索引
	 */
	protected int[][] quadraticSplit(Rectangle rectangle, int page) {
		if (rectangle == null) {
			throw new IllegalArgumentException("Rectangle cannot be null.");
		}

		datas[usedSpace] = rectangle; // 先添加进去
		branches[usedSpace] = page;
		int total = usedSpace + 1; // 结点总数

		// 标记访问的条目
		int[] mask = new int[total];
		for (int i = 0; i < total; i++) {
			mask[i] = 1;
		}

		// 每个组只是有total/2个条目
		int c = total / 2 + 1;
		// 每个结点最小条目个数
		int minNodeSize = Math.round(tree.getNodeCapacity()
				* tree.getFillFactor());
		// 至少有两个
		if (minNodeSize < 2)
			minNodeSize = 2;

		// 记录没有被检查的条目的个数
		int rem = total;

		int[] group1 = new int[c];// 记录分配的条目的索引
		int[] group2 = new int[c];// 记录分配的条目的索引
		// 跟踪被插入每个组的条目的索引
		int i1 = 0, i2 = 0;

		int[] seed = quadraticPickSeeds();
		group1[i1++] = seed[0];
		group2[i2++] = seed[1];
		rem -= 2;
		mask[group1[0]] = -1;
		mask[group2[0]] = -1;

		while (rem > 0) {
			// 将剩余的所有条目全部分配到group1组中，算法终止
			if (minNodeSize - i1 == rem) {
				for (int i = 0; i < total; i++)// 总共rem个
				{
					if (mask[i] != -1)// 还没有被分配
					{
						group1[i1++] = i;
						mask[i] = -1;
						rem--;
					}
				}
				// 将剩余的所有条目全部分配到group1组中，算法终止
			} else if (minNodeSize - i2 == rem) {
				for (int i = 0; i < total; i++)// 总共rem个
				{
					if (mask[i] != -1)// 还没有被分配
					{
						group2[i2++] = i;
						mask[i] = -1;
						rem--;
					}
				}
			} else {
				// 求group1中所有条目的最小外包矩形
				Rectangle mbr1 = (Rectangle) datas[group1[0]].clone();
				for (int i = 1; i < i1; i++) {
					mbr1 = mbr1.getUnionRectangle(datas[group1[i]]);
				}
				// 求group2中所有条目的外包矩形
				Rectangle mbr2 = (Rectangle) datas[group2[0]].clone();
				for (int i = 1; i < i2; i++) {
					mbr2 = mbr2.getUnionRectangle(datas[group2[i]]);
				}

				// 找出下一个进行分配的条目
				double dif = Double.NEGATIVE_INFINITY;
				double areaDiff1 = 0, areaDiff2 = 0;
				int sel = -1;
				for (int i = 0; i < total; i++) {
					if (mask[i] != -1)// 还没有被分配的条目
					{
						// 计算把每个条目加入每个组之后面积的增量，选择两个组面积增量差最大的条目索引
						Rectangle a = mbr1.getUnionRectangle(datas[i]);
						areaDiff1 = a.getArea() - mbr1.getArea();

						Rectangle b = mbr2.getUnionRectangle(datas[i]);
						areaDiff2 = b.getArea() - mbr2.getArea();

						if (Math.abs(areaDiff1 - areaDiff2) > dif) {
							dif = Math.abs(areaDiff1 - areaDiff2);
							sel = i;
						}
					}
				}

				if (areaDiff1 < areaDiff2)// 先比较面积增量
				{
					group1[i1++] = sel;
				} else if (areaDiff1 > areaDiff2) {
					group2[i2++] = sel;
				} else if (mbr1.getArea() < mbr2.getArea())// 再比较自身面积
				{
					group1[i1++] = sel;
				} else if (mbr1.getArea() > mbr2.getArea()) {
					group2[i2++] = sel;
				} else if (i1 < i2)// 最后比较条目个数
				{
					group1[i1++] = sel;
				} else if (i1 > i2) {
					group2[i2++] = sel;
				} else {
					group1[i1++] = sel;
				}
				mask[sel] = -1;
				rem--;

			}
		}// end while

		int[][] ret = new int[2][];
		ret[0] = new int[i1];
		ret[1] = new int[i2];

		for (int i = 0; i < i1; i++) {
			ret[0][i] = group1[i];
		}
		for (int i = 0; i < i2; i++) {
			ret[1][i] = group2[i];
		}
		return ret;
	}

	/**
	 * 1、对每一对条目E1和E2，计算包围它们的Rectangle J，计算d = area(J) - area(E1) - area(E2);<br>
	 * 2、Choose the pair with the largest d
	 * 
	 * @return 返回两个条目如果放在一起会有最多的冗余空间的条目索引
	 */
	protected int[] quadraticPickSeeds() {
		double inefficiency = Double.NEGATIVE_INFINITY;
		int i1 = 0, i2 = 0;

		//
		for (int i = 0; i < usedSpace; i++) {
			for (int j = i + 1; j <= usedSpace; j++)// 注意此处的j值
			{
				Rectangle rectangle = datas[i].getUnionRectangle(datas[j]);
				double d = rectangle.getArea() - datas[i].getArea()
						- datas[j].getArea();

				if (d > inefficiency) {
					inefficiency = d;
					i1 = i;
					i2 = j;
				}
			}
		}
		return new int[] { i1, i2 };
	}

//	/**
//	 * @return
//	 */
	// public int[] linearPickSeeds()
	// {
	//
	// }

	
	
	/**
	 * @return 返回包含结点中所有条目的最小Rectangle
	 */
	@Override
	public Rectangle getNodeRectangle() {
		if (usedSpace > 0) {
			Rectangle[] rectangles = new Rectangle[usedSpace];
			System.arraycopy(datas, 0, rectangles, 0, usedSpace);
			Rectangle ret = Rectangle.getUnionRectangle(rectangles);
//			if (ret.getHilbertValue() > lhv) {
//				lhv = ret.getHilbertValue();
//			}
			return ret;
		} else {
			return new Rectangle(new Point(new float[] { 0, 0 }),
								 new Point(new float[] { 0, 0 }));
		}
	}

	/**
	 * @return 是否根节点
	 */
	@Override
	public boolean isRoot() {
		return (parent == Constants.NIL);
	}

	/**
	 * @return 是否非叶子结点
	 */
	@Override
	public boolean isIndex() {
		return (level != 0);
	}

	/**
	 * @return 是否叶子结点
	 */
	@Override
	public boolean isLeaf() {
		return (level == 0);
	}

	@Override
	public String toString() {
		String s = "< Page: " + pageNumber + ", Level: " + level 
				+ ", UsedSpace: " + usedSpace + ", Parent: " + parent + " >\n";
		
		for (int i = 0; i < usedSpace; i++) {
		    s += "  " + (i + 1) + ") " + datas[i].toStr() + " --> " + " page: " + branches[i] + "\n";
		}
		
		return s;
	}

	/**
	 * 记R树的根节点记为T。搜索算法要求输入一个搜索矩形S，输出所有与S相交的索引记录。<br>
	 * <b>步骤S1：</b>搜索子树——如果T不是一个叶节点，则检查其中的每一个条目E，如果EI与S相交，
	 * 则对Ep所指向的那个子树根节点调用Search算法。这里注意，Search算法接收的
	 * 输入为一个根节点，所以在描述算法的时候，原文称对子树根节点调用Search算法， 而不是对子树调用Search算法。<br>
	 * <b>步骤S2：</b>搜索叶节点——如果T是一个叶节点，则检查其中的每个条目E，如果EI与S相交， 则E就是需要返回的检索结果之一。
	 * 
	 * 输入一个搜索矩形S，输出所有与S相交的索引记录 从根节点开始搜索，搜索不是单向的 搜索子树
	 * 搜索叶节点，检查其中的每个条目E，如果其外形轮廓与S相交，则E就是需要返回的检索结果之一
	 * 
	 * @param region
	 * @return List<Rectangle>
	 */
	public List<Rectangle> search(Rectangle region, RTNode root) {
		return null;

	}

	/**
	 * <b>步骤CL1：</b>初始化——记R树的根节点为N。<br>
	 * <b>步骤CL2：</b>检查叶节点——如果N是个叶节点，返回N<br>
	 * <b>步骤CL3：</b>选择子树——如果N不是叶节点，则从N中所有的条目中选出一个最佳的条目F，
	 * 选择的标准是：如果E加入F后，F的外廓矩形FI扩张最小，则F就是最佳的条目。如果有两个
	 * 条目在加入E后外廓矩形的扩张程度相等，则在这两者中选择外廓矩形较小的那个。<br>
	 * <b>步骤CL4：</b>向下寻找直至达到叶节点——记Fp指向的孩子节点为N，然后返回步骤CL2循环运算， 直至查找到叶节点。
	 * <p>
	 * 
	 * @param Rectangle
	 * @return RTDataNode
	 */
	protected abstract RTDataNode chooseLeaf(Rectangle rectangle);
	
	/**
	 * R树的根节点为T，查找包含rectangle的叶子结点
	 * <p>
	 * 1、如果T不是叶子结点，则逐个查找T中的每个条目是否包围rectangle，若包围则递归调用findLeaf()<br>
	 * 2、如果T是一个叶子结点，则逐个检查T中的每个条目能否匹配rectangle<br>
	 * 
	 * @param rectangle
	 * @return 返回包含rectangle的叶节点
	 */
	protected abstract RTDataNode findLeaf(Rectangle rectangle);

}
