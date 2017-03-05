package tradable;

import exceptions.InvalidDataException;
import price.Price;

/**
 * 
 * 
 * A factory which is used to construct an instance of an object which provides
 * the default implementation for the Message interface.
 * 
 * @author Peter Swantek
 *
 */

public class TradableImplFactory {

    public static Tradable makeTradable(String newUserName, String newProductSymbol, Price orderPrice, int originalVolume, String newSide) throws InvalidDataException {

        return new TradableImpl(newUserName, newProductSymbol, orderPrice, originalVolume, newSide);
    }

}
