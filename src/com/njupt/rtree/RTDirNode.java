package com.njupt.rtree;

import java.util.List;


/**
 * @ClassName RTDirNode
 * @Description 非叶节点
 */
public class RTDirNode extends RTNode {

	public RTDirNode(RTree tree, int parent, int pageNumber, int level) {
		super(tree, parent, pageNumber, level);
	}

	/**
	 * @param index
	 * @return 对应索引下的孩子结点
	 */
	public RTNode getChild(int index) {
		if (index < 0 || index >= usedSpace) {
			throw new IndexOutOfBoundsException("" + index);
		}
		return tree.file.readNode(branches[index]);
	}

	@Override
	public RTDataNode chooseLeaf(Rectangle rectangle) {
		int index;

		switch (tree.getTreeType()) {
		case Constants.RTREE_LINEAR:

		case Constants.RTREE_QUADRATIC:

		case Constants.RTREE_EXPONENTIAL:
			index = findLeastEnlargement(rectangle);
			break;
		case Constants.RSTAR:
			if (level == 1)// 即此结点指向叶节点
			{
				index = findLeastOverlap(rectangle);
			} else {
				index = findLeastEnlargement(rectangle);
			}
			break;

		default:
			throw new IllegalStateException("Invalid tree type.");
		}

		return getChild(index).chooseLeaf(rectangle);
	}

	/**
	 * @param rectangle
	 * @return 返回最小重叠面积的结点的索引，如果重叠面积相等则选择加入此Rectangle后面积增量更小的，
	 *         如果面积增量还相等则选择自身面积更小的
	 */
	private int findLeastOverlap(Rectangle rectangle) {
		float overlap = Float.POSITIVE_INFINITY;
		int sel = -1;

		for (int i = 0; i < usedSpace; i++) {
			RTNode node = getChild(i);
			float ol = 0;

			for (int j = 0; j < node.datas.length; j++) {
				ol += rectangle.intersectingArea(node.datas[j]);
			}
			if (ol < overlap) {
				overlap = ol;// 记录重叠面积最小的
				sel = i;// 记录第几个孩子的索引
			} else if (ol == overlap)// 如果重叠面积相等则选择加入此Rectangle后面积增量更小的,如果面积增量还相等则选择自身面积更小的
			{
				double area1 = datas[i].getUnionRectangle(rectangle).getArea()
						- datas[i].getArea();
				double area2 = datas[sel].getUnionRectangle(rectangle)
						.getArea() - datas[sel].getArea();

				if (area1 == area2) {
					sel = (datas[sel].getArea() <= datas[i].getArea()) ? sel
							: i;
				} else {
					sel = (area1 < area2) ? i : sel;
				}
			}
		}
		return sel;
	}

	/**
	 * @param rectangle
	 * @return 面积增量最小的结点的索引，如果面积增量相等则选择自身面积更小的
	 */
	private int findLeastEnlargement(Rectangle rectangle) {
		double area = Double.POSITIVE_INFINITY;
		int sel = -1;

		for (int i = 0; i < usedSpace; i++) {
			double enlargement = datas[i].getUnionRectangle(rectangle)
					.getArea() - datas[i].getArea();
			if (enlargement < area) {
				area = enlargement;
				sel = i;
			} else if (enlargement == area) {
				sel = (datas[sel].getArea() <= datas[i].getArea()) ? sel : i;
			}
		}

		return sel;
	}

	/**
	 * 插入新的Rectangle后从插入的叶节点开始向上调整RTree，直到根节点
	 * 
	 * @param node1
	 *            引起需要调整的孩子结点
	 * @param node2
	 *            分裂的结点，若未分裂则为null
	 */
	public void adjustTree(RTNode node1, RTNode node2) {
		// 先要找到指向原来旧的结点（即未添加Rectangle之前）的条目的索引
		for (int i = 0; i < usedSpace; i++) {
			if (branches[i] == node1.pageNumber) {
				datas[i] = node1.getNodeRectangle();// 更新数据
//				tree.file.writeNode(this);//新加的
				break;
			}
		}

		if (node2 == null) {
			tree.file.writeNode(this);
		}
		/*
		 * 如果发生分裂我们必须插入新的结点，否则我们必须继续调整tree直到碰到root结点。
		 */
		if (node2 != null) {
			insert(node2);// 插入新的结点

		} else if (!isRoot())// 还没到达根节点
		{
			RTDirNode parent = (RTDirNode) getParent();
			parent.adjustTree(this, null);// 向上调整直到根节点
		}
	}
	
//	/**
//	 * @param S S is a set of nodes that contains the node being updated,
//	 * its cooperating sibings(if overflow has occurred) and newly
//	 * created node NN(if split has occurred).
//	 */
	/**
	 * @param node the node being updated
	 * @param siblings its cooperating sibings(if overflow has occurred)
	 * @param NN newly created node(if split has occurred).
	 */
	public void adjustTree(/*List<RTNode> S*/RTNode node, List<RTNode> siblings, RTNode NN) {
		// 先要找到指向原来旧的结点（即未添加Rectangle之前）的条目的索引
		for (int i = 0; i < usedSpace; i++) {
			if (branches[i] == node.pageNumber) {
				datas[i] = node.getNodeRectangle();// 更新数据
//				tree.file.writeNode(this);//新加的
				break;
			}
		}

		if (NN == null) {
			tree.file.writeNode(this);
		}
		/*
		 * 如果发生分裂我们必须插入新的结点，否则我们必须继续调整tree直到碰到root结点。
		 */
		if (NN != null) {
			insert(NN);// 插入新的结点

		} else if (!isRoot())// 还没到达根节点
		{
			RTDirNode parent = (RTDirNode) getParent();
			parent.adjustTree(this, null);// 向上调整直到根节点
		}
	}

