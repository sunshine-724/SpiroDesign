package org.example.model;

import java.awt.Color;
import java.awt.geom.Point2D;

public class SpiroGear {
    Point2D.Double position;
    Double radius;
    Color color;

    public void changeRadius(Double radius) {
        this.radius = radius;
    }

    public void changeColor(Color color) {
        this.color = color;
    }

    public Double getRadius() {
        return radius;
    }
}
