package org.example.model;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.*;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Timer;

import org.example.spiroIO.SpiroIO;
import org.example.view.View;
import org.example.lib.Pair;
import org.example.lib.PathSegment;

/*
 * Modelクラスは、Spiro.appのデータを管理するクラス。
 */

public class Model implements Serializable { // Serializableを実装
    private static final long serialVersionUID = 1L; // serialVersionUIDを追加

    /**
     * スパーギアとピニオンギアのインスタンスを保持する。
     * スパーギアは大きな円で、ピニオンギアは小さな円。
     * ピニオンギアはスパーギアの周りを回転し、ペンを動かしてスピロデザインを描く。
     */
    private SpurGear spurGear;
    private PinionGear pinionGear;

    /**
     * SpiroIOは、スピロデザインのデータを読み書きするためのクラス。
     * ファイルからスパーギア、ピニオンギアのデータを読み込んだり、保存したりする。
     * SpiroIOはModelの一部ではないため、シリアライズしない
     */
    private transient SpiroIO spiroIO; // transientとしてマーク

    /**
     * Viewのリストを保持する。
     * ViewはGUIコンポーネントであり、シリアライズすべきではないためtransientとする。
     */
    private transient List<View> views = new ArrayList<>(); // transientとしてマーク

    /**
     * タイマーは一定の間隔でModelのデータを更新し、Viewに通知し、スピロデザインの描画をアニメーションするために使用される。
     * TimerはGUI関連のオブジェクトであり、シリアライズすべきではないためtransientとする。
     */
    private transient Timer timer; // transientとしてマーク

    // 軌跡をPathSegmentのリストで管理するように変更
    private List<PathSegment> pathSegments; // locus から変更
    private transient PathSegment currentPathSegment; // 現在描画中のセグメント (transientとしてマーク)

    /** 描画開始時刻(ミリ秒) */
    private long startTime;

    /** 一時停止時間(ミリ秒) */
    private long pauseTime;

    /**
     * 1ミリ秒あたりのフレーム数
     * 60 FPSで描画するため、1フレームあたりの時間は1000 / 60ミリ秒
     */
    private static final int FRAME_PER_MILLISECOND = 1000 / 60; // 60 FPS

    // --- 追加: ピニオンギアの状態保存用フィールド ---
    private Point2D.Double savedPinionCenter = null;
    private double savedTheta = 0.0;
    private double savedAlpha = 0.0;

    /**
     * Modelクラスのコンストラクタ。
     * スパーギアとピニオンギアのインスタンスを初期化し、SpiroIOを初期化する。
     * タイマーを設定し、一定の間隔でデータを更新するようにする。
     * ただしタイマーは非同期で動作し、他の処理をブロックしない。
     */
    public Model() {
        // コンストラクタでギアを初期化する代わりに、resetGears()を呼び出す
        initializeTransientFields(); // まずtransientフィールドを初期化
        resetGears(); // ギアとパスを初期化
    }

    /**
     * transientフィールドを初期化するヘルパーメソッド。
     * デシリアライズ後にも呼び出される必要がある。
     */
    private void initializeTransientFields() {
        spiroIO = new SpiroIO();
        views = new ArrayList<>(); // 再度初期化
        timer = new Timer(FRAME_PER_MILLISECOND, e -> {
            updateData(); // データ更新
            // Modelが更新されたらViewに再描画を通知
            // ModelはViewに依存しないが、Modelの状態が変更されたことをViewに伝える必要がある
            for (View view : views) {
                view.repaint();
            }
        });
    }

