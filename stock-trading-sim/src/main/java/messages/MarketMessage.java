package messages;

import exceptions.InvalidDataException;

/**
 * A class to represent the state of the market. A valid market state can only
 * be the represented by the Strings: "OPEN", "PREOPEN", and "CLOSED" the set
 * method makes sure that the market state is neither null or an empty String,
 * will only allow the state to be set to a valid state.
 * 
 * @author Peter Swantek
 *
 */

public class MarketMessage {

    private String state;

    public MarketMessage(String newState) throws InvalidDataException {

        setState(newState);
    }

    // Set the market's state data field.  Reject null or empty Strings, verifies that market state is a valid value
    private void setState(String newState) throws InvalidDataException {
        if (newState == null || newState.trim().isEmpty()) {
            throw new InvalidDataException("Error: Market message's state can't be null or empty.");
        }

        else if (!newState.trim().toUpperCase().equals("CLOSED") && !newState.trim().toUpperCase().equals("PREOPEN") && !newState.trim().toUpperCase().equals("OPEN")) {
            throw new InvalidDataException("Error: Market message state was set to an illegal value.");
        }

        state = newState.trim().toUpperCase();
    }

    public String getState() {
        return state;
    }

    @Override
    public String toString() {
        return "[" + getState() + "]";
    }
}
