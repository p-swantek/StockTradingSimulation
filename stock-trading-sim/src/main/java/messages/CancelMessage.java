package messages;

import exceptions.InvalidDataException;
import price.Price;

/**
 * A class to represent the data regarding a cancellation of an order or a quote
 * side within the stock exchange. Implements the interface Message which
 * contains behaviors that are common to messages. This class uses delegation
 * for common behaviors. Contains a reference to an implementation object which
 * provides default implementations for the Message interface.
 * 
 * @author Peter Swantek
 *
 */

public class CancelMessage implements Message, Comparable<CancelMessage> {

    private Message delegate; // The delegate for default implementation of Message

    public CancelMessage(String newUser, String newProduct, Price newPrice, int newVolume, String newDetails, String newSide, String newId) throws InvalidDataException {

        delegate = MessageImplFactory.makeMessage(newUser, newProduct, newPrice, newVolume, newDetails, newSide, newId); // Use factory so as not to hard code the Impl class
    }

    // Return the result of calling the compareTo() of the Price object in this message,
    // compares to the Price in the CancelMessage which was passed in
    @Override
    public int compareTo(CancelMessage cm) {
        return getPrice().compareTo(cm.getPrice());
    }

    // Delegate common behaviors, have the delegate perform them
    @Override
    public String getUser() {
        return delegate.getUser();
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
    public int getVolume() {
        return delegate.getVolume();
    }

    @Override
    public String getDetails() {
        return delegate.getDetails();
    }

    @Override
    public String getSide() {
        return delegate.getSide();
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public String toString() {
        return "User: " + getUser() + ", Product: " + getProduct() + ", Price: " + getPrice() + ", Volume: " + getVolume() + ", Details: " + getDetails() + ", Side: " + getSide() + ", Id: " + getId();
    }
}
