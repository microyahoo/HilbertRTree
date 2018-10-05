package com.njupt.btree;
public class BTreeOperation<K,V>
{
    public char operation;
    public K key;
//    public V value;
    public V result;
    public boolean resultPut;

//    public RTreeOperation( char operation, K key, V value ){
//        this.operation = operation;
//        this.key = key;
//        this.value = value;
//        result = null;
//        resultPut = false;
//    }

    public BTreeOperation( char operation, K key )
    {
    	this.operation = operation;
        this.key = key;
        result = null;
        resultPut = false;
    }

    public BTreeOperation( char operation )
    {
        this( operation, null );
    }


    public void putResult( V result )
    {
        this.result = result;
        resultPut = true;
        notify();
    }

    /**
     * Wait until we get the result from the tree.
     * @return
     */
    public V getResult()
    {
        // Wait until we get the result from the tree.
        try{
            while( !resultPut ) {
                wait();
            }
        // Interrupted? Return what we have.
        } catch( InterruptedException ie ) {}

        return result;
    }
}
