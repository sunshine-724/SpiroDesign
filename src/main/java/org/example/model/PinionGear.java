package org.example.model;

import java.awt.Point;
import java.awt.geom.Point2D;

class PinionGear extends SpiroGear {

    //status
    private Double speed;
    private Double theta;
    private Double alpha;

    public PinionGear() {
        // Constructor logic here
    }

    public void move(Integer deltaTime){

    }

    public void setPosition(Point2D.Double position) {
        this.position = position;
    }

    public void changeSpeed(Double speed) {
        this.speed = speed;
    }
}