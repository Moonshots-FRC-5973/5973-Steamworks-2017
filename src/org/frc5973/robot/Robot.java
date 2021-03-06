/* Created Tue Jan 24 09:27:35 EST 2017 */
package org.frc5973.robot;
//RealMoonshots
/**
 * Imports the necessary libraries and classes
 * Includes Decimal and Time java classes
 * Includes Strongback classes
 * Includes WipLib classes
 * Includes TimedDriveCommand from the 5973 package
 */
import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

import org.strongback.Strongback;
import org.strongback.SwitchReactor;
import org.strongback.components.Motor;
import org.strongback.components.ui.ContinuousRange;
import org.strongback.components.ui.FlightStick;
import org.strongback.drive.TankDrive;
import org.strongback.hardware.Hardware;
import org.strongback.util.Values;

import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import org.frc5973.robot.TimedDriveCommand;

/**
 * @SupressWarning 'unused
 * @author Moonshots Team 2017
 * Sets up motors, joysticks, and other devices
 * Subclasses IterativeRobot which contains robotInit(), autonomousInit(), teleopInit(), and teleopPeriodic()
 *
 */
public class Robot extends IterativeRobot {
	
	//Declares the ports for the motors and joysticks
	private static final int JOYSTICK_PORT = 0; // in driver station
	private static final int RMOTOR_FRONT = 3;
	private static final int RMOTOR_REAR = 2;
	private static final int LMOTOR_FRONT = 0;
	private static final int LMOTOR_REAR = 1;
	
	//Declares the ports for the winch and door
	private static final int WINCH_PORT = 4;
	private static final int DOOR_PORT = 5;
	private static final int WINCH2_PORT = 6;
	
	//Declares the TankDrive reference along with the ContinuousRange objects
	private TankDrive drive;
	private ContinuousRange driveSpeed;
	private ContinuousRange turnSpeed;
	private ContinuousRange turnSpeed2;

	// We moved this up here so we can output this variable in the teleop
	protected ContinuousRange sensitivity;
	
	// Used to limit and format the number of console outputs
	private int filter = 0;
	private String pattern = "###.###";
	private DecimalFormat myFormat = new DecimalFormat(pattern);
	private double sen;

