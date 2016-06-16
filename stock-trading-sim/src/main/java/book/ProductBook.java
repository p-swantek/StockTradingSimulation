package book;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import messages.CancelMessage;
import messages.FillMessage;
import price.Price;
import price.PriceFactory;
import publishers.CurrentMarketPublisher;
import publishers.LastSalePublisher;
import publishers.MarketDataDTO;
import publishers.MessagePublisher;
import tradable.Tradable;
import tradable.TradableDTO;
import domain.Order;
import domain.Quote;
import domain.QuoteSide;
import exceptions.InvalidDataException;
import exceptions.OrderNotFoundException;

/**
 * @author Peter Swantek
 * 
 * A class that represents the booked tradables for both the BUY and the SELL side.  This class will maintain a reference
 * to two ProductBookSide objects, one representing the BUY side and the other representing the SELL side.  This class will
 * maintain a record of the stock product that it is associated with, the latest market data of the stock, a list of Quotes
 * for each user, and a record of tradables that have been completely traded or cancelled.
 *
 */

public class ProductBook {
	
	private String stockSymbol;
	private ProductBookSide buySide;  // BUY ProductBookSide
	private ProductBookSide sellSide;  // SELL ProductBookSide
	private String latestMarketData = "";
	private HashSet<String> userQuotes = new HashSet<String>();  // Current Quotes for users
	private HashMap<Price, ArrayList<Tradable>> oldEntries = new HashMap<Price, ArrayList<Tradable>>();  // Old tradables that have been traded/cancelled
	

	public ProductBook(String newStockSymbol) throws InvalidDataException {
		setStockSymbol(newStockSymbol);
		buySide = new ProductBookSide(this, "BUY");
		sellSide = new ProductBookSide(this, "SELL");
	}
	
	// Sets the stock symbol for the ProductBook, makes sure that it isn't null or empty
	private void setStockSymbol(String newStockSymbol) throws InvalidDataException {
		if (newStockSymbol == null || newStockSymbol.trim().isEmpty())
			throw new InvalidDataException("Error: The ProductBook was set with a null or empty stock symbol.");
		
		stockSymbol = newStockSymbol.trim().toUpperCase();
	}
	
	// Get the stock symbol of this ProductBook
	public String getStockSymbol() {
		return stockSymbol;
	}
	
	// Gets all the Orders from the BUY and SELL ProductBookSides that have any remaining volume
	public synchronized ArrayList<TradableDTO> getOrdersWithRemainingQty(String userName) throws InvalidDataException {
		if (userName == null || userName.trim().isEmpty())
			throw new InvalidDataException("Error: ProductBook tried to retrieve orders by using a null or empty user name.");
		
		ArrayList<TradableDTO> tempList = new ArrayList<TradableDTO>();
		tempList.addAll(buySide.getOrdersWithRemainingQty(userName.trim().toUpperCase()));
		tempList.addAll(sellSide.getOrdersWithRemainingQty(userName.trim().toUpperCase()));
		
		return tempList;
	}
	
	// Checks to see if an Order that was cancelled is actually able to be cancelled
	// Will be too late to cancel the Order if it is already contained in the old entries record
	// Changed the way HashMap is iterated over
	public synchronized void checkTooLateToCancel(String orderId) throws InvalidDataException, OrderNotFoundException {
		if (orderId == null || orderId.trim().isEmpty())
			throw new InvalidDataException("Error: ProductBook tried to check the cancellation status of an invalid Id.");
		
		
		//Collection<ArrayList<Tradable>> listsAtPrice = oldEntries.values();
		
		for (Entry<Price, ArrayList<Tradable>> entry : oldEntries.entrySet())
			for (Tradable order : entry.getValue())
				if (order.getId().equals(orderId)){
					
					CancelMessage cm = new CancelMessage(order.getUser(), order.getProduct(), order.getPrice(), order.getRemainingVolume(),
							"Too late to cancel.", order.getSide(), order.getId());
					
					MessagePublisher.getInstance().publishCancel(cm);
					return;
				}
			
		
		// Throw an exception if the Order isn't found
		throw new OrderNotFoundException("Error: The ProductBook couldn't locate the Order to see if it was too late to cancel.");
		
	}
	
