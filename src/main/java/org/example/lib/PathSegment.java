package org.example.lib;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * スピログラフの軌跡の一部分（セグメント）を表すクラス。
 * このセグメントは、特定のペン色で描画された点のシーケンスを保持する。
 */
public class PathSegment implements Serializable {
    private static final long serialVersionUID = 1L; // シリアライズID

    /** このセグメントを構成する点のリスト */
    private List<Point2D.Double> points;
    /** このセグメントの色 */
    private Color color;

    /**
     * 指定された色で新しいPathSegmentを初期化する。
     * @param color このセグメントの描画色
     */
    public PathSegment(Color color) {
        this.color = color;
        this.points = new ArrayList<>();
    }

    /**
     * 指定された色と既存の点のリストで新しいPathSegmentを初期化する。
     * (デシリアライズ後やクローン作成時に便利)
     * @param color このセグメントの描画色
     * @param points このセグメントの点リスト
     */
    public PathSegment(Color color, List<Point2D.Double> points) {
        this.color = color;
        this.points = new ArrayList<>(points); // ポイントリストをコピーして安全性を高める
    }

    /**
     * セグメントに点を追加する。
     * @param point 追加する点
     */
    public void addPoint(Point2D.Double point) {
        this.points.add(point);
    }

    /**
     * このセグメントの点のリストを取得する。
     * @return 点のリスト
     */
    public List<Point2D.Double> getPoints() {
        return points;
    }

    /**
     * このセグメントの色を取得する。
     * @return セグメントの色
     */
    public Color getColor() {
        return color;
    }
}
