package publishers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import client.User;
import exceptions.AlreadySubscribedException;
import exceptions.NotSubscribedException;
import messages.CancelMessage;
import messages.FillMessage;
import messages.MarketMessage;

/**
 * @author Peter Swantek
 * 
 * A class that publishes the particular messages in the stock exchange.  This class employs the singleton design pattern
 * to guarantee that only one instance is created. This class will allow users to sign up in order to be sent relevant messages
 * about stocks that they are interested in. This publisher keeps a record of all the stocks and which users are interested
 * in each particular stock, maintains this by using a HashMap with the stock as the key and an ArrayList of users interested
 * in that stock as the value.  All methods will be synchronized as to be thread safe.
 *
 */

public class MessagePublisher implements Publisher {
	
	// Have data be volatile so that it is obtained from main memory instead of a thread's cached memory
	private volatile static MessagePublisher theInstance = null;
	private static Map<String, ArrayList<User>> subscriberRecord = new HashMap<String, ArrayList<User>>();

	private MessagePublisher(){}
	
	// Makes sure only one instance is created. Uses Double-Checked locking to make sure synchronization only occurs
	// for the initial creation of the instance
	public static MessagePublisher getInstance() {
		if (theInstance == null){
			synchronized(MessagePublisher.class){
				if (theInstance == null)
					theInstance = new MessagePublisher();
			}
		}
			
		return theInstance;
	}
	
	// Subscribes a user for a certain stock.  Finds the ArrayList of users for the stock
	// within the HashMap.  If the user is already a subscriber then an exception will be thrown, otherwise
	// the user is added to the ArrayList associated with that stock
	
	public synchronized void subscribe(User u, String product) throws AlreadySubscribedException {
		if (!subscriberRecord.containsKey(product)){
			ArrayList<User> newUserList = new ArrayList<User>();
			newUserList.add(u);
			subscriberRecord.put(product, newUserList);
		}
		
		else {
			ArrayList<User> userList = subscriberRecord.get(product);
			if (userList.contains(u))
				throw new AlreadySubscribedException("Error: " + u.getUserName() + " was already subscribed to: " + product);
			userList.add(u);
		}
	}

	// Unsubscribes a user for a stock.  Finds the ArrayList of users for the particular stock. 
	// If the user isn't present in the list, an exception is thrown. Otherwise the user is removed 
	// from the list
	
	public synchronized void unSubscribe(User u, String product) throws NotSubscribedException {
		ArrayList<User> userList = subscriberRecord.get(product);
		if (!userList.contains(u))
			throw new NotSubscribedException("Error: " + u.getUserName() + " was not subscribed to: " + product);
		
		userList.remove(u);
		
	}
	
	// Get the ArrayList of users for the stock passed in. Then, for each user
	// in that ArrayList, pass the user the appropriate message type.
	
	// Publish CancelMessages to users based on stock within the cancel message.
	// Have the user whose name matches the user name of the message accept the message
	public synchronized void publishCancel(CancelMessage cm){
		ArrayList<User> userList = subscriberRecord.get(cm.getProduct());
		if (userList == null){
			userList = new ArrayList<User>();
			subscriberRecord.put(cm.getProduct(), userList);
		}
		
		for (User u : userList)
			if (u.getUserName().equals(cm.getUser()))
				u.acceptMessage(cm);
		
	}
	
	// Publish FillMessages to users based on stock within the fill message.
	// Have the user whose name matches the user name of the message accept the message
	public synchronized void publishFill(FillMessage fm){
		ArrayList<User> userList = subscriberRecord.get(fm.getProduct());
		if (userList == null){
			userList = new ArrayList<User>();
			subscriberRecord.put(fm.getProduct(), userList);
		}
		
		for (User u : userList)
			if (u.getUserName().equals(fm.getUser()))
				u.acceptMessage(fm);
	}
	
	// Publish the market state to the users, regardless of the stock they are interested in.  Get all the lists
	// of subscribed users as a collection and iterate through the set of lists, iterating over the users in each 
	// list. For each user, have them accept the market state represented in the MarketMessage
	public synchronized void publishMarketMessage(MarketMessage mm){
		Collection<ArrayList<User>> userLists = subscriberRecord.values();
		for (ArrayList<User> list : userLists)
			for (User u : list)
				u.acceptMarketMessage(mm.toString());
	}

}
