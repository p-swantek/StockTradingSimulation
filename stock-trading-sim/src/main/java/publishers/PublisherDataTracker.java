package publishers;

import java.util.List;

import client.User;
import exceptions.AlreadySubscribedException;
import exceptions.NotSubscribedException;

/**
 * General behaviors to add or remove records of stocks and interested users for
 * publication
 * 
 * @author Peter Swantek
 *
 */
public interface PublisherDataTracker {

    /**
     * Records a user as being interested in the given stock product
     * 
     * @param u an interested user, should be notified of stock changes
     * @param product the product this user is interested in
     */
    void addUser(User u, String product) throws AlreadySubscribedException;

    /**
     * Remove a user from receiving notifications for the given product
     * 
     * @param u the user that would like to be not be notified when this stock
     *            changes
     * @param product the stock product
     */
    void removeUser(User u, String product) throws NotSubscribedException;

    /**
     * Obtain the list of users for this stock product
     * 
     * @param stock the stock to get users for
     * @return a list of users interseted in this stock. If stock is null or
     *         can't be located, returns an empty list
     */
    List<User> getRegisteredUsers(String stock);

    /**
     * Initialize a certain stock with a list to hold users that are interested
     * 
     * @param stock the product which users will register for
     * @param lst the list of users for this stock
     */
    void initStockRecord(String stock);

    /**
     * Indicates whether the tracker has a record for a certain stock
     * 
     * @param stock the stock product to check
     * @return true if there is a record of this product, false otherwise
     */
    boolean containsRecord(String stock);

    /**
     * Obtain all the users that this tracker is keeping records of
     * 
     * @return a list of all users in this record keeper
     */
    List<User> getAllUsers();

}
