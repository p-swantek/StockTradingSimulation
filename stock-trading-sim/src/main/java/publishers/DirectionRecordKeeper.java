package publishers;

import java.util.HashMap;
import java.util.Map;

import price.Price;

class DirectionRecordKeeper implements PriceDirectionTracker {

    private Map<String, Price> mapping;

    public DirectionRecordKeeper() {
        mapping = new HashMap<>();
    }

    @Override
    public char calculateDirection(String product, Price newPrice) {
        Price previousPrice = mapping.get(product);

        update(product, newPrice);

        return previousPrice.compareTo(newPrice) == 0 ? '=' : (previousPrice.compareTo(newPrice) == 1 ? ((char) 8595) : ((char) 8593));

    }

    @Override
    public void update(String stock, Price newPrice) {
        mapping.put(stock, newPrice);

    }

    @Override
    public boolean containsProduct(String stock) {
        return mapping.containsKey(stock);
    }

    @Override
    public void addStock(String stock) {

    }

}
