package driver;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import book.ProductService;
import client.User;
import client.UserImplFactory;
import client.UserSim;
import client.UserSimSettings;

public class MainAutomatedTest {

    public static CountDownLatch countDownLatch;

    public static void main(String[] args) {

        setupTradingSystem();
        automatedTestMode();
    }

    public synchronized static void simDone() {

        countDownLatch.countDown();
    }

    private static void setupTradingSystem() {
        try {
            ProductService.getInstance().createProduct("IBM");
            ProductService.getInstance().createProduct("CBOE");
            ProductService.getInstance().createProduct("GOOG");
            ProductService.getInstance().createProduct("AAPL");
            ProductService.getInstance().createProduct("GE");
            ProductService.getInstance().createProduct("T");
        } catch (Exception ex) {
            Logger.getLogger(MainAutomatedTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void automatedTestMode() {

        String name = "REX";
        boolean goodName = false;
        String error = "";
        int errorCount = 0;
        do {
            name = JOptionPane.showInputDialog(null, error + "Enter your First name", "Name", JOptionPane.INFORMATION_MESSAGE);

            if (name == null) {
                System.out.println("No Name provided - Defaulting to 'REX'");
                name = "REX";
                goodName = true;
                JOptionPane.showMessageDialog(null, "You have been assigned the default user name of 'REX'", "Default Name", JOptionPane.INFORMATION_MESSAGE);
            } else if (name.matches("^[a-zA-Z]+$")) {
                goodName = true;
            } else {
                errorCount++;
                if (errorCount >= 3) {
                    System.out.println("Too many tried - Defaulting to 'REX'");
                    name = "REX";
                    goodName = true;
                    JOptionPane.showMessageDialog(null, "You have been assigned the default user name of 'REX'", "Default Name", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    error = "Names must consist of letters only - please try again.\n\n";
                }
            }
        } while (!goodName);

        
       
        // Moved the creating of UserImpl into the try/catch, since set method throws InvalidDataException
        try {
        	User user1 = UserImplFactory.makeUser(name.toUpperCase());
            user1.connect();
            user1.showMarketDisplay();
        } catch (Exception ex) {
            Logger.getLogger(MainAutomatedTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        String results = JOptionPane.showInputDialog(null,
                "Enter simulation duration in seconds\nand click 'OK' to open the market.", 180);
        if (results == null) {
            System.out.println("Simulation Cancelled");
            System.exit(0);
        }
        int duration = Integer.parseInt(results);

        JOptionPane.showMessageDialog(null, "Please add all available Stock Symbols to your Market Display.\n" + ""
                + "Simulation will begin 15 seconds after clicking 'Ok'.",
                "Add Products", JOptionPane.INFORMATION_MESSAGE);

        ///
        System.out.println("Simulation starting in 15 seconds...");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            Logger.getLogger(MainAutomatedTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            ProductService.getInstance().setMarketState("PREOPEN"); // Replace PREOPEN with your preresenation of PREOPEN
        } catch (Exception ex) {
            Logger.getLogger(MainAutomatedTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        ///
        System.out.println("Simulation starting in 10 seconds...");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            Logger.getLogger(MainAutomatedTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            ProductService.getInstance().setMarketState("OPEN"); // Replace OPEN with your preresenation of OPEN
        } catch (Exception ex) {
            Logger.getLogger(MainAutomatedTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("Simulation starting in 5 seconds...");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            Logger.getLogger(MainAutomatedTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        UserSimSettings.addProductData("IBM", 189.40, 189.60, 200);
        UserSimSettings.addProductData("CBOE", 28.00, 28.15, 300);
        UserSimSettings.addProductData("GOOG", 608.00, 608.75, 500);
        UserSimSettings.addProductData("AAPL", 600.00, 601.00, 350);
        UserSimSettings.addProductData("GE", 19.55, 19.95, 100);
        UserSimSettings.addProductData("T", 34.25, 34.65, 250);

        System.out.println("Simulation starting: " + duration + " seconds remain.");

        int numSimUsers = 5;
        countDownLatch = new CountDownLatch(numSimUsers);

        // Added try/catch block here since the constructor of UserImpl throws an InvalidDataException
       
	    for (int i = 0; i < numSimUsers; i++) {
	    	try {
	            User u = UserImplFactory.makeUser("SIM" + (i + 1));
	            UserSim us = new UserSim((duration * 1000), u, false);
	            new Thread(us).start();
	        }
	    	catch (Exception ex){
	    		Logger.getLogger(MainAutomatedTest.class.getName()).log(Level.SEVERE, null, ex);
	    	}
        }
        
        

        try {
            countDownLatch.await();
            System.out.println("Done Waiting");
            ProductService.getInstance().setMarketState("CLOSED"); // Replace CLOSED with your preresenation of CLOSED
            JOptionPane.showMessageDialog(null, "The simulation has completed.",
                    "Completed", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            Logger.getLogger(MainAutomatedTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
