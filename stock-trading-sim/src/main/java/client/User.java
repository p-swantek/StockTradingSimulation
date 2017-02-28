package client;

import java.util.List;

import exceptions.AlreadyConnectedException;
import exceptions.AlreadySubscribedException;
import exceptions.InvalidConnectionIdException;
import exceptions.InvalidDataException;
import exceptions.InvalidMarketStateException;
import exceptions.InvalidPriceOperation;
import exceptions.NoSuchProductException;
import exceptions.OrderNotFoundException;
import exceptions.UserNotConnectedException;
import messages.CancelMessage;
import messages.FillMessage;
import price.Price;
import tradable.TradableDTO;

/**
 * An interface of behaviors common to users of the stock exchange.
 * 
 * @author Peter Swantek
 *
 */

public interface User {

    String getUserName();

    void acceptLastSale(String product, Price price, int volume);

    void acceptMessage(FillMessage fm);

    void acceptMessage(CancelMessage cm);

    void acceptMarketMessage(String message);

    void acceptTicker(String product, Price price, char direction);

    void acceptCurrentMarket(String product, Price buyPrice, int buyVolume, Price sellPrice, int sellVolume);

    void connect() throws InvalidDataException, AlreadyConnectedException, UserNotConnectedException, InvalidConnectionIdException;

    void disConnect() throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException;

    void showMarketDisplay() throws Exception;

    String submitOrder(String product, Price price, int volume, String side)
            throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, InvalidMarketStateException, NoSuchProductException;

    void submitOrderCancel(String product, String side, String orderId)
            throws InvalidDataException, OrderNotFoundException, InvalidMarketStateException, NoSuchProductException, UserNotConnectedException, InvalidConnectionIdException;

    void submitQuote(String product, Price buyPrice, int buyVolume, Price sellPrice, int sellVolume)
            throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, InvalidMarketStateException, NoSuchProductException;

    void submitQuoteCancel(String product) throws InvalidDataException, InvalidMarketStateException, NoSuchProductException, UserNotConnectedException, InvalidConnectionIdException;

    void subscribeCurrentMarket(String product) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, AlreadySubscribedException;

    void subscribeLastSale(String product) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, AlreadySubscribedException;

    void subscribeMessages(String product) throws AlreadySubscribedException, InvalidDataException, UserNotConnectedException, InvalidConnectionIdException;

    void subscribeTicker(String product) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, AlreadySubscribedException;

    Price getAllStockValue() throws InvalidPriceOperation, InvalidDataException;

    Price getAccountCosts();

    Price getNetAccountValue() throws InvalidDataException, InvalidPriceOperation;

    String[][] getBookDepth(String product) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, NoSuchProductException;

    String getMarketState() throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException;

    List<TradableUserData> getOrderIds();

    List<String> getProductList();

    Price getStockPositionValue(String sym) throws InvalidDataException, InvalidPriceOperation;

    int getStockPositionVolume(String product) throws InvalidDataException;

    List<String> getHoldings();

    List<TradableDTO> getOrdersWithRemainingQty(String product) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException;

}
