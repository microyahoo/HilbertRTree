package com.njupt.btree;

/*
 * Sequential B*-Tree implementation for the 
 * Concurrent Search Tree Project for
 * Parallel Computing I
 *
 * Author: David C. Larsen <dcl9934@cs.rit.edu>
 * Date: April. 12, 2011
 */

import edu.rit.mp.*;
import edu.rit.mp.buf.*;
import edu.rit.pj.*;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A cluster implementation of a B+-Tree.
 */
public class BTreeClu
{
    /** The underlying BTree that all of the nodes will have. */
    private static volatile BTree bTree = BTree.newInstance();
    
    /* Environmental data */
    private static Comm world;
    private static int rank;
    private static int size;
    
    /** Used for sending commands to the worker nodes. */
    private static CharacterBuf command = new CharacterItemBuf();
    /** Used for sending keys to worker nodes. */
    private static IntegerBuf key = new IntegerItemBuf();

    /** Used to keep track of which node was last assigned work. */
    private static volatile int lastNodeUsed = 0;
    
    /** A queue of the operations that need to be performed on the tree. */
    private static LinkedBlockingQueue<BTreeOperation<Integer,Integer>> opQ = null;

    /** Used to determine if the load generator should keep working. */
    private static boolean running = true;

    /** Command-line arguments. */
    private static String[] arguments;

    /**
     * Starts running the BTree cluster.
     */
    public static void run(String[] args) throws Exception
    {
        long startTime = System.currentTimeMillis();
        
        System.out.println("startTime = " + startTime);
        Comm.init(args);
        System.out.println("After comm init....");
        world = Comm.world();
        System.out.println("world = " + world);
        rank = world.rank();
        size = world.size();
        System.out.println("rank = " + rank);
        System.out.println("size = " + size);
        arguments = args;
        
        if( rank == 0 )
        {
            if( opQ == null )
            {
                opQ = new LinkedBlockingQueue<BTreeOperation<Integer,Integer>>();
            }

            new Thread("Load generator") {

            	@Override
                public void run()
                {
                    int maxIter = 1000;
                    if( arguments.length > 0 )
                    {
                        try{
                            maxIter = Integer.parseInt( arguments[0] );
                        } catch( NumberFormatException nfe )
                        {
                            System.err.println( nfe );
                        }
                    }

                    for( int i = 0; i < maxIter; i++ )
                    {
                        put( i );
                    }
                    
                    try{
                        opQ.put( new BTreeOperation<Integer,Integer>( 'q' ) ); 
                    } catch( InterruptedException ie )
                    {
                        terminate();
                    }

                    while( running || !opQ.isEmpty() )
                    {
                        try{
                            BTreeOperation<Integer, Integer> nextOp = opQ.take();
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
            	
            	private void dispatch( Comm world, int workerNode, BTreeOperation op )
                {
            		char c = op.operation;
            		Integer key = (Integer) op.key;
            		try{
                        world.send( workerNode,  CharacterBuf.buffer(c) );
                        System.out.println("world.sent(" + workerNode + ", " + c + ")");
                        switch( c )
                        {
                            case 'g':
                                world.send( workerNode, IntegerBuf.buffer(key) );
                                break;
                                
                            case 'p':
                                world.send( workerNode,  IntegerBuf.buffer(key) );
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

                private void put( int key )
                {
                    try{
                        opQ.put( new BTreeOperation<Integer,Integer>( 'p',
                                                                      key));
                    } catch( InterruptedException ie )
                    {
                        System.out.println(ie);
                    }
                }

                private void get( int key )
                {
                    try{
                        opQ.put( new BTreeOperation<Integer, Integer>( 'g',
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
//                    world.receive( 0, key );
//                    got = get( new Integer(key.get( 0 )) );
//                    System.out.println("operation g : rank = " + rank + ", key = " + key.get(0));//add
                    break;
                    
                case 'p':
                    world.receive( 0, key );
                    put( new Integer(key.get( 0 )));
                    System.out.println("operation p : rank = " + rank + ", key = " + key.get(0));//add
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

    public static boolean put( Integer key )
    {
    	boolean ret = bTree.insertKey(key);
//    	System.out.println(bTree);
        return ret;
    }

    public static boolean remove( int key )
    {
        return bTree.removeKey(key);
    }

    /** {@inheritDoc} */
    public static int size()
    {
        return bTree.sizeOfKeys();
    }

    /** 
     * Terminate the main thread of execution.
     */
    public static void terminate()
    {
        running = false;
    }
    
    public static BTree getBTree() {
    	return bTree;
    }
}
