package publishers;

import client.User;
import exceptions.AlreadySubscribedException;
import exceptions.NotSubscribedException;
import messages.CancelMessage;
import messages.FillMessage;
import messages.MarketMessage;

/**
 * A class that publishes the particular messages in the stock exchange. This
 * class employs the singleton design pattern to guarantee that only one
 * instance is created. This class will allow users to sign up in order to be
 * sent relevant messages about stocks that they are interested in. Uses a
 * record keeper to keep track of all the subscribing information. All methods
 * will be synchronized as to be thread safe.
 * 
 * @author Peter Swantek
 *
 */

public class MessagePublisher implements Publisher {

    // Have data be volatile so that it is obtained from main memory instead of a thread's cached memory
    private static volatile MessagePublisher theInstance = null;
    private static PublisherDataTracker recorder;

    private MessagePublisher() {
        recorder = DataTrackers.getStockRecorder();
    }

    // Makes sure only one instance is created. Uses Double-Checked locking to make sure synchronization only occurs
    // for the initial creation of the instance
    public static MessagePublisher getInstance() {
        if (theInstance == null) {
            synchronized (MessagePublisher.class) {
                if (theInstance == null)
                    theInstance = new MessagePublisher();
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
    // in that ArrayList, pass the user the appropriate message type.

    // Publish CancelMessages to users based on stock within the cancel message.
    // Have the user whose name matches the user name of the message accept the message
    public synchronized void publishCancel(CancelMessage cm) {
        if (!recorder.containsRecord(cm.getProduct())) {
            recorder.initStockRecord(cm.getProduct());
        }

        for (User u : recorder.getRegisteredUsers(cm.getProduct())) {
            if (u.getUserName().equals(cm.getUser())) {
                u.acceptMessage(cm);
            }
        }

    }

    // Publish FillMessages to users based on stock within the fill message.
    // Have the user whose name matches the user name of the message accept the message
    public synchronized void publishFill(FillMessage fm) {
        if (!recorder.containsRecord(fm.getProduct())) {
            recorder.initStockRecord(fm.getProduct());
        }

        for (User u : recorder.getRegisteredUsers(fm.getProduct())) {
            if (u.getUserName().equals(fm.getUser())) {
                u.acceptMessage(fm);
            }
        }
    }

    // Publish the market state to the users, regardless of the stock they are interested in.  Get all the lists
    // of subscribed users as a collection and iterate through the set of lists, iterating over the users in each 
    // list. For each user, have them accept the market state represented in the MarketMessage
    public synchronized void publishMarketMessage(MarketMessage mm) {
        for (User u : recorder.getAllUsers()) {
            u.acceptMarketMessage(mm.toString());
        }
    }

}
