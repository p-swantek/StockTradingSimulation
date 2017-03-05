package publishers;

/**
 * Factory methods to return concrete implementations of data trackers
 * 
 * @author Peter Swantek
 *
 */
public class DataTrackers {

    private DataTrackers() {
    }

    /**
     * Get a data tracker to be used by publishers to keep track of stocks and
     * interested users
     * 
     * @return a tracker that will deal with tracking users that are associated
     *         with a stock
     */
    public static PublisherDataTracker getStockRecorder() {
        return new StockRecordKeeper();
    }

    /**
     * Get a data tracker to be used the ticker publisher to publish ticker data
     * 
     * @return a tracker that will deal with tracking how the price for a stock
     *         changes
     */
    public static PriceDirectionTracker getDirectionRecorder() {
        return new DirectionRecordKeeper();
    }

}
