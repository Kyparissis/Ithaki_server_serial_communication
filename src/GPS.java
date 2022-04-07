//package src;

import ithakimodem.*;

// ----------------------------------
// 			 LIBRARIES
// ----------------------------------	
import java.util.ArrayList;
import java.lang.Math;


/**
 * @apiNote	This class contains methods in order to receive and analyse Global 
 * Positioning System (GPS) packets, following the NMEA serial Protocol. 
 * This protocol anticipates a number of GPS sentences that carry position,
 * speed, time, satelite identity etc. in the form of ASCII characters.
 */
public class GPS {
	
	/**
	 * This method sends a request to Ithaki's VirtualModem using the GPSRequestCode
	 * in order to receive GPS (NMEA Protocol) packets and stores the latitude and
	 * longitude of those packets in lists. Then analyses those lists in order to
	 * find 4 map points with distance of at least 4'' betweeen the each other.
	 * At last, it creates a "T=" parameter for those map points and returns it
	 * 
	 * @param VirtualModem		Ithaki's modem we send the request to
	 * @param GPSRequestCode	The request code in order to receive GPS packets
	 * 
	 * @return The "T=" parameter (FULL_TParameter) for the chosen 4 map points
	 */
	public static String GPSApplication(Modem VirtualModem, String GPSRequestCode) {
		// Sending request to the virtual modem.
		boolean VirtualModemResponse = VirtualModem.write((GPSRequestCode + "\r").getBytes());
		// Getting virtual modem's "answer" to the request.
		if (VirtualModemResponse) {
			String VirtualModemOutput = "";
			String NMEAProtocol_GPS_FullString = "";
			
			// GPS Points LISTS (1 Holding it's Latitude, the other it's Longitude)(SAME INDEX)
			ArrayList<String> AllLatitudeCoordinates = new ArrayList<String>();
			ArrayList<String> AllLongitudeCoordinates = new ArrayList<String>();
			
			// Before sending the GPS Packets, Ithaki sends the message "START ITHAKI GPS TRACKING\r"
			// and at the end of the GPS packet stream, sends the message "STOP ITHAKI GPS TRACKING\r"
			//
			// We scan packets until we meet the end message.
			while (!VirtualModemOutput.endsWith("STOP ITHAKI GPS TRACKING\r")) {
				
				NMEAProtocol_GPS_FullString = getNEXTOutputLine(VirtualModem);	// Read next line (Empty or Packet)
				
				VirtualModemOutput += NMEAProtocol_GPS_FullString;		// Save the line to our FULL OUTPUT String var.
				
				// ERROR HANDLING (In order to receive packets, Ithaki has to first send the "START ITHAKI GPS TRACKING\r")
				if ((VirtualModemOutput == "") || !(VirtualModemOutput.startsWith("START ITHAKI GPS TRACKING\r"))) {
					// Console output:
					System.out.println("Code ERROR or end of connection...");
					
					return "";	// Return an EMPTY FULL_TParameter
				}
				
				// READ the lines starting with the GGA protocol header ("$GPGGA").
				// SKIP empty lines and then parse the GPS packet received
				if (NMEAProtocol_GPS_FullString.startsWith("$GPGGA")) {
					// Save GPS Packet's Latitude and Longitude to our Points Lists
					AllLatitudeCoordinates.add(getLatitude(NMEAProtocol_GPS_FullString));
					AllLongitudeCoordinates.add(getLongitude(NMEAProtocol_GPS_FullString));
					
					// Console output:
					System.out.println("-> Received GPS Packet (NMEA Protocol): " + NMEAProtocol_GPS_FullString);
				}
			}
			
			// DISTANCE CHOSEN GPS Points LISTS (1 Holding it's Latitude, the other it's Longitude)(SAME INDEX)
			int NumberOfCoordsToSave = 4;
			ArrayList<String> ChosenCoordsLIST_LATITUDE = new ArrayList<String>();
			ArrayList<String> ChosenCoordsLIST_LONGITUDE = new ArrayList<String>();
			
			String FULL_TParameter = "";
			
			System.out.println("(Analysing GPS packets' points to find ones with distance at least 4'' between them...)");
			// Loop from Point #0 to Point #MAX and then find a second Point with distance greater or equal than 4'' to the first one
			for (int i = 0; i < AllLatitudeCoordinates.size() && ChosenCoordsLIST_LATITUDE.size() < NumberOfCoordsToSave; i++) {
				for (int j = i; j < AllLatitudeCoordinates.size() && ChosenCoordsLIST_LATITUDE.size() < NumberOfCoordsToSave; j++) {
					// Check if the distance of Point #1 (i) and Point #2 (j) is equal or greater than 4''[ = DMS2DecRadians(0, 0, 4) ]
					if (distanceOfPointsRadians(
							// Point #1 Decimal-Degrees Coordinates
							// LATITUDE
							DDM2DecDegrees(getLatitudeDegreesINT(AllLatitudeCoordinates.get(i)),
									   getLatitudeMinutesDOUBLE(AllLatitudeCoordinates.get(i))),
							// LONGITUDE
							DDM2DecDegrees(getLongitudeDegreesINT(AllLongitudeCoordinates.get(i)),
									   getLongitudeMinutesDOUBLE(AllLongitudeCoordinates.get(i))),
							
							
							// Point #2 Decimal-Degrees Coordinates
							// LATITUDE 
							DDM2DecDegrees(getLatitudeDegreesINT(AllLatitudeCoordinates.get(j)),
									   getLatitudeMinutesDOUBLE(AllLatitudeCoordinates.get(j))),
							// LONGITUDE
							DDM2DecDegrees(getLongitudeDegreesINT(AllLongitudeCoordinates.get(j)),
									   getLongitudeMinutesDOUBLE(AllLongitudeCoordinates.get(j)))) >= DMS2DecRadians(0, 0, 4)) {						
						// If distance check true:
						// Checks if the two points already exist in the "chosen" list due to a previous measurement 
						if ((!ChosenCoordsLIST_LATITUDE.contains(AllLatitudeCoordinates.get(i)))   && 
							(!ChosenCoordsLIST_LONGITUDE.contains(AllLongitudeCoordinates.get(i))) && 
							(!ChosenCoordsLIST_LATITUDE.contains(AllLatitudeCoordinates.get(j)))   && 
							(!ChosenCoordsLIST_LONGITUDE.contains(AllLongitudeCoordinates.get(j)))) {
								// If unique points true:
								// Save Point #1 Latitude and Longitude
								ChosenCoordsLIST_LATITUDE.add(AllLatitudeCoordinates.get(i));
								ChosenCoordsLIST_LONGITUDE.add(AllLongitudeCoordinates.get(i));
								System.out.println("[GPS] Point found: LATITUDE: " + AllLatitudeCoordinates.get(i) + " , LONGITUDE: " + AllLongitudeCoordinates.get(i));
				
								// Save Point #2 Latitude and Longitude
								ChosenCoordsLIST_LATITUDE.add(AllLatitudeCoordinates.get(j));
								ChosenCoordsLIST_LONGITUDE.add(AllLongitudeCoordinates.get(j));
								System.out.println("[GPS] Point found: LATITUDE: " + AllLatitudeCoordinates.get(j) + " , LONGITUDE: " + AllLongitudeCoordinates.get(j));
						}
						i = j - 1; // If we find a valid distance between Point i and Point j we continue
								   // searching more Points after Point j ( j - 1 because loop does j++ )
						break;
					}
				}
			}
			
			// Generate the T= parameter for each point we have chosen and put them together
			// ex "T=123456789123T=456789012345"
			// where T=123456789123 : generateT_Parameter() for Point #1
			//		 T=456789012345 : generateT_Parameter() for Point #2
			//       ...
			// (Ithaki can get a max of 9 "T=.."	 parameters after the gps request code so i < 9)
			for (int i = 0; i < ChosenCoordsLIST_LATITUDE.size() && i < 9; i++) {
				FULL_TParameter += generateT_Parameter(ChosenCoordsLIST_LATITUDE.get(i), ChosenCoordsLIST_LONGITUDE.get(i));
			}
			
			return FULL_TParameter; 
			
		} else
			
			return "";	// Return an EMPTY FULL_TParameter in case of no response from the Virtual Modem
	}
	
