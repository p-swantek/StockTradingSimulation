package messages;

import price.Price;
import exceptions.InvalidDataException;

/**
 * @author Peter Swantek
 * 
 * A factory, its static makeMessage method will construct an instance of an implementation class for the Message interface
 */

public class MessageImplFactory {
	
	public static Message makeMessage(String newUser, String newProduct, Price newPrice, int newVolume,
			String newDetails, String newSide, String newId) throws InvalidDataException {
		
		return new MessageImpl(newUser, newProduct, newPrice, newVolume, newDetails, newSide, newId);
		
	}

}
