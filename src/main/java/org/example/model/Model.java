package org.example.model;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Timer;

import org.example.spiroIO.SpiroIO;
import org.example.view.View;
import org.example.lib.*;

/*
 * Modelクラスは、Spiro.appのデータを管理するクラスです。
 */

public class Model {

    /**
     * スパーギアとピニオンギアのインスタンスを保持します。
     * スパーギアは大きな円で、ピニオンギアは小さな円です。
     * ピニオンギアはスパーギアの周りを回転し、ペンを動かしてスピロデザインを描きます。
     */
    SpurGear spurGear;
    PinionGear pinionGear;

    /**
     * SpiroIOは、スピロデザインのデータを読み書きするためのクラスです。
     * ファイルからスパーギア、ピニオンギアのデータを読み込んだり、保存したりします。
     */

    SpiroIO spiroIO;

    /**
     * Viewのリストを保持します。
     * Viewは、すぴろデザインの描画レイヤーとユーザーインターフェースを担当します。
     * ModelはViewにデータを提供し、ViewはModelの状態を表示します。
     * ModelはViewに依存しませんが、ViewはModelのデータを表示するためにModelに依存します。
     * ModelはViewに対して通知を行い、ViewはModelのデータを更新します。
     */
    List<View> views = new ArrayList<>(); // Viewのリスト

    /**
     * タイマーは一定の間隔でModelのデータを更新し、Viewに通知し、スピロデザインの描画をアニメーションするために使用されます。
     */
    Timer timer;

    /** 描画開始時刻(ミリ秒) */
    private long startTime;

    /** 一時停止時間(ミリ秒) */
    private long pauseTime;

    List<Point2D.Double> locus = new ArrayList<>(); // Locus of the pen

    /**
     * 1ミリ秒あたりのフレーム数
     * 60 FPSで描画するため、1フレームあたりの時間は1000 / 60ミリ秒
     */
    private static final int FRAME_PER_MILLISECOND = 1000 / 60; // 60 FPS

    /**
     * Modelクラスのコンストラクタです。
     * スパーギアとピニオンギアのインスタンスを初期化し、SpiroIOを初期化します。
     * タイマーを設定し、一定の間隔でデータを更新するようにします。
     * ただしタイマーは非同期で動作し、他の処理をブロックしません。
     */
    public Model() {
        spurGear = new SpurGear();
        pinionGear = new PinionGear();

        spiroIO = new SpiroIO();

        startTime = System.currentTimeMillis();
        pauseTime = 0;
        timer = new Timer(FRAME_PER_MILLISECOND, e -> {
            updateData(); // データ更新
        });
    }

    /**
     * スピロデザインのの描画を開始します。
     * タイマーを開始または再開し、描画開始時刻を記録します。
     */

    public void start() {
        System.out.println("start");
        timer.start();
        startTime = System.currentTimeMillis() - pauseTime;
    }

    /**
     * スピロデザインの描画を停止します。
     * タイマーを停止し、現在の時間を記録します。
     * これにより、再開時に前回の続きから描画を再開できます。
     */

    public void stop() {
        System.out.println("stop");
        timer.stop();
        pauseTime = System.currentTimeMillis() - startTime;
    }

    /**
     * Modelのデータを更新します。
     * このメソッドは、ピニオンギアの位置とペンの位置を更新し、描画する点を軌跡の点としてリストに追加します。
     * 
     * @see #updateData()
     */

    private void updateData() {
        long currentTime = System.currentTimeMillis() - startTime;

        // ピニオンギアの位置とペンの位置を更新
        pinionGear.move(currentTime, spurGear.getSpurRadius(), spurGear.getSpurPosition());

        // ペンの位置を取得
        Point2D.Double penPosition = pinionGear.getPen().getPosition();

        // ペンの位置が相対座標なので絶対座標に変換
        Point2D.Double absolutePenPosition = new Point2D.Double(
                penPosition.x + pinionGear.getPinionPosition().x,
                penPosition.y + pinionGear.getPinionPosition().y);

        // ローカスにペンの位置を追加
        locus.add(absolutePenPosition);
    }

    /**
     * ロード後Viewにデータの更新を通知します。
     */
    private void notifyViewsLoading(List<Point2D.Double> locusData, Color penColor, double penSize) {
        for (View view : views) {
            view.setLocusData(locusData,penColor, penSize);
        }
    }

