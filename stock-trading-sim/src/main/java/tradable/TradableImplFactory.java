package tradable;

import price.Price;
import exceptions.InvalidDataException;

/**
 * @author Peter Swantek
 * 
 * A factory which is used to construct an instance of an object which provides the default implementation for 
 * the Message interface.
 *
 */

public class TradableImplFactory {
	
	public static Tradable makeTradable(String newUserName, String newProductSymbol, Price orderPrice,
			int originalVolume, String newSide) throws InvalidDataException {
		
		return new TradableImpl(newUserName, newProductSymbol, orderPrice, originalVolume, newSide);
	}
			
			

}
