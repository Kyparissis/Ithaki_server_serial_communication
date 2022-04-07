//package src;

import ithakimodem.*;

//----------------------------------
// 			LIBRARIES
//----------------------------------	
import java.io.File;
import java.io.FileWriter;


/**
 * @apiNote	This class contains methods in order to receive text packet's from Ithaki's
 * VirtualModem and	cope with possible errors in them using ARQ (Automatic Repeat Request)
 * mechanisms.
 */
public class ARQ {

	/**
	 * This method uses an Automatic Repeat Request method to receive ARQ packets
	 * from Ithaki's VirtualModem for the duration of the runtimeInSeconds (sec)
	 * Then saves in a file the number of nack request each packet (ack request) needed AND in a
	 * second file saves the time it took to receive that packet (without noise) from the VirtualModem.
	 * (Those files are saved in the folder directory [Use \ for UNIX, / for WINDOWS)]
	 * At last, it calculates the Bit Error Rate using the calculateBER() method ...
	 * 
	 * @param VirtualModem 		Ithaki's modem we send the request to
	 * @param AckResult_RequestCode 	ACK(positive acknowledgement) result packet request code (sends NEW packet)
	 * @param NackResult_RequestCode 	NACK(negative acknowledgement) result packet request code (sends same packet)
	 * @param runtimeInSeconds 	Duration of the ARQ mechanism
	 * @param folder 			The directory to store the ouput files
	 */
	public static void ARQApplication(Modem VirtualModem, String AckResult_RequestCode, String NackResult_RequestCode, int runtimeInSeconds, String folder) {		
		// Trying to create output files in order to store: ARQ Packet times && 
		//													Nack requests per ARQ for a packet
		try (FileWriter ARQPacketsTimes = new FileWriter(new File(folder + "ARQ_PacketsReceiveTimesMillis.txt"));
			 FileWriter ARQNackResultsCounterPerPacket = new FileWriter(new File(folder + "ARQ_NackResultsPerPacket.txt"))) {
						
			String[] getPacketARQResults = new String[2];
			int NackResultsCounterPerARQPacket = 0;
			String ARQPacketText = "";
			
			// ARQ packet TIMERS
			long packet_sentTime = 0; 		// Time we send the first request (Ack)	
			long packet_receivedTime = 0; 	// Time we receive the correct packet after a number of Nack requests
			
			// Requests COUNTERS
			int NackResultsFULLCounter = 0;		// Each Packet means NackResultsCounterPerARQPacket results
			int AckResultsFULLCounter = 0;		// Each packet means 1 ACK result

			
			long startCallingPacketsTime = System.currentTimeMillis(); // Time we started to ask for echo packets from the modem
			
			// Running the ARQ Application for runtimeInSeconds (sec):
			while (System.currentTimeMillis() <= startCallingPacketsTime + 1000 * runtimeInSeconds) {				
				
				// Receiving packets and calculating it's time:
				packet_sentTime = System.currentTimeMillis();		// Time we send the next packet request
				getPacketARQResults = receivePacket_StopAndWaitARQ(VirtualModem, AckResult_RequestCode, NackResult_RequestCode);
				packet_receivedTime = System.currentTimeMillis();	// Time we receive the packet (Error-FREE)
				//
				// AND:
				// Saving the results from the getPacketARQ(VirtualModem, AckResult_RequestCode, NackResult_RequestCode) into variables
				ARQPacketText = getPacketARQResults[0];
				NackResultsCounterPerARQPacket = Integer.valueOf(getPacketARQResults[1]);
				
				// Increasing ACK and NACK counters accordingly
				NackResultsFULLCounter += NackResultsCounterPerARQPacket;	
				AckResultsFULLCounter++;									

				
				// Console output:
				System.out.println("[ARQ][Packet #" + AckResultsFULLCounter + " (PC=" + getPacketPC(ARQPacketText) + ") delivery time: " + (packet_receivedTime - packet_sentTime) + " (ms)]\n");						
				
				// Files output:
				ARQNackResultsCounterPerPacket.write(NackResultsCounterPerARQPacket + "\r\n");		// Save packet's nack counter
				ARQPacketsTimes.write((packet_receivedTime - packet_sentTime) + "\r\n");			// Save packet's system response time
			}

			
//			long stopCallingPacketsTime = System.currentTimeMillis(); // Time we stopped asking for echo packets from the modem
			
			// Results console output:
			System.out.println("[ARQ][ Received " + AckResultsFULLCounter + " packets SUCCESSFULLY (Error-FREE) from Ithaki's Virtual Modem ]");
			System.out.println("\n( We had, IN TOTAL: "+ NackResultsFULLCounter + " NACK(negative acknowledgement) packet results in "
													   + AckResultsFULLCounter + " ACK(positive acknowledgement) packet results. )");
			System.out.println(".");
			System.out.println("BER (Bit Error Rate) Value = " + getBitErrorRate(AckResultsFULLCounter,NackResultsFULLCounter));
			
		} catch (Exception x) {	 // If files couldn't be created, THROW EXCEPTION
			System.out.println("Caught EXCEPTION: " + x + " ! Couldn't create files: "+ folder + "ARQPacketsTimes.txt AND"
																					  + folder + "ARQPacketsNackRequestsPerPacket.txt .");
		}

	}