	/**
	 * Reads the VirtualModem's output and return the next line it returns
	 * (Reads until "\n" is returned from the modem)
	 * 
	 * @param VirtualModem
	 * 
	 * @return Next line from the VirtualModem's output
	 */
	public static String getNEXTOutputLine(Modem VirtualModem) {
		String SingleLineText = "";
		char returnCharModem = ' ';

		while (true) {
			returnCharModem = (char) VirtualModem.read();
			if (returnCharModem != '\n')
				SingleLineText += returnCharModem;
			else
				break;
		}
		return SingleLineText;
	}
	
	/**
	 * Generates a "T=AABBCCDDEEZZ" request parameter.
	 * This parameter allows the display of a GPS point, with Longitude AA(Degrees) BB' CC'' (DMS Form) and
	 * Latitude DD(Degrees) EE' ZZ'' (DMS Form), in a .jpeg image from Google Maps sent to the user through
	 * Ithaki's Virtual Modem.
	 * 
	 * DMS: Degrees Minutes Seconds
	 * DDM: Degrees Decimal-Minutes
	 * 
	 * @param NMEAProtocol_GPS_String_LATITUDE The GPS point's Latitude in DDM Form (NMEA Protocol)
	 * @param NMEAProtocol_GPS_String_LONGITUDE The GPS point's Longitude in DDM Form (NMEA Protocol)
	 * 
	 * @return the "T=AABBCCDDEEZZ" parameter of the given point's coordinates
	 */
	private static String generateT_Parameter(String NMEAProtocol_GPS_String_LATITUDE, String NMEAProtocol_GPS_String_LONGITUDE) {
		String AA = getLongitudeDeg(NMEAProtocol_GPS_String_LONGITUDE).substring(1, 3);
		String BB = getLongitudeMin(NMEAProtocol_GPS_String_LONGITUDE).substring(0, 2);
		String CC = Integer.toString((int)(Double.valueOf("0." + getLongitudeMin(NMEAProtocol_GPS_String_LONGITUDE).substring(3, 7)) * 60));
		String DD = getLatitudeDeg(NMEAProtocol_GPS_String_LATITUDE);
		String EE = getLatitudeMin(NMEAProtocol_GPS_String_LATITUDE).substring(0, 2);
		String ZZ = Integer.toString((int)(Double.valueOf("0." + getLatitudeMin(NMEAProtocol_GPS_String_LATITUDE).substring(3, 7)) * 60));

		return "T=" + AA + BB + CC + DD + EE + ZZ;
	}
	
//	/**
//	 * CONVERT:
//	 * Degrees Minutes Seconds Coordinates form --> Decimal-Degrees Coordinates form
//	 * 
//	 * @param Degrees
//	 * @param Minutes
//	 * @param Seconds
//	 * 
//	 * @return Decimal Degrees
//	 */
//	private static double DMS2DecDegrees(int Degrees, int Minutes, int Seconds) {
//		return ((double) Degrees + (((double) Minutes) / 60.0) + (((double) Seconds) / 3600.0));
//	}
	
