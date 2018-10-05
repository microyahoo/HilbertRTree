package com.njupt.rtree;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName RTDataNode
 * @Description
 */
public class RTDataNode extends RTNode {
	public RTDataNode(RTree tree, int parent, int pageNumber) {
		super(tree, parent, pageNumber, 0);// 叶子结点默认属于0层
	}

	public RTDataNode(RTree tree, int parent) {
		super(tree, parent, -1, 0);// 先传递一个默认值负数（-1）作为pageNumber
	}

	/**
	 * 在叶节点中插入Rectangle，插入后如果其父节点不为空则需要向上调整树直到根节点；<br>
	 * 若插入Rectangle之后超过结点容量则需要分裂结点
	 * 
	 * @param rectangle
	 * @return rectangle被插入的叶节点的pageNumber
	 */
	public int insert(Rectangle rectangle, int page) {
		if (usedSpace < tree.getNodeCapacity()) {
			datas[usedSpace] = rectangle;
			branches[usedSpace] = page;
			usedSpace++;
			tree.file.writeNode(this);// 更新文件
			RTDirNode parent = (RTDirNode) getParent();

			if (parent != null)
				parent.adjustTree(this, null);
			return pageNumber;

		} else {// 超过结点容量
			RTDataNode[] splitNodes = splitLeaf(rectangle, page);
			RTDataNode l = splitNodes[0];
			RTDataNode ll = splitNodes[1];

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
				RTDirNode r = new RTDirNode(tree, Constants.NIL, 0, 1);
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
				RTDirNode parentNode = (RTDirNode) getParent();
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
	 * 插入Rectangle之后超过容量需要分裂
	 * 
	 * @param rectangle
	 * @param page
	 * @return
	 */
	public RTDataNode[] splitLeaf(Rectangle rectangle, int page) {
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

		RTDataNode l = new RTDataNode(tree, parent);
		RTDataNode ll = new RTDataNode(tree, parent);

		int[] group1 = group[0];
		int[] group2 = group[1];

		for (int i = 0; i < group1.length; i++) {
			l.addData(datas[group1[i]], branches[group1[i]]);
		}

		for (int i = 0; i < group2.length; i++) {
			ll.addData(datas[group2[i]], branches[group2[i]]);
		}
		return new RTDataNode[] { l, ll };
	}

	@Override
	public RTDataNode chooseLeaf(Rectangle rectangle) {
		return this;
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
	protected int delete(Rectangle rectangle) {
		for (int i = 0; i < usedSpace; i++) {
			if (datas[i].equals(rectangle)) {
				int pointer = branches[i];
				deleteData(i);
				tree.file.writeNode(this);// 删除数据后需要重新写入，即内容有变化需重新写入
				List<RTNode> deleteEntriesList = new ArrayList<RTNode>();
				condenseTree(deleteEntriesList);

				// 重新插入删除结点中剩余的条目
				for (int j = 0; j < deleteEntriesList.size(); j++) {
					RTNode node = deleteEntriesList.get(j);
					if (node.isLeaf())// 叶子结点，直接把其上的数据重新插入
					{
						for (int k = 0; k < node.usedSpace; k++) {
							tree.insert(node.datas[k], node.branches[k]);
						}
					} else {// 非叶子结点，需要先后序遍历出其上的所有结点
						List<RTNode> traNodes = tree.traversePostOrder(node);

						// 把其中的叶子结点中的条目重新插入
						for (int index = 0; index < traNodes.size(); index++) {
							RTNode traNode = traNodes.get(index);
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
	protected RTDataNode findLeaf(Rectangle rectangle) {
		for (int i = 0; i < usedSpace; i++) {
			if (datas[i].enclosure(rectangle)) {
				return this;
			}
		}
		return null;
	}

}
