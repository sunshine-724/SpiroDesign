package org.example.model;

import org.example.lib.PathSegment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ModelPathSegmentTest {
    private Model model;

    @BeforeEach
    void setUp() {
        model = new Model();
        model.resetGears();
    }

    @Test
    void testInitialPathSegmentIsEmpty() {
        List<PathSegment> segments = model.getPathSegments();
        assertEquals(1, segments.size());
        assertTrue(segments.get(0).getPoints().isEmpty());
    }

    @Test
    void testAddPointToCurrentSegment() {
        Point2D.Double p = new Point2D.Double(100, 100);
        model.setPenPosition(p);
        List<PathSegment> segments = model.getPathSegments();
        assertEquals(1, segments.size());
        assertEquals(1, segments.get(0).getPoints().size());
        assertEquals(p, segments.get(0).getPoints().get(0));
    }

    @Test
    void testChangePenColorCreatesNewSegment() {
        Point2D.Double p1 = new Point2D.Double(100, 100);
        model.setPenPosition(p1);
        model.changePenColor(Color.RED);
        Point2D.Double p2 = new Point2D.Double(200, 200);
        model.setPenPosition(p2);

        List<PathSegment> segments = model.getPathSegments();
        assertEquals(2, segments.size());
        assertEquals(Color.BLACK, segments.get(0).getColor());
        assertEquals(Color.RED, segments.get(1).getColor());
        assertEquals(p2, segments.get(1).getPoints().get(0));
    }

    @Test
    void testChangePenSizeCreatesNewSegment() {
        Point2D.Double p1 = new Point2D.Double(10, 10);
        model.setPenPosition(p1);
        model.changePenSize(5.0);
        Point2D.Double p2 = new Point2D.Double(20, 20);
        model.setPenPosition(p2);

        List<PathSegment> segments = model.getPathSegments();
        assertEquals(2, segments.size());
        assertEquals(2.0, segments.get(0).getPenSize());
        assertEquals(5.0, segments.get(1).getPenSize());
        assertEquals(p2, segments.get(1).getPoints().get(0));
    }

    @Test
    void testMultiplePointsInSegment() {
        Point2D.Double p1 = new Point2D.Double(1, 1);
        Point2D.Double p2 = new Point2D.Double(2, 2);
        model.setPenPosition(p1);
        model.setPenPosition(p2);

        List<PathSegment> segments = model.getPathSegments();
        assertEquals(1, segments.size());
        assertEquals(2, segments.get(0).getPoints().size());
        assertEquals(p1, segments.get(0).getPoints().get(0));
        assertEquals(p2, segments.get(0).getPoints().get(1));
    }

    @Test
    void testStartNewPathSegment() {
        Point2D.Double p1 = new Point2D.Double(10, 10);
        model.setPenPosition(p1);
        model.startNewPathSegment();
        Point2D.Double p2 = new Point2D.Double(20, 20);
        model.setPenPosition(p2);

        List<PathSegment> segments = model.getPathSegments();
        assertEquals(2, segments.size());
        assertEquals(p1, segments.get(0).getPoints().get(0));
        assertEquals(p2, segments.get(1).getPoints().get(0));
    }
}
