import lejos.nxt.*;
import java.util.*;
import java.io.*;

/*
 * Antoine Bosselut
 * Obstacle Detection
 */

public class ObstacleDetection extends Thread {
	private UltrasonicSensor TopUS;
	private UltrasonicSensor BottomUS;
	private Motor sensorMotor = Motor.C;
	private boolean seeObject = false;
	private Navigation navigator;
	private Odometer odo;
	DataInputStream input;
	DataOutputStream output;

	public ObstacleDetection(UltrasonicSensor theTopUS,
			UltrasonicSensor theBottomUS, Odometer odometer,
			DataInputStream theInput, DataOutputStream theOutput) {
		TopUS = theTopUS;
		BottomUS = theBottomUS;
		odo = odometer;
		input = theInput;
		output = theOutput;
	}// end constructor

	/**
	 * @return void 
	 * run method (required for Thread)
	 */
	public void run() {
		int distance = 255;
		boolean floorChange = false;
		// Set initial conditions

		while (!seeObject) {
			if (GridTraveller.runThread) {
				BottomUS.ping();
				try {
					Thread.sleep(30);
				} catch (InterruptedException e) {

				}
				distance = BottomUS.getDistance();
				// Ping the bottom sensor once

				// If there is an object where the ping was recorded and the
				// robot has not stacked three blocks yet,
				// Do the condition.
				if (Master.blocksStacked < 3 && isObject(distance, BottomUS)) {

					double yLocationGrid = distance
							* Math.cos((odo.getTheta())) + odo.getY();
					double xLocationGrid = distance
							* Math.sin((odo.getTheta())) + odo.getX();
					// Record the location of the object on the grid

					if ((((yLocationGrid > 85) && (yLocationGrid < 95)) || ((yLocationGrid > 205) && (yLocationGrid < 215)))
							&& ((Math.abs(odo.getTheta() - 90) > 5) || (Math
									.abs(odo.getTheta() - 270) > 5))) {
						floorChange = true;
					}// end if
					if (((xLocationGrid > 85) && (xLocationGrid < 95))
							|| ((xLocationGrid > 205) && (xLocationGrid < 215))
							&& ((Math.abs(odo.getTheta() - 180) > 5) || (Math
									.abs(odo.getTheta()) > 5))) {
						floorChange = true;
					}// end if
						// If the object's location is at a junction of the
						// floor and the robot is travelling perpendicular to
						// this junction, the robot will ignore it. If the
						// object is a styrofoam block, the robot will pick it
						// up
						// once it has pushed it off the grid floor. if it is
						// the result of an uneven junction of the grid floor,
						// the robot will successfully ignore it.

					// If there is no change in the gridfloor, do the condition
					if (!floorChange) {
						int n;
						try {
							output.writeInt(1);
							output.flush();
						} catch (IOException e) {

						}// end catch

						try {
							n = input.readInt();
						} catch (IOException ioe) {

						}// end catch
						Master.blocksStacked++;
						// Tell the slave to pick up the block. Wait for a reply
						// from the slave to do anything else

						BottomUS.ping();
						try {
							Thread.sleep(30);
						} catch (InterruptedException e1) {

						}
						if (BottomUS.getDistance() < 13) {
							try {
								output.writeInt(2);
								output.flush();
								Master.blocksStacked--;
							} catch (IOException e) {

							}// end catch
							try {
								n = input.readInt();
							} catch (IOException ioe) {

							}// end catch
						}// end if
							// If the sensor notices an object where the robot
							// just picked up the block, it indicates that
							// the slave was unsuccessful in picking up the
							// block. Tell the slave to decrement its
							// blocksStacked
							// counter

					}// end if
					Motor.A.forward();
					Motor.B.forward();
					// Resume travelling
				}// end if
			}// end if
		}// end while
		Sound.beepSequence();
	}// end run()

	/**
	 * 
	 * @param distance
	 * @param sensor
	 * @return boolean 
	 * Method to look for objects
	 */
	public boolean isObject(int distance, UltrasonicSensor sensor) {
		// If the distance originally recorded by the sensor is less than 15, do
		// the condition
		if (distance < (15)) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e1) {

			}

			Motor.A.stop();
			Motor.B.stop();
			// Stop the robot's movement

			int[] distances = new int[20];
			int count = 0;
			for (int i = 0; i < distances.length; i++) {
				sensor.ping();
				try {
					Thread.sleep(30);
				} catch (InterruptedException e) {

				}
				distances[i] = sensor.getDistance();
				if ((distances[i] < 13))
					count++;

			}// end
				// Ping the bottom sensor 20 times to assert that there is a
				// block where the initially pinged.
				// Counts the number of times the sensor pings a distance less
				// than 13.

			if (count > 5)
				return true;
			// if the count is greater than 5, return true, indicating an object
			// is seen

		}// end if
		Motor.A.forward();
		Motor.B.forward();
		return false;
		// If no object is seen, start forward movement again and return false

	}// end isObject

}// end ObstacleDetection
