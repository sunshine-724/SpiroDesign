package org.example.controller;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;

import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import org.example.model.Model;
import org.example.view.View;

/**
 * Controllerクラスは、ビューとモデルを結びつけ、マウスイベントを処理します。
 * ユーザーの操作に応じて、モデルの状態を更新し、ビューを再描画します。
 */
    public class Controller extends MouseInputAdapter implements MouseWheelListener {
    protected Model model;
    protected View view;

    /**
     * ドラッグモードを定義する列挙型
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
    }

    /*public void mouseWheelMoved(MouseWheelEvent e_wheel) {
        Integer amount = -e_wheel.getWheelRotation();
        int modifiers = e_wheel.getModifiersEx();
        boolean isShiftDown = (modifiers & MouseEvent.SHIFT_DOWN_MASK) != 0;
        if (amount == 0)
            return;
        Point scroll = new Point(0, amount);
        if (isShiftDown)
            scroll = new Point(amount, 0);
        view.scaling(isShiftDown);
    }*/

    public void mouseWheelMoved(MouseWheelEvent e_wheel) {
        double rotation = e_wheel.getPreciseWheelRotation();

        // 拡大・縮小率の計算：回転が小さいほど緩やかにズーム
        double zoomFactor = Math.pow(1.1, -rotation);

        // マウスカーソル位置を中心にズームを実行（View クラス側で処理）
        view.zoomAt(e_wheel.getPoint(), zoomFactor);
    }

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

    public void mousePressed(MouseEvent e_press) {
        Point pressedPoint = e_press.getPoint();
        Point2D pressWorldPoint = view.screenToWorld(pressedPoint);

        if (e_press.isPopupTrigger() || SwingUtilities.isRightMouseButton(e_press)) {
        view.showMenu(pressedPoint.x, pressedPoint.y);
        return;
    }

        Point2D spurCenter = model.getSpurGearPosition();
        double spurRadius = model.getSpurGearRadius();
        Point2D pinionCenter = model.getPinionGearPosition();
        double pinionRadius = model.getPinionGearRadius();
        Point2D penPosition = model.getPenPosition();

        double distToSpur = pressWorldPoint.distance(spurCenter);
        double distToPinion = pressWorldPoint.distance(pinionCenter);
        double distToPen = pressWorldPoint.distance(model.getPenPosition());

        if (distToSpur < 10) {
            draggingMode = DraggingMode.MOVE_SPUR_CENTER;
        } else if (Math.abs(distToSpur - spurRadius) < 10) {
            draggingMode = DraggingMode.RESIZE_SPUR_RADIUS;
        } else if (distToPinion < 10) {
            draggingMode = DraggingMode.MOVE_PINION;
        } else if (distToPen < 10) {
            draggingMode = DraggingMode.NONE;
        } else {
            draggingMode = DraggingMode.PAN;
        }

        model.mousePressed(pressedPoint);
    }

    public void mouseReleased(MouseEvent e_release) {
        draggingMode = DraggingMode.NONE;
        model.mouseReleased(e_release.getPoint());
    }

    public void mouseDragged(MouseEvent e_drag) {
        Point currentPoint = e_drag.getPoint();
        this.pressPoint = currentPoint;
        Point2D currentWorld = view.screenToWorld(currentPoint);
        this.pressWorldPoint = currentWorld;
        double dx = currentWorld.getX() - pressWorldPoint.getX();
        double dy = currentWorld.getY() - pressWorldPoint.getY();

        switch (draggingMode) {
            case MOVE_SPUR_CENTER:
                model.moveSpurGearBy(dx, dy);
                break;

            case RESIZE_SPUR_RADIUS:
                double newRadius = currentWorld.distance(model.getSpurGearPosition());
                model.setSpurRadius(newRadius);
                view.repaint();
                break;

            case MOVE_PINION:
                Point2D spurCenter = model.getSpurGearPosition();
                double spurRadius = model.getSpurGearRadius();

                double dxRaw = currentWorld.getX() - spurCenter.getX();
                double dyRaw = currentWorld.getY() - spurCenter.getY();
                double dist = Math.hypot(dxRaw, dyRaw);
                if (dist < 5.0) break;

                double unitX = dxRaw / dist;
                double unitY = dyRaw / dist;

                double newPinionRadius = Math.abs(spurRadius - dist);

                double minRadius = 5.0;
                if (newPinionRadius < minRadius) {
                    newPinionRadius = minRadius;
                }

                double hysteresis = 2.0;
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
                int panDx = currentPoint.x - pressPoint.x;
                int panDy = currentPoint.y - pressPoint.y;
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
}
