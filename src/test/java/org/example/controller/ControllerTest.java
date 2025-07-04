package org.example.controller;

import org.example.model.Model;
import org.example.view.View;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ControllerTest {

    private Model model;
    private View view;
    private Controller controller;

    @BeforeEach
    void setUp() {
        model = mock(Model.class);
        view = mock(View.class);
        controller = new Controller(view, model);
    }

    @Test
    void testMouseWheelMovedZoomsView() {
        var wheelEvent = mock(java.awt.event.MouseWheelEvent.class);
        when(wheelEvent.getPoint()).thenReturn(new Point(100, 100));
        when(wheelEvent.getPreciseWheelRotation()).thenReturn(1.0);

        controller.mouseWheelMoved(wheelEvent);

        verify(view).zoomAt(eq(new Point(100, 100)), anyDouble());
    }

    @Test
    void testMouseClickedSetsPenPosition() {
        Point screenPoint = new Point(100, 100);
        Point2D worldPoint = new Point2D.Double(50, 50);

        when(view.screenToWorld(screenPoint)).thenReturn(worldPoint);
        when(model.getPinionGearPosition()).thenReturn(new Point2D.Double(48, 48));
        when(model.getPinionGearRadius()).thenReturn(10.0);
        when(model.isRunning()).thenReturn(false);

        MouseEvent click = mock(MouseEvent.class);
        when(click.getPoint()).thenReturn(screenPoint);

        controller.mouseClicked(click);

        verify(model).setPenPosition(eq(worldPoint));
        verify(view).repaint();
    }

    @Test
    void testMousePressedSetsDraggingMode() {
        Point screenPoint = new Point(100, 100);
        Point2D worldPoint = new Point2D.Double(50, 50);
        when(view.screenToWorld(screenPoint)).thenReturn(worldPoint);

        when(model.getSpurGearPosition()).thenReturn(new Point2D.Double(55, 55));
        when(model.getSpurGearRadius()).thenReturn(10.0);
        when(view.getScale()).thenReturn(1.0);

        MouseEvent press = mock(MouseEvent.class);
        when(press.getPoint()).thenReturn(screenPoint);
        when(press.isPopupTrigger()).thenReturn(false);
        when(press.getButton()).thenReturn(MouseEvent.BUTTON1);

        controller.mousePressed(press);
        verify(model).mousePressed(screenPoint);
    }

    @Test
    void testMouseReleasedCallsModel() {
        Point releasePoint = new Point(150, 150);
        MouseEvent release = mock(MouseEvent.class);
        when(release.getPoint()).thenReturn(releasePoint);

        controller.mouseReleased(release);
        verify(model).mouseReleased(releasePoint);
    }

    @Test
    void testMouseDraggedUpdatesModel() {
        Point startPoint = new Point(100, 100);
        Point2D startWorld = new Point2D.Double(10, 10);
        Point endPoint = new Point(120, 100);
        Point2D endWorld = new Point2D.Double(20, 10);

        when(view.screenToWorld(startPoint)).thenReturn(startWorld);
        when(view.screenToWorld(endPoint)).thenReturn(endWorld);
        when(view.getScale()).thenReturn(1.0);

        MouseEvent press = mock(MouseEvent.class);
        when(press.getPoint()).thenReturn(startPoint);
        when(press.isPopupTrigger()).thenReturn(false);
        controller.mousePressed(press);

        MouseEvent drag = mock(MouseEvent.class);
        when(drag.getPoint()).thenReturn(endPoint);
        controller.mouseDragged(drag);

        verify(view).repaint();
        verify(model).mouseDragged(endPoint);
    }

    @Test
    void testMenuButtonClickedStart() {
        controller.onMenuButtonClicked("Start");
        verify(view).clearLoadedLocusData();
        verify(model).start();
    }

    @Test
    void testMenuButtonClickedStop() {
        controller.onMenuButtonClicked("Stop");
        verify(model).stop();
    }

    @Test
    void testMenuButtonClickedClear() {
        controller.onMenuButtonClicked("Clear");
        verify(model).resetGears();
        verify(model).stop();
        verify(view).clearLoadedLocusData();
        verify(view).repaint();
    }

    @Test
    void testMenuButtonClickedColor() {
        controller.onColorSelected(Color.RED);
        verify(model).changePenColor(Color.RED);
        verify(view).repaint();
    }

    @Test
    void testMenuButtonClickedSpeed() {
        controller.onSpeedSelected(2.0);
        verify(model).changeSpeed(2.0);
    }
}
