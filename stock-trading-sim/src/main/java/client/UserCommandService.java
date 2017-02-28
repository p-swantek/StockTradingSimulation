package client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import book.ProductService;
import domain.Order;
import domain.Quote;
import exceptions.AlreadyConnectedException;
import exceptions.AlreadySubscribedException;
import exceptions.InvalidConnectionIdException;
import exceptions.InvalidDataException;
import exceptions.InvalidMarketStateException;
import exceptions.NoSuchProductException;
import exceptions.NotSubscribedException;
import exceptions.OrderNotFoundException;
import exceptions.UserNotConnectedException;
import price.Price;
import publishers.CurrentMarketPublisher;
import publishers.LastSalePublisher;
import publishers.MessagePublisher;
import publishers.TickerPublisher;
import tradable.TradableDTO;

/**
 * Represents a facade with which the users of the trading system will interact.
 * Users will use this service in order to perform actions within the trading
 * system such as submitting/canceling orders and quotes. Users will also be
 * able to subscribe to publishers that will send relevant information to
 * subscribed users. This class will allow for separation of the users from the
 * implementation of the trading system.
 * 
 * @author Peter Swantek
 *
 */
public class UserCommandService {

    private volatile static UserCommandService theInstance = null;
    private static HashMap<String, Long> connectedUserIds = new HashMap<String, Long>(); // Record of the Ids of connected users
    private static HashMap<String, User> connectedUsers = new HashMap<String, User>(); // Record of the connected users
    private static HashMap<String, Long> connectedTime = new HashMap<String, Long>(); // Record of the time at which a user connects

    private UserCommandService() {
    }

    // Singleton, employs double-check locking
    public static UserCommandService getInstance() {
        if (theInstance == null) {
            synchronized (UserCommandService.class) {
                if (theInstance == null)
                    theInstance = new UserCommandService();
            }
        }

        return theInstance;
    }

    // Verifies that a user is actually registered within the command service
    private void verifyUser(String userName, long connId) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException {
        if (userName == null || userName.trim().isEmpty())
            throw new InvalidDataException("Error: The UserCommandService attempted to verify a user but was given a null or empty user name.");

        if (!connectedUserIds.containsKey(userName.trim().toUpperCase()))
            throw new UserNotConnectedException("Error: The UserCommandService tried to verify a user, but had no matching connected user.");

        if (connId != connectedUserIds.get(userName.trim().toUpperCase()))
            throw new InvalidConnectionIdException("Error: UserCommandService attempted to verify a user but was given an invalid connection id.");

    }

    // Connects a user to the command service, generates a unique id for this user and records the time at which the user connected
    public synchronized long connectUser(User user) throws InvalidDataException, AlreadyConnectedException {
        if (user == null)
            throw new InvalidDataException("Error: The UserCommandService tried to connect a null User.");

        if (connectedUserIds.containsKey(user.getUserName()))
            throw new AlreadyConnectedException("Error: " + user.getUserName() + " was already connected to the UserCommandService.");

        connectedUserIds.put(user.getUserName(), System.nanoTime());
        connectedUsers.put(user.getUserName(), user);
        connectedTime.put(user.getUserName(), System.currentTimeMillis());

        return connectedUserIds.get(user.getUserName());
    }

    // Disconnects a user from the command service
    public synchronized void disConnect(String userName, long connId) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException {
        if (userName == null || userName.trim().isEmpty())
            throw new InvalidDataException("Error: The UserCommandService attempted to disconnect a user but was given a null or emtpy user name.");

        verifyUser(userName, connId);

        connectedUserIds.remove(userName.trim().toUpperCase());
        connectedUsers.remove(userName.trim().toUpperCase());
        connectedTime.remove(userName.trim().toUpperCase());

    }

    // Allows for a user to query the book depth for a particular stock
    public String[][] getBookDepth(String userName, long connId, String product) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, NoSuchProductException {
        if (userName == null || userName.trim().isEmpty())
            throw new InvalidDataException("Error: UserCommandService tried to get the book depth but the user name was null or empty.");
        if (product == null || product.trim().isEmpty())
            throw new InvalidDataException("Error: UserCommandService tried to get the book depth on a null or empty stock product.");

        verifyUser(userName, connId);

        return ProductService.getInstance().getBookDepth(product);
    }

    // Allows a user to obtain the current state of the market
    public String getMarketState(String userName, long connId) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException {
        if (userName == null || userName.trim().isEmpty())
            throw new InvalidDataException("Error: UserCommandService tried to get a market state for a null or empty user name.");

        verifyUser(userName, connId);

        return ProductService.getInstance().getMarketState();

    }