	// Obtains the book depth for each ProductBookSide
	public synchronized String[][] getBookDepth() {
		String[][] bd = new String[2][];
		
		bd[0] = buySide.getBookDepth();
		bd[1] = sellSide.getBookDepth();
		
		return bd;
		
	}
	
	// Gives the market data based on the information contained in the ProductBookSides
	// If any Price is null, use PriceFactory to make a new limit Price representing $0.00 so as to not pass a null Price
	public synchronized MarketDataDTO getMarketData() {
		Price bestBuySidePrice = buySide.topOfBookPrice();
		if (bestBuySidePrice == null)
			bestBuySidePrice = PriceFactory.makeLimitPrice("0.00");
		
		Price bestSellSidePrice = sellSide.topOfBookPrice();
		if (bestSellSidePrice == null)
			bestSellSidePrice = PriceFactory.makeLimitPrice("0.00");
		
		int bestBuyVolume = buySide.topOfBookVolume();
		int bestSellVolume = sellSide.topOfBookVolume();
		
		MarketDataDTO results = new MarketDataDTO(getStockSymbol(), bestBuySidePrice, bestBuyVolume, bestSellSidePrice, bestSellVolume);
		
		return results;
	}
	
	
	// Adds the given tradable to the record of old entries, will also change the remaining and cancelled volume 
	// of the tradable
	// Volume update here is causing an exception? (Fixed)
	// Added a temp variable to hold the old remaining volume, then set it to 0 so the exception doesn't occur
	public synchronized void addOldEntry(Tradable trd) throws InvalidDataException {
		if (trd == null)
			throw new InvalidDataException("Error: ProductBook attempted to add a null Tradable to its old entries.");
		
		if (!oldEntries.containsKey(trd.getPrice())) {
			ArrayList<Tradable> newList = new ArrayList<Tradable>();
			oldEntries.put(trd.getPrice(), newList);
		}
		
		int oldRemainingVolume = trd.getRemainingVolume(); // Store remaining volume in temp variable
		trd.setRemainingVolume(0);  // Then set remaining volume
		trd.setCancelledVolume(oldRemainingVolume);  // Finally, set the cancelled volume to the old remaining volume
		
		
		ArrayList<Tradable> listAtPrice = oldEntries.get(trd.getPrice());
		listAtPrice.add(trd);
	}
	
	// Opens the market for trading, any pending trades are executed
	public synchronized void openMarket() throws InvalidDataException {
		Price buyPrice = buySide.topOfBookPrice();
		Price sellPrice = sellSide.topOfBookPrice();
		if (buyPrice == null || sellPrice == null)
			return;
		
		while (buyPrice.greaterOrEqual(sellPrice) || buyPrice.isMarket() || sellPrice.isMarket()) {
			ArrayList<Tradable> topOfBuySide = buySide.getEntriesAtPrice(buyPrice);
			HashMap<String, FillMessage> allFills = null;
			ArrayList<Tradable> toRemove = new ArrayList<Tradable>();
			
			for (Tradable trd : topOfBuySide) {
				allFills = sellSide.tryTrade(trd);
				if (trd.getRemainingVolume() == 0)
					toRemove.add(trd);
			}
			
			for (Tradable trd : toRemove)
				buySide.removeTradable(trd);
			
			updateCurrentMarket();
			
			Price lastSalePrice = determineLastSalePrice(allFills);
			int lastSaleVolume = determineLastSaleQuantity(allFills);
			LastSalePublisher.getInstance().publishLastSale(getStockSymbol(), lastSalePrice, lastSaleVolume);
			
			buyPrice = buySide.topOfBookPrice();
			sellPrice = sellSide.topOfBookPrice();
			if (buyPrice == null || sellPrice == null)
				break;
		}
		
		
	}
	
	// Closes the market for trading and cancels all activity for each ProductBookSide
	public synchronized void closeMarket() throws InvalidDataException, OrderNotFoundException {
		buySide.cancelAll();
		sellSide.cancelAll();
		updateCurrentMarket();
		
	}
	
