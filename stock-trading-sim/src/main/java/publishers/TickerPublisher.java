package publishers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import client.User;
import exceptions.AlreadySubscribedException;
import exceptions.NotSubscribedException;
import price.Price;
import price.PriceFactory;

/**
 * @author Peter Swantek
 * 
 * A class that publishes the stock ticker for a stock in the stock exchange.  This class employs the singleton design pattern
 * to guarantee that only one instance is created. This class will allow users to sign up in order to be sent tickers for the
 * stocks that they are interested in. This publisher keeps a record of all the stocks and which users are interested
 * in each particular stock, maintains this by using a HashMap with the stock as the key and an ArrayList of users interested
 * in that stock as the value. This publisher also maintains a HashMap with stocks as keys and the most recent
 * trading Price object for that stock as the value. This will allow the publisher to tell users
 * if the stock is trading at an increased/decreased/equal Price as compared to the previous Price.
 *  All methods will be synchronized as to be thread safe.
 *
 */

public class TickerPublisher implements Publisher {

	// Have data be volatile so that it is obtained from main memory instead of a thread's cached memory
	private volatile static TickerPublisher theInstance = null;
	private static Map<String, ArrayList<User>> subscriberRecord = new HashMap<String, ArrayList<User>>();
	private static Map<String, Price> recentTickerValues = new HashMap<String, Price>();  // Record of stocks and their most recent trading Price
	
	private TickerPublisher(){}
	
	// Makes sure only one instance is created. Uses Double-Checked locking to make sure synchronization only occurs
	// for the initial creation of the instance
	public static TickerPublisher getInstance() {
		if (theInstance == null){
			synchronized(TickerPublisher.class){
				if (theInstance == null)
					theInstance = new TickerPublisher();
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
	
	// Publish the ticker for a given stock with the associated new trading Price. The HashMap of stocks and Prices will contain the 
	// previous trading Price for the stock.  These will be compared, and their difference/equality will determine which of 4 characters
	// will be sent to the users: and equals sign if Prices equal, UP/DOWN arrows if Prices are of different values, and a space if 
	// the stock has never had a previous Price. The HashMap will then update the stock with the new trading Price. If the new trading
	// Price is null, use the PriceFactory to make a Price represented by $0.00 so that a null Price isn't passed to users. The publisher will
	// then publish the stock, new Price, and comparison result character to all interested users
	
	public synchronized void publishTicker(String product, Price p){
		char direction = ' ';
		Price newTradePrice = p;
		if (newTradePrice == null)
			newTradePrice = PriceFactory.makeLimitPrice("$0.00");
		
		if (!recentTickerValues.containsKey(product)){
			recentTickerValues.put(product, newTradePrice);
		}
		
		else{
			Price previousPrice = recentTickerValues.get(product);
			int compareResult = previousPrice.compareTo(newTradePrice);
			direction = (compareResult == 0 ? '=' : (compareResult == 1 ? ((char)8595) : ((char)8593)));
			recentTickerValues.replace(product, newTradePrice);
		}
	
		ArrayList<User> userList = subscriberRecord.get(product);
		if (userList == null){
			userList = new ArrayList<User>();
			subscriberRecord.put(product, userList);
		}
		
		for (User u : userList)
			u.acceptTicker(product, newTradePrice, direction);
		
	}
	

}
