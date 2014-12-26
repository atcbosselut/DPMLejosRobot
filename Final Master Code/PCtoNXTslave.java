/*
 * Irtaza Rizvi
 * PC to NXT Communication
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.Sound;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTConnection;

public class PCtoNXTslave {
	// status messages
	static final String connected = "Connected";
	static final String waiting = "Waiting...";
	static final String closing = "Closing...";
	public static int startingCorner, goalX, goalY, startingX, startingY;

	/**
	 * 
	 * @return int[] 
	 * Get coordinates from PC for the final destination using
	 *         Bluetooth
	 */
	public static int[] getCoordinates() {

		LCD.drawString(waiting, 0, 0);
		NXTConnection connection = Bluetooth.waitForConnection();
		// Establish for Bluetooth connection

		LCD.clear();
		LCD.drawString(connected, 0, 0);
		// Display that Bluetooth is connected

		DataInputStream in = connection.openDataInputStream();
		// Create data input stream with the connection from the PC

		try {
			startingCorner = in.readInt();
			in.readChar();
			goalX = in.readInt() * 30;
			in.readChar();
			goalY = in.readInt() * 30;
		} catch (IOException e) {
			LCD.drawString("Error reading input!", 0, 2);
		}// end catch
			// Read the starting corner and final coordinates from the PC

		try {
			in.close();
		} catch (IOException e) {
			LCD.drawString("Error closing InputStream!", 0, 2);
		}// end catch
			// Close the input stream

		LCD.clear();
		LCD.drawString(closing, 0, 0);
		// Display closing

		connection.close();
		LCD.clear();
		// close Bluetooth connection

		switch (startingCorner) {
		case 1:
			startingX = 0;
			startingY = 0;
			break;

		case 2:
			startingX = 300;
			startingY = 0;
			goalX = startingX - goalX;
			break;

		case 3:
			startingX = 300;
			startingY = 300;
			goalX = startingX - goalX;
			goalY = startingY - goalY;
			break;

		case 4:
			startingX = 0;
			startingY = 300;
			goalY = startingY - goalY;
			break;

		}// end switch
			// Based on the starting corner, modify the final coordinates so
			// they are the same final location
			// if the odometer assumes its starting location to be (0,0).

		int[] coordinates = { goalX, goalY };
		return coordinates;
		// Return coordinates
	}// end getCoordinates
}// end PCtoNXTslave()
