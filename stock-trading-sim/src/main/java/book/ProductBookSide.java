package book;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import messages.CancelMessage;
import messages.FillMessage;
import price.Price;
import publishers.MessagePublisher;
import tradable.Tradable;
import tradable.TradableDTO;
import exceptions.InvalidDataException;
import exceptions.OrderNotFoundException;

/**
 * 
 * @author Peter Swantek
 * 
 * This class represents either the BUY or the SELL side of a ProductBook for a certain stock.
 * The ProductBookSide will maintain a list of tradables that are trading at a certain Price.  The Buy Side 
 * will be represented by a descending list of Prices while the Sell side will be represented by an ascending list
 * of Prices.  The class will also keep a record of the side it represents, a reference to the ProductBook to which it belongs,
 * as well as a reference to an implementor of the TradeProcessor interface (this will actually perform the trades).
 *
 */

public class ProductBookSide {
	
	private String side;
	private HashMap<Price, ArrayList<Tradable>> bookEntries = new HashMap<Price, ArrayList<Tradable>>();  // Record of Prices and tradables
	private TradeProcessor executor;  // Reference to the TradeProcessor
	private ProductBook parentBook;  // The ProductBook that this ProductBookSide is associated with
	
	
	public ProductBookSide(ProductBook theParent, String newSide) throws InvalidDataException {
		setParentBook(theParent);
		setSide(newSide);
		executor = TradeProcessorImplFactory.makePriceTimeImpl(this); // Use factory as to not hard code the implementor, create an implementor that performs the PriceTime trading algorithm
		
	}
	
	// Set the parent ProductBook of this ProductBookSide, make sure the parent isn't null
	private void setParentBook(ProductBook newParent) throws InvalidDataException {
		if (newParent == null)
			throw new InvalidDataException("Error: ProductBookSide was set with a null ProductBook.");
		
		parentBook = newParent;
	}

	// Sets the side and makes sure that it is a valid value
	private void setSide(String newSide) throws InvalidDataException {
		if (newSide == null || newSide.trim().isEmpty())
			throw new InvalidDataException("Error: ProductBookSide can't have a null or empty side.");
		
		if (!newSide.trim().toUpperCase().equals("BUY") && !newSide.trim().toUpperCase().equals("SELL"))
			throw new InvalidDataException("Error: ProductBookSide's side must be BUY or SELL.");
		
		side = newSide.trim().toUpperCase();
	}
	
	// Gets the side of this ProductBookSide
	public String getSide(){
		return side;
	}
	
	// Looks into the record to find an Order that matches the given user and also has remaining volume.  Return the data on this Order
	// Changed the way HashMap is iterated over
	// added the use of a temp hashmap to iterate thru
	public synchronized ArrayList<TradableDTO> getOrdersWithRemainingQty(String userName) throws InvalidDataException {
		if (userName == null || userName.trim().isEmpty())
			throw new InvalidDataException("Error: ProductBookSide tried to obtain orders for a null or empty user name.");
		
		ArrayList<TradableDTO> results = new ArrayList<TradableDTO>();
		//Collection<ArrayList<Tradable>> tradableLists = bookEntries.values();
		HashMap<Price, ArrayList<Tradable>> tempMap = new HashMap<Price, ArrayList<Tradable>>(bookEntries);
		for (Entry<Price, ArrayList<Tradable>> entry : tempMap.entrySet())
			for (Tradable tradable : entry.getValue())
				if (userName.trim().toUpperCase().equals(tradable.getUser()) && tradable.getRemainingVolume() > 0) {
					
					TradableDTO orderInfo = new TradableDTO(tradable.getProduct(), tradable.getPrice(), tradable.getOriginalVolume(),
							tradable.getRemainingVolume(), tradable.getCancelledVolume(), tradable.getUser(), tradable.getSide(), tradable.isQuote(), tradable.getId());
					
					results.add(orderInfo);
				}
		
		return results;
	}
	
	// Gets the entries at the top of the BookSide
	// Sorts the Price list to be ascending or descending based on if this is a BUY or a SELL side ProductBookSide
	synchronized ArrayList<Tradable> getEntriesAtTopOfBook() {
		if (bookEntries.isEmpty())
			return null;
		
		ArrayList<Price> sortedList = new ArrayList<Price>(bookEntries.keySet());
		Collections.sort(sortedList);
		if (getSide().equals("BUY"))
			Collections.reverse(sortedList);
		
		return bookEntries.get(sortedList.get(0));
		
	}
	
