package messages;

import exceptions.InvalidDataException;
import price.Price;

/**
 * A factory, its static makeMessage method will construct an instance of an
 * implementation class for the Message interface
 * 
 * @author Peter Swantek
 */

public class MessageImplFactory {

    public static Message makeMessage(String newUser, String newProduct, Price newPrice, int newVolume, String newDetails, String newSide, String newId) throws InvalidDataException {

        return new MessageImpl(newUser, newProduct, newPrice, newVolume, newDetails, newSide, newId);

    }

}