    /**
     * オブジェクトのデシリアライズ後に呼び出されるメソッド。
     * transientとしてマークされたフィールドを再初期化する。
     */
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject(); // デフォルトのデシリアライズ処理を実行
        initializeTransientFields(); // transientフィールドを再初期化
        // ロードされたパスセグメントに基づいてcurrentPathSegmentを再構築
        if (pathSegments != null && !pathSegments.isEmpty()) {
            // 最後のセグメントを現在のペン色で新しいセグメントとして開始
            // ロードされたセグメントの最後の点を引き継ぐ
            PathSegment lastLoadedSegment = pathSegments.get(pathSegments.size() - 1);
            if (!lastLoadedSegment.getPoints().isEmpty()) {
                currentPathSegment = new PathSegment(
                        lastLoadedSegment.getColor(),
                        lastLoadedSegment.getPenSize(),
                        new ArrayList<>(lastLoadedSegment.getPoints()));
            } else {
                currentPathSegment = new PathSegment(pinionGear.getPen().getColor());
            }
            // ロードされたリストから最後のセグメントを削除し、currentPathSegmentで拡張できるようにする
            pathSegments.remove(pathSegments.size() - 1);
        } else {
            // ロードされたパスがない場合、新しいセグメントを開始
            currentPathSegment = new PathSegment(pinionGear.getPen().getColor());
            pathSegments = new ArrayList<>(); // pathSegmentsも初期化
        }

