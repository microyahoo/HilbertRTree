package com.njupt.btree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class BTNode {
	private List<Integer>	 keys; 			// 关键字,以非降序排序
	private List<BTNode>	 childNodes; 	// 内结点的子结点
	private BTNode 			 parent; 		// 父结点

	public BTNode() {
		keys = new LinkedList<Integer>();
		childNodes = new ArrayList<BTNode>();
	}

	public BTNode(BTNode parent) {
		this();
		this.parent = parent;
	}

	public BTNode getParentNode() {
		return parent;
	}

	public void setParentNode(BTNode parentNode) {
		this.parent = parentNode;
	}

	/**
	 * 获取所有关键字列表
	 * 
	 * @return List<Integer>
	 */
	public List<Integer> getKeys() {
		return keys;
	}

	/**
	 * 获取索引为index的关键字
	 * 
	 * @param index
	 *            int
	 * @return Integer
	 */
	public Integer getKey(int index) {
		return keys.get(index);
	}

	public void setKeys(List<Integer> keys) {
		this.keys = keys;
	}

	public void setChildNodes(List<BTNode> childNodes) {
		this.childNodes = childNodes;
	}

	public List<BTNode> getChildNodes() {
		return childNodes;
	}

	public BTNode getChild(int index) {
		return childNodes.get(index);
	}

	/**
	 * 设置父节点并批量添加孩子结点,添加后需要排序
	 * 
	 * @param children
	 * @return boolean
	 */
	public boolean addChildren(List<BTNode> children) {
		for (BTNode node : children) {
			node.setParentNode(this);
		}

		childNodes.addAll(children);

		// 排序
		Collections.sort(childNodes, new Comparator<BTNode>() {

			@Override
			public int compare(BTNode o1, BTNode o2) {
				return o1.getKeys().get(0).compareTo(o2.getKeys().get(0));
			}
		});

		return true;
	}

	public void addKey(Integer key) {
		SearchResult searchResult = searchKey(this, key);
		this.getKeys().add(searchResult.getIndex(), key);
	}

	/**
	 * 在结点node中搜索关键字key
	 * 
	 * @param node
	 *            BTNode
	 * @param key
	 *            Integer
	 * @return
	 */
	public SearchResult searchKey(BTNode node, Integer key) {
		boolean result = false;
		int index;

		List<Integer> keys = node.getKeys();

		int low = 0;
		int high = keys.size() - 1;
		int mid = 0;
		while (low <= high) {
			mid = (low + high) >>> 1;
			Comparable<Integer> midVal = keys.get(mid);
			int cmp = midVal.compareTo(key);

			if (cmp < 0)
				low = mid + 1;
			else if (cmp > 0)
				high = mid - 1;
			else {
				break;
			}
		}

		if (low <= high) {// 查找成功
			result = true;
			index = mid;
		} else {
			index = low;
		}

		return new SearchResult(result, index);
	}

	/**
	 * 设置父节点并添加孩子结点,添加后需要排序
	 * 
	 * @param child
	 * @return boolean
	 */
	public boolean addChild(BTNode child) {
		child.setParentNode(this);

		childNodes.add(child);

		// 排序
		Collections.sort(childNodes, new Comparator<BTNode>() {

			@Override
			public int compare(BTNode o1, BTNode o2) {
				return o1.getKeys().get(0).compareTo(o2.getKeys().get(0));
			}
		});

		return true;
	}

	/**
	 * 删除孩子结点
	 * 
	 * @param childNode
	 */
	public void removeChild(BTNode childNode) {
		childNodes.remove(childNode);
	}

	/**
	 * @return 结点中关键字个数
	 */
	public int sizeOfKeys() {
		return keys.size();
	}

	/**
	 * @return 孩子个数
	 */
	public int sizeOfChildren() {
		return childNodes.size();
	}

}
