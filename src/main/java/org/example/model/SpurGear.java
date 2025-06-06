package org.example.model;

import java.awt.Color;
import java.awt.geom.Point2D;

/**
 * スピログラフのスパーギアを表すクラス。
 * スパーギアは、スピロギアの一種で、特定の位置と半径を持つ。
 * 
 * このクラスは、スパーギアの位置、半径、色を管理し、変更できるメソッドを提供する。
 */
public class SpurGear extends SpiroGear {
    /**
     * スパーギアの初期の位置を表す2D座標。
     */
    private static final Point2D.Double DEFAULT_POSITION = new Point2D.Double(400, 400);

    /**
     * スパーギアの初期の半径。
     */
    private static final double DEFAULT_RADIUS = 100.0;

    /**
     * スパーギアの初期の色。
     */
    private static final Color DEFAULT_COLOR = Color.BLACK;

    /**
     * スパーギアを作成するデフォルトコンストラクタ。
     * 初期位置、半径、色を設定する。
     */
    public SpurGear() {
        super(DEFAULT_POSITION, DEFAULT_RADIUS, DEFAULT_COLOR);
    }

    /**
     * スパーギアを特定の位置、半径、色で作成するコンストラクタ。
     * 
     * @param position ギアの位置
     * @param radius   ギアの半径
     * @param color    ギアの色
     */
    public SpurGear(Point2D.Double position, double radius, Color color) {
        super(position, radius, color);
    }

    /**
     * スパーギアの位置を返すメソッド。
     * 
     * @param position 新しい位置
     */
    public double getSpurRadius() {
        return radius;
    }

    /**
     * スパーギアの位置を返すメソッド。
     * 
     * @return ギアの位置
     */
    public Point2D.Double getSpurPosition() {
        return position;
    }
}