	/**
	 * CONVERT:
	 * Degrees Minutes Seconds Coordinates form --> Decimal-Radians Coordinates form
	 * 
	 * @param Degrees
	 * @param Minutes
	 * @param Seconds
	 * 
	 * @return Decimal Radians
	 */
	private static double DMS2DecRadians(int Degrees, int Minutes, int Seconds) {
		return Math.toRadians((double) Degrees + (((double) Minutes) / 60.0) + (((double) Seconds) / 3600.0));
	}
	
	/**
	 * CONVERT:
	 * Degrees Decimal-Minutes Coordinates form --> Decimal-Degrees Coordinates form
	 * 
	 * @param Degrees
	 * @param DecMinutes Decimal Minutes
	 * 
	 * @return Decimal Degrees
	 */
	private static double DDM2DecDegrees(int Degrees, double DecMinutes) {
		return ((double) Degrees + (DecMinutes / 60));
	}
	
//	/**
//	 * CONVERT:
//	 * Degrees Decimal-Minutes Coordinates form --> Decimal-Radians Coordinates form
//	 * 
//	 * @param Degrees
//	 * @param DecMinutes Decimal Minutes
//	 * 
//	 * @return Decimal Radians
//	 */
//	private static double DDM2DecRadians(int Degrees, double DecMinutes) {
//		return Math.toRadians((double) Degrees + (DecMinutes / 60));
//	}

