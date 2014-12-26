/*  
 * Irtaza Rizvi 
 * Odometer
 */
import lejos.nxt.*;

public class Odometer extends Thread {
	// robot position
	private double x, y, theta;

	// odometer update period, in ms
	private static final long ODOMETER_PERIOD = 25;

	// lock object for mutual exclusion
	private Object lock;

	// default constructor
	public Odometer() {
		x = 0.0;
		y = 0.0;
		theta = 0.0;
		lock = new Object();
	}// end constructor

	/**
	 * @return void 
	 * run method (required for Thread)
	 */
	public void run() {
		long updateStart, updateEnd;
		double radiusLeft = 2.65;
		double radiusRight = 2.65;
		double wheelDistance = 25.1;
		double finalTachoLeft, finalTachoRight;
		double initialTachoLeft = 0, initialTachoRight = 0;
		double deltaTachoLeft, deltaTachoRight;
		double delta_C, delta_Theta;

		Motor.A.resetTachoCount();
		Motor.B.resetTachoCount();
		// Reset Tacho Count

		while (true) {
			updateStart = System.currentTimeMillis();

			finalTachoLeft = (Motor.A.getTachoCount() * Math.PI / 180);
			finalTachoRight = (Motor.B.getTachoCount() * Math.PI / 180);
			// Get new tacho counts in radians

			deltaTachoLeft = finalTachoLeft - initialTachoLeft;
			deltaTachoRight = finalTachoRight - initialTachoRight;
			// Calculate the change in tacho count

			initialTachoLeft = finalTachoLeft;
			initialTachoRight = finalTachoRight;
			// Set final tacho value as the inital tacho value for future
			// calculation

			delta_C = (deltaTachoRight * radiusRight + deltaTachoLeft
					* radiusLeft) / 2;
			delta_Theta = (deltaTachoLeft * radiusLeft - deltaTachoRight
					* radiusRight)
					/ wheelDistance;

			synchronized (lock) {
				x += delta_C
						* Math.sin(Math.toRadians(theta) + (delta_Theta / 2));

				y += delta_C
						* Math.cos(Math.toRadians(theta) + (delta_Theta / 2));

				theta += Math.toDegrees(delta_Theta);

				// Make theta positive in counterclockwise direction
				if (theta < 0) {
					theta += 360;
				}// end if

				// Keep theta less the 360 degrees
				if (theta > 360) {
					theta %= 360;
				}// end if
			}// end synchronization

			/*
			 * The Odometer records by how many tachos the left and right motors
			 * have rotated through and subtracts the previous tacho count from
			 * this value to get the number of tachos by which the orientation
			 * of the wheels has changed. From these TachoChange values, the
			 * Odometer calculates by how much the direction angle of the robot
			 * has changed and the distance (delta_C) the robot has travelled.
			 * Using these distance and direction change values, the Odometer
			 * calculates the current coordinates of the robot based on the path
			 * it has travelled, assuming it began at (0,0).
			 */

			// this ensures that the odometer only runs once every period
			updateEnd = System.currentTimeMillis();
			if (updateEnd - updateStart < ODOMETER_PERIOD) {
				try {
					Thread.sleep(ODOMETER_PERIOD - (updateEnd - updateStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometer will be interrupted by
					// another thread
				}// end catch
			}// end if
		}// end while
	}// end run()

	/**
	 * 
	 * @param position
	 * @param update
	 * @return void 
	 * accessors
	 */
	public void getPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				position[0] = x;
			if (update[1])
				position[1] = y;
			if (update[2])
				position[2] = theta;
		}// end synchronization
	}// end getPosition()

	/**
	 * 
	 * @return double 
	 * accessors
	 */
	public double getX() {
		double result;
		synchronized (lock) {
			result = x;
		}// end synchronization
		return result;
	}// end getX()

	/**
	 * 
	 * @return double 
	 * accessors
	 */
	public double getY() {
		double result;
		synchronized (lock) {
			result = y;
		}// end synchronization
		return result;
	}// end getY()

	/**
	 * 
	 * @return double 
	 * accessors
	 */
	public double getTheta() {
		double result;
		synchronized (lock) {
			result = theta;
		}// end synchronization
		return result;
	}// end getTheta

	/**
	 * 
	 * @param position
	 * @param update
	 * @return void 
	 * mutators
	 */
	public void setPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				x = position[0];
			if (update[1])
				y = position[1];
			if (update[2])
				theta = position[2];
		}// end synchronization
	}// end setPosition()

	/**
	 * 
	 * @param x
	 * @return void 
	 * mutators
	 */
	public void setX(double x) {
		synchronized (lock) {
			this.x = x;
		}// end synchronization
	}// end setX

	/**
	 * 
	 * @param y
	 * @return void 
	 * mutators
	 */
	public void setY(double y) {
		synchronized (lock) {
			this.y = y;
		}// end synchronization
	}// end setY

	/**
	 * 
	 * @param theta
	 * @return void 
	 * mutators
	 */
	public void setTheta(double theta) {
		synchronized (lock) {
			this.theta = theta;
		}// end synchronization
	}// end setTheta
}// end Odometer