package com.njupt.rtree;

import java.util.HashMap;
import java.util.Map;


public class CachedPersistentPageFile extends PersistentPageFile {
	private Map<Integer, CachedObject> cache;
	private int usedSpace = 0;
	private int cacheSize = 0;

	public CachedPersistentPageFile(String fileName, int cacheSize) {
		super(fileName);
		this.cacheSize = cacheSize;
		cache = new HashMap<Integer, CachedObject>(cacheSize);
	}

	protected RTNode readNode(int page) throws PageFaultError {
		RTNode n = readFromCache(page);
		if (null != n) {
			return n;
		} else {
			return super.readNode(page);
		}
	}

	protected int writeNode(RTNode n) throws PageFaultError {
		int page = super.writeNode(n);
		writeToCache(n, page);
		return page;
	}

	private RTNode readFromCache(int page) {
		CachedObject c = cache.get(page);
		if (c != null) {
			int rank = c.rank;
			for (CachedObject co : cache.values()) {
				if (co.rank > rank) {
					co.rank--;
				} else if (co.rank == rank) {
					co.rank = usedSpace - 1;
				}
			}
			return c.object;
		} else {
			return null;
		}
	}

	private void writeToCache(RTNode o, int page) {
		CachedObject c = cache.get(page);

		if (null != c) {
			c.object = o;
			int rank = c.rank;
			for (CachedObject co : cache.values()) {
				if (co.rank > rank) {
					co.rank--;
				} else if (co.rank == rank) {
					co.rank = usedSpace - 1;
				}
			}
		} else if (usedSpace < cacheSize) {// cache is not full
			cache.put(page, new CachedObject(o, page, usedSpace));
			usedSpace++;
			return;
		} else {// cache is full
			for (CachedObject co : cache.values()) {
				if (co.rank == 0) {
					cache.remove(co.page);
					break;
				}
			}
			for (CachedObject co : cache.values()) {
				co.rank--;
			}

			cache.put(page, new CachedObject(o, page, usedSpace - 1));
		}
	}

	protected RTNode deletePage(int page) throws PageFaultError {
		CachedObject c = cache.get(page);
		if (c != null) {
			int rank = c.rank;

			for (CachedObject co : cache.values()) {
				if (co.rank > rank) {
					co.rank--;
				}
			}

			cache.remove(page);
			usedSpace--;
		}
		return super.deletePage(page);
	}
}

class CachedObject {
	int rank = 0;
	int page = -1;
	RTNode object;

	public CachedObject(RTNode o, int page, int rank) {
		this.object = o;
		this.page = page;
		this.rank = rank;
	}
}