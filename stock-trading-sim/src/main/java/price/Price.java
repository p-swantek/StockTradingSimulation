package price;


/**
 * @author Peter Swantek
 * 
 * An immutable data class representing Prices within the stock market.
 * Price objects can be created and given a value so that they become 
 * instances of Limit Prices.  No value passed will result in the creation
 * of a Market Price.  Supports a variety of mathematical operations on 
 * Price objects as well as comparisons.
 * 
 *
 */

public class Price implements Comparable<Price> {
	
	private long value;
	private boolean marketPrice = false; // A boolean to represent whether the price is a market price
	
	// Make a new Price object, make its value to the value passed in
	Price(long newValue){
		setValue(newValue);
	}
	
	// Create a market price, set its value to 0.  Set the boolean to be true.
	Price(){
		setValue(0);
		setMarket();
	}
	
	/***** Getter/Setter methods *****/
	
	private void setValue(long newValue){
		value = newValue;
	}
	
	// If Price created is a market price, set the marketPrice boolean value accordingly
	private void setMarket(){
		marketPrice = true;
	}
	
	
	public long getValue(){
		return value;
	}
	
	/***** Price math Operations *****/
	
	
	public Price add(Price p) throws InvalidPriceOperation {
		if (this.isMarket() || p.isMarket() || p == null)
			throw new InvalidPriceOperation("Couldn't add.  A price was either a market price or was null.");
		
		long summedValue = this.getValue() + p.getValue();
		return PriceFactory.makeLimitPrice(summedValue);
	}
	
	
	public Price subtract(Price p) throws InvalidPriceOperation {
		if (this.isMarket() || p.isMarket() || p == null)
			throw new InvalidPriceOperation("Couldn't subtract.  A price was either a market price or was null.");
		
		long difference = this.getValue() - p.getValue();
		return PriceFactory.makeLimitPrice(difference);
	}
	
	
	public Price multiply(int value) throws InvalidPriceOperation {
		if (this.isMarket())
			throw new InvalidPriceOperation("Cannot perform multiplication on a market price.");
		
		long product = this.getValue() * value;
		return PriceFactory.makeLimitPrice(product);
	}
	
	/***** Price Comparison Operations *****/
	
	public int compareTo(Price p) {
		if (this.getValue() != p.getValue())
			return (this.getValue() < p.getValue() ? -1 : 1);
		
		return 0;
	}
	
	
	public boolean greaterOrEqual(Price p){
		if (this.isMarket() || p.isMarket())
			return false;
		
		return (this.getValue() >= p.getValue() ? true : false);
	}
	
	public boolean greaterThan(Price p){
		if (this.isMarket() || p.isMarket())
			return false;
		
		return (this.getValue() > p.getValue() ? true : false);
	}
	
	public boolean lessOrEqual(Price p){
		if (this.isMarket() || p.isMarket())
			return false;
		
		return (this.getValue() <= p.getValue() ? true : false);
	}
	
	public boolean lessThan(Price p){
		if (this.isMarket() || p.isMarket())
			return false;
		
		return (this.getValue() < p.getValue() ? true : false);
	}
	
	public boolean equals(Price p){
		if (this.isMarket() || p.isMarket())
			return false;
		
		return (this.getValue() == p.getValue() ? true : false);
	}
	
	public boolean isMarket(){
		return marketPrice;
	}
	
	public boolean isNegative(){
		if(this.isMarket())
			return false;
		
		return (this.getValue() < 0 ? true : false);
	}
	
	/***** Utilities *****/
	
	public String toString(){
		if (this.isMarket())
			return "MKT";

		double formattedNumber = this.getValue() / 100.0;
		String string = String.format("$%,.2f", formattedNumber);
		return string;
	
	}
	
}