	/**
	 * @Override 
	 * Sets up the Motor objects and creates the TankDrive
	 * Records no data or events
	 * Execution period is extended so it doesn't time out
	 */
	@Override
	public void robotInit() {
		//Using the recording features of Strongback is something we can look at next year
		Strongback.configure().recordNoData().recordNoCommands().recordNoEvents()
				.useExecutionPeriod(200, TimeUnit.MILLISECONDS).initialize();
		//Sets up the two cameras, one facing forward and once facing backwards
		CameraServer.getInstance().startAutomaticCapture(0);
		CameraServer.getInstance().startAutomaticCapture(1);
		// Set up the robot hardware ...
		Motor left_front = Hardware.Motors.victorSP(LMOTOR_FRONT).invert(); // left
																			// rear
		Motor left_rear = Hardware.Motors.victorSP(LMOTOR_REAR).invert(); // left
																			// front
		// DoubleToDoubleFunction SPEED_LIMITER = Values.limiter(-0.1, 0.1);
		Motor right_front = Hardware.Motors.victorSP(RMOTOR_FRONT); // right
																	// rear
		Motor right_rear = Hardware.Motors.victorSP(RMOTOR_REAR); // right front

		Motor left = Motor.compose(left_front, left_rear);
		Motor right = Motor.compose(right_front, right_rear);

		Motor winch = Hardware.Motors.victorSP(WINCH_PORT);
		Motor winch2 = Hardware.Motors.victorSP(WINCH2_PORT);
		
		Motor winch_compose = Motor.compose(winch, winch2);
		Motor door = Hardware.Motors.victorSP(DOOR_PORT);

		drive = new TankDrive(left, right);
		// Set up the human input controls for teleoperated mode. We want to use
		// the Logitech Attack 3D's throttle as a
		// "sensitivity" input to scale the drive speed and throttle, so we'll
		// map it from it's native [-1,1] to a simple scale
		// factor of [0,1] ...
		FlightStick joystick = Hardware.HumanInterfaceDevices.logitechExtreme3D(JOYSTICK_PORT);
	
		SwitchReactor reactor = Strongback.switchReactor();
		ContinuousRange sensitivity = joystick.getThrottle().map(t -> ((t + 1.0) / 2.0));
		sensitivity = joystick.getThrottle().map(Values.mapRange(-1.0, 1.0).toRange(0.0, 1.0));
		driveSpeed = joystick.getPitch().scale(sensitivity::read).invert(); // scaled
		turnSpeed = joystick.getRoll().scale(sensitivity::read);
		turnSpeed2 = joystick.getYaw().scale(sensitivity::read);
		// scaled and
		// inverted
		reactor.onTriggered(joystick.getButton(7), () -> switchControls());

		reactor.onTriggered(joystick.getButton(3), () -> winch_compose.setSpeed(1));
		reactor.onUntriggered(joystick.getButton(3), () -> winch_compose.stop());
		// reactor.onTriggered(joystick.getButton(4), () -> winch.setSpeed(-1));
		// reactor.onUntriggered(joystick.getButton(4), () -> winch.stop());

		reactor.onTriggered(joystick.getButton(12), () -> door.setSpeed(1));
		reactor.onUntriggered(joystick.getButton(12), () -> door.stop());

		reactor.onTriggered(joystick.getButton(11), () -> door.setSpeed(-1));
		reactor.onUntriggered(joystick.getButton(11), () -> door.stop());
	}
/**
 * @Override
 * Code for autonomous mode
 * Creates three command objects and allows for correction for robot veering to left
 */
	@Override
	public void autonomousInit() {
		Strongback.start();
		TimedDriveCommand forward = new TimedDriveCommand(drive, .2, 0.0, false, 1, 1250);
		TimedDriveCommand turn_right = new TimedDriveCommand(drive, .25, .10, false, 1, 20);
		TimedDriveCommand turn_left = new TimedDriveCommand(drive, .25, -.10, false, 1, 200);
		TimedDriveCommand back = new TimedDriveCommand(drive, -.2, .0, false, 1, 400);
		
		forward.execute();
		//turn_left.execute();
		forward.execute();
		//turn_left.execute();
		forward.execute();
		//turn_left.execute();
		forward.execute();
		forward.execute();
	}
	/**
	@Override
	public void autonomousInit() {
		Strongback.start();
		TimedDriveCommand forward = new TimedDriveCommand(drive, .2, 0.0, false, 1, 1500);
		TimedDriveCommand turn_right = new TimedDriveCommand(drive, .25, .10, false, 1, 20);
		TimedDriveCommand turn_left = new TimedDriveCommand(drive, .25, -.10, false, 1, 200);
		TimedDriveCommand back = new TimedDriveCommand(drive, -.2, .0, false, 1, 400);
		
		forward.execute();
		turn_left.execute();
		forward.execute();
		turn_left.execute();
		forward.execute();
		back.execute();
	}
	**/
	/**
	 * @SuppressWarnings("deprecation")
	 * 
	 * @Override public void autonomousInit() { // Start Strongback functions
	 *           ... Strongback.start(); if
	 *           (SmartDashboard.getBoolean("DB/Button 0")) { TimedDriveCommand
	 *           forward = new TimedDriveCommand(drive, .25, 0.0, false, 1,
	 *           1000); forward.execute();
	 * 
	 *           TimedDriveCommand turn_right = new TimedDriveCommand(drive,
	 *           .25, .10, false, 1, 500); turn_right.execute();
	 *           forward.execute(); turn_right.execute(); forward.execute();
	 * 
	 *           }
	 * 
	 *           else if (SmartDashboard.getBoolean("DB/Button 1")) {
	 *           TimedDriveCommand forward = new TimedDriveCommand(drive, .25,
	 *           0.0, false, 1, 2000); forward.execute();
	 * 
	 *           TimedDriveCommand turn_right = new TimedDriveCommand(drive,
	 *           .25, .10, false, 1, 1400); turn_right.execute();
	 * 
	 *           TimedDriveCommand forward1 = new TimedDriveCommand(drive, .25,
	 *           0.0, false, 1, 750); forward1.execute();
	 * 
	 *           }
	 * 
	 *           else if (SmartDashboard.getBoolean("DB/Button 2")) {
	 *           TimedDriveCommand forward = new TimedDriveCommand(drive, .25,
	 *           0.0, false, 1, 2000); forward.execute();
	 * 
	 *           TimedDriveCommand turn_left = new TimedDriveCommand(drive, .25,
	 *           -.10, false, 1, 1400); turn_left.execute();
	 * 
	 *           TimedDriveCommand forward1 = new TimedDriveCommand(drive, .25,
	 *           0.0, false, 1, 750); forward1.execute(); }
	 **/
	/**
	 * double measure = pot.get(); System.out.println(measure);
	 **/
	/**
	 * while (true) { try { AnalogInput exampleInput = new AnalogInput(0);
	 * double volts = exampleInput.getAverageVoltage();
	 * System.out.println(volts); TimeUnit.SECONDS.wait(1); } catch
	 * (InterruptedException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); }
	 **/

	// Moves forward for certain time depending on value of analog
	/**
	 * AnalogInput exampleAnalog = new AnalogInput(0); double volts =
	 * exampleAnalog.getAverageVoltage(); if (volts >= 0 && volts <= 1) new
	 * TimedDriveCommand(drive, 0.5, 0.0, false, .5); else if (volts > 1 &&
	 * volts <= 3) new TimedDriveCommand(drive, 0.5, 0.0, false, 1); else if
	 * (volts > 3 && volts <= 5) new TimedDriveCommand(drive, 0.5, 0.0, false,
	 * 1.5);
	 **/
	
/**
 * Used to switch direction of the robot in teleop
 * Can switch left and right, but that is disabled
 */
	public void switchControls() {
		driveSpeed = driveSpeed.invert();
		// turnSpeed = turnSpeed.invert();
		// turnSpeed2 = turnSpeed2.invert();
	}

	/**
	 * @Override
	 * Method that initiates teloperated mode
	 */
	@Override
	public void teleopInit() {
		// Kill anything running if it is ...
		Strongback.disable();
		// Start Strongback functions ...
		Strongback.start();
	}

	/**
	 * @Override
	 * Reads the joystick to determine what it should do in teleop
	 */
	@Override
	public void teleopPeriodic() {
		drive.arcade(driveSpeed.read(), turnSpeed.read());

	}

	@Override
	public void disabledInit() {
		// Tell Strongback that the robot is disabled so it can flush and kill
		// commands.
		Strongback.disable();
	}

}
