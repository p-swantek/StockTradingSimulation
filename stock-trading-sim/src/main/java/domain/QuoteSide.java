package domain;

import price.Price;
import tradable.Tradable;
import tradable.TradableImplFactory;
import exceptions.InvalidDataException;

/**
 * @author Peter Swantek
 * 
 * The QuoteSide class is an object that represents a certain side of the book for a quote
 * within the stock exchange.  As a QuoteSide object can be traded, it will implement the Tradable
 * interface.  The QuoteSide object will also use delegation to delegate many of its
 * common behaviors to a class that implements the Tradable interface (a TradableImpl object).
 * 
 */

public class QuoteSide implements Tradable {
	
	private Tradable delegate;  // Reference to an implementation object
	private String orderId;
	
	// Creates a new QuoteSide object with the given user name, the symbol of the stock to be ordered, the
	// price of that stock, the volume of the stock to order, and which side of the book the order will
	// represent.  Will construct an implementation object using these parameters.
	
	public QuoteSide(String newUserName, String newProductSymbol, 
			Price sidePrice, int originalVolume, String newSide) throws InvalidDataException {
		
		delegate = TradableImplFactory.makeTradable(newUserName, newProductSymbol, sidePrice, originalVolume, newSide); // Use factory to create the delegate
		setOrderId();
	}
	
	// Copy constructor.  Performs a deep copy of the passed in QuoteSide object and returns a reference
	// to a newly created QuoteSide object with the same attributes as the passed in QuoteSide.
	// Since the QuoteSide to be copied is assumed to be valid, this constructor uses a try/catch so that the
	// client doesn't have to deal with an exception that will never get thrown.
	
	public QuoteSide(QuoteSide qs) {
		try {
		delegate = TradableImplFactory.makeTradable(qs.getUser(), qs.getProduct(), qs.getPrice(), qs.getOriginalVolume(), qs.getSide());  // Use factory to create the delegate
		setOrderId();
		}
		catch (InvalidDataException e){
			e.printStackTrace();
		}
	}
	
	/***** Getter/Setter methods *****/
	
	// Sets the ID for this QuoteSide
	private void setOrderId(){
		orderId = getUser() + getProduct() + System.nanoTime();
	}
	
	/***** The QuoteSide delegates its behaviors to the implementation class *****/
	
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

	// Doesn't delegate this behavior, returns true since a QuoteSide is part of a quote.
	public boolean isQuote() {
		return true;
	}

	// QuoteSide returns its ID, does not delegate this behavior.
	public String getId() {
		return orderId;
	}
	
	/***** Utilities *****/
	
	public String toString(){
		return getPrice().toString() + " x " + getRemainingVolume() + " (Original Vol: " + getOriginalVolume() +
				", CXL'd Vol: " + getCancelledVolume() + ") [" + getId() + "]";
	
	}
}
