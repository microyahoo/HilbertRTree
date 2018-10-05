package com.njupt.hilbert.rtree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @ClassName HilbertRTDataNode
 * @Description
 */
public class HilbertRTDataNode extends HilbertRTNode {
	
	private static final long serialVersionUID = 6903514643428970529L;

	public HilbertRTDataNode(HilbertRTree tree, int parent, int pageNumber) {
		super(tree, parent, pageNumber, 0);// 叶子结点默认属于0层
	}

	public HilbertRTDataNode(HilbertRTree tree, int parent) {
		super(tree, parent, -1, 0);// 先传递一个默认值负数（-1）作为pageNumber
	}

	/**
	 * 在叶节点中插入Rectangle，插入后如果其父节点不为空则需要向上调整树直到根节点；<br>
	 * 若插入Rectangle之后超过结点容量则需要分裂结点
	 * 
	 * @param rectangle
	 * @return rectangle被插入的叶节点的pageNumber
	 */
	@Deprecated
	public int insert(Rectangle rectangle, int page) {
		if (usedSpace < tree.getNodeCapacity()) {
			datas[usedSpace] = rectangle;
			branches[usedSpace] = page;
			usedSpace++;
			tree.file.writeNode(this);// 更新文件
			HilbertRTDirNode parent = (HilbertRTDirNode) getParent();

			if (parent != null)
				parent.adjustTree(this, null);
			return pageNumber;

		} else {// 超过结点容量
			HilbertRTDataNode[] splitNodes = splitLeaf(rectangle, page);
			HilbertRTDataNode l = splitNodes[0];
			HilbertRTDataNode ll = splitNodes[1];

			if (isRoot()) {
				// root is full, so we must split it. From now on root will be
				// an Index and not a Leaf.
				l.parent = 0;
				l.pageNumber = -1;
				ll.parent = 0;
				ll.pageNumber = -1;
				tree.file.writeNode(l);
				tree.file.writeNode(ll);
				// 根节点已满，需要分裂。创建新的根节点,它的pageNumber=0，level=1
				HilbertRTDirNode r = new HilbertRTDirNode(tree, Constants.NIL, 0, 1);
				r.addData(l.getNodeRectangle(), l.pageNumber);
				r.addData(ll.getNodeRectangle(), ll.pageNumber);
				tree.file.writeNode(r);

			} else {// 不是根节点
				// use old page number for left child,
				// a new page number for the right child.
				l.pageNumber = pageNumber;
				ll.pageNumber = -1;
				tree.file.writeNode(l);
				tree.file.writeNode(ll);
				HilbertRTDirNode parentNode = (HilbertRTDirNode) getParent();
				parentNode.adjustTree(l, ll);
			}

			for (int i = 0; i < l.usedSpace; i++) {
				if (l.branches[i] == page) {
					return l.pageNumber;
				}
			}

			for (int i = 0; i < ll.usedSpace; i++) {
				if (ll.branches[i] == page) {
					return ll.pageNumber;
				}
			}

			return -1;
		}
	}

