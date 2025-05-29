package org.example.model;

import java.awt.Color;
import java.awt.geom.Point2D;

public class Pen{
    double penSize;
    Point2D.Double position;
    Color color;

    Pen(Point2D.Double position,Color color){
        this.position = position;
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public double getPenSize() {
        return penSize;
    }

    public Point2D.Double getPosition(){
        return position;
    }



}