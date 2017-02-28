package domain;

import exceptions.InvalidDataException;
import price.Price;

/**
 * A class to represent the quotes within the stock exchange. A quote contains
 * references to two QuoteSide objects, one representing the buy side of the
 * quote and the other representing the sell side.
 * 
 * @author Peter Swantek
 * 
 */

public class Quote {

    private String userName;
    private String stockSymbol;
    private QuoteSide buy; // QuoteSide representing the buy side of the book
    private QuoteSide sell; // QuoteSide representing the sell side of the book

    // Constructs a new Quote object.  Contains parameters to indicate the user name, the stock product
    // to buy, and the buying/selling prices and volumes.  These parameters will be used to construct
    // the two QuoteSide objects.  The buy and sell QuoteSides will be constructed with the appropriate
    // parameters that were passed into the Quote constructor

    public Quote(String newUserName, String newProductSymbol, Price buyPrice, int buyVolume, Price sellPrice, int sellVolume) throws InvalidDataException {

        setUserName(newUserName);
        setStockSymbol(newProductSymbol);
        buy = new QuoteSide(newUserName, newProductSymbol, buyPrice, buyVolume, "BUY");
        sell = new QuoteSide(newUserName, newProductSymbol, sellPrice, sellVolume, "SELL");
    }

    // Strip leading and trailing whitespace, convert to upper case
    private void setUserName(String newUserName) throws InvalidDataException {
        if (newUserName == null || newUserName.trim().isEmpty()) {
            throw new InvalidDataException("Error: User name for a Quote can't be null or empty.");
        }

        userName = newUserName.trim().toUpperCase();
    }

    // Strip leading and trailing whitespace, convert to upper case
    private void setStockSymbol(String newProductSymbol) throws InvalidDataException {
        if (newProductSymbol == null || newProductSymbol.trim().isEmpty()) {
            throw new InvalidDataException("Error: Stock symbol for a Quote can't be null or empty.");
        }

        stockSymbol = newProductSymbol.trim().toUpperCase();
    }

    public String getUserName() {
        return userName;
    }

    public String getProduct() {
        return stockSymbol;
    }

    // Returns a new QuoteSide object that is a copy of either the buy or the sell QuoteSide
    public QuoteSide getQuoteSide(String side) {
        if (side.trim().toUpperCase().equals("BUY")) {
            return new QuoteSide(buy);
        }

        return new QuoteSide(sell);
    }

    @Override
    public String toString() {
        return getUserName() + " quote: " + getProduct() + " " + buy.toString() + " - " + sell.toString();
    }

}
