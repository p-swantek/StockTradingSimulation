
package client;

import java.util.HashMap;

public class UserSimSettings {

    private static HashMap<String, Double> buySideBases = new HashMap<String, Double>();
    private static HashMap<String, Double> sellSideBases = new HashMap<String, Double>();
    private static HashMap<String, Integer> volumeBases = new HashMap<String, Integer>();
    
    public static final double priceVariance = 0.05;
    public static final double volumeVariance = 0.25;
    
    private UserSimSettings() {}

    public static void addProductData(String product, double bb, double sb, int vol)
    {
        buySideBases.put(product, bb);
        sellSideBases.put(product, sb);
        volumeBases.put(product, vol);
    }
    
    public static double getBuyPriceBase(String product) {
        if (!buySideBases.containsKey(product))
            return 0.0;
        else
            return buySideBases.get(product);
    }
    
    public static double getSellPriceBase(String product) {
        if (!sellSideBases.containsKey(product))
            return 0.0;
        else
            return sellSideBases.get(product);
    }
    
    public static int getVolumeBase(String product) {
        if (!volumeBases.containsKey(product))
            return 0;
        else
            return volumeBases.get(product);
    }
}
