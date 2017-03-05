package publishers;

import price.Price;

/**
 * Behaviors of a record tracker that tracks the direction of change in price
 * for certain stocks
 * 
 * @author Peter Swantek
 *
 */
public interface PriceDirectionTracker {

    void addStock(String stock);

    char calculateDirection(String stock, Price newPrice);

    void update(String stock, Price newPrice);

    boolean containsProduct(String stock);

}
