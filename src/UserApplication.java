//package src;

import ithakimodem.*;

//----------------------------------
// 			LIBRARIES
//----------------------------------	
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime; 		   // Import the LocalDateTime class
import java.time.format.DateTimeFormatter; // Import the DateTimeFormatter class

/**
 * @apiNote	This is a SCRIPT that runs the applications needed to "communicate" receive/generate
 * output requested from the assignment instructions (Assignment-Instructions.pdf) .
 * 
 * (From each session, we keep/save the output folder)
 */
public class UserApplication {
	
	public static void main(String[] args) {
		
		System.out.println("Computer Networks I - ASSIGNMENT");
		System.out.println("JAVA SERIAL COMMUNICATIONS PROGRAMMING");
		System.out.println("-----------------------------------------");
		
		// -----------------------------------------------------------
		//     SAVING Ithaki's REQUEST CODES into VARIABLES
		// 	(from: http://ithaki.eng.auth.gr/netlab/action.php) 
		// -----------------------------------------------------------		
		final String echo_request_code = "E????";					// Echo request code
		final String image_request_code_withoutErrors = "M????"; 	// Image request code (Tx/Rx error free)
		final String image_request_code_withErrors = "G????";		// Image request code (Tx/Rx with errors)
		final String gps_request_code = "P????"; 					// GPS request code
		final String ack_result_code = "Q????";						// ARQ - ACK (positive acknowledgement) result code
		final String nack_result_code = "R????";					// ARQ - NACK (negative acknowledgement) result code   
		
		// -----------------------------------------------------------
		// 		    CREATING the virtual modem && SETTINGS
		// -----------------------------------------------------------		
		Modem IthakiModem = new Modem();
		
		int IthakiModemSPEED = 80000;
		IthakiModem.setSpeed(IthakiModemSPEED);			// SETIING the virtual modem's (Ithaki's) SPEED [Limits: 1 - 80 Kbps]
		int IthakiModemTIMEOUT = 2000;
		IthakiModem.setTimeout(IthakiModemTIMEOUT);		// SETTING the virtual modem's (Ithaki's) TIMEOUT
		
		// =====================================================
		// 				OPENING the virtual modem
		// =====================================================
		if(IthakiModem.open("ithaki")) {	// "ithaki" argument opens up an Ithaki's Virtual Modem (Other modems available)
			// Console output:
			System.out.println();
	        System.out.println("=====================================================");
			System.out.println("         Opened an Ithaki's virtual modem!           ");
			System.out.println();
			System.out.println("Modem's SPEED: " + IthakiModemSPEED + "bps           ");
			System.out.println("Modem's TIMEOUT: " + IthakiModemTIMEOUT + " (s)      ");
	        System.out.println("=====================================================");
		} else {
			return;
		}
		
		// CURRENTLY IN "COMMAND" MODE.
		// Only "AT" (from "AT"tention) type of commands are accepted
		
		// -----------------------------------------------------------
		// 			Setting the virtual modem to data mode
		//     	   (NEEDED TO RECEIVE EVERY TYPE OF COMMAND)
		// -----------------------------------------------------------
		// Console output:
		System.out.println();
        System.out.println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
		System.out.println("  DIALING UP Ithaki - Setting the modem to DATA mode ");
		System.out.println("   (Needed to receive every other type of command)   ");
		System.out.println();
		System.out.println("           Typing \"ATD2310ITHAKI\" ...              ");
        System.out.println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
        //
		if(IthakiModem.write("ATD2310ITHAKI\r".getBytes())) {
            getALLOutputLines(IthakiModem);        	
        }
		
		// -----------------------------------------------------------
        //	  Testing the connection to the modem by typing TEST
		// -----------------------------------------------------------
		// Console output:
		System.out.println();
        System.out.println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
		System.out.println(" Typing \"TEST\" to test the connection the modem... ");
        System.out.println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
        //
		if(IthakiModem.write(("TEST\r").getBytes())) {
			String TESTOutputLines = getALLOutputLines(IthakiModem);
			// Expected output: PSTART DD-MM-YYYY HH:MM:SS ITHAKI JAVA LAB SERVER TEST PSTOP
			if (!TESTOutputLines.contains("ITHAKI JAVA LAB SERVER TEST")) {
				// Console output:
				System.out.println("Connection couldn't be tested. Aborting...");
				
				return;
			}
		}	
        
		// -----------------------------------------------------------
	    //         APPLICATIONS' RESULTS and MEASUREMENTS
		// ----------------------------------------------------------- 
		// Console output:
		System.out.println();
        System.out.println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
		System.out.println("            Running ALL the APPLICATIONS...          ");
        System.out.println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
        // An array containing date and times the application started running
        LocalDateTime[] DateTime = new LocalDateTime[5];
		
        /*  
		 * ---------------------------
         * |     Echo Application    |
         * ---------------------------
         */
		// Console output:
		System.out.println();
		System.out.println("---------------------------");
    	System.out.println("|     Echo Application    |");
		System.out.println("---------------------------");
		System.out.println();
		DateTime[0] = LocalDateTime.now();  // Time the Echo application starts
		//
	    Echo.EchoApplication(IthakiModem, echo_request_code, (5 * 60), "output/Echo/");	    
	    
	    /* ---------------------------
         * |     ARQ Application     |
         * ---------------------------
         */
		// Console output:
	    System.out.println();
		System.out.println("---------------------------");
    	System.out.println("|     ARQ Application     |");
		System.out.println("---------------------------");
		System.out.println();
		DateTime[1] = LocalDateTime.now();	// Time the ARQ application starts
	    //
	    ARQ.ARQApplication(IthakiModem, ack_result_code, nack_result_code, (5 * 60), "output/ARQ/");    
	    
	    /* ---------------------------
         * |  CAM-IMAGES Application |
         * ---------------------------
         */
	    // Console output:
	    System.out.println();
		System.out.println("---------------------------");
    	System.out.println("|  CAM-IMAGES Application |");
		System.out.println("---------------------------");
		System.out.println();
		DateTime[2] = LocalDateTime.now();	// Time the Images application starts
		//
		// (image_request_code_withoutErrors produces an error free image)
		// (image_request_code_withErrors produces pseudoerrors in the image)
		// When "CAM=FIX" (Default value) accompanies the image_request_code, the fixxed camera is used 
		// When "CAM=PTZ" accompanies the image_request_code, the moveable (using a servo moter) camera is used
	    Image.generateImage(Image.receiveImageBytes(IthakiModem, image_request_code_withoutErrors + "CAM=FIX"), "output/images/error-free/image_errorFree_FIX.jpg");
	    Image.generateImage(Image.receiveImageBytes(IthakiModem, image_request_code_withoutErrors + "CAM=PTZ"), "output/images/error-free/image_errorFree_PTZ.jpg");
	    Image.generateImage(Image.receiveImageBytes(IthakiModem, image_request_code_withErrors + "CAM=FIX"), "output/images/with-errors/image_withErrors_FIX.jpg");
	    Image.generateImage(Image.receiveImageBytes(IthakiModem, image_request_code_withErrors + "CAM=PTZ"), "output/images/with-errors/image_withErrors_PTZ.jpg");
	    
	    /* ---------------------------
         * |     GPS Application     |
         * ---------------------------
         */
		// Console output:
	    System.out.println();
		System.out.println("---------------------------");
    	System.out.println("|     GPS Application     |");
		System.out.println("---------------------------");
		System.out.println();
		DateTime[3] = LocalDateTime.now();	// Time the GPS application starts
        //
        // When "R=ΧPPPPLL" accompanies the code gps_request_code, Ithaki's server send LL GPS packets from 
		// the pre-saved route Χ, starting with the packet PPPP.
		// HERE: Route -> X = 1
		//		 Starting packet -> PPPP = 0280 (Random packet number we chose)
		//		 Number of packets -> LL = 99 (MAX)
		String RParameter = "R=1028099";		
	 	String FULL_TParameter = GPS.GPSApplication(IthakiModem, gps_request_code + RParameter);
	    // Returns the: FULL_TParameter = "T=AABBCCDDEEFF" (Longtitude: AA BB' CC'' and Latitude: DD EE' FF'')
	 	//
	    // Generate an image taken, from Google Maps at the cordinates with Longtitude: AA BB' CC'' and Latitude: DD EE' FF'', by Ithaki 
	 	System.out.println("(Receiving Google Maps image with the chosen points being marked...)");
	 	System.out.println(".");
	 	System.out.print(".");
		DateTime[4] = LocalDateTime.now();	// Time the GPS-IMAGE application starts
	    Image.generateImage(Image.receiveImageBytes(IthakiModem, gps_request_code + FULL_TParameter), "output/images/GPS/GPS_Image_GoogleMaps.jpg");	    	    	    
		// -----------------------------------------------------------        

		// =====================================================
		//          Closed the Ithaki's virtual modem!          
		// =====================================================
	    if(IthakiModem.close()) {
			// Console output:
	    	System.out.println();
	        System.out.println("=====================================================");
	    	System.out.println("         Closed the Ithaki's virtual modem!          ");
	        System.out.println("=====================================================");
	    } else 
	    	return;
	    
	    // Files output:
	    saveAppsDatesTimes2FILE(DateTime);
	    saveRequestCodes2FILE(echo_request_code, image_request_code_withoutErrors, image_request_code_withErrors, 
	    					  gps_request_code, ack_result_code, nack_result_code, RParameter , FULL_TParameter);
	}
	