        // ロードされたModelの状態を反映
        stop(); // ロード後はアニメーションを停止状態にする
        pauseTime = 0; // ロード後は一時停止時間をリセット
        startTime = System.currentTimeMillis(); // 開始時刻もリセットして、再開時に正しく動作するようにする
    }

    /**
     * スピロデザインのの描画を開始する。
     * タイマーを開始または再開し、描画開始時刻を記録する。
     */
    public void start() {
        System.out.println("start");
        if (pathSegments == null) {
            pathSegments = new ArrayList<>();
        }
        if (currentPathSegment == null) {
            currentPathSegment = new PathSegment(pinionGear.getPen().getColor());
        }
        if (timer.isRunning()) {
            return; // 多重実行防止
        }
        // --- 修正: 逆算ロジックを削除し、保存値を復元 ---
        new javax.swing.Timer(100, e -> {
            // 保存されたピニオンギア状態があれば復元
            if (savedPinionCenter != null) {
                pinionGear.setPosition(new Point2D.Double(savedPinionCenter.x, savedPinionCenter.y));
                pinionGear.theta = savedTheta;
                pinionGear.alpha = savedAlpha;
            }
            if (!timer.isRunning()) {
                timer.start();
                startTime = System.currentTimeMillis() - pauseTime;
                pauseTime = 0;
            }
            ((javax.swing.Timer)e.getSource()).stop();
        }).start();
    }

    /**
     * スピロデザインの描画を停止する。
     * タイマーを停止し、現在の時間を記録する。
     * これにより、再開時に前回の続きから描画を再開できる。
     */
    public void stop() {
        System.out.println("stop");
        // ペンの位置を出力
        Point2D.Double penPos = getPenPosition();
        System.out.println("Pen position after stop: " + penPos);
        if (!timer.isRunning()) {
            return; // 多重実行防止
        }
        timer.stop();
        this.pauseTime = 0;
        this.startTime = System.currentTimeMillis();
        resetSpirographTime();

        // --- 追加: ピニオンギアの状態を保存 ---
        savedPinionCenter = pinionGear.getPinionPosition() != null
            ? new Point2D.Double(pinionGear.getPinionPosition().x, pinionGear.getPinionPosition().y)
            : null;
        savedTheta = pinionGear.theta;
        savedAlpha = pinionGear.alpha;
    }

    /**
     * ピニオンギア中心ドラッグ後やstop後に呼び出し、thetaOffsetを現在の中心角度にセットする
     */
    public void resetSpirographTime() {
        // 経過時間リセット
        this.pauseTime = 0;
        this.startTime = System.currentTimeMillis();
        // thetaOffsetを現在の中心角度にセット
        Point2D.Double spurCenter = spurGear.getSpurPosition();
        Point2D.Double pinionCenter = pinionGear.getPinionPosition();
        if (spurCenter != null && pinionCenter != null) {
            double thetaOffset = -Math.atan2(pinionCenter.y - spurCenter.y, pinionCenter.x - spurCenter.x);
            pinionGear.setThetaOffset(thetaOffset);
        }
    }

    /**
     * ペン色を変えずに新しいセグメントを開始する（ピニオンギア中心移動時など用）
     */
    public void startNewPathSegment() {
        if (currentPathSegment != null && !currentPathSegment.getPoints().isEmpty()) {
            pathSegments.add(currentPathSegment);
        }
        currentPathSegment = new PathSegment(getPenColor(), getPenSize());
        Point2D.Double penPos = getPenPosition();
        if (penPos != null) {
            currentPathSegment.addPoint(penPos);
        }
    }

    /**
     * ペンの色を変更する。
     * 色を変更する際に、現在の描画セグメントを終了し、新しい色のセグメントを開始する。
     * @param newColor 新しいペンの色
     */
    public void changePenColor(Color newColor) {
        if (currentPathSegment != null && !currentPathSegment.getPoints().isEmpty()
                && !currentPathSegment.getColor().equals(newColor)) {
            pathSegments.add(currentPathSegment);
        }
        pinionGear.getPen().changeColor(newColor);
        currentPathSegment = new PathSegment(newColor, getPenSize());
    }

    /**
     * ペンサイズを変更する。
     * サイズを変更する際に、現在の描画セグメントを終了し、新しいサイズのセグメントを開始する。
     * @param newSize 新しいペンサイズ
     */
    public void changePenSize(double newSize) {
        if (currentPathSegment != null && !currentPathSegment.getPoints().isEmpty()
                && currentPathSegment.getPenSize() != newSize) {
            pathSegments.add(currentPathSegment);
        }
        pinionGear.getPen().setPenSize(newSize);
        currentPathSegment = new PathSegment(getPenColor(), newSize);
    }

    /**
     * Modelのデータを更新する。
     * このメソッドは、ピニオンギアの位置とペンの位置を更新し、描画する点を軌跡の点としてリストに追加する。
     */
    private void updateData() {
        long currentTime = System.currentTimeMillis() - startTime;

        // ピニオンギアの位置とペンの位置を更新
        pinionGear.move(currentTime, spurGear.getSpurRadius(), spurGear.getSpurPosition());

        // ペンの位置を取得
        Point2D.Double penPosition = pinionGear.getPen().getPosition();

        // currentPathSegmentがnullの場合は初期化（通常はresetGearsやreadObjectで初期化される）
        if (currentPathSegment == null) {
            currentPathSegment = new PathSegment(pinionGear.getPen().getColor());
            pathSegments.add(currentPathSegment);
        }
        currentPathSegment.addPoint(penPosition);
    }

    /**
     * ロード後Viewにデータの更新を通知する。
     *
     * @param loadedPathSegments 軌跡のデータ (PathSegmentのリスト)
     * @param penColor           ペンの色
     * @param penSize            ペンのサイズ
     */
    private void notifyViewsLoading(List<PathSegment> loadedPathSegments, Color penColor, double penSize) { // 引数を変更
        for (View view : views) {
            view.setLocusData(loadedPathSegments, penColor, penSize); // ViewのsetLocusDataも変更が必要
        }
    }

    /**
     * Viewに半径の変更があったことを通知する。
     *
     * @param newRadius 新しいスパーギアの半径
     */
    private void notifiyViewsChangeAndPinionRadius(double newRadius) {
        for (View view : views) {
            view.changeSpurAndPinionRadius(newRadius);
        }
    }

    /**
     * Viewにデータの更新を通知するためにリスナーを追加する。(Observerパターン)
     *
     * @param view 通知を受け取るViewのインスタンス
     */
    public void addView(View view) {
        // viewsがデシリアライズ後にnullになる可能性があるので、ここで初期化を確認
        if (this.views == null) {
            this.views = new ArrayList<>();
        }
        views.add(view);
    }

    /**
     * 軌跡の取得メソッドを変更
     *
     * @return PathSegmentのリストと現在のPathSegmentを結合したリスト
     */
    public List<PathSegment> getPathSegments() { // getLocus() から変更
        List<PathSegment> allSegments = new ArrayList<>(pathSegments);
        if (currentPathSegment != null && !currentPathSegment.getPoints().isEmpty()) {
            allSegments.add(new PathSegment(
                currentPathSegment.getColor(),
                currentPathSegment.getPenSize(),
                new ArrayList<>(currentPathSegment.getPoints())
            ));
        }
        return allSegments;
    }

    /**
     * ピニオンギアのスピードを変更する。
     *
     * @param speed 変更したいピニオンギアのスピード。
     */
    public void changeSpeed(Double speed) {
        pinionGear.changeSpeed(speed);
    }

    /**
     * ピニオンギアの位置を設定する。
     *
     * @param position ピニオンギアの位置(絶対座標)
     */
    public void setPinionGearPosition(Point2D position) {
        Point2D.Double newPosition = new Point2D.Double(position.getX(), position.getY());
        pinionGear.setPosition(newPosition);
    }

    /**
     * ピニオンギアの位置を取得する。
     *
     * @return ピニオンギアの位置(絶対座標)
     */
    public Point2D.Double getPinionGearPosition() {
        return pinionGear.getPinionPosition();
    }

    /**
     * ピニオンギアの半径を変更する。
     *
     * @param radius 変更したいピニオンギアの半径
     */
    public void changePinionGearRadius(Double radius) {
        pinionGear.changeRadius(radius);
    }

    /**
     * ピニオンギアの半径を取得する。
     *
     * @return ピニオンギアの半径
     */
    public Double getPinionGearRadius() {
        return pinionGear.getPinionRadius();
    }

    /**
     * ピニオンギアの速度を取得する。
     *
     * @return ピニオンギアの速度
     */
    public double getPinionGearSpeed() {
        return pinionGear.getSpeed();
    }

    /**
     * スパーギアの半径を変更する。
     *
     * @param radius スパーギアの半径
     */
    public void changeSpurGearRadius(Double radius) {
        spurGear.changeRadius(radius);
        // notifiyViewsChangeAndPinionRadius(radius); // ← 削除
    }

    /**
     * スパーギアの半径を取得する。
     *
     * @return スパーギアの半径
     */
    public Double getSpurGearRadius() {
        return spurGear.getSpurRadius();
    }

    /**
     * スパーギアの中心位置を取得する。
     * スパーギアの中心位置は、スピロデザインの描画領域の中心を基準にした絶対座標で返される。
     *
     * @return スパーギアの中心位置(絶対座標)
     */
    public Point2D.Double getSpurGearPosition() {
        return spurGear.getSpurPosition();
    }

    /**
     * スパーギアとピニオンギアを指定された座標に移動する。
     * このメソッドは、スパーギアとピニオンギアの位置を指定された座標に移動させる。
     * ペンの位置もピニオンギアが移動すると同時に更新される。
     *
     * @param dx 移動量のX座標
     * @param dy 移動量のY座標
     */
    public void moveSpurGearBy(double dx, double dy) {
        spurGear.setPosition(new Point2D.Double(spurGear.getSpurPosition().x + dx,
                spurGear.getSpurPosition().y + dy));

        pinionGear.setPosition(new Point2D.Double(pinionGear.getPinionPosition().x + dx,
                pinionGear.getPinionPosition().y + dy));
    }

    /**
     * ピニオンギアのペンの位置を指定された座標に移動する。
     *
     * @param dx 移動量のX座標
     * @param dy 移動量のY座標
     */
    public void movePenBy(double dx, double dy) {
        Point2D.Double penPosition = pinionGear.getPen().getPosition();

        if (penPosition != null) {
            double newX = penPosition.getX() + dx;
            double newY = penPosition.getY() + dy;

            penPosition = new Point2D.Double(newX, newY);
            pinionGear.setPenPosition(penPosition);
        }
    }

    /**
     * スパーギアの半径を設定する。
     *
     * @param radius スパーギアの半径
     */
    public void setSpurRadius(double radius) {
        spurGear.changeRadius(radius);
        // notifiyViewsChangeAndPinionRadius(radius); // ← 削除
    }

    /**
     * ピニオンギアのペンの位置を取得する。
     * ペンの位置は、ピニオンギアの中心位置を基準にした相対座標から絶対座標に変換されて返される。
     *
     * @return ピニオンギアのペンの位置(絶対座標)
     */
    public Point2D.Double getPenPosition() {
        return pinionGear.getPen().getPosition();
    }

    /**
     * ピニオンギアのペンの色を取得する。
     *
     * @return ピニオンギアのペンの色
     */
    public Color getPenColor() {
        return pinionGear.getPen().getColor();
    }

    /**
     * ピニオンギアのペンの位置を設定する。
     *
     * @param position ピニオンギアのペンの位置(絶対座標)
     */
    public void setPenPosition(Point2D.Double position) {
        System.out.println("Model.setPenPosition called: " + position);
        // 現在のセグメントが空でなければ追加
        if (currentPathSegment != null && !currentPathSegment.getPoints().isEmpty()) {
            pathSegments.add(currentPathSegment);
        }
        pinionGear.setPenPosition(position);
        // 新しいセグメントを開始
        currentPathSegment = new PathSegment(pinionGear.getPen().getColor());
        currentPathSegment.addPoint(position);
    }

    /**
     * ピニオンギアのペンの位置を設定する。
     *
     * @param pos ペンの位置
     */
    public void setPenPosition(Point2D pos) {
        Point2D.Double newPos = new Point2D.Double(pos.getX(), pos.getY());
        // 追加: 再配置座標を出力
        System.out.println("Pen position set by click: " + newPos);
        // 現在のセグメントが空でなければ追加
        if (currentPathSegment != null && !currentPathSegment.getPoints().isEmpty()) {
            pathSegments.add(currentPathSegment);
        }
        pinionGear.setPenPosition(newPos);
        // --- ここから逆算ロジック追加 ---
        // 幾何学的逆算: 新しいペン位置からalphaを計算
        Point2D.Double spurCenter = spurGear.getSpurPosition();
        Point2D.Double pinionCenter = pinionGear.getPinionPosition();
        double spurRadius = spurGear.getSpurRadius();
        double pinionRadius = pinionGear.getPinionRadius();
        double penOffset = 25.0; // PinionGearのDEFAULT_PEN_OFFSET_RADIUSと合わせる

        if (spurCenter != null && pinionCenter != null) {
            // 1. ピニオンギア中心角度thetaを計算
            double dx = pinionCenter.x - spurCenter.x;
            double dy = pinionCenter.y - spurCenter.y;
            double theta = -Math.atan2(dy, dx); // 公転角度

            // 2. ピニオンギアの自転角度を計算
            double rotationAngle = (spurRadius / pinionRadius) * theta;

            // 3. ペンの相対角度alphaを逆算
            double px = newPos.x - pinionCenter.x;
            double py = newPos.y - pinionCenter.y;
            double alpha = Math.atan2(py, px) - rotationAngle;

            // 4. ピニオンギアの内部状態を更新
            pinionGear.theta = theta;
            pinionGear.alpha = alpha;

            // 5. savedAlphaも即時更新
            savedAlpha = alpha;
            savedTheta = theta;
            savedPinionCenter = new Point2D.Double(pinionCenter.x, pinionCenter.y);

            System.out.println("Reverse-calculated theta=" + theta + ", alpha=" + alpha);
        }
        // --- ここまで逆算ロジック追加 ---

        // 新しいセグメントを開始
        currentPathSegment = new PathSegment(pinionGear.getPen().getColor());
        currentPathSegment.addPoint(newPos);
    }

    /**
     * ピニオンギアのペンのサイズを取得する。
     *
     * @return ピニオンギアのペンのサイズ
     */
    public double getPenSize() {
        return pinionGear.getPen().getPenSize();
    }

    /**
     * デザインの描画開始時刻を取得する。
     *
     * @return スピロデザインの開始時刻(ミリ秒)
     */
    public long getSpirographStartTime() {
        return startTime;
    }

    /**
     * スピログラフの現在時刻を取得する。
     *
     * @return スピログラフの現在時刻(ミリ秒)
     */
    public long getSpirographCurrentTime() {
        return System.currentTimeMillis();
    }

    /**
     * マウスがクリックされた時の処理を行う。
     * このメソッドは、マウスの位置を引数として受け取り、クリックイベントに応じた処理を行う。
     * 例えば、ピニオンギアの位置、半径の変更だったり、スパーギアの位置、半径の変更だったりすることができる。
     *
     * @param position マウスの位置
     */
    public void mouseClicked(Point position) {
    }

    /**
     * マウスが押された時の処理を行う。
     * このメソッドはドラッグイベントの開始時に呼び出されることを想定しており、主にドラッグの開始地点の設定を行う。
     * 例えば、ピニオンギアの位置、半径の変更だったり、スパーギアの位置、半径の変更だったりすることができる。
     *
     * @param position マウスの位置
     */
    public void mousePressed(Point position) {
    }

    /**
     * マウスが離された時の処理を行う。
     * このメソッドは、マウスの位置を引数として受け取り、離上イベントに応じた処理を行います。
     * 例えば、ピニオンギアの位置を設定したり、スパーギアの半径を変更したりすることができる。
     *
     * @param position マウスの位置
     */
    public void mouseReleased(Point position) {
    }

    /**
     * マウスがドラッグされた時の処理を行う。
     * このメソッドは、マウスの位置を引数として受け取り、ドラッグイベントに応じた処理を行う。
     * 例えば、ピニオンギアの位置を更新したり、スパーギアの半径を変更したりすることができる。
     *
     * @param position マウスの位置
     */
    public void mouseDragged(Point position) {

    }

    /**
     * Loadボタンが押された時の処理を行う。
     * FileChooserなどで選択されたファイルを引数として受け取り、
     * そのファイルからスピロデザインのデータを読み込む。
     * 読み込みが成功した場合、Modelのデータを更新し、Viewに通知する。
     *
     * @param file 読み込み対象のファイル
     */
    public void pressLoadButton(File file) {
        if (file != null) {
            loadData(file);
        }
    }

    /**
     * Saveボタンが押された時の処理を行う。
     * FileChooserなどで選択されたファイルを引数として受け取り、
     * そのファイルにスピロデザインのデータを保存する。
     *
     * @param file 保存先のファイル
     * @param pen  ペンの情報
     */
    public void pressSaveButton(File file, Pen pen) {
        saveData(file, this, pen);
    }

    /**
     * 指定されたファイルからスピロデザインのデータを読み込む。
     * 読み込みが成功した場合、軌跡とペンの情報を読み込みViewに通知する。
     * 読み込みに失敗した場合は、エラーメッセージを表示する。
     *
     * @param file 読み込み対象のファイル
     * @return 読み込みに成功した場合はtrue、失敗した場合はfalseを返す。
     */
    public Boolean loadData(File file) {
        try {
            Pair<Model, Pen> pair = spiroIO.loadSpiro(file);
            if (pair != null) {
                Model loadedModel = pair.first; // ロードされたModelインスタンス
                Pen loadedPen = pair.second; // ロードされたPenインスタンス

                // --- ここから修正 ---
                // ロードされたpathSegmentsを今あるリストに追加
                if (this.pathSegments == null) {
                    this.pathSegments = new ArrayList<>();
                }
                List<PathSegment> loadedSegments = loadedModel.getPathSegments();
                if (loadedSegments != null && !loadedSegments.isEmpty()) {
                    this.pathSegments.addAll(loadedSegments);
                }
                // ギア情報・ペン情報は上書き
                this.spurGear = loadedModel.getSpurGear();
                this.pinionGear = loadedModel.getPinionGear();

                // ロードされたペンの状態を現在のピニオンギアのペンに反映させる
                this.pinionGear.getPen().setPosition(loadedPen.getPosition());
                this.pinionGear.getPen().setPenSize(loadedPen.getPenSize());
                this.pinionGear.getPen().changeColor(loadedPen.getColor());

                this.pauseTime = 0;
                this.startTime = System.currentTimeMillis();
                stop();

                // Viewに追加後の全軌跡を通知
                notifyViewsLoading(this.pathSegments, this.pinionGear.getPen().getColor(),
                        this.pinionGear.getPen().getPenSize());

                // currentPathSegmentは新規で開始（既存の点があれば追加してから）
                if (currentPathSegment != null && !currentPathSegment.getPoints().isEmpty()) {
                    this.pathSegments.add(currentPathSegment);
                }
                currentPathSegment = new PathSegment(this.pinionGear.getPen().getColor(), this.pinionGear.getPen().getPenSize());

                return true;
                // --- ここまで修正 ---
            } else {
                System.err.println("Failed to load data: No data found in the file.");
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error loading data: " + e.getMessage());
            return false;
        }
    }

    /**
     * 指定されたファイルにスピロデザインのデータを保存する。
     * 保存が成功した場合、指定されたファイルにスパーギア、ピニオンギアのデータとペンの情報を保存する。
     * 保存に失敗した場合は、エラーメッセージを表示する。
     *
     * @param file  保存先のファイル
     * @param model 保存するModelのインスタンス
     * @param pen   保存するPenのインスタンス
     * @return 保存に成功した場合はtrue、失敗した場合はfalseを返す。
     */
    public Boolean saveData(File file, Model model, Pen pen) {
        // 保存する前に現在のセグメントをpathSegmentsに追加
        // currentPathSegmentがnullでなく、かつ点が含まれている場合のみ追加
        if (currentPathSegment != null && !currentPathSegment.getPoints().isEmpty()) {
            pathSegments.add(currentPathSegment);
        }

        try {
            spiroIO.saveSpiro(file, model, pen);
            return true;
        } catch (Exception e) {
            System.err.println("Error saving data: " + e.getMessage());
            return false;
        } finally {
            // 保存後、currentPathSegmentをPathSegmentsから取り除き、再度有効な状態にする
            // これにより、保存処理によってcurrentPathSegmentがpathSegmentsに永続化されず、
            // その後の描画で点が引き続きcurrentPathSegmentに追加される
            if (pathSegments != null && !pathSegments.isEmpty() && pathSegments.contains(currentPathSegment)) {
                pathSegments.remove(currentPathSegment);
            }
        }
    }

    /**
     * ギアとペンを初期状態にリセットする。
     * このメソッドは、Clearボタンが押されたときや、初期化時に呼び出される。
     */
    public void resetGears() {
        this.spurGear = new SpurGear();
        this.pinionGear = new PinionGear();
        // 必要に応じてペンの位置もリセット
        // --- 修正: ペンの初期位置をピニオンギアの中心＋オフセットにする ---
        Point2D.Double pinionCenter = this.pinionGear.getPinionPosition();
        double penOffset = 25.0; // PinionGearのDEFAULT_PEN_OFFSET_RADIUSと合わせる
        double angle = 0.0;
        Point2D.Double penPos = new Point2D.Double(
            pinionCenter.x + penOffset * Math.cos(angle),
            pinionCenter.y + penOffset * Math.sin(angle)
        );
        this.pinionGear.getPen().setPosition(penPos);
        this.pinionGear.getPen().setPenSize(Pen.DEFAULT_PEN_SIZE);
        this.pinionGear.getPen().changeColor(Pen.DEFAULT_COLOR);

        // 軌跡もクリアし、新しいセグメントを開始
        this.pathSegments = new ArrayList<>(); // locus から変更
        this.currentPathSegment = new PathSegment(this.pinionGear.getPen().getColor()); // 新しいセグメントを開始

        this.pauseTime = 0; // 一時停止時間もリセット
        this.startTime = System.currentTimeMillis(); // 開始時間もリセット

        // 多重実行防止: タイマーが動いていれば止める
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }

    }

    // スパーギアとピニオンギアのゲッターを追加 (SpiroIOがModelをロードする際に必要になる可能性)
    public SpurGear getSpurGear() {
        return spurGear;
    }

    public PinionGear getPinionGear() {
        return pinionGear;
    }
}

