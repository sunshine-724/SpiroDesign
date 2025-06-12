package org.example.model;

import org.junit.jupiter.api.Test;
import java.awt.Color;
import java.awt.geom.Point2D;

public class SpurGearTest {

    @Test
    void testDefaultConstructor() {
        // 入力値
        SpurGear gear = new SpurGear();
        // 予想される値
        Point2D.Double expectedPosition = new Point2D.Double(400, 400);
        double expectedRadius = 100.0;

        // 判定
        if (expectedPosition.equals(gear.getSpurPosition()) &&
                Math.abs(expectedRadius - gear.getSpurRadius()) < 1e-9) {
            System.out.println("良好");
        } else {
            System.out.println("不良");
        }
    }

    @Test
    void testParameterizedConstructor() {
        // 入力値
        Point2D.Double pos = new Point2D.Double(123, 456);
        double radius = 77.7;
        Color color = Color.RED;
        SpurGear gear = new SpurGear(pos, radius, color);
        // 予想される値
        Point2D.Double expectedPosition = pos;
        double expectedRadius = radius;
        Color expectedColor = color;

        // 判定
        if (expectedPosition.equals(gear.getSpurPosition()) &&
                Math.abs(expectedRadius - gear.getSpurRadius()) < 1e-9
        /* 色の判定が必要なら追加 */) {
            System.out.println("良好");
        } else {
            System.out.println("不良");
        }
    }

    @Test
    void testGetSpurRadius() {
        // 入力値
        SpurGear gear = new SpurGear(new Point2D.Double(0, 0), 42.0, Color.BLUE);
        // 予想される値
        double expectedRadius = 42.0;

        // 判定
        if (Math.abs(gear.getSpurRadius() - expectedRadius) < 1e-9) {
            System.out.println("良好");
        } else {
            System.out.println("不良");
        }
    }

    @Test
    void testGetSpurPosition() {
        // 入力値
        Point2D.Double pos = new Point2D.Double(1, 2);
        SpurGear gear = new SpurGear(pos, 10.0, Color.GREEN);
        // 予想される値
        Point2D.Double expectedPosition = pos;

        // 判定
        if (gear.getSpurPosition().equals(expectedPosition)) {
            System.out.println("良好");
        } else {
            System.out.println("不良");
        }
    }
}
