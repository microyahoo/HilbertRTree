package com.njupt.hilbert.rtree;

import edu.rit.mp.*;
import edu.rit.mp.buf.*;
import edu.rit.pj.*;
import edu.rit.util.Range;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

/**
 * A cluster implementation of a Hilbert R-Tree.
 */
public class HilbertRTreeClu
{
    /** The underlying Hilbert RTree that all of the nodes will have. */
	static HilbertRTree tree = 
			new HilbertRTree(2, 0.4f, 5, Constants.RTREE_QUADRATIC, 2);
    /**
     * The hilbert RTree's root node
     */
    static HilbertRTNode root;
    
    /** Environmental data */
    static Comm world;
    static int rank;
    static int size;
    
    /** store all rectangles. */
    static List<Rectangle> list = new ArrayList<Rectangle>();
    static Rectangle[] rectangles = null;

    static CountDownLatch startGate = new CountDownLatch(1);

    /** Command-line arguments. */
    static String[] arguments;
    
    static ObjectBuf<Rectangle>[] slices;
    static ObjectBuf<Rectangle> myslices;
    static IntegerItemBuf mySliceSize = IntegerBuf.buffer();
    
    static Range[] sliceRanges;
    
    /** storage all the root nodes. */
    static ObjectBuf<HilbertRTNode>[] sliceNodes;
    static ObjectBuf<HilbertRTNode> myslicesNode;
    
    /**
     * Starts running the Hilbert RTree cluster.
     */
    public static void run(String[] args) throws Exception
    {
        long startTime = System.currentTimeMillis();
        
        System.out.println("startTime = " + startTime);
        Comm.init( args );
        System.out.println("After comm init....");
        world = Comm.world();
        System.out.println("world = " + world);
        rank = world.rank();
        size = world.size();
        System.out.println("rank = " + rank);
        System.out.println("size = " + size);
        arguments = args;
        
        /*
         * A parallel team of two threads executes two parallel sections concurrently, 
         * the master section and the worker section. The master section first call 
         * the masterSection(), the worker section blocks. workers由于调用receive()
         * 方法而阻塞，当masterSection()方法调用结束后，the worker section解除阻塞，首先将
         * 划分的大小发送到集群的每台机器(workers)上，此时workers的receive()方法解除阻塞，并
         * 进行myslices的初始化，接着每台机器(workers)都调用workerSection()，master把划分
         * 的数据发送到每台机器(workers)上，每台机器在接收到数据之后分别进行索引的构建
         */
        if ( rank == 0 ) {
        	sliceNodes = new ObjectBuf[size];
        	for (int i = 0; i < size; i++) {
            	sliceNodes[i] = new ObjectItemBuf<HilbertRTNode>();
            }
        	new ParallelTeam(2).execute(new ParallelRegion() {
				
				@Override
				public void run() throws Exception {
					//the master section
					execute(new ParallelSection() {
						
						@Override
						public void run() throws Exception {
							masterSection();
//							world.barrier(Tag.FIRST);
//							startGate.countDown();
							barrier();
							System.out.println("rank = " + rank + " after masterSection....");
						}
					}, 
					//the worker section
					new ParallelSection() {
						
						@Override
						public void run() throws Exception {
//							startGate.await();
							barrier();
							
							for (int i = 1; i < size; i++) {
								world.send(i, Tag.SIZE, new IntegerItemBuf(slices[i].length()), null);
								System.out.println("rank = " + rank + ", send the size = " + slices[i].length());
							}
							System.out.println("rank = " + rank + ", after send the size. ");
							
							System.out.println("rank = " + rank + " before workerSection");
							workerSection();
							System.out.println("rank = " + rank + " after workerSection");
						}
					});
				}
			});
        	
        	
        } else {
//        	System.out.println("rank = " + rank + ", blocked before the Tag.FIRST.");
//        	world.barrier(Tag.FIRST);
//        	System.out.println("rank = " + rank + ", blocked after the Tag.FIRST.");
        	
        	System.out.println("rank = " + rank + ", blocked before the Tag.SIZE.");
        	world.receive(0, Tag.SIZE, mySliceSize);
        	System.out.println("rank = " + rank + ", blocked after the Tag.SIZE.");
        	
    		System.out.println("rank = " + rank + ", mySliceSize = " + mySliceSize.item);
    		Range ran = new Range(0, mySliceSize.item - 1);
    		Rectangle[] recs = new Rectangle[mySliceSize.item];
    		myslices = new ObjectArrayBuf_1<Rectangle> ( recs, ran );//需要注意
        	
			System.out.println("rank = " + rank + " before workerSection");
        	workerSection();
        	System.out.println("rank = " + rank + " after workerSection");
        }
        
//        System.out.println("rank = " + rank + ", mySlices.length = " + myslices.length());//暂时注释
//        System.out.println("\nrank = " + rank + ", now print all the rectangles: ");//暂时注释
        for (int i = 0; i < myslices.length(); i++) {
//        	System.out.println("rank = " + rank + ", "+ myslices.get(i));//暂时注释
        	put(myslices.get(i));
        }
        
        /*
         * R-tree合并过程
         * 首先将集群中的每台主机中构建的R-tree的根节点发
         * 送到rank==0的机器上然后构建最终的R-tree索引
         */
        root = tree.file.readNode(0);
        myslicesNode = ObjectBuf.buffer(root);
//        System.out.println("rank = " + rank + ", myslicesNode = " + myslicesNode.get(0));
        
//        System.out.println("++++++++++++++++++++++++++++++++++before++++++++++++++++++++++++++++++++++++++=");
        
        world.gather(0, myslicesNode, sliceNodes);
//        if (rank == 0)
//	        for (int i = 0; i < size; i++) {
//	        	System.out.println(sliceNodes[i].get(0));
//	        }
		System.out.println("++++++++++++++++++++++++++++++++++++after++++++++++++++++++++++++++++++++++++=");
		
    }
    
