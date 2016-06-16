package tradable;

import price.Price;

/**
 * @author Peter Swantek
 * 
 * A Data Transfer Object that holds all the important data of a tradable Object.
 * 
 */

public class TradableDTO {
	
	public String product;
	public Price price;
	public int originalVolume;
	public int remainingVolume;
	public int cancelledVolume;
	public String user;
	public String side;
	public boolean isQuote;
	public String id;
	
	// Construct the data transfer object by passing in all relevant information from a tradable object.
	public TradableDTO(String newProduct, Price newPrice, int newOriginalVolume, int newRemainingVolume, 
						int newCancelledVolume, String newUser, String newSide, boolean newIsQuote, String newId){
		product = newProduct;
		price = newPrice;
		originalVolume = newOriginalVolume;
		remainingVolume = newRemainingVolume;
		cancelledVolume = newCancelledVolume;
		user = newUser;
		side = newSide;
		isQuote = newIsQuote;
		id = newId;
	}
	
	
	/***** Utilities *****/
	
	public String toString(){
		return "Product: " + product + ", Price: " + price + ", OriginalVolume: " + originalVolume 
				+ ", RemainingVolume: " + remainingVolume + ", CancelledVolume: " + cancelledVolume 
				+ ", User: " + user + ", Side: " + side + ", IsQuote: " + isQuote + ", Id: " + id;
	}
}
