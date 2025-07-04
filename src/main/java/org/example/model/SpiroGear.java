package org.example.model;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.Serializable;

/**
 * スピログラフのスパーギアとスピロギアの親クラスを表す。
 * このクラスは、ギアの位置、半径、色を管理し、変更できるメソッドを提供する。
 */
public class SpiroGear implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * ギアの位置を表す2D座標。
     */
    public Point2D.Double position;

    /**
     * ギアの半径。
     */
    public double radius;

    /**
     * ギアの色。
     */
    public Color color;

    /**
     * スピログラフのギアを作成するコンストラクタ。
     *
     * @param position ギアの位置
     * @param radius   ギアの半径
     * @param color    ギアの色
     */
    public SpiroGear(Point2D.Double position, double radius, Color color) {
        this.position = position;
        this.radius = radius;
        this.color = color;
    }

    /**
     * ギアの半径を変更するメソッド。
     *
     * @param radius 新しい半径
     */
    public void changeRadius(double radius) {
        this.radius = radius;
    }

    /**
     * ギアの色を変更するメソッド。
     *
     * @param color 新しい色
     */
    public void changeColor(Color color) {
        this.color = color;
    }

    /**
     * ギアの位置を設定するメソッド。
     * これを親クラスで定義することで、子クラスで@Overrideを正しく使用できる。
     * @param position 新しい位置
     */
    public void setPosition(Point2D.Double position) {
        this.position = position;
    }
}