	// Gets the book depth, displays the total volume of tradables at a certain Price.
	// Sorts the Price list accordingly based on the side, then sums the remaining volumes of all tradables at that Price
	public synchronized String[] getBookDepth(){
		if (bookEntries.isEmpty())
			return new String[]{"<Empty>"};
		
		String[] results = new String[bookEntries.size()];
		ArrayList<Price> sortedList = new ArrayList<Price>(bookEntries.keySet());
		Collections.sort(sortedList);
		if (getSide().equals("BUY"))
			Collections.reverse(sortedList);
		
		for (int i = 0; i < sortedList.size(); i++){
			ArrayList<Tradable> tradablesAtPrice = bookEntries.get(sortedList.get(i));
			int remainingVolumeSum = 0;
			for (Tradable tradable : tradablesAtPrice)
				remainingVolumeSum += tradable.getRemainingVolume();
			results[i] = sortedList.get(i) + " x " + remainingVolumeSum;
		}
		
		return results;
			
	}
	
	// Returns a list of all the data of entries at the given Price.
	// makes sure that the given Price is not null before proceeding
	synchronized ArrayList<Tradable> getEntriesAtPrice(Price price) throws InvalidDataException {
		if (price == null)
			throw new InvalidDataException("Error: ProductBookSide tried to get entries on a null Price.");
		
		if (!bookEntries.containsKey(price))
			return null;
		
		return bookEntries.get(price);
	}
	
	// True if the record contains a market price, false otherwise
	public synchronized boolean hasMarketPrice() {
		ArrayList<Price> priceList = new ArrayList<Price>(bookEntries.keySet());
		for (Price p : priceList)
			if (p.isMarket())
				return true;
		
		return false;
	}
	
	// True if the record only contains a market price, false otherwise
	public boolean hasOnlyMarketPrice() {
		ArrayList<Price> priceList = new ArrayList<Price>(bookEntries.keySet());
		for (Price p : priceList)
			if (!p.isMarket())
				return false;
		
		return true;
	}
	
	// Sort the list of Prices based on the side, then get the first Price from that list 
	public synchronized Price topOfBookPrice() {
		if (bookEntries.isEmpty())
			return null;
		
		ArrayList<Price> sortedList = new ArrayList<Price>(bookEntries.keySet());
		Collections.sort(sortedList);
		if (getSide().equals("BUY"))
			Collections.reverse(sortedList);
		
		return sortedList.get(0);
		
	}
	
	// Sort the list of Prices based on the side, then return the total remaining volume of all the tradables at that Price
	public synchronized int topOfBookVolume() {
		if (bookEntries.isEmpty())
			return 0;
		
		ArrayList<Price> sortedList = new ArrayList<Price>(bookEntries.keySet());
		Collections.sort(sortedList);
		if (getSide().equals("BUY"))
			Collections.reverse(sortedList);
		
		ArrayList<Tradable> tradableList = bookEntries.get(sortedList.get(0));
		int remainingVolumeSum = 0;
		for (Tradable tradable : tradableList)
			remainingVolumeSum += tradable.getRemainingVolume();
		
		return remainingVolumeSum;
	}
	
	// True if the record contains no entries, false otherwise
	public synchronized boolean isEmpty() {
		if (bookEntries.isEmpty())
			return true;
		
		return false;
	}
	
	// Cancels all the Orders or Quotes in the record. Makes use of temp lists and HashMap so that modifications are 
	// applied to the real list while the temps are iterated through, will avoid inconsistent results that may occur
	// if the list being iterated over is modified during the iteration
	// Changed the iteration thru the HashMap
	// Added a temp HashMap to iterate over so that the cancels can be applied only on real list
	// Make a temp ArrayList of keys(Prices) and a new temp HashMap
	public synchronized void cancelAll() throws OrderNotFoundException, InvalidDataException {
		//Collection<ArrayList<Tradable>> allLists = bookEntries.values();
		ArrayList<Price> tempKeyList = new ArrayList<Price>(bookEntries.keySet());
		HashMap<Price, ArrayList<Tradable>> tempMap = new HashMap<Price, ArrayList<Tradable>>(bookEntries);
		
		for (Price p : tempKeyList){
			ArrayList<Tradable> tempList = new ArrayList<Tradable>(tempMap.get(p));
			for (Tradable trd : tempList){
				if (trd.isQuote())
					submitQuoteCancel(trd.getUser());
				else
					submitOrderCancel(trd.getId());
			}
				
		}
	}
		
	
	// Removes the Quote associated with the given user and returns the relevant data on that Quote.
	// Again makes use of temp list and HashMap so that changes made are made on real list and will not affect the iteration
	// added use of temp hashmap and arrayList to iterate over
	public synchronized TradableDTO removeQuote(String user) throws InvalidDataException {
		if (user == null || user.trim().isEmpty())
			throw new InvalidDataException("Error: ProductBookSide attempted to remove a quote for an invalid user name.");
		
		//Collection<ArrayList<Tradable>> tradableLists = bookEntries.values();
		ArrayList<Price> tempKeyList = new ArrayList<Price>(bookEntries.keySet());
		HashMap<Price, ArrayList<Tradable>> tempMap = new HashMap<Price, ArrayList<Tradable>>(bookEntries);
		
		for (Price p : tempKeyList){
			ArrayList<Tradable> tempList = new ArrayList<Tradable>(tempMap.get(p));
			for (Tradable tradable : tempList)
				if (tradable.getUser().equals(user.trim().toUpperCase())){
					
					bookEntries.get(tradable.getPrice()).remove(tradable);
					TradableDTO tradableInfo = new TradableDTO(tradable.getProduct(), tradable.getPrice(), tradable.getOriginalVolume(), tradable.getRemainingVolume(), 
						tradable.getCancelledVolume(), tradable.getUser(), tradable.getSide(), tradable.isQuote(), tradable.getId());
					
					
					
					if (bookEntries.get(tradable.getPrice()).isEmpty())
						bookEntries.remove(tradable.getPrice());
					
					return tradableInfo;
				}
		}
			
		return null;
		
		
	}
	
