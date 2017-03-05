package publishers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import client.User;
import exceptions.AlreadySubscribedException;
import exceptions.NotSubscribedException;

/**
 * Class that takes care of managing the records of lists of users that are
 * associated with a certain stock
 * 
 * @author Peter Swantek
 *
 */

class StockRecordKeeper implements PublisherDataTracker {

    private Map<String, List<User>> subscriberRecord;

    public StockRecordKeeper() {
        subscriberRecord = new HashMap<>();
    }

    @Override
    public void addUser(User u, String product) throws AlreadySubscribedException {
        if (!subscriberRecord.containsKey(product)) {
            ArrayList<User> newUserList = new ArrayList<User>();
            newUserList.add(u);
            subscriberRecord.put(product, newUserList);
        }

        else {
            List<User> userList = subscriberRecord.get(product);
            if (userList.contains(u)) {
                throw new AlreadySubscribedException("Error: " + u.getUserName() + " was already subscribed to: " + product);
            }
            userList.add(u);
        }

    }

    @Override
    public void removeUser(User u, String product) throws NotSubscribedException {
        List<User> userList = subscriberRecord.get(product);
        if (!userList.remove(u)) {
            throw new NotSubscribedException("Error: " + u.getUserName() + " was not subscribed to: " + product);
        }

    }

    @Override
    public List<User> getRegisteredUsers(String stock) {
        List<User> users = subscriberRecord.get(stock);
        return users == null ? new ArrayList<User>() : new ArrayList<User>(users);
    }

    @Override
    public void initStockRecord(String stock) {
        subscriberRecord.put(stock, new ArrayList<User>());
    }

    @Override
    public boolean containsRecord(String stock) {
        return subscriberRecord.containsKey(stock);
    }

    @Override
    public List<User> getAllUsers() {
        List<User> allUsers = new ArrayList<>();

        for (List<User> list : subscriberRecord.values()) {
            allUsers.addAll(list);
        }

        return allUsers;
    }

}
