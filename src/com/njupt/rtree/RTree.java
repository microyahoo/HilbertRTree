package com.njupt.rtree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * 
 * @ClassName RTree
 * @Description
 */
public class RTree {
	public static double alpha_dist;
	public static int numOfClusters = 0;
	
	/**
	 * 数据被存储在page文件
	 */
	protected PageFile file = null;
	
	
	/**
	 * 为PageFile初始化，创建根节点并将根节点写入file中
	 * 
	 * @param dimension
	 * @param fillFactor
	 * @param capacity
	 * @param file
	 *            内存或者文件
	 * @param treeType
	 */
	public RTree(int dimension, float fillFactor, int capacity, PageFile file,
			int treeType) {
		if (dimension <= 1) {
			throw new IllegalArgumentException(
					"Dimension must be larger than 1.");
		}

		if (fillFactor < 0 || fillFactor > 0.5) {
			throw new IllegalArgumentException(
					"Fill factor must be between 0 and 0.5.");
		}

		if (capacity <= 1) {
			throw new IllegalArgumentException(
					"Capacity must be larger than 1.");
		}

		if (file.tree != null) {
			throw new IllegalArgumentException(
					"PageFile already in use by another rtree instance.");
		}
		
		file.initialize(this, dimension, fillFactor, capacity, treeType);
		this.file = file;

		// 每个结点必须存储在唯一的page，根节点总是存储在page 0.
		RTDataNode root = new RTDataNode(this, Constants.NIL, 0);
		file.writeNode(root);

	}

	public RTree(PageFile file) {
		if (file.tree != null) {
			throw new IllegalArgumentException(
					"PageFile already in use by another rtree instance.");
		}

		if (file.treeType == -1) {
			throw new IllegalArgumentException(
					"PageFile is empty. Use some other RTree constructor.");
		}

		file.tree = this;
		this.file = file;
	}

	/**
	 * 默认写入内存
	 * 
	 * @param dimension
	 * @param fillFactor
	 * @param capacity
	 * @param treeType
	 */
	public RTree(int dimension, float fillFactor, int capacity, int treeType) {
		this(dimension, fillFactor, capacity, new MemoryPageFile(), treeType);
	}

	/**
	 * @return RTree的维度
	 */
	public int getDimension() {
		return file.dimension;
	}

	public int getPageSize() {
		return file.pageSize;
	}

	public float getFillFactor() {
		return file.fillFactor;
	}

	/**
	 * @return 返回结点容量
	 */
	public int getNodeCapacity() {
		return file.nodeCapacity;
	}

	/**
	 * @return 返回树的类型
	 */
	public int getTreeType() {
		return file.treeType;
	}

	/**
	 * Returns the level of the root Node, which signifies the level of the
	 * whole tree. Loads one page into main memory.
	 */
	public int getTreeLevel() {
		return file.readNode(0).getLevel();// 根节点总是存储在page 0.
	}

	/**
	 * <b>步骤I1：</b>为新记录寻找保存位置——调用算法ChooseLeaf，选择一个用于保存E的叶节点L。<br>
	 * <b>步骤I2：</b>将记录存入叶节点——如果节点L中有存储空间，则将E保存在里面。否则使用算法
	 * SplitNode执行节点分裂操作，节点L将变成两个新节点L和LL，L和LL中保存了E和旧L中的所有条目。<br>
	 * <b>步骤I3：</b>向上传递树的变化——对节点L调用算法AdjustTree。如果步骤I2中进行过节点分裂操作，
	 * 那还要对LL调用算法AdjustTree。<br>
	 * <b>步骤I4：</b>树的长高——如果节点的分裂操作向上传递导致根节点分裂，那就要新建一个根节点。
	 * 新的根节点的两个子节点就是旧子节点分裂后形成的两个节点。<br>
	 * <p>
	 * 向Rtree中插入Rectangle<br>
	 * 1、先找到合适的叶节点 <br>
	 * 2、再向此叶节点中插入<br>
	 * 
	 * @param rectangle
	 * @param page
	 * @return rectangle被插入的叶子结点的pageNumber。(the parent of the data entry.)
	 */
	public int insert(Rectangle rectangle, int page) {
		if (rectangle == null)
			throw new IllegalArgumentException("Rectangle cannot be null.");

		if (rectangle.getHigh().getDimension() != getDimension()) {
			throw new IllegalArgumentException(
					"Rectangle dimension different than RTree dimension.");
		}

		RTNode root = file.readNode(0);

		RTDataNode leaf = root.chooseLeaf(rectangle);

		return leaf.insert(rectangle, page);
	}
	
