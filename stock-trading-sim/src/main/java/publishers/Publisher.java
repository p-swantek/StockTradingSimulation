package publishers;

import client.User;
import exceptions.AlreadySubscribedException;
import exceptions.NotSubscribedException;

/**
 * @author Peter Swantek
 * 
 * A set of behaviors to be implemented by publishers within the trading system.  Publishers will be using the observer
 * design pattern to publish information to users who register to receive that information.  All publishers will have the 
 * common behaviors of being able to subscribe and unsubscribe users for certain stocks.
 *
 */
public interface Publisher {
	
	void subscribe(User u, String product) throws AlreadySubscribedException;
	
	void unSubscribe(User u, String product) throws NotSubscribedException;
	

}
