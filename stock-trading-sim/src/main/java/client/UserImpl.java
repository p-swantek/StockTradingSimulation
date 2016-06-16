package client;

import exceptions.AlreadyConnectedException;
import exceptions.AlreadySubscribedException;
import exceptions.InvalidConnectionIdException;
import exceptions.InvalidDataException;
import exceptions.InvalidMarketStateException;
import exceptions.NoSuchProductException;
import exceptions.OrderNotFoundException;
import exceptions.UserNotConnectedException;
import gui.UserDisplayManager;
import java.sql.Timestamp;
import java.util.ArrayList;
import messages.CancelMessage;
import messages.FillMessage;
import price.InvalidPriceOperation;
import price.Price;
import tradable.TradableDTO;

/**
 * Represents the implementation of the User interface.  Defines all the behaviors that are performed by users of the trading system.
 * 
 * @author Peter Swantek
 *
 */
public class UserImpl implements User {

	private String userName;
	private long connectionId;
	private ArrayList<String> availableStocks = null;   // The stocks currently available to this user
	private ArrayList<TradableUserData> subscribedOrders = new ArrayList<TradableUserData>();  // A list of TradableUserData representing the orders to which the user subscribed
	private Position userPosition;   // Records the account position of this user
	private UserDisplayManager theDisplayManager = null;   // The gui facade
	
	
	public UserImpl(String newUserName) throws InvalidDataException {
		setUserName(newUserName);
		userPosition = new Position();
	}
	
	private void setUserName(String newUserName) throws InvalidDataException { 
		if (isInvalidParameter(newUserName))
			throw new InvalidDataException("Error: An attempt was made to create a new User that had a null or empty user name.");
		
		userName = newUserName.trim().toUpperCase();
	}
	
	public long getConnectionId() {
		return connectionId;
	}
	
	public String getUserName() {
		return userName;
	}

	// Accept the last sale of a certain stock and update the position of the user accordingly
	public void acceptLastSale(String product, Price price, int volume) {
		
		if (theDisplayManager != null)
			theDisplayManager.updateLastSale(product, price, volume);
		
		try {
			userPosition.updateLastSale(product, price);
		}
		catch (InvalidDataException theException){
			System.out.println(theException.getMessage());
			
		}
		
	}

	// Accept a fill message for a trade
	// Added newline character to end of the String
	public void acceptMessage(FillMessage fm) {
		Timestamp theTimeStamp = new Timestamp(System.currentTimeMillis());
		String messageSummary = "(" + theTimeStamp + ")" + " Fill Message: " + fm.getSide() + " " + fm.getVolume() + " " + fm.getProduct() + " at " + fm.getPrice() +
								" " + fm.getDetails() + " (Tradable id: " + fm.getId() + ")\n";
		
		if (theDisplayManager != null)
			theDisplayManager.updateMarketActivity(messageSummary);
		
		try{
			userPosition.updatePosition(fm.getProduct(), fm.getPrice(), fm.getSide(), fm.getVolume());
		}
		catch (InvalidDataException | InvalidPriceOperation theException){
			System.out.println(theException.getMessage());
			
		}
		
	}

	// Accept a cancel message for a trade
	// Added newline character to end of the String
	public void acceptMessage(CancelMessage cm) {
		Timestamp theTimeStamp = new Timestamp(System.currentTimeMillis());
		String messageSummary = "(" + theTimeStamp + ")" + " Cancel Message: " + cm.getSide() + " " + cm.getVolume() + " " + cm.getProduct() + " at " + cm.getPrice() +
				" " + cm.getDetails() + " (Tradable id: " + cm.getId() + ")\n";
		
		if (theDisplayManager != null)
			theDisplayManager.updateMarketActivity(messageSummary);
	}

	// Accept a market message
	public void acceptMarketMessage(String message) {
		if (theDisplayManager != null)
			theDisplayManager.updateMarketState(message);
	}

	// Accept a ticker message
	public void acceptTicker(String product, Price price, char direction) {
		if (theDisplayManager != null)
			theDisplayManager.updateTicker(product, price, direction);
		
	}

