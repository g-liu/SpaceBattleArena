package com.awesome;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import ihs.apcs.spacebattle.*;
import ihs.apcs.spacebattle.commands.*;
import ihs.apcs.spacebattle.games.KingOfTheBubbleGameInfo;
import javafx.scene.transform.Rotate;

public class TinFoil extends BasicSpaceship {
    boolean radarPerformed, rotatePerformed, thrustPerformed, backThrustPerformed, warpPerformed;

    List<Point> bubblesNearby;

    final double MIN_AVOID_DISTANCE = 10;
    final double MAX_DEFENSE_DISTANCE = 60;
    @Override
    public RegistrationData registerShip(int numImages, int worldWidth, int worldHeight)
    {
        radarPerformed = false;
        rotatePerformed = false;
        thrustPerformed = false;
        backThrustPerformed = false;
        warpPerformed = false;
        return new RegistrationData("TinFoil", new Color(117, 225, 240), 0);
    }

    @Override
    public ShipCommand getNextCommand(BasicEnvironment env) {
        ObjectStatus ship = env.getShipStatus();
        RadarResults radarResults = env.getRadar();
        List<ObjectStatus> allObjects = new ArrayList<ObjectStatus>();

        if (ship.getHealth() < 10) {
            System.out.println("repairing...");
            return new RepairCommand(50);
        }

        if (radarResults != null) {
            String[] types = new String[] { "Ship", "Planet", "Asteroid", "Torpedo" };
            List<ObjectStatus> shipsNearby = radarResults.getByType("Ship");
            List<ObjectStatus> asteroidsNearby = radarResults.getByType("Asteroid");
            List<ObjectStatus> planetsNearby = radarResults.getByType("Planet");

            // reset radar
            radarResults = null;

            bubblesNearby = ((KingOfTheBubbleGameInfo) env.getGameInfo()).getBubblePositions();

            if (planetsNearby.size() > 0) {
                ObjectStatus closestPlanet = getClosest(ship, planetsNearby);
                if (distanceBetween(ship, closestPlanet) < MIN_AVOID_DISTANCE) {
                    return new WarpCommand();
                }
            }

            // what out for ships
            if (shipsNearby.size() > 0) {
                ObjectStatus closestShip = getClosest(ship, shipsNearby);

                if (!rotatePerformed) {
                    rotatePerformed = true;
                    return takeDefensiveAction(ship, closestShip);
                } else if (distanceBetween(closestShip, ship) < MAX_DEFENSE_DISTANCE) {
                    rotatePerformed = false;
                    if (ship.getEnergy() > FireTorpedoCommand.getInitialEnergyCost()) {
                        System.out.println("Fire torpedo at ship");
                        return new FireTorpedoCommand('F');
                    }
                }
            }

            // watch out for asteroids
            if (asteroidsNearby.size() > 0) {
                ObjectStatus closestAsteroid = getClosest(ship, asteroidsNearby);

                if (!rotatePerformed) {
                    rotatePerformed = true;
                    return takeDefensiveAction(ship, closestAsteroid);
                } else if (distanceBetween(closestAsteroid, ship) < MAX_DEFENSE_DISTANCE) {
                    rotatePerformed = false;
                    if (ship.getEnergy() > FireTorpedoCommand.getInitialEnergyCost()) {
                        System.out.println("Fire torpedo at asteroid");
                        return new FireTorpedoCommand('F');
                    }
                }
            }

            // all clear, let's warp or travel
            Point closestBubble = getClosestPoint(ship, bubblesNearby);
            if (!rotatePerformed) {
                rotatePerformed = true;
                System.out.println("Going towards bubble " + closestBubble);
                warpPerformed = false;
                return rotateTowardsPoint(ship, closestBubble);
            } else {
                rotatePerformed = false;
                if (ship.getEnergy() > 2 * WarpCommand.getInitialEnergyCost() && !warpPerformed) {
                    System.out.println("Warp to closest bubble");
                    warpPerformed = true;
                    return new WarpCommand(Math.max(0, Math.min(400, ship.getPosition().getDistanceTo(closestBubble))));
                }
                else {
                    warpPerformed = false;
                    System.out.println("Thrust towards closest bubble");
                    return new ThrustCommand('B', ship.getPosition().getDistanceTo(closestBubble) / 90, 1);
                }
            }
        } else {
            System.out.println("Scanning......");
            return new RadarCommand(4);
        }
    }

    private RotateCommand rotateTowardsPoint(ObjectStatus ship, Point p) {
        double angleBetween = getAngleBetween(ship, p);
        return new RotateCommand(angleBetween);
    }

    private double getAngleBetween(ObjectStatus ship, Point p) {
        return ship.getPosition().getAngleTo(p) - ship.getOrientation();
    }

    private double getDistanceRatio(double initialDistance, Point p1, Point p2) {
        return p1.getDistanceTo(p2) / initialDistance;
    }

    private ObjectStatus getClosest(ObjectStatus ship, List<ObjectStatus> objects) {
        if (objects.size() == 0) return null;
        ObjectStatus closestObject = objects.get(0);
        for (ObjectStatus o : objects) {
            if (ship.getPosition().getDistanceTo(o.getPosition()) < ship.getPosition().getDistanceTo(closestObject.getPosition())) {
                closestObject = o;
            }
        }
        return closestObject;
    }

    private double distanceBetween(ObjectStatus o1, ObjectStatus o2) {
        return o1.getPosition().getDistanceTo(o2.getPosition());
    }

    private ShipCommand takeDefensiveAction(ObjectStatus ship, ObjectStatus closestThreat) {
        if (distanceBetween(closestThreat, ship) < MIN_AVOID_DISTANCE) {
            if (ship.getEnergy() > WarpCommand.getInitialEnergyCost()) {
                // go to random
                System.out.println("Warp to random");
                return new WarpCommand();
            } else {
                System.out.println("Raise shields due to offensive threat");
                return new RaiseShieldsCommand(10);
            }
        }
        else if (distanceBetween(closestThreat, ship) < MAX_DEFENSE_DISTANCE) {
            System.out.println("Rotate for defense against " + closestThreat.getPosition().toString());
            return rotateTowardsPoint(ship, closestThreat.getPosition());
        } else {
            // point to closest bubble
            Point closestBubble = getClosestPoint(ship, bubblesNearby);
            System.out.println("Rotate for heading towards nearest bubble " + closestBubble.toString());
            return rotateTowardsPoint(ship, closestBubble);
        }
    }

    private Point getClosestPoint(ObjectStatus ship, List<Point> points) {
        if (points.size() == 0) return null;
        Point closestPoint = points.get(0);
        for (Point p : points) {
            if (ship.getPosition().getDistanceTo(p) < ship.getPosition().getDistanceTo(closestPoint)) {
                closestPoint = p;
            }
        }
        return closestPoint;
    }
}
