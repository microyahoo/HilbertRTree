package com.njupt.hilbert.rtree;

import edu.rit.mp.*;
import edu.rit.mp.buf.*;
import edu.rit.pj.*;
import edu.rit.util.Arrays;
import edu.rit.util.Range;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A cluster implementation of a Hilbert R-Tree.
 */
public class RTreeClu
{
    /** The underlying Hilbert RTree that all of the nodes will have. */
	private static volatile HilbertRTree tree = 
			new HilbertRTree(2, 0.4f, 5, Constants.RTREE_QUADRATIC, 2);
    
    /** Environmental data */
    static Comm world;
    static int rank;
    static int size;
    
    /** Used for sending commands to the worker nodes. */
    private static CharacterBuf command = new CharacterItemBuf();
    /** Used for sending keys to worker nodes. */
    static ObjectBuf<Rectangle> keys = ObjectBuf.buffer();

    /** Used to keep track of which node was last assigned work. */
    private static volatile int lastNodeUsed = 0;
    
    /** A queue of the operations that need to be performed on the tree. */
    private static BlockingQueue<RTreeOperation<Rectangle,Integer>> opQ = null;
    
    /**
     * store all rectangles.
     */
    static List<Rectangle> list = new ArrayList<>();
    static Rectangle[] rectangles = null;

    static CountDownLatch startGate = new CountDownLatch(1);
    
    /** Used to determine if the load generator should keep working. */
    static boolean running = true;

    /** Command-line arguments. */
    private static String[] arguments;
    
    static ObjectBuf<Rectangle>[] slices;
    static ObjectBuf<Rectangle> myslices;
    
    static Range[] sliceRanges;
    
