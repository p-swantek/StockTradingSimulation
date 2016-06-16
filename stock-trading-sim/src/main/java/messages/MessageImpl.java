package messages;

import price.Price;
import exceptions.InvalidDataException;

/**
 * @author Peter Swantek
 * 
 * A class that provides the default implementation of the Message interface. Will maintain the common data fields
 * for Messages as well as provide default implementations for behaviors defined by the Message interface. This class
 * makes sure to set all data fields to valid values; there can be no null Prices or Strings, and Strings can't be empty.
 * The side must be set to a valid side, which is represented by the two following Strings: "BUY" and "SELL".
 *
 */

public class MessageImpl implements Message {
	
	private String user;
	private String product;
	private Price price;
	private int volume;
	private String details;
	private String side;
	private String id;
	
	// Have the constructor set all the data fields, throws an exception if any data passed in is invalid
	
	public MessageImpl(String newUser, String newProduct, Price newPrice, int newVolume,
			String newDetails, String newSide, String newId) throws InvalidDataException {
		
		setUser(newUser);
		setProduct(newProduct);
		setPrice(newPrice);
		setVolume(newVolume);
		setDetails(newDetails);
		setSide(newSide);
		setId(newId);
	}
	
	// These methods set the data fields, they make sure that the data being used to set a respective field is valid
	// Invalid data includes: null Prices/Strings a field; A field being set to an empty String; The volume for a trade being 
	// negative.  The side MUST be either "BUY" or "SELL" or it is considered an invalid value
	
	private void setUser(String newUser) throws InvalidDataException {
		if (newUser == null || newUser.trim().isEmpty())
			throw new InvalidDataException("Error: The user name for a message can't be null or empty.");
		user = newUser.trim().toUpperCase();
	}
	
	private void setProduct(String newProduct) throws InvalidDataException {
		if (newProduct == null || newProduct.trim().isEmpty())
			throw new InvalidDataException("Error: The product for a message can't be null or empty.");
		product = newProduct.trim().toUpperCase();
	}
	
	private void setPrice(Price newPrice) throws InvalidDataException {
		if (newPrice == null)
			throw new InvalidDataException("Error: The price for a message can't be null.");
		price = newPrice;
	}
	
	private void setVolume(int newVolume) throws InvalidDataException {
		if (newVolume < 0)
			throw new InvalidDataException("Error: The volume for a message can't be negative.");
		volume = newVolume;
	}
	
	private void setDetails(String newDetails) throws InvalidDataException {
		if (newDetails == null || newDetails.trim().isEmpty())
			throw new InvalidDataException("Error: The details for a message can't be null or empty.");
		details = newDetails;
	}
	
	private void setSide(String newSide) throws InvalidDataException {
		if (newSide == null || newSide.trim().isEmpty())
			throw new InvalidDataException("Error: The message's market side was either null or empty.");
		
		if (!newSide.trim().toUpperCase().equals("BUY") && !newSide.trim().toUpperCase().equals("SELL"))
			throw new InvalidDataException("Error: The side of the message can only be BUY or SELL.");
		
		side = newSide.trim().toUpperCase();
	}
	
	private void setId(String newId) throws InvalidDataException {
		if (newId == null || newId.trim().isEmpty())
			throw new InvalidDataException("Error: The message was passed an invalid id.");
		id = newId;
	}
	
	// These are the default implementations for the behaviors defined by the Message interface
	
	public String getUser() {
		return user;
	}
	
	public String getProduct() {
		return product;
	}
	
	public Price getPrice() {
		return price;
	}

	public int getVolume() {
		return volume;
	}

	public String getDetails() {
		return details;
	}
	
	public String getSide() {
		return side;
	}

	public String getId() {
		return id;
	}

}
