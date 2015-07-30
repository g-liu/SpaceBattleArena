package com.awesome;

import java.awt.Color;

import ihs.apcs.spacebattle.*;
import ihs.apcs.spacebattle.commands.*;

public class TinFoil extends BasicSpaceship {
    private Point midpoint;

    double distanceToCenter;

    double maxDistance;

    boolean alreadyRotated;
    boolean alreadyThrusted;
    boolean alreadyBackThrusted;

    double thrustAmount;

    private final double ANGLE_DELTA = 5.0;
    private final double STOP_DISTANCE = 200;

    @Override
    public RegistrationData registerShip(int numImages, int worldWidth, int worldHeight)
    {
        midpoint = new Point(worldWidth / 2, worldHeight / 2);

        alreadyRotated = false;
        alreadyThrusted = false;
        alreadyBackThrusted = false;

        maxDistance = (new Point(0, 0)).getDistanceTo(new Point(worldWidth, worldHeight)) / 2;

        thrustAmount = 1;

        return new RegistrationData("TinFoil", new Color(117, 225, 240), 0);
    }

    @Override
    public ShipCommand getNextCommand(BasicEnvironment env) {
        ObjectStatus ship = env.getShipStatus();

        distanceToCenter = ship.getPosition().getDistanceTo(midpoint);
        double degreesToCenter = ship.getPosition().getAngleTo(midpoint) - ship.getOrientation() % 360;

        if (distanceToCenter >= 300) {
            if(!alreadyRotated) {
                alreadyRotated = true;
                thrustAmount = distanceToCenter / maxDistance;
                return new RotateCommand(degreesToCenter);
            } else if (!alreadyThrusted) {
                alreadyThrusted = true;
                alreadyBackThrusted = false;
                return new ThrustCommand('B', 10, 1);
            }
        }

        if (distanceToCenter < 300 && !alreadyBackThrusted) {
            alreadyRotated = false;
            alreadyThrusted = false;
            alreadyBackThrusted = true;
            return new ThrustCommand('F', 10, 1);
        }
        return new IdleCommand(0.1);
    }
}
