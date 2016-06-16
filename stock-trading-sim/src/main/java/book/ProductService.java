package book;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import messages.MarketMessage;
import publishers.MarketDataDTO;
import publishers.MessagePublisher;
import tradable.TradableDTO;
import domain.Order;
import domain.Quote;
import exceptions.InvalidDataException;
import exceptions.InvalidMarketStateException;
import exceptions.InvalidMarketStateTransition;
import exceptions.NoSuchProductException;
import exceptions.OrderNotFoundException;
import exceptions.ProductAlreadyExistsException;

/**
 * 
 * @author Peter Swantek
 * 
 * This class serves as the facade to the trading system, will be implemented using the singleton design patter so that
 * only one instance of this class is ever created; having two instances of this class provide access to the trading system
 * would most likely produce inconsistent results.  This class will keep a record of all the ProductBook objects that are
 * associated with individual stocks as well as maintain the status of the market (OPEN, CLOSED, or PREOPEN).  Users will
 * interact with this class to perform actions within the trading system.
 *
 */

public class ProductService {

	private volatile static ProductService theInstance = null;
	private static HashMap<String, ProductBook> allBooks = new HashMap<String, ProductBook>();  // Record of all ProductBooks
	private static String marketState = "CLOSED";  // Market state, originally set to be CLOSED
	
	private ProductService(){}
	
	// Static method to return the one instance of this class
	public static ProductService getInstance() {
		if (theInstance == null){
			synchronized(ProductService.class){
				if (theInstance == null)
					theInstance = new ProductService();
			}
		}
			
		return theInstance;
	}
	
	// Method that produces an ArrayList of TradableDTOs with information from Orders that have remaining quantity
	// The user name and stock passed in must be valid (non null and not empty)
	public synchronized ArrayList<TradableDTO> getOrdersWithRemainingQty(String userName, String product) throws InvalidDataException {
		if (userName == null || userName.trim().isEmpty())
			throw new InvalidDataException("Error: ProductService attempted to use an invalid user name.");
		if (product == null || product.trim().isEmpty())
			throw new InvalidDataException("Error: ProductService attemptd to use an invalid stock symbol.");
		
		//if (allBooks.containsKey(product.trim().toUpperCase())){
			ProductBook book = allBooks.get(product.trim().toUpperCase());
			return book.getOrdersWithRemainingQty(userName.trim().toUpperCase());
		//}
			
		//return null;	
	}
	
	// Grabs the ProductBook associated with given product, produces the market data from that particular book
	// product must not be null or empty
	public synchronized MarketDataDTO getMarketData(String product) throws InvalidDataException {
		if (product == null || product.trim().isEmpty())
			throw new InvalidDataException("Error: ProductService attempted to get market data for an invalid product.");
		
		//if (allBooks.containsKey(product.trim().toUpperCase())){
			ProductBook book = allBooks.get(product.trim().toUpperCase());
			return book.getMarketData();
		//}
		
		//return null;
			
	}
	
	// Gets the current market state
	public synchronized String getMarketState() {
		return marketState;
	}
	
	// Grabs the ProductBook associated with the given product and returns the depth of that particular book
	public synchronized String[][] getBookDepth(String product) throws InvalidDataException, NoSuchProductException {
		if (product == null || product.trim().isEmpty())
			throw new InvalidDataException("Error: ProductService attempted to get the book depth using an invalid product.");
		
		if (!allBooks.containsKey(product.trim().toUpperCase()))
			throw new NoSuchProductException("Error: ProductService had no record for " + product);
		
		ProductBook book = allBooks.get(product.trim().toUpperCase());
		return book.getBookDepth();
	}
	
	public synchronized ArrayList<String> getProductList() {
		return new ArrayList<String>(allBooks.keySet());
	}
	
	// Change the state of the market so that trading can occur.  Market can only have the following sequence
	// of transitions: CLOSED -> PREOPEN -> OPEN -> CLOSED otherwise an exception will be thrown
	// Changed iteration method
	// Maybe add temp HashMap and ArrayList?
	public synchronized void setMarketState(String state) throws InvalidDataException, InvalidMarketStateTransition, OrderNotFoundException {
		if (state == null || state.trim().isEmpty())
			throw new InvalidDataException("Error: ProductService tried to set the market state with an invalid value.");
		if (!state.trim().toUpperCase().equals("CLOSED") && !state.trim().toUpperCase().equals("PREOPEN") && !state.trim().toUpperCase().equals("OPEN"))
			throw new InvalidDataException("Error: ProductService can only set the market state to: PREOPEN, OPEN, or CLOSED.");
		
		if (getMarketState().equals("CLOSED") && !state.trim().toUpperCase().equals("PREOPEN"))
			throw new InvalidMarketStateTransition("Error: Market state is CLOSED and can only transition to being PREOPEN.");
		
		if (getMarketState().equals("PREOPEN") && !state.trim().toUpperCase().equals("OPEN"))
			throw new InvalidMarketStateTransition("Error: Market state is PREOPEN and can only transition to being OPEN.");
		
		if (getMarketState().equals("OPEN") && !state.trim().toUpperCase().equals("CLOSED"))
			throw new InvalidMarketStateTransition("Error: Market state is OPEN and can only transition to being CLOSED.");
		
		marketState = state.trim().toUpperCase();
		MessagePublisher.getInstance().publishMarketMessage(new MarketMessage(marketState));
		
		if (getMarketState().equals("OPEN")){
			//Collection<ProductBook> theBooks = allBooks.values();
			for (Entry<String, ProductBook> entry : allBooks.entrySet())
				//for (ProductBook book : entry.getValue())
				entry.getValue().openMarket();
		}
		
		if (getMarketState().equals("CLOSED")){
			//Collection<ProductBook> theBooks = allBooks.values();
			for (Entry<String, ProductBook> entry : allBooks.entrySet())
			//for (ProductBook book : theBooks)
				entry.getValue().closeMarket();
		}
			
	}
	
