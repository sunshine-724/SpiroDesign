package org.example.model;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.Serializable;

/**
 * スピログラフのピニオンギアを表すクラス。
 * ピニオンギアは、スピロギアの一種で、特定の位置と半径を持ち、回転して描画を生成する。
 * このクラスは、ピニオンギアの位置、半径、色の管理を行い、ペンとの連携を可能にする。
 */
public class PinionGear extends SpiroGear implements Serializable {
    private static final long serialVersionUID = 1L;

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
    public double thetaOffset; // 追加: 現在の中心角度のオフセット

    /**
     * ピニオンギアの角加速度。
     * ギアの回転速度の変化を表す。
     */
    public double alpha; // この変数名は誤解を招く可能性あり。ペン先のピニオンギアに対する角度として使用。

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
     * スピログラフの基本的なペン先のオフセット角度。
     * 通常、スピログラフではペン先はピニオンギアの中心から特定の距離に固定される。
     * この角度は、そのペン先のピニオンギアに対する初期位置を表す。
     */
    private static final double DEFAULT_PEN_OFFSET_ANGLE = 0.0; // 初期角度を0とする

    /**
     * スピログラフのペン先のピニオンギアに対する相対半径。
     * これが実際の「ペン先までの距離」になる。
     * これがないと、ピニオンギアの半径がそのままペン先の距離として使われてしまう。
     */
    private static final double DEFAULT_PEN_OFFSET_RADIUS = 25.0; // 例として25.0を設定

    // --- 追加: ペン先までの距離を動的に保持するフィールド ---
    private double penOffsetRadius = DEFAULT_PEN_OFFSET_RADIUS;

    // --- 追加: 内接/外接モードを保持するフラグ ---
    private boolean isInner = false; // trueなら内接、falseなら外接

    /**
     * ピニオンギアを作成するデフォルトコンストラクタ。
     * 初期位置、半径、色を設定する。
     */
    public PinionGear() {
        super(DEFAULT_POSITION, DEFAULT_RADIUS, DEFAULT_COLOR);
        this.speed = 1.0; // 変更: デフォルトの速度を1/10に設定 (10.0 から 1.0 へ)
        this.theta = 0.0; // 初期角度を設定
        this.thetaOffset = 0.0; // 追加
        this.alpha = DEFAULT_PEN_OFFSET_ANGLE; // ペン先のオフセット角度を設定

        // Penオブジェクトの生成と初期位置の設定例
        pen = new Pen(Pen.DEFAULT_PEN_SIZE, Pen.DEFAULT_COLOR, new Point2D.Double(0, 0));
    }

    /**
     * ピニオンギアを特定の位置、半径、色で作成するコンストラクタ。
     * このコンストラクタは現在使用されていないため、修正は控え、デフォルトコンストラクタに注力。
     *
     * @param pen   ピニオンギアに関連付けられたペンオブジェクト
     * @param speed 入力されるスピード
     * @param theta ピニオンギアの角度
     * @param alpha ピニオンギアの角加速度（ペン先の相対角度として使用）
     */
    public PinionGear(Pen pen, double speed, double theta, double alpha) {
        super(pen.getPosition(), pen.penSize, pen.color); // これはPinionGearではなくPenの属性を設定しているため不適切
        this.pen = pen;
        this.speed = speed;
        this.theta = theta;
        this.alpha = alpha; // ここはペン先のオフセット角度として使う
        this.penOffsetRadius = DEFAULT_PEN_OFFSET_RADIUS;
    }

