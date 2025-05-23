package org.example.model;

import java.awt.geom.Point2D;

public class PinionGear extends SpiroGear {
    private Pen pen;
    public double speed;
    public double theta;
    public double alpha;

    public PinionGear(Pen pen, double speed, double theta, double alpha) {
        super(pen.getPosition(), pen.penSize, pen.color);
        this.pen = pen;
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
