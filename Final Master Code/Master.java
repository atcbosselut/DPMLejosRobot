import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.nxt.*;
import lejos.nxt.comm.*;
import lejos.util.Delay;

/*
 * Antoine Bosselut
 * Irtaza Rizvi
 * Felix Le Dem
 * 
 * Master Class
 */

public class Master {

	// Static variables
	public static int blocksStacked = 0;

	/**
	 * 
	 * @param args
	 * @return void 
	 * The master class initializes all the threads and variables
	 *         that are necessary for the completion of the user requirements
	 */
	public static void main(String[] args) {
		int coordinates[] = PCtoNXTslave.getCoordinates();
		// Call the PCtoNXTslave to acquire the drop coordinates

		String slaveName = "R2-D2";
		NXTCommConnector connector = Bluetooth.getConnector();
		NXTConnection con = connector.connect(slaveName, 0);
		// Make connection with the slave

		DataInputStream input = con.openDataInputStream();
		DataOutputStream output = con.openDataOutputStream();
		// Set up input/output data streams with the slave

		UltrasonicSensor bottomUS = new UltrasonicSensor(SensorPort.S1);
		UltrasonicSensor topUS = new UltrasonicSensor(SensorPort.S2);
		LightSensor leftLS = new LightSensor(SensorPort.S3);
		LightSensor rightLS = new LightSensor(SensorPort.S4);
		// Initialize Ultrasonic and Light Sensors

		Odometer odometer = new Odometer();
		Navigation nav = new Navigation(odometer);
		ObstacleDetection OD = new ObstacleDetection(topUS, bottomUS, odometer,
				input, output);
		// Create project threads

		odometer.start();
		// Start odometer

		int j = 0;
		// Set up counter

		for (int i = 0; i < 30; i++) {
			bottomUS.ping();
			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {

			}// end catch
			if (bottomUS.getDistance() < 45)
				j++;
		}// end for
		if (j > 10) {
			nav.turnTo(180);
			odometer.setTheta(0);
		}// end if
			// Ping the counter 30 times and count the number of times it sees
			// an obstacle with in 45 cm of it. Because there is a
			// 3-square free space, the counter should not be incremented if the
			// robot is not facing a wall. if the count is greater
			// than 10 after having pinged 30 times, it is assumed to be
			// initially facing a wall .If the robot starts off
			// facing a wall, rotate it 180 degrees.

		USLocalizer USL = new USLocalizer(odometer, topUS,
				USLocalizer.LocalizationType.FALLING_EDGE);
		USL.doLocalization();
		LightLocalizer LL = new LightLocalizer(odometer, leftLS, rightLS, nav);
		LL.doLightLocalization();
		// The robot localizes itself so that it's starting position is (0,0).

		GridTraveller gridTraveller = new GridTraveller(odometer, nav, leftLS,
				rightLS, bottomUS, topUS, OD);
		// Create GridTraveller, the tool for navigation

		gridTraveller.travelTo(60, 60);
		// Travel to (60,60)

		gridTraveller.travelTo(coordinates[0], coordinates[1]);
		// Travel to the drop coordinates

		Motor.A.backward();
		Motor.B.backward();
		Motor.A.setSpeed(200);
		Motor.B.setSpeed(200);
		while (Math.abs(odometer.getX() - coordinates[0]) < 10
				&& Math.abs(odometer.getY() - coordinates[1]) < 10)
			;
		Motor.A.stop();
		Motor.B.stop();
		// Once the robot's wheels have reached the drop coordinates, move
		// backwards 10 cm so that the clamp is
		// directly above the drop coordinates

		try {
			output.writeInt(3);
			output.flush();
		} catch (IOException e) {
		}// end catch
		int n;
		try {
			n = input.readInt();
		} catch (IOException ioe) {

		}// end catch
			// Send a signal to drop the blocks to the slave and wait for a
			// reply before doing anything else.

		Motor.A.backward();
		Motor.B.backward();
		Motor.A.setSpeed(200);
		Motor.B.setSpeed(200);
		for (int i = 0; i < 200000; i++)
			;
		Motor.A.stop();
		Motor.B.stop();
		// Move backwards until the robot is clear from the stack.

	}// end main
}// end class

