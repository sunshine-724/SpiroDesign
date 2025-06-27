package org.example.controller;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.Color;
import java.io.File;
import java.awt.Cursor; // 追加: java.awt.Cursorをインポート

import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import org.example.model.Model;
import org.example.view.View;


/**
 * Controllerクラスは、ビューとモデルを結びつけ、マウスイベントを処理する。
 * ユーザーの操作に応じて、モデルの状態を更新し、ビューを再描画する。
 */
public class Controller extends MouseInputAdapter implements MouseWheelListener, View.MenuButtonListener {
    protected Model model;
    protected View view;

    /**
     * ドラッグモードを定義する列挙型。
     * NONE: ドラッグしていない状態
     * MOVE_SPUR_CENTER: スパーギアの中心を移動
     * MOVE_PINION: ピニオンギアを移動
     * RESIZE_SPUR_RADIUS: スパーギアの半径を変更
     * PAN: ビューをパン（移動）
     */
    private enum DraggingMode {
        NONE,
        MOVE_SPUR_CENTER,
        MOVE_PINION,
        RESIZE_SPUR_RADIUS,
        PAN
    }

    private DraggingMode draggingMode = DraggingMode.NONE;
    private Point pressPoint;
    private Point2D pressWorldPoint;
    private boolean isInner = true;

    /**
     * コンストラクタ
     * @param view ビューオブジェクト
     * @param model モデルオブジェクト
     */
    public Controller(View view, Model model) {
        this.view = view;
        this.model = model;
        view.addMouseListener(this);
        view.addMouseMotionListener(this);
        view.setMenuButtonListener(this);
    }

    /**
     * マウスホイールが回転した時の処理。
     * ズームイン・ズームアウトを行う。
     * @param e_wheel マウスホイールイベント
     */
    @Override
    public void mouseWheelMoved(MouseWheelEvent e_wheel) {
        double rotation = e_wheel.getPreciseWheelRotation();
        double zoomFactor = Math.pow(1.1, -rotation);
        view.zoomAt(e_wheel.getPoint(), zoomFactor);
    }

    /**
     * マウスがクリックされた時の処理。
     * ペンの位置を設定する。
     * @param e_click マウスクリックイベント
     */
    @Override
    public void mouseClicked(MouseEvent e_click) {
        Point clickedScreenPoint = e_click.getPoint();
        if (SwingUtilities.isLeftMouseButton(e_click)) {
            model.setPenPosition(view.screenToWorld(clickedScreenPoint));
        }
        view.repaint();
    }

    /**
     * マウスボタンが押された時の処理。
     * ドラッグ操作の開始点を記録し、ドラッグモードを設定する。
     * @param e_press マウスプレスイベント
     */
    @Override
    public void mousePressed(MouseEvent e_press) {
        pressPoint = e_press.getPoint();
        pressWorldPoint = view.screenToWorld(pressPoint);

        // スパーギアの中心をドラッグ
        if (model.getSpurGearPosition().distance(pressWorldPoint) < 10 / view.getScale()) {
            draggingMode = DraggingMode.MOVE_SPUR_CENTER;
        }
        // スパーギアの半径をドラッグ
        else if (Math.abs(model.getSpurGearPosition().distance(pressWorldPoint) - model.getSpurGearRadius()) < 10 / view.getScale()) {
            draggingMode = DraggingMode.RESIZE_SPUR_RADIUS;
            view.setDefiningSpurGear(true); // スパーギア定義中フラグをセット
            view.setSpurGearCenterScreen(view.worldToScreen(model.getSpurGearPosition())); // 中心点を設定
        }
        // ピニオンギアをドラッグ
        else if (model.getPinionGearPosition().distance(pressWorldPoint) < 10 / view.getScale()) {
            draggingMode = DraggingMode.MOVE_PINION;
            view.setDefiningPinionGear(true);
            view.setPinionGearCenterScreen(view.worldToScreen(model.getPinionGearPosition()));
        }
        // 背景をパン
        else {
            draggingMode = DraggingMode.PAN;
        }

        // 右クリックでポップアップメニューを表示
        if (e_press.isPopupTrigger() || SwingUtilities.isRightMouseButton(e_press)) {
            view.MenuDisplay.show(e_press.getComponent(), e_press.getX(), e_press.getY());
        }
    }

