package simpledb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import simpledb.Predicate.Op;

/** A class to represent a fixed-width histogram over a single integer-based field.
 */
public class IntHistogram {
	
	// Hashtable from bucket number to bucket's lower bound
	HashMap<Integer, Integer> bucketMins;
	
	// Hashtable from bucket number to bucket's upper bound
	//HashMap<Integer, Integer> bucketMaxes;
	
	// Hashtable from bucket number to number of elements in the bucket
	HashMap<Integer, Integer> counts;
	
	private int BUCKETS;  // Number of buckets
	private int MIN;  // Minimum of the histogram
	private int MAX;  // Maximum of the histogram
	private int numValues;  // Total number of elements in the histogram
	private int bucketSize;

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
    	this.MIN = min;
    	this.MAX = max;
    	this.numValues = 0;
    	this.bucketMins = new HashMap<Integer, Integer>();
    	this.counts = new HashMap<Integer, Integer>();
    	
    	int range = MAX - MIN;
    	if (buckets > (range))
    		BUCKETS = range;
    	else BUCKETS = buckets;
    	this.bucketSize = range / BUCKETS;
    	int i;
    	for(i=0; i<BUCKETS; i++) {
    		bucketMins.put(i, min + i*bucketSize);
    		counts.put(i, 0);
    	}
    }

    /**
     * @return the total number of values in the histogram
     **/
    public int getNumValues() {
    	return numValues;
    }
    
    /**
     * Add a value to the set of values that you are keeping a histogram of.
     * @param v Value to add to the histogram
     */
    public void addValue(int v) {
    	// some code goes here
    	this.numValues++;

    	int bucketNum = findBucket(v);
	    counts.put(bucketNum, counts.get(bucketNum)+1);
    }
    
    /*
     * Finds the bucket for the given value. If none found, returns -1
     */
    private int findBucket(int v) {
    	if (v > MAX || v < MIN) {
    		return -1;
    	} else if (v > bucketMins.get(BUCKETS-1)) {
    		return BUCKETS-1;
    	} else {
    		return (v - MIN) / bucketSize;
    	}
    }
    
    /*
     * Finds the number of values that the given bucket holds
     */
    private int findBucketWidth(int bucket) {
    	if (bucket == BUCKETS-1) {
    		return (MAX-MIN) - (BUCKETS-1) * bucketSize;
    	} else {
    		return bucketSize;
    	}
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
    	String opname = op.toString();
    	if (opname.equals("LIKE") || opname.equals("=")) {
    		return estimateEq(v);
    	} else if (opname.equals("<")) {
    		return estimateLT(v);
    	} else if (opname.equals(">")) {
    		return estimateGT(v);
    	} else if (opname.equals("<=")) {
    		return 1 - estimateGT(v);
    	} else if (opname.equals(">=")) {
    		return 1 - estimateLT(v);
    	} else if (opname.equals("<>")) {
    		return 1 - estimateEq(v);
    	}
        return -1.0;
    }
    
    private double estimateEq(int v) {
    	int bucket = findBucket(v);
    	if (bucket == -1)
    		return 0.0;
    	int height = counts.get(findBucket(v));
    	int width = findBucketWidth(bucket);
    	return 1.0 * height / width / numValues;
    } 
    
    private double estimateLT(int v) {
    	
    	// Find the selectivity contribution of v's bucket
    	int bucket = findBucket(v);
    	if (bucket == -1) {
    		if (v < MIN)
    			return 0.0;
    		if (v > MAX)
    			return 1.0;
    		throw new IllegalStateException("Could not find" +
    				" a bucket for " + v);
    	}
    	int height = counts.get(findBucket(v));
    	int width = findBucketWidth(bucket);
    	int leftBound = bucketMins.get(bucket);
    	double bucketPart = 1.0 * (leftBound - v) / width;
    	double bucketFrac = 1.0 * height / numValues;
    	double vBucketSelectivity = bucketPart * bucketFrac;
    	
    	// Calculate the other buckets' selectivity contribution
    	double othersSelectivity = 0.0;
    	int curHeight;
    	for(int i=0; i<bucket; i++) {
    		curHeight = counts.get(i);
    		othersSelectivity += (1.0 * curHeight / numValues);
    	}
    	return vBucketSelectivity + othersSelectivity;
    }
    
    private double estimateGT(int v) {
    	double ltestimate = estimateLT(v);
    	double eqestimate = estimateEq(v);
    	return 1 - (ltestimate + eqestimate);
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
    	String toOutput = "{";
    	int bucketMin;
    	int bucketMax;
    	for(int i=0; i<BUCKETS; i++) {
    		bucketMin = bucketMins.get(i);
    		if (i == BUCKETS-1) {
    			bucketMax = MAX;
    		} else {
    			bucketMax = bucketMins.get(i+1)-1;
    		}
    		toOutput += ("Bucket " + i + " (" + bucketMin + " - ");
    		toOutput += (bucketMax + "): " + counts.get(i) + "\n");
    		
    	}
    	toOutput += ("}\n");
        // some code goes here
        return toOutput;
    }
}
