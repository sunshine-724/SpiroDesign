package org.example.controller;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.Color; // Colorをインポート
import java.io.File; // Fileをインポート

import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import org.example.model.Model;
import org.example.view.View;
// import org.example.model.SpurGear; // 不要になったため削除
// import org.example.model.PinionGear; // 不要になったため削除


/**
 * Controllerクラスは、ビューとモデルを結びつけ、マウスイベントを処理する。
 * ユーザーの操作に応じて、モデルの状態を更新し、ビューを再描画する。
 */
public class Controller extends MouseInputAdapter implements MouseWheelListener, View.MenuButtonListener { // View.MenuButtonListenerを実装
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
    private Point pressPoint; // ドラッグ開始時のスクリーン座標
    private Point2D pressWorldPoint; // ドラッグ開始時のワールド座標
    private boolean isInner = true; // ピニオンギアがスパーギアの内側にあるかどうかのフラグ

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
        // MenuButtonListenerをViewに設定
        view.setMenuButtonListener(this); // ここでリスナーを設定
    }

    /**
     * マウスホイールが回転した時の処理。
     * ズームイン・ズームアウトを行う。
     * @param e_wheel マウスホイールイベント
     */
    @Override
    public void mouseWheelMoved(MouseWheelEvent e_wheel) {
        double rotation = e_wheel.getPreciseWheelRotation();

        // 拡大・縮小率の計算：回転が小さいほど緩やかにズームする
        double zoomFactor = Math.pow(1.1, -rotation);

        // マウスカーソル位置を中心にズームを実行（View クラス側で処理）
        view.zoomAt(e_wheel.getPoint(), zoomFactor);
    }

    /**
     * マウスがクリックされた時の処理。
     * ペンの位置を設定する。
     * @param e_click マウスクリックイベント
     */
    @Override
    public void mouseClicked(MouseEvent e_click) {
        Point clickedpoint = e_click.getPoint();
        Point2D worldClicked = view.screenToWorld(clickedpoint);
        Point2D pinionCenter = model.getPinionGearPosition();
        double distance = worldClicked.distance(pinionCenter);
        double radius = model.getPinionGearRadius();

        // クリック位置がピニオンギアの半径内であればペンの位置を設定
        if (distance <= radius) {
            model.setPenPosition(worldClicked);
            view.repaint();
            return;
        }
        model.mouseClicked(clickedpoint); // その他のモデル処理
    }

    /**
     * マウスが押された時の処理。
     * ドラッグモードを設定し、ポップアップメニューを表示する。
     * @param e_press マウスプレスイベント
     */
    @Override
    public void mousePressed(MouseEvent e_press) {
        this.pressPoint = e_press.getPoint(); // ドラッグ開始点を記録
        this.pressWorldPoint = view.screenToWorld(this.pressPoint); // ドラッグ開始点のワールド座標を記録

        // 右クリック（ポップアップトリガー）であればメニュー表示
        if (e_press.isPopupTrigger() || SwingUtilities.isRightMouseButton(e_press)) {
            view.showMenu(pressPoint.x, pressPoint.y);
            return;
        }

        Point2D spurCenter = model.getSpurGearPosition();
        double spurRadius = model.getSpurGearRadius();
        Point2D pinionCenter = model.getPinionGearPosition();
        Point2D penPosition = model.getPenPosition();

        double distToSpur = (spurCenter != null) ? pressWorldPoint.distance(spurCenter) : Double.MAX_VALUE;
        double distToPinion = (pinionCenter != null) ? pressWorldPoint.distance(pinionCenter) : Double.MAX_VALUE;
        double distToPen = (penPosition != null) ? pressWorldPoint.distance(penPosition) : Double.MAX_VALUE;


        // クリック位置に基づいてドラッグモードを決定
        // スケールを考慮したヒット判定
        double hitTolerance = 10.0 / view.getScale();

        if (spurCenter != null && distToSpur < hitTolerance) {
            draggingMode = DraggingMode.MOVE_SPUR_CENTER;
        } else if (spurCenter != null && Math.abs(distToSpur - spurRadius) < hitTolerance) {
            draggingMode = DraggingMode.RESIZE_SPUR_RADIUS;
        } else if (pinionCenter != null && distToPinion < hitTolerance) {
            draggingMode = DraggingMode.MOVE_PINION;
        } else if (penPosition != null && distToPen < hitTolerance) {
            // ペンのドラッグは現在未対応（NONEに設定）
            draggingMode = DraggingMode.NONE;
        } else {
            draggingMode = DraggingMode.PAN; // いずれにも該当しない場合はパンモード
        }

        model.mousePressed(pressPoint); // モデルへの通知
    }

    /**
     * マウスが離された時の処理。
     * ドラッグモードをリセットする。
     * @param e_release マウスリリースイベント
     */
    @Override
    public void mouseReleased(MouseEvent e_release) {
        draggingMode = DraggingMode.NONE;
        model.mouseReleased(e_release.getPoint()); // モデルへの通知
    }

    /**
     * マウスがドラッグされた時の処理。
     * ドラッグモードに応じて、スパーギア、ピニオンギアの移動・サイズ変更、またはビューのパンを行う。
     * @param e_drag マウスドラッグイベント
     */
    @Override
    public void mouseDragged(MouseEvent e_drag) {
        Point currentPoint = e_drag.getPoint();
        Point2D currentWorld = view.screenToWorld(currentPoint);

        // ワールド座標での移動量を計算
        double dx = currentWorld.getX() - pressWorldPoint.getX();
        double dy = currentWorld.getY() - pressWorldPoint.getY();

        switch (draggingMode) {
            case MOVE_SPUR_CENTER:
                model.moveSpurGearBy(dx, dy);
                break;

            case RESIZE_SPUR_RADIUS:
                Point2D spurCenterForResize = model.getSpurGearPosition();
                if (spurCenterForResize != null) {
                    double newRadius = currentWorld.distance(spurCenterForResize);
                    model.setSpurRadius(newRadius);
                }
                break;

            case MOVE_PINION:
                Point2D spurCenter = model.getSpurGearPosition();
                if (spurCenter == null) break; // スパーギアがnullなら処理しない

                double spurRadius = model.getSpurGearRadius();

                double dxRaw = currentWorld.getX() - spurCenter.getX();
                double dyRaw = currentWorld.getY() - spurCenter.getY();
                double dist = Math.hypot(dxRaw, dyRaw);
                if (dist < 5.0 / view.getScale()) break; // スケールを考慮した最小距離

                double unitX = dxRaw / dist;
                double unitY = dyRaw / dist;

                double newPinionRadius = Math.abs(spurRadius - dist);

                double minRadius = 5.0; // 最小半径
                if (newPinionRadius < minRadius) {
                    newPinionRadius = minRadius;
                }

                double hysteresis = 2.0 / view.getScale(); // スケールを考慮したヒステリシス
                double innerLimit = spurRadius - newPinionRadius + hysteresis;
                double outerLimit = spurRadius + newPinionRadius - hysteresis;

                if (isInner) {
                    if (dist >= innerLimit) {
                        isInner = false;
                    }
                } else {
                    if (dist <= outerLimit) {
                        isInner = true;
                    }
                }

                double distanceFromSpurCenter = isInner
                    ? spurRadius - newPinionRadius
                    : spurRadius + newPinionRadius;

                double newCenterX = spurCenter.getX() + unitX * distanceFromSpurCenter;
                double newCenterY = spurCenter.getY() + unitY * distanceFromSpurCenter;
                Point2D.Double newCenter = new Point2D.Double(newCenterX, newCenterY);

                model.setPinionGearPosition(newCenter);
                model.changePinionGearRadius(newPinionRadius);
                // ペンはピニオンギアの移動に応じてModelのupdateData()で更新されるか、
                // あるいはModelのmovePenBy()で別途移動させる。
                // ここではModelのmovePenBy()の呼び出しは残す。
                model.movePenBy(dx, dy);
                break;

            case PAN:
                // スクリーン座標での移動量を計算してパン
                int panDx = currentPoint.x - this.pressPoint.x; // 正しいドラッグ開始点を使用
                int panDy = currentPoint.y - this.pressPoint.y; // 正しいドラッグ開始点を使用
                view.pan(panDx, panDy);
                break;

            default:
                break;
        }

        // 次のドラッグイベントのために、現在の点を前回のドラッグ開始点として設定
        this.pressPoint = currentPoint;
        this.pressWorldPoint = currentWorld;

        view.repaint(); // ビューを再描画
        model.mouseDragged(currentPoint); // モデルへの通知（必要であれば）
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
                // Model内のresetGears()メソッドを呼び出し、ギアとペンをリセットする
                model.resetGears();
                model.stop(); // 描画を停止

                view.clearLoadedLocusData(); // ロードされた軌跡データもクリア
                view.repaint();
                break;
            case "Save":
                File saveFile = view.chooseSaveFile();
                if (saveFile != null) {
                    if (model.saveData(saveFile, model, model.getPinionGear().getPen())) { // getPinionGear()を使用
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
                        model.stop(); // 読み込み後に自動再生しない場合は停止
                    } else {
                        view.displaySaveSuccessMessage("読み込みに失敗しました。");
                    }
                }
                break;
            case "Small":
                model.getPinionGear().getPen().setPenSize(1.0); // getPinionGear()を使用
                view.repaint();
                break;
            case "Medium":
                model.getPinionGear().getPen().setPenSize(3.0); // getPinionGear()を使用
                view.repaint();
                break;
            case "Large":
                model.getPinionGear().getPen().setPenSize(5.0); // getPinionGear()を使用
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
        model.getPinionGear().getPen().changeColor(color); // getPinionGear()を使用
        view.repaint();
    }

    /**
     * MenuButtonListenerインターフェースの実装メソッド。
     * スピードスライダーの値が変更された時の処理を行う。
     * @param speed 選択されたスピード
     */
    @Override
    public void onSpeedSelected(double speed) {
        model.changeSpeed(speed);
    }
}
