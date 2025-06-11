package org.example.model;

import org.junit.jupiter.api.Test;
import java.awt.Color;
import java.awt.geom.Point2D;

import static org.junit.jupiter.api.Assertions.*;

public class SpurGearTest {

    @Test
    void testDefaultConstructor() {
        SpurGear gear = new SpurGear();
        assertEquals(new Point2D.Double(400, 400), gear.getSpurPosition());
        assertEquals(100.0, gear.getSpurRadius(), 1e-9);
    }

    @Test
    void testParameterizedConstructor() {
        Point2D.Double pos = new Point2D.Double(123, 456);
        double radius = 77.7;
        Color color = Color.RED;
        SpurGear gear = new SpurGear(pos, radius, color);
        assertEquals(pos, gear.getSpurPosition());
        assertEquals(radius, gear.getSpurRadius(), 1e-9);
    }

    @Test
    void testGetSpurRadius() {
        SpurGear gear = new SpurGear(new Point2D.Double(0, 0), 42.0, Color.BLUE);
        assertEquals(42.0, gear.getSpurRadius(), 1e-9);
    }

    @Test
    void testGetSpurPosition() {
        Point2D.Double pos = new Point2D.Double(1, 2);
        SpurGear gear = new SpurGear(pos, 10.0, Color.GREEN);
        assertEquals(pos, gear.getSpurPosition());
    }
}
