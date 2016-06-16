package client;


import exceptions.InvalidDataException;


/**
 * Produces instances of implementations of the User interface, currently only has one implementation but allows for further implementations to be added in the future.
 * 
 * 
 * @author Peter Swantek
 *
 */
public class UserImplFactory {
	
	public static User makeUser(String newUserName) throws InvalidDataException{
		return new UserImpl(newUserName);
	}

}
