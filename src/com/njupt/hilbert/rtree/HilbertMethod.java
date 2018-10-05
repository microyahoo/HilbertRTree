package com.njupt.hilbert.rtree;

public class HilbertMethod {
	public static final int BITS_PER_DIM = 16;
	
	/**
	 * @param x1
	 * @param x2
	 * @return 获取坐标的Hilbert值
	 */
	public static long getHilbertValue(int x1, int x2) {
	    long res = 0;
	
	    for (int ix = BITS_PER_DIM - 1; ix >= 0; ix--) {
	        long h = 0;
	        long b1 = (x1 & (1 << ix)) >> ix;
	        long b2 = (x2 & (1 << ix)) >> ix;
	
	        if (b1 == 0 && b2 == 0) {
	            h = 0;
	        } else if (b1 == 0 && b2 == 1) {
	            h = 1;
	        } else if (b1 == 1 && b2 == 0) {
	            h = 3;
	        } else if (b1 == 1 && b2 == 1) {
	            h = 2;
	        }
	        res += h << (2 * ix);
	    }
	    return res;
	}
	
	/**
	 * 计算点(x,y)的Hilbert编码值，在len * len的格网中
	 * @param len len必须为2的幂次方
	 * @param x
	 * @param y
	 * @return
	 */
	public static int getHilbertValue(int len, int x, int y) {
		int nextlen = 0, tmp;
		if (len == 1)
			return 0;
		else {
			nextlen = len / 2;
			tmp = nextlen * nextlen;
			if (x < nextlen) {
				if (y < nextlen) {
					return getHilbertValue(nextlen, y, x);
				} else {
					return getHilbertValue(nextlen, x, y - nextlen) + tmp;
				}
			} else {
				if (y < nextlen) {
					return getHilbertValue(nextlen, nextlen - y - 1, 2 * nextlen - x - 1) 
							+ 3 * tmp;
				} else {
					return getHilbertValue(nextlen, x - nextlen, y - nextlen) + 2 * tmp;
				}
			}
		}
	}
	
	/**
	 * Find the Hilbert order (=vertex index) for the given grid cell 
	 * coordinates.
	 * @param x cell column (from 0)
	 * @param y cell row (from 0)
	 * @param r resolution of Hilbert curve (grid will have Math.pow(2,r) 
	 * rows and cols)
	 * @return Hilbert order 
	 */
	public static long encode(int x, int y, int r) {

	    int mask = (1 << r) - 1;
	    int hodd = 0;
	    int heven = x ^ y;
	    int notx = ~x & mask;
	    int noty = ~y & mask;
	    int temp = notx ^ y;

	    int v0 = 0, v1 = 0;
	    for (int k = 1; k < r; k++) {
	        v1 = ((v1 & heven) | ((v0 ^ noty) & temp)) >> 1;
	        v0 = ((v0 & (v1 ^ notx)) | (~v0 & (v1 ^ noty))) >> 1;
	    }
	    hodd = (~v0 & (v1 ^ x)) | (v0 & (v1 ^ noty));

	    return interleaveBits(hodd, heven);
	}

	/**
	 * Interleave the bits from two input integer values
	 * @param odd integer holding bit values for odd bit positions
	 * @param even integer holding bit values for even bit positions
	 * @return the integer that results from interleaving the input bits
	 *
	 * @todo: I'm sure there's a more elegant way of doing this !
	 */
	private static long interleaveBits(int odd, int even) {
		long val = 0;
	    // Replaced this line with the improved code provided by Tuska
	    // int n = Math.max(Integer.highestOneBit(odd), Integer.highestOneBit(even));
	    int max = Math.max(odd, even);
	    int n = 0;
	    while (max > 0) {
	        n++;
	        max >>= 1;
	    }

	    for (int i = 0; i < n; i++) {
	        int bitMask = 1 << i;
	        int a = (even & bitMask) > 0 ? (1 << (2 * i)) : 0;
	        int b = (odd & bitMask) > 0 ? (1 << (2 * i + 1)) : 0;
	        val += a + b;
	    }

	    return val;
	}
	
	public static void main(String[] args) {
		System.out.println(getHilbertValue(5, 4));
		System.out.println(getHilbertValue(16, 5, 4));
		System.out.println(getHilbertValue(15, 14));
		System.out.println(getHilbertValue(16, 15, 14));
		System.out.println("-------------------------------------------");
		System.out.println(getHilbertValue(5, 5));//34
		System.out.println(getHilbertValue(25, 5));//978
		System.out.println(getHilbertValue(25, 25));//642
		System.out.println(getHilbertValue(15, 15));//170
		System.out.println("-------------------------------------------");
		System.out.println(encode(5, 5, 3));//34
		System.out.println(encode(25, 5, 3));//978
		System.out.println(encode(25, 25, 3));//642
		System.out.println(encode(15, 15, 3));//170
		
		System.out.println("-------------------------------------------");
		System.out.println(encode(5, 5, 4));//34
		System.out.println(encode(25, 5, 4));//984
		System.out.println(encode(25, 25, 4));//642
		System.out.println(encode(15, 15, 4));//170

	}
}
