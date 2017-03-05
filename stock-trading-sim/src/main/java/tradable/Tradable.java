package tradable;

import exceptions.InvalidDataException;
import price.Price;

/**
 * 
 * 
 * An interface that will be implemented by objects that can be traded within
 * the stock exchange.
 * 
 * @author Peter Swantek
 * 
 */

public interface Tradable {

    String getProduct();

    Price getPrice();

    int getOriginalVolume();

    int getRemainingVolume();

    int getCancelledVolume();

    void setCancelledVolume(int newCancelledVolume) throws InvalidDataException;

    void setRemainingVolume(int newRemainingVolume) throws InvalidDataException;

    String getUser();

    String getSide();

    boolean isQuote();

    String getId();

}