	// Cancels an Order within the ProductBookSide designated by the given side
	public synchronized void cancelOrder(String side, String orderId) throws InvalidDataException, OrderNotFoundException {
		if (side == null || side.trim().isEmpty())
			throw new InvalidDataException("Error: ProductBook tried to cancel an order using an invalid side.");
		if (!side.trim().toUpperCase().equals("BUY") && !side.trim().toUpperCase().equals("SELL"))
			throw new InvalidDataException("Error: The side for a canceled order must be BUY or SELL.");
		if (orderId == null || orderId.trim().isEmpty())
			throw new InvalidDataException("Error: ProductBook tried to cancel an order using an invalid Id.");
		
		if (side.trim().toUpperCase().equals("BUY"))
			buySide.submitOrderCancel(orderId);
		else
			sellSide.submitOrderCancel(orderId);
		
		updateCurrentMarket();
		
	}
	
	// Cancels the Quotes on the BUY and SELL ProductBookSides
	public synchronized void cancelQuote(String userName) throws InvalidDataException {
		if (userName == null || userName.trim().isEmpty())
			throw new InvalidDataException("Error: ProductBook tried to cancel a quote using an invalid user name.");
		
		buySide.submitQuoteCancel(userName.trim().toUpperCase());
		sellSide.submitQuoteCancel(userName.trim().toUpperCase());
		
		updateCurrentMarket();
	}
	
	// Adds a Quote to the ProductBookSide, before doing so makes sure that the given Quote is not null and
	// contains valid Prices and volumes.  If Quote is valid, it is added to the BUY and SELL ProductBookSides
	// and the user of that Quote is added to the record of users with Quotes
	public synchronized void addToBook(Quote q) throws InvalidDataException {
		if (q == null)
			throw new InvalidDataException("Error: ProductBook attempted to add a null Quote to the book.");
		
		QuoteSide buy = q.getQuoteSide("BUY");
		QuoteSide sell = q.getQuoteSide("SELL");
		Price zeroPrice = PriceFactory.makeLimitPrice("0.00");
		
		if (sell.getPrice().lessOrEqual(buy.getPrice()))
			throw new InvalidDataException("Error: A Quote was illegal since the sell Price was less than or equal to the buy Price.");
		if (buy.getPrice().lessOrEqual(zeroPrice) || sell.getPrice().lessOrEqual(zeroPrice))
			throw new InvalidDataException("Error: A Quote was illegal as a sell or buy Price was less than or equal to 0.");
		
		if (buy.getOriginalVolume() <= 0 || sell.getOriginalVolume() <= 0)
			throw new InvalidDataException("Error: A Quote was illegal as it contained an original volume less than or equal to 0.");
		
		if (userQuotes.contains(q.getUserName())){
			buySide.removeQuote(q.getUserName());
			sellSide.removeQuote(q.getUserName());
			updateCurrentMarket();
		}
		
		addToBook("BUY", buy);
		addToBook("SELL", sell);
		
		userQuotes.add(q.getUserName());
				
		updateCurrentMarket();
	}
	
	// Adds the given Order to the ProductBookSide
	public synchronized void addToBook(Order o) throws InvalidDataException {
		if (o == null)
			throw new InvalidDataException("Error: ProductBook attempted to add a null Order to the book.");
		
		addToBook(o.getSide(), o);
		
		updateCurrentMarket();
	}
	
