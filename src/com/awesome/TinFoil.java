package com.awesome;

import java.awt.Color;

import ihs.apcs.spacebattle.*;
import ihs.apcs.spacebattle.commands.*;

public class TinFoil extends BasicSpaceship {
    private Point midpoint;

    double prevDistanceToCenter;
    double distanceToCenter;

    double maxDistance;

    private final double ANGLE_DELTA = 5.0;
    private final double STOP_DISTANCE = 200;

    @Override
    public RegistrationData registerShip(int numImages, int worldWidth, int worldHeight)
    {
        midpoint = new Point(worldWidth / 2, worldHeight / 2);

        maxDistance = (new Point(0, 0)).getDistanceTo(new Point(worldWidth, worldHeight)) / 2;

        prevDistanceToCenter = 0d;

        return new RegistrationData("TinFoil", new Color(117, 225, 240), 0);
    }

    @Override
    public ShipCommand getNextCommand(BasicEnvironment env) {
        ObjectStatus ship = env.getShipStatus();

        prevDistanceToCenter = distanceToCenter;
        distanceToCenter = ship.getPosition().getDistanceTo(midpoint);


        double degreesToCenter = ship.getPosition().getAngleTo(this.midpoint) - ship.getOrientation() % 360;

        if (degreesToCenter < -180) {
            do {
                degreesToCenter += 360;
            } while (degreesToCenter < 0);
        } else if (degreesToCenter > 180) {
            do {
                degreesToCenter -= 360;
            } while (degreesToCenter > 0);
        }

        System.out.println(distanceToCenter + "\t" + degreesToCenter);

        if (Math.abs(degreesToCenter) > ANGLE_DELTA) {
            return new RotateCommand(degreesToCenter);
        }

        if (ship.getSpeed() > 50 && distanceToCenter > STOP_DISTANCE) {
            return new IdleCommand(0.1);
        } else if (distanceToCenter <= STOP_DISTANCE && prevDistanceToCenter - distanceToCenter > 0) {
            return new ThrustCommand('F', 0.5, distanceToCenter / STOP_DISTANCE);
        } else if (distanceToCenter <= STOP_DISTANCE && prevDistanceToCenter - distanceToCenter < 0) {
            return new ThrustCommand('B', 0.5, distanceToCenter / STOP_DISTANCE);
        } else {
            return new ThrustCommand('B', 1, distanceToCenter / maxDistance);
        }
    }
}
