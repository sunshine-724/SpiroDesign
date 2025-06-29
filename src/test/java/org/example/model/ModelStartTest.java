package org.example.model;

import org.example.lib.PathSegment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.geom.Point2D;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ModelStartTest {
    private Model model;

    @BeforeEach
    void setUp() {
        model = new Model();
        model.resetGears();
    }

    @Test
    void testStartAddsPointsToPathSegment() throws InterruptedException {
        List<PathSegment> before = model.getPathSegments();
        assertTrue(before.isEmpty() || before.get(0).getPoints().isEmpty());

        model.start();
        // タイマーで軌跡が追加されるまで少し待つ
        Thread.sleep(500);

        List<PathSegment> after = model.getPathSegments();
        // 軌跡が1つ以上追加されていること
        boolean hasPoints = after.stream().anyMatch(seg -> seg.getPoints().size() > 0);
        assertTrue(hasPoints, "start()後に軌跡が追加されているべき");
    }

    @Test
    void testPenPositionChangesAfterStart() throws InterruptedException {
        Point2D.Double before = model.getPenPosition();
        model.start();
        Thread.sleep(200);
        Point2D.Double after = model.getPenPosition();
        // ペン位置が(0,0)以外になっていることを確認
        assertNotNull(after);
        boolean notOrigin = Math.abs(after.x) > 1e-6 || Math.abs(after.y) > 1e-6;
        assertTrue(notOrigin, "start()後にペン位置が(0,0)以外になっているべき");
    }

    @Test
    void testStartStopStartPathAndPenPosition() throws InterruptedException {
        // 1回目のstart
        model.start();
        Thread.sleep(500);
        Point2D.Double penAfterFirstStart = model.getPenPosition();
        List<PathSegment> pathAfterFirstStart = model.getPathSegments();
        assertNotNull(penAfterFirstStart);
        assertTrue(pathAfterFirstStart.stream().anyMatch(seg -> seg.getPoints().size() > 0));

        // stop
        model.stop();
        Point2D.Double penAfterStop = model.getPenPosition();
        List<PathSegment> pathAfterStop = model.getPathSegments();
        assertNotNull(penAfterStop);
        assertEquals(penAfterFirstStart, penAfterStop, "stop直後のペン位置は変わらないはず");

        // 2回目のstart
        model.start();
        Thread.sleep(500);
        Point2D.Double penAfterSecondStart = model.getPenPosition();
        List<PathSegment> pathAfterSecondStart = model.getPathSegments();
        assertNotNull(penAfterSecondStart);
        // ペン位置が動いていること
        boolean moved = penAfterSecondStart.distance(penAfterStop) > 1e-6;
        assertTrue(moved, "2回目のstart後にペン位置が動いているべき");
        // 軌跡がさらに増えていること
        int totalPointsBefore = pathAfterStop.stream().mapToInt(seg -> seg.getPoints().size()).sum();
        int totalPointsAfter = pathAfterSecondStart.stream().mapToInt(seg -> seg.getPoints().size()).sum();
        assertTrue(totalPointsAfter > totalPointsBefore, "2回目のstart後に軌跡が増えているべき");
    }

    @Test
    void testStartStopWaitStartWithTime() throws InterruptedException {
        // 1回目のstart
        model.start();
        Thread.sleep(500);
        Point2D.Double penAfterFirstStart = model.getPenPosition();
        List<PathSegment> pathAfterFirstStart = model.getPathSegments();
        assertNotNull(penAfterFirstStart);
        int pointsAfterFirstStart = pathAfterFirstStart.stream().mapToInt(seg -> seg.getPoints().size()).sum();
        assertTrue(pointsAfterFirstStart > 0);

        // stop
        model.stop();
        Point2D.Double penAfterStop = model.getPenPosition();
        List<PathSegment> pathAfterStop = model.getPathSegments();
        int pointsAfterStop = pathAfterStop.stream().mapToInt(seg -> seg.getPoints().size()).sum();

        // 十分待機（stop中はペン位置も軌跡も変化しないことを確認）
        Thread.sleep(500);
        Point2D.Double penAfterWait = model.getPenPosition();
        List<PathSegment> pathAfterWait = model.getPathSegments();
        int pointsAfterWait = pathAfterWait.stream().mapToInt(seg -> seg.getPoints().size()).sum();

        assertEquals(penAfterStop, penAfterWait, "stop中はペン位置が変化しないべき");
        assertEquals(pointsAfterStop, pointsAfterWait, "stop中は軌跡が増えないべき");

        // 2回目のstart
        model.start();
        Thread.sleep(500);
        Point2D.Double penAfterSecondStart = model.getPenPosition();
        List<PathSegment> pathAfterSecondStart = model.getPathSegments();
        int pointsAfterSecondStart = pathAfterSecondStart.stream().mapToInt(seg -> seg.getPoints().size()).sum();

        // ペン位置が動いていること
        boolean moved = penAfterSecondStart.distance(penAfterWait) > 1e-6;
        assertTrue(moved, "再start後にペン位置が動いているべき");
        // 軌跡がさらに増えていること
        assertTrue(pointsAfterSecondStart > pointsAfterWait, "再start後に軌跡が増えているべき");
    }
}