    // Allows a user to obtain a list of the orders that currently have remaining quantity
    public synchronized List<TradableDTO> getOrdersWithRemainingQty(String userName, long connId, String product) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException {
        if (userName == null || userName.trim().isEmpty())
            throw new InvalidDataException("Error: UserCommandService tried to get the remaing quantity of orders for a user, but was given a null or empty user name.");
        if (product == null || product.trim().isEmpty())
            throw new InvalidDataException("Error: UserCommandService tried to get the remaining quantity of orders for a user, but was given a null or empty stock symbol.");

        verifyUser(userName, connId);

        return ProductService.getInstance().getOrdersWithRemainingQty(userName, product);

    }

    // Allows a user to obtain a list of all the stocks
    public ArrayList<String> getProducts(String userName, long connId) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException {
        if (userName == null || userName.trim().isEmpty())
            throw new InvalidDataException("Error: The UserCommandService attempted to get a list of products, but was given a null or empty user name.");

        verifyUser(userName, connId);

        ArrayList<String> results = ProductService.getInstance().getProductList();
        Collections.sort(results);

        return results;
    }

    // Allows a user to submit orders to the trading system
    public String submitOrder(String userName, long connId, String product, Price price, int volume, String side)
            throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, InvalidMarketStateException, NoSuchProductException {
        if (userName == null || userName.trim().isEmpty())
            throw new InvalidDataException("Error: The UserCommandService tried to submit an order using a null or empty user name.");
        if (product == null || product.trim().isEmpty())
            throw new InvalidDataException("Error: The UserCommandService tried to submit an order using a null or empty stock symbol.");
        if (price == null)
            throw new InvalidDataException("Error: The UserCommandService tried to submit an order but used a null Price.");
        if (side == null || side.trim().isEmpty())
            throw new InvalidDataException("Error: The UserCommandService tried to submit an order but was given a null or empty side.");
        if (!side.trim().toUpperCase().equals("BUY") && !side.trim().toUpperCase().equals("SELL"))
            throw new InvalidDataException("Error: UserCommandService tried to submit an order, but was given a side that wasn't BUY or SELL.");

        verifyUser(userName, connId);
        Order order = new Order(userName, product, price, volume, side);
        String resultingId = ProductService.getInstance().submitOrder(order);

        return resultingId;

    }

    // Allows a user to submit order cancellations to the trading system
    public void submitOrderCancel(String userName, long connId, String product, String side, String orderId)
            throws InvalidDataException, OrderNotFoundException, InvalidMarketStateException, NoSuchProductException, UserNotConnectedException, InvalidConnectionIdException {
        if (userName == null || userName.trim().isEmpty())
            throw new InvalidDataException("Error: The UserCommandService tried to cancel an order using a null or empty user name.");
        if (product == null || product.trim().isEmpty())
            throw new InvalidDataException("Error: The UserCommandService tried to cancel an order using a null or empty stock symbol.");
        if (side == null || side.trim().isEmpty())
            throw new InvalidDataException("Error: The UserCommandService tried to cancel an order but was given a null or empty side.");
        if (!side.trim().toUpperCase().equals("BUY") && !side.trim().toUpperCase().equals("SELL"))
            throw new InvalidDataException("Error: UserCommandService tried to cancel an order, but was given a side that wasn't BUY or SELL.");
        if (orderId == null || orderId.trim().isEmpty())
            throw new InvalidDataException("Error: The UserCommandService tried to cancel an order but was given a null or empty orderId.");

        verifyUser(userName, connId);

        ProductService.getInstance().submitOrderCancel(product, side, orderId);

    }

    // Allows a user to submit quotes to the trading system
    public void submitQuote(String userName, long connId, String product, Price buyPrice, int buyVolume, Price sellPrice, int sellVolume)
            throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, InvalidMarketStateException, NoSuchProductException {
        if (userName == null || userName.trim().isEmpty())
            throw new InvalidDataException("Error: The UserCommandService tried to submit a quote using a null or empty user name.");
        if (product == null || product.trim().isEmpty())
            throw new InvalidDataException("Error: The UserCommandService tried to submit a quote using a null or empty stock symbol.");
        if (buyPrice == null || sellPrice == null)
            throw new InvalidDataException("Error: The UserCommandService treid to submit a quote but used a null buying Price or selling Price.");

        verifyUser(userName, connId);
        Quote quote = new Quote(userName, product, buyPrice, buyVolume, sellPrice, sellVolume);

        ProductService.getInstance().submitQuote(quote);
    }

    // Allows a user to submit quote cancellations to the trading system
    public void submitQuoteCancel(String userName, long connId, String product)
            throws InvalidDataException, InvalidMarketStateException, NoSuchProductException, UserNotConnectedException, InvalidConnectionIdException {
        if (userName == null || userName.trim().isEmpty())
            throw new InvalidDataException("Error: The UserCommandService tried to cancel a quote using a null or empty user name.");
        if (product == null || product.trim().isEmpty())
            throw new InvalidDataException("Error: The UserCommandService tried to cancel a quote using a null or empty stock symbol.");

        verifyUser(userName, connId);
        ProductService.getInstance().submitQuoteCancel(userName, product);
    }

