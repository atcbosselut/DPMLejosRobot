import lejos.nxt.*;

/*
 * Felix Le Dem
 * Light Localization
 */
public class LightLocalizer {

	// Instance variables
	private Odometer odo;
	private LightSensor leftLS;
	private LightSensor rightLS;
	private TwoWheeledRobot robot;
	private Navigation nav;
	private final double leftAverage = 51;
	private final double rightAverage = 57;

	public LightLocalizer(Odometer odo, LightSensor leftLS,
			LightSensor rightLS, Navigation nav) {

		this.odo = odo;
		this.leftLS = leftLS;
		this.rightLS = rightLS;
		TwoWheeledRobot twoWheeledRobot = new TwoWheeledRobot(Motor.A, Motor.B);
		this.robot = twoWheeledRobot;
		this.nav = nav;
	}// end constructor

	/**
	 * @return void 
	 * Perform Light Localization
	 */
	public void doLightLocalization() {
		nav.turnTo(0);
		// After ultrasonic localization, turn to heading 0. It should be
		// roughly the y-axis

		robot.setForwardSpeed(5);
		rightLS.readValue();
		boolean lineFound = false;

		while (!lineFound) {
			if (Math.abs(leftLS.readValue() - leftAverage) > 5)
				lineFound = true;
			if (Math.abs(rightLS.readValue() - rightAverage) > 5)
				lineFound = true;
		}// end else
			// Move forward until one of the light sensors observes a gridline

		robot.setForwardSpeed(0); // Stop
		Sound.beep();
		if ((Math.abs(leftLS.readValue() - leftAverage) > 5)
				&& (Math.abs(rightLS.readValue() - rightAverage) > 5))
			;
		// If both light sensors are on the grid line, do nothing.

		else if (Math.abs(leftLS.readValue() - leftAverage) > 5) {
			Motor.B.forward();
			Motor.B.setSpeed(100);
			while (Math.abs(rightLS.readValue() - rightAverage) <= 5)
				;
			Sound.beep();
			Motor.B.setSpeed(0);
		}// end else
			// if the left light sensor observes a gridline, but not the right
			// one, move the right light sensor
			// forward until it observes the gridline. At this point, both
			// sensors should be on the gridline.

		else {
			Motor.A.forward();
			Motor.A.setSpeed(100);
			while (Math.abs(leftLS.readValue() - leftAverage) <= 5)
				;
			Sound.beep();
			Motor.A.setSpeed(0);
		}// end else
			// if the right light sensor observes a gridline, but not the left
			// one, move the left light sensor
			// forward until it observes the gridline. At this point, both
			// sensors should be on the gridline.
		lineFound = false;
		odo.setTheta(0);
		odo.setY(7.5);
		/*
		 * At this point, the robot is parallel to the y-axis and the light
		 * sensors are exactly on the black gridline. Thus, the theta can be set
		 * to 0 and the y-position can be set to 7.5 cm, as the light sensors
		 * are placed 7.5 cm from the wheel axis.
		 */

		robot.setForwardSpeed(-5);
		while (odo.getY() > .5)
			;
		robot.setForwardSpeed(0);
		/*
		 * In order to assure that the light sensor localization in the
		 * x-direction makes the robot finish at the origin, the robot backs up
		 * until the wheel axis is on the gridline. Therefore, when the robot
		 * rotates 90 degrees, it will be perpendicular to the y-axis at y = 0,
		 * and parallel to the x-axis.
		 */

		nav.turnTo(90);
		// Turn the robot to be facing the x-direction

		robot.setForwardSpeed(5);

		while (!lineFound) {
			if (Math.abs(leftLS.readValue() - leftAverage) > 5)
				lineFound = true;
			if (Math.abs(rightLS.readValue() - rightAverage) > 5)
				lineFound = true;
		}// end while
			// Move forward until one of the light sensors observes a gridline

		robot.setForwardSpeed(0); // Stop
		Sound.beep();
		if ((Math.abs(leftLS.readValue() - leftAverage) > 5)
				&& (Math.abs(rightLS.readValue() - rightAverage) > 5))
			;
		// If both light sensors are on the grid line, do nothing.

		else if (Math.abs(leftLS.readValue() - leftAverage) > 5) {
			Motor.B.forward();
			Motor.B.setSpeed(100);
			while (Math.abs(rightLS.readValue() - rightAverage) <= 5)
				;
			Sound.beep();
			Motor.B.setSpeed(0);
		}// end else
			// if the left light sensor observes a gridline, but not the right
			// one, move the right light sensor
			// forward until it observes the gridline. At this point, both
			// sensors should be on the gridline.

		else {
			Motor.A.forward();
			Motor.A.setSpeed(100);
			while (Math.abs(leftLS.readValue() - leftAverage) <= 5)
				;
			Sound.beep();
			Motor.A.setSpeed(0);
		}// end else
			// if the right light sensor observes a gridline, but not the left
			// one, move the left light sensor
			// forward until it observes the gridline. At this point, both
			// sensors should be on the gridline.

		lineFound = false;
		odo.setX(7.5);
		/*
		 * At this point, the robot is parallel to the x-axis and the light
		 * sensors are exactly on the black gridline. Thus, the theta can be set
		 * to 90 and the x-position can be set to 7.5 cm, as the light sensors
		 * are placed 7.5 cm from the wheel axis. The robot is at roughly (7.5,
		 * 0).
		 */

		robot.setForwardSpeed(-5);
		while (odo.getX() > .5)
			;
		robot.setForwardSpeed(0);
		odo.setTheta(90);
		// Move the robot backwards to the origin and set theta to 90 degrees as
		// the robot is in the x-direction.

	}
}
