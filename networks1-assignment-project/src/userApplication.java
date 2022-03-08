import ithakimodem.*;

import java.io.File;
import java.io.FileWriter;
import java.util.Date;

public class userApplication {
	public static void main(String[] args) {
		
		// CREATING the virtual modem 
		// -----------------------------------------------------------		
		Modem IthakiModem = new Modem();
		IthakiModem.setSpeed(80000);		// SETIING the virtual modem's (Ithaki's) SPEED [1 - 80 Kbps]
		IthakiModem.setTimeout(2000);		// Setting the virtual modem's (Ithaki's) TIMEOUT
		// -----------------------------------------------------------
		
		// OPENING the virtual modem
		// -----------------------------------------------------------
		IthakiModem.open("Ithaki");
		
		// SAVING Ithaki's REQUEST CODES into VARIABLES
		// (from: http://ithaki.eng.auth.gr/netlab/action.php) (!! CHANGE EVERY 2 HOURS)
		// -----------------------------------------------------------
		String echo_request_code = "E7984"; 				// Echo request code
		String image_request_code_withoutErrors = "M7232";  // Image request code (Tx/Rx error free)
		String image_request_code_withErrors = "G1888"; 	// Image request code (Tx/Rx with errors)
		String gps_request_code = "P1347"; 					// GPS request code
//		String gps_request_code_Complete = gps_request_code + "R=1000099";
		String ack_result_code = "Q1743"; 					// ACK (positive acknowledgement) result code
		String nack_result_code = "R1411"; 					// NACK (negative acknowledgement) result code
		// AND ALSO:
		// SAVING THOSE Ithaki's REQUEST CODES into a .txt FILE (requests_codes.txt)
		// and the session (time and date) those request codes were available 
		// -----------------------------------------------------------
	    try (FileWriter request_codes = new FileWriter(new File("logs/requests_codes.txt"))) {
	    	request_codes.write("----------------------------------------------------------- \n");
	    	request_codes.write("LAB DURATION FROM HH:MM UNTIL HH:MM, DATE: DD-MM-YYYY \n");
	    	request_codes.write("----------------------------------------------------------- \n");
	    	request_codes.write("Echo request code: " + echo_request_code + "\n");
	    	request_codes.write("Image request code (Tx/Rx error free): " + image_request_code_withoutErrors + "\n");
	    	request_codes.write("Image request code (Tx/Rx with errors): " + image_request_code_withErrors + "\n");
	    	request_codes.write("GPS request code: " + gps_request_code + "\n");
//	    	request_codes.write("GPS Full request code: " + gps_request_code_Complete + "\n");
	    	request_codes.write("ACK (positive acknowledgement) result code: " + ack_result_code + "\n");
	    	request_codes.write("NACK (negative acknowledgement) result code: " + nack_result_code + "\n");
	    	request_codes.write("----------------------------------------------------------- \n");
	    } catch (Exception x) {
	    	System.out.println("Caught EXCEPTION: " + x + " !! Couldn't save REQUEST CODES into a FILE!");
	    }
		// -----------------------------------------------------------

	    
	    
	    
//	    // -----------------------------------------------------------
//		// Ithaki's virtual modem's request codes, sometimes need to be followed by
//		// the character "\r" or ASCII 13 (code delimiter).
//	    String code_delimiter = "\r";
	    
	    
 
	    
	        


		// CLOSING the virtual modem
		// -----------------------------------------------------------
	    IthakiModem.close();

	}
	
	
	
}
