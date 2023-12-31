package org.firstinspires.ftc.teamcode.competition.qualifiers.teleop;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

@TeleOp
public class QualifiersTeleop extends LinearOpMode {
    int pixelsPossesed = 0;
    int slideLevelOne = 0;
    int slideLevelTwo = 0;
    int slideLevelThree = 0;

    int slideLevelGround = 0;

    int hangUp = 0;
    int liftUp = 0;

    float slideKi = 0;
    float slideKp = 0;
    float slideKd = 0;
    float slideReference = 0;

    double IMPERFECT_STRAFING = 1.1;


    double currentYaw = 0;
    double currentPitch = 0;
    double currentRoll = 0;

    double distanceToRamp = 0;
    double distanceToPixel = 0;

    double pixelSensorDistance = 0;




    DcMotor frontLeftMotor = hardwareMap.dcMotor.get("frontLeftMotor");
    DcMotor backLeftMotor = hardwareMap.dcMotor.get("backLeftMotor");
    DcMotor frontRightMotor = hardwareMap.dcMotor.get("frontRightMotor");
    DcMotor backRightMotor = hardwareMap.dcMotor.get("backRightMotor");
    DcMotor intakeMotor = hardwareMap.dcMotor.get("intakeMotor");
    DcMotor leftSlideMotor = hardwareMap.dcMotor.get("leftSlideMotor");
    DcMotor rightSlideMotor = hardwareMap.dcMotor.get("rightSlideMotor");

    DcMotor hangMotor = hardwareMap.dcMotor.get("hangMotor");

    Servo droneLauncher = hardwareMap.servo.get("droneLauncher");

    IMU imu = hardwareMap.get(IMU.class, "imu");
    IMU.Parameters parameters = new IMU.Parameters(new RevHubOrientationOnRobot(
            RevHubOrientationOnRobot.LogoFacingDirection.UP,
            RevHubOrientationOnRobot.UsbFacingDirection.FORWARD));

    private DistanceSensor pixelSensor;

    @Override
    public void runOpMode() throws InterruptedException {

        pixelSensor = hardwareMap.get(DistanceSensor.class, "pixelSensor");

        imu.initialize(parameters);

        frontRightMotor.setDirection(DcMotorSimple.Direction.FORWARD);
        backRightMotor.setDirection(DcMotorSimple.Direction.FORWARD);
        frontLeftMotor.setDirection(DcMotorSimple.Direction.FORWARD);
        backLeftMotor.setDirection(DcMotorSimple.Direction.FORWARD);

        leftSlideMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightSlideMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        leftSlideMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightSlideMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        hangMotor.setDirection(DcMotorSimple.Direction.FORWARD);
        hangMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        hangMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);



        waitForStart();

        if (isStopRequested()) return;

        while (opModeIsActive()) {
            if (pixelsPossesed <= 2){
                if (gamepad1.right_trigger > 0){
                    intakeMotor.setPower(gamepad1.right_trigger);
                }
            }

            if (gamepad1.dpad_left){
                pixelsPossesed = 1;
            }

            if (gamepad1.left_bumper){
                moveMotorTicks(hangUp, hangMotor, 0.75);
            }

            if (gamepad1.right_bumper){
                moveMotorTicks(liftUp, hangMotor, -1);
            }

            if (gamepad1.dpad_right){
                pixelsPossesed = 2;
            }

            if (gamepad1.dpad_up){
                pixelsPossesed = 0;
            }

            if (gamepad2.a){
                moveSlideToGround();
            }

            if (gamepad2.x){
                moveSlideToLevelOne();
            }

            if (gamepad2.b){
                moveSlideToLevelTwo();
            }

            if (gamepad2.y){
                moveSlideToLevelThree();
            }

            if (gamepad1.options) {
                imu.resetYaw();
            }



            double y = -gamepad1.left_stick_y;
            double x = gamepad1.left_stick_x;
            double rx = gamepad1.right_stick_x;

            double botHeading = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS);

            double rotX = x * Math.cos(-botHeading) - y * Math.sin(-botHeading);
            double rotY = x * Math.sin(-botHeading) + y * Math.cos(-botHeading);

            rotX = rotX * IMPERFECT_STRAFING;

