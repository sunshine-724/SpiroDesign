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

    // マウスのコロコロ
    public void mouseWheelMoved(MouseWheelEvent e_wheel) {
        Integer amount = -e_wheel.getWheelRotation();
        int modifiers = e_wheel.getModifiersEx();
        boolean isShiftDown = (modifiers & MouseEvent.SHIFT_DOWN_MASK) != 0;
        if(amount == 0) return;
        Point scroll = new Point(0, amount);
        if(isShiftDown) scroll = new Point(amount, 0);
        
    }

    // マウスクリック
    public void mouseClicked(MouseEvent e_click) {
        Point clickedpoint = e_click.getPoint();
    }
    public void mousePressed(MouseEvent e_press) {
        Point pressedpoint = e_press.getPoint();
    }
    public void mouseReleased(MouseEvent e_release) {
        Point releasedpoint = e_release.getPoint();
    }

    public void mouseDrag(MouseEvent e_drag) {
    Cursor cursor = Cursor.getPredefinedCursor(13);
    Component component = (Component)e_drag.getSource();
    component.setCursor(cursor);
    this.current = e_drag.getPoint();
    Integer integer1 = Integer.valueOf(this.current.x - this.previous.x);
    Integer integer2 = Integer.valueOf(this.current.y - this.previous.y);
    Point point = new Point(integer1.intValue(), integer2.intValue());
    scrollBy(point, e_drag);
    this.previous = this.current;
    }
  
}
