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
    protected Model model = null;
    protected View view = null;
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
    public Controller(View view) {
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
        view.Point(scroll);
    }

    private Mode currentMode = Mode.NONE;
    private Point pressPoint;
    private Point2D gearCenter;

    // マウスクリック
    public void mouseClicked(MouseEvent e_click) {
        Point clickedpoint = e_click.getPoint();
        switch (currentMode) {
            case SPUR_GEAR:
            case PINION_GEAR:
                Point2D currentWorld = view.toWorldCoordinates(clickedpoint);
                double radius = gearCenter.distance(currentWorld);
                // 一時表示のためのプレビュー用のギアを書いてもらうかも
                view.setPreviewGear(gearCenter, radius, currentMode);
                break;
            case PAN:
                int dx = clickedpoint.x - pressPoint.x;
                int dy = clickedpoint.y - pressPoint.y;
                view.pan(dx,dy);
                pressPoint = clickedpoint;
                break;
            default: 
                break;
        model.Point(clickedpoint);
        }
    }

    public void mousePressed(MouseEvent e_press) {
        Point pressedpoint = e_press.getPoint();
        switch (currentMode) {
            case SPUR_GEAR:
            case PINION_GEAR:
                gearCenter = view.toWorldCoordinates(pressedpoint);
                break;
            case PAN:
                break;
            default: 
                break;
        }
        model.Point(pressedpoint);
    }

    public void mouseReleased(MouseEvent e_release) {
        Point releasedpoint = e_release.getPoint();
        switch (currentMode) {
        case SPUR_GEAR:
        case PINION_GEAR:
            Point2D releaseWorld = view.toWorldCoordinates(releasedpoint);
            double radius = gearCenter.distance(releaseWorld);
            model.addGear(new Gear(gearCenter, radius, currentMode));
            view.clearPreview();
        break;
        case PAN:
            break;
        default:
            break;
        model.Point(releasedpoint);
        }
    }
    currentMode = mode.NONE;

    /*public void mouseDragged(MouseEvent e_drag) {

    }*/
  
}

