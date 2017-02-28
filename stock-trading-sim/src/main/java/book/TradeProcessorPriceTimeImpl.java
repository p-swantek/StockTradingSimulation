package book;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import exceptions.InvalidDataException;
import messages.FillMessage;
import price.Price;
import tradable.Tradable;

/**
 * A class that provides an implementation of the TradeProcessor interface. This
 * particular class will provide an implementation of the PriceTime trading
 * algorithm. The class maintains a record of FillMessages for trades as well as
 * a reference to the ProductBookSide object with which it is associated.
 * 
 * @author Peter Swantek
 *
 */

public class TradeProcessorPriceTimeImpl implements TradeProcessor {

    private Map<String, FillMessage> fillMessages;
    private ProductBookSide theOwner;

    public TradeProcessorPriceTimeImpl(ProductBookSide newOwner) throws InvalidDataException {
        fillMessages = new HashMap<>();
        setTheOwner(newOwner);
    }

    // Sets the ProductBookSide that owns this implementor, makes sure the ProductBookSide isn't null
    private void setTheOwner(ProductBookSide newOwner) throws InvalidDataException {
        if (newOwner == null) {
            throw new InvalidDataException("Error: The owner of a TradeProccesorPriceTimeImpl object was null.");
        }

        theOwner = newOwner;
    }

    // Creates a unique key for FillMessages
    private String makeFillKey(FillMessage fm) throws InvalidDataException {
        if (fm == null) {
            throw new InvalidDataException("Error: TradeProcessorImpl attempted to use a null FillMessage.");
        }

        return fm.getUser() + fm.getId() + fm.getPrice();
    }

    // Searches the record of FillMessages to see if the given FillMessage is present.
    // If the FillMessage isn't present, it is new and this will return true.
    // If the FillMessage is in the record, but has a different side or user, also return true as this is a new FillMessage
    // Otherwise, the FillMessage is not new so return false
    private boolean isNewFill(FillMessage fm) throws InvalidDataException {
        if (fm == null) {
            throw new InvalidDataException("Error: TradeProcessorImpl attempted to use a null FillMessage.");
        }

        String key = makeFillKey(fm);

        if (!fillMessages.containsKey(key)) {
            return true;
        }

        FillMessage oldFill = fillMessages.get(key);
        if (!oldFill.getSide().equals(fm.getSide())) {
            return true;
        } else if (!oldFill.getId().equals(fm.getId())) {
            return true;
        }

        return false;
    }

    // If the given FillMessage is new, make a key for it and add it to the record.
    // Otherwise, if the FillMessage isn't new, make a key for it and grab the FillMessage with that key from the record,
    // Update the volume of that FillMessage to be the sum of its volume plus the volume of the passed FillMessage, also
    // update the details to be the details of the passed in FillMessage
    private void addFillMessage(FillMessage fm) throws InvalidDataException {
        if (fm == null) {
            throw new InvalidDataException("Error: TradeProcessorImpl attempted to use a null FillMessage.");
        }

        if (isNewFill(fm)) {
            String key = makeFillKey(fm);
            fillMessages.put(key, fm);
        }

        else {
            String key = makeFillKey(fm);
            FillMessage theMessage = fillMessages.get(key);
            int updatedVolume = fm.getVolume() + theMessage.getVolume();
            theMessage.setVolume(updatedVolume);
            theMessage.setDetails(fm.getDetails());
        }

    }

    // Perform trading of the passed in tradable
    public Map<String, FillMessage> doTrade(Tradable trd) throws InvalidDataException {
        fillMessages = new HashMap<>();
        ArrayList<Tradable> tradedOut = new ArrayList<>();
        List<Tradable> entriesAtPrice = theOwner.getEntriesAtTopOfBook();

        for (Tradable t : entriesAtPrice) {

            // Go to After-For section if remaing volume is 0
            if (trd.getRemainingVolume() == 0) {
                break;
            }

            if (trd.getRemainingVolume() >= t.getRemainingVolume()) {
                tradedOut.add(t);
                Price tradePrice;
                if (t.getPrice().isMarket()) {
                    tradePrice = trd.getPrice();
                } else {
                    tradePrice = t.getPrice();
                }

                FillMessage tFillMessage = new FillMessage(t.getUser(), t.getProduct(), tradePrice, t.getRemainingVolume(), "leaving 0", t.getSide(), t.getId());

                addFillMessage(tFillMessage);

                FillMessage trdFillMessage = new FillMessage(trd.getUser(), trd.getProduct(), tradePrice, t.getRemainingVolume(), "leaving " + (trd.getRemainingVolume() - t.getRemainingVolume()),
                        trd.getSide(), trd.getId());

                addFillMessage(trdFillMessage);

                trd.setRemainingVolume(trd.getRemainingVolume() - t.getRemainingVolume());
                t.setRemainingVolume(0);

                theOwner.addOldEntry(t);
            }

            else {
                int remainder = t.getRemainingVolume() - trd.getRemainingVolume();
                Price tradePrice;
                if (t.getPrice().isMarket()) {
                    tradePrice = trd.getPrice();
                } else {
                    tradePrice = t.getPrice();
                }

                FillMessage tFillMessage = new FillMessage(t.getUser(), t.getProduct(), tradePrice, trd.getRemainingVolume(), "leaving " + remainder, t.getSide(), t.getId());

                addFillMessage(tFillMessage);

                FillMessage trdFillMessage = new FillMessage(trd.getUser(), trd.getProduct(), tradePrice, trd.getRemainingVolume(), "leaving 0", trd.getSide(), trd.getId());

                addFillMessage(trdFillMessage);

                trd.setRemainingVolume(0);
                t.setRemainingVolume(remainder);

                theOwner.addOldEntry(trd);

                break;
            }

        }

        // After-For section

        for (Tradable tradable : tradedOut) {
            entriesAtPrice.remove(tradable);
        }

        if (entriesAtPrice.isEmpty()) {
            theOwner.clearIfEmpty(theOwner.topOfBookPrice());
        }

        return fillMessages;

    }

}