	// Accept a current market message
	public void acceptCurrentMarket(String product, Price buyPrice, int buyVolume, Price sellPrice, int sellVolume) {
		if (theDisplayManager != null)
			theDisplayManager.updateMarketData(product, buyPrice, buyVolume, sellPrice, sellVolume);
		
	}

	// Connect to the user command service
	public void connect() throws InvalidDataException, AlreadyConnectedException, UserNotConnectedException, InvalidConnectionIdException {
		
		connectionId = UserCommandService.getInstance().connectUser(this);
		availableStocks = UserCommandService.getInstance().getProducts(getUserName(), connectionId);
		
	}

	// Disconnect from the user command service
	public void disConnect() throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException {
		
		UserCommandService.getInstance().disConnect(getUserName(), getConnectionId());
		
	}

	// Bring up the gui for the market display for a user
	// What kind of exception should be thrown here?
	public void showMarketDisplay() throws Exception, UserNotConnectedException {
		if (availableStocks == null)
			throw new UserNotConnectedException("Error: Can't display market for " + getUserName() + ", this user is not currently connected.");
		
		if (theDisplayManager == null)
			theDisplayManager = new UserDisplayManager(this);
		
		theDisplayManager.showMarketDisplay();
	}

	// Allows a user to submit an order to the trading system
	// the data for the tradable submitted will be added to this user's record of tradables
	public String submitOrder(String product, Price price, int volume, String side) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, InvalidMarketStateException, NoSuchProductException {
		if (isInvalidParameter(product) || isInvalidParameter(side))
			throw new InvalidDataException("Error:  A User attempted to submit an order by passing in a product or bookside that was either null or empty.");
		if (isInvalidParameter(price))
			throw new InvalidDataException("Error: A User attempted to submit an order with a null price.");
		
		String id = UserCommandService.getInstance().submitOrder(getUserName(), getConnectionId(), product, price, volume, side);
		TradableUserData tradableData = new TradableUserData(getUserName(), product, side, id);
		subscribedOrders.add(tradableData);
		
		return id;
		
	}

	// Allows a user to submit an order cancel to the trading system
	public void submitOrderCancel(String product, String side, String orderId) throws InvalidDataException, OrderNotFoundException, InvalidMarketStateException, NoSuchProductException, UserNotConnectedException, InvalidConnectionIdException {
		if (isInvalidParameter(product) || isInvalidParameter(side) || isInvalidParameter(orderId))
			throw new InvalidDataException("Error:  A User attempted to submit an order cancellation using null or empty parameters.");
		
		UserCommandService.getInstance().submitOrderCancel(getUserName(), getConnectionId(), product, side, orderId);
		
	}

	// Allows a user to submit a quote to the trading system
	public void submitQuote(String product, Price buyPrice, int buyVolume, Price sellPrice, int sellVolume) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, InvalidMarketStateException, NoSuchProductException {
		if (isInvalidParameter(product))
			throw new InvalidDataException("Error: A User attempted to submit a quote by passing in a null or empty product.");
		if (isInvalidParameter(buyPrice) || isInvalidParameter(sellPrice))
			throw new InvalidDataException("Error: A User attempted to submit a quote by passing in a null price.");
		
		UserCommandService.getInstance().submitQuote(getUserName(), getConnectionId(), product, buyPrice, buyVolume, sellPrice, sellVolume);
		
	}

	// Allows a user to submit a quote cancel to the trading system
	public void submitQuoteCancel(String product) throws InvalidDataException, InvalidMarketStateException, NoSuchProductException, UserNotConnectedException, InvalidConnectionIdException {
		if (isInvalidParameter(product))
			throw new InvalidDataException("Error: A User attempted to submit a quote cancel by passing in a null or empty product.");
		
		UserCommandService.getInstance().submitQuoteCancel(getUserName(), getConnectionId(), product);
		
	}

	// Allows a user to subscribe for current market messages 
	public void subscribeCurrentMarket(String product) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, AlreadySubscribedException {
		if (isInvalidParameter(product))
			throw new InvalidDataException("Error: A User attempted to subscribe for the current market by passing in a null or empty product.");
		
		UserCommandService.getInstance().subscribeCurrentMarket(getUserName(), getConnectionId(), product);
		
	}