	// Attempts to cancel the associated Order with the given user and publishes a CancelMessage based on the information.
	// Again makes use of temp list and HashMap so that changes made are made on real list and will not affect the iteration
	// changed iteration method
	// Added a temp HashMap and arrayList to iterate over
	public synchronized void submitOrderCancel(String orderId) throws InvalidDataException, OrderNotFoundException {
		if (orderId == null || orderId.trim().isEmpty())
			throw new InvalidDataException("Error: ProductBookSide tried to submit an order cancel with an invalid order ID.");
		
		//Collection<ArrayList<Tradable>> tradableLists = bookEntries.values();
		ArrayList<Price> tempKeyList = new ArrayList<Price>(bookEntries.keySet());
		HashMap<Price, ArrayList<Tradable>> tempMap = new HashMap<Price, ArrayList<Tradable>>(bookEntries);
		
		for (Price p : tempKeyList){
			ArrayList<Tradable> tempList = new ArrayList<Tradable>(tempMap.get(p));
			for (Tradable tradable : tempList)
				if (tradable.getId().equals(orderId)){
					
					bookEntries.get(tradable.getPrice()).remove(tradable);
					
					CancelMessage cm = new CancelMessage(tradable.getUser(), tradable.getProduct(), tradable.getPrice(), tradable.getRemainingVolume(),
						tradable.getSide() + " Order Cancelled", tradable.getSide(), tradable.getId());
					
					MessagePublisher.getInstance().publishCancel(cm);
					
					addOldEntry(tradable);
					if (bookEntries.get(tradable.getPrice()).isEmpty())
						bookEntries.remove(tradable.getPrice());
					return;
				}
			
		}
		
		// Check to see if the Order with that Id is too late to cancel
		parentBook.checkTooLateToCancel(orderId);
		return;
	}
	
	// Submit a cancellation for a Quote. Removes the Quote and publishes a CancelMessage with the info from that Quote
	public synchronized void submitQuoteCancel(String userName) throws InvalidDataException {
		if (userName == null || userName.trim().isEmpty())
			throw new InvalidDataException("Error: ProductBookSide tried to submit a quote cancel with an invalid order ID.");
		
		TradableDTO results = removeQuote(userName.trim().toUpperCase());
		if (results == null)
			return;
		
		CancelMessage cm = new CancelMessage(results.user, results.product, results.price, results.remainingVolume,
				"Quote " + results.side + "-Side Cancelled", results.side, results.id);
		
		MessagePublisher.getInstance().publishCancel(cm);
		
	}
	
	
	// Will have the parent ProductBook of this ProductBookSide add the given tradable to its record of old entries
	public void addOldEntry(Tradable trd) throws InvalidDataException {
		if (trd == null)
			throw new InvalidDataException("Error: ProductBookSide tried to put a null Tradable into its parent book.");
		
		parentBook.addOldEntry(trd);
		
	}
	
	// Will get the Price of the given tradable and either add it to the list of other tradables at that Price if there is
	// already a record of that Price. Otherwise, if the Price isn't in the record, make a new list for that Price and 
	// add the tradable to that list
	public synchronized void addToBook(Tradable trd) throws InvalidDataException {
		if (trd == null)
			throw new InvalidDataException("Error: ProductBookSide tried to add a null Tradable to its book.");
		
		if (!bookEntries.containsKey(trd.getPrice())){
			ArrayList<Tradable> newList = new ArrayList<Tradable>();
			newList.add(trd);
			bookEntries.put(trd.getPrice(), newList);
		}
		else {
			ArrayList<Tradable> listAtPrice = bookEntries.get(trd.getPrice());
			listAtPrice.add(trd);
		}
	}
	
