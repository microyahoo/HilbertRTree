package com.njupt.hilbert.rtree;

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

	protected HilbertRTNode readNode(int page) throws PageFaultError {
		HilbertRTNode n = readFromCache(page);
		if (null != n) {
			return n;
		} else {
			return super.readNode(page);
		}
	}

	protected int writeNode(HilbertRTNode n) throws PageFaultError {
		int page = super.writeNode(n);
		writeToCache(n, page);
		return page;
	}

	private HilbertRTNode readFromCache(int page) {
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

	private void writeToCache(HilbertRTNode o, int page) {
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

	protected HilbertRTNode deletePage(int page) throws PageFaultError {
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
	HilbertRTNode object;

	public CachedObject(HilbertRTNode o, int page, int rank) {
		this.object = o;
		this.page = page;
		this.rank = rank;
	}
}