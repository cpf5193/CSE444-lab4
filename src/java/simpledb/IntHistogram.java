package simpledb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/** A class to represent a fixed-width histogram over a single integer-based field.
 */
public class IntHistogram {
	
	// Hashtable from bucket number to bucket's lower bound
	HashMap<Integer, Integer> bucketMins;
	
	// Hashtable from bucket number to bucket's upper bound
	//HashMap<Integer, Integer> bucketMaxes;
	
	// Hashtable from bucket number to list of counts of values in the bucket	
	HashMap<Integer, ArrayList<Integer>> counts;
	
	int BUCKETS;  // Number of buckets
	int MIN;  // Minimum of the histogram
	int MAX;  // Maximum of the histogram
	int numValues;  // Total number of elements in the histogram
	int bucketSize;

    /**
     * Create a new IntHistogram.
     * 
     * This IntHistogram should maintain a histogram of integer values that it receives.
     * It should split the histogram into "buckets" buckets.
     * 
     * The values that are being histogrammed will be provided one-at-a-time through the "addValue()" function.
     * 
     * Your implementation should use space and have execution time that are both
     * constant with respect to the number of values being histogrammed.  For example, you shouldn't 
     * simply store every value that you see in a sorted list.
     * 
     * @param buckets The number of buckets to split the input value into.
     * @param min The minimum integer value that will ever be passed to this class for histogramming
     * @param max The maximum integer value that will ever be passed to this class for histogramming
     */
    public IntHistogram(int buckets, int min, int max) {
    	// some code goes here
    	this.BUCKETS = buckets;
    	this.MIN = min;
    	this.MAX = max;
    	this.numValues = 0;
    	this.bucketMins = new HashMap<Integer, Integer>();
    	this.counts = new HashMap<Integer, ArrayList<Integer>>();
    	
    	int range = MAX - MIN;
    	this.bucketSize = range / BUCKETS;
    	int i;
    	for(i=0; i<BUCKETS-1; i++) {
    		bucketMins.put(i, min + i*bucketSize);
    		// Initialize the counts to 0
    		counts.put(i, new ArrayList<Integer>(Collections.nCopies(bucketSize, 0)));
    	}
    	// Fill in the last bucket, may be wider than other buckets
    	bucketMins.put(i, min + i*bucketSize);
    	int lastBucketSize = range - (bucketSize*(BUCKETS-1));
    	// Initialize the counts to 0
    	counts.put(i, new ArrayList<Integer>(Collections.nCopies(lastBucketSize, 0)));
    }

    /**
     * Add a value to the set of values that you are keeping a histogram of.
     * @param v Value to add to the histogram
     */
    public void addValue(int v) {
    	// some code goes here
    	this.numValues++;

    	int bucketNum;
    	int bucketSlot;
    	if (v > bucketMins.get(BUCKETS-1)) {
    		// v belongs in the last bucket
    		bucketNum = BUCKETS-1;
    		bucketSlot = v - (bucketMins.get(BUCKETS-1));
    	} else {
    		bucketNum = (v - MIN) / this.bucketSize;
	    	bucketSlot = (v - MIN) % this.bucketSize;
    	}	
    	ArrayList<Integer> temp = counts.get(bucketNum);
	    temp.set(bucketSlot, temp.get(bucketSlot)+1);
	    counts.put(bucketNum, temp);
    }

    /**
     * Estimate the selectivity of a particular predicate and operand on this table.
     * 
     * For example, if "op" is "GREATER_THAN" and "v" is 5, 
     * return your estimate of the fraction of elements that are greater than 5.
     * 
     * @param op Operator
     * @param v Value
     * @return Predicted selectivity of this particular operator and value
     */
    public double estimateSelectivity(Predicate.Op op, int v) {

    	// some code goes here
        return -1.0;
    }
    
    /**
     * @return
     *     the average selectivity of this histogram.
     *     
     *     This is not an indispensable method to implement the basic
     *     join optimization. It may be needed if you want to
     *     implement a more efficient optimization
     * */
    public double avgSelectivity()
    {
        // some code goes here
        return 1.0;
    }
    
    /**
     * @return A string describing this histogram, for debugging purposes
     */
    public String toString() {
        // some code goes here
        return null;
    }
}
