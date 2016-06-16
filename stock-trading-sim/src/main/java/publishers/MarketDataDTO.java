package publishers;

import price.Price;

/**
 * @author Peter Swantek
 * 
 * A class that will serve as a DTO for all the data of the market. Contains the data
 * regarding the selling Price/volume and the buying Price/volume for a certain stock.
 *
 */

public class MarketDataDTO {
	
	public String product;
	public Price buyPrice;
	public int buyVolume;
	public Price sellPrice;
	public int sellVolume;
	
	public MarketDataDTO(String newProduct, Price newBuyPrice, int newBuyVolume, 
			Price newSellPrice, int newSellVolume) {
		
		product = newProduct;
		buyPrice = newBuyPrice;
		buyVolume = newBuyVolume;
		sellPrice = newSellPrice;
		sellVolume = newSellVolume;
	}
	
	public String toString() {
		return "Product: " + product + ", Buy Price: " + buyPrice + ", Buy Volume: " + buyVolume +
				", Sell Price: " + sellPrice + ", Sell Volume: " + sellVolume;
	}

}
