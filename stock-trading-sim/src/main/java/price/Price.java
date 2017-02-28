package price;

import exceptions.InvalidPriceOperation;

/**
 * An immutable data class representing Prices within the stock market. Price
 * objects can be created and given a value so that they become instances of
 * Limit Prices. No value passed will result in the creation of a Market Price.
 * Supports a variety of mathematical operations on Price objects as well as
 * comparisons.
 * 
 * @author Peter Swantek
 */

public class Price implements Comparable<Price> {

    private long value;
    private boolean marketPrice; // A boolean to represent whether the price is a market price

    // Make a new Price object, make its value to the value passed in
    Price(long newValue) {
        setValue(newValue);
        marketPrice = false;
    }

    // Create a market price, set its value to 0.  Set the boolean to be true.
    Price() {
        setValue(0);
        setAsMarket();
    }

    private void setValue(long newValue) {
        value = newValue;
    }

    // If Price created is a market price, set the marketPrice boolean value accordingly
    private void setAsMarket() {
        marketPrice = true;
    }

    public long getValue() {
        return value;
    }

    public Price add(Price p) throws InvalidPriceOperation {
        if (isMarket() || p == null || p.isMarket()) {
            throw new InvalidPriceOperation("Couldn't add.  A price was either a market price or was null.");
        }

        return PriceFactory.makeLimitPrice(getValue() + p.getValue());
    }

    public Price subtract(Price p) throws InvalidPriceOperation {
        if (isMarket() || p == null || p.isMarket()) {
            throw new InvalidPriceOperation("Couldn't subtract.  A price was either a market price or was null.");
        }

        return PriceFactory.makeLimitPrice(getValue() - p.getValue());
    }

    public Price multiply(int value) throws InvalidPriceOperation {
        if (isMarket()) {
            throw new InvalidPriceOperation("Cannot perform multiplication on a market price.");
        }

        return PriceFactory.makeLimitPrice(getValue() * value);
    }

    @Override
    public int compareTo(Price p) {
        if (getValue() != p.getValue()) {
            return getValue() < p.getValue() ? -1 : 1;
        }

        return 0;
    }

    public boolean greaterOrEqual(Price p) {
        if (isMarket() || p.isMarket()) {
            return false;
        }

        return getValue() >= p.getValue();
    }

    public boolean greaterThan(Price p) {
        if (isMarket() || p.isMarket()) {
            return false;
        }

        return getValue() > p.getValue();
    }

    public boolean lessOrEqual(Price p) {
        if (this.isMarket() || p.isMarket()) {
            return false;
        }

        return getValue() <= p.getValue();
    }

    public boolean lessThan(Price p) {
        if (this.isMarket() || p.isMarket()) {
            return false;
        }

        return getValue() < p.getValue();
    }

    @Override
    public boolean equals(Object p) {
        if (p == null) {
            return false;
        }

        else if (p instanceof Price) {

            Price price = (Price) p;

            if (isMarket() || price.isMarket()) {
                return false;
            }

            return getValue() == price.getValue();
        }

        return false;

    }

    public boolean isMarket() {
        return marketPrice;
    }

    public boolean isNegative() {
        if (isMarket()) {
            return false;
        }

        return getValue() < 0;
    }

    @Override
    public String toString() {
        if (this.isMarket()) {
            return "MKT";
        }

        double formattedNumber = getValue() / 100.0;
        String string = String.format("$%,.2f", formattedNumber);
        return string;

    }

}
