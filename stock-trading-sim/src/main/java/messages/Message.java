package messages;

import price.Price;

/**
 * An interface that represents common behaviors by messages within the trading
 * system.
 * 
 * @author Peter Swantek
 * 
 */

public interface Message {

    String getUser();

    String getProduct();

    Price getPrice();

    int getVolume();

    String getDetails();

    String getSide();

    String getId();

}