	/**
	 * 从R树中删除Rectangle
	 * <p>
	 * 1、寻找包含记录的结点--调用算法findLeaf()来定位包含此记录的叶子结点L，如果没有找到则算法终止。<br>
	 * 2、删除记录--将找到的叶子结点L中的此记录删除<br>
	 * 3、调用算法condenseTree<br>
	 * 
	 * @param rectangle
	 * @return 被删除条目的数据指针
	 */
	public int delete(Rectangle rectangle) {
		if (rectangle == null) {
			throw new IllegalArgumentException("Rectangle cannot be null.");
		}

		if (rectangle.getHigh().getDimension() != getDimension()) {
			throw new IllegalArgumentException(
					"Rectangle dimension different than RTree dimension.");
		}

		RTNode root = file.readNode(0);

		RTDataNode leaf = root.findLeaf(rectangle);

		if (leaf != null) {
			return leaf.delete(rectangle);
		}

		return -1;
	}

	/**
	 * 从给定的结点root开始后序遍历所有的结点
	 * 
	 * @param root
	 * @return 所有遍历的结点集合
	 */
	public List<RTNode> traversePostOrder(RTNode root) {
		if (root == null)
			throw new IllegalArgumentException("Node cannot be null.");

		List<RTNode> list = new ArrayList<RTNode>();

		if (!root.isLeaf()) {
			for (int i = 0; i < root.usedSpace; i++) {
				List<RTNode> a = traversePostOrder(((RTDirNode) root)
						.getChild(i));
				for (int j = 0; j < a.size(); j++) {
					list.add(a.get(j));
				}
			}
		}

		list.add(root);

		return list;
	}

	/**
	 * @param rectangle
	 * @return 返回与给定的rectangle相交的所有结点的枚举
	 */
	public Enumeration<RTNode> intersection(Rectangle rectangle) {
		class IntersectionEnum implements Enumeration<RTNode> {
			private List<RTNode> nodes;

			private int index = 0;

			private boolean hasNext = true;

			public IntersectionEnum(Rectangle rectangle) {
				nodes = intersection(rectangle, file.readNode(0));
				if (nodes.isEmpty()) {
					hasNext = false;
				}
			}

			@Override
			public boolean hasMoreElements() {
				return hasNext;
			}

			@Override
			public RTNode nextElement() {
				if (!hasNext) {
					throw new NoSuchElementException("intersection");
				}

				RTNode node = nodes.get(index);
				index++;

				if (index == nodes.size()) {
					hasNext = false;
				}
				return node;
			}

		}
		return new IntersectionEnum(rectangle);
	}

	/**
	 * @param rectangle
	 * @param node
	 *            当前结点
	 * @return 返回与给定的rectangle相交的所有叶子结点的集合
	 */
	public List<RTNode> intersection(Rectangle rectangle, RTNode node) {
		if (rectangle == null || node == null) {
			throw new IllegalArgumentException("Arguments cannot be null.");
		}
		if (rectangle.getDimension() != getDimension()) {
			throw new IllegalArgumentException(
					"Rectangle dimension different than Rtree dimension.");
		}

		List<RTNode> list = new ArrayList<RTNode>();

		if (node.getNodeRectangle().isIntersection(rectangle)) {
			if (node.isLeaf())// 新加的
			{
				list.add(node);
			} else
			// if(! node.isLeaf())
			{
				for (int i = 0; i < node.usedSpace; i++) {
					if (node.datas[i].isIntersection(rectangle)) {
						// 递归调用
						List<RTNode> nodes = intersection(rectangle,
								((RTDirNode) node).getChild(i));
						for (int j = 0; j < nodes.size(); j++) {
							list.add(nodes.get(j));
						}
					}
				}
			}
		}

		return list;
	}

