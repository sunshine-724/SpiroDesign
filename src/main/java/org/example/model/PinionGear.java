package org.example.model;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D;

class PinionGear extends SpiroGear {

    //status
    private Double speed;
    private Double theta;
    private Double alpha;

    private Pen pen;

    public PinionGear() {
        // Constructor logic here
        this.pen = new Pen(new Point2D.Double(0, 0), null); // Initialize with default position and color
    }

    public void move(Integer deltaTime){

    }

    public Pen getPen() {
        return pen;
    }

    public void setPosition(Point2D.Double position) {
        this.position = position;
    }

    public Point2D.Double getPinionPosition() {
        // Assuming pinion gear position is calculated based on some logic
        // For now, returning a default position
        return new Point2D.Double(100, 0);
    }

    public void changeSpeed(Double speed) {
        this.speed = speed;
    }
}