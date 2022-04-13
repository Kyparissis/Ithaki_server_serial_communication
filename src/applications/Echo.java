package applications;

import ithakimodem.*;

// ----------------------------------
// 			 LIBRARIES
// ----------------------------------	
import java.io.File;
import java.io.FileWriter;

/**
 * @apiNote	This class contains methods in order to receive text packet's
 * from Ithaki's VirtualModem
 */
public class Echo {
	/**
	 * This method  receives ECHO packets from Ithaki's VirtualModem for the duration 
	 * of the runtimeInSeconds (sec). Then saves in a file the time it took to receive 
	 * that packet from the VirtualModem.
	 * (Those files are saved in the folder directory [Use \ for UNIX, / for WINDOWS)]
	 * 
	 * @param VirtualModem 		Ithaki's modem we send the request to
	 * @param EchoRequestCode	The request code in order to receive Echo packets
	 * @param runtimeInSeconds 	Duration of the application
	 * @param folder 			The directory to store the ouput files
	 * 
	 * @return
	 */
	public static void EchoApplication(Modem VirtualModem, String EchoRequestCode, int runtimeInSeconds, String folder) {
		// Trying to create output files in order to store: ECHO packet times
		try (FileWriter echoPacketsTimes = new FileWriter(new File(folder + "Echo_PacketsReceiveTimesMillis.txt"))) {
			String EchoPacketText = "";

			// ECHO packet TIMERS
			long packet_sentTime = 0; 		// The time the echo packet was sent from the modem
			long packet_receivedTime = 0; 	// The time we received the echo packet from the modem
			
			// COUNTERS
			int NumberOfPacketsReceived = 0;

			long startCallingPacketsTime = System.currentTimeMillis(); // Time we started to ask for echo packets from the modem
			
			while (System.currentTimeMillis() <= startCallingPacketsTime + 1000 * runtimeInSeconds) {
				
				// Receiving packets and calculating it's time:
				packet_sentTime = System.currentTimeMillis();
				EchoPacketText = receivePacket(VirtualModem, EchoRequestCode);
				packet_receivedTime = System.currentTimeMillis();
				
				// Increasing  counters accordingly
				NumberOfPacketsReceived++;

				
				// Console output:
				System.out.println("-> Received Echo packet: " + EchoPacketText );
				System.out.println("[ECHO][Packet #" + NumberOfPacketsReceived + " (PC=" + getPacketPC(EchoPacketText) + ") delivery time: " + (packet_receivedTime - packet_sentTime) + " (ms)]\n");
				
				// Files output:
				echoPacketsTimes.write((packet_receivedTime - packet_sentTime) + "\r\n");		// Save packet's system response time
				

			}

//			long stopCallingPacketsTime = System.currentTimeMillis(); // Time we stopped asking for echo packets from the modem
			
			// Results console output:
		    System.out.println("[ECHO][ Received " + NumberOfPacketsReceived +" packets SUCCESSFULLY from Ithaki's Virtual Modem ]");
			
		} catch (Exception x) {
			System.out.println("Caught EXCEPTION: " + x + " ! Couldn't create files: "+ folder + "echoPacketsTimes.txt AND" +
					                                                                    folder + "Echo_PacketsINFO.txt .");
		}
		
	}
	
	/**
	 * This method sends a request to Ithaki's VirtualModem using the 
	 * RequestCode and receives as an "answer" a String packet.
	 * 
	 * @param VirtualModem	Ithaki's modem we send the request to
	 * @param RequestCode	The request code in order to receive ECHO packets
	 * 
	 * @return Packet's text FULL String
	 */
	private static String receivePacket(Modem VirtualModem, String RequestCode) {
		String ReceivedPacketText = ""; // Variable to build the packet's string
		
		// Sending request to the virtual modem.
		boolean VirtualModemResponse = VirtualModem.write((RequestCode + "\r").getBytes());
		// Getting virtual modem's "answer" to the request.
		if (VirtualModemResponse) {
			// Every packet starts with header PSTART and ends with footer PSTOP
			// We can until our "builder" string ends with the footer
			int VirtualModemOutput;
			while (!ReceivedPacketText.endsWith("PSTOP")) {
				VirtualModemOutput = VirtualModem.read();	// Read "answer's" Integer
				ReceivedPacketText += (char) VirtualModemOutput;	// Append Integer's char value to packet's String
			}
		}
		return ReceivedPacketText;
	}
	
	/**
	 * Methods to parse the "Echo type" packet sent by the VirtualModem
	 * 
	 * @param EchoPacket : An "Echo type" packet sent by the VirtualModem. Example:
	 *               			  	"PSTART DD-MM-YYYY HH-MM-SS PC PSTOP"
	 */
	// -------------------------------------------------------
	// Returns DATE (from given ex. = "DD-MM-YYYY")
//	private static String getPacketDate(String EchoPacket) { 
//		return EchoPacket.split(" ")[1];
//	}

	// Returns HOUR (from given ex. = "HH-MM-SS")
//	private static String getPacketHour(String EchoPacket) {
//		return EchoPacket.split(" ")[2];
//	}
	
	// Returns PacketCounter%100 (from given ex. = "PC")
	private static String getPacketPC(String EchoPacket) { 
		return EchoPacket.split(" ")[3];
	}
	
}