	/**
	 * Uses the 'HAVERSINE' formula to calculate the absolute angular distance 
	 * between two points – that is, the shortest distance over the earth’s
	 * surface - In RADIANS.
	 * 
	 * This formula is for calculations on the basis of a spherical earth
	 * (ignoring ellipsoidal effects) – which is accurate enough for most purposes…
	 * [In fact, the earth is very slightly ellipsoidal; using a spherical model
	 * gives errors typically up to 0.3%]
	 * 
	 * @param latPoint1 The FIRST point's LATITUDE in a Decimal Degrees form
	 * @param lonPoint1 The FIRST point's LONGITUDE in a Decimal Degrees form
	 * @param latPoint2 The SECOND point's LATITUDE in a Decimal Degrees form
	 * @param lonPoint2 The SECOND point's LONGITUDE in a Decimal Degrees form
	 * 
	 * @return
	 */
	private static double distanceOfPointsRadians(double latPoint1, double lonPoint1, double latPoint2, double lonPoint2) {
		// Difference of latitudes and longitudes
		double dLat = Math.toRadians(latPoint2 - latPoint1);
		double dLon = Math.toRadians(lonPoint2 - lonPoint1);

		// Convert Latitudes from DEGREES to RADIANS
		latPoint1 = Math.toRadians(latPoint1);
		latPoint2 = Math.toRadians(latPoint2);

		// Apply the HAVERSINE formula
		double a = Math.pow(Math.sin(dLat / 2), 2) + Math.pow(Math.sin(dLon / 2), 2) * Math.cos(latPoint1) * Math.cos(latPoint2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)); 
		
		return Math.abs(c);
	}
	
	
	/**
	 * Methods to parse the NMEA Protocol GPS packets sent by the VirtualModem.
	 * 
	 * SiRF NMEA Reference Manual: http://ithaki.eng.auth.gr/netlab/sirf-nmea-reference-manual.pdf
	 * 
	 * @param NMEAProtocol_GPS_FullString : A NMEA GPS packet sent by the VirtuaModem. Example:
	 *                        "$GPGGA,045208.000,4037.6331,N,02257.5633,E,1,07,1.5,57.8,M,36.1,M,,0000*6D"
	 */
	// -----------------------------------------------------------
//	// Returns HEADER (from given ex. = "$GPGGA")
//	private static String getHeader(String NMEAProtocol_GPS_FullString) {
//		return (NMEAProtocol_GPS_FullString.split(","))[0];
//	}
	
//	// Returns UTC-TIME (from given ex. = "045208.000")
//	private static String getUTCTime(String NMEAProtocol_GPS_FullString) {
//		return (NMEAProtocol_GPS_FullString.split(","))[1];
//	}

	// Returns LATITUDE - DDM (Degrees Decimal Minutes) (from given ex. = "4037.6331")
	private static String getLatitude(String NMEAProtocol_GPS_FullString) { 
		return (NMEAProtocol_GPS_FullString.split(","))[2];
	}
	
	// Returns Latitude DEGREES (from given ex. = "40")
	private static String getLatitudeDeg(String NMEAProtocol_GPS_String_LATITUDE) {
		return NMEAProtocol_GPS_String_LATITUDE.substring(0, 2);
	}
	
	private static int getLatitudeDegreesINT(String NMEAProtocol_GPS_String_LATITUDE) { 
		return Integer.valueOf(getLatitudeDeg(NMEAProtocol_GPS_String_LATITUDE));
	}
	
	// Returns Latitude Decimal MINUTES (from given ex. = "37.6331")
	private static String getLatitudeMin(String NMEAProtocol_GPS_String_LATITUDE) {  
		return NMEAProtocol_GPS_String_LATITUDE.substring(2, 9); 
	}

	private static double getLatitudeMinutesDOUBLE(String NMEAProtocol_GPS_String_LATITUDE) { 
		return Double.valueOf(getLatitudeMin(NMEAProtocol_GPS_String_LATITUDE)); 
	}
	