	/**
	 * 如果插入结点后导致上溢则需要分裂，<br>
	 * 否则不需要分裂，只需更新数据并重新写入file，最后adjustTree()
	 * 
	 * @param node
	 * @return 如果结点需要分裂则返回true
	 */
	protected boolean insert(RTNode node) {
		if (usedSpace < tree.getNodeCapacity()) {
			datas[usedSpace] = node.getNodeRectangle();
			branches[usedSpace] = node.pageNumber;
			usedSpace++;
			node.parent = pageNumber;
			tree.file.writeNode(node);
			tree.file.writeNode(this);
			/* 先获取其父节点，然后从其父节点开始调整树结构 */
			RTDirNode parent = (RTDirNode) getParent();
			if (parent != null) {
				parent.adjustTree(this, null);
			}
			return false;
		} else {// 非叶子结点需要分裂
			RTDirNode[] a = splitIndex(node);
			RTDirNode n = a[0];
			RTDirNode nn = a[1];

			if (isRoot()) {
				n.parent = 0;// 其父节点为根节点
				n.pageNumber = -1;
				nn.parent = 0;
				nn.pageNumber = -1;
				/*
				 * 先将分裂后的结点写入file，它会返回一个存储page，然后遍历孩子结点，
				 * 将孩子结点的parent指针指向此结点，然后将孩子结点重新写入file中
				 */
				int p = tree.file.writeNode(n);
				for (int i = 0; i < n.usedSpace; i++) {
					RTNode ch = n.getChild(i);
					ch.parent = p;
					tree.file.writeNode(ch);
				}
				p = tree.file.writeNode(nn);
				for (int i = 0; i < nn.usedSpace; i++) {
					RTNode ch = nn.getChild(i);
					ch.parent = p;
					tree.file.writeNode(ch);
				}
				// 新建根节点，层数加1
				RTDirNode newRoot = new RTDirNode(tree, Constants.NIL, 0,
						level + 1);
				newRoot.addData(n.getNodeRectangle(), n.pageNumber);
				newRoot.addData(nn.getNodeRectangle(), nn.pageNumber);
				tree.file.writeNode(newRoot);

			} else {// not root node, but need split
				n.pageNumber = pageNumber;
				n.parent = parent;
				nn.pageNumber = -1;
				nn.parent = parent;
				tree.file.writeNode(n);
				int j = tree.file.writeNode(nn);
				for (int i = 0; i < nn.usedSpace; i++) {
					RTNode ch = nn.getChild(i);
					ch.parent = j;
					tree.file.writeNode(ch);
				}
				RTDirNode p = (RTDirNode) getParent();
				p.adjustTree(n, nn);
			}
		}
		return true;
	}

	/**
	 * 非叶子结点的分裂
	 * 
	 * @param node
	 * @return
	 */
	private RTDirNode[] splitIndex(RTNode node) {
		int[][] group = null;

		switch (tree.getTreeType()) {
		case Constants.RTREE_LINEAR:
			break;
		case Constants.RTREE_QUADRATIC:
			group = quadraticSplit(node.getNodeRectangle(), node.pageNumber);
			break;
		case Constants.RTREE_EXPONENTIAL:
			break;
		case Constants.RSTAR:
			break;
		default:
			throw new IllegalStateException("Invalid tree type.");
		}

		RTDirNode index1 = new RTDirNode(tree, parent, pageNumber, level);
		RTDirNode index2 = new RTDirNode(tree, parent, -1, level);

		int[] group1 = group[0];
		int[] group2 = group[1];

		for (int i = 0; i < group1.length; i++) {
			index1.addData(datas[group1[i]], branches[group1[i]]);
		}
		for (int i = 0; i < group2.length; i++) {
			index2.addData(datas[group2[i]], branches[group2[i]]);
		}

		return new RTDirNode[] { index1, index2 };
	}

	@Override
	protected RTDataNode findLeaf(Rectangle rectangle) {
		for (int i = 0; i < usedSpace; i++) {
			if (datas[i].enclosure(rectangle)) {
				RTDataNode leaf = getChild(i).findLeaf(rectangle);
				if (leaf != null)
					return leaf;
			}
		}
		return null;
	}

}
