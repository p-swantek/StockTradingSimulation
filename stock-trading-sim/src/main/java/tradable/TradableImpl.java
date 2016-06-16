package tradable;

import exceptions.InvalidDataException;
import price.Price;

/**
 * @author Peter Swantek
 * 
 * A class that provides the default implementations for the Tradable interface.  Other classes that
 * implement the Tradable interface will own a reference to this implementation class.  These classes
 * can then delegate behaviors to be performed by this implementation class.
 * 
 */

public class TradableImpl implements Tradable {
	
	private String userName;
	private String stockSymbol;
	private String side;
	private Price price;
	private String originalOrderVolume;
	private String remainingOrderVolume;
	private String cancelledOrderVolume = "0";
	
	// Constructs a new TradableImpl object, will use the given parameters to set the data fields.

	public TradableImpl(String newUserName, String newProductSymbol, Price orderPrice,
			int originalVolume, String newSide) throws InvalidDataException {
		
		setUserName(newUserName);
		setStockSymbol(newProductSymbol);
		setSide(newSide);
		setPrice(orderPrice);
		setOriginalOrderVolume(originalVolume);
		setRemainingVolume(originalVolume);
	}
	
	/***** Getter/Setter methods *****/
	// The string parameter passed in is stripped of leading and trailing whitespace; converted to uppercase
	private void setUserName(String newUserName) throws InvalidDataException {
		if (newUserName == null || newUserName.trim().isEmpty())
			throw new InvalidDataException("Error: User name can't be null or empty.");
		userName = newUserName.trim().toUpperCase();
	}
	
	// The string parameter passed in is stripped of leading and trailing whitespace; converted to uppercase
	private void setStockSymbol(String newProductSymbol) throws InvalidDataException {
		if (newProductSymbol == null || newProductSymbol.trim().isEmpty())
			throw new InvalidDataException("Error: Stock symbol can't be null or empty.");
		stockSymbol = newProductSymbol.trim().toUpperCase();
	}
	
	// The string parameter passed in is stripped of leading and trailing whitespace; converted to uppercase
	private void setSide(String newSide) throws InvalidDataException {
		if (newSide == null || newSide.trim().isEmpty())
			throw new InvalidDataException("Error: Bookside can't be null or empty.");
		
		if (!newSide.trim().toUpperCase().equals("BUY") && !newSide.trim().toUpperCase().equals("SELL"))
			throw new InvalidDataException("Error: The value for a bookside can only be either BUY or SELL.");
		
		side = newSide.trim().toUpperCase();
	}
	
	private void setPrice(Price newPrice) throws InvalidDataException {
		if (newPrice == null)
			throw new InvalidDataException("Error: A null price was passed in.");
		price = newPrice;
	}
	
	private void setOriginalOrderVolume(int newOriginalOrderVolume) throws InvalidDataException {
		if (newOriginalOrderVolume <= 0)
			throw new InvalidDataException("Error: Volume was less than or equal to 0.");
		originalOrderVolume = String.valueOf(newOriginalOrderVolume);
	}

	
	public String getProduct() {
		return stockSymbol;
	}

	
	public Price getPrice() {
		return price;
	}

	
	public int getOriginalVolume() {
		return Integer.parseInt(originalOrderVolume);
	}

	
	public int getRemainingVolume() {
		return Integer.parseInt(remainingOrderVolume);
	}

	
	public int getCancelledVolume() {
		return Integer.parseInt(cancelledOrderVolume);
	}

	
	public void setCancelledVolume(int newCancelledVolume) throws InvalidDataException {
		if (newCancelledVolume < 0 || (newCancelledVolume + this.getRemainingVolume() > this.getOriginalVolume()))
				throw new InvalidDataException("Error: Cancelled volume was either less than 0 or set to be greater than the original volume.");
		cancelledOrderVolume = String.valueOf(newCancelledVolume);
	}

	
	public void setRemainingVolume(int newRemainingVolume) throws InvalidDataException { 
		if (newRemainingVolume < 0 || (newRemainingVolume + this.getCancelledVolume() > this.getOriginalVolume()))
				throw new InvalidDataException("Error: Remaining volume was either less than 0 or set to be greater than the original volume.");
		remainingOrderVolume = String.valueOf(newRemainingVolume);
	}

	
	public String getUser() {
		return userName;
	}

	
	public String getSide() {
		return side;
	}

	// Default implementation is false, represents an Order
	public boolean isQuote() {
		return false;
			
	}

	// Each class that owns this implementor overrides this method
	public String getId() {
		return null;
	}
	
}