//	// Returns NORTH/SOUTH Indicator (from given ex. = "N")
//	private static String getNorthSouthIndicator(String NMEAProtocol_GPS_FullString) { 
//		return (NMEAProtocol_GPS_FullString.split(","))[3];
//	}

	// Returns LONGITUDE (from given ex. = "02257.5633")
	private static String getLongitude(String NMEAProtocol_GPS_FullString) { 
		return (NMEAProtocol_GPS_FullString.split(","))[4];
	}
	
	// Returns Longitude DEGREES (from given ex. = "022")
	private static String getLongitudeDeg(String NMEAProtocol_GPS_String_LONGITUDE) {
		return NMEAProtocol_GPS_String_LONGITUDE.substring(0, 3);
	}

	private static int getLongitudeDegreesINT(String NMEAProtocol_GPS_String_LONGITUDE) {
		return Integer.valueOf(getLongitudeDeg(NMEAProtocol_GPS_String_LONGITUDE));
	}
	
	// Returns Longitude Decimal MINUTES (from given ex. = "57.5633")
	private static String getLongitudeMin(String NMEAProtocol_GPS_String_LONGITUDE) {
		return NMEAProtocol_GPS_String_LONGITUDE.substring(3, 10);
	}

	private static double getLongitudeMinutesDOUBLE(String NMEAProtocol_GPS_String_LONGITUDE) {
		return Double.valueOf(getLongitudeMin(NMEAProtocol_GPS_String_LONGITUDE));
	}
	
//	// Returns EAST/WEST Indicator (from given ex. = "E")
//	private static String getEastWestIndicator(String NMEAProtocol_GPS_FullString) { 
//		return (NMEAProtocol_GPS_FullString.split(","))[5];
//	}
	
//	// Returns the position fix indicator (from given ex. = "1")
//	private static String getPositionFixIndicator(String NMEAProtocol_GPS_FullString) {
//		return (NMEAProtocol_GPS_FullString.split(","))[6];
//	}
	
//	// Returns the number of active satelites (from given ex. = "07" )
//	private static String getActiveSatelites(String NMEAProtocol_GPS_FullString) { 
//		return (NMEAProtocol_GPS_FullString.split(","))[7];
//	}
	
//	// ...
//	private static String getHDOP(String NMEAProtocol_GPS_FullString) {
//		return (NMEAProtocol_GPS_FullString.split(","))[8];
//	}
	
//	// ...
//	private static String getSeaLevel(String NMEAProtocol_GPS_FullString) {
//		return (NMEAProtocol_GPS_FullString.split(","))[9];
//	}
	
//	// ...
//	private static String getSeaLevelUNIT(String NMEAProtocol_GPS_FullString) {
//		return (NMEAProtocol_GPS_FullString.split(","))[10];
//	}
	
//	// ...
//	private static String getDiffRefStationID(String NMEAProtocol_GPS_FullString) {
//		return ((NMEAProtocol_GPS_FullString.split(","))[14]).split("\\*")[0];
//	}
	
//	//...
//	private static String getChecksum(String NMEAProtocol_GPS_FullString) {
//		return "*" + ((NMEAProtocol_GPS_FullString.split(","))[14]).split("\\*")[1];
//	}
	// -----------------------------------------------------------
}
