package book;

import java.util.HashMap;
import messages.FillMessage;
import tradable.Tradable;
import exceptions.InvalidDataException;

public interface TradeProcessor {
	
	public HashMap<String, FillMessage> doTrade(Tradable trd) throws InvalidDataException;

}
