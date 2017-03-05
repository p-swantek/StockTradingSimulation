package publishers;

import client.User;
import exceptions.AlreadySubscribedException;
import exceptions.NotSubscribedException;
import price.Price;
import price.PriceFactory;

/**
 * 
 * 
 * A class that publishes the stock ticker for a stock in the stock exchange.
 * This class employs the singleton design pattern to guarantee that only one
 * instance is created. This class will allow users to sign up in order to be
 * sent tickers for the stocks that they are interested in. This publisher keeps
 * a record of all the stocks and which users are interested in each particular
 * stock. It also maintains a record of how a price for a stock has changed with
 * respect to the previous price for that stock
 * 
 * @author Peter Swantek
 *
 */

public class TickerPublisher implements Publisher {

    // Have data be volatile so that it is obtained from main memory instead of a thread's cached memory
    private static volatile TickerPublisher theInstance = null;
    private static PublisherDataTracker recorder;
    private static PriceDirectionTracker directions; // Record of stocks and their most recent trading Price

    private TickerPublisher() {
        recorder = DataTrackers.getStockRecorder();
        directions = DataTrackers.getDirectionRecorder();
    }

    // Makes sure only one instance is created. Uses Double-Checked locking to make sure synchronization only occurs
    // for the initial creation of the instance
    public static TickerPublisher getInstance() {
        if (theInstance == null) {
            synchronized (TickerPublisher.class) {
                if (theInstance == null) {
                    theInstance = new TickerPublisher();
                }
            }
        }

        return theInstance;
    }

    // Subscribes a user for a certain stock.  Finds the ArrayList of users for the stock
    // within the HashMap.  If the user is already a subscriber then an exception will be thrown, otherwise
    // the user is added to the ArrayList associated with that stock
    @Override
    public synchronized void subscribe(User u, String product) throws AlreadySubscribedException {
        recorder.addUser(u, product);
    }

    // Unsubscribes a user for a stock.  Finds the ArrayList of users for the particular stock. 
    // If the user isn't present in the list, an exception is thrown. Otherwise the user is removed 
    // from the list
    @Override
    public synchronized void unSubscribe(User u, String product) throws NotSubscribedException {
        recorder.removeUser(u, product);

    }

    // Publish the ticker for a given stock with the associated new trading Price. The HashMap of stocks and Prices will contain the 
    // previous trading Price for the stock.  These will be compared, and their difference/equality will determine which of 4 characters
    // will be sent to the users: and equals sign if Prices equal, UP/DOWN arrows if Prices are of different values, and a space if 
    // the stock has never had a previous Price. The HashMap will then update the stock with the new trading Price. If the new trading
    // Price is null, use the PriceFactory to make a Price represented by $0.00 so that a null Price isn't passed to users. The publisher will
    // then publish the stock, new Price, and comparison result character to all interested users

    public synchronized void publishTicker(String product, Price p) {

        Price newTradePrice = adjustIfNull(p);

        if (!directions.containsProduct(product)) {
            directions.update(product, newTradePrice);
        }

        char direction = directions.calculateDirection(product, newTradePrice);

        if (!recorder.containsRecord(product)) {
            recorder.initStockRecord(product);
        }

        for (User u : recorder.getRegisteredUsers(product)) {
            u.acceptTicker(product, newTradePrice, direction);
        }

    }

    private static Price adjustIfNull(Price p) {
        return p == null ? PriceFactory.makeLimitPrice("$0.00") : p;
    }

}
