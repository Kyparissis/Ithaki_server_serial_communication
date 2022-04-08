import ithakimodem.*;

//----------------------------------
// 			LIBRARIES
//----------------------------------	
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime; 		   // Import the LocalDateTime class
import java.time.format.DateTimeFormatter; // Import the DateTimeFormatter class

public class UserApplication {
	
	public static void main(String[] args) {
		
		System.out.println("Computer Networks I - ASSIGNMENT");
		System.out.println("JAVA SERIAL COMMUNICATIONS PROGRAMMING");
		System.out.println("-----------------------------------------");
		
		// -----------------------------------------------------------
		// SAVING Ithaki's REQUEST CODES and SESSION into VARIABLES
		// 	(from: http://ithaki.eng.auth.gr/netlab/action.php) 
		// 		[SESSION FROM 23:28 UNTIL 1:28, 05-04-2022]
		// -----------------------------------------------------------		
		final String echo_request_code = "E0510";					// Echo request code
		final String image_request_code_withoutErrors = "M4401"; 	// Image request code (Tx/Rx error free)
		final String image_request_code_withErrors = "G7704";		// Image request code (Tx/Rx with errors)
		final String gps_request_code = "P7910"; 					// GPS request code
		final String ack_result_code = "Q2633";						// ARQ - ACK (positive acknowledgement) result code
		final String nack_result_code = "R7784";					// ARQ - NACK (negative acknowledgement) result code   
		
		// -----------------------------------------------------------
		// 			CREATING the virtual modem / SETTINGS
		// -----------------------------------------------------------		
		Modem IthakiModem = new Modem();
		
		IthakiModem.setSpeed(80000);		// SETIING the virtual modem's (Ithaki's) SPEED [Limits: 1 - 80 Kbps]
		IthakiModem.setTimeout(2000);		// SETTING the virtual modem's (Ithaki's) TIMEOUT
		
		// =====================================================
		// 				OPENING the virtual modem
		// =====================================================
		if(IthakiModem.open("ithaki")) {	// "ithaki" argument opens up an Ithaki's Virtual Modem (Other were available)
			// Console output:
			System.out.println();
	        System.out.println("=====================================================");
			System.out.println("         Opened an Ithaki's virtual modem!           ");
	        System.out.println("=====================================================");
		} else {
			return;
		}
		
//		System.out.println("Currently in COMMAND mode - ONLY \"AT\" commands accepted");
//		System.out.println();
		
		// -----------------------------------------------------------
		// 			Setting the virtual modem to data mode
		// 			  (NEEDED TO RECEIVE EVERY COMMAND)
		// -----------------------------------------------------------
		// Console output:
		System.out.println();
        System.out.println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
		System.out.println("DIALING UP Ithaki - Setting the modem to DATA mode...");
		System.out.println("   (Needed to receive every other type of command)   ");
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
			getALLOutputLines(IthakiModem);
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
        LocalDateTime[] DateTime = new LocalDateTime[4];
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
	    Echo.EchoApplication(IthakiModem, echo_request_code, (5 * 1), "output/Echo/");	    
	    
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
	    ARQ.ARQApplication(IthakiModem, ack_result_code, nack_result_code, (5 * 1), "output/ARQ/");    
	    
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
	    Image.generateImage(Image.getImageData(IthakiModem, image_request_code_withoutErrors + "CAM=FIX"), "output/images/error-free/image_errorFree_FIX.jpg");
	    Image.generateImage(Image.getImageData(IthakiModem, image_request_code_withoutErrors + "CAM=PTZ"), "output/images/error-free/image_errorFree_PTZ.jpg");
	    Image.generateImage(Image.getImageData(IthakiModem, image_request_code_withErrors + "CAM=FIX"), "output/images/with-errors/image_withErrors_FIX.jpg");
	    Image.generateImage(Image.getImageData(IthakiModem, image_request_code_withErrors + "CAM=PTZ"), "output/images/with-errors/image_withErrors_PTZ.jpg");
	    
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
		// HERE: Route = 1
		//		 Starting packet: 0280
		//		 Number of packets: 99 (MAX)
		String RParameter = "R=1028099";		
	 	String FULL_TParameter = GPS.GPSApplication(IthakiModem, gps_request_code + RParameter);
	    // Returns the: FULL_TParameter = "T=AABBCCDDEEFF" (Longtitude: AA BB' CC'' and Latitude: DD EE' FF'')
	 	//
	    // Generate an image taken, from Google Maps at the cordinates with Longtitude: AA BB' CC'' and Latitude: DD EE' FF'', by Ithaki 
	 	System.out.println("(Generating Google Maps image with the chosen points being marked...)");
	 	System.out.println(".");
	 	System.out.print(".");
	    Image.generateImage(Image.getImageData(IthakiModem, gps_request_code + FULL_TParameter), "output/images/GPS/GPS_Image_GoogleMaps.jpg");	    	    	    
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
	    					  gps_request_code, ack_result_code, nack_result_code);
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
		String FullText = null;
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
											  String gps_request_code, String ack_result_code, String nack_result_code){
		try(FileWriter RequestCodes = new FileWriter(new File("output/" + "RequestCodes.txt"))){
			RequestCodes.write("Echo request code: " + echo_request_code + "\r\n");
			RequestCodes.write("ARQ request code: " + image_request_code_withoutErrors + "\r\n");
			RequestCodes.write("Image request code (Tx/Rx error free): " + image_request_code_withoutErrors + "\r\n");
			RequestCodes.write("Image request code (Tx/Rx with errors): " + image_request_code_withErrors + "\r\n");			
			RequestCodes.write("GPS request code: " + gps_request_code + "\r\n");			
			RequestCodes.write("ARQ - ACK (positive acknowledgement) result code: " + ack_result_code + "\r\n");			
			RequestCodes.write("ARQ - NACK (negative acknowledgement) result code: " + nack_result_code + "\r\n");			
		} catch (Exception x) {
			System.out.println(x);
		}
	}
	
	/**
	 * Saves the date and time the applications started into a .txt file
	 * 
	 * @param DatesTimes An array containing the date and times the application started running
	 */
	private static void saveAppsDatesTimes2FILE(LocalDateTime[] DatesTimes) {
		try(FileWriter Applications_DatesTimes = new FileWriter(new File("output/" + "Applications_DatesTimes.txt"))){
			DateTimeFormatter DateTimeFORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
			
			Applications_DatesTimes.write("Echo application run at: " + DatesTimes[0].format(DateTimeFORMAT) + "\r\n");
			Applications_DatesTimes.write("ARQ application run at: " + DatesTimes[1].format(DateTimeFORMAT) + "\r\n");
			Applications_DatesTimes.write("Images application run at: " + DatesTimes[2].format(DateTimeFORMAT) + "\r\n");
			Applications_DatesTimes.write("GPS application run at: " + DatesTimes[3].format(DateTimeFORMAT) + "\r\n");	
		} catch (Exception x) {
			System.out.println(x);
		}
	}	
	
}
