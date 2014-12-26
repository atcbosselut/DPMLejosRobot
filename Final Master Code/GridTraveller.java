/*
 * Felix Le Dem
 * Grid Traveller
 */
import lejos.nxt.*;

public class GridTraveller {
	// instance variables
	boolean lineFound;
	double heading;
	double correction;
	int count_1 = 0;
	public static boolean runThread = false;
	private LightSensor leftLS, rightLS;
	private UltrasonicSensor topUS;
	private double leftAverage, rightAverage;
	private Odometer odometer;
	private ObstacleDetection theOD;
	private final Motor leftMotor = Motor.A, rightMotor = Motor.B,
			topMotor = Motor.C;
	private Navigation nav;

	public enum direction {
		X, Y
	};

	public enum tryDirection {
		left, right, madeIt
	};

	private tryDirection trying;
	private direction dir;

	public GridTraveller(Odometer odometer, Navigation nav, LightSensor leftLS,
			LightSensor rightLS, UltrasonicSensor bottomUS,
			UltrasonicSensor topUS, ObstacleDetection OD) {
		this.odometer = odometer;
		this.nav = nav;
		this.leftAverage = 51;
		this.rightAverage = 57;
		this.leftLS = leftLS;
		this.rightLS = rightLS;
		this.topUS = topUS;
		theOD = OD;
		theOD.start();
	}// end constructor

