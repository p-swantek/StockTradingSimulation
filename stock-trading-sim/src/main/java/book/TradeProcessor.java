package book;

import java.util.Map;

import exceptions.InvalidDataException;
import messages.FillMessage;
import tradable.Tradable;

public interface TradeProcessor {

    public Map<String, FillMessage> doTrade(Tradable trd) throws InvalidDataException;

}