	/**
	 * @param rectangle
	 * @param node
	 *            当前结点
	 * @return 返回与给定的rectangle相交的所有结点的集合
	 */
	public List<RTNode> intersection_All(Rectangle rectangle, RTNode node) {
		if (rectangle == null || node == null) {
			throw new IllegalArgumentException("Arguments cannot be null.");
		}
		if (rectangle.getDimension() != getDimension()) {
			throw new IllegalArgumentException(
					"Rectangle dimension different than Rtree dimension.");
		}

		List<RTNode> list = new ArrayList<RTNode>();

		if (node.getNodeRectangle().isIntersection(rectangle)) {
			list.add(node);

			if (!node.isLeaf()) {
				for (int i = 0; i < node.usedSpace; i++) {
					if (node.datas[i].isIntersection(rectangle)) {
						// 递归调用
						List<RTNode> nodes = intersection_All(rectangle,
								((RTDirNode) node).getChild(i));
						for (int j = 0; j < nodes.size(); j++) {
							list.add(nodes.get(j));
						}
					}
				}
			}
		}

		return list;
	}

	/**
	 * @param rectangle
	 *            给定的rectangle
	 * @param node
	 *            活动结点
	 * @return 返回所有与给定rectangle相交的叶子结点中的rectangle组成的集合,封装成Data集合
	 */
	public List<Data> intersection_Rectangles(Rectangle rectangle, RTNode node) {
		if (rectangle == null || node == null) {
			throw new IllegalArgumentException("Arguments cannot be null.");
		}
		if (rectangle.getDimension() != getDimension()) {
			throw new IllegalArgumentException(
					"Rectangle dimension different than Rtree dimension.");
		}

		List<Data> list = new ArrayList<Data>();

		if (node.getNodeRectangle().isIntersection(rectangle)) {
			if (node.isLeaf())// 新加的
			{
				Rectangle[] rectangles = new Rectangle[node.usedSpace];
				for (int i = 0; i < rectangles.length; i++) {
					rectangles[i] = node.datas[i];
					if (rectangle.isIntersection(rectangles[i]))
						list.add(new Data(rectangles[i], i));
				}
			} else {
				for (int i = 0; i < node.usedSpace; i++) {
					if (node.datas[i].isIntersection(rectangle)) {
						// 递归调用
						List<Data> nodes = intersection_Rectangles(rectangle,
								((RTDirNode) node).getChild(i));
						for (int j = 0; j < nodes.size(); j++) {
							list.add(nodes.get(j));
						}
					}
				}
			}
		}

		return list;
	}

	/**
	 * 
	 * @param point
	 *            查询Point
	 * @return 返回与给定的point最近的Rectangle，封装成Data类型的集合
	 */
	public List<Data> nearestNeighbor(Point point) {
		return nearestNeighborSearch(file.readNode(0), point,
				Float.POSITIVE_INFINITY, false);
		// return nearestNeighborSearch(root, point, 5000);
	}

	/**
	 * 给定查询点，查询指定范围内的n个对象
	 * 
	 * @param point
	 *            查询点.
	 * @param range
	 *            搜索范围.
	 * @param n
	 *            查询对象的数目.
	 * @return the leaf elements found near the point.The length of the returned
	 *         array would be equal to <code>n</code>.
	 */
	public Data[] nearestSearch(Point queryPoint, float range, int n) {
		if (n <= 0 || range < 0 || queryPoint == null)
			throw new IllegalArgumentException(
					"RTree.nearestSearch: Illegal arguments");

		Data[] datas = new Data[n];

		// 固定nearest，查询出在指定范围内的所有对象
		List<Data> dataList = nearestNeighborSearch(file.readNode(0),
				queryPoint, range, true);

		// 对查询的结果按minDist排序
		Collections.sort(dataList, new Comparator<Data>() {

			@Override
			public int compare(Data o1, Data o2) {
				float f = o1.minDist - o2.minDist;
				if (f > 0)
					return 1;
				else if (f < 0)
					return -1;
				return 0;
			}
		});

		// 取前n个
		if (dataList.size() < n) {
			for (int i = 0; i < dataList.size(); i++) {
				datas[i] = dataList.get(i);
			}
		} else {
			for (int i = 0; i < n; i++) {
				datas[i] = dataList.get(i);
			}
		}
		return datas;

	}

