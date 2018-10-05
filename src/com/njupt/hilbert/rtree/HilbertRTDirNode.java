package com.njupt.hilbert.rtree;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @ClassName HilbertRTDirNode
 * @Description 非叶节点
 */
public class HilbertRTDirNode extends HilbertRTNode {

	private static final long serialVersionUID = 82956935810904816L;

	public HilbertRTDirNode(HilbertRTree tree, int parent, int pageNumber, int level) {
		super(tree, parent, pageNumber, level);
	}

	/**
	 * @param index
	 * @return 对应索引下的孩子结点
	 */
	public HilbertRTNode getChild(int index) {
		if (index < 0 || index >= usedSpace) {
			throw new IndexOutOfBoundsException("" + index);
		}
		return tree.file.readNode(branches[index]);
	}

	@Deprecated
	@Override
	public HilbertRTDataNode chooseLeaf(Rectangle rectangle) {
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

	@Override
	protected HilbertRTDataNode chooseLeaf(Rectangle rectangle, long hilbertValue) {
		int index = findClosestLhv(hilbertValue);
		if (index == -1) {
			throw new RuntimeException(
					"Not found the entry with minimum LHV value greater than @hilbertValue.");
		}
		return getChild(index).chooseLeaf(rectangle, hilbertValue);
	}

	/**
	 * 寻找最接近的LHV
	 * <p>
	 * choose the entry with the minimum LHV value greater than @hilbertValue.
	 * 
	 * @param hilbertValue
	 * @return
	 */
	private int findClosestLhv(long hilbertValue) {
		if (hilbertValue >= LHV) {
			return usedSpace - 1;
		}

		for (int i = 0; i < usedSpace; i++) {
			if (datas[i].getHilbertValue() < hilbertValue) {
				continue;
			}
			return i;
		}
		return -1;
	}

	/**
	 * @param rectangle
	 * @return 返回最小重叠面积的结点的索引，如果重叠面积相等则选择加入此Rectangle后面积增量更小的，
	 *         如果面积增量还相等则选择自身面积更小的
	 */
	@Deprecated
	private int findLeastOverlap(Rectangle rectangle) {
		float overlap = Float.POSITIVE_INFINITY;
		int sel = -1;

		for (int i = 0; i < usedSpace; i++) {
			HilbertRTNode node = getChild(i);
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
	@Deprecated
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
	 * get @node's position in its parent.
	 * @param node
	 * @return
	 */
	public int getPosition(HilbertRTNode node) {
		if (node == null) {
			throw new RuntimeException("node must be not null!");
		}
		for (int i = 0; i < usedSpace; i++) {
			if (branches[i] == node.pageNumber) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * 插入新的Rectangle后从插入的叶节点开始向上调整RTree，直到根节点
	 * 
	 * @param node1
	 * @param node2
	 */
	public void adjustTree(HilbertRTNode node1, HilbertRTNode node2) {
		if (node1 == null) {
			throw new IllegalArgumentException("node1 must be not null!");
		}
		
		int min = Math.round(tree.getNodeCapacity() * tree.getFillFactor());
		
		/*
		 * 1、[Left, Node] mark = 2, node2 not equal to null. 
		 * 2、[Node, Right] mark = 2, node2 not equal to null. 
		 * 3、[Node, newNode] mark = 1, node2 not equal to null. 
		 * 4、[Node, null] mark = 1, node2 = null.//TODO
		 */
		int mark = 0;
		/* 先要找到指向原来旧的结点（即未添加Rectangle之前）的条目的索引 */
		for (int i = 0; i < usedSpace; i++) {
			if (branches[i] == node1.pageNumber) {
				datas[i] = node1.getNodeRectangle();// update datas
				mark++;
				continue;
			} else if (node2 != null && branches[i] == node2.pageNumber) {
				datas[i] = node2.getNodeRectangle();// update datas
				mark++;
			}
		}
		if (usedSpace >= min) {
			/*
			 * Need to sort according to the hilbert value first.
			 */
			RectangleAndPage[] raps = new RectangleAndPage[usedSpace];
			for (int i = 0; i < usedSpace; i++) {
				raps[i] = new RectangleAndPage(datas[i], branches[i]);
			}
			Arrays.sort(raps);
			int used = usedSpace;
			clear();
			for (int i = 0; i < used; i++) {
				addData(raps[i].rec, raps[i].page);
			}
			tree.file.writeNode(this);
			
			/*
			 * 如果发生分裂我们必须插入新的结点，否则我们必须继续调整tree直到hit root node。
			 */
			if (node2 != null && mark == 1) {
				insert( node2, Constants.HILBERT );// 插入新的结点

			} else if (!isRoot())// 还没到达根节点
			{
				HilbertRTDirNode parent = (HilbertRTDirNode) getParent();
				parent.adjustTree(this, null);// 向上调整直到根节点
			}
		} else if (isRoot()) {
				if (mark == 1 && node1 instanceof HilbertRTDirNode) {
					node1.pageNumber = 0;
					node1.parent = -1;
					for (int i = 0; i < node1.usedSpace; i++) {
						HilbertRTNode node = ((HilbertRTDirNode)node1).getChild(i);
						node.parent = 0;
					}
					tree.file.writeNode(node1);
				} else if (mark == 1 && node1 instanceof HilbertRTDataNode) {
					node1.pageNumber = 0;
					node1.parent = -1;
					tree.file.writeNode(node1);
				}
		} else {
			HilbertRTDirNode[] splitNodes = handleUnderflow();
			HilbertRTDirNode l = splitNodes[0];
			HilbertRTDirNode ll = splitNodes[1];
			switch (merge) {
			case Constants.NO_MERGE:
				tree.file.writeNode(l);
				tree.file.writeNode(ll);
				HilbertRTDirNode parentNode = (HilbertRTDirNode) getParent();
				parentNode.adjustTree(l, ll);
				break;
				
			case Constants.LEFT://right sibling is not exist, merge with left sibling.  
			case Constants.RIGHT://left sibling is not exist, merge with right sibling.
				tree.file.writeNode(l);
				parentNode = (HilbertRTDirNode) getParent();
				assert(parentNode != null);
				if (parentNode != null)
					System.out.println("parentNode != null");
				parentNode.deleteNode(parentNode.getPosition(this));
				parentNode.adjustTree(l, null);
				break;
				
			case Constants.DOUBLE://merge left sibling with right sibling.
				tree.file.writeNode(l);
				tree.file.writeNode(ll);
				parentNode = (HilbertRTDirNode) getParent();
				assert(parentNode != null);
				if (parentNode != null)
					System.out.println("parentNode != null");
				parentNode.deleteNode(parentNode.getPosition(this) + 1);
				parentNode.adjustTree(l, ll);
				break;
				
			case Constants.NONE: //left sibling and right sibling are not exist.
				//TODO
				break;
				
			default:
					break;
			}
		}
	}

	private HilbertRTDirNode[] handleUnderflow() {
		int min = Math.round(tree.getNodeCapacity() * tree.getFillFactor());
		System.out.println("min = " + min);
		
		HilbertRTDirNode parentNode = (HilbertRTDirNode) getParent();
		HilbertRTDirNode leftSibling = null;
		HilbertRTDirNode rightSibling = null;
		
		/*
		 * find the siblings.
		 */
		if ( parentNode != null ) {
			int position = parentNode.getPosition(this);
			if (position == 0 && position < parentNode.usedSpace - 1) {
				leftSibling = null;
				rightSibling = (HilbertRTDirNode)(parentNode.getChild(1));
			} else if (/*position > 0 && */position == parentNode.usedSpace - 1) {
				leftSibling = parentNode.usedSpace != 1 ? 
						(HilbertRTDirNode)(parentNode.getChild(parentNode.usedSpace - 2)) : null;
				rightSibling = null;
			} else {
				leftSibling = (HilbertRTDirNode)(parentNode.getChild(position - 1));
				rightSibling = (HilbertRTDirNode)(parentNode.getChild(position + 1));
			}
		}
		
		/*
		 * 
		 */
		if (leftSibling != null && leftSibling.usedSpace > min) {
			HilbertRTDirNode[] nodes = evenlyDistributed(leftSibling, this, null);
			merge = Constants.NO_MERGE;
			return nodes;
		} else if (rightSibling != null && rightSibling.usedSpace > min) {
			HilbertRTDirNode[] nodes = evenlyDistributed(this, rightSibling, null);
			merge = Constants.NO_MERGE;
			return nodes;
		} else if (leftSibling != null && rightSibling != null) {
			HilbertRTDirNode[] nodes = evenlyDistributed(leftSibling, this, rightSibling);
			merge = Constants.DOUBLE;
			return nodes;
		} else if (leftSibling != null && rightSibling == null) {
			HilbertRTDirNode node = merge(leftSibling, this); //evenlyDistributed(leftSibling, this, null, null, Constants.NIL);
			merge = Constants.LEFT;
			return new HilbertRTDirNode[] { node, null };
		} else if (rightSibling != null) {
			HilbertRTDirNode node = merge(rightSibling, this); //evenlyDistributed(this, rightSibling, null, null, Constants.NIL);
			merge = Constants.RIGHT;
			return new HilbertRTDirNode[] { node, null };
		} else {
			//TODO
//			System.out.println("Not reached.");
//			return null;
			merge = Constants.NONE;
			return new HilbertRTDirNode[] { this, null };
		}
	}
	
	private HilbertRTDirNode merge(HilbertRTDirNode node1, HilbertRTDirNode node2) {
		if (node1.usedSpace + node2.usedSpace > tree.getNodeCapacity())
			throw new IllegalArgumentException("node1 and node2 can not be merged.");
		
		HilbertRTDirNode newNode = new HilbertRTDirNode(tree, node1.parent, node1.pageNumber, node1.level);
		List<NodeRectAndPage> nodeList = new ArrayList<NodeRectAndPage>();
		
		for (int i = 0; i < node1.usedSpace; i++) {
			nodeList.add(new NodeRectAndPage(node1.getChild(i), node1.datas[i], node1.branches[i]));
		}
		for (int i = 0; i < node2.usedSpace; i++) {
			nodeList.add(new NodeRectAndPage(node2.getChild(i), node2.datas[i], node2.branches[i]));
		}
		
		Collections.sort(nodeList);
		node1.clear();
		node2.clear();
		for (int i = 0; i < nodeList.size(); i++) {
			NodeRectAndPage tmp = nodeList.get(i);
			newNode.addData(tmp.rec, tmp.page);
			HilbertRTNode ch = tmp.node;
			ch.parent = newNode.pageNumber;
		}
		return newNode;
	}
	
	private HilbertRTDirNode[] evenlyDistributed(HilbertRTDirNode node1, 
			HilbertRTDirNode node2, HilbertRTDirNode node3) {
		if (node1 == null || node2 == null)
			throw new IllegalArgumentException("node1 or node2 can not be null.");
		
		List<NodeRectAndPage> nodeList = new ArrayList<NodeRectAndPage>();
		
		for (int i = 0; i < node1.usedSpace; i++) {
			nodeList.add(new NodeRectAndPage(node1.getChild(i), node1.datas[i], node1.branches[i]));
		}
		for (int i = 0; i < node2.usedSpace; i++) {
			nodeList.add(new NodeRectAndPage(node2.getChild(i), node2.datas[i], node2.branches[i]));
		}
		if (node3 != null) {
			for (int i = 0; i < node3.usedSpace; i++) {
				nodeList.add(new NodeRectAndPage(node3.getChild(i), node3.datas[i], node3.branches[i]));
			}
		}
		Collections.sort(nodeList);
		node1.clear();
		node2.clear();
		
		for (int i = 0; i < nodeList.size() / 2; i++) {
			NodeRectAndPage tmp = nodeList.get(i);
			node1.addData(tmp.rec, tmp.page);
			HilbertRTNode ch = tmp.node;
			ch.parent = node1.pageNumber;
		}
		for (int i = nodeList.size() / 2; i < nodeList.size(); i++) {
			NodeRectAndPage tmp = nodeList.get(i);
			node2.addData(tmp.rec, tmp.page);
			HilbertRTNode ch = tmp.node;
			ch.parent = node2.pageNumber;
		}
		return new HilbertRTDirNode[] { node1, node2 };
	}
	
	public static int binarySearch(int[] a, int fromIndex, int toIndex,
			int key) {
		int low = fromIndex;
		int high = toIndex - 1;

		while (low <= high) {
			int mid = (low + high) >>> 1;
			int midVal = a[mid];

			if (midVal < key)
				low = mid + 1;
			else if (midVal > key)
				high = mid - 1;
			else
				return mid; // key found
		}
		return -(low + 1); // key not found.
	}

	public static int binarySearch(Object[] a, int fromIndex, int toIndex,
			Object key) {
		int low = fromIndex;
		int high = toIndex - 1;

		while (low <= high) {
			int mid = (low + high) >>> 1;
			Comparable midVal = (Comparable) a[mid];
			int cmp = midVal.compareTo(key);

			if (cmp < 0)
				low = mid + 1;
			else if (cmp > 0)
				high = mid - 1;
			else
				return mid; // key found
		}
		return -(low + 1); // key not found.
	}

	public static void binarySort(Object[] a, int lo, int hi, int start) {
		assert lo <= start && start <= hi;
		if (start == lo)
			start++;
		for (; start < hi; start++) {
			@SuppressWarnings("unchecked")
			Comparable<Object> pivot = (Comparable) a[start];

			// Set left (and right) to the index where a[start] (pivot) belongs
			int left = lo;
			int right = start;
			assert left <= right;
			/*
			 * Invariants: pivot >= all in [lo, left). pivot < all in [right,
			 * start).
			 */
			while (left < right) {
				int mid = (left + right) >>> 1;
				if (pivot.compareTo(a[mid]) < 0)
					right = mid;
				else
					left = mid + 1;
			}
			assert left == right;

			/*
			 * The invariants still hold: pivot >= all in [lo, left) and pivot <
			 * all in [left, start), so pivot belongs at left. Note that if
			 * there are elements equal to pivot, left points to the first slot
			 * after them -- that's why this sort is stable. Slide elements over
			 * to make room for pivot.
			 */
			int n = start - left; // The number of elements to move
			// Switch is just an optimization for arraycopy in default case
			switch (n) {
			case 2:
				a[left + 2] = a[left + 1];
			case 1:
				a[left + 1] = a[left];
				break;
			default:
				System.arraycopy(a, left, a, left + 1, n);
			}
			a[left] = pivot;
		}
	}

	/**
	 * 如果插入结点后导致上溢则需要分裂，<br>
	 * 否则不需要分裂，只需更新数据并重新写入file，最后adjustTree()
	 * 
	 * @param node
	 * @param TAG only as a tag
	 * @return 如果结点需要分裂则返回true
	 */
	protected boolean insert(HilbertRTNode node, int TAG) {
		if ( usedSpace < tree.getNodeCapacity() ) {
			int point = -1;
			int i;
			// 寻找插入点
			for (i = 0; i < usedSpace; i++) {
				if ( datas[i].getHilbertValue() < node.LHV ) {
					continue;
				} else {
					point = i;
					break;
				}
			}

//			if (i == usedSpace) {
			if (point == -1) {
				datas[usedSpace] = node.getNodeRectangle();
				branches[usedSpace] = node.pageNumber;
			} else {
				// 数据后移
				for (i = usedSpace - 1; i >= point; i--) {
					datas[i + 1] = datas[i];
					branches[i + 1] = branches[i];
				}
				datas[point] = node.getNodeRectangle();
				branches[point] = node.pageNumber;
			}

			if (node.LHV > LHV) {
				LHV = node.LHV;
			}
			usedSpace++;
			node.parent = pageNumber;
			tree.file.writeNode(node);
			tree.file.writeNode(this);// 更新文件
			HilbertRTDirNode parent = (HilbertRTDirNode) getParent();

			if (parent != null)
				parent.adjustTree(this, null);
			return false;
			
		} else {// 非叶子结点需要分裂
			HilbertRTDirNode[] a = handleOverflow( node );
			HilbertRTDirNode n = a[0];
			HilbertRTDirNode nn = a[1];
			
			if (isRoot()) {
				n.parent = 0;// 其父节点为根节点
				n.pageNumber = -1;
				nn.parent = 0;
				nn.pageNumber = -1;
				/*
				 * 先将分裂后的结点写入file，它会返回一个存储page，然后遍历孩子结点，
				 * 将孩子结点的parent指针指向此结点，然后将孩子结点重新写入file中
				 */
				int p = tree.file.writeNode( n );
				for (int i = 0; i < n.usedSpace; i++) {
					HilbertRTNode ch = n.getChild(i);
//					System.out.println("n = " + ch);
					ch.parent = p;
					tree.file.writeNode(ch);
				}
				p = tree.file.writeNode(nn);
				for (int i = 0; i < nn.usedSpace; i++) {
					HilbertRTNode ch = nn.getChild(i);
//					System.out.println("nn = " + ch);
					ch.parent = p;
					tree.file.writeNode(ch);
				}
				// 新建根节点，层数加1
				HilbertRTDirNode newRoot = new HilbertRTDirNode(tree, Constants.NIL, 0,
						level + 1);
				newRoot.addData(n.getNodeRectangle(), n.pageNumber);
				newRoot.addData(nn.getNodeRectangle(), nn.pageNumber);
				tree.file.writeNode(newRoot);

			} else {// not root node
//				n.pageNumber = pageNumber;
				n.parent = parent;
//				nn.pageNumber = -1;
				nn.parent = parent;
				int j = tree.file.writeNode(n);
				for (int i = 0; i < n.usedSpace; i++) {
					HilbertRTNode ch = n.getChild(i);
					ch.parent = j;
					tree.file.writeNode(ch);
				}
				j = tree.file.writeNode(nn);
				for (int i = 0; i < nn.usedSpace; i++) {
					HilbertRTNode ch = nn.getChild(i);
					ch.parent = j;
					tree.file.writeNode(ch);
				}
				HilbertRTDirNode p = (HilbertRTDirNode) getParent();
				p.adjustTree( n, nn );
			}
		}
		return true;
	}

	/**
	 * The parent node is full, and the @node is added to it, so split is inevitable. 
	 * @param node the @node want to be added.
	 * @return
	 */
	public HilbertRTDirNode[] handleOverflow(HilbertRTNode node) {
		HilbertRTDirNode parentNode = (HilbertRTDirNode) getParent();
		HilbertRTDirNode leftSibling = null;
		HilbertRTDirNode rightSibling = null;
		/*
		 * find the siblings.
		 */
		if ( parentNode != null ) {
			if ( LHV == parentNode.LHV ) {
				leftSibling = parentNode.usedSpace != 1 ? 
						(HilbertRTDirNode)(parentNode.getChild(parentNode.usedSpace - 2)) : null;
				rightSibling = null;
			} else if ( LHV == parentNode.datas[0].getHilbertValue()) {
				leftSibling = null;
				rightSibling = (HilbertRTDirNode)(parentNode.getChild(1));
			} else {
				for (int i = 1; i < parentNode.usedSpace - 1; i++) {
					if (LHV == parentNode.datas[i].getHilbertValue()) {
						leftSibling = (HilbertRTDirNode)(parentNode.getChild(i - 1));
						rightSibling = (HilbertRTDirNode)(parentNode.getChild(i + 1));
					}
				}
			}
		} 
		
		/*
		 * First select the left sibling only if left sibling is not null, 
		 * otherwise the right sibling or split. 
		 * Distribute all the entries that come from @node and its sibling 
		 * evenly among the node's according to the Hilbert value.
		 */
		if (leftSibling != null && leftSibling.usedSpace != tree.getNodeCapacity()) {
			return evenlyDistributed(leftSibling, this, node);
			
		} else if (rightSibling != null && rightSibling.usedSpace != tree.getNodeCapacity()) {
			return evenlyDistributed(this, rightSibling, node);
			
		} else { //need to split
			HilbertRTDirNode index2 = new HilbertRTDirNode(tree, parent, -1, level);
			return evenlyDistributed(this, index2, node);
		}
	}
	
	private HilbertRTDirNode[] evenlyDistributed(HilbertRTDirNode node1, 
			HilbertRTDirNode node2, HilbertRTNode node) {
		List<RectangleAndPage> dataList = new ArrayList<RectangleAndPage>();
		for (int i = 0; i < node1.usedSpace; i++) {
			dataList.add(new RectangleAndPage(node1.datas[i], node1.branches[i]));
		}
		for (int i = 0; i < node2.usedSpace; i++) {
			dataList.add(new RectangleAndPage(node2.datas[i], node2.branches[i]));
		}
		if (node != null)
			dataList.add(new RectangleAndPage(node.getNodeRectangle(), node.pageNumber));
		Collections.sort(dataList);
		node1.clear();
		node2.clear();
		for (int i = 0; i < dataList.size() / 2; i++) {
			node1.addData(dataList.get(i).rec, dataList.get(i).page);
		}
		for (int i = dataList.size() / 2; i < dataList.size(); i++) {
			node2.addData(dataList.get(i).rec, dataList.get(i).page);
		}
		return new HilbertRTDirNode[] { node1, node2 };
	}
	
	/**
	 * 非叶子结点的分裂
	 * 
	 * @param node
	 * @return
	 */
	@Deprecated
	private HilbertRTDirNode[] splitIndex(HilbertRTNode node) {
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

		HilbertRTDirNode index1 = new HilbertRTDirNode(tree, parent, pageNumber, level);
		HilbertRTDirNode index2 = new HilbertRTDirNode(tree, parent, -1, level);

		int[] group1 = group[0];
		int[] group2 = group[1];

		for (int i = 0; i < group1.length; i++) {
			index1.addData(datas[group1[i]], branches[group1[i]]);
		}
		for (int i = 0; i < group2.length; i++) {
			index2.addData(datas[group2[i]], branches[group2[i]]);
		}

		return new HilbertRTDirNode[] { index1, index2 };
	}

	@Override
	protected HilbertRTDataNode findLeaf(Rectangle rectangle) {
		for (int i = 0; i < usedSpace; i++) {
			if (datas[i].enclosure(rectangle)) {
				HilbertRTDataNode leaf = getChild(i).findLeaf(rectangle);
				if (leaf != null)
					return leaf;
			}
		}
		return null;
	}

	public static void main(String[] args) {
		int[] test = new int[] { 1, 2, 3, 4, 5, 7, 8 };
		int i = HilbertRTDirNode.binarySearch( test, 0, test.length - 1, 10 );
		System.out.println( i );
		System.out.println(HilbertRTDirNode.binarySearch( test, 0, test.length - 1, 4 ));
	}
}

class NodeRectAndPage implements Comparable<NodeRectAndPage> {
	HilbertRTNode node;
	Rectangle rec;
	int page;
	
	public NodeRectAndPage(HilbertRTNode node, Rectangle rec, int page) {
		super();
		this.node = node;
		this.rec = rec;
		this.page = page;
	}

	@Override
	public int compareTo(NodeRectAndPage o) {
		return Long.compare(rec.getHilbertValue(), o.rec.getHilbertValue());
	}

	@Override
	public String toString() {
		return "NodeRectAndPage [node=" + node + ", rec=" + rec + ", page="
				+ page + "]";
	}

}
