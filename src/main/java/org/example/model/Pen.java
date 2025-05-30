package org.example.model;

import java.awt.Color;
import java.awt.geom.Point2D;

public class Pen {
    public double penSize;
    public Color color;
    private Point2D.Double position;

    public Pen(double penSize, Color color, Point2D.Double position) {
        this.penSize = penSize;
        this.color = color;
        this.position = position;
    }

    public void move(int time, Point2D.Double pinionPosition, Double pinionRadius, Double theta, Double alpha) {
        double penX = pinionPosition.x + pinionRadius * Math.cos(theta + alpha);
        double penY = pinionPosition.y + pinionRadius * Math.sin(theta + alpha);
        this.position.setLocation(penX, penY);
    }

    public void setPosition(Point2D.Double position) {
        this.position = position;
    }

    public Point2D.Double getPosition() {
        return position;
    }

    public void changeColor(Color color) {
        this.color = color;
    }

    public void changeSize(double size) {
        this.penSize = size;
    }
}
