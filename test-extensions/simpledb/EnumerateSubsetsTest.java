package simpledb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import simpledb.systemtest.SimpleDbTestBase;

public class EnumerateSubsetsTest extends SimpleDbTestBase {
    HashMap<Integer, Set<BitSet>> sets;
    boolean setsCreated;
	
    /**
     * Set up the test; create some initial tables to work with
     */
    @Before
    public void setUp() throws Exception {
    	this.sets = new HashMap<Integer, Set<BitSet>>();
    	this.setsCreated = false;
    }
    
    /**
     * Determine whether the enumerateSubsets function can correctly enumerate 
     * a small vector of integers
     */
    @Test
    public void enumCorrectnessTestSmall() {
    	Vector<Integer> v = new Vector<Integer>();
    	for(int i=-1; i<2; i++) {
    		v.add(i);
    	}
    	Set<Set<Integer>> sets1 = enumerateSubsets(v, 1);
    	
    	// Set up the answer for enumerating for size 1
    	Set<Set<Integer>> answer1 = new HashSet<Set<Integer>>();
    	for(int i=-1; i<2; i++) {
    		Set<Integer> nextSet= new HashSet<Integer>();
    		nextSet.add(i);
    		answer1.add(nextSet);
    	}
    	Assert.assertEquals(sets1.size(), answer1.size());
    	Assert.assertEquals(sets1, answer1);
    	
    	Set<Set<Integer>> sets2 = enumerateSubsets(v, 2);
    	
    	// Set up the answer for enumerating for size 2
    	Set<Set<Integer>> answer2 = new HashSet<Set<Integer>>();
    	
    	Set<Integer> subset1 = new HashSet<Integer>();
    	subset1.add(-1);
    	subset1.add(0);
    	
    	Set<Integer> subset2 = new HashSet<Integer>();
    	subset2.add(-1);
    	subset2.add(1);
    	
    	Set<Integer> subset3 = new HashSet<Integer>();
    	subset3.add(0);
    	subset3.add(1);
    	
    	answer2.add(subset1);
    	answer2.add(subset2);
    	answer2.add(subset3);
    	
    	Assert.assertEquals(sets2.size(), answer2.size());
    	Assert.assertEquals(sets2, answer2);
    	
    	Set<Set<Integer>> sets3 = enumerateSubsets(v, 3);
    	
    	// Set up the answer for enumerating for size 3
    	Set<Set<Integer>> answer3 = new HashSet<Set<Integer>>();
    	Set<Integer> allElts = new HashSet<Integer>();
    	for(int i=-1; i<2; i++) {
    		allElts.add(i);
    	}
    	answer3.add(allElts);
    	
    	Assert.assertEquals(sets3.size(), answer3.size());
    	Assert.assertEquals(sets3, answer3);
    }
    
    /**
     * Determine whether the enumerateSubsets enumerations can get the correct
     * number of subsets for each size
     */
    @Test
    public void enumSizeTestLarge() {
    	Vector<Integer> v = new Vector<Integer>();
    	for(int i=1; i<=15; i++) {
    		v.add(i);
    	}
    	ArrayList<Integer> allSizes = new ArrayList<Integer>();
    	for(int i=1; i<=15; i++) {
    		allSizes.add(enumerateSubsets(v, i).size());
    	}
		ArrayList<Integer> answerSizes = new ArrayList<Integer>(Arrays.asList(15,
				105, 455, 1365, 3003, 5005, 6435, 6435, 5005, 3003, 1365, 455, 105));
    	// Size should equal the result of combination 20 C 3
    	for(int i=0; i<answerSizes.size(); i++) {
    		Assert.assertEquals(answerSizes.get(i), allSizes.get(i));
    	}
    }
    
    /**
     * Ensure that the improved enumerator is consistently faster
     */
    @Test
    public void enumTestSpeed() {
    	for(int n=0; n<10; n++) {
	    	Vector<Integer> v = new Vector<Integer>();
	    	for(int i=1; i<=10; i++) {
	    		v.add(i);
	    	}
	    	
	    	long startNew = System.currentTimeMillis();
	    	ArrayList<Integer> resultsNewEnum = new ArrayList<Integer>();
	    	for(int i=1; i<=10; i++) {
	    		resultsNewEnum.add(enumerateSubsets(v, i).size());
	    	}
	    	long endNew = System.currentTimeMillis();
	    	long runtimeNew = endNew - startNew;
	    	
	    	long startOld = System.currentTimeMillis();
	    	ArrayList<Integer> resultsOldEnum = new ArrayList<Integer>();
	    	for(int i=1; i<=10; i++) {
	    		resultsOldEnum.add(oldEnumerateSubsets(v, i).size());
	    	}
	    	long endOld = System.currentTimeMillis();
	    	long runtimeOld = endOld - startOld;
	    	
	    	Assert.assertTrue(runtimeNew < runtimeOld);
    	}
    }
    
