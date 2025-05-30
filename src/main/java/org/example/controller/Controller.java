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

    public void mouseWheelMoved(MouseWheelEvent e) {
        Integer amount = -e.getWheelRotation();
        int modifiers = e.getModifiersEx();
        boolean isShiftDown = (modifiers & MouseEvent.SHIFT_DOWN_MASK) != 0;
        if(amount == 0) return;
        Point scroll = new Point(0, amount);
        if(isShiftDown) scroll = new Point(amount, 0);
        
    }

    public void mouseClick(MouseEvent paramMouseEvent) {
        Point point = paramMouseEvent.getPoint();
        point.translate((this.view.displayMousePointer).x, (this.view.displayMousePointer).y);
        System.out.println(point);
    }

    public void mouseRightClick(MouseEvent paramMouseEvent){
        
    }

    public void mouseDrag(MouseEvent paramMouseEvent) {
    Cursor cursor = Cursor.getPredefinedCursor(13);
    Component component = (Component)paramMouseEvent.getSource();
    component.setCursor(cursor);
    this.current = paramMouseEvent.getPoint();
    Integer integer1 = Integer.valueOf(this.current.x - this.previous.x);
    Integer integer2 = Integer.valueOf(this.current.y - this.previous.y);
    Point point = new Point(integer1.intValue(), integer2.intValue());
    scrollBy(point, paramMouseEvent);
    this.previous = this.current;
    }
  
}
