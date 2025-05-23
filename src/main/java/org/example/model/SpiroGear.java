package org.example.model;

import java.awt.Color;
import java.awt.geom.Point2D;

public class SpiroGear {
    public Point2D.Double position;
    public double radius;
    public Color color;

    public SpiroGear(Point2D.Double position, double radius, Color color) {
        this.position = position;
        this.radius = radius;
        this.color = color;
    }

    public void changeRadius(double radius) {
        this.radius = radius;
    }

    public void changeColor(Color color) {
        this.color = color;
    }
}