    /**
     * Determine whether 
     * 
     * */
    
    
    /**
     * Helper method to enumerate all of the subsets of a given size of a
     * specified vector.
     * 
     * @param v
     *            The vector whose subsets are desired
     * @param size
     *            The size of the subsets of interest
     * @return a set of all subsets of the specified size
     */
    @SuppressWarnings("unchecked")
    private <T> Set<Set<T>> oldEnumerateSubsets(Vector<T> v, int size) {
        Set<Set<T>> els = new HashSet<Set<T>>();
        els.add(new HashSet<T>());
        // Iterator<Set> it;
        // long start = System.currentTimeMillis();

        for (int i = 0; i < size; i++) {
            Set<Set<T>> newels = new HashSet<Set<T>>();
            for (Set<T> s : els) {
                for (T t : v) {
                    Set<T> news = (Set<T>) (((HashSet<T>) s).clone());
                    if (news.add(t))
                        newels.add(news);
                }
            }
            els = newels;
        }

        return els;

    }
    
    /**
     * Helper method to enumerate all of the subsets of a given size of a
     * specified vector.
     * 
     * @param v
     *            The vector whose subsets are desired
     * @param size
     *            The size of the subsets of interest
     * @return a set of all subsets of the specified size
     */
    private <T> Set<Set<T>> enumerateSubsets(Vector<T> v, int size) {
        Set<Set<T>> els = new HashSet<Set<T>>();

        // For speedup, we create BitSets for all combinations of bits
        // from 1 up to 2^(v.size())-1 on the first call to enumerateSubsets,
        // and use the number of ones that are set in the BitSet to determine
        // the size that this BitMap is a combination for, adding it to that
        // size's slot. We build up sets of these BitSets for each size, 
        // so that on subsequent lookups, we already have the set of BitSets
        // to return.

        // If this is the first time, create the subset BitSets
        if (!setsCreated)
        	createSets(v, size);

        // Create the actual subset using the BitSets for the given size
    	Set<BitSet> bitSets = sets.get(size);
    	Iterator<BitSet> iter = bitSets.iterator();
    	for (int i=0; i<bitSets.size(); i++) {
    		// Get the next BitSet to create a subset from
    		BitSet bits = (BitSet) iter.next();
    		
    		// Create an actual subset from the subset BitSet
    		Set<T> newSet = new HashSet<T>();
    		for (int j = bits.nextSetBit(0); j >= 0; j = bits.nextSetBit(j+1)) {
    		     newSet.add(v.get(j));
    		}
    		
    		// Add the subset to the return set
    		els.add(newSet);
    	}
    	return els;
    }
    
    private <T> void createSets(Vector<T> v, int size) {
        // Initialize sets for each size
        for (int i=1; i<=v.size(); i++) {
        	sets.put(i, new HashSet<BitSet>());
        }
        
        // Find the largest number represented by a BitSet
        // of all 1's of size v.size() e.g. 11111 for v.size() = 5
        long maxBitSetVal = (long)Math.pow(2, v.size())-1;
        BitSet bits;
        
        // Use the fact that we need all combinations of positions,
        // which can be represented as bit strings, to create the
        // appropriate BitSets, organizing them by size
        for (long j=1; j<=maxBitSetVal; j++) {
        	// Create a BitSet from this long
        	bits = convertLongToBitset(j);
        	
        	// Calculate the number of bits set in this long
        	int setBits = Long.bitCount(j);
        	
        	// Add the new BitSet to the map
        	if (sets.containsKey(setBits)) {
        		// Map already contains a set for this size, add to it
        		Set<BitSet> temp = sets.get(setBits);
        		temp.add(bits);
        		sets.put(setBits, temp);
        	} else {
        		// No set exists for this size yet, create new entry
        		Set<BitSet> newSet = new HashSet<BitSet>();
        		newSet.add(bits);
        		sets.put(setBits, newSet);
        	}
        }
        setsCreated = true;
    }
    
    private static BitSet convertLongToBitset(long value) {
        BitSet bits = new BitSet();
        int index = 0;
        while (value != 0L) {
          if (value % 2L != 0) {
            bits.set(index);
          }
          ++index;
          value = value >>> 1;
        }
        return bits;
      }
}