	/**
	 * Insert @rectangle in a leaf node L.
	 * <P>
	 * <li>if L has an empty slot, insert @rectangle in L in the appropriate place
	 * accroding to the Hilbert order and return.<br>
	 * <li>if L is full, invoke handleOverflow(L, r), which will return new leaf if
	 * split was inevitable.
	 * 
	 * @param rectangle
	 * @param page
	 * @param TAG
	 *            仅仅作为HilbertRTree的标记而已
	 * @return
	 */
	public int insert(Rectangle rectangle, int page, int TAG) {
		if (usedSpace < tree.getNodeCapacity()) {
			int point = -1;
			int i;
			// 寻找插入点
			for (i = 0; i < usedSpace; i++) {
				if (datas[i].getHilbertValue() < rectangle.getHilbertValue()) {
					continue;
				} else {
					point = i;
					break;
				}
			}

//			if (i == usedSpace) {
			if (point == -1) {
				datas[usedSpace] = rectangle;
				branches[usedSpace] = page;
			} else {
				// 数据后移
				for (i = usedSpace - 1; i >= point; i--) {
					datas[i + 1] = datas[i];
					branches[i + 1] = branches[i];
				}
				datas[point] = rectangle;
				branches[point] = page;
			}

			if (rectangle.getHilbertValue() > LHV) {
				LHV = rectangle.getHilbertValue();
			}
			usedSpace++;
			tree.file.writeNode(this);// 更新文件
			HilbertRTDirNode parent = (HilbertRTDirNode) getParent();

			if (parent != null)
				parent.adjustTree(this, null);
			return pageNumber;

		} else {// 超过结点容量
			HilbertRTDataNode[] splitNodes = handleOverflow(rectangle, page);
//			HilbertRTDataNode[] splitNodes = splitLeaf(rectangle, page);
			HilbertRTDataNode l = splitNodes[0];
			HilbertRTDataNode ll = splitNodes[1];

			if (isRoot()) {
				/* root is full, so we must split it. From now on 
				 * root will be an Index and not a Leaf.
				 */
				l.parent = 0;
				l.pageNumber = -1;
				ll.parent = 0;
				ll.pageNumber = -1;
				tree.file.writeNode(l);
				tree.file.writeNode(ll);
				/* 根节点已满，需要分裂。创建新的根节点,它的pageNumber=0，level=1 */
				HilbertRTDirNode r = new HilbertRTDirNode(tree, Constants.NIL, 0, 1);
				r.addData(l.getNodeRectangle(), l.pageNumber);
				r.addData(ll.getNodeRectangle(), ll.pageNumber);
				tree.file.writeNode(r);

			} else {// 不是根节点
				/* use old page number for left child,
				 * a new page number for the right child.
				 */
//				l.pageNumber = pageNumber;
//				if (ll.pageNumber == Constants.NOPAGE)
//					ll.pageNumber = -1;
				tree.file.writeNode(l);
				tree.file.writeNode(ll);
				HilbertRTDirNode parentNode = (HilbertRTDirNode) getParent();
				parentNode.adjustTree( l, ll );
			}

			for (int i = 0; i < l.usedSpace; i++) {
				if (l.branches[i] == page) {
					return l.pageNumber;
				}
			}

			for (int i = 0; i < ll.usedSpace; i++) {
				if (ll.branches[i] == page) {
					return ll.pageNumber;
				}
			}

			return -1;
		}
	}

	/**
	 * 处理结点的下溢
	 * @param hilbert the old hilbert value
	 * @return
	 */
	private HilbertRTDataNode[] handleUnderflow() {
		int min = Math.round(tree.getNodeCapacity() * tree.getFillFactor());
		System.out.println("min = " + min);
		
		HilbertRTDirNode parentNode = (HilbertRTDirNode) getParent();
		HilbertRTDataNode leftSibling = null;
		HilbertRTDataNode rightSibling = null;
		
		/*
		 * find the siblings.
		 */
		if ( parentNode != null ) {
			int position = parentNode.getPosition(this);
			if (position == 0 && position < parentNode.usedSpace - 1) {
				leftSibling = null;
				rightSibling = (HilbertRTDataNode)(parentNode.getChild(1));
			} else if (/*position > 0 && */position == parentNode.usedSpace - 1) {
				leftSibling = parentNode.usedSpace != 1 ? 
						(HilbertRTDataNode)(parentNode.getChild(parentNode.usedSpace - 2)) : null;
				rightSibling = null;
			} else {
				leftSibling = (HilbertRTDataNode)(parentNode.getChild(position - 1));
				rightSibling = (HilbertRTDataNode)(parentNode.getChild(position + 1));
			}
		}
		
		/*
		 * 分情况讨论：
		 * 1、左兄弟存在且左兄弟中已使用的entries > min，则可以从左兄弟借，并均匀分布
		 * 2、右兄弟存在且左兄弟中已使用的entries > min，则可以右左兄弟借，并均匀分布
		 * 3、左右兄弟都存在但已使用的entries < min，合并结点
		 * 4、左兄弟存在但右兄弟不存在则merge
		 * 5、右兄弟存在但左兄弟不存在则merge
		 * 6、左右兄弟都不存在
		 */
		if (leftSibling != null && leftSibling.usedSpace > min) {
			HilbertRTDataNode[] nodes = evenlyDistributed(leftSibling, this, null, null, Constants.NIL);
			merge = Constants.NO_MERGE;
			return nodes;
		} else if (rightSibling != null && rightSibling.usedSpace > min) {
			HilbertRTDataNode[] nodes = evenlyDistributed(this, rightSibling, null, null, Constants.NIL);
			merge = Constants.NO_MERGE;
			return nodes;
		} else if (leftSibling != null && rightSibling != null) {
			HilbertRTDataNode[] nodes = evenlyDistributed(leftSibling, this, rightSibling, null, Constants.NIL);
			merge = Constants.DOUBLE;
			return nodes;
		} else if (leftSibling != null && rightSibling == null) {
			HilbertRTDataNode node = merge(leftSibling, this); 
			merge = Constants.LEFT;
			return new HilbertRTDataNode[] { node, null };
		} else if (rightSibling != null) {
			HilbertRTDataNode node = merge(rightSibling, this); 
			merge = Constants.RIGHT;
			return new HilbertRTDataNode[] { node, null };
		} else {
			//TODO
//			System.out.println("Not reached.");
//			return null;
			merge = Constants.NONE;
			return new HilbertRTDataNode[] { this, null };
		}
	}
	