    /**
     * Viewにデータの更新を通知するためにリスナーを追加します。(Observerパターン)
     * 
     * @param view 通知を受け取るViewのインスタンス
     * @see #notifyViewsLoading()
     */
    public void addView(View view) {
        views.add(view);
    }

    /**
     * 軌跡を取得します。
     * 軌跡は、ピニオンギアのペンが描いた点のリストです。
     * 
     * @return ピニオンギアのペンが描いた点のリスト。
     */
    public List<Point2D.Double> getLocus() {
        return locus;
    }

    /**
     * ピニオンギアのスピードを変更します。
     * 
     * @param speed 変更したいピニオンギアのスピード。
     */
    public void changeSpeed(Double speed) {
        pinionGear.changeSpeed(speed);
    }

    /**
     * ピニオンギアの位置を設定します。
     * 指定されたピニオンギアの位置は、スパーギアの中心位置を基準にした相対座標に変換されます。
     * 
     * @param position ピニオンギアの位置(絶対座標)
     */
    public void setPinionGearPosition(Point2D.Double position) {
        pinionGear.setPosition(position);
    }

    /**
     * ピニオンギアの位置を取得します。
     * ピニオンギアの位置は、スパーギアの中心位置を基準にした相対座標から、絶対座標に変換されて返ります。
     * 
     * @return ピニオンギアの位置(絶対座標)
     */
    public Point2D.Double getPinionGearPosition() {
        return pinionGear.getPinionPosition();
    }

    /**
     * ピニオンギアの半径を変更します。
     * 
     * @param radius 変更したいピニオンギアの半径
     */

    public void changePinionGearRadius(Double radius) {
        pinionGear.changeRadius(radius);
    }

    /**
     * ピニオンギアの半径を取得します。
     * 
     * @return ピニオンギアの半径
     */
    public Double getPinionGearRadius() {
        return pinionGear.getPinionRadius();
    }

    /**
     * スパーギアの中心位置を設定します。
     * スパーギアの中心位置は、スピロデザインの描画領域の中心を基準にします。
     * 
     * @param position スパーギアの中心位置(絶対座標)
     */

    public void changeSpurGearRadius(Double radius) {
        spurGear.changeRadius(radius);
    }

    /**
     * スパーギアの半径を取得します。
     * 
     * @return スパーギアの半径
     */
    public Double getSpurGearRadius() {
        return spurGear.getSpurRadius();
    }

    /**
     * スパーギアの中心位置を取得します。
     * スパーギアの中心位置は、スピロデザインの描画領域の中心を基準にした絶対座標で返されます。
     * 
     * @return スパーギアの中心位置(絶対座標)
     */
    public Point2D.Double getSpurGearPosition() {
        return spurGear.getSpurPosition();
    }

    /**
     * ピニオンギアのペンの位置を取得します。
     * ペンの位置は、ピニオンギアの中心位置を基準にした相対座標から絶対座標に変換されて返されます。
     * 
     * @return ピニオンギアのペンの位置(絶対座標)
     */
    public Point2D.Double getPenPosition() {
        return pinionGear.getPen().getPosition();
    }

    /**
     * ピニオンギアのペンの色を取得します。
     * 
     * @return ピニオンギアのペンの色
     */
    public Color getPenColor() {
        return pinionGear.getPen().getColor();
    }

    /**
     * ピニオンギアのペンのサイズを取得します。
     * 
     * @return ピニオンギアのペンのサイズ
     */
    public double getPenSize() {
        return pinionGear.getPen().getPenSize();
    }

    /**
     * デザインの描画開始時刻を取得します。
     * 
     * @return スピロデザインの開始時刻(ミリ秒)
     */
    public long getSpirographStartTime() {
        return startTime;
    }

    /**
     * スピログラフの現在時刻を取得します。
     * 
     * @return スピログラフの現在時刻(ミリ秒)
     */
    public long getSpirographCurrentTime() {
        return System.currentTimeMillis();
    }

    /**
     * マウスがクリックされた時の処理を行います。
     * このメソッドは、マウスの位置を引数として受け取り、クリックイベントに応じた処理を行います。
     * 例えば、ピニオンギアの位置、半径の変更だったり、スパーギアの位置、半径の変更だったりすることができます。
     * 
     * @param position マウスの位置
     */
    public void mouseClicked(Point position) {
    }

