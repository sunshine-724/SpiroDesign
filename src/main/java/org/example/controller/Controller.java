package org.example.controller;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.event.MouseInputAdapter;

import org.example.model.Model;
import org.example.view.View;


public class Controller extends MouseInputAdapter implements MouseWheelListener {
    protected Model model = null;
    protected View view = null;
    private Point previous = null;
    private Point current = null;

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

    private Mode currentMode = NONE;
    private Point pressPoint;
    private point2D gearCenter;

    // マウスクリック
    public void mouseClicked(MouseEvent e_click) {
        Point clickedpoint = e_click.getPoint();
        model.Point(clickedpoint);
    }

    public void mousePressed(MouseEvent e_press) {
        Point pressedpoint = e_press.getPoint();
        switch (currentMode) {
            case SPUR_GEAR:
            case PINION_GEAR:
                gearCenter = view.toWorldCoordinates(pressPoint);
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
        model.Point(releasedpoint);
    }

    /*public void mouseDragged(MouseEvent e_drag) {

    }*/
  
}
