package client;

import exceptions.InvalidDataException;

/**
 * Holds all the data relevant to any tradable that the user has submitted to
 * the trading system.
 * 
 * @author Peter Swantek
 *
 */
public class TradableUserData {

    private String userName;
    private String stockSymbol;
    private String side;
    private String orderId;

    public TradableUserData(String newUserName, String newStock, String newSide, String newOrderId) throws InvalidDataException {
        setUserName(newUserName);
        setStockSymbol(newStock);
        setSide(newSide);
        setOrderId(newOrderId);

    }

    private void setUserName(String newUserName) throws InvalidDataException {
        if (newUserName == null || newUserName.trim().isEmpty()) {
            throw new InvalidDataException("Error: A TradableUserData was passed a null or empty userName on creation.");
        }

        userName = newUserName.trim().toUpperCase();

    }

    private void setStockSymbol(String newStock) throws InvalidDataException {
        if (newStock == null || newStock.trim().isEmpty()) {
            throw new InvalidDataException("Error: A TradableUserData was passed a null or empty stock symbol on creation.");
        }
        stockSymbol = newStock.trim().toUpperCase();

    }

    private void setSide(String newSide) throws InvalidDataException {
        if (newSide == null || newSide.trim().isEmpty()) {
            throw new InvalidDataException("Error: A TradableUserData was passed an invalid side on creation.");
        }

        side = newSide.trim().toUpperCase();

    }

    private void setOrderId(String newOrderId) throws InvalidDataException {
        if (newOrderId == null || newOrderId.trim().isEmpty()) {
            throw new InvalidDataException("Error: A TradableUserData was passed a null or empty orderId on creation.");
        }

        orderId = newOrderId;

    }

    public String getUserName() {
        return userName;
    }

    public String getStockSymbol() {
        return stockSymbol;
    }

    public String getSide() {
        return side;
    }

    public String getOrderId() {
        return orderId;
    }

    @Override
    public String toString() {
        return "User " + getUserName() + ", " + getSide() + " " + getStockSymbol() + " (" + getOrderId() + ")";
    }

}
