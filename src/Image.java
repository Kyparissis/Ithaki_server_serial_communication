//package src;

// ----------------------------------
// 			 LIBRARIES
// ----------------------------------
import ithakimodem.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;

/**
 * @apiNote This class contains methods in order to receive/create a .jpeg/.jpg image frame, from
 * the videoCoder hosted at http://ithaki.eng.auth.gr/netlab/video.html (and provides live
 * feed from a road near the School Of Engineering of Aristotle University Of Thessaloniki),
 * through Ithaki's Virtual Modem.
 */
public class Image {

	/**
	 * This method sends a request to the VirtualModem using the given
	 * ImageRequestCode and then uses a ByteArrayOutputStream to save the image's
	 * data sent SERIALLY by the VirtualModem.
	 * 
	 * @param VirtualModem     Ithaki's modem we send the request to
	 * @param ImageRequestCode The request code in order to receive an image
	 * 
	 * @return The image's binary file's data in a byte array, from start of image
	 *         delimiter to end of image delimiter (including the delimiters 4 bytes)
	 */
	public static byte[] getImageData(Modem VirtualModem, String ImageRequestCode) {
		ByteArrayOutputStream ImageDataBuffer = new ByteArrayOutputStream();
		byte[] ImageData = null;

		// Sending request to the virtual modem using the request code given
		boolean VirtualModemResponse = VirtualModem.write((ImageRequestCode + "\r").getBytes());
		if (VirtualModemResponse) {
			// Getting virtual modem's "answer" to the request
			int VirtualModemOutput;

			// We receive the image data from the modem until we find the image's file
			// breaking flag
			// Since the modem sends a .jpg/.jpeg image, we use JPG_BreakingFlagIsFound()
			// method to check if we need to stop receiving image data
			while (!JPG_BreakingFlagIsFound(ImageDataBuffer)) {
				VirtualModemOutput = VirtualModem.read();
				ImageDataBuffer.write((byte) VirtualModemOutput);
			}

			ImageData = ImageDataBuffer.toByteArray();
		}

		return ImageData;
	}

	/**
	 * This method generates an image file at the PC's directory:
	 * DirectoryAndFileNameAndExtension given the image's data ImageData
	 * 
	 * @param ImageData                        the image's binary file's data in a byte array                                           
	 * @param DirectoryAndFileNameAndExtension directory to save the image file in the PC/filename.extension                                          
	 * 
	 * @return TRUE if image generated successfully, 
	 * 		   FALSE if an error occurred or the image is not generated successfully
	 */
	public static boolean generateImage(byte[] ImageData, String DirectoryAndFileNameAndExtension) {
		// Check if ImageData was generated SUCCESSFULLY
		if (!(ImageData == null)) {
			// Create the file at the location given by the user
			File ImageFile = new File(DirectoryAndFileNameAndExtension);

			// Use a FileOutputStream to write the image data to the image file
			try (FileOutputStream ImageStream = new FileOutputStream(ImageFile)) {
				ImageStream.write(ImageData);
			} catch (Exception x) {
				// Console output:
				System.out.println("Caught EXCEPTION: " + x + " ! Couldn't generate " + DirectoryAndFileNameAndExtension);
			}
			
			// Console output:
			// (CONFIRMATION MESSAGE)
			System.out.println();
			System.out.println();
			System.out.println("[IMAGE][ SUCCESSFULLY generated image: " + DirectoryAndFileNameAndExtension + " ]");
			System.out.println();
			System.out.println();

			return true;
		} else {
			// Console output:
			System.out.println("[IMAGE][ Couldn't generate .jpg image! No file data? ]");

			return false;
		}
	}

	/**
	 * This method scans the Image's data ByteArrayOutputStream to find it's data
	 * breaking flag Breaking flags are used to determine an image's type.
	 * In a JPEG/JPG Image: The binary file STARTS with the bytes 0xFF 0xD8 (start of image delimiter)
	 * 								 	    & ENDS with the bytes 0xFF 0xD9 (end of image delimiter).
	 * 
	 * @param ImageDataBuffer Image's current data ByteArrayOutputStream
	 * 
	 * @return TRUE if JPG/JPEG's breaking flag is found at the end of the ImageDataBuffer, 
	 * 		   FALSE else
	 */
	private static boolean JPG_BreakingFlagIsFound(ByteArrayOutputStream ImageDataBuffer) {		
		// We check if the ImageDataBuffer.toByteArray() has data and if it has more
		// than 4 bytes, since 4 bytes are used for the start of image delimiter 
		// and the end of image delimiter
		if (ImageDataBuffer.toByteArray().length > 4) {

			// We check the second to last byte of the array for the byte: 0xFF
			// We check the last byte of the array for the byte: 0xD9
			if ((ImageDataBuffer.toByteArray()[ImageDataBuffer.toByteArray().length - 2] == ((byte) (0xFF)))
					&& (ImageDataBuffer.toByteArray()[ImageDataBuffer.toByteArray().length - 1] == ((byte) (0xD9)))) {
				return true;	// The end of image delimiter is  found at the end of the buffer!!

			} else
				return false;	// The end of image delimiter is NOT found at the end of the buffer
		} else
			return false; // Not enough bytes in the buffer to determine if image is sent
	}

}