    /**
     * マウスが押された時の処理を行います。
     * このメソッドはドラッグイベントの開始時に呼び出されることを想定しており、主にドラッグの開始地点の設定を行います。
     * 例えば、ピニオンギアの位置、半径の変更だったり、スパーギアの位置、半径の変更だったりすることができます。
     * 
     * @param position マウスの位置
     */
    public void mousePressed(Point position) {
    }

    /**
     * マウスが離された時の処理を行います。
     * このメソッドは、マウスの位置を引数として受け取り、離上イベントに応じた処理を行います。
     * 例えば、ピニオンギアの位置を設定したり、スパーギアの半径を変更したりすることができます。
     * 
     * @param position マウスの位置
     */
    public void mouseReleased(Point position) {
    }

    // ドラッグすると
    // ピニオンギアの半径を更新
    // スパーギアの半径を更新
    // スパーギアの中心位置を更新
    // ピニオンギアの中心位置を更新
    /**
     * マウスがドラッグされた時の処理を行います。
     * このメソッドは、マウスの位置を引数として受け取り、ドラッグイベントに応じた処理を行います。
     * 例えば、ピニオンギアの位置を更新したり、スパーギアの半径を変更したりすることができます。
     * 
     * @param position
     */
    public void mouseDragged(Point position) {
        
    }

    /**
     * Loadボタンが押された時の処理を行います。
     * FileChooserなどで選択されたファイルを引数として受け取り、
     * そのファイルからスピロデザインのデータを読み込みます。
     * 読み込みが成功した場合、Modelのデータを更新し、Viewに通知します。
     * 
     * @param file 読み込み対象のファイル
     * @see #loadData(File)
     */
    public void pressLoadButton(File file) {
        // 読み込みダイアログを開く
        // ファイルを選択して読み込み

        if (file != null) {
            loadData(file);
        }
    }

    /**
     * Saveボタンが押された時の処理を行います。
     * FileChooserなどで選択されたファイルを引数として受け取り、
     * そのファイルにスピロデザインのデータを保存します。
     * 
     * @param file 保存先のファイル
     * @param pen  ペンの情報
     * @see #saveData(File, Model, Pen)
     */
    public void pressSaveButton(File file, Pen pen) {
        saveData(file, this, pen);
    }

    /**
     * 指定されたファイルからスピロデザインのデータを読み込みます。
     * 読み込みが成功した場合、軌跡とペンの情報を読み込みViewに通知します。
     * 読み込みに失敗した場合は、エラーメッセージを表示します。
     * 
     * @see org.example.spiroIO.SpiroIO#loadSpiro(File)
     * @param file
     * @return 読み込みに成功した場合はtrue、失敗した場合はfalseを返します。
     * @throws Exception 読み込みに失敗した場合にスローされる例外
     */
    public Boolean loadData(File file) {
        try {
            Pair<Model, Pen> pair = spiroIO.loadSpiro(file);
            if (pair != null) {
                Model model = pair.first;
                Pen pen = pair.second;

                List<Point2D.Double> locus = model.locus;
                Color penColor = pen.getColor();
                double penSize = pen.getPenSize();

                notifyViewsLoading(locus, penColor, penSize); // Viewにデータの更新を通知

                return true; // 読み込みに成功した場合はtrueを返す
            }else {
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
     * 指定されたファイルにスピロデザインのデータを保存します。
     * 保存が成功した場合、指定されたファイルにスパーギア、ピニオンギアのデータとペンの情報を保存します。
     * 保存に失敗した場合は、エラーメッセージを表示します。
     * 
     * @see org.example.spiroIO.SpiroIO#saveSpiro(File, Model, Pen)
     * @param file
     * @param model
     * @param pen
     * @return 保存に成功した場合はtrue、失敗した場合はfalseを返します。
     * @throws Exception 保存に失敗した場合にスローされる例外
     */
    public Boolean saveData(File file, Model model, Pen pen) {
        try{
            spiroIO.saveSpiro(file, model, pen);
            return true; // 保存に成功した場合はtrueを返す
        } catch (Exception e) {
            System.err.println("Error saving data: " + e.getMessage());
            return false; // 保存に失敗した場合はfalseを返す
            // エラーメッセージを表示するなどの処理を追加
        }
    }
}