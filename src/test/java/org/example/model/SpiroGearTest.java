package org.example.model;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.geom.Point2D;

import static org.junit.jupiter.api.Assertions.*;

public class SpiroGearTest {

    @Test
    void testConstructor() {
        Point2D.Double pos = new Point2D.Double(100, 200);
        double radius = 50.0;
        Color color = Color.RED;

        SpiroGear gear = new SpiroGear(pos, radius, color);

        assertEquals(pos, gear.position);
        assertEquals(radius, gear.radius, 1e-9);
        assertEquals(color, gear.color);
    }

    @Test
    void testChangeRadius() {
        SpiroGear gear = new SpiroGear(new Point2D.Double(0, 0), 30.0, Color.BLUE);
        gear.changeRadius(45.5);
        assertEquals(45.5, gear.radius, 1e-9);
    }

    @Test
    void testChangeColor() {
        SpiroGear gear = new SpiroGear(new Point2D.Double(0, 0), 30.0, Color.BLUE);
        gear.changeColor(Color.GREEN);
        assertEquals(Color.GREEN, gear.color);
    }
}