    /**
     * Starts running the RTree cluster.
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
        
        if ( rank == 0 ) {
        	masterSection();
//        	world.broadcast(rank, Tag.EXECUTE, CharacterBuf.buffer(Tag.EXE));
        	world.barrier(Tag.FIRST);
        	System.out.println("rank = " + rank + " after masterSection....");
        	workerSection();
        	
        	
//        	new ParallelTeam(2).execute(new ParallelRegion() {
//				
//				@Override
//				public void run() throws Exception {
//					
//					execute(new ParallelSection() {
//						
//						@Override
//						public void run() throws Exception {
//							masterSection();
//							world.barrier(Tag.FIRST);
//							startGate.countDown();
////							barrier();
//							System.out.println("rank = " + rank + " after masterSection....");
//						}
//					}, new ParallelSection() {
//						
//						@Override
//						public void run() throws Exception {
//							startGate.await();
////							barrier();
//							System.out.println("rank = " + rank + " before second barrier, workerSection_rank0 ***********");
//							world.barrier(Tag.SECOND);
//							System.out.println("rank = " + rank + " after second barrier, workerSection_rank0 ***********");
//							workerSection();
//						}
//					});
//				}
//			});
        } else {
        	System.out.println("rank = " + rank + ", blocked before the Tag.FIRST.");
        	world.barrier(Tag.FIRST);
//        	world.broadcast(0, Tag.EXECUTE, command);
        	System.out.println("rank = " + rank + ", blocked after the Tag.FIRST.");
        	
//        	System.out.println("*********** rank = " + rank + " before second barrier.");
//			world.barrier(Tag.SECOND);
//			System.out.println("*********** rank = " + rank + " after second barrier.");

			System.out.println("rank = " + rank + " before workerSection");
        	workerSection();
        	System.out.println("rank = " + rank + " after workerSection");
        }
        
        System.out.println("rank = " + rank + ", mySlices.length = " + myslices.length());
		
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++=");
		
        if( rank == 0 )
        {
            if( opQ == null )
            {
                opQ = new LinkedBlockingQueue<RTreeOperation<Rectangle,Integer>>();
            }
            
            new Thread("Load generator") {

            	@Override
                public void run()
                {
                    int maxIter = 100;
                    if( arguments.length > 0 )
                    {
                        try{
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
                			file = new File("d:\\RTree_work\\LB.txt");
                		else {
                			String fileString = File.separator + "home" + 
                					File.separator + "joe" + File.separator + 
                					"parajava" + File.separator + "LB.txt";
							file = new File(fileString);
						}
//                		if (file == null)
//                			throw new RuntimeException("File is not exist!");
                		
						BufferedReader reader = new BufferedReader(new FileReader(file));
						String line;
						int j = 0;
						while( (line = reader.readLine()) != null  && j < maxIter  )
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
							
							final Rectangle rectangle = new Rectangle(p1, p2, hilbertValue);
							
//							list.add(rectangle);
							
							System.out.println("insert " + j + "th " + rectangle + "......");
							put( rectangle );
							j++;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
                    
                    try{
                        opQ.put( new RTreeOperation<Rectangle,Integer>( 'q' ) ); 
                    } catch( InterruptedException ie )
                    {
                        terminate();
                    }

                    while( running || !opQ.isEmpty() )
                    {
                        try{
                        	RTreeOperation<Rectangle, Integer> nextOp = opQ.take();
                            if( running )
                            {
                                if( nextOp.operation == 'q' )
                                {
                                    terminate();
                                }
                                else
                                {
                                	System.out.println("nextOp.key = " +  nextOp.key + ", lastNodeUsed = " + lastNodeUsed);//add
                                    dispatch( world, lastNodeUsed, nextOp );
                                    lastNodeUsed = (lastNodeUsed + 1) % size;
                                }
                            }
                        } catch( InterruptedException ie )
                        {
                            // Quit
                            System.err.println("exiting");
                        }
                    }
                    
                    // Tell all of the workers to shut down -- there's no more work.
                    for( int i = size - 1; i >= 0; i-- )
                    {
                        try{
                        	System.out.println("world.sent(" + i + ", q)");
                            world.send( i,  CharacterBuf.buffer('q') );
                        } catch( IOException ioe )
                        {
                            System.err.println(ioe);
                        }
                    }
                }// end run()

//                private BTreeCluWorkerThread
//                dispatch( Comm world, int workerNode, RTreeOperation op )
//                {
//                    BTreeCluWorkerThread thread =
//                        new BTreeCluWorkerThread( world, workerNode, op );
//                    thread.start();
//                    return thread;
//                }
            	
            	private void dispatch( Comm world, int workerNode, RTreeOperation<Rectangle, Integer> op )
                {
            		char c = op.operation;
            		Rectangle key = (Rectangle) op.key;
            		try{
                        world.send( workerNode,  CharacterBuf.buffer(c) );
                        System.out.println("world.sent(" + workerNode + ", " + c + ")");
                        switch( c )
                        {
                            case 'g':
                                world.send( workerNode, ObjectBuf.buffer(key) );
                                break;
                                
                            case 'p':
                                world.send( workerNode,  ObjectBuf.buffer(key) );
                                System.out.println("world.sent(" + workerNode + ", " + key + ")");
                                break;
                                
                            case 'q':
                                return;
                        }
                    } catch( Exception ex )
                    {
                        System.err.println(ex);
                    }
                }

                private void put( Rectangle key )
                {
                    try{
                        opQ.put( new RTreeOperation<Rectangle,Integer>( 'p',
                                                                      key));
                    } catch( InterruptedException ie )
                    {
                        System.out.println(ie);
                    }
                }

                private void get( Rectangle key )
                {
                    try{
                        opQ.put( new RTreeOperation<Rectangle, Integer>( 'g',
                                                                       key ));
                    } catch( InterruptedException ie )
                    {
                        System.out.println(ie);
                    }
                }
            }.start(); //end new Thread("Load generator")
        } //end if (rank == 0)

        
        // Listen for commands, execute them.
        while( true )
        {
            System.out.println("rank = " + rank + " , before world.receive() ....");//add
            world.receive(0, command);
            char c = command.get(0);
            System.out.println("rank = " + rank + " , after world.receive() ...., and command = " + c);//add
            switch( c )
            {
                case 'g':
//                    world.receive( 0, keys );
//                    got = get( new Integer(keys.get( 0 )) );
//                    System.out.println("operation g : rank = " + rank + ", keys = " + keys.get(0));//add
                    break;
                    
                case 'p':
                    world.receive( 0, keys );
                    put( keys.get( 0 ));
                    System.out.println("operation p : rank = " + rank + ", keys = " + keys.get(0));//add
                    break;
                    
                case 'q':
                    System.out.println( rank + " says goodbye" );
                    return;
                    
                default:
                    System.err.print( rank + " got an un-recognized command: ");
                    System.err.println( command.get( 0 ) );
                    return;
            }
        }
    }

    private static void workerSection() throws IOException {
    	System.out.println("rank = " + rank + ", worker section....*****************.");
    	try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	
    	if (rank != 0)
    		world.scatter(0, Tag.DATA, null, myslices);
    	System.out.println("myslices = " + myslices + ", myslices.length() = " + myslices.length());
    	
	}

    private static void masterSection() {
    	System.out.println("master section.....");
    	
    	int maxIter = 100;
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
    			file = new File("d:\\RTree_work\\LB.txt");
    		else {
    			String fileString = File.separator + "home" + 
    					File.separator + "joe" + File.separator + 
    					"parajava" + File.separator + "LB.txt";
				file = new File(fileString);
			}
    		
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			int j = 0;
			while( (line = reader.readLine()) != null  && j < maxIter  )
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
				
				System.out.println( "insert " + j + "th " + rec + "......" );
//				put( rec );
				j++;
			}
			
			System.out.println("list.size() = " + list.size());
	        Collections.sort( list );
			rectangles = list.toArray(new Rectangle[] {});
			System.out.println("rectangles[0] = " + rectangles[0]);
			list = null;	//free the @list's storage.
			sliceRanges = new Range(0, rectangles.length - 1).subranges(size);
	        System.out.println("rank = " + rank + ", rectangles.length = " + rectangles.length);
	        System.out.println("rank = " + rank + ", sliceRanges.length = " + sliceRanges.length);
			slices = ObjectBuf.sliceBuffers(rectangles, sliceRanges);
			myslices = slices[rank];
			
			world.scatter(rank, Tag.DATA, slices, myslices);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static boolean put( Rectangle key )
    {
    	tree.insert(key, -2, Constants.HILBERT);
        return true;
    }

    /** 
     * Terminate the main thread of execution.
     */
    public static void terminate()
    {
        running = false;
    }
    
    public static HilbertRTree getRTree() {
    	return tree;
    }
}


