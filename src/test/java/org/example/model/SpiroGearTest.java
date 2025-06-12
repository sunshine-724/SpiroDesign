package org.example.model;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.geom.Point2D;

import static org.junit.jupiter.api.Assertions.*;

public class SpiroGearTest {

    @Test
    void testConstructor() {
        // 入力値と予想される値を先に決める
        Point2D.Double expectedPos = new Point2D.Double(100, 200);
        double expectedRadius = 50.0;
        Color expectedColor = Color.RED;

        // テスト対象のインスタンス生成
        SpiroGear gear = new SpiroGear(expectedPos, expectedRadius, expectedColor);

        // 予想通りなら「良好」と出力
        boolean result = expectedPos.equals(gear.position)
                && Math.abs(expectedRadius - gear.radius) < 1e-9
                && expectedColor.equals(gear.color);

        if (result) {
            System.out.println("良好");
        } else {
            System.out.println("不良");
        }

        // 通常のアサーションも残しておく
        assertEquals(expectedPos, gear.position);
        assertEquals(expectedRadius, gear.radius, 1e-9);
        assertEquals(expectedColor, gear.color);
    }

    @Test
    void testChangeRadius() {
        // 入力値と予想される値を先に決める
        double initialRadius = 30.0;
        double newRadius = 45.5;
        SpiroGear gear = new SpiroGear(new Point2D.Double(0, 0), initialRadius, Color.BLUE);

        // 半径を変更
        gear.changeRadius(newRadius);

        // 予想通りなら「良好」と出力
        if (Math.abs(newRadius - gear.radius) < 1e-9) {
            System.out.println("良好");
        } else {
            System.out.println("不良");
        }

        // 通常のアサーションも残しておく
        assertEquals(newRadius, gear.radius, 1e-9);
    }

    @Test
    void testChangeColor() {
        // 入力値と予想される値を先に決める
        Color initialColor = Color.BLUE;
        Color newColor = Color.GREEN;
        SpiroGear gear = new SpiroGear(new Point2D.Double(0, 0), 30.0, initialColor);

        // 色を変更
        gear.changeColor(newColor);

        // 予想通りなら「良好」と出力
        if (newColor.equals(gear.color)) {
            System.out.println("良好");
        } else {
            System.out.println("不良");
        }

        // 通常のアサーションも残しておく
        assertEquals(newColor, gear.color);
    }
}
