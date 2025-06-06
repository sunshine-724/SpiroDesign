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
    /*private Gear spurGear; 
    private Gear pinionGear;
    private Pen pen;*/
    private Point previous = null;
    private Point current = null;

    // 変数の定義
    public enum Mode {
        NONE,
        SPUR_GEAR,
        PINION_GEAR,
        PAN
    }

    // マウスリスナーの登録
    public Controller(View view ,Model model) {
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
        if(amount == 0) return;
        Point scroll = new Point(0, amount);
        if(isShiftDown) scroll = new Point(amount, 0);
        view.scaling(isShiftDown); //エラーは消えたけど多分違う
    }

    private Mode currentMode = Mode.NONE;
    private Point pressPoint;
    private Point2D gearCenter;

    // マウスクリック
    public void mouseClicked(MouseEvent e_click) {
        Point clickedpoint = e_click.getPoint();
        model.mouseClicked(clickedpoint);
    }

    public void mousePressed(MouseEvent e_press) {
        Point pressedPoint = e_press.getPoint();
        /*Point2D worldPoint = view.toWorldCoordinates(pressedPoint);
        boolean inSpur = spurGear.contains(worldPoint);
        boolean inPinion = pinionGear.contains(worldPoint);
        boolean onPen = pen.contains(worldPoint);
        if (inSpur && !inPinion) {
            currentMode = Mode.SPUR_GEAR;
        }else if (inPinion && !onPen) {
            currentMode = Mode.PINION_GEAR;
        }else if (!inSpur) {
            currentMode = Mode.PAN;
        }else {
            currentMode = Mode.NONE;
        }*/
        model.mousePressed(pressedPoint);
    }

    public void mouseReleased(MouseEvent e_release) {
        Point releasedPoint = e_release.getPoint();
        currentMode = Mode.NONE;
        model.mouseReleased(releasedPoint);
    }

    public void mouseDragged(MouseEvent e_drag) {
        Point currentPoint = e_drag.getPoint();
        /*Point2D currentWorld = view.toWorldCoordinates(currentPoint);
        switch (currentMode) {
            case SPUR_GEAR:
                spurGear.moveTo(currentWorld);
                view.repaint();
                break;
            case PINION_GEAR:
                pinionGear.moveTo(currentWorld);
                view.repaint();
                break;
            case PAN:
                int dx = currentPoint.x - pressedPoint.x;
                int dy = currentPoint.y - pressedPoint.y;
                view.pan(dx, dy);
                pressedPoint = currentPoint;
                break;
        }*/
        model.mouseDragged(currentPoint);
    }

    /*public boolean contains(Point2D p) {
        double distance = center.distance(p);
        return distance <= radius;
    }*/
  
}