	/**
	 * Reads the VirtualModem's output and return the full text it returns
	 * (Reads until -1 is returned from the modem (End of stream))
	 * 
	 * @param VirtualModem Ithaki's modem we send the request to
	 * 
	 * @return WHOLE text from the VirtualModem's output
	 */
	public static String getALLOutputLines(Modem VirtualModem) {
		int returnIntModem;
		String FullText = "";
		while (true) {
			returnIntModem = VirtualModem.read();
			if (returnIntModem == -1) {	
				break;
			}
			System.out.print((char) returnIntModem);
			FullText += (char) returnIntModem;
		}
		return FullText;
	}
	
	/**
	 * Saves all the request codes used into a .txt file
	 */
	private static void saveRequestCodes2FILE(String echo_request_code,String image_request_code_withoutErrors, String image_request_code_withErrors, 
											  String gps_request_code, String ack_result_code, String nack_result_code,
											  String RParameter, String FULL_TParameter){
		try(FileWriter RequestCodes = new FileWriter(new File("output/" + "RequestCodes.txt"))){
			RequestCodes.write("Echo request code: " + echo_request_code + "\r\n");
			RequestCodes.write("Image request code (Tx/Rx error free): " + image_request_code_withoutErrors + "\r\n");
			RequestCodes.write("Image request code (Tx/Rx with errors): " + image_request_code_withErrors + "\r\n");			
			RequestCodes.write("GPS request code: " + gps_request_code + "\r\n");	
			RequestCodes.write("GPS - R= Parameter code: " + RParameter + "\r\n");	
			RequestCodes.write("GPS - Points' Full T= Parameter code: " + FULL_TParameter + "\r\n");			
			RequestCodes.write("ARQ - ACK (positive acknowledgement) result code: " + ack_result_code + "\r\n");			
			RequestCodes.write("ARQ - NACK (negative acknowledgement) result code: " + nack_result_code + "\r\n");			
		} catch (Exception x) {
			System.out.println("Caught EXCEPTION: " + x + " ! Couldn't create files: output/RequestCodes.txt .");
			System.out.println("Request codes are not saved into a file!");			}
	}
	
