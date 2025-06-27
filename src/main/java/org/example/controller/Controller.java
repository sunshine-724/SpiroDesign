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
 * Controllerクラスは、ビューとモデルを結びつけ、マウスイベントを処理します。 ユーザーの操作に応じて、モデルの状態を更新し、ビューを再描画します。
 */
public class Controller extends MouseInputAdapter implements MouseWheelListener {

    protected Model model;
    protected View view;

    /**
     * ドラッグモードを定義する列挙型 NONE: ドラッグしていない状態 MOVE_SPUR_CENTER: スパーギアの中心を移動
     * MOVE_PINION: ピニオンギアを移動 RESIZE_SPUR_RADIUS: スパーギアの半径を変更 PAN: ビューをパン（移動）
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
     *
     * @param view ビューオブジェクト
     * @param model モデルオブジェクト
     */
    public Controller(View view, Model model) {
        this.view = view;
        this.model = model;
        view.addMouseListener(this);
        view.addMouseMotionListener(this);
        view.addMouseWheelListener(this);

        Point2D pinionCenter = model.getPinionGearPosition();
        double pinionRadius = model.getPinionGearRadius();

        double angle = Math.toRadians(45);
        double penDistance = pinionRadius * 0.7;
        double penX = pinionCenter.getX() + penDistance * Math.cos(angle);
        double penY = pinionCenter.getY() + penDistance * Math.sin(angle);

        Point2D initialPenPosition = new Point2D.Double(penX, penY);
        model.setPenPosition(initialPenPosition);
        model.setPenVisible(true);

    }

    public void mouseWheelMoved(MouseWheelEvent e_wheel) {
        double rotation = e_wheel.getPreciseWheelRotation();
        double zoomFactor = Math.pow(1.1, -rotation);
        view.zoomAt(e_wheel.getPoint(), zoomFactor);
    }

    public void mouseClicked(MouseEvent e_click) {
        Point clickedpoint = e_click.getPoint();
        Point2D worldClicked = view.screenToWorld(clickedpoint);
        Point2D pinionCenter = model.getPinionGearPosition();
        double distance = worldClicked.distance(pinionCenter);
        double radius = model.getPinionGearRadius();
        if (distance <= radius * 0.9) {
            double minDistance = 10.0;
            Point2D penPosition = worldClicked;

            if (distance < minDistance) {
                double offset = minDistance - distance + 1.0;
                penPosition = new Point2D.Double(
                    pinionCenter.getX() + offset / Math.sqrt(2),
                    pinionCenter.getY() - offset / Math.sqrt(2)
                );
            }

            model.setPenPosition(worldClicked);
            model.setPenVisible(true);
            view.repaint();
            return;
        }
        model.mouseClicked(clickedpoint);
    }

    public void mousePressed(MouseEvent e_press) {
        Point pressedPoint = e_press.getPoint();
        Point2D pressWorldPoint = view.screenToWorld(pressedPoint);
        this.pressPoint = pressedPoint;
        this.pressWorldPoint = pressWorldPoint;

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
        Point2D currentWorld = view.screenToWorld(currentPoint);
        double dx = currentWorld.getX() - pressWorldPoint.getX();
        double dy = currentWorld.getY() - pressWorldPoint.getY();
        Point2D spurCenter = model.getSpurGearPosition();
        Point2D pinionCenter = model.getPinionGearPosition();
        double spurRadius = model.getSpurGearRadius();
        double pinionRadius = model.getPinionGearRadius();
        double dxRaw = 0.0, dyRaw = 0.0, dist = 0.0, unitX = 0.0, unitY = 0.0;

        switch (draggingMode) {
            case MOVE_SPUR_CENTER:
                model.setPenVisible(false);
                model.moveSpurGearBy(dx, dy);
                break;

            case RESIZE_SPUR_RADIUS:
                model.setPenVisible(false);
                double newSpurRadius = currentWorld.distance(spurCenter);
                if (newSpurRadius < 10.0) {
                    newSpurRadius = 10.0;
                }

                double oldSpurRadius = spurRadius;
                double oldPinionRadius = pinionRadius;

                double scale = newSpurRadius / oldSpurRadius;

                double newPinionRadius = oldPinionRadius * scale;
                if (newPinionRadius < 5.0) {
                    newPinionRadius = 5.0;
                }

                dxRaw = pinionCenter.getX() - spurCenter.getX();
                dyRaw = pinionCenter.getY() - spurCenter.getY();
                dist = Math.hypot(dxRaw, dyRaw);
                if (dist < 1e-6) {
                    break;
                }
                unitX = dxRaw / dist;
                unitY = dyRaw / dist;

                double distanceFromSpurCenter = isInner
                        ? newSpurRadius - newPinionRadius
                        : newSpurRadius + newPinionRadius;

                double newCenterX = spurCenter.getX() + unitX * distanceFromSpurCenter;
                double newCenterY = spurCenter.getY() + unitY * distanceFromSpurCenter;
                Point2D newPinionCenter = new Point2D.Double(newCenterX, newCenterY);

                model.setSpurRadius(newSpurRadius);
                model.changePinionGearRadius(newPinionRadius);
                model.setPinionGearPosition(newPinionCenter);
                break;

            case MOVE_PINION:
                model.setPenVisible(false);
                dxRaw = currentWorld.getX() - spurCenter.getX();
                dyRaw = currentWorld.getY() - spurCenter.getY();
                dist = Math.hypot(dxRaw, dyRaw);
                if (dist < 5.0) {
                    break;
                }

                unitX = dxRaw / dist;
                unitY = dyRaw / dist;

                double newPinionR = Math.abs(spurRadius - dist);
                if (newPinionR < 5.0) {
                    newPinionR = 5.0;
                }

                double hysteresis = 2.0;
                double innerLimit = spurRadius - newPinionR + hysteresis;
                double outerLimit = spurRadius + newPinionR - hysteresis;

                if (isInner && dist >= innerLimit) {
                    isInner = false;
                } else if (!isInner && dist <= outerLimit) {
                    isInner = true;
                }

                distanceFromSpurCenter = isInner
                        ? spurRadius - newPinionR
                        : spurRadius + newPinionR;

                double centerX = spurCenter.getX() + unitX * distanceFromSpurCenter;
                double centerY = spurCenter.getY() + unitY * distanceFromSpurCenter;

                model.setPinionGearPosition(new Point2D.Double(centerX, centerY));
                model.changePinionGearRadius(newPinionR);
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
