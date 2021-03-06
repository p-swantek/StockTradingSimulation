package messages;

import exceptions.InvalidDataException;
import price.Price;

/**
 * @author Peter Swantek
 * 
 *         A class to represent the data regarding a fill of an order or a quote
 *         side within the stock exchange. Implements the interface Message
 *         which contains behaviors that are common to messages. This class uses
 *         delegation for common behaviors. Contains a reference to an
 *         implementation object which provides default implementations for the
 *         Message interface.
 *
 */

public class FillMessage implements Message, Comparable<FillMessage> {

    private Message delegate; // The delegate

    public FillMessage(String newUser, String newProduct, Price newPrice, int newVolume, String newDetails, String newSide, String newId) throws InvalidDataException {

        delegate = MessageImplFactory.makeMessage(newUser, newProduct, newPrice, newVolume, newDetails, newSide, newId); // Use factory so as not to hard code the Impl class

    }

    // Return the result of calling the compareTo() of the Price object in this message,
    // compares to the Price in the CancelMessage which was passed in
    @Override
    public int compareTo(FillMessage fm) {
        return getPrice().compareTo(fm.getPrice());
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
        return "User: " + getUser() + ", Product: " + getProduct() + ", Price: " + getPrice() + ", Volume: " + getVolume() + ", Details: " + getDetails() + ", Side: " + getSide();
    }

    // Public set methods added here, these will result in the delegate being updated with the new volume or details

    public void setVolume(int newVolume) throws InvalidDataException {
        delegate = MessageImplFactory.makeMessage(getUser(), getProduct(), getPrice(), newVolume, getDetails(), getSide(), getId());
    }

    public void setDetails(String newDetails) throws InvalidDataException {
        delegate = MessageImplFactory.makeMessage(getUser(), getProduct(), getPrice(), getVolume(), newDetails, getSide(), getId());
    }
}
