package price;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Peter Swantek
 * 
 * A Price Factory to create the different types of Price objects.
 * Implements the flyweight design pattern, keeps a record of previously created
 * Price objects in a HashMap.  If requested to make a certain Price object, the factory
 * checks if that Price has been created already and returns a reference to it if it has. 
 * Returns a new Price instance if one has not been created yet.  The factory also creates
 * a single instance of a market price and keeps a reference to it.  When a request is made
 * for a market price object, this reference is then returned.
 *
 */
public class PriceFactory {
	
	private static Map<String, Price> pricesCreated = new HashMap<String, Price>(); // The record of created Price objects.
	private static Price marketPrice = new Price();   // Only keep a reference to a single market price reference.
	
	// Makes a limit price based on the given String representing a dollar amount.
	// Will parse the String into a long value representing the amount of cents in the value.
	// Passes the value to a static method that makes a limit price from a long value.
	
	public static Price makeLimitPrice(String newValue){
		long cents = parseLong(newValue);
		Price createdPrice = makeLimitPrice(cents);
		return createdPrice;
	}
	
	// Creates a limit price with a value equal to the long that is passed in.
	// Keeps track of Price objects previously created with a HashMap.
	// If the requested Price has already been created, return a reference to that Price.
	// If the requested Price has not been created, create a new Price and put an entry for the new Price in the HashMap.
	
	public static Price makeLimitPrice(long newValue){
		String key = String.valueOf(newValue);
		Price createdPrice = pricesCreated.get(key);
		if (createdPrice == null){
			createdPrice = new Price(newValue);
			pricesCreated.put(key, createdPrice);
			return createdPrice;
		}
		
		return createdPrice;
	}
	
	// When a market price is requested, return the reference of the single market price object.
	
	public static Price makeMarketPrice(){
		return marketPrice;
	}
	
	// Parse a string representing a dollar amount.  
	// Strips the '$', '.', and ',' characters and converts the string to a long.
	// Should refine this possibly?
	
	private static long parseLong(String dollarAmount){
		String newString = dollarAmount.replaceAll("[$ ,]", "");
		double amount = Double.parseDouble(newString);
		double doubleValue = Math.round(amount * 100.0);
		long cents = (long)doubleValue;
		return cents;
		
	}
}