	// Create a new ProductBook for the given product, throws exception if product is invalid or product already exists
	public synchronized void createProduct(String product) throws InvalidDataException, ProductAlreadyExistsException {
		if (product == null || product.trim().isEmpty())
			throw new InvalidDataException("Error: ProductService tried to create a product using an invalid stock symbol.");
		
		if (allBooks.containsKey(product.trim().toUpperCase()))
			throw new ProductAlreadyExistsException("Error: ProductService tried to create a product that already exists.");
		
		ProductBook newBook = new ProductBook(product.trim().toUpperCase());
		allBooks.put(product.trim().toUpperCase(), newBook);
	}
	
	// Submit a Quote into the system, gets the ProductBook for the Quote's product and puts the Quote into this ProductBook
	// Exceptions will be thrown if the Quote is null, if Quote has invalid product, or if market state is not 
	// in the appropriate state for trading
	public synchronized void submitQuote(Quote q) throws InvalidDataException, InvalidMarketStateException, NoSuchProductException {
		if (q == null)
			throw new InvalidDataException("Error: ProductService tried to submit a null Quote.");
		
		if (getMarketState().equals("CLOSED"))
			throw new InvalidMarketStateException("Error: ProductService tried to submit a Quote when the market state was CLOSED.");
		
		if (!allBooks.containsKey(q.getProduct()))
			throw new NoSuchProductException("Error: ProductService tried to submit a Quote that didn't contain a real stock symbol.");
		
		ProductBook book = allBooks.get(q.getProduct());
		book.addToBook(q);
	}
	
	// Submit an Order into the system, gets the ProductBook for the Order's product and puts the Order into this ProductBook
	// Exceptions will be thrown if the Order is null, if Order has invalid product, or if market state is not 
	// in the appropriate state for trading
	public synchronized String submitOrder(Order o) throws InvalidMarketStateException, InvalidDataException, NoSuchProductException {
		if (o == null)
			throw new InvalidDataException("Error: ProductService attemted to submit a null Order.");
		
		if (getMarketState().equals("CLOSED"))
			throw new InvalidMarketStateException("Error: ProductService tried to submit an Order when the market state was CLOSED.");
		
		if (getMarketState().equals("PREOPEN") && o.getPrice().isMarket())
			throw new InvalidDataException("Error: ProductService attempted to submit an Order that had a market price when the market state was PREOPEN.");
		
		if (!allBooks.containsKey(o.getProduct()))
			throw new NoSuchProductException("Error: ProductService tried to submit an Order that didn't contain a real stock symbol.");
		
		ProductBook book = allBooks.get(o.getProduct());
		book.addToBook(o);
		
		return o.getId();
	}
	
	// Submits a cancellation for an Order, will grab the associated ProductBook and then cancel the Order through that 
	public synchronized void submitOrderCancel(String product, String side, String orderId) throws OrderNotFoundException, InvalidMarketStateException, InvalidDataException, NoSuchProductException {
		if (product == null || product.trim().isEmpty())
			throw new InvalidDataException("Error: ProductService attempted to submit an Order cancel by using an invalid stock symbol.");
		
		if (side == null || side.trim().isEmpty())
			throw new InvalidDataException("Error: ProductService attempted to submit an Order cancel with an invalid side.");
		
		if (!side.trim().toUpperCase().equals("BUY") && !side.trim().toUpperCase().equals("SELL"))
			throw new InvalidDataException("Error: ProductService attemped to cancel an Order, but the side wasn't BUY or SELL.");
		
		if (orderId == null || orderId.trim().isEmpty())
			throw new InvalidDataException("Error: ProductService attempted to submit an Order cancel with an invalid Id.");
		
		if (getMarketState().equals("CLOSED"))
			throw new InvalidMarketStateException("Error: ProductService tried to cancel an Order when the market state was CLOSED.");
		
		if (!allBooks.containsKey(product.trim().toUpperCase()))
			throw new NoSuchProductException("Error: ProductService tried to cancel an Order that didn't contain a real stock symbol.");
		
		ProductBook book = allBooks.get(product.trim().toUpperCase());
		book.cancelOrder(side.trim().toUpperCase(), orderId);
	}
	
	// Submits a cancellation for a Quote, will grab the associated ProductBook and then cancel the Order through that 
	public synchronized void submitQuoteCancel(String userName, String product) throws InvalidDataException, InvalidMarketStateException, NoSuchProductException {
		if (userName == null || userName.trim().isEmpty())
			throw new InvalidDataException("Error: ProductService tried to submit a cancel for a Quote but used an invalid user name.");
		
		if (product == null || product.trim().isEmpty())
			throw new InvalidDataException("Error: ProductService tried to submit a cancel for a Quote but used an invalid stock symbol.");
		
		if (getMarketState().equals("CLOSED"))
			throw new InvalidMarketStateException("Error: ProductService tried to cancel a Quote when the market state was CLOSED.");
		
		if (!allBooks.containsKey(product.trim().toUpperCase()))
			throw new NoSuchProductException("Error: ProductService tried to cancel a Quote that didn't contain a real stock symbol.");
		
		ProductBook book = allBooks.get(product.trim().toUpperCase());
		book.cancelQuote(userName.trim().toUpperCase());
	}
	
	
}
