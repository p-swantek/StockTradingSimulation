package domain;

import price.Price;
import tradable.Tradable;
import tradable.TradableImplFactory;
import exceptions.InvalidDataException;

/**
 * @author Peter Swantek
 * 
 * The Order class is an object that represents an order within the stock exchange.
 * Since orders are objects that will be able to be traded within the stock exchange, it will 
 * implement the Tradable interface.  The Order object will also use delegation to delegate many of its
 * common behaviors to a class that implements the Tradable interface (a TradableImpl object).
 * 
 */

public class Order implements Tradable {
	
	private Tradable delegate;	// The reference to an implementation object
	private String orderId;
	
	// Creates a new Order with the given user name, the symbol of the stock to be ordered, the
	// price of that stock, the volume of the stock to order, and which side of the book the order will
	// represent.  Will construct an implementation object using these parameters.
	
	public Order(String newUserName, String newProductSymbol, Price orderPrice, 
			int originalVolume, String newSide) throws InvalidDataException {
		
		delegate = TradableImplFactory.makeTradable(newUserName, newProductSymbol, orderPrice, originalVolume, newSide);  // Use factory to create the delegate
		setOrderId();
	}
	
	/***** Getter/Setter methods *****/
	
	// Sets the ID for this particular order.
	private void setOrderId(){
		orderId = getUser() + getProduct() + getPrice().toString() + System.nanoTime();
	}
	
	/***** The Order delegates its behaviors to the implementation class *****/
	
	public String getProduct() {
		return delegate.getProduct();
	}

	
	public Price getPrice() {
		return delegate.getPrice();
	}

	
	public int getOriginalVolume() {
		return delegate.getOriginalVolume();
	}

	
	public int getRemainingVolume() {
		return delegate.getRemainingVolume();
	}

	
	public int getCancelledVolume() {
		return delegate.getCancelledVolume();
	}

	
	public void setCancelledVolume(int newCancelledVolume) throws InvalidDataException {
		delegate.setCancelledVolume(newCancelledVolume);
	}

	
	public void setRemainingVolume(int newRemainingVolume) throws InvalidDataException {
		delegate.setRemainingVolume(newRemainingVolume);
	}

	
	public String getUser() {
		return delegate.getUser();
	}

	
	public String getSide() {
		return delegate.getSide();
	}

	
	public boolean isQuote() {
		return delegate.isQuote();
	}

	// Order returns its ID, does not delegate this behavior.
	public String getId() {
		return orderId;
	}
	
	/***** Utilities *****/
	
	public String toString(){
		return getUser() + " order: " + getSide() + " " + getRemainingVolume() + " " + getProduct() +
				" at " + getPrice().toString() + " (Original Vol: " + getOriginalVolume() + ", CXL'd Vol: " +
				getCancelledVolume() + "), ID: " + getId();
	}
	

}
