package org.example.model;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D;

public class PinionGear extends SpiroGear {
    private Pen pen;
    public double speed;
    public double theta;
    public double alpha;

    // **デフォルト値**
    private static final Point2D.Double DEFAULT_POSITION = new Point2D.Double(470, 400);
    private static final double DEFAULT_RADIUS = 30.0;
    private static final Color DEFAULT_COLOR = Color.BLACK;

    // **デフォルトコンストラクタ**
    public PinionGear() {
        super(DEFAULT_POSITION, DEFAULT_RADIUS, DEFAULT_COLOR);
    }

    public PinionGear(Pen pen, double speed, double theta, double alpha) {
        super(pen.getPosition(), pen.penSize, pen.color);
        this.pen = pen;
        this.speed = speed;
        this.theta = theta;
        this.alpha = alpha;
    }

    public void move(int time, Double spurRadius, Point2D.Double spurPosition) {
        // 移動処理（具体的な動作は未定）
        theta = speed * time;
        // **ピニオンギアの中心座標を更新**
        double centerX = spurPosition.x + (spurRadius - radius) * Math.cos(-theta);
        double centerY = spurPosition.y + (spurRadius - radius) * Math.sin(-theta);

        // 位置の更新
        this.position.setLocation(centerX, centerY);
    }

    public void setPosition(Point2D.Double position) {
        this.position = position;
    }

    public void changeSpeed(double speed) {
        this.speed = speed;
    }

    public Point2D.Double getPinionPosition() {
        return position;
    }

    public double getPinionRadius() {
        return radius;
    }

    public double getTheta() {
        return theta;
    }

    public double getAlpha() {
        return alpha;
    }

    public Pen getPen() {
        return pen;
    }
}