	// Updates the current market with the Prices/volumes at the top of each ProductBookSide and publishes this to users
	// Added checks to see if top Price of buy or sell side is null, make it "$0.00" if so
	// Do not want to pass null Prices
	public synchronized void updateCurrentMarket() {
		Price buySideTopOfBookPrice = buySide.topOfBookPrice();
		if (buySideTopOfBookPrice == null)
			buySideTopOfBookPrice = PriceFactory.makeLimitPrice("0.00");
		
		Price sellSideTopOfBookPrice = sellSide.topOfBookPrice();
		if (sellSideTopOfBookPrice == null)
			sellSideTopOfBookPrice = PriceFactory.makeLimitPrice("0.00");
		
		String data = "" + buySideTopOfBookPrice + buySide.topOfBookVolume() + sellSideTopOfBookPrice +
					sellSide.topOfBookVolume();
		
		if (!latestMarketData.equals(data)){
			MarketDataDTO info = new MarketDataDTO(getStockSymbol(), buySideTopOfBookPrice, buySide.topOfBookVolume(), 
					sellSideTopOfBookPrice, sellSide.topOfBookVolume());
			
			CurrentMarketPublisher.getInstance().publishCurrentMarket(info);
			latestMarketData = data;
		}
	}
	
	// Determines the last Sale Price of a stock based on the given list of FillMessages
	// Reversed the sorted list of FillMessages, will give the highest Price. 
	// this change resulted in giving the proper output(REMOVED IN PHASE 4, was wrong)
	private synchronized Price determineLastSalePrice(HashMap<String, FillMessage> fills) throws InvalidDataException {
		if (fills == null)
			throw new InvalidDataException("Error: ProductBook was given a null HashMap when trying to determine last sale Price.");
		
		ArrayList<FillMessage> msgs = new ArrayList<FillMessage>(fills.values());
		Collections.sort(msgs);
		//Collections.reverse(msgs);
		
		return msgs.get(0).getPrice();
		
	}
	
	// Determines the last Sale Volume of a stock based on the given list of FillMessages
	// Reversed the sorted list of FillMessages(REMOVED IN PHASE 4, was wrong)
	private synchronized int determineLastSaleQuantity(HashMap<String, FillMessage> fills) throws InvalidDataException {
		if (fills == null)
			throw new InvalidDataException("Error: ProductBook was given a null HashMap when trying to determine last sale quantity.");
		
		ArrayList<FillMessage> msgs = new ArrayList<FillMessage>(fills.values());
		Collections.sort(msgs);
		//Collections.reverse(msgs);
		
		return msgs.get(0).getVolume();
	}
	
	// Adds the given tradable to a ProductBookSide determined by the side parameter
	// If the market is open, it will attempt to try to trade the tradable
	private synchronized void addToBook(String side, Tradable trd) throws InvalidDataException {
		if (side == null || side.trim().isEmpty())
			throw new InvalidDataException("Error: ProductBook tried to add to book using an invalid side.");
		if (!side.trim().toUpperCase().equals("BUY") && !side.trim().toUpperCase().equals("SELL"))
			throw new InvalidDataException("Error: Side can only be BUY or SELL.");
		if (trd == null)
			throw new InvalidDataException("Error: ProductBook tried to add a null Tradable to the book.");
		
		if (ProductService.getInstance().getMarketState().equals("PREOPEN")){
			if (side.trim().toUpperCase().equals("BUY"))
				buySide.addToBook(trd);
			else
				sellSide.addToBook(trd);
			
			return;
		}
		
		HashMap<String, FillMessage> allFills = null;
		if (side.trim().toUpperCase().equals("BUY"))
			allFills = sellSide.tryTrade(trd);
		else
			allFills = buySide.tryTrade(trd);
		
		if (allFills != null && !allFills.isEmpty()){
			updateCurrentMarket();
			int difference = trd.getOriginalVolume() - trd.getRemainingVolume();
			Price lastSalePrice = determineLastSalePrice(allFills);
			LastSalePublisher.getInstance().publishLastSale(getStockSymbol(), lastSalePrice, difference);
		}
		
		if (trd.getRemainingVolume() > 0){
			if (trd.getPrice().isMarket()){
				CancelMessage cm = new CancelMessage(trd.getUser(), trd.getProduct(), trd.getPrice(), trd.getRemainingVolume(),
			"Cancelled", trd.getSide(), trd.getId());
				
				MessagePublisher.getInstance().publishCancel(cm);
			}
			
			else {
				if (side.trim().toUpperCase().equals("BUY"))
					buySide.addToBook(trd);
				else
					sellSide.addToBook(trd);
			}
				
		}
			
			
	}
	
}
