package publishers;

import client.User;
import exceptions.AlreadySubscribedException;
import exceptions.NotSubscribedException;
import price.Price;
import price.PriceFactory;

/**
 * 
 * 
 * A class that publishes the data for the last sale in the stock exchange. This
 * class employs the singleton design pattern to guarantee that only one
 * instance is created. This class will allow users to sign up in order to be
 * sent relevant data about stocks that they are interested in.
 * 
 * @author Peter Swantek
 *
 */

public class LastSalePublisher implements Publisher {

    // Have data be volatile so that it is obtained from main memory instead of a thread's cached memory
    private static volatile LastSalePublisher theInstance = null;
    private static PublisherDataTracker recorder;

    private LastSalePublisher() {
        recorder = DataTrackers.getStockRecorder();
    }

    // Makes sure only one instance is created. Uses Double-Checked locking to make sure synchronization only occurs
    // for the initial creation of the instance
    public static LastSalePublisher getInstance() {
        if (theInstance == null) {
            synchronized (LastSalePublisher.class) {
                if (theInstance == null) {
                    theInstance = new LastSalePublisher();
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

    // Get the ArrayList of users for the stock passed in. Then, for each user
    // in that ArrayList call the acceptLastSale method, passing in the stock, Price, and trading volume.
    // If any price is null, have the PriceFactory make a price representing $0.00 and pass that to users,
    // not a null Price. After publishing the last sale to users, will then have the TickerPublisher
    // publish the ticker information for the passed in stock and Price

    public synchronized void publishLastSale(String product, Price lastSalePrice, int lastSaleVolume) {
        Price priceToPass = lastSalePrice;
        if (priceToPass == null) {
            priceToPass = PriceFactory.makeLimitPrice("$0.00");
        }

        if (!recorder.containsRecord(product)) {
            recorder.initStockRecord(product);
        }

        for (User u : recorder.getRegisteredUsers(product)) {
            u.acceptLastSale(product, priceToPass, lastSaleVolume);
        }

        TickerPublisher.getInstance().publishTicker(product, priceToPass);
    }

}
