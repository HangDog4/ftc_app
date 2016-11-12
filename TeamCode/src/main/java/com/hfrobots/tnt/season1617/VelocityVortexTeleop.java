/**
 Copyright (c) 2016 HF Robotics (http://www.hfrobots.com)
 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:
 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 **/

package com.hfrobots.tnt.season1617;


import com.hfrobots.tnt.corelib.state.State;
import com.hfrobots.tnt.corelib.state.ToggleState;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.Range;

/**
 * Provide a basic manual operational mode that controls the tank drive.
 */
@TeleOp(name="00-VV Teleop")
@SuppressWarnings("unused")
public class VelocityVortexTeleop extends VelocityVortexHardware

{
    protected boolean useEncoders = false;

    private State collectorToggleState;

    private State collectorReverseToggleState;

    private State particleShooterState;

    private State conveyorForwardToggleState;

    private State conveyorReverseToggleState;

    /*
     * Construct the class.
     *
     * The system calls this member when the class is instantiated.
     */
    public VelocityVortexTeleop() {
    }

    /**
     * Places the robot into the initial pre-running state.
     * Robot must fit within sizing guidelines (18x18)
     * before this state completes.
     */
    @Override
    public void init() {
        super.init();

        if (!useEncoders) {
            drive.setRunMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        } else {
            drive.setRunMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }

        collectorToggleState = new ToggleState(telemetry, collectorToggleButton) {
            @Override
            protected void toggledOn() {
                collectorMotor.setPower(1);
            }

            @Override
            protected void toggledOff() {
                collectorMotor.setPower(0);
            }
        };

        collectorReverseToggleState = new ToggleState(telemetry, collectorReverseToggleButton) {
            @Override
            protected void toggledOn() {
                collectorMotor.setPower(-1);
            }

            @Override
            protected void toggledOff() {
                collectorMotor.setPower(0);
            }
        };

        //continuous rotation, 0.5 is stop,
        // 0 is one direction, 1 the other
        conveyorForwardToggleState  = new ToggleState(telemetry, conveyorForwardToggleButton) {
            @Override
            protected void toggledOn() {
                conveyorServo.setPosition(0);
            }

            @Override
            protected void toggledOff() {
                conveyorServo.setPosition(0.5);
            }
        };

        conveyorReverseToggleState  = new ToggleState(telemetry, conveyorReverseToggleButton) {
            @Override
            protected void toggledOn() {
               conveyorServo.setPosition(1);
            }

            @Override
            protected void toggledOff() {
                conveyorServo.setPosition(0.5);
            }
        };
    }


    /**
     * Implement a state machine that controls the robot during
     * manual-operation.  The state machine uses gamepad input to transition
     * between states.
     * <p>
     * The system calls this member repeatedly while the OpMode is running.
     */
    @Override
    public void loop()

    {
        handleDrive();
        handleCollector();
        handleParticleShooter();

        updateGamepadTelemetry();
    }

    /**
     * Runs the state machine for the particle shooter
     */
    private void handleParticleShooter() {
        // TODO: Currently, not a state machine :(
        conveyorForwardToggleState.doStuffAndGetNextState();
        conveyorReverseToggleState.doStuffAndGetNextState();

        if (shooterTrigger.isPressed()) {
            topParticleShooter.setPower(1);
            bottomParticleShooter.setPower(1);
        } else {
            topParticleShooter.setPower(0);
            bottomParticleShooter.setPower(0);
        }
    }

    /**
     * Runs the state machine for the collector mechanism
     */
    private void handleCollector() {
        collectorToggleState = collectorToggleState.doStuffAndGetNextState();
        collectorReverseToggleState = collectorReverseToggleState.doStuffAndGetNextState();
    }

    private void handleDrive() {
        //----------------------------------------------------------------------
        //
        // DC Motors
        //
        // Obtain the current values of the joystick controllers.
        //
        // Note that x and y equal -1 when the joystick is pushed all of the way
        // forward and all the way right (i.e. away from the human holder's body).
        //
        // The clip method guarantees the value never exceeds the range +-1.
        //
        // The DC motors are scaled to make it easier to control them at slower
        // speeds.
        //
        // The setPower methods write the motor power values to the DcMotor
        // class, but the power levels aren't applied until the loop() method ends.
        //

        // these scale the motor power based on the amount of input on the drive stick
        float xValue = scaleMotorPower(driverLeftStickX.getPosition());
        float yValue = -scaleMotorPower(driverLeftStickY.getPosition());

        //calculate the power needed for each motor
        float leftPower = yValue + xValue;
        float rightPower = yValue - xValue;


        //clip the power values so that it only goes from -1 to 1
        leftPower = Range.clip(leftPower, -1, 1);
        rightPower = Range.clip(rightPower, -1, 1);

        //set the power of the motors with the gamepad values
        drive.drivePower(leftPower, rightPower);
    }

    private void updateGamepadTelemetry() {
        telemetry.addData ("06", "GP1 Left x: " + -driverLeftStickX.getPosition());
        telemetry.addData ("07", "GP1 Left y: " + -driverLeftStickY.getPosition());
    }
}