    /**
     * マウスがドラッグされた時の処理。
     * 現在のドラッグモードに応じて、ギアの移動、半径変更、ビューのパンを行う。
     * @param e_drag マウスドラッグイベント
     */
    @Override
    public void mouseDragged(MouseEvent e_drag) {
        Point currentScreenPoint = e_drag.getPoint();
        Point2D currentWorldPoint = view.screenToWorld(currentScreenPoint);

        if (draggingMode == DraggingMode.MOVE_SPUR_CENTER) {
            model.moveSpurGear(currentWorldPoint);
        } else if (draggingMode == DraggingMode.RESIZE_SPUR_RADIUS) {
            model.changeSpurGearRadius(currentWorldPoint);
            view.setCurrentDragPointScreen(currentScreenPoint); // ガイド描画用に現在のドラッグ点を更新
        } else if (draggingMode == DraggingMode.MOVE_PINION) {
            model.movePinionGear(currentWorldPoint);
            view.setCurrentDragPointScreenForPinion(currentScreenPoint);
        } else if (draggingMode == DraggingMode.PAN) {
            model.panView(pressPoint, currentScreenPoint);
            pressPoint = currentScreenPoint; // ドラッグ開始点を更新
        }
        view.repaint();
        view.updateCursor(currentScreenPoint); // カーソルを更新
    }

    /**
     * マウスボタンが離された時の処理。
     * ドラッグモードをリセットする。
     * @param e_release マウスリリースイベント
     */
    @Override
    public void mouseReleased(MouseEvent e_release) {
        // 右クリックでポップアップメニューを表示
        if (e_release.isPopupTrigger() || SwingUtilities.isRightMouseButton(e_release)) {
            view.MenuDisplay.show(e_release.getComponent(), e_release.getX(), e_release.getY());
        }

        draggingMode = DraggingMode.NONE;
        view.setDefiningSpurGear(false); // フラグをリセット
        view.setDefiningPinionGear(false);
        view.repaint();
        view.updateCursor(e_release.getPoint()); // カーソルを更新
    }

    /**
     * マウスがコンポーネントに入った時の処理。
     * @param e_enter マウスエンターイベント
     */
    @Override
    public void mouseEntered(MouseEvent e_enter) {
        isInner = true;
    }

    /**
     * マウスがコンポーネントから出た時の処理。
     * @param e_exit マウスイグジットイベント
     */
    @Override
    public void mouseExited(MouseEvent e_exit) {
        isInner = false;
        view.setCursor(Cursor.getDefaultCursor()); // デフォルトカーソルに戻す
    }

    /**
     * マウスが移動した時の処理。
     * カーソルを更新する。
     * @param e_move マウスムーブイベント
     */
    @Override
    public void mouseMoved(MouseEvent e_move) {
        if (isInner) { // マウスがパネル内にある場合のみカーソルを更新
            view.updateCursor(e_move.getPoint());
        }
    }

    /**
     * MenuButtonListenerインターフェースの実装メソッド。
     * メニューボタンがクリックされた時の処理を行う。
     * @param buttonName クリックされたボタンの名前
     */
    @Override
    public void onMenuButtonClicked(String buttonName) {
        switch (buttonName) {
            case "Start":
                model.start();
                break;
            case "Stop":
                model.stop();
                break;
            case "Clear":
                model.resetGears(); // ギアをリセット
                view.repaint();
                break;
            case "Save":
                File saveFile = view.chooseSaveFile();
                if (saveFile != null) {
                    if (model.saveData(saveFile, model, model.getPinionGear().getPen())) {
                        view.displaySaveSuccessMessage("保存しました！");
                    } else {
                        view.displaySaveSuccessMessage("保存に失敗しました。");
                    }
                }
                break;
            case "Load":
                File loadFile = view.chooseLoadFile();
                if (loadFile != null) {
                    if (model.loadData(loadFile)) {
                        view.displaySaveSuccessMessage("読み込みました！");
                    } else {
                        view.displaySaveSuccessMessage("読み込みに失敗しました。");
                    }
                }
                break;
            case "Small":
                model.getPinionGear().getPen().setPenSize(1.0);
                view.repaint();
                break;
            case "Medium":
                model.getPinionGear().getPen().setPenSize(3.0);
                view.repaint();
                break;
            case "Large":
                model.getPinionGear().getPen().setPenSize(5.0);
                view.repaint();
                break;
            default:
                break;
        }
    }

    /**
     * MenuButtonListenerインターフェースの実装メソッド。
     * 色選択ダイアログで色を選択した時の処理を行う。
     * @param color 選択された色
     */
    @Override
    public void onColorSelected(Color color) {
        model.changePenColor(color);
        view.repaint();
    }

    /**
     * MenuButtonListenerインターフェースの実装メソッド。
     * スピードスライダーの値が変更された時の処理を行う。
     * @param speed 選択されたスピード (スライダーの整数値: 1〜10)
     */
    @Override
    public void onSpeedSelected(double speed) {
        // スライダーから受け取った速度値 (1〜10) を実際の速度 (0.1〜1.0) にスケールダウンしてモデルに渡す
        model.changeSpeed(speed / 10.0);
        view.repaint();
    }
}
