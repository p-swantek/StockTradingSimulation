package domain;

import exceptions.InvalidDataException;
import price.Price;
import tradable.Tradable;
import tradable.TradableImplFactory;

/**
 * The Order class is an object that represents an order within the stock
 * exchange. Since orders are objects that will be able to be traded within the
 * stock exchange, it will implement the Tradable interface. The Order object
 * will also use delegation to delegate many of its common behaviors to a class
 * that implements the Tradable interface (a TradableImpl object).
 * 
 * @author Peter Swantek
 * 
 */

public class Order implements Tradable {

    private Tradable delegate; // The reference to an implementation object
    private String orderId;

    // Creates a new Order with the given user name, the symbol of the stock to be ordered, the
    // price of that stock, the volume of the stock to order, and which side of the book the order will
    // represent.  Will construct an implementation object using these parameters.

    public Order(String newUserName, String newProductSymbol, Price orderPrice, int originalVolume, String newSide) throws InvalidDataException {

        delegate = TradableImplFactory.makeTradable(newUserName, newProductSymbol, orderPrice, originalVolume, newSide); // Use factory to create the delegate
        setOrderId();
    }

    // Sets the ID for this particular order.
    private void setOrderId() {
        orderId = getUser() + getProduct() + getPrice().toString() + System.nanoTime();
    }

    @Override
    public String getProduct() {
        return delegate.getProduct();
    }

    @Override
    public Price getPrice() {
        return delegate.getPrice();
    }

    @Override
    public int getOriginalVolume() {
        return delegate.getOriginalVolume();
    }

    @Override
    public int getRemainingVolume() {
        return delegate.getRemainingVolume();
    }

    @Override
    public int getCancelledVolume() {
        return delegate.getCancelledVolume();
    }

    @Override
    public void setCancelledVolume(int newCancelledVolume) throws InvalidDataException {
        delegate.setCancelledVolume(newCancelledVolume);
    }

    @Override
    public void setRemainingVolume(int newRemainingVolume) throws InvalidDataException {
        delegate.setRemainingVolume(newRemainingVolume);
    }

    @Override
    public String getUser() {
        return delegate.getUser();
    }

    @Override
    public String getSide() {
        return delegate.getSide();
    }

    @Override
    public boolean isQuote() {
        return delegate.isQuote();
    }

    // Order returns its ID, does not delegate this behavior.
    @Override
    public String getId() {
        return orderId;
    }

    @Override
    public String toString() {
        return getUser() + " order: " + getSide() + " " + getRemainingVolume() + " " + getProduct() + " at " + getPrice().toString() + " (Original Vol: " + getOriginalVolume() + ", CXL'd Vol: "
                + getCancelledVolume() + "), ID: " + getId();
    }

}