	/**
	 * See the paper "<b><u>Nearest Neighbor Queries</u></b>"<br>
	 * 1、如果一个点P到MBR M的距离MINDIST(P,M)大于点P到另一个MBR M’的距离MINMAXDIST(P,M’)，
	 * 则M将被剪掉。因为它不可能包含NN（根据定理1和定理2）。我们利用向下剪枝。<br>
	 * 2、如果一个点P到一给定对象O的实际距离大于点P到MBR M的距离MINMAXDIST (P,M)，则M将被剪掉（实际
	 * 中是作为NN距离的估计被替换掉）。因为M包含对象O将离P更近（根据定理2）。这被用于向下剪枝。<br>
	 * 3、如果每个MBR的距离MINDIST(P,M)大于点P到一给定对象O的实际距离，则此M将被剪掉，因为它不可能包含
	 * 更靠近O的一个对象（定理1）。这被用于向上剪枝。<br>
	 * <p>
	 * 
	 * 该算法从根结点开始向下访问各层MBR。算法中首先假定最近邻距离Nearest为无穷大。随着搜索的下降，
	 * 对每个最新访问的<b><u>非叶结点</u></b>首先计算出其中所有 的MBR的MINDIST值，并将这些值
	 * 排序后放入活动分支链表ABL(Active Branch List)中，接着对ABL运用剪枝策略1和策略2来移
	 * 除不必要的分支。算法在ABL上重复进行直至ABL为空。每次重复过程中算法都选择链表中的下一个分支，并
	 * 在与该分支的MBR相应的结点上递归进行以上过程。对于<b><u>叶结点</u></b>算法对每个对象调用一
	 * 个特定类型的距离函数，并将计算出的值逐个与Nearest比较，选择其中更小的值替换Nearest。在递归
	 * 过程返回时使用这个新的最近邻距离的估计值作为判断条件，采用策略3剪枝以移除ABL中所有 MINDIST值 大于Nearest的MBR所在的分支。
	 * 
	 * @param node
	 *            活动分支中的当前结点
	 * @param queryPoint
	 *            查询Point
	 * @param nearest
	 *            point到当前Rectangle的最近距离
	 * @param nearestIsFixed
	 *            nearest是否固定不变
	 * @return
	 */
	protected List<Data> nearestNeighborSearch(RTNode node, Point queryPoint,
			float nearest, boolean nearestIsFixed) {
		List<Data> ret = new ArrayList<Data>();
		Rectangle rectangle;

		if (node.isLeaf())// node为叶子结点则比较Point到每个条目的最小距离
		{
			for (int i = 0; i < node.usedSpace; i++) {
				float dist = node.datas[i].getMinDist(queryPoint);
				if (dist < nearest) {
					rectangle = node.datas[i];
					if (!nearestIsFixed)// nearest不固定
						nearest = dist;// 可以尝试注释此处
					ret.add(new Data(rectangle, dist, i));
				}
			}
		} else {// node为非叶子结点
				// 1、生成分支列表
				// 对每个最新访问的非叶结点首先计算出其中所有
				// 的MBR的MINDIST、MINMAXDIST值，并将这些值排序后放入活动分支链表ABL(Active Branch
				// List)中
			BranchList[] branchList = new BranchList[node.usedSpace];
			for (int i = 0; i < node.usedSpace; i++) {
				RTNode rtNode = ((RTDirNode) node).getChild(i);
				branchList[i] = new BranchList(rtNode, rtNode
						.getNodeRectangle().getMinDist(queryPoint), rtNode
						.getNodeRectangle().getMinMaxDist(queryPoint));
			}

			// 2、排序分支列表
			// 把给定的point到node结点的孩子结点的MINDIST按从小到大顺序排序
			Arrays.sort(branchList, new BranchListMinDistComparator());

			// 3、剪枝分支列表
			int last = pruneBranchList(nearest, branchList, branchList.length);

			if (last != branchList.length)// 测试用
				System.out.println("不相等！！！ last = " + last + ",length = "
						+ branchList.length);

			for (int i = 0; i < last; i++) {
				if (branchList[i].minDist < nearest) {
					List<Data> nonLeaf = nearestNeighborSearch(
							branchList[i].node, queryPoint, nearest,
							nearestIsFixed);

					if (nonLeaf != null && nonLeaf.size() > 0) {
						for (int j = 0; j < nonLeaf.size(); j++)
							ret.add(nonLeaf.get(j));
					}

					int t = last;// 测试用
					last = pruneBranchList(nearest, branchList, last);
					if (last != t)// 测试用
						System.out.println("****不相等！！！ last = " + last
								+ ",length = " + t);
				}// end if
			}// end for
		}// end else

		return ret;
	}