	// Allows a user to subscribe for last sale messages
	public void subscribeLastSale(String product) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, AlreadySubscribedException {
		if (isInvalidParameter(product))
			throw new InvalidDataException("Error: A User attempted to subscribe for the last sale by passing in a null or empty product.");
		
		UserCommandService.getInstance().subscribeLastSale(getUserName(), getConnectionId(), product);
		
	}

	// Allows a user to subscribe for messages
	public void subscribeMessages(String product) throws AlreadySubscribedException, InvalidDataException, UserNotConnectedException, InvalidConnectionIdException {
		if (isInvalidParameter(product))
			throw new InvalidDataException("Error: A User attempted to subscribe for messages by passing in a null or empty product.");
		
		UserCommandService.getInstance().subscribeMessages(getUserName(), getConnectionId(), product);
		
	}

	// Allows a user to subscribe for ticker messages
	public void subscribeTicker(String product) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, AlreadySubscribedException {
		if (isInvalidParameter(product))
			throw new InvalidDataException("Error: A User attempted to subscribe for tickers by passing in a null or empty product.");
		
		UserCommandService.getInstance().subscribeTicker(getUserName(), getConnectionId(), product);
		
	}

	// Allows a user to query the value of all stock holdings 
	public Price getAllStockValue() throws InvalidPriceOperation, InvalidDataException {
		
		return userPosition.getAllStockValue();
	}

	// Allows a user to obtain the current account costs
	public Price getAccountCosts() {
		
		return userPosition.getAccountCosts();
	}

	// Allows a user to obtain his or her net account value 
	public Price getNetAccountValue() throws InvalidDataException, InvalidPriceOperation {
		
		return userPosition.getNetAccountValue();
	}

	// Allows a user to query the book depth for a stock
	public String[][] getBookDepth(String product) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, NoSuchProductException {
		if (isInvalidParameter(product))
			throw new InvalidDataException("Error: A User attempted to get the book depth by passing in a null or empty product.");
		
		return UserCommandService.getInstance().getBookDepth(getUserName(), getConnectionId(), product);
	}

	// Allows a user to obtain the current state of the market
	public String getMarketState() throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException {
		
		return UserCommandService.getInstance().getMarketState(getUserName(), getConnectionId());
	}

	// Allows a user to grab a list of data regarding all current orders
	public ArrayList<TradableUserData> getOrderIds() {
		
		return subscribedOrders;
	}

	// Allows a user to grab a list of all the available stocks
	public ArrayList<String> getProductList() {
		
		return availableStocks;
	}

	// Allows a user to query the value of a current stock 
	public Price getStockPositionValue(String sym) throws InvalidDataException, InvalidPriceOperation {
		if (isInvalidParameter(sym))
			throw new InvalidDataException("Error: A User attempted to get the stock position value by passing in a null or empty product.");
		
		return userPosition.getStockPositionValue(sym);
	}

	// Allows a user to query the volume of a current stock 
	public int getStockPositionVolume(String product) throws InvalidDataException {
		if (isInvalidParameter(product))
			throw new InvalidDataException("Error: A User attempted to get the stock position volume by passing in a null or empty product.");
		
		return userPosition.getStockPositionVolume(product);
	}

	// Allows a user to obtain a list of all current stock holdings
	public ArrayList<String> getHoldings() {
		
		return userPosition.getHoldings();
	}

	// Allows a user to query all the orders that currently have remaining quantity
	public ArrayList<TradableDTO> getOrdersWithRemainingQty(String product) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException {
		if (isInvalidParameter(product))
			throw new InvalidDataException("Error: A User attempted to get the orders with remaining quantity by passing in a null or empty product.");
		
		return UserCommandService.getInstance().getOrdersWithRemainingQty(getUserName(), getConnectionId(), product);
	}
	
	// Two helper methods, will check if a String or Price object is valid.  Strings can't be empty and neither Stings nor Prices can be null
	// These return true if the parameter is invalid, false if it is a good parameter
	private boolean isInvalidParameter(String string) {
		if (string == null || string.trim().isEmpty())
			return true;
		return false;
	}

	private boolean isInvalidParameter(Price price) {
		if (price == null)
			return true;
		return false;
	}
	
}