    /**
     * ピニオンギアの位置を更新する。
     * スピログラフの軌跡を描画するための重要な計算を行う。
     * ペンの位置もこのメソッド内で計算され、更新される。
     *
     * @param time         時間の経過（ミリ秒単位）
     * @param spurRadius   スパーギアの半径
     * @param spurPosition スパーギアの中心位置（ワールド座標）
     */
    public void move(long time, Double spurRadius, Point2D.Double spurPosition) {
        // --- 追加: 変数の値を出力 ---
        System.out.println("PinionGear.move: 計算前 theta=" + theta);
        System.out.println("PinionGear.move: speed=" + speed + ", time=" + time + ", thetaOffset=" + thetaOffset);

        System.out.println("PinionGear.move: 計算後 theta=" + theta);

        // 公転半径の計算: 内接か外接かで異なる
        double revolutionRadius;
        if (isInner) {
            revolutionRadius = spurRadius - radius; // 内接 (ハイポサイクロイド)
        } else {
            revolutionRadius = spurRadius + radius; // 外接 (エピサイクロイド)
        }

        // ピニオンギアの中心の絶対座標を計算
        double pinionCenterX = spurPosition.x + revolutionRadius * Math.cos(-theta);
        double pinionCenterY = spurPosition.y + revolutionRadius * Math.sin(-theta);

        // ピニオンギアの現在の絶対位置を設定
        this.position.setLocation(pinionCenterX, pinionCenterY);

        // ペン先の絶対座標を計算
        // 自転角度の係数も内接か外接かで異なる
        double rotationFactor;
        if (isInner) {
            rotationFactor = (spurRadius / radius) - 1; // ハイポサイクロイド
        } else {
            rotationFactor = -((spurRadius / radius) + 1); // エピサイクロイド: 符号を反転
        }
        double rotationAngle = rotationFactor * theta;

        // ペン先の相対半径（ピニオンギアの中心からの距離）
        double penAbsoluteX = pinionCenterX + penOffsetRadius * Math.cos(rotationAngle + alpha);
        double penAbsoluteY = pinionCenterY + penOffsetRadius * Math.sin(rotationAngle + alpha);

        pen.setPosition(new Point2D.Double(penAbsoluteX, penAbsoluteY));
    }

    /**
     * ピニオンギアの位置を設定する。
     * 親クラスのsetPositionメソッドをオーバーライドする。
     *
     * @param position 新しい位置
     */
    @Override // SpiroGearのsetPositionをオーバーライド
    public void setPosition(Point2D.Double position) {
        this.position = position;
    }

    /**
     * ピニオンギアの半径を変更する。
     * SpiroGearのchangeRadiusを呼び出す。
     *
     * @param radius 新しい半径
     */
    @Override // SpiroGearのchangeRadiusをオーバーライド
    public void changeRadius(double radius) {
        super.changeRadius(radius); // 親クラスのメソッドを呼び出す
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
     * @return ピニオンギアの現在の座標
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
     * ピニオンギアの角加速度を取得する。（ここではペン先の相対角度として使用）
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

    /**
     * ピニオンギアに関連付けられたペンの位置を設定する。
     * このメソッドは、PenオブジェクトのsetPositionを呼び出す。
     *
     * @param position 新しいペンの位置（絶対座標）
     */
    public void setPenPosition(Point2D.Double position) {
        // --- 追加: ペン先までの距離を逆算して保存 ---
        if (this.position != null) {
            double dx = position.x - this.position.x;
            double dy = position.y - this.position.y;
            this.penOffsetRadius = Math.hypot(dx, dy);
        }
        pen.setPosition(position);
    }

    // --- 追加: ペン先までの距離のgetter/setter ---
    public double getPenOffsetRadius() {
        return penOffsetRadius;
    }
    public void setPenOffsetRadius(double r) {
        this.penOffsetRadius = r;
    }

    /**
     * ピニオンギアの速度を取得する。
     *
     * @return ピニオンギアの速度
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * thetaOffsetを設定する
     *
     * @param offset 新しいthetaOffset
     */
    public void setThetaOffset(double offset) {
        this.thetaOffset = offset;
    }

    /**
     * thetaOffsetを取得する
     *
     * @return thetaOffset
     */
    public double getThetaOffset() {
        return thetaOffset;
    }

    /**
     * ピニオンギアがスパーギアの内側で回転しているか設定する。
     * @param inner 内側で回転している場合はtrue
     */
    public void setInner(boolean inner) {
        isInner = inner;
    }

    /**
     * ピニオンギアがスパーギアの内側で回転しているか取得する。
     * @return 内側で回転している場合はtrue
     */
    public boolean isInner() {
        return isInner;
    }
}

