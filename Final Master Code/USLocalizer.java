import lejos.nxt.*;

/*
 * Antoine Bosselut
 * Ultrasonic Localization
 */

public class USLocalizer {
	public enum LocalizationType {
		FALLING_EDGE, RISING_EDGE
	};

	public static double ROTATION_SPEED = 30;
	public static final int DISTANCE_FROM_WALL = 45;
	public static final int NOISE_ERROR = 2;

	private Odometer odo;
	private TwoWheeledRobot robot = new TwoWheeledRobot(Motor.A, Motor.B);
	private UltrasonicSensor us;
	private LocalizationType locType;
	private boolean[] update = { true, true, true };

	public USLocalizer(Odometer odo, UltrasonicSensor us,
			LocalizationType locType) {
		this.odo = odo;
		this.us = us;
		this.locType = locType;

		// switch off the ultrasonic sensor
		us.off();
	}// end constructor

	/**
	 * @return void 
	 * Do us localization
	 */
	public void doLocalization() {
		double[] pos = new double[3];
		double angleA, angleB;
		boolean found = false;
		int distance, previousDistance;

		// If a FALLING_EDGE localization is needed, do the condition. Will
		// Always be FALLING_EDGE
		if (locType == LocalizationType.FALLING_EDGE) {

			robot.setForwardSpeed(0);
			boolean change = false;
			robot.setRotationSpeed(ROTATION_SPEED);
			previousDistance = getFilteredData();
			distance = getFilteredData();
			// Turn clockwise

			while (change == false) {
				if (distance > (DISTANCE_FROM_WALL + 2)
						&& previousDistance > (DISTANCE_FROM_WALL - 2))
					change = true;
				previousDistance = distance;
				distance = getFilteredData();
			}// end while
			Sound.beep();
			odo.setTheta(0);
			// rotate the robot until it sees no wall

			change = false;
			while (change == false) {
				if (distance < (DISTANCE_FROM_WALL - 2)
						&& previousDistance > (DISTANCE_FROM_WALL + 2))
					change = true;
				previousDistance = distance;
				distance = getFilteredData();
			}// end while
				// keep rotating until the robot sees a wall

			Sound.beep();
			odo.getPosition(pos, update);
			angleA = pos[2];
			// Set the angle to the position when it first sees wall

			robot.setRotationSpeed(-ROTATION_SPEED);
			// Turn counterclockwise

			change = false;
			while (change == false) {
				if (distance > (DISTANCE_FROM_WALL + 2)
						&& previousDistance > (DISTANCE_FROM_WALL - 2))
					change = true;
				previousDistance = distance;
				distance = getFilteredData();
			}// end while
				// switch direction and wait until it sees no wall. Once it sees
				// no wall, the next instance of it seeing
				// a wall will indicate it has rotated to the left wall.

			Sound.beep();

			change = false;
			while (change == false) {
				if (distance < (DISTANCE_FROM_WALL - 2)
						&& previousDistance < (DISTANCE_FROM_WALL + 2))
					change = true;
				previousDistance = distance;
				distance = getFilteredData();
			}// end while
				// keep rotating until the robot sees a wall

			Sound.beep();
			odo.getPosition(pos, update);
			angleB = pos[2];
			// Get current angle (Second fringe angle)

			if (angleB > 180)
				angleB = 360 - angleB;

			double theta = 0.0;
			odo.getPosition(pos, update);
			theta = 45 - (angleA + angleB) / 2;
			// angleA is clockwise from angleB, so assume the average of the
			// angles to the right of angleB is 45 degrees past 'north'

			odo.setPosition(new double[] { 0.0, 0.0, theta }, new boolean[] {
					true, true, true });
			// update the odometer position

		}// end if
	}// end doLocalization

	/**
	 * 
	 * @return int 
	 * Get filtered data from us sensor
	 */
	private int getFilteredData() {
		int distance;
		us.ping();
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
		}// end catch
		distance = us.getDistance();
		return distance;
		// ping and return the distance
	}// end getFilteredData()

}// end USLocalizer.java