    private static void workerSection() throws IOException {
    	System.out.println("rank = " + rank + ", worker section....*****************.");
    	
    	if (rank != 0) {
    		world.scatter( 0, Tag.DATA, null, myslices );
    	}
    	else {
    		world.scatter( 0, Tag.DATA, slices, myslices );
		}
//    	System.out.println("rank = " + rank + ", myslices = " + myslices + ", myslices.length() = " + myslices.length());//暂时注释
    	
	}

    /**
     * 由于采用Master-Worker结构，因此此方法为master结点调用，功能如下：
     * 首先读取数据集文件，计算每条记录的Hilbert值并根据Hilbert值进行排
     * 序，然后对排序的数据根据集群结点数目进行等量划分。
     */
    private static void masterSection() {
    	System.out.println("master section.....");
    	
    	int maxIter = 1000;
        if( arguments.length > 0 )
        {
            try {
                maxIter = Integer.parseInt( arguments[0] );
            } catch( NumberFormatException nfe )
            {
                System.err.println( nfe );
            }
        }

        try {
        	Properties prop = System.getProperties();
    		String os = prop.getProperty("os.name");
    		File file = null;
    		if (os.startsWith("Win"))
//    			file = new File("d:\\RTree_work\\LB.txt");
    			file = new File("d:\\RTree_work\\32000.txt");
    		else {
    			String fileString = File.separator + "home" + 
    					File.separator + "joe" + File.separator + 
    					"parajava" + File.separator + "LB.txt";
				file = new File(fileString);
			}
    		
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			int j = 0;
			while( (line = reader.readLine()) != null  /*&& j < maxIter  */)
			{
				String[] splits = line.split(" ");
				float lx = Float.parseFloat(splits[1]);
				float ly = Float.parseFloat(splits[2]);
				float hx = Float.parseFloat(splits[3]);
				float hy = Float.parseFloat(splits[4]);
				
				Point p1 = new Point(new float[] { lx, ly });
				Point p2 = new Point(new float[] { hx, hy });
				
				int[] c = new int[p1.getDimension()];
				for (int i = 0; i < p1.getDimension(); i++) {
					c[i] = (int) ((p1.getFloatCoordinate(i) + p2.getFloatCoordinate(i) ) / 2);
				}
				
				long hilbertValue = HilbertMethod.getHilbertValue(c[0], c[1]);
				
				final Rectangle rec = new Rectangle( p1, p2, hilbertValue );
				
				list.add( rec );
				
//				System.out.println( "insert " + j + "th " + rec + "......" );//暂时注释
				j++;
			}
			
			System.out.println("list.size() = " + list.size());
	        Collections.sort( list );
			rectangles = list.toArray(new Rectangle[] {});
//			System.out.println("rectangles[0] = " + rectangles[0]);
			list = null;	//free the @list's storage.
			sliceRanges = new Range(0, rectangles.length - 1).subranges(size);
//	        System.out.println("rank = " + rank + ", rectangles.length = " + rectangles.length);//暂时注释
//	        System.out.println("rank = " + rank + ", sliceRanges.length = " + sliceRanges.length);//暂时注释
			slices = ObjectBuf.sliceBuffers(rectangles, sliceRanges);
			myslices = slices[rank];
//			System.out.println(myslices.getClass());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static boolean put( Rectangle key )
    {
    	tree.insert(key, -2, Constants.HILBERT);
        return true;
    }
    
    public static HilbertRTree getRTree() {
    	return tree;
    }
}

interface Tag {
	public static final int DATA = 0;
	public static final int FIRST = 1;
	public static final int SECOND = 2;
	public static final int SIZE = 3;
}

