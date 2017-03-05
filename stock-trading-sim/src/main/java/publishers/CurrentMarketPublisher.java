package publishers;

import client.User;
import exceptions.AlreadySubscribedException;
import exceptions.NotSubscribedException;
import price.Price;
import price.PriceFactory;

/**
 * A class that publishes the data for the current market in the stock exchange.
 * This class employs the singleton design pattern to guarantee that only one
 * instance is created. This class will allow users to sign up in order to be
 * sent relevant data about stocks that they are interested in. This publisher
 * keeps a record of all the stocks and which users are interested in each
 * particular stock, utilizes a record keeper object to manage the subscriptions
 * for stock
 * 
 * @author Peter Swantek
 *
 */

public class CurrentMarketPublisher implements Publisher {

    // Have data be volatile so that it is obtained from main memory instead of a thread's cached memory
    private static volatile CurrentMarketPublisher theInstance = null;
    private static PublisherDataTracker recorder;

    private CurrentMarketPublisher() {
        recorder = DataTrackers.getStockRecorder();
    }

    // Makes sure only one instance is created. Uses Double-Checked locking to make sure synchronization only occurs
    // for the initial creation of the instance
    public static CurrentMarketPublisher getInstance() {
        if (theInstance == null) {
            synchronized (CurrentMarketPublisher.class) {
                if (theInstance == null) {
                    theInstance = new CurrentMarketPublisher();
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

    // Get the ArrayList of users for the stock represented within the MarketDataDTO. Then, for each user
    // in that ArrayList call the acceptCurrentMarket method, passing in the data obtained from the DTO.
    // If any price is null, have the PriceFactory make a price representing $0.00 and pass that to users,
    // not a null Price

    public synchronized void publishCurrentMarket(MarketDataDTO md) {
        Price buyPrice = md.buyPrice;
        Price sellPrice = md.sellPrice;
        if (buyPrice == null) {
            buyPrice = PriceFactory.makeLimitPrice("$0.00");
        }

        if (sellPrice == null) {
            sellPrice = PriceFactory.makeLimitPrice("$0.00");
        }

        if (!recorder.containsRecord(md.product)) { //if there isn't a record for this stock product yet
            recorder.initStockRecord(md.product);
        }

        for (User u : recorder.getRegisteredUsers(md.product)) {
            u.acceptCurrentMarket(md.product, buyPrice, md.buyVolume, sellPrice, md.sellVolume);
        }

    }

}