	/**
	 * Stop-and-wait ARQ Protocol Algorithm.
	 * This method uses the AckResult_RequestCode to receive a new packet from Ithaki's VirtualModem.
	 * (Positive ackn. since last packet was received successfully)
	 * Then compares the packet's FCS with the packet's sequense's XOR result to determine
	 * on whether this packet is error-free or not.
	 * --> If ERRORS (SequenceXORResult != FCS): Resend the same packet using the NackResult_RequestCode and check again.
	 * --> If Error-FREE (SequenceXORResult == FCS): return the error-free packet's text String
	 * 
	 * @param VirtualModem 		Ithaki's modem we send the request to
	 * @param AckResult_RequestCode 	ACK(positive acknowledgement) result packet request code (sends NEW packet)
	 * @param NackResult_RequestCode 	NACK(negative acknowledgement) result packet request code (sends same packet)
	 * 
	 * @return ARRAY String with: [0]: The error-free packet's text String
	 * 							  [1]: (String)The number of times we re-requested the same packet due to errors in it.
	 */
	public static String[] receivePacket_StopAndWaitARQ(Modem VirtualModem, String AckResult_RequestCode, String NackResult_RequestCode) {
		
		// Asking the modem to send a new packet.
		String ReceivedPacketText = receivePacket(VirtualModem, AckResult_RequestCode);
		
		// Console output:
		System.out.println("-> Received NEW packet, with ack_request_code: " + ReceivedPacketText);
		
		int SequenceXORResult = 0, FCS = 0;
		int NackResultsCounter = 0;
		while (true) {
			SequenceXORResult = getSequenceXOR(getPacketEncryptedSequence(ReceivedPacketText));
			FCS = getFCS_INT(ReceivedPacketText);
			
			// Checking for errors in last received packet 
			if (SequenceXORResult != FCS) {
				// ERRORS FOUND!
				// Console output:
				System.out.println("---> Sequence XOR Result != FCS (ERRORS in packet). Requesting SAME packet with nack_request_code...");
				
				// Asking the modem to send the same packet again
				ReceivedPacketText = receivePacket(VirtualModem, NackResult_RequestCode);
				NackResultsCounter ++;
				
				// Console output:
				System.out.println("---> Received packet again: " + ReceivedPacketText);				
			} else {
				// ERROR FREE
				break;
			}
		}		
		// Console output:
		System.out.println("------> Error-FREE packet: " + ReceivedPacketText + " (Received after "+ NackResultsCounter + " NACK results / re-requests).");
		
		// Return the error free packet AND the number of times the modem resent the same packet due to errors
		return new String[]{ReceivedPacketText, 
							Integer.toString(NackResultsCounter)};
	
	}

	/**
	 * This method sends a request to Ithaki's VirtualModem using the 
	 * RequestCode and receives as an "answer" a String packet.
	 * 
	 * @param VirtualModem	Ithaki's modem we send the request to
	 * @param RequestCode	The request code in order to receive same or new packet
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
	 * Calculates the result of applying the XOR operator consecutively at the 16
	 * "pseudoencrypted" characters that are included in the ARQ packet.
	 * 
	 * @param Sequence The "pseudoencrypted" characters String
	 * 
	 * @return the final XOR result as an Integer
	 */
	private static int getSequenceXOR(String Sequence) {
		char XORResult = 0; // Initialise to 0 because of the identity element ( A ^ 0 = A ).
							// ( This means that any value XOR’d with zero is left unchanged )
		// So FIRSTLY XORResult = (Sequence.toCharArray())[0] ^ 0 = (Sequence.toCharArray())[0]
		// and then   XORResult = (Sequence.toCharArray())[0] ^ (Sequence.toCharArray())[1]
		// and then   XORResult = ...
		// and         ...
		for (int i = 0; i < (Sequence.toCharArray()).length; i++) {
			XORResult ^= (Sequence.toCharArray())[i];
		}

		return (int) XORResult;
	}

