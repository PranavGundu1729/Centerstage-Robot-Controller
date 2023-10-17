package org.firstinspires.ftc.teamcode.tests.roadrunner.headingprimitives;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.roadrunner.MecanumDrive;
public final class TangentHeading extends LinearOpMode{

    @Override
    public void runOpMode() throws InterruptedException {
        MecanumDrive drive = new MecanumDrive(hardwareMap, new Pose2d(0, 0, 0));

        waitForStart();

        Actions.runBlocking(
                drive.actionBuilder(drive.pose)
                        .setTangent(0)
                        .splineTo(new Vector2d(48, 48), Math.PI / 2)
                        .build());
    }
}