            double denominator = Math.max(Math.abs(rotY) + Math.abs(rotX) + Math.abs(rx), 1);
            double frontLeftPower = (rotY + rotX + rx) / denominator;
            double backLeftPower = (rotY - rotX + rx) / denominator;
            double frontRightPower = (rotY - rotX - rx) / denominator;
            double backRightPower = (rotY + rotX - rx) / denominator;

            frontLeftMotor.setPower(frontLeftPower);
            backLeftMotor.setPower(backLeftPower);
            frontRightMotor.setPower(frontRightPower);
            backRightMotor.setPower(backRightPower);


        }
    }
    public void moveMotorTicks(int ticks, DcMotor motor, double power){
        motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motor.setTargetPosition(ticks);
        motor.setPower(power);
        motor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        while (motor.isBusy()){
        }
    }

    public void shootDrone(){

    }
    public void moveSlideToPositionPID(DcMotor leftSlideMotor, DcMotor rightSlideMotor, float ticks, float Kp, float Ki, float Kd, float reference){
        double integralSum = 0;
        float lastError = 0;

        ElapsedTime timer = new ElapsedTime();

        while (leftSlideMotor.getCurrentPosition() != ticks) {
            float encoderPosition = leftSlideMotor.getCurrentPosition();
            float error = reference - encoderPosition;
            float derivative = (float) ((error - lastError) / timer.seconds());

            integralSum = integralSum + (error * timer.seconds());

            double out = (Kp * error) + (Ki * integralSum) + (Kd * derivative);

            leftSlideMotor.setPower(out);
            rightSlideMotor.setPower(out);

            lastError = error;

            timer.reset();

            if (pixelsPossesed <= 2){
                if (gamepad1.right_trigger > 0){
                    intakeMotor.setPower(gamepad1.right_trigger);
                }
            }

            if (gamepad1.left_bumper){
                moveMotorTicks(hangUp, hangMotor, 0.75);
            }

            if (gamepad1.right_bumper){
                moveMotorTicks(liftUp, hangMotor, -1);
            }

            if (gamepad1.dpad_left){
                pixelsPossesed = 1;
            }

            if (gamepad1.dpad_right){
                pixelsPossesed = 2;
            }

            if (gamepad1.dpad_up){
                pixelsPossesed = 0;
            }


            if (gamepad1.options) {
                imu.resetYaw();
            }



            double y = -gamepad1.left_stick_y;
            double x = gamepad1.left_stick_x;
            double rx = gamepad1.right_stick_x;

            double botHeading = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS);

            double rotX = x * Math.cos(-botHeading) - y * Math.sin(-botHeading);
            double rotY = x * Math.sin(-botHeading) + y * Math.cos(-botHeading);

            rotX = rotX * IMPERFECT_STRAFING;

            double denominator = Math.max(Math.abs(rotY) + Math.abs(rotX) + Math.abs(rx), 1);
            double frontLeftPower = (rotY + rotX + rx) / denominator;
            double backLeftPower = (rotY - rotX + rx) / denominator;
            double frontRightPower = (rotY - rotX - rx) / denominator;
            double backRightPower = (rotY + rotX - rx) / denominator;

            frontLeftMotor.setPower(frontLeftPower);
            backLeftMotor.setPower(backLeftPower);
            frontRightMotor.setPower(frontRightPower);
            backRightMotor.setPower(backRightPower);

        }


    }

    public void moveSlideToLevelOne(){
        moveSlideToPositionPID(leftSlideMotor, rightSlideMotor, slideLevelOne, slideKp, slideKi, slideKd, slideReference);
    }
    public void moveSlideToLevelTwo(){
        moveSlideToPositionPID(leftSlideMotor, rightSlideMotor, slideLevelTwo, slideKp, slideKi, slideKd, slideReference);
    }
    public void moveSlideToLevelThree(){
        moveSlideToPositionPID(leftSlideMotor, rightSlideMotor, slideLevelThree, slideKp, slideKi, slideKd, slideReference);
    }

    public void moveSlideToGround(){
        moveSlideToPositionPID(leftSlideMotor, rightSlideMotor, slideLevelGround, slideKp, slideKi, slideKd, slideReference);
    }

    private class FtcDashboard {
    }
}