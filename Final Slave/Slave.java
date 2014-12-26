import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.Sound;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTCommConnector;
import lejos.nxt.comm.NXTConnection;
import lejos.nxt.comm.RS485;

public class Slave {
	// status messages
	static final String connected = "Connected";
	static final String waiting = "Waiting...";
	static final String closing = "Closing...";

	/**
	 * 
	 * @param args
	 * @return void 
	 * Main method for slave NXT
	 */
	public static void main(String[] args) {

		ClampObject objectClamp = new ClampObject();
		// wait for Bluetooth connection
		NXTCommConnector connectors = Bluetooth.getConnector();
		LCD.drawString(waiting, 0, 0);
		NXTConnection connection = connectors.waitForConnection(0, 0);
		Motor.C.rotate(190);
		// Display that Bluetooth is connected
		LCD.clear();
		LCD.drawString(connected, 0, 0);

		// create IO streams (You will need to use DataInputStream and
		// DataOutputStream, see the LeJOS API)
		DataInputStream in = connection.openDataInputStream();
		DataOutputStream out = connection.openDataOutputStream();

		// Read data from input stream
		int n = 0;
		while (n != 5) {
			try {
				n = in.readInt();
				LCD.drawString("Number:" + n, 0, 1);
			} catch (IOException ioe) {

			}

			if (n == 0)
				;
			else if (n == 1) {
				objectClamp.clamp();
			} else if (n == 3)
				objectClamp.drop();
			else if (n == 2) {
				ClampObject.blocksStacked--;
				if (ClampObject.blocksStacked == 0) {
					ClampObject.clampMotor.rotate(90);

				}
			}

			try {
				out.writeInt(1);
				out.flush();
			} catch (IOException ioe) {

			}

		}// end while
			// Display closing
		LCD.clear();
		LCD.drawString(closing, 0, 0);
		try {
			in.close();
			out.close();
		} catch (IOException ioe) {
			LCD.drawString("Write Exception", 0, 5);
		}

		// close Bluetooth connection
		connection.close();
		LCD.clear();
	}

}
