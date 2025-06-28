package org.example.controller;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.Color;
import java.io.File;

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
        Point clickedpoint = e_click.getPoint();
        Point2D worldClicked = view.screenToWorld(clickedpoint);
        Point2D pinionCenter = model.getPinionGearPosition();
        double distance = worldClicked.distance(pinionCenter);
        double radius = model.getPinionGearRadius();

        if (distance <= radius) {
            model.setPenPosition(worldClicked);
            view.repaint();
            return;
        }
        model.mouseClicked(clickedpoint);
    }

    /**
     * マウスが押された時の処理。
     * ドラッグモードを設定し、ポップアップメニューを表示する。
     * @param e_press マウスプレスイベント
     */
    @Override
    public void mousePressed(MouseEvent e_press) {
        this.pressPoint = e_press.getPoint();
        this.pressWorldPoint = view.screenToWorld(this.pressPoint);

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

        double hitTolerance = 10.0 / view.getScale();

        if (spurCenter != null && distToSpur < hitTolerance) {
            draggingMode = DraggingMode.MOVE_SPUR_CENTER;
        } else if (spurCenter != null && Math.abs(distToSpur - spurRadius) < hitTolerance) {
            draggingMode = DraggingMode.RESIZE_SPUR_RADIUS;
        } else if (pinionCenter != null && distToPinion < hitTolerance) {
            draggingMode = DraggingMode.MOVE_PINION;
        } else if (penPosition != null && distToPen < hitTolerance) {
            draggingMode = DraggingMode.NONE;
        } else {
            draggingMode = DraggingMode.PAN;
        }

        model.mousePressed(pressPoint);
    }

    /**
     * マウスが離された時の処理。
     * ドラッグモードをリセットする。
     * @param e_release マウスリリースイベント
     */
    @Override
    public void mouseReleased(MouseEvent e_release) {
        if (draggingMode == DraggingMode.MOVE_PINION) {
            Point2D.Double newCenter = model.getPinionGearPosition();
            double newRadius = model.getPinionGearRadius();
            Point2D.Double penPos = model.getPenPosition();

            double dx = penPos.x - newCenter.x;
            double dy = penPos.y - newCenter.y;
            double dist = Math.hypot(dx, dy);
            if (dist > newRadius) {
                dist = newRadius;
                double angle = Math.atan2(dy, dx);
                penPos.setLocation(newCenter.x + dist * Math.cos(angle), newCenter.y + dist * Math.sin(angle));
                model.getPinionGear().getPen().setPosition(penPos);
            }
            model.getPinionGear().alpha = Math.atan2(dy, dx);

            // --- 追加: スパーギア中心とピニオンギア中心の角度をthetaOffsetにセット ---
            Point2D.Double spurCenter = model.getSpurGearPosition();
            if (spurCenter != null && newCenter != null) {
                double thetaOffset = -Math.atan2(newCenter.y - spurCenter.y, newCenter.x - spurCenter.x);
                model.getPinionGear().setThetaOffset(thetaOffset);
            }

            model.startNewPathSegment();
            model.resetSpirographTime();
        }
        draggingMode = DraggingMode.NONE;
        model.mouseReleased(e_release.getPoint());
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
                    model.setSpurRadius(newRadius); // ドラッグ中も半径を即時変更
                }
                break;

            case MOVE_PINION:
                Point2D spurCenter = model.getSpurGearPosition();
                if (spurCenter == null) break;

                double spurRadius = model.getSpurGearRadius();

                double dxRaw = currentWorld.getX() - spurCenter.getX();
                double dyRaw = currentWorld.getY() - spurCenter.getY();
                double dist = Math.hypot(dxRaw, dyRaw);
                if (dist < 5.0 / view.getScale()) break;

                double unitX = dxRaw / dist;
                double unitY = dyRaw / dist;

                double newPinionRadius = Math.abs(spurRadius - dist);

                double minRadius = 5.0;
                if (newPinionRadius < minRadius) {
                    newPinionRadius = minRadius;
                }

                double hysteresis = 2.0 / view.getScale();
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
                model.movePenBy(dx, dy);
                break;

            case PAN:
                int panDx = currentPoint.x - this.pressPoint.x;
                int panDy = currentPoint.y - this.pressPoint.y;
                view.pan(panDx, panDy);
                break;

            default:
                break;
        }

        this.pressPoint = currentPoint;
        this.pressWorldPoint = currentWorld;

        view.repaint();
        model.mouseDragged(currentPoint);
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
                // --- 追加: ロード済み軌跡をクリア ---
                view.clearLoadedLocusData();
                model.start();
                break;
            case "Stop":
                model.stop();
                break;
            case "Clear":
                model.resetGears();
                model.stop();
                view.clearLoadedLocusData();
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
                        model.stop();
                    } else {
                        view.displaySaveSuccessMessage("読み込みに失敗しました。");
                    }
                }
                break;
            case "Small":
                model.changePenSize(1.0);
                view.repaint();
                break;
            case "Medium":
                model.changePenSize(2.0);
                view.repaint();
                break;
            case "Large":
                model.changePenSize(3.0);
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
        model.changePenColor(color); // Modelの新しい色変更メソッドを呼び出す
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
