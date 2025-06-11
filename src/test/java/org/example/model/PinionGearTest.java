package org.example.model;

import org.junit.jupiter.api.Test;
import java.awt.Color;
import java.awt.geom.Point2D;

import static org.junit.jupiter.api.Assertions.*;

public class PinionGearTest {

    @Test
    void testDefaultConstructor() {
        PinionGear gear = new PinionGear();
        assertEquals(new Point2D.Double(470, 400), gear.getPinionPosition());
        assertEquals(30.0, gear.getPinionRadius(), 1e-9);
    }

    @Test
    void testParameterizedConstructor() {
        Pen pen = new Pen(2.0, Color.GREEN, new Point2D.Double(10, 10));
        double speed = 1.5;
        double theta = Math.PI / 4;
        double alpha = 0.1;
        PinionGear gear = new PinionGear(pen, speed, theta, alpha);
        assertEquals(pen.getPosition(), gear.getPinionPosition());
        assertEquals(pen.penSize, gear.getPinionRadius(), 1e-9);
        assertEquals(speed, gear.speed, 1e-9);
        assertEquals(theta, gear.theta, 1e-9);
        assertEquals(alpha, gear.alpha, 1e-9);
        assertEquals(pen, gear.getPen());
    }

    @Test
    void testMove() {
        Pen pen = new Pen(2.0, Color.BLUE, new Point2D.Double(0, 0));
        PinionGear gear = new PinionGear(pen, 0.01, 0.0, 0.0);
        Point2D.Double spurPos = new Point2D.Double(100, 100);
        double spurRadius = 50.0;
        gear.move(100, spurRadius, spurPos);
        double expectedTheta = 0.01 * 100;
        double expectedX = 100 + (spurRadius - gear.getPinionRadius()) * Math.cos(-expectedTheta);
        double expectedY = 100 + (spurRadius - gear.getPinionRadius()) * Math.sin(-expectedTheta);
        assertEquals(expectedTheta, gear.getTheta(), 1e-9);
        assertEquals(new Point2D.Double(expectedX, expectedY), gear.getPinionPosition());
    }

    @Test
    void testSetPosition() {
        PinionGear gear = new PinionGear();
        Point2D.Double pos = new Point2D.Double(7, 8);
        gear.setPosition(pos);
        assertEquals(pos, gear.getPinionPosition());
    }

    @Test
    void testChangeSpeed() {
        PinionGear gear = new PinionGear();
        gear.changeSpeed(2.2);
        assertEquals(2.2, gear.speed, 1e-9);
    }

    @Test
    void testGetters() {
        Pen pen = new Pen(1.0, Color.BLACK, new Point2D.Double(0, 0));
        PinionGear gear = new PinionGear(pen, 3.3, 4.4, 5.5);
        assertEquals(3.3, gear.speed, 1e-9);
        assertEquals(4.4, gear.getTheta(), 1e-9);
        assertEquals(5.5, gear.getAlpha(), 1e-9);
        assertEquals(pen, gear.getPen());
    }
}
