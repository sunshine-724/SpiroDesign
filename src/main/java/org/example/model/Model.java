package org.example.model;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.*;
import java.io.File;
import java.io.IOException; // IOExceptionをインポート
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Timer;

import org.example.spiroIO.SpiroIO;
import org.example.view.View;
import org.example.lib.*;

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

    List<Point2D.Double> locus = new ArrayList<>(); // Locus of the pen

    /** 描画開始時刻(ミリ秒) */
    private long startTime; // 宣言を追加

    /** 一時停止時間(ミリ秒) */
    private long pauseTime; // 宣言を追加

    /**
     * 1ミリ秒あたりのフレーム数
     * 60 FPSで描画するため、1フレームあたりの時間は1000 / 60ミリ秒
     */
    private static final int FRAME_PER_MILLISECOND = 1000 / 60; // 60 FPS

    /**
     * Modelクラスのコンストラクタ。
     * スパーギアとピニオンギアのインスタンスを初期化し、SpiroIOを初期化する。
     * タイマーを設定し、一定の間隔でデータを更新するようにする。
     * ただしタイマーは非同期で動作し、他の処理をブロックしない。
     */
    public Model() {
        // コンストラクタでギアを初期化する代わりに、resetGears()を呼び出す
        resetGears(); // 初期化もresetGears()で行う
        initializeTransientFields(); // transientフィールドの初期化を呼び出す
    }

    /**
     * transientフィールドを初期化するヘルパーメソッド。
     * デシリアライズ後にも呼び出される必要がある。
     */
    private void initializeTransientFields() {
        spiroIO = new SpiroIO();
        views = new ArrayList<>(); // 再度初期化
        timer = new Timer(FRAME_PER_MILLISECOND, e -> { // FRAME_PER_MILLISEensecond を修正
            updateData(); // データ更新
            // Modelが更新されたらViewに再描画を通知
            // ModelはViewに依存しないが、Modelの状態が変更されたことをViewに伝える必要がある
            for (View view : views) {
                view.repaint();
            }
        });
        // デシリアライズ後にstart()やstop()が呼ばれるまでtimerは停止状態なので、startTime/pauseTimeはここでリセットしない。
        // resetGears()でリセットされることを想定。
    }

    /**
     * オブジェクトのデシリアライズ後に呼び出されるメソッド。
     * transientとしてマークされたフィールドを再初期化する。
     */
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject(); // デフォルトのデシリアライズ処理を実行
        initializeTransientFields(); // transientフィールドを再初期化
        // タイマーは停止した状態でロードされるため、ロードされた開始時刻と停止時刻を適切に設定
        // これはloadDataでModelの状態を置き換えた後に処理するべきかもしれない
    }


    /**
     * スピロデザインのの描画を開始する。
     * タイマーを開始または再開し、描画開始時刻を記録する。
     */
    public void start() {
        System.out.println("start");
        if (!timer.isRunning()) { // 既に実行中でない場合のみ開始
            timer.start();
            startTime = System.currentTimeMillis() - pauseTime;
        }
    }

    /**
     * スピロデザインの描画を停止する。
     * タイマーを停止し、現在の時間を記録する。
     * これにより、再開時に前回の続きから描画を再開できる。
     */
    public void stop() {
        System.out.println("stop");
        if (timer.isRunning()) { // 実行中の場合のみ停止
            timer.stop();
            pauseTime = System.currentTimeMillis() - startTime;
        }
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

        locus.add(penPosition);
    }

    /**
     * ロード後Viewにデータの更新を通知する。
     *
     * @param locusData 軌跡のデータ
     * @param penColor  ペンの色
     * @param penSize   ペンのサイズ
     */
    private void notifyViewsLoading(List<Point2D.Double> locusData, Color penColor, double penSize) {
        for (View view : views) {
            view.setLocusData(locusData, penColor, penSize);
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
     * 軌跡を取得する。
     * 軌跡は、ピニオンギアのペンが描いた点のリスト。
     *
     * @return ピニオンギアのペンが描いた点のリスト。
     */
    public List<Point2D.Double> getLocus() {
        return locus;
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
        notifiyViewsChangeAndPinionRadius(radius);
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

        // このメソッドはピニオンギアの他にペンも同時に動く。
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
        notifiyViewsChangeAndPinionRadius(radius);
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
        pinionGear.setPenPosition(position);
    }

    /**
     * ピニオンギアのペンの位置を設定する。
     *
     * @param pos ペンの位置
     */
    public void setPenPosition(Point2D pos) {
        Point2D.Double newPos = new Point2D.Double(pos.getX(), pos.getY());
        pinionGear.setPenPosition(newPos);
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

                // ロードされたデータで現在のモデルの状態を更新
                this.spurGear = loadedModel.getSpurGear();
                this.pinionGear = loadedModel.getPinionGear();
                this.locus = loadedModel.getLocus();
                // transientフィールドはデシリアライズされないので、readObject()で再初期化される
                // ここでは、ロードされたペンの状態を現在のピニオンギアのペンに反映させる
                this.pinionGear.getPen().setPosition(loadedPen.getPosition());
                this.pinionGear.getPen().setPenSize(loadedPen.getPenSize());
                this.pinionGear.getPen().changeColor(loadedPen.getColor());

                // ロード時にタイマーが再開されないように stop() を呼び出す。
                // start/pauseTimeもリセットされるべきだが、それはloadDataの呼び出し元（Controller）
                // またはModelのresetGears()で行うのが望ましい。
                this.pauseTime = 0; // ロード後は一時停止時間をリセット
                this.startTime = System.currentTimeMillis(); // 開始時刻もリセットして、再開時に正しく動作するようにする
                stop(); // ロード後はアニメーションを停止状態にする（Controllerでも行うが念のため）


                notifyViewsLoading(this.locus, this.pinionGear.getPen().getColor(), this.pinionGear.getPen().getPenSize()); // Viewにデータの更新を通知

                return true; // 読み込みに成功した場合はtrueを返す
            } else {
                System.err.println("Failed to load data: No data found in the file.");
                return false; // 読み込みに失敗した場合はfalseを返す
            }
        } catch (Exception e) {
            System.err.println("Error loading data: " + e.getMessage());
            // エラーメッセージを表示するなどの処理を追加
            return false; // 読み込みに失敗した場合はfalseを返す
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
        try {
            spiroIO.saveSpiro(file, model, pen);
            return true; // 保存に成功した場合はtrueを返す
        } catch (Exception e) {
            System.err.println("Error saving data: " + e.getMessage());
            return false; // 保存に失敗した場合はfalseを返す
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
        this.pinionGear.getPen().setPosition(new Point2D.Double(0, 0)); // Penのデフォルト位置をModelに設定
        this.pinionGear.getPen().setPenSize(Pen.DEFAULT_PEN_SIZE);
        this.pinionGear.getPen().changeColor(Pen.DEFAULT_COLOR);

        // 軌跡もクリア
        this.locus.clear();
        this.pauseTime = 0; // 一時停止時間もリセット
        this.startTime = System.currentTimeMillis(); // 開始時間もリセット
    }

    // スパーギアとピニオンギアのゲッターを追加 (SpiroIOがModelをロードする際に必要になる可能性)
    public SpurGear getSpurGear() {
        return spurGear;
    }

    public PinionGear getPinionGear() {
        return pinionGear;
    }
}