	private HilbertRTDataNode merge(HilbertRTDataNode node1, HilbertRTDataNode node2) {
		if (node1.usedSpace + node2.usedSpace > tree.getNodeCapacity())
			throw new IllegalArgumentException("node1 and node2 can not be merged.");
		
		HilbertRTDataNode newNode = new HilbertRTDataNode(tree, node1.parent, node1.pageNumber);
		List<RectangleAndPage> dataList = new ArrayList<RectangleAndPage>();
		for (int i = 0; i < node1.usedSpace; i++) {
			dataList.add(new RectangleAndPage(node1.datas[i], node1.branches[i]));
		}
		for (int i = 0; i < node2.usedSpace; i++) {
			dataList.add(new RectangleAndPage(node2.datas[i], node2.branches[i]));
		}
		Collections.sort(dataList);
		node1.clear();
		node2.clear();
		for (int i = 0; i < dataList.size(); i++) {
			newNode.addData(dataList.get(i).rec, dataList.get(i).page);
		}
		return newNode;
	}

	/**
	 * Distribute all the entries that come from @node1 and @node2
	 * evenly among the two nodes according to the Hilbert value.
	 * @param node1
	 * @param node2
	 * @param node3
	 * @param rectangle
	 * @param page
	 * @return
	 */
	private HilbertRTDataNode[] evenlyDistributed(HilbertRTDataNode node1, 
			HilbertRTDataNode node2, HilbertRTDataNode node3, Rectangle rectangle, int page) {
		if (node1 == null || node2 == null)
			throw new IllegalArgumentException("node1 or node2 can not be null.");
		
		List<RectangleAndPage> dataList = new ArrayList<RectangleAndPage>();
		for (int i = 0; i < node1.usedSpace; i++) {
			dataList.add(new RectangleAndPage(node1.datas[i], node1.branches[i]));
		}
		for (int i = 0; i < node2.usedSpace; i++) {
			dataList.add(new RectangleAndPage(node2.datas[i], node2.branches[i]));
		}
		if (node3 != null) {
			for (int i = 0; i < node3.usedSpace; i++) {
				dataList.add(new RectangleAndPage(node3.datas[i], node3.branches[i]));
			}
		}
		if (rectangle != null)
			dataList.add(new RectangleAndPage(rectangle, page));
		Collections.sort(dataList);
		node1.clear();
		node2.clear();
		for (int i = 0; i < dataList.size() / 2; i++) {
			node1.addData(dataList.get(i).rec, dataList.get(i).page);
		}
		for (int i = dataList.size() / 2; i < dataList.size(); i++) {
			node2.addData(dataList.get(i).rec, dataList.get(i).page);
		}
		return new HilbertRTDataNode[] { node1, node2 };
	}
	
	
	/**
	 * The overflow handling algorithm in the Hilbert R-tree treats the
	 * overflowing nodes either by moving some of the entries to one of the s-1
	 * cooperating siblings or splitting s nodes to s+1 nodes.
	 * 
	 * @param rectangle the @rectangle wants to insert
	 * @param page
	 * @return new leaf if split was inevitable.
	 */
	private HilbertRTDataNode[] handleOverflow(Rectangle rectangle, int page) {
		HilbertRTDirNode parentNode = (HilbertRTDirNode) getParent();
		HilbertRTDataNode leftSibling = null;
		HilbertRTDataNode rightSibling = null;
		/*
		 * find the siblings.
		 */
		if ( parentNode != null ) {
			if ( LHV == parentNode.LHV ) {
				leftSibling = parentNode.usedSpace != 1 ? 
						(HilbertRTDataNode)(parentNode.getChild(parentNode.usedSpace - 2)) : null;
				rightSibling = null;
			} else if ( LHV == parentNode.datas[0].getHilbertValue()) {
				leftSibling = null;
				rightSibling = (HilbertRTDataNode)(parentNode.getChild(1));
			} else {
				for (int i = 1; i < parentNode.usedSpace - 1; i++) {
					if (LHV == parentNode.datas[i].getHilbertValue()) {
						leftSibling = (HilbertRTDataNode)(parentNode.getChild(i - 1));
						rightSibling = (HilbertRTDataNode)(parentNode.getChild(i + 1));
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
			return evenlyDistributed(leftSibling, this, null, rectangle, page);
			
		} else if (rightSibling != null && rightSibling.usedSpace != tree.getNodeCapacity()) {
			return evenlyDistributed(this, rightSibling, null, rectangle, page);
			
		} else { //need to split
			HilbertRTDataNode newNode = new HilbertRTDataNode(tree, parent);
			return evenlyDistributed(this, newNode, null, rectangle, page);
		}
	}

	@Override
	public HilbertRTDataNode chooseLeaf(Rectangle rectangle) {
		return this;
	}

	/**
	 * In Hilbert R-tree we do not need to re-insert orphaned nodes,
	 * whenever a father node underflows. Instead, we borrow keys from
	 * the siblings or we merge an underflowing node with its siblings.
	 * We are able to do so, because the nodes have a clear ordering
	 * (Largest Hilbert Value LHV);<br>
	 * <b>Notice that</b>, for deletion, we need s cooperating siblings 
	 * while for insertion we need s-1;
	 * 
	 * @param rectangle want to delete
	 * @param TAG only as a tag
	 * @return
	 */
	protected int delete(Rectangle rectangle, int TAG) {
		int min = Math.round(tree.getNodeCapacity() * tree.getFillFactor());
		int pointer;
		int i = 0;
		for (i = 0; i < usedSpace; i++) {
			if (datas[i].equals(rectangle)) {
				break;
			}
		}
		
		/*
		 * 1. Find the host leaf.
		 * 2. Delete @rectangle from node L.
		 * 3. if L underflows, borrow some entries from s cooperating siblings.
		 * 		if all the siblings are ready to underflow, merge s+1 to s nodes,
		 * 		adjust the resulting nodes.
		 * 4. adjust MBR and LHV in parent levels.
		 * 		form a set S that contains L and its cooperating siblings(if underflow has occurred).
		 * 		invoke adjustTree(S).
		 */
		
		/* not found */
		if ( i == usedSpace ) {
			System.out.println("rectangle = " + rectangle + " can not be found.");
			return Constants.NIL;
		}
		
		pointer = branches[i];
		
		deleteData(i);
		if (usedSpace >= min) {
			tree.file.writeNode(this);
			HilbertRTDirNode parent = (HilbertRTDirNode) getParent();
			if (parent != null)
				parent.adjustTree(this, null);
		} else if (!isRoot()) {
			HilbertRTDataNode[] splitNodes = handleUnderflow();
			HilbertRTDataNode l = splitNodes[0];
			HilbertRTDataNode ll = splitNodes[1];
			
			switch (merge) {
			case Constants.NO_MERGE:
				tree.file.writeNode(l);
				tree.file.writeNode(ll);
				HilbertRTDirNode parentNode = (HilbertRTDirNode) getParent();
				parentNode.adjustTree(l, ll);
				break;
				
			case Constants.LEFT://right sibling is not exist, merge with left sibling.  
			case Constants.RIGHT://left sibling is not exist, merge with right sibling.
				parentNode = (HilbertRTDirNode) getParent();
				assert(parentNode != null);
				if (parentNode != null)
					System.out.println("parentNode != null");
				parentNode.deleteNode(parentNode.getPosition(this));
				tree.file.writeNode(l);
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
				System.out.println("Cannot be reached.");
				break;
				
			default:
					break;
			}
		}

		return pointer;
		
		
	}
	
	/**
	 * 插入Rectangle之后超过容量需要分裂
	 * 
	 * @param rectangle
	 * @param page
	 * @return
	 */
	@Deprecated
	public HilbertRTDataNode[] splitLeaf(Rectangle rectangle, int page) {
		int[][] group = null;

		switch (tree.getTreeType()) {
		case Constants.RTREE_LINEAR:
			break;
		case Constants.RTREE_QUADRATIC:
			group = quadraticSplit(rectangle, page);
			break;
		case Constants.RTREE_EXPONENTIAL:
			break;
		case Constants.RSTAR:
			break;
		default:
			throw new IllegalArgumentException("Invalid tree type.");
		}

		HilbertRTDataNode l = new HilbertRTDataNode(tree, parent);
		HilbertRTDataNode ll = new HilbertRTDataNode(tree, parent);

		int[] group1 = group[0];
		int[] group2 = group[1];

		for (int i = 0; i < group1.length; i++) {
			l.addData(datas[group1[i]], branches[group1[i]]);
		}

		for (int i = 0; i < group2.length; i++) {
			ll.addData(datas[group2[i]], branches[group2[i]]);
		}
		return new HilbertRTDataNode[] { l, ll };
	}
	
	/**
	 * 从叶节点中删除此条目rectangle
	 * <p>
	 * 先删除此rectangle，再调用condenseTree()返回删除结点的集合，把其中的叶子结点中的每个条目重新插入；
	 * 非叶子结点就从此结点开始遍历所有结点，然后把所有的叶子结点中的所有条目全部重新插入
	 * 
	 * @param rectangle
	 * @return The data pointer of the deleted entry.
	 */
	@Deprecated
	protected int delete(Rectangle rectangle) {
		for (int i = 0; i < usedSpace; i++) {
			if (datas[i].equals(rectangle)) {
				int pointer = branches[i];
				deleteData(i);
				tree.file.writeNode(this);// 删除数据后需要重新写入，即内容有变化需重新写入
				List<HilbertRTNode> deleteEntriesList = new ArrayList<HilbertRTNode>();
				condenseTree(deleteEntriesList);

				// 重新插入删除结点中剩余的条目
				for (int j = 0; j < deleteEntriesList.size(); j++) {
					HilbertRTNode node = deleteEntriesList.get(j);
					if (node.isLeaf())// 叶子结点，直接把其上的数据重新插入
					{
						for (int k = 0; k < node.usedSpace; k++) {
							tree.insert(node.datas[k], node.branches[k]);
						}
					} else {// 非叶子结点，需要先后序遍历出其上的所有结点
						List<HilbertRTNode> traNodes = tree.traversePostOrder(node);

						// 把其中的叶子结点中的条目重新插入
						for (int index = 0; index < traNodes.size(); index++) {
							HilbertRTNode traNode = traNodes.get(index);
							if (traNode.isLeaf()) {
								for (int t = 0; t < traNode.usedSpace; t++) {
									tree.insert(traNode.datas[t],
											traNode.branches[t]);
								}
							}
							if (node != traNode)
								tree.file.deletePage(traNode.pageNumber);
							else {
								System.out.println("两者相等。。。。");
							}
						}// end for
					}// end else
					tree.file.deletePage(node.pageNumber);
				}// end for

				return pointer;
			}// end if
		}// end for
		return Constants.NIL;
	}

	@Override
	protected HilbertRTDataNode findLeaf(Rectangle rectangle) {
		for (int i = 0; i < usedSpace; i++) {
//			if (datas[i].enclosure(rectangle)) {
			if (datas[i].equals(rectangle)) {
				return this;
			}
		}
		return null;
	}

	@Override
	protected HilbertRTDataNode chooseLeaf(Rectangle rectangle, long hilbertValue) {
		return this;
	}

}

class RectangleAndPage implements Comparable<RectangleAndPage> {
	Rectangle rec;
	int page;
	
	public RectangleAndPage(Rectangle rec, int page) {
		super();
		this.rec = rec;
		this.page = page;
	}

	@Override
	public int compareTo(RectangleAndPage o) {
		return Long.compare(rec.getHilbertValue(), o.rec.getHilbertValue());
	}

	@Override
	public String toString() {
		return "RectangleAndPage [rec=" + rec + ", page=" + page + "]";
	}
}
