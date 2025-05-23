package org.example.model;

import java.awt.Color;
import java.awt.geom.Point2D;

public class SpurGear extends SpiroGear {
    public double speed;
    public double theta;
    public double alpha;

    public SpurGear(double speed, double theta, double alpha) {
        super(new Point2D.Double(), 0, Color.BLACK);
        this.speed = speed;
        this.theta = theta;
        this.alpha = alpha;
    }

    public void move(int deltatime) {
        // 移動処理（具体的な動作は未定）
    }

    public void setPosition(Point2D.Double position) {
        this.position = position;
    }

    public void changeSpeed(double speed) {
        this.speed = speed;
    }

}
