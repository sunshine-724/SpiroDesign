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
        MOVE_SPUR_CENTER,
        MOVE_PINION,
        RESIZE_SPUR_RADIUS,
        PAN
    }

    private DraggingMode draggingMode = DraggingMode.NONE;
    private Point pressPoint;
    private Point2D pressWorldPoint;

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
        pressWorldPoint = view.screenToWorld(pressPoint);

        Point2D spurCenter = model.getSpurGearPosition();
        double spurRadius = model.getSpurGearRadius();
        Point2D pinionCenter = model.getPinionGearPosition();
        double pinionRadius = model.getPinionGearRadius();

        double distToSpur = pressWorldPoint.distance(spurCenter);
        double distToPinion = pressWorldPoint.distance(pinionCenter);

        if (distToSpur < 10) {
            draggingMode = DraggingMode.MOVE_SPUR_CENTER;
        } else if (Math.abs(distToSpur - spurRadius) < 10) {
            draggingMode = DraggingMode.RESIZE_SPUR_RADIUS;
        } else if (distToPinion < 10) {
            draggingMode = draggingMode.MOVE_PINION;
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
        Point2D currentWorld = view.screenToWorld(currentPoint);
        double dx = currentWorld.getX() - pressWorldPoint.getX();
        double dy = currentWorld.getY() - pressWorldPoint.getY();

        switch (draggingMode) {
            case MOVE_SPUR_CENTER:
                model.moveSpurGearBy(dx, dy);
                model.movePinionGearBy(dx, dy);
                model.movePenBy(dx, dy);
                break;

            case RESIZE_SPUR_RADIUS:
                double newRadius = currentWorld.distance(model.getSpurGearPosition());
                model.setSpurRadius(newRadius);
                view.repaint();
                break;

            case MOVE_PINION:
                Point2D newCenter = new Point2D.Double(
                    model.getSpurGearPosition().getX() + (currentWorld.getX() - model.getSpurGearPosition().getX()),
                    model.getSpurGearPosition().getY() + (currentWorld.getY() - model.getSpurGearPosition().getY())
                );
                double newPinionRadius = newCenter.distance(model.getSpurGearPosition());
                model.setPinionGearPosition(newCenter);
                model.changePinionGearRadius(newPinionRadius);
                break;

            case PAN:
                int panDx = currentPoint.x - pressPoint.x;
                int panDy = currentPoint.y - pressPoint.y;
                view.pan(panDx, panDy);
                break;

            default:
                break;
        }
        pressPoint = currentPoint;
        pressWorldPoint = currentWorld;
        view.repaint();
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