	/**
	 * Saves the date and time the applications started into a .txt file
	 * 
	 * @param DatesTimes An array containing the date and times the application started running
	 */
	private static void saveAppsDatesTimes2FILE(LocalDateTime[] DatesTimes) {
		try(FileWriter AppsExecutionDatesTimes = new FileWriter(new File("output/" + "AppsExecutionDatesTimes.txt"))){
			DateTimeFormatter DateTimeFORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
			
			AppsExecutionDatesTimes.write("Echo application run at: " + DatesTimes[0].format(DateTimeFORMAT) + "\r\n");
			AppsExecutionDatesTimes.write("ARQ application run at: " + DatesTimes[1].format(DateTimeFORMAT) + "\r\n");
			AppsExecutionDatesTimes.write("Images application run at: " + DatesTimes[2].format(DateTimeFORMAT) + "\r\n");
			AppsExecutionDatesTimes.write("GPS application run at: " + DatesTimes[3].format(DateTimeFORMAT) + "\r\n");
			AppsExecutionDatesTimes.write("GPS-IMAGE application run at: " + DatesTimes[3].format(DateTimeFORMAT) + "\r\n");	

		} catch (Exception x) {
			System.out.println("Caught EXCEPTION: " + x + " ! Couldn't create files: output/AppsExecutionDatesTimes.txt .");
			System.out.println("Applications' execution times are not saved into a file!");		}
	}

}
