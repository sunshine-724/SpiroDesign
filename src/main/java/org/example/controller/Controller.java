package org.example.controller;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;

import javax.swing.event.MouseInputAdapter;

import org.example.model.Model;
import org.example.view.View;

public class Controller extends MouseInputAdapter implements MouseWheelListener {
    protected Model model;
    protected View view;

    // 変数の定義
    private enum DraggingMode {
        NONE,
        MOVE_CENTER,
        RESIZE_RADIUS,
        PAN
    }

    private DraggingMode draggingMode = DraggingMode.NONE;

    // マウスリスナーの登録
    public Controller(View view, Model model) {
        this.view = view;
        this.model = model;
        view.addMouseListener(this);
        view.addMouseMotionListener(this);
    }

    // マウスのコロコロ
    public void mouseWheelMoved(MouseWheelEvent e_wheel) {
        Integer amount = -e_wheel.getWheelRotation();
        int modifiers = e_wheel.getModifiersEx();
        boolean isShiftDown = (modifiers & MouseEvent.SHIFT_DOWN_MASK) != 0;
        if (amount == 0)
            return;
        Point scroll = new Point(0, amount);
        if (isShiftDown)
            scroll = new Point(amount, 0);
        view.scaling(isShiftDown);
    }

    private Point pressPoint;

    // マウスクリック
    public void mouseClicked(MouseEvent e_click) {
        Point clickedpoint = e_click.getPoint();
        Point2D worldClicked = view.screenToWorld(clickedpoint);
        Point2D pinionCenter = model.getPinionGearPosition();
        double distance = worldClicked.distance(pinionCenter);
        double radius = model.getPinionGearRadius();
        if (distance <= radius) {
        model.setPenPosition(worldClicked);
        view.repaint();
        return;
    }
        model.mouseClicked(clickedpoint);
    }

    public void mousePressed(MouseEvent e_press) {
        Point pressedPoint = e_press.getPoint();
        pressPoint = pressedPoint;

        // ギア中心までの距離を計算
        double distance = pressedPoint.distance(model.getSpurGearPosition());

        if (distance < 10) {
            draggingMode = DraggingMode.MOVE_CENTER;
        } else if (Math.abs(distance - model.getSpurGearRadius()) < 10) {
            draggingMode = DraggingMode.RESIZE_RADIUS;
        } else {
            draggingMode = DraggingMode.PAN;
        }

        model.mousePressed(pressedPoint);
    }

    public void mouseReleased(MouseEvent e_release) {
        draggingMode = DraggingMode.NONE;
        model.mouseReleased(e_release.getPoint());
    }

    public void mouseDragged(MouseEvent e_drag) {
        Point currentPoint = e_drag.getPoint();

        switch (draggingMode) {
            case MOVE_CENTER:
                int dx = currentPoint.x - pressPoint.x;
                int dy = currentPoint.y - pressPoint.y;

                model.moveSpurGearBy(dx, dy);
                pressPoint = currentPoint;
                view.repaint();
                break;

            case RESIZE_RADIUS:
                double newRadius = currentPoint.distance(model.getSpurGearPosition());
                model.setSpurRadius(newRadius);
                view.repaint();
                break;

            case PAN:
                int panDx = currentPoint.x - pressPoint.x;
                int panDy = currentPoint.y - pressPoint.y;
                view.pan(panDx, panDy);
                pressPoint = currentPoint;
                break;

            default:
                model.mouseDragged(currentPoint);
                break;
        }
        model.mouseDragged(currentPoint);
    }
}

/*
 * public void mouseMoved(MouseEvent e_cursor) {
 * Point p = e_cursor.getPoint();
 * double d = p.distance(model.getSpurGearPosition());
 * if (d < 10) {
 * view.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
 * } else if (Math.abs(d - model.getSpurGearRadius()) < 10) {
 * view.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
 * } else {
 * view.setCursor(Cursor.getDefaultCursor());
 * }
 * }
 */

// modelに欲しいかもしれないもの
/*
 * public void moveSpurGearBy(int dx, int dy) {
 * spurPosition.setLocation(spurPosition.getX() + dx, spurPosition.getY() + dy);
 * 
 * // ピニオンやペンも相対移動
 * pinionPosition.setLocation(pinionPosition.getX() + dx, pinionPosition.getY()
 * + dy);
 * penPosition.setLocation(penPosition.getX() + dx, penPosition.getY() + dy);
 * }
 * 
 * public void setSpurRadius(double newRadius) {
 * spurRadius = newRadius;
 * }
 */

// viewに欲しいかもしれないもの
/*
 * private int offsetX = 0;
 * private int offsetY = 0;
 * 
 * public void pan(int dx, int dy) {
 * offsetX += dx;
 * offsetY += dy;
 * repaint();
 * }
 * 
 * public Point2D toWorldCoordinates(Point screenPoint) {
 * return new Point2D.Double(screenPoint.x - offsetX, screenPoint.y - offsetY);
 * }
 * 
 * public Point toScreenCoordinates(Point2D worldPoint) {
 * return new Point((int)(worldPoint.getX() + offsetX), (int)(worldPoint.getY()
 * + offsetY));
 * }
 * 
 */