	/**
	 * @param x
	 * @param y
	 * @return void 
	 * travel to the give points
	 */
	public void travelTo(double x, double y) {
		dir = direction.X;
		double xDistance = 0;
		double yDistance = 0;
		lineFound = false;
		correction = 0;
		heading = 0;
		double turnAngle = 0;

		boolean notArrived = Math.abs(x - odometer.getX()) > 15
				|| Math.abs(y - odometer.getY()) > 15;
		// Set arrival condition

		while (notArrived) {

			xDistance = x - odometer.getX();
			yDistance = y - odometer.getY();
			if (dir == direction.X && (Math.abs(xDistance) < 15))
				dir = direction.Y;
			if (dir == direction.Y && (Math.abs(yDistance) < 15))
				dir = direction.X;
			if (dir == direction.X) {
				if (xDistance > 0)
					turnAngle = 90;
				else
					turnAngle = 270;
			}// end if
			else {
				if (yDistance > 0)
					turnAngle = 0;
				else
					turnAngle = 180;
			}// end else
				// Move in x-direction unless the robot is on the x-gridline on
				// which it must finish. Then move in y-direction
				// unless the robot is already at its y-coordinate
			if (!((odometer.getTheta() > 355 || odometer.getTheta() < 5) && turnAngle == 0))
				nav.turnTo(turnAngle);
			// Do not turn if it caused a wrap around of theta. This condition
			// has been shown to mess up the odometry

			boolean objectDetected[] = { false, false, false, false, false };
			topMotor.resetTachoCount();
			topMotor.setSpeed(100);
			topMotor.forward();
			topMotor.rotate(90);
			for (int i = 0; i < 5; i++) {
				if (obstacleDetect()) {
					objectDetected[i] = true;
				}// end if
				switch (i) {
				case 0:
					topMotor.rotate(-60);
					break;
				case 1:
					topMotor.rotate(-30);
					break;
				case 2:
					topMotor.rotate(-30);
					break;
				case 3:
					topMotor.rotate(-60);
					break;
				default:
					break;
				}// end switch
			}// end for
			topMotor.rotate(92);
			/*
			 * Ping the ultrasonic sensor at 90, 30, 0, -30 and -90 degree
			 * angles to locate cinderblocks around the robot
			 */

			/*
			 * If there is an object located in front of the robot, do condition
			 */
			if ((objectDetected[1] || objectDetected[2] || objectDetected[3])) {

				// If there is an obstacle on the right, set the avoidance
				// direction to left
				if (trying == tryDirection.right && objectDetected[0])
					trying = tryDirection.left;
				/*
				 * if there is an obstacle on both sides. go backwards 60 cm
				 */
				if ((trying == tryDirection.left && objectDetected[4])
						|| (objectDetected[0] && objectDetected[4])) {// CORNERED!!
					double originalX = odometer.getX();
					double originalY = odometer.getY();
					heading = odometer.getTheta();

					leftMotor.backward();
					rightMotor.backward();
					leftMotor.setSpeed(200);
					rightMotor.setSpeed(200);
					if (heading <= 45 || heading >= 315) {
						while (odometer.getY() > originalY - 60)
							;
					} else if (heading >= 45 && heading <= 135) {
						while (odometer.getX() > originalX - 60)
							;
					} else if (heading >= 135 && heading <= 225) {
						while (odometer.getY() < originalY + 60)
							;
					} else if (heading >= 225 && heading <= 315) {
						while (odometer.getX() < originalX + 60)
							;
					}// end else if
					leftMotor.stop();
					rightMotor.stop();
					leftMotor.setSpeed(0);
					rightMotor.setSpeed(0);

				}// end if

				/*
				 * If there is an obstacle only on the right, turn left, and go
				 * forward one grid square
				 */
				if (objectDetected[0] || trying == tryDirection.left) {
					trying = tryDirection.left;
					nav.turnTo(odometer.getTheta() - 90);// Turn Left
				}// end if

				/*
				 * If there is no obstacle on the right, turn right and go
				 * forward one grid square
				 */
				else {
					trying = tryDirection.right;
					nav.turnTo(odometer.getTheta() + 90);// Turn Right
				}// end else

			}// end if

			leftMotor.forward();
			rightMotor.forward();
			leftMotor.setSpeed(200);
			rightMotor.setSpeed(200);
			runThread = true;
			// Set motors to go forward

			while (!lineFound) {
				if (Math.abs(leftLS.readValue() - leftAverage) > 5)
					lineFound = true;
				if (Math.abs(rightLS.readValue() - rightAverage) > 5)
					lineFound = true;
			}// end while
				// Pass the first set of gridlines
			lineFound = false;
			for (int i = 0; i < 100000; i++)
				;

			while (!lineFound) {
				if (Math.abs(leftLS.readValue() - leftAverage) > 5)
					lineFound = true;
				if (Math.abs(rightLS.readValue() - rightAverage) > 5)
					lineFound = true;
			}// end while
			/*
			 * Travel until a light sensor observes a gridline. This will mean
			 * the robot has travelled one square
			 */

			leftMotor.stop();
			rightMotor.stop();
			leftMotor.setSpeed(0);
			rightMotor.setSpeed(0);
			runThread = false;

			if ((Math.abs(leftLS.readValue() - leftAverage) > 5)
					&& (Math.abs(rightLS.readValue() - rightAverage) > 5)) {

			}// end else if
			/*
			 * If both light sensors are on the gridline, do nothing
			 */

			else if (Math.abs(leftLS.readValue() - leftAverage) > 5) {
				rightMotor.forward();
				rightMotor.setSpeed(200);
				while (Math.abs(rightLS.readValue() - rightAverage) <= 5)
					;
				rightMotor.stop();
				rightMotor.setSpeed(0);
			} // end else if
			/*
			 * If only the left sensor is on the gridline, advance the right
			 * wheel until the right sensor touches it
			 */

			else {
				leftMotor.forward();
				leftMotor.setSpeed(200);
				while (Math.abs(leftLS.readValue() - leftAverage) <= 5)
					;
				leftMotor.stop();
				leftMotor.setSpeed(0);
			}// end else
			/*
			 * If only the right sensor is on the gridline, advance the left
			 * wheel until the left sensor touches it
			 */
			heading = odometer.getTheta();
			Sound.beepSequenceUp();

			/*
			 * 4. Corrects odometry & moves its wheels back onto the grid line
			 */

			if (heading <= 45 || heading >= 315) {// if facing positive x
				odometer.setTheta(0);// correct heading
				correction = odometer.getY();
				correction = correction / 30.0;
				correction = Math.round(correction);
				odometer.setY(correction * 30 + 7.5);// corrects Y
				leftMotor.backward();
				rightMotor.backward();
				leftMotor.setSpeed(200);
				rightMotor.setSpeed(200);
				while (Math.abs(odometer.getY() - correction * 30) > 1)
					;// moves the wheels to the previous grid line
				leftMotor.stop();
				rightMotor.stop();
				leftMotor.setSpeed(0);
				rightMotor.setSpeed(0);
				if (dir == direction.Y)
					trying = tryDirection.madeIt;
			}// end else if
				// Correct the odometry and back up until the gridline that was
				// just noticed is in front of the sensors

			else if (heading > 45 && heading < 135) {
				odometer.setTheta(90);
				correction = odometer.getX();
				correction = correction / 30;
				correction = Math.round(correction);
				odometer.setX(correction * 30 + 7.5);
				leftMotor.backward();
				rightMotor.backward();
				leftMotor.setSpeed(200);
				rightMotor.setSpeed(200);
				while (Math.abs(odometer.getX() - correction * 30) > 1)
					;
				leftMotor.stop();
				rightMotor.stop();
				leftMotor.setSpeed(0);
				rightMotor.setSpeed(0);

				if (dir == direction.X)
					trying = tryDirection.madeIt;
			}// end else if
				// Correct the odometry and back up until the gridline that was
				// just noticed is in front of the sensors

			else if (heading >= 135 && heading <= 225) {
				odometer.setTheta(180);
				correction = odometer.getY();
				correction = correction / 30;
				correction = Math.round(correction);
				odometer.setY(correction * 30 - 7.5);
				leftMotor.backward();
				rightMotor.backward();
				leftMotor.setSpeed(200);
				rightMotor.setSpeed(200);
				while (Math.abs(odometer.getY() - correction * 30) > 1)
					;
				leftMotor.stop();
				rightMotor.stop();
				leftMotor.setSpeed(0);
				rightMotor.setSpeed(0);
				if (dir == direction.Y)
					trying = tryDirection.madeIt;
			}// end else if
				// Correct the odometry and back up until the gridline that was
				// just noticed is in front of the sensors

			else if (heading > 225 && heading < 315) {
				odometer.setTheta(270);
				correction = odometer.getX();
				correction = correction / 30;
				correction = Math.round(correction);
				odometer.setX(correction * 30 - 7.5);
				leftMotor.backward();
				rightMotor.backward();
				leftMotor.setSpeed(200);
				rightMotor.setSpeed(200);
				while (Math.abs(odometer.getX() - correction * 30) > 1)
					;
				leftMotor.stop();
				rightMotor.stop();
				leftMotor.setSpeed(0);
				rightMotor.setSpeed(0);
				if (dir == direction.X)
					trying = tryDirection.madeIt;
			}// end else if
				// Correct the odometry and back up until the gridline that was
				// just noticed is in front of the sensors

			lineFound = false;

			notArrived = Math.abs(x - odometer.getX()) > 15
					|| Math.abs(y - odometer.getY()) > 15;
			/*
			 * Check if robot has arrived at the final destination
			 */

		}// end while
	}// end travelTo

	/**
	 * @return boolean 
	 * If the ultrasonic sees an obstacle within 50 cm at least
	 *         5 times in the direction it is looking while it pings 30 times,
	 *         assume there is an obstacle there
	 */
	private boolean obstacleDetect() {
		int count = 0;
		for (int i = 0; i < 15; i++) {
			topUS.ping();
			if (topUS.getDistance() < 50)
				count++;
			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {

			}// end catch
		}// end for
		if (count >= 5) {
			Sound.beep();
			return true;
		}// end if
		return false;
	}// end obstacleDetect

}// end class

