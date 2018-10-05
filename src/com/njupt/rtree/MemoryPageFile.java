package com.njupt.rtree;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;


public class MemoryPageFile extends PageFile {
	private static Logger logger = Logger.getLogger(MemoryPageFile.class);

	private Map<Integer, RTNode> file = new HashMap<>();

	/*
	 * 调用PageFile的initialize()初始化，并把缓存Map文件清空
	 */
	protected void initialize(RTree tree, int dimension, float fillFactor,
			int capacity, int treeType) {
		super.initialize(tree, dimension, fillFactor, capacity, treeType);
		file.clear();
	}

	@Override
	protected RTNode readNode(int page) throws PageFaultError {
		if (page < 0) {
			logger.error("Page number cannot be negative.",
					new IllegalArgumentException(
							"Page number cannot be negative."));
			throw new IllegalArgumentException(
					"Page number cannot be negative.");
		}

		RTNode ret = file.get(page);

		if (ret == null) {
			logger.error("Invalid page number request.", new PageFaultError(
					"Invalid page number request."));
			throw new PageFaultError("Invalid page number request.");
		}

		return ret;
	}

	@Override
	protected int writeNode(RTNode node) throws PageFaultError {
		if (node == null) {
			throw new IllegalArgumentException("Node cannot be null.");
		}

		/*
		 * 如果node结点所在的pageNumber < 0,则从缓存文件Map中从0开始查找第一个没使用的Key作为存储的索引
		 * 如果node结点所在的pageNumber >= 0，则直接取出其pageNumber作为存储的Key
		 */
		int i = 0;
		if (node.pageNumber < 0) {
			while (true) {
				if (!file.containsKey(i)) {
					node.pageNumber = i;//新加的
					break;
				}
				i++;
			}
		} else {
			i = node.pageNumber;
		}

		file.put(i, node);

		return i;
	}

	@Override
	protected RTNode deletePage(int page) throws PageFaultError {
		return file.remove(page);
	}

	public void dumpMemory() {
		for (RTNode n : file.values()) {
			System.out.println(n);
		}
	}
}
