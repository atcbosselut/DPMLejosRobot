import lejos.nxt.Motor;
import lejos.nxt.comm.RConsole;
import lejos.util.*;

public class ClampObject {

	private static final Motor pulleyMotor = Motor.A;
	public static final Motor clampMotor = Motor.B;
	public static final Motor sweeperMotor = Motor.C;
	public static int blocksStacked = 0;

	/**
	 * @return void 
	 * Clamp object
	 */
	public static void clamp() {
		// The clamp starts at its high point with the clamp as open as it can
		// be. First the NXT calls the sweepers to
		// straighten the block. Then the clamp closes a bit to be able to avoid
		// hitting other parts of the robot on the
		// way down. Then the puller lowers the clamp to the level of the block.
		// Then the clamp closes. Then the pulley
		// raises both the clamp and the block to a level right above the
		// sweepers.
		if (blocksStacked == 0) {
			sweeperMotorMotion();
			clampMotor.setSpeed(20);
			clampMotor.rotate(-40);
			pulleyMotor.setSpeed(225);
			pulleyMotor.rotate(660);
			clampMotor.setSpeed(500);
			clampMotor.backward();

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {

			}
			// clampMotor.lock(50);
			pulleyMotor.rotate(-575);

		}// end if

		// The clamp starts right above the sweepers and is holding one or
		// multiple blocks. First, the NXT calls the sweepers
		// to straighten out the block underneath and make sure it is in a
		// position where the clamp would be able to pick
		// it up. The the clamp drops to a level right above the block it must
		// pick up. It open, laying the blocks it is holding,
		// onto the block. Then the puller drops the clamp to the level of the
		// first block. Then, the clamp closes. Then the pulley
		// raises the bottom block (and any others on top by default) to a level
		// right above the sweepers.
		else {
			sweeperMotorMotion();
			clampMotor.setSpeed(200);
			pulleyMotor.rotate(210);
			clampMotor.rotate(50);
			pulleyMotor.rotate(350);
			clampMotor.setSpeed(500);
			clampMotor.backward();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {

			}
			pulleyMotor.rotate(-565);
		}// end else
		sweeperMotor.rotate(190);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {

		}
		blocksStacked++;
	}// main

	/**
	 * @return void 
	 * Drop object
	 */
	public static void drop() {
		sweeperMotorMotion();
		pulleyMotor.rotate(565);
		Delay.msDelay(1500);
		clampMotor.rotate(40);
		Delay.msDelay(500);
		pulleyMotor.rotate(-565);
	}// end drop

	/**
	 * @return void 
	 * Clamp object Starts the sweepers that straighten the block
	 *         out. Three separate sweeping motions take place to ensure that
	 *         the block is perfectly straightened out and in the direction it
	 *         needs to be facing.
	 */
	public static void sweeperMotorMotion() {
		sweeperMotor.setSpeed(400);
		sweeperMotor.rotate(-190);
		sweeperMotor.rotate(290);
		sweeperMotor.rotate(-100);
		sweeperMotor.rotate(100);
		sweeperMotor.rotate(-100);
		sweeperMotor.rotate(100);
		sweeperMotor.rotate(-290);
	}// end sweeperMotorMotion

}