	// Attempt to perform trades against the BUY or SELL side depending on the side of the given tradable
	// Changed the iteration thu the HashMap
	public HashMap<String, FillMessage> tryTrade(Tradable trd) throws InvalidDataException {
		if (trd == null)
			throw new InvalidDataException("Error: ProductBookSide attempted to trade a null Tradable.");
		
		HashMap<String, FillMessage> allFills;
		if (getSide().equals("BUY"))
			allFills = trySellAgainstBuySideTrade(trd);
		
		else 
			allFills = tryBuyAgainstSellSideTrade(trd);
		
		//Collection<FillMessage> fillMessages = allFills.values();
		for (Entry<String, FillMessage> entry : allFills.entrySet())
			MessagePublisher.getInstance().publishFill(entry.getValue());
			
		return allFills;
	}
	
	// Have the TradeProcessor attempt to trade the given tradable.
	//Verify the While loop condition here
	public synchronized HashMap<String, FillMessage> trySellAgainstBuySideTrade(Tradable trd) throws InvalidDataException {
		if (trd == null)
			throw new InvalidDataException("Error: ProductBookSide attempted a SellAgainstBuySideTrade with a null Tradable.");
		
		HashMap<String, FillMessage> allFills = new HashMap<String, FillMessage>();
		HashMap<String, FillMessage> fillMsgs = new HashMap<String, FillMessage>();
		
		while ( (trd.getRemainingVolume() > 0 && !isEmpty()) 
								&&
			(trd.getPrice().isMarket() || trd.getPrice().lessOrEqual(topOfBookPrice())) ) {
			
			 
			
			HashMap<String, FillMessage> temp = executor.doTrade(trd);
			
			fillMsgs = mergeFills(fillMsgs, temp);
		}
		
		allFills.putAll(fillMsgs);
		
		return allFills;
	}
	
	// Merge the resulting FillMessages from all the trades into one list so that the user doesn't receive many FillMessages
	private HashMap<String, FillMessage> mergeFills(HashMap<String, FillMessage> existing, HashMap<String, FillMessage> newOnes)
		throws InvalidDataException {
		if (existing == null || newOnes == null)
			throw new InvalidDataException("Error: ProductBookSide attempted to merge a null mapping of FillMessages.");
		
		if (existing.isEmpty())
			return new HashMap<String, FillMessage>(newOnes);
		
		HashMap<String, FillMessage> results = new HashMap<String, FillMessage>(existing);
		for (String key : newOnes.keySet()) {
			if (!existing.containsKey(key)) 
				results.put(key, newOnes.get(key));
			else {
				FillMessage fm = results.get(key);
				fm.setVolume(newOnes.get(key).getVolume());
				fm.setDetails(newOnes.get(key).getDetails());
				
			}
			
		}
		
		return results;
	}
	
	// Have the TradeProcessor attempt to trade the given tradable.
	// Verify the While loop condition here
	public synchronized HashMap<String, FillMessage> tryBuyAgainstSellSideTrade(Tradable trd) throws InvalidDataException {
		if (trd == null)
			throw new InvalidDataException("Error: ProductBookSide attempted a BuyAgainstSellSideTrade with a null Tradable.");
		
		HashMap<String, FillMessage> allFills = new HashMap<String, FillMessage>();
		HashMap<String, FillMessage> fillMsgs = new HashMap<String, FillMessage>();
		
		while ( (trd.getRemainingVolume() > 0 && !isEmpty()) 
								&&
				(trd.getPrice().isMarket() || trd.getPrice().lessOrEqual(topOfBookPrice())) ) {
			
			HashMap<String, FillMessage> temp = executor.doTrade(trd);
			
			fillMsgs = mergeFills(fillMsgs, temp);
		}
		
		allFills.putAll(fillMsgs);
		
		return allFills;
	}
	
	// Clears out the record for a certain Price if there are no more tradables at that certain Price
	public synchronized void clearIfEmpty(Price p) throws InvalidDataException {
		if (p ==  null)
			throw new InvalidDataException("Error: ProductBookSide attempted a clear operation but used a null Price.");
		
		//if (bookEntries.containsKey(p)) {
			ArrayList<Tradable> listAtPrice = bookEntries.get(p);
			if (listAtPrice.isEmpty())
				bookEntries.remove(p);
		//}
	}
	
	// Remove a tradable from the record, if that tradable was the last tradable at a certain Price, that Price
	// is also removed from the record
	public synchronized void removeTradable(Tradable trd) throws InvalidDataException {
		if (trd == null)
			throw new InvalidDataException("Error: ProductBookSide attempted to perform a remove on a null Tradable.");
		
		ArrayList<Tradable> entries = bookEntries.get(trd.getPrice());
		if (entries == null)
			return;
		
		boolean wasPresent = entries.remove(trd);
		if(!wasPresent)
			return;
		if(entries.isEmpty())
			clearIfEmpty(trd.getPrice());
	}
}
