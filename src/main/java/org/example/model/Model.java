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

public class Model {

    SpurGear spurGear;
    PinionGear pinionGear;

    SpiroIO spiroIO;

    List<View> views = new ArrayList<>(); // Viewのリスト

    // Point2D.Double spurGearPosition; //ref only
    // Point2D.Double pinionGearPosition; //ref only

    Timer timer;
    private long startTime;
    private long pauseTime;

    List<Point2D.Double> locus = new ArrayList<>(); // Locus of the pen

    // Constants
    private static final int FRAME_PER_MILLISECOND = 1000 / 60; // 60 FPS

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

    public void start() {
        System.out.println("start");
        timer.start();
        startTime = System.currentTimeMillis() - pauseTime;
    }

    public void stop() {
        System.out.println("stop");
        timer.stop();
        pauseTime = System.currentTimeMillis() - startTime;
    }

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



    //ファイルを読み込んだ後、viewに
    private void notifyViewsLoading() {
        for (View view : views) {
            view.getLocus(locus);
        }
    }

    public void addView(View view) {
        views.add(view);
    }

    public List<Point2D.Double> getLocus() {
        return locus;
    }

    public void changeSpeed(Double speed) {
        pinionGear.changeSpeed(speed);
    }

    public void setPinionGearPosition(Point2D.Double position) {
        pinionGear.setPosition(position);
    }

    public void changeSpurGeearRadius(Double radius) {
        spurGear.changeRadius(radius);
    }

    public void changePinionGearRadius(Double radius) {
        pinionGear.changeRadius(radius);
    }

    public Double getSpurGearRadius() {
        return spurGear.getSpurRadius();
    }

    public Double getPinionGearRadius() {
        return pinionGear.getPinionRadius();
    }

    public Point2D.Double getPinionGearPosition() {
        return pinionGear.getPinionPosition();
    }

    public Point2D.Double getSpurGearPosition() {
        return spurGear.getSpurPosition();
    }

    public Point2D.Double getPenPosition() {
        return pinionGear.getPen().getPosition();
    }

    public Color getPenColor() {
        return pinionGear.getPen().getColor();
    }

    public double getPenSize() {
        return pinionGear.getPen().getPenSize();
    }

    public long getSpirographStartTime() {
        return startTime;
    }

    public long getSpirographCurrentTime() {
        return System.currentTimeMillis();
    }

    public Point2D.Double getPenPositionAtTime() {
        return pinionGear.getPen().getPosition();
    }

    // クリックすると
    // ピニオンギアの位置を更新
    // ペンの座標の位置を更新
    public void mouseClicked(Point position) {
    }

    public void mousePressed(Point position) {
    }

    public void mouseReleased(Point position) {
    }

    // ドラッグすると
    // ピニオンギアの半径を更新
    // スパーギアの半径を更新
    // スパーギアの中心位置を更新
    // ピニオンギアの中心位置を更新
    public void mouseDragged(Point position) {

    }

    public void pressLoadButton(File file) {
        // 読み込みダイアログを開く
        // ファイルを選択して読み込み

        if (file != null) {
            loadData(file);
        }
    }

    public void pressSaveButton(File file, Pen pen) {
        saveData(file,this,pen);
    }

    // 以下まだ未実装
    public void loadData(File file) {
        Pair<Model,Pen> pair = spiroIO.loadSpiro(file);
        if (pair != null) {
            Model model = pair.first;
            Pen pen = pair.second;

            List<Point2D.Double> locus = model.locus;
        }
    }

    public void saveData(File file, Model model,Pen pen) {
        spiroIO.saveSpiro(file,model,pen);
    }
}