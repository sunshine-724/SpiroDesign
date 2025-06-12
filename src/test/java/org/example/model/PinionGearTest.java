package org.example.model;

import org.junit.jupiter.api.Test;
import java.awt.Color;
import java.awt.geom.Point2D;

import static org.junit.jupiter.api.Assertions.*;

public class PinionGearTest {

    @Test
    void testDefaultConstructor() {
        // 入力値
        Point2D.Double expectedPosition = new Point2D.Double(470, 400);
        double expectedRadius = 30.0;

        // テスト実行
        PinionGear gear = new PinionGear();
        Point2D.Double actualPosition = gear.getPinionPosition();
        double actualRadius = gear.getPinionRadius();

        // 判定と出力
        if (expectedPosition.equals(actualPosition) && Math.abs(expectedRadius - actualRadius) < 1e-9) {
            System.out.println("良好");
        } else {
            System.out.println("不良");
        }

        // 従来のアサーションも残す場合
        assertEquals(expectedPosition, actualPosition);
        assertEquals(expectedRadius, actualRadius, 1e-9);
    }

    @Test
    void testParameterizedConstructor() {
        // 入力値と予想値
        Pen pen = new Pen(2.0, Color.GREEN, new Point2D.Double(10, 10));
        double speed = 1.5;
        double theta = Math.PI / 4;
        double alpha = 0.1;
        Point2D.Double expectedPosition = pen.getPosition();
        double expectedRadius = pen.penSize;

        // テスト実行
        PinionGear gear = new PinionGear(pen, speed, theta, alpha);

        // 判定と出力
        if (expectedPosition.equals(gear.getPinionPosition())
                && Math.abs(expectedRadius - gear.getPinionRadius()) < 1e-9
                && Math.abs(speed - gear.speed) < 1e-9
                && Math.abs(theta - gear.theta) < 1e-9
                && Math.abs(alpha - gear.alpha) < 1e-9
                && pen.equals(gear.getPen())) {
            System.out.println("良好");
        } else {
            System.out.println("不良");
        }

        // アサーション
        assertEquals(expectedPosition, gear.getPinionPosition());
        assertEquals(expectedRadius, gear.getPinionRadius(), 1e-9);
        assertEquals(speed, gear.speed, 1e-9);
        assertEquals(theta, gear.theta, 1e-9);
        assertEquals(alpha, gear.alpha, 1e-9);
        assertEquals(pen, gear.getPen());
    }

    @Test
    void testMove() {
        // 入力値と予想値
        Pen pen = new Pen(2.0, Color.BLUE, new Point2D.Double(0, 0));
        PinionGear gear = new PinionGear(pen, 0.01, 0.0, 0.0);
        Point2D.Double spurPos = new Point2D.Double(100, 100);
        double spurRadius = 50.0;
        double expectedTheta = 0.01 * 100;
        double expectedX = 100 + (spurRadius - gear.getPinionRadius()) * Math.cos(-expectedTheta);
        double expectedY = 100 + (spurRadius - gear.getPinionRadius()) * Math.sin(-expectedTheta);
        Point2D.Double expectedPosition = new Point2D.Double(expectedX, expectedY);

        // テスト実行
        gear.move(100, spurRadius, spurPos);

        // 判定と出力
        if (Math.abs(expectedTheta - gear.getTheta()) < 1e-9
                && expectedPosition.equals(gear.getPinionPosition())) {
            System.out.println("良好");
        } else {
            System.out.println("不良");
        }

        // アサーション
        assertEquals(expectedTheta, gear.getTheta(), 1e-9);
        assertEquals(expectedPosition, gear.getPinionPosition());
    }

    @Test
    void testSetPosition() {
        // 入力値と予想値
        PinionGear gear = new PinionGear();
        Point2D.Double pos = new Point2D.Double(7, 8);

        // テスト実行
        gear.setPosition(pos);

        // 判定と出力
        if (pos.equals(gear.getPinionPosition())) {
            System.out.println("良好");
        } else {
            System.out.println("不良");
        }

        // アサーション
        assertEquals(pos, gear.getPinionPosition());
    }

    @Test
    void testChangeSpeed() {
        // 入力値と予想値
        PinionGear gear = new PinionGear();
        double expectedSpeed = 2.2;

        // テスト実行
        gear.changeSpeed(expectedSpeed);

        // 判定と出力
        if (Math.abs(expectedSpeed - gear.speed) < 1e-9) {
            System.out.println("良好");
        } else {
            System.out.println("不良");
        }

        // アサーション
        assertEquals(expectedSpeed, gear.speed, 1e-9);
    }

    @Test
    void testGetters() {
        // 入力値と予想値
        Pen pen = new Pen(1.0, Color.BLACK, new Point2D.Double(0, 0));
        double speed = 3.3;
        double theta = 4.4;
        double alpha = 5.5;

        // テスト実行
        PinionGear gear = new PinionGear(pen, speed, theta, alpha);

        // 判定と出力
        if (Math.abs(speed - gear.speed) < 1e-9
                && Math.abs(theta - gear.getTheta()) < 1e-9
                && Math.abs(alpha - gear.getAlpha()) < 1e-9
                && pen.equals(gear.getPen())) {
            System.out.println("良好");
        } else {
            System.out.println("不良");
        }

        // アサーション
        assertEquals(speed, gear.speed, 1e-9);
        assertEquals(theta, gear.getTheta(), 1e-9);
        assertEquals(alpha, gear.getAlpha(), 1e-9);
        assertEquals(pen, gear.getPen());
    }
}
