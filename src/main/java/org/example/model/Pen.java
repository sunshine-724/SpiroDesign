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

    public void move(int deltatime) {
        // 移動処理（具体的な動作は未定）
    }

    public void setPosition(Point2D.Double position) {
        this.position = position;
    }

    public Point2D.Double getPosition() { // ゲッターを追加
        return position;
    }

    public void changeColor(Color color) {
        this.color = color;
    }

    public void changeSize(double size) {
        this.penSize = size;
    }
}
