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

import com.hfrobots.tnt.corelib.control.DebouncedButton;
import com.hfrobots.tnt.corelib.control.NinjaGamePad;
import com.hfrobots.tnt.corelib.control.RangeInput;
import com.hfrobots.tnt.corelib.drive.DriveTrain;
import com.hfrobots.tnt.corelib.drive.DualDcMotor;
import com.hfrobots.tnt.corelib.drive.ExtendedDcMotor;
import com.hfrobots.tnt.corelib.drive.Gear;
import com.hfrobots.tnt.corelib.drive.NinjaMotor;
import com.hfrobots.tnt.corelib.drive.TankDrive;
import com.hfrobots.tnt.corelib.drive.Wheel;
import com.hfrobots.tnt.corelib.units.RotationalDirection;
import com.qualcomm.ftccommon.DbgLog;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;

public abstract class VelocityVortexHardware extends OpMode {

    // Required hardware map
    // Motor Controller 1 (AL00XQ80)
    //     Port 1 - leftDrive (encoder is in port 1)
    //     Port 2 - rightDrive (encoder is in port 2)
    //

    protected NinjaGamePad ninjaGamepad;

    protected RangeInput leftStickX;

    protected RangeInput leftStickY;

    protected DebouncedButton collectorToggleButton;

    protected ExtendedDcMotor collectorMotor;

    protected TankDrive drive;

    /**
     * Perform any actions that are necessary when the OpMode is enabled.
     * <p/>
     * The system calls this member once when the OpMode is enabled.
     */
    @Override
    public void init() {

        // Build an instance of our more advanced gamepad class

        ninjaGamepad = new NinjaGamePad(gamepad1);
        leftStickX = ninjaGamepad.getLeftStickX();
        leftStickY = ninjaGamepad.getLeftStickY();
        collectorToggleButton = new DebouncedButton(ninjaGamepad.getAButton());
        collectorMotor = NinjaMotor.asNeverest40(hardwareMap.dcMotor.get("collectorMotor"));

        ExtendedDcMotor leftMotor1 = NinjaMotor.asNeverest20(hardwareMap.dcMotor.get("leftDrive1"));
        ExtendedDcMotor leftMotor2 = NinjaMotor.asNeverest20(hardwareMap.dcMotor.get("leftDrive2"));
        ExtendedDcMotor rightMotor1 = NinjaMotor.asNeverest20(hardwareMap.dcMotor.get("rightDrive1"));
        ExtendedDcMotor rightMotor2 = NinjaMotor.asNeverest20(hardwareMap.dcMotor.get("rightDrive2"));
        DualDcMotor leftMotor = new DualDcMotor(leftMotor1, leftMotor2);
        DualDcMotor rightMotor = new DualDcMotor(rightMotor1, rightMotor2);

        Wheel stealthWheel = Wheel.andyMarkStealth();
        Gear dummyGear = new Gear(1);
        DriveTrain leftDriveTrain = new DriveTrain(stealthWheel, RotationalDirection.COUNTER_CLOCKWISE, leftMotor, new Gear[] {dummyGear, dummyGear});
        DriveTrain rightDriveTrain = new DriveTrain(stealthWheel, RotationalDirection.CLOCKWISE, rightMotor, new Gear[] {dummyGear, dummyGear});

        drive = new TankDrive(leftDriveTrain, rightDriveTrain);
    }

    /**
     * Access whether a warning has been generated.
     */
    boolean wasWarningGenerated() {
        return warningGenerated;
    }

    /**
     * Access the warning message.
     */
    String getWarningMessage()

    {
        return warningMessage;
    }

    /**
     * Mutate the warning message by ADDING the specified message to the current
     * message; set the warning indicator to true.
     * <p/>
     * A comma will be added before the specified message if the message isn't
     * empty.
     */
    void appendWarningMessage(String exceptionMessage) {
        if (warningGenerated) {
            warningMessage += ", ";
        }
        warningGenerated = true;
        warningMessage += exceptionMessage;
    }

    /**
     * Scale the joystick input using a nonlinear algorithm.
     */
    float scaleMotorPower(float unscaledPower) {

        //
        // Ensure the values are legal.
        //
        float clippedPower = Range.clip(unscaledPower, -1, 1);

        float[] scaleFactors =
                {0.00f, 0.05f, 0.09f, 0.10f, 0.12f
                        , 0.15f, 0.18f, 0.24f, 0.30f, 0.36f
                        , 0.43f, 0.50f, 0.60f, 0.72f, 0.85f
                        , 1.00f, 1.00f
                };

        // scale changes sensitivity of joy stick by multiplying input by numbers 0 to 1
        //
        // Get the corresponding index for the given unscaled power.
        //
        int scaleIndex = (int) (clippedPower * 16.0);

        if (scaleIndex < 0) {
            scaleIndex = -scaleIndex;
        } else if (scaleIndex > 16) {
            scaleIndex = 16;
        }

        final float scaledPower;

        if (clippedPower < 0) {
            scaledPower = -scaleFactors[scaleIndex];
        } else {
            scaledPower = scaleFactors[scaleIndex];
        }

        return scaledPower;
    }

    /**
     * Indicate whether a message is a available to the class user.
     */
    private boolean warningGenerated = false;

    /**
     * Store a message to the user if one has been generated.
     */
    private String warningMessage;

}