	/**
	 * 对按MINDIST排序后的branchList进行剪枝
	 * <p>
	 * <b><u>剪枝策略1：</u></b>如果MINDIST(P,M1) >
	 * MINMAXDIST(P,M2)，则M1将被剪掉。因为它不可能包含NN（根据定理1和定理2）<br>
	 * <b><u>剪枝策略2:</u></b>如果一个点P到一给定对象O的实际距离大于点P到MBR M的距离MINMAXDIST
	 * (P,M)，则M将被剪掉（实际中是作为NN距离的估计被替换掉）。<br>
	 * <b><u>剪枝策略3:</u></b>如果每个MBR的距离MINDIST(P,M)大于点P到一给定对象O的实际距离，则此M将被剪掉，
	 * 因为它不可能包含更靠近O的一个对象（定理1）。<br>
	 * 
	 * @param nearest
	 * @param branchList
	 * @param usedSpace
	 * @return 如果返回值为Last，则搜索时只需要查找branchList中[0,Last]，其余被剪掉
	 */
	public int pruneBranchList(float nearest, BranchList[] branchList,
			int usedSpace) {
		int last = usedSpace;
		int i;

		// 剪枝策略1：如果MINDIST(P,M1) >
		// MINMAXDIST(P,M2)，则M1将被剪掉。因为它不可能包含NN（根据定理1和定理2）
		for (i = 0; i < last; i++) {
			// 先和最大的MINDIST比较，如果其MINMAXDIST小于最大的MINDIST则查找出具体位置
			if (branchList[i].minMaxDist < branchList[last - 1].minDist) {
				for (int j = 0; j < last; j++) {
					if ((i != j)
							&& (branchList[j].minDist > branchList[i].minMaxDist)) {
						last = j;
						break;
					}
				}
			}
		}

		// 剪枝策略2:如果一个点P到一给定对象O的实际距离大于点P到MBR M的距离MINMAXDIST
		// (P,M)，则M将被剪掉（实际中是作为NN距离的估计被替换掉）。
		// nearest > MINMAXDIST(P,M)
		// -> nearest = MIMMAXDIST(P,M)
		for (i = 0; i < last; i++) {
			if (nearest > branchList[i].minMaxDist)
				nearest = branchList[i].minMaxDist;
		}

		// 剪枝策略3:如果每个MBR的距离MINDIST(P,M)大于点P到一给定对象O的实际距离，则此M将被剪掉，因为它不可能包含更靠近O的一个对象（定理1）。
		// nearest < MINDIST(P,M)
		for (i = 0; i < last && nearest >= branchList[i].minDist; i++)
			;

		last = i;

		return last;
	}

	public List<RTNode> traverseByLevel() {
		RTNode root = file.readNode(0);

		if (root == null)
			throw new IllegalArgumentException("Node cannot be null.");

		List<RTNode> list = traverseByLevel(root);

		return list;
	}

	/**
	 * @param root
	 *            遍历开始的结点
	 * @return 返回自底向上，自左向右的包含所有结点的集合
	 */
	public List<RTNode> traverseByLevel(RTNode root) {
		if (root == null)
			throw new IllegalArgumentException("Node cannot be null.");

		List<RTNode> ret = new ArrayList<RTNode>();
		List<RTNode> list = traversePostOrder(root);

		for (int i = getTreeLevel(); i >= 0; i--) {
			for (int j = 0; j < list.size(); j++) {
				RTNode n = list.get(j);
				if (n.getLevel() == i) {
					ret.add(n);
				}
			}
		}

		return ret;
	}
}
