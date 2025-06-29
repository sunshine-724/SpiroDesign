package org.example.model;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.Serializable; // 追加: Serializableをインポート

/**
 * スピログラフを描画するペンを表すクラス。
 * ペンのサイズ、色、位置を管理し、ピニオンギアの動きに応じて移動する機能を提供する。
 */
public class Pen implements Serializable { // Serializableを実装
    private static final long serialVersionUID = 1L; // serialVersionUIDを追加

    /**
     * ペンのサイズを表す変数。
     */
    public double penSize;

    /**
     * ペンの色を表す変数。
     */
    public Color color;

    /**
     * ペンの位置を表す2D座標。
     * ピニオンギアの位置に基づいて更新される。
     */
    private Point2D.Double position;

    /**
     * デフォルトのペンサイズ。
     */
    public static final double DEFAULT_PEN_SIZE = 2.0; // medium相当のサイズに変更

    /**
     * デフォルトのペンの色。
     */
    public static final Color DEFAULT_COLOR = Color.BLACK;

    /**
     * ペンを作成するデフォルトコンストラクタ。
     * デフォルトのペンサイズと色を設定する。
     */
    public Pen() {
        this.penSize = DEFAULT_PEN_SIZE;
        this.color = DEFAULT_COLOR;
        this.position = new Point2D.Double(0, 0);
    }

    /**
     * ペンを特定のサイズ、色、位置で作成するコンストラクタ。
     *
     * @param penSize  ペンのサイズ
     * @param color    ペンの色
     * @param position ペンの位置
     */
    public Pen(double penSize, Color color, Point2D.Double position) {
        this.penSize = penSize;
        this.color = color;
        this.position = position;
    }

    /**
     * ピニオンギアの動きに応じてペンを移動させるメソッド。
     * ペンの位置は、ピニオンギアの中心からの相対位置として計算されるべきである。
     *
     * @param pinionPosition ピニオンギアの絶対位置（中心座標）
     * @param penOffsetRadius ペン先のピニオンギアに対する相対半径
     * @param rotationAngle  ピニオンギアの自転角度
     * @param alpha          ペン先のオフセット角度（ピニオンギア中心からの相対角度）
     */
    public void setPenPosition(Point2D.Double pinionPosition, Double penOffsetRadius, Double rotationAngle, Double alpha) {
        System.out.println("setPenPosition called");
        // デバッグ用: 計算値を出力
        System.out.println("pinionPosition.x = " + pinionPosition.x);
        System.out.println("pinionPosition.y = " + pinionPosition.y);
        System.out.println("penOffsetRadius = " + penOffsetRadius);
        System.out.println("rotationAngle = " + rotationAngle);
        System.out.println("alpha = " + alpha);

        double newX = pinionPosition.x + penOffsetRadius * Math.cos(rotationAngle + alpha);
        double newY = pinionPosition.y + penOffsetRadius * Math.sin(rotationAngle + alpha);
        System.out.println("newX = " + newX);
        System.out.println("newY = " + newY);
        this.position.setLocation(newX, newY);
    }


    /**
     * ペンの位置を設定するメソッド。
     *
     * @param position 新しい位置
     */
    public void setPosition(Point2D.Double position) {
        System.out.println("Pen.setPosition called: " + position);
        this.position = position;
    }

    /**
     * ペンの位置を取得するメソッド。
     *
     * @return ペンの位置
     */
    public Point2D.Double getPosition() {
        return position;
    }

    /**
     * ペンの色を変更するメソッド。
     *
     * @param color 新しい色
     */
    public void changeColor(Color color) {
        this.color = color;
    }

    /**
     * ペンのサイズを変更するメソッド。
     *
     * @param size 新しいサイズ
     */
    public void setPenSize(double size) {
        this.penSize = size;
    }

    /**
     * ペンのサイズを取得するメソッド。
     *
     * @return ペンのサイズ
     */
    public double getPenSize() {
        return penSize;
    }

    /**
     * ペンの色を取得するメソッド。
     *
     * @return ペンの色
     */
    public Color getColor() {
        return color;
    }
}
