package org.example.lib;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class PathSegmentTest {

    @Test
    void testAddAndGetPoints() {
        PathSegment segment = new PathSegment(Color.BLUE, 3.0);
        Point2D.Double p1 = new Point2D.Double(1, 2);
        Point2D.Double p2 = new Point2D.Double(3, 4);
        segment.addPoint(p1);
        segment.addPoint(p2);

        assertEquals(2, segment.getPoints().size());
        assertEquals(p1, segment.getPoints().get(0));
        assertEquals(p2, segment.getPoints().get(1));
    }

    @Test
    void testColorAndPenSize() {
        PathSegment segment = new PathSegment(Color.GREEN, 5.0);
        assertEquals(Color.GREEN, segment.getColor());
        assertEquals(5.0, segment.getPenSize());
    }

    @Test
    void testCopyConstructor() {
        PathSegment original = new PathSegment(Color.RED, 2.0);
        original.addPoint(new Point2D.Double(10, 10));
        PathSegment copy = new PathSegment(original.getColor(), original.getPenSize(), original.getPoints());
        assertEquals(original.getColor(), copy.getColor());
        assertEquals(original.getPenSize(), copy.getPenSize());
        assertEquals(original.getPoints(), copy.getPoints());
        // リストが独立していること
        copy.addPoint(new Point2D.Double(20, 20));
        assertNotEquals(original.getPoints().size(), copy.getPoints().size());
    }

    @Test
    void testEmptySegment() {
        PathSegment segment = new PathSegment(Color.BLACK, 1.0);
        assertTrue(segment.getPoints().isEmpty());
    }

    @Test
    void testSegmentWithInitialPoints() {
        Point2D.Double p1 = new Point2D.Double(1, 2);
        Point2D.Double p2 = new Point2D.Double(3, 4);
        PathSegment segment = new PathSegment(Color.ORANGE, 2.0, Arrays.asList(p1, p2));
        assertEquals(2, segment.getPoints().size());
        assertEquals(p1, segment.getPoints().get(0));
        assertEquals(p2, segment.getPoints().get(1));
    }

    @Test
    void testSegmentWithEmptyInitialPoints() {
        PathSegment segment = new PathSegment(Color.PINK, 2.0, Collections.emptyList());
        assertTrue(segment.getPoints().isEmpty());
    }
}
