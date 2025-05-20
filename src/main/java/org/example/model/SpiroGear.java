package org.example.model;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.Vector;

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

    public Vector2D.Double getPenPosition(){
        return pen.getPenPosition();
    }

    public Color getPenColor(){
        return pen.getColor();
    }
}