    // Enables a user to subscribe to current market messages
    public void subscribeCurrentMarket(String userName, long connId, String product) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, AlreadySubscribedException {
        if (userName == null || userName.trim().isEmpty())
            throw new InvalidDataException("Error: The UserCommandService tried to perform a subscription request with a null or empty user name.");
        if (product == null || product.trim().isEmpty())
            throw new InvalidDataException("Error: The UserCommandService tried to perform a subscription request with a null or empty stock.");

        verifyUser(userName, connId);
        CurrentMarketPublisher.getInstance().subscribe(connectedUsers.get(userName.trim().toUpperCase()), product);
    }

    // Enables a user to subscribe to last sale messages
    public void subscribeLastSale(String userName, long connId, String product) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, AlreadySubscribedException {
        if (userName == null || userName.trim().isEmpty())
            throw new InvalidDataException("Error: The UserCommandService tried to perform a subscription request with a null or empty user name.");
        if (product == null || product.trim().isEmpty())
            throw new InvalidDataException("Error: The UserCommandService tried to perform a subscription request with a null or empty stock.");

        verifyUser(userName, connId);
        LastSalePublisher.getInstance().subscribe(connectedUsers.get(userName.trim().toUpperCase()), product);
    }

    // Enables a user to subscribe to messages
    public void subscribeMessages(String userName, long connId, String product) throws AlreadySubscribedException, InvalidDataException, UserNotConnectedException, InvalidConnectionIdException {
        if (userName == null || userName.trim().isEmpty())
            throw new InvalidDataException("Error: The UserCommandService tried to perform a subscription request with a null or empty user name.");
        if (product == null || product.trim().isEmpty())
            throw new InvalidDataException("Error: The UserCommandService tried to perform a subscription request with a null or empty stock.");

        verifyUser(userName, connId);
        MessagePublisher.getInstance().subscribe(connectedUsers.get(userName.trim().toUpperCase()), product);
    }

    // Enables a user to subscribe to ticker messages
    public void subscribeTicker(String userName, long connId, String product) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, AlreadySubscribedException {
        if (userName == null || userName.trim().isEmpty())
            throw new InvalidDataException("Error: The UserCommandService tried to perform a subscription request with a null or empty user name.");
        if (product == null || product.trim().isEmpty())
            throw new InvalidDataException("Error: The UserCommandService tried to perform a subscription request with a null or empty stock.");

        verifyUser(userName, connId);
        TickerPublisher.getInstance().subscribe(connectedUsers.get(userName.trim().toUpperCase()), product);
    }

    // Enables a user to unsubscribe to current market messages
    public void unSubscribeCurrentMarket(String userName, long connId, String product) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, NotSubscribedException {
        if (userName == null || userName.trim().isEmpty())
            throw new InvalidDataException("Error: The UserCommandService tried to perform an unsubscription request with a null or empty user name.");
        if (product == null || product.trim().isEmpty())
            throw new InvalidDataException("Error: The UserCommandService tried to perform an unsubscription request with a null or empty stock.");

        verifyUser(userName, connId);
        CurrentMarketPublisher.getInstance().unSubscribe(connectedUsers.get(userName.trim().toUpperCase()), product);

    }

    // Enables a user to unsubscribe to last sale messages
    public void unSubscribeLastSale(String userName, long connId, String product) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, NotSubscribedException {
        if (userName == null || userName.trim().isEmpty())
            throw new InvalidDataException("Error: The UserCommandService tried to perform an unsubscription request with a null or empty user name.");
        if (product == null || product.trim().isEmpty())
            throw new InvalidDataException("Error: The UserCommandService tried to perform an unsubscription request with a null or empty stock.");

        verifyUser(userName, connId);
        LastSalePublisher.getInstance().unSubscribe(connectedUsers.get(userName.trim().toUpperCase()), product);
    }

    // Enables a user to unsubscribe to ticker messages
    public void unSubscribeTicker(String userName, long connId, String product) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, NotSubscribedException {
        if (userName == null || userName.trim().isEmpty())
            throw new InvalidDataException("Error: The UserCommandService tried to perform an unsubscription request with a null or empty user name.");
        if (product == null || product.trim().isEmpty())
            throw new InvalidDataException("Error: The UserCommandService tried to perform an unsubscription request with a null or empty stock.");

        verifyUser(userName, connId);
        TickerPublisher.getInstance().unSubscribe(connectedUsers.get(userName.trim().toUpperCase()), product);
    }

    // Enables a user to unsubscribe to messages
    public void unSubscribeMessages(String userName, long connId, String product) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, NotSubscribedException {
        if (userName == null || userName.trim().isEmpty())
            throw new InvalidDataException("Error: The UserCommandService tried to perform an unsubscription request with a null or empty user name.");
        if (product == null || product.trim().isEmpty())
            throw new InvalidDataException("Error: The UserCommandService tried to perform an unsubscription request with a null or empty stock.");

        verifyUser(userName, connId);
        MessagePublisher.getInstance().unSubscribe(connectedUsers.get(userName.trim().toUpperCase()), product);
    }

}
