package org.example.model;

import java.awt.Color;
import java.awt.geom.Point2D;

public class SpurGear extends SpiroGear {
    public SpurGear(Point2D.Double position, double radius, Color color) {
        super(position, radius, color);
    }

    public double getSpurRadius() {
        return radius;
    }

    public Point2D.Double getSpurPosition() {
        return position;
    }
}
