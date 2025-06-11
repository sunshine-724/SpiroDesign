package org.example.model;

import java.awt.Color;
import java.awt.geom.Point2D;

/**
 * スピログラフのピニオンギアを表すクラス。
 * 
 * ピニオンギアは、スピロギアの一種で、特定の位置と半径を持ち、回転して描画を生成する。
 * このクラスは、ピニオンギアの位置、半径、色の管理を行い、ペンとの連携を可能にする。
 * 
 */
public class PinionGear extends SpiroGear {

    /**
     * ピニオンギアのペンを表すオブジェクト。
     * ペンは、ピニオンギアの位置に基づいて描画を行う。
     */
    private Pen pen;

    /**
     * 入力されるスピード
     */
    public double speed;

    /**
     * ピニオンギアの角度。
     * ギアの回転状態を表す。
     */
    public double theta;

    /**
     * ピニオンギアの角加速度。
     * ギアの回転速度の変化を表す。
     */
    public double alpha;

    /**
     * ピニオンギアの初期位置を表す2D座標。
     * デフォルトでは、(470, 400)に設定される。
     */
    private static final Point2D.Double DEFAULT_POSITION = new Point2D.Double(470, 400);

    /**
     * ピニオンギアの初期半径。
     * デフォルトでは、30.0に設定される。
     */
    private static final double DEFAULT_RADIUS = 30.0;

    /**
     * ピニオンギアの初期色。
     * デフォルトでは、黒色に設定される。
     */
    private static final Color DEFAULT_COLOR = Color.BLACK;

    /**
     * ピニオンギアを作成するデフォルトコンストラクタ。
     * 初期位置、半径、色を設定する。
     */
    public PinionGear() {
        super(DEFAULT_POSITION, DEFAULT_RADIUS, DEFAULT_COLOR);
        // Penオブジェクトの生成と初期位置の設定例
        pen = new Pen();
        pen.setPosition(new java.awt.geom.Point2D.Double(0, 0));
    }

    /**
     * ピニオンギアを特定の位置、半径、色で作成するコンストラクタ。
     * 
     * @param pen   ピニオンギアに関連付けられたペンオブジェクト
     * @param speed 入力されるスピード
     * @param theta ピニオンギアの角度
     * @param alpha ピニオンギアの角加速度
     */
    public PinionGear(Pen pen, double speed, double theta, double alpha) {
        super(pen.getPosition(), pen.penSize, pen.color);
        this.pen = pen;
        this.speed = speed;
        this.theta = theta;
        this.alpha = alpha;
    }

    /**
     * ピニオンギアの位置を更新する
     * 
     * @param time         時間の経過（ミリ秒単位）
     * @param spurRadius   スパーギアの半径
     * @param spurPosition スパーギアの中心位置
     */
    public void move(long time, Double spurRadius, Point2D.Double spurPosition) {
        theta = speed * time;
        double centerX = spurPosition.x + (spurRadius - radius) * Math.cos(-theta);
        double centerY = spurPosition.y + (spurRadius - radius) * Math.sin(-theta);

        this.position.setLocation(centerX, centerY);
        pen.move(this.position, this.radius, this.theta, this.alpha);
    }

    public void setPosition(Point2D.Double position) {
        this.position = position;
    }

    /**
     * ピニオンギアの速度を変更する。
     * 
     * @param speed 新しい速度
     */
    public void changeSpeed(double speed) {
        this.speed = speed;
    }

    /**
     * ピニオンギアの座標を取得する。
     * 
     * @param theta ピニオンギアの座標
     */
    public Point2D.Double getPinionPosition() {
        return position;
    }

    /**
     * ピニオンギアの半径を取得する。
     * 
     * @return ピニオンギアの半径
     */
    public double getPinionRadius() {
        return radius;
    }

    /**
     * スパーギアから中心から見たピニオンギアの角度を取得する。
     * 
     * @return スパーギアから中心から見たピニオンギアの角度
     */
    public double getTheta() {
        return theta;
    }

    /**
     * ピニオンギアの角加速度を取得する。
     * 
     * @return ピニオンギアの角加速度
     */
    public double getAlpha() {
        return alpha;
    }

    /**
     * ピニオンギアに関連付けられたペンを取得する。
     * 
     * @return ピニオンギアのペンオブジェクト
     */
    public Pen getPen() {
        return pen;
    }
}
