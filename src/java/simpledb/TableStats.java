package simpledb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TableStats represents statistics (e.g., histograms) about base tables in a
 * query. 
 * 
 * This class is not needed in implementing lab1 and lab2.
 */
public class TableStats {

	private class AttrRange {
		public Type type;
		public int min;
		public int max;
		
		AttrRange(Type type, int min, int max) {
			this.type = type;
			this.min = min;
			this.max = max;
		}
	}
	
    private static final ConcurrentHashMap<String, TableStats> statsMap = new ConcurrentHashMap<String, TableStats>();

    static final int IOCOSTPERPAGE = 1000;

    public static TableStats getTableStats(String tablename) {
        return statsMap.get(tablename);
    }

    public static void setTableStats(String tablename, TableStats stats) {
        statsMap.put(tablename, stats);
    }
    
    public static void setStatsMap(HashMap<String,TableStats> s)
    {
        try {
            java.lang.reflect.Field statsMapF = TableStats.class.getDeclaredField("statsMap");
            statsMapF.setAccessible(true);
            statsMapF.set(null, s);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    public static Map<String, TableStats> getStatsMap() {
        return statsMap;
    }

    public static void computeStatistics() {
        Iterator<Integer> tableIt = Database.getCatalog().tableIdIterator();

        System.out.println("Computing table stats.");
        while (tableIt.hasNext()) {
            int tableid = tableIt.next();
            TableStats s = new TableStats(tableid, IOCOSTPERPAGE);
            setTableStats(Database.getCatalog().getTableName(tableid), s);
        }
        System.out.println("Done.");
    }

    /**
     * Number of bins for the histogram. Feel free to increase this value over
     * 100, though our tests assume that you have at least 100 bins in your
     * histograms.
     */
    static final int NUM_HIST_BINS = 100;
    private ArrayList<AttrRange> attrRanges;
    private ArrayList<Object> histograms;
    private TransactionId tid;
    private int ioCost;
    

    /**
     * Create a new TableStats object, that keeps track of statistics on each
     * column of a table
     * 
     * @param tableid
     *            The table over which to compute statistics
     * @param ioCostPerPage
     *            The cost per page of IO. This doesn't differentiate between
     *            sequential-scan IO and disk seeks.
     */
    public TableStats(int tableid, int ioCostPerPage) {
        // For this function, you'll have to get the
        // DbFile for the table in question,
        // then scan through its tuples and calculate
        // the values that you need.
        // You should try to do this reasonably efficiently, but you don't
        // necessarily have to (for example) do everything
        // in a single scan of the table.

        // some code goes here
    	this.tid = new TransactionId();
    	createHistograms(tableid);
    	populateHistograms(tableid);
    	HeapFile hf = (HeapFile)Database.getCatalog().getDatabaseFile(tableid);
    	this.ioCost = hf.numPages() * ioCostPerPage;
    }
    
    /*
     * Helper method for the constructor that scans through the tuples for a
     * given table, computing the min and max for each attribute of the table
     * and creating a histogram for each, populating the histogram with the 
     * values of the tuples, and adding the histogram to the 'histograms' structure
     * @param tableid the id of the table to create histograms for
     */
    private void createHistograms(int tableid) {
    	HeapFile table = (HeapFile)Database.getCatalog().getDatabaseFile(tableid);
    	// Fetch the TupleDesc and set up the structure to hold the stats
    	TupleDesc desc = table.getTupleDesc();
    	int tupleSize = desc.numFields();
    	attrRanges = new ArrayList<AttrRange>(tupleSize);
    	histograms = new ArrayList<Object>(tupleSize);
    	
    	// Initialize the stats for each attribute
    	for(int i=0; i<desc.numFields(); i++) {
    		attrRanges.add(i, new AttrRange(desc.getFieldType(i),
    										Integer.MAX_VALUE,
    					                    Integer.MIN_VALUE));
    	}
    	
    	DbFileIterator iter = table.iterator(tid);
    	try {
			iter.open();
		} catch (DbException | TransactionAbortedException e1) {
			e1.printStackTrace();
		}
    	
    	// Set the min/max for each attribute
    	try {
			while(iter.hasNext()) {
				Tuple tup = iter.next();
				for(int i=0; i<tupleSize; i++) {;
					int intValue = (int) tup.getField(i).hashCode();
					AttrRange range = attrRanges.get(i);
					if(intValue < range.min)
						range.min = intValue;
					if(intValue > range.max)
						range.max = intValue;
					attrRanges.set(i, range);
				}
			}
		} catch (NoSuchElementException | DbException
				| TransactionAbortedException e) {
			System.out.println("Failed to get next tuple from table" +
					           " in createHistograms" + tableid);
			e.printStackTrace();
		}
    	
    	// For each created AttrRange, create a histogram
    	for(AttrRange ar : attrRanges) {
    		if(ar.type == Type.STRING_TYPE) {
    			histograms.add(new StringHistogram(NUM_HIST_BINS));
    		} else {
    			histograms.add(new IntHistogram(NUM_HIST_BINS, ar.min, ar.max));
    		}
    	}
    }
    
    /*
         * Scans through the tuples of the TableStats's tuples to populate the histograms,
         * modifying the state of the histograms structure
         */
        private void populateHistograms(int tableid) {
        	HeapFile table = (HeapFile)Database.getCatalog().getDatabaseFile(tableid);
        	
        	// Fetch the TupleDesc and set up the structure to hold the stats
        	TupleDesc desc = table.getTupleDesc();
        	int tupleSize = desc.numFields();
        	
        	// Get an iterator over the table's tuples
        	DbFileIterator iter = table.iterator(tid);
        	try {
				iter.open();
			} catch (DbException | TransactionAbortedException e1) {
				e1.printStackTrace();
			}
        	
        	// Add the values to the histograms
        	try {
    			while(iter.hasNext()) {
    				Tuple tup = iter.next();
    				for(int i=0; i<tupleSize; i++) {
    					Type fType = tup.getField(i).getType();
    					if(fType == Type.INT_TYPE) {
    						IntHistogram hist = (IntHistogram) histograms.get(i);
    						int value = (int) tup.getField(i).hashCode();
    						hist.addValue(value);
    						histograms.set(i, hist);
    					} else {
    						StringHistogram hist = (StringHistogram) 
    											   histograms.get(i);
    						String value = (String) tup.getField(i).toString();
    						hist.addValue(value);
    						histograms.set(i, value);
    					}
    				}
    				
    			}
    		} catch (NoSuchElementException | DbException
    				| TransactionAbortedException e) {
    			System.out.println("Failed to add values to histogram from table " +
    							    "in populateHistograms " + tableid);
    			e.printStackTrace();
    		}
        }
    /**
     * Estimates the cost of sequentially scanning the file, given that the cost
     * to read a page is costPerPageIO. You can assume that there are no seeks
     * and that no pages are in the buffer pool.
     * 
     * Also, assume that your hard drive can only read entire pages at once, so
     * if the last page of the table only has one tuple on it, it's just as
     * expensive to read as a full page. (Most real hard drives can't
     * efficiently address regions smaller than a page at a time.)
     * 
     * @return The estimated cost of scanning the table.
     */
    public double estimateScanCost() {
    	return ioCost;
    }

    /**
     * This method returns the number of tuples in the relation, given that a
     * predicate with selectivity selectivityFactor is applied.
     * 
     * @param selectivityFactor
     *            The selectivity of any predicates over the table
     * @return The estimated cardinality of the scan with the specified
     *         selectivityFactor
     */
    public int estimateTableCardinality(double selectivityFactor) {
        // some code goes here
    	return (int)(totalTuples() * selectivityFactor);
    }

    /**
     * The average selectivity of the field under op.
     * @param field
     *        the index of the field
     * @param op
     *        the operator in the predicate
     * The semantic of the method is that, given the table, and then given a
     * tuple, of which we do not know the value of the field, return the
     * expected selectivity. You may estimate this value from the histograms.
     * */
    public double avgSelectivity(int field, Predicate.Op op) {
        // some code goes here
        return 1.0;
    }

    /**
     * Estimate the selectivity of predicate <tt>field op constant</tt> on the
     * table.
     * 
     * @param field
     *            The field over which the predicate ranges
     * @param op
     *            The logical operation in the predicate
     * @param constant
     *            The value against which the field is compared
     * @return The estimated selectivity (fraction of tuples that satisfy) the
     *         predicate
     */
    public double estimateSelectivity(int field, Predicate.Op op, Field constant) {
        // some code goes here
    	Type cType = constant.getType();
    	if (cType == Type.INT_TYPE) {
    		IntHistogram hist = (IntHistogram)histograms.get(field);
    		return hist.estimateSelectivity(op, constant.hashCode());
    	} else {
    		StringHistogram hist = (StringHistogram) histograms.get(field);
    		return hist.estimateSelectivity(op, constant.toString());
    	}
    }

    /**
     * return the total number of tuples in this table
     * */
    public int totalTuples() {
    	if(histograms.size() == 0) {
    		return 0;
    	} else {
    		// All histograms should have the same number of tuples, just use
    		// the first one to retrieve # of tuples
    		Object o = histograms.get(0);
    		if(o instanceof IntHistogram) {
    			IntHistogram hist = (IntHistogram) o;
    			return hist.getNumValues();
    		} else {
    			StringHistogram hist = (StringHistogram) o;
    			return hist.getNumValues();
    		}
    	}
    }
}
