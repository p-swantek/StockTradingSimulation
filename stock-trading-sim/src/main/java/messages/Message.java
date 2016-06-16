package messages;

import price.Price;

/**
 * @author Peter Swantek
 *
 * An interface that represents common behaviors by messages within the trading system.
 * 
 */

public interface Message {
	
	String getUser();
	
	String getProduct();
	
	Price getPrice();
	
	int getVolume();
	
	String getDetails();
	
	String getSide();
	
	String getId();

}
