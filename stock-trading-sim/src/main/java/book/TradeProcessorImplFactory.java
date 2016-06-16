package book;

import exceptions.InvalidDataException;

/**
 * 
 * @author Peter Swantek
 * 
 * A class that will produce different implementations of the TradeProcessor interface. 
 * This will allow flexibility in adding different trade processing implementations in the future.
 * The static method will return a particular implementation of the TradeProcessor interface.
 * 
 *
 */

public class TradeProcessorImplFactory {
	
	// Currently only implementation is the PriceTime implementation.
	// Future static methods can be added that produce other implementations of the interface
	public static TradeProcessor makePriceTimeImpl(ProductBookSide bookSide) throws InvalidDataException {
		
		return new TradeProcessorPriceTimeImpl(bookSide);
	}

}
