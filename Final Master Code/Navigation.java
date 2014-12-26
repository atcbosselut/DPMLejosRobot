/* 
 * Irtaza Rizvi 
 * Navigation
 */
import lejos.nxt.*;

public class Navigation {
	private final double tolerance;
	private Odometer odometer;
	private boolean turning;
	private boolean travelling;
	private final int frowardSpeed;
	private final int turnSpeed;
	private final Motor leftMotor = Motor.A, rightMotor = Motor.B;

	// Constructor for Navigation class
	public Navigation(Odometer odometer) {
		this.odometer = odometer;
		this.turning = false;
		this.travelling = false;
		this.tolerance = 5;
		this.frowardSpeed = 200;
		this.turnSpeed = 200;
		this.leftMotor.smoothAcceleration(true);
		this.rightMotor.smoothAcceleration(true);
	}// end construction

	/**
	 * 
	 * @param x
	 * @param y
	 * @return void 
	 * Method that takes waypoints and travels to them
	 */
	public void travelTo(double x, double y) {
		// Set travelling boolean to true to indicate method is alive
		travelling = true;

		double theta = 0;
		double forwardError = 0;
		double current_X;
		double current_Y;
		double delta_X;
		double delta_Y;
		boolean moveForward = true;

		// Keep moving the robot forward until it reaches the waypoint
		while (moveForward) {
			// Get current x and y coordinates
			current_X = odometer.getX();
			current_Y = odometer.getY();

			// Use the waypoints to calculate delta x and delta y
			delta_X = x - current_X;
			delta_Y = y - current_Y;

			// Calculate theta, the angle of the waypoint according to my
			// odometer
			if (delta_X < 0) {
				theta = Math.PI * (3.0 / 2.0) - Math.atan(delta_Y / delta_X);
			} else if (delta_X > 0) {
				theta = (Math.PI / 2.0) - Math.atan(delta_Y / delta_X);
			}// end else if

			// Convert radians to degrees
			theta = Math.toDegrees(theta);

			// Call the turn to method to turn the robot in the theta direction
			turnTo(theta);

			// Calculate forward error
			forwardError = (delta_X * delta_X) + (delta_Y * delta_Y);

			// Move the robot forward by rotating both tires at same speed
			leftMotor.forward();
			rightMotor.forward();
			leftMotor.setSpeed(frowardSpeed);
			rightMotor.setSpeed(frowardSpeed);

			// Set boolean moveForward to false if the forward error is less
			// than 1cm. This means robot has reached the waypoint.
			moveForward = forwardError > 1;
		}// end while

		// Stop the motors
		leftMotor.stop();
		rightMotor.stop();

		// Set travelling boolean to false to indicate method is dead
		travelling = false;
	}// end travelTo

	/**
	 * 
	 * @param theta
	 * @return void 
	 * Method to turn the robot in the theta direction
	 */
	public void turnTo(double theta) {
		// Set turning boolean to true to indicate method is alive
		turning = true;
		if (theta < 0) {
			theta = theta + 360;
		}
		if (theta >= 360)
			theta = theta - 360;
		double currentTheta = odometer.getTheta();
		double errorTheta = 0;

		// Calculate error in angle using the theta of the waypoint and the
		// current angle
		if (-180.0 < theta - currentTheta && theta - currentTheta < 180.0) {
			errorTheta = theta - currentTheta;
		}// end if

		// If error is less than -180 degrees then add 360 degrees so the robot
		// has to take a small turn
		else if (theta - currentTheta < -180.0) {
			errorTheta = (theta - currentTheta) + 2 * 180.0;
		}// end else if

		// If error is more than 180 degrees then subtract 360 degrees so the
		// robot has to take a small turn
		else if (theta - currentTheta > 180.0) {
			errorTheta = (theta - currentTheta) - 2 * 180.0;
		}// end else if

		// If error is positive turn right
		if (errorTheta > 0) {
			// Keep turning till error in angle is less than tolerance
			while (Math.abs(theta - odometer.getTheta()) >= tolerance) {
				// Turn left tire forward and right backward to turn right
				leftMotor.forward();
				rightMotor.backward();
				leftMotor.setSpeed(turnSpeed);
				rightMotor.setSpeed(turnSpeed);
			}// end while

			// Stop motors
			leftMotor.stop();
			rightMotor.stop();

			// Set turning boolean to false to indicate method is dead
			turning = false;
		}// end if

		// If error is negative turn left
		else {
			// Keep turning till error in angle is less than tolerance
			while (Math.abs(theta - odometer.getTheta()) >= tolerance) {
				// Turn left tire backward and right forward to turn left
				rightMotor.forward();
				leftMotor.backward();
				leftMotor.setSpeed(turnSpeed);
				rightMotor.setSpeed(turnSpeed);
			}// end while

			// Stop motors
			leftMotor.stop();
			rightMotor.stop();

			// Set turning boolean to false to indicate method is dead
			turning = false;
		}// end else
	}// end turnTo

	/**
	 * 
	 * @return boolean 
	 * Method to check if a thread has called either turnTo or
	 *         travelTo method
	 */

	public boolean isNavigating() {
		return (travelling || turning);
	}// end isNavigating()
}
