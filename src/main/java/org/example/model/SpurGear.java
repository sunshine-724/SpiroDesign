package org.example.model;

import java.awt.Color;
import java.awt.geom.Point2D;

public class SpurGear extends SpiroGear {
    // **デフォルト値**
    private static final Point2D.Double DEFAULT_POSITION = new Point2D.Double(400, 400);
    private static final double DEFAULT_RADIUS = 100.0;
    private static final Color DEFAULT_COLOR = Color.BLACK;

    // **デフォルトコンストラクタ**
    public SpurGear() {
        super(DEFAULT_POSITION, DEFAULT_RADIUS, DEFAULT_COLOR);
    }

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
