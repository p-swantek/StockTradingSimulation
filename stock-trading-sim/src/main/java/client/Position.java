package client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import exceptions.InvalidDataException;
import exceptions.InvalidPriceOperation;
import price.Price;
import price.PriceFactory;

/**
 * Maintains and records the relevant data for the position of a user in the
 * trading system. This class contains behavior and data that will allow the
 * position of a user to be updated and changed as the user makes trades within
 * the trading system. A user's postion will reflect his or her stock holdings
 * as well as the balance of the holdings and balance changes due to trading.
 * 
 * @author Peter Swantek
 *
 */

public class Position {

    private Map<String, Integer> holdings; // Stock holdings
    private Price accountCosts; // Initial account costs before any updates
    private Map<String, Price> lastSales; // Record of all the previous sales for this user

    public Position() {
        holdings = new HashMap<>();
        accountCosts = PriceFactory.makeLimitPrice("0.00");
        lastSales = new HashMap<>();
    }

    // Updates the position for the buy/sell side of a particular stock with the new prices and volumes
    // Will grab the stock from the record of stocks and update the current holding volume for that stock, will remove the stock
    // from the record if the holding volume becomes 0

    public void updatePosition(String product, Price price, String side, int volume) throws InvalidDataException, InvalidPriceOperation {
        if (product == null || product.trim().isEmpty()) {
            throw new InvalidDataException("Error: A user's position was updated with a product that was null or empty.");
        } else if (price == null) {
            throw new InvalidDataException("Error: A user's position was updated with a null Price.");
        } else if (side == null || side.trim().isEmpty()) {
            throw new InvalidDataException("Error: A user's position was updated with a side that was null or empty.");
        } else if (!side.trim().toUpperCase().equals("BUY") && !side.trim().toUpperCase().equals("SELL")) {
            throw new InvalidDataException("Error: The user's position was to be updated, but the side passed in wasn't BUY or SELL.");
        }

        int adjustedVolume;

        // Adjust the volume based on the side
        if (side.trim().toUpperCase().equals("BUY")) {
            adjustedVolume = volume;
        } else {
            adjustedVolume = -volume;
        }

        if (!holdings.containsKey(product.trim().toUpperCase())) { // No record of stock, put it into the record
            holdings.put(product.trim().toUpperCase(), adjustedVolume);
        } else {
            int currentHoldingVolume = holdings.get(product.trim().toUpperCase()); // Grab the relevant stock and its volume, update with new volume or remove if volume is 0
            currentHoldingVolume += adjustedVolume;
            if (currentHoldingVolume == 0) {
                holdings.remove(product.trim().toUpperCase());
            } else {
                holdings.put(product.trim().toUpperCase(), currentHoldingVolume);
            }

        }
        // Update the account costs based on the price and volume of the stock
        Price multiplied = price.multiply(volume);
        if (side.trim().toUpperCase().equals("BUY")) {
            accountCosts = accountCosts.subtract(multiplied);
        } else {
            accountCosts = accountCosts.add(multiplied);
        }
    }

    // Update the record for the last sale of this particular stock
    public void updateLastSale(String product, Price price) throws InvalidDataException {
        if (product == null || product.trim().isEmpty()) {
            throw new InvalidDataException("Error: A user attempted to update the last sale position with a null or empty value.");
        } else if (price == null) {
            throw new InvalidDataException("Error: A user attempted to update the last sale position using a null Price.");
        }

        lastSales.put(product.trim().toUpperCase(), price);

    }

    // Grabs the volume associated with this stock
    public int getStockPositionVolume(String product) throws InvalidDataException {
        if (product == null || product.trim().isEmpty()) {
            throw new InvalidDataException("Error: A user attempted to obtain the position for a stock which was null or empty.");
        }

        else if (!holdings.containsKey(product.trim().toUpperCase())) {
            return 0;
        }

        return holdings.get(product.trim().toUpperCase());

    }

    // Return a list of all the stocks currently held by this user
    public List<String> getHoldings() {

        List<String> holdingKeys = new ArrayList<>(holdings.keySet());
        Collections.sort(holdingKeys);

        return holdingKeys;
    }

    // Grabs the current value associated with this particular stock, will return $0.00 if the stock isn't being held currently
    public Price getStockPositionValue(String product) throws InvalidDataException, InvalidPriceOperation {
        if (product == null || product.trim().isEmpty()) {
            throw new InvalidDataException("Error: A user attempted to get a stock position's value, but was supplied with a null or empty stock symbol.");
        }

        else if (holdings.get(product.trim().toUpperCase()) == null) {
            return PriceFactory.makeLimitPrice("0.00");
        }

        Price lastSalePrice = lastSales.get(product.trim().toUpperCase());
        if (lastSalePrice == null) {
            return PriceFactory.makeLimitPrice("0.00");
        }

        Price positionValue = lastSalePrice.multiply(holdings.get(product.trim().toUpperCase()));
        return positionValue;
    }

    public Price getAccountCosts() {
        return accountCosts;
    }

    // Sums the values for every stock currently held and returns the total value of all these stocks
    public Price getAllStockValue() throws InvalidPriceOperation, InvalidDataException {
        Price totalValue = PriceFactory.makeLimitPrice("0.00");

        for (String product : holdings.keySet()) {
            totalValue = totalValue.add(getStockPositionValue(product));
        }

        return totalValue;
    }

    // Updates the net value of the account for a user based on the total value of all stocks held
    public Price getNetAccountValue() throws InvalidDataException, InvalidPriceOperation {
        Price totalStockValue = getAllStockValue();

        return totalStockValue.add(accountCosts);

    }

}