	/**
	 * Calculates the BER (Bit Error Rate) by using the formula:
	 * 				P1 = (1 - Pb) ^ F	(1)
	 * ,where: P1: Probability that a bit is received in error; aka the Bit Error Rate (BER)
	 * 		   Pb: Probability that a frame/sequence arrives witn no bit errors
	 * 		    F: Number of bits per frame/sequence
	 * 
	 * so (1) => P1 ^ (1/F) = 1 - Pb => 
	 * 	   		 Pb = 1 - P1 ^ (1/F) => 
	 *     		 BER = 1 - P1 ^ (1/F)
	 * @see Bibliography:
	 * Data and Computer Communications 8th Edition William Starlings -> Page 186 (6.3 ERROR DETECTION)
	 * 
	 * @param NumberOfFramesWITHOUTError The number of frames/sequences that had no bit errors
	 * @param NumberOfFramesWITHError    The number of frames/sequences that had bit error(s)
	 * 
	 * @return BER (Bit Error Rate) value
	 */
	public static double getBitErrorRate(int NumberOfFramesWITHOUTError, int NumberOfFramesWITHError) {
		double BER = 0;	// Probability that a bit is received in error.
		double P1 = 0;	// Probability that a frame/sequence arrives with no bit errors
		double F = 0;	// Number of bits per packet sequence
		int bytesPerChar = 2; 	// Char is 2 bytes in Java (Unicode)( Unlike C/C++ (1 byte in C - ASCII))
		int bitsPerByte = 8;	// Each byte is equal to 8 bits
		int bitsPerChar = bytesPerChar * bitsPerByte;
		int CharactersPerPacket = 16;
		
		F = CharactersPerPacket * bitsPerChar;
		
		P1 = (((double)NumberOfFramesWITHOUTError) / ((double)(NumberOfFramesWITHOUTError + NumberOfFramesWITHError)));
		BER = 1 - Math.pow(P1, 1.0/F);

		return BER;
	}

	/**
	 * Methods to parse the "ARQ type" packet sent by the VirtualModem
	 * 
	 * @param ARQPacket : An "ARQ type" packet sent by the VirtualModem. Example:
	 *                 "PSTART DD-MM-YYYY HH-MM-SS PC <ΧΧΧΧΧΧΧΧΧΧΧΧΧΧΧΧ> FCS PSTOP"
	 */
	// -------------------------------------------------------
	// Returns DATE (from given ex. = "DD-MM-YYYY")
//	private static String getPacketDate(String ARQPacket) { 
//		return ARQPacket.split(" ")[1];
//	}

	// Returns HOUR (from given ex. = "HH-MM-SS")
//	private static String getPacketHour(String ARQPacket) {
//		return ARQPacket.split(" ")[2];
//	}
	
	// Returns PacketCounter%100 (from given ex. = "PC")
	private static String getPacketPC(String ARQPacket) { 
		return ARQPacket.split(" ")[3];
	}

//	private static int getPacketPC_INT(String ARQPacket) { 
//		return Integer.valueOf(getPacketPC(ARQPacket));
//	}

	// Returns the pseudoencrypted 16-char sequence (from given ex. = "XXXXXXXXXXXXXXXX", without ">" & "<")
	private static String getPacketEncryptedSequence(String ARQPacket) { 
		return ARQPacket.split(" ")[4].substring(1, 17);
	}

	// Returns Frame Check Sequence (from given ex. = "FCS")
	private static String getFCS(String ARQPacket) { 
		return ARQPacket.split(" ")[5];
	}

	private static int getFCS_INT(String ARQPacket) { 
		return Integer.valueOf(getFCS(ARQPacket));
	}
	// -------------------------------------------------------

}
