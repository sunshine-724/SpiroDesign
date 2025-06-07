/*
 * View.java - スピログラフ描画アプリケーションのViewコンポーネント
 *
 * このクラスは、MVC（Model-View-Controller）モデルにおけるViewの役割を担う。
 *
 * **変更点履歴:**
 * - `displayDrawPen`および`displaySpirographPath`（現`displaySpirographLocus`）メソッド内の
 * 重複する変数宣言とメソッド呼び出しを修正し、`model.getPenSize()`を使用するよう変更した。
 * - UIコンポーネント（ボタン）に「Save」と「Load」ボタンを追加した。
 * - スピログラフの軌跡描画メソッド`displaySpirographPath`を`displaySpirographLocus`に名称変更し、
 * Modelから軌跡の全データ（`List<Point2D.Double> locus`）を取得して描画するようロジックを修正した。
 * また、Modelがこのデータを提供するよう変更が必要である旨の重要なコメントを追加した。
 * - ユーザー画面移動（パン）機能のためのマウスリスナーをView内部から削除した。
 * これはControllerがマウスイベントを一元的に処理するためである。
 * - スパーギアの半径をピッキングしてドラッグで決定できるようにするための機能を追加した。
 * - `isDefiningSpurGear`, `spurGearCenterScreen`, `currentDragPointScreen`変数を追加した。
 * - `setDefiningSpurGear`, `setSpurGearCenterScreen`, `setCurrentDragPointScreen`, `clearSpurGearDefinition`
 * といったControllerがViewの状態を操作するための公開メソッドを追加した。
 * - `screenToWorld`ヘルパーメソッドを追加し、画面座標をワールド座標に変換できるようにした。
 * - `paintComponent`メソッド内で、スパーギア定義中に一時的な円を描画するロジックを追加した。
 * - ピニオンギアの半径をピッキングしてドラッグで決定できるようにする機能を追加した。（スパーギアより優先）
 * - `isDefiningPinionGear`, `pinionGearCenterScreen`, `currentDragPointScreenForPinion`変数を追加した。
 * - `setDefiningPinionGear`, `setPinionGearCenterScreen`, `setCurrentDragPointScreenForPinion`, `clearPinionGearDefinition`
 * といったControllerがViewの状態を操作するための公開メソッドを追加した。
 * - `paintComponent`メソッド内で、ピニオンギア定義中に一時的な円を描画するロジックを追加した。この描画はスパーギアの仮描画よりも優先されるように（コードの後のほうに）配置した。
 * - コード全体で「Path」という用語を「Locus」に変更した。
 * - **Modelの `notifyViewsLoading()` メソッドが呼び出す `getLocus(List<Point2D.Double> locus)` に合わせて、このメソッドを追加し、ロードされた軌跡データを描画に利用するように変更した。**
 * - **`displaySpirographLocus` メソッドが、Modelから取得した `locus` または `getLocus` で設定された `loadedLocusData` を描画するように変更した。**
 *
 * **他クラスの必要な変更点:**
 * - **Model.java**:
 * - 軌跡（Locus）データを保持する`List<Point2D.Double> locus`フィールドを追加する必要がある。
 * - `updateData()`メソッド内で、現在のペン位置を`locus`リストに追加する処理が必要である。
 * - `public List<Point2D.Double> getLocus()`メソッドを追加する必要がある。
 * - `Pen`クラスの`getPenSize()`メソッドが存在することを前提としている。
 * - **Controller.java**:
 * - `mouseDrag`メソッドは、標準の`MouseMotionListener`インターフェースの`mouseDragged`メソッドに名称を変更する必要がある。
 * - ViewのインスタンスにController自身をマウスリスナーとして追加する必要がある。
 * - スパーギア定義モードと**ピニオンギア定義モード**の管理、マウスイベントからの半径計算、Modelへの更新ロジックを追加する必要がある。
 * - **ピニオンギア定義モードはスパーギア定義モードよりも優先度が高いことを考慮したロジックを実装する必要がある。**
 */

package org.example.view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.RenderingHints;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.AffineTransform;
import java.util.Map;
import java.util.HashMap;
import java.util.List; // List をインポート
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.example.model.Model;
import java.io.File;

public class View extends JPanel {

    private Model model;

    public JPanel MenuDisplay;

    public Map<String, JButton> subButton;

    public JTextField speedDisplay;

    public Map<String, JButton> penSizeDisplay;

    public JColorChooser colorPalletDisplay;

    private double scale = 1.0; // デフォルトスケール（100%）
    private static final double MIN_SCALE = 0.5; // 最小スケール（50%）
    private static final double MAX_SCALE = 2.0; // 最大スケール（200%）

    // ユーザー画面移動（パン）用の変数 (Controllerによって管理されるため、Viewからは直接操作しない)
    private Point2D.Double viewOffset = new Point2D.Double(0, 0); // 描画内容のオフセット（ワールド座標系）

    // スパーギアの半径定義用の変数
    private boolean isDefiningSpurGear = false; // スパーギアの半径定義中か
    private Point spurGearCenterScreen = null; // スパーギアの中心点（画面座標）
    private Point currentDragPointScreen = null; // 現在のドラッグ点（画面座標）

    // ピニオンギアの半径定義用の変数
    private boolean isDefiningPinionGear = false; // ピニオンギアの半径定義中か
    private Point pinionGearCenterScreen = null; // ピニオンギアの中心点（画面座標）
    private Point currentDragPointScreenForPinion = null; // 現在のドラッグ点（ピニオンギア用、画面座標）

    // ロードされた軌跡データを保持する変数
    private List<Point2D.Double> loadedLocusData = null;

    public View(Model model) {
        this.model = model;

        // レイアウトマネージャーは要件によって適切なものを選択する（例: BorderLayout, GridLayoutなど）。
        // 現在はnullレイアウトを使用しているが、これはUIの柔軟性を低下させる。
        this.setLayout(null);
        this.setBackground(Color.WHITE);

        // UIコンポーネントの初期化
        this.MenuDisplay = new JPanel();
        // MenuDisplayも適切なレイアウトマネージャーを使用することを推奨する。
        MenuDisplay.setLayout(null);

        // ボタンのマップを初期化
        this.subButton = new HashMap<>();
        this.penSizeDisplay = new HashMap<>();

        // subButtonマップにボタンを作成し追加
        String[] buttonNames = { "Pen", "SpurGear", "PinionGear", "Start", "Stop", "Clear", "Save", "Load" };
        for (String name : buttonNames) {
            JButton button = new JButton(name);
            subButton.put(name, button);
            MenuDisplay.add(button);
            // アクションリスナーはControllerで追加する必要がある。
        }

        // penSizeDisplayマップにボタンを作成し追加
        String[] penSizes = { "Small", "Medium", "Large" };
        for (String size : penSizes) {
            JButton button = new JButton(size);
            penSizeDisplay.put(size, button);
            MenuDisplay.add(button);
        }

        // スピード表示用テキストフィールドを初期化
        this.speedDisplay = new JTextField("0.0");
        speedDisplay.setEditable(true);
        MenuDisplay.add(speedDisplay);

        // カラーチューザーを初期化し、メニューパネルに追加
        this.colorPalletDisplay = new JColorChooser();
        MenuDisplay.add(colorPalletDisplay);

        // メインパネルにMenuDisplayを追加
        this.add(MenuDisplay);

        // コンポーネントのサイズと位置を設定（仮の値、レイアウトマネージャーの使用を強く推奨）
        MenuDisplay.setBounds(10, 10, 200, 600);
        int yOffset = 10;
        for (JButton button : subButton.values()) {
            button.setBounds(10, yOffset, 180, 30);
            yOffset += 35;
        }
        speedDisplay.setBounds(10, yOffset, 180, 30);
        yOffset += 35;
        for (JButton button : penSizeDisplay.values()) {
            button.setBounds(10, yOffset, 180, 30);
            yOffset += 35;
        }
        colorPalletDisplay.setBounds(10, yOffset, 180, 250);

        this.setVisible(true);

        // View内部のマウスイベントリスナーは削除された。
        // ControllerがViewにMouseListenerとMouseMotionListenerを追加し、
        // すべてのマウスイベントを処理する。
    }

    /**
     * コンポーネントの描画メソッド
     *
     * @param g グラフィックスコンテキスト
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(getBackground());
        g2d.fillRect(0, 0, getWidth(), getHeight());

        if (model == null) {
            return;
        }

        AffineTransform originalTransform = g2d.getTransform();

        // ユーザー画面移動（パン）の適用
        // viewOffsetはワールド座標系でのオフセット
        g2d.translate(viewOffset.x, viewOffset.y);

        // スケーリングの適用
        g2d.scale(scale, scale);

        // スパーギアの描画
        Point2D.Double spurPosition = model.getSpurGearPosition();
        if (spurPosition != null) {
            displaySpur(g2d, spurPosition);
        }

        // ピニオンギアの描画
        Point2D.Double pinionPosition = model.getPinionGearPosition();
        if (pinionPosition != null) {
            displayPinion(g2d, pinionPosition);
        }

        // スピログラフの軌跡を描画
        displaySpirographLocus(g2d);

        // ペンの描画 (現在のペン先の位置)
        Point2D.Double penPosition = model.getPenPosition();
        Color penColor = model.getPenColor();
        if (penPosition != null) {
            displayDrawPen(g2d, penPosition, penColor);
        }

        // スパーギア定義中の仮描画
        if (isDefiningSpurGear && spurGearCenterScreen != null) {
            Point2D.Double centerWorld = screenToWorld(spurGearCenterScreen);
            double tempRadius = 0;
            if (currentDragPointScreen != null) {
                // 画面座標での距離を計算し、ワールド座標での半径に変換
                tempRadius = spurGearCenterScreen.distance(currentDragPointScreen) / scale;
            }

            g2d.setColor(Color.ORANGE); // 仮の描画色
            g2d.setStroke(new BasicStroke(1.5f));
            // 円の左上座標を計算（中心座標から半径分を引く）
            double x = centerWorld.x - tempRadius;
            double y = centerWorld.y - tempRadius;
            g2d.drawOval((int) x, (int) y, (int) (tempRadius * 2), (int) (tempRadius * 2));
        }

        // ピニオンギア定義中の仮描画（スパーギアより優先）
        if (isDefiningPinionGear && pinionGearCenterScreen != null) {
            Point2D.Double centerWorld = screenToWorld(pinionGearCenterScreen);
            double tempRadius = 0;
            if (currentDragPointScreenForPinion != null) {
                // 画面座標での距離を計算し、ワールド座標での半径に変換
                tempRadius = pinionGearCenterScreen.distance(currentDragPointScreenForPinion) / scale;
            }

            g2d.setColor(Color.MAGENTA); // 仮の描画色（ピニオンギア用）
            g2d.setStroke(new BasicStroke(1.5f));
            // 円の左上座標を計算（中心座標から半径分を引く）
            double x = centerWorld.x - tempRadius;
            double y = centerWorld.y - tempRadius;
            g2d.drawOval((int) x, (int) y, (int) (tempRadius * 2), (int) (tempRadius * 2));
        }


        g2d.setTransform(originalTransform);

        // 現在のスケールを表示 (スケールが元に戻された後に描画)
        g2d.setColor(Color.BLACK);
        g2d.drawString("Scale: " + getScalePercent(), 10, getHeight() - 10);
    }

    /**
     * ピニオンギアを描画するメソッド
     *
     * @param g        Graphics2Dオブジェクト
     * @param position ピニオンギアの中心位置 (Modelからの生座標)
     */
    public void displayPinion(Graphics2D g, Point2D.Double position) {
        Color originalColor = g.getColor();
        java.awt.Stroke originalStroke = g.getStroke();

        g.setColor(Color.BLUE);
        g.setStroke(new BasicStroke(2.0f));
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double radius = model.getPinionGearRadius();
        double x = position.x - radius;
        double y = position.y - radius;

        g.drawOval((int) x, (int) y, (int) (radius * 2), (int) (radius * 2));

        g.setColor(originalColor);
        g.setStroke(originalStroke);
    }

    public void displayMousePointer(Graphics2D g, Point2D.Double position, Color color) {
        // 未実装
    }

    /**
     * スパーギアを描画するメソッド
     *
     * @param g        描画コンテキスト
     * @param position スパーギアの中心位置 (Modelからの生座標)
     */
    public void displaySpur(Graphics2D g, Point2D.Double position) {
        Color originalColor = g.getColor();
        java.awt.Stroke originalStroke = g.getStroke();

        g.setColor(Color.RED);
        g.setStroke(new BasicStroke(2.0f));
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double radius = model.getSpurGearRadius();
        double x = position.x - radius;
        double y = position.y - radius;

        g.drawOval((int) x, (int) y, (int) (radius * 2), (int) (radius * 2));

        g.setColor(originalColor);
        g.setStroke(originalStroke);
    }

    /**
     * ペンポイントを描画するメソッド
     *
     * @param g        描画コンテキスト
     * @param position ペンポイントの位置 (Modelからの生座標)
     * @param color    ペンの色
     */
    public void displayDrawPen(Graphics2D g, Point2D.Double position, Color color) {
        Color originalColor = g.getColor();
        java.awt.Stroke originalStroke = g.getStroke();

        g.setColor(color != null ? color : Color.GREEN);
        double penSize = model.getPenSize();
        g.setStroke(new BasicStroke((float) penSize));

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.fillOval((int) (position.x - penSize / 2), (int) (position.y - penSize / 2), (int) penSize, (int) penSize);

        g.setColor(originalColor);
        g.setStroke(originalStroke);
    }

    /**
     * スピログラフの軌跡（Locus）を描画するメソッド
     *
     * @param g2d 描画コンテキスト
     */
    private void displaySpirographLocus(Graphics2D g2d) {
        Color originalColor = g2d.getColor();
        java.awt.Stroke originalStroke = g2d.getStroke();

        g2d.setColor(model.getPenColor());
        g2d.setStroke(new BasicStroke((float) model.getPenSize()));

        // ロードされた軌跡データが存在すればそちらを優先し、なければModelの現在の軌跡を使用する
        List<Point2D.Double> locusToDraw = (loadedLocusData != null && !loadedLocusData.isEmpty()) ?
                                            loadedLocusData : model.getLocus();


        if (locusToDraw != null && locusToDraw.size() > 1) {
            // 軌跡の点を線で結んで描画
            for (int i = 0; i < locusToDraw.size() - 1; i++) {
                Point2D.Double p1 = locusToDraw.get(i);
                Point2D.Double p2 = locusToDraw.get(i + 1);
                g2d.drawLine((int) p1.x, (int) p1.y, (int) p2.x, (int) p2.y);
            }
        }

        g2d.setColor(originalColor);
        g2d.setStroke(originalStroke);
    }

    /**
     * 画面の拡大縮小を行うメソッド
     *
     * @param zoomIn trueなら拡大、falseなら縮小
     */
    public void scaling(boolean zoomIn) {
        double scaleChange = zoomIn ? 0.1 : -0.1;
        double newScale = scale + scaleChange;

        if (newScale >= MIN_SCALE && newScale >= MAX_SCALE) { // MIN_SCALE と MAX_SCALE を正しく比較
            scale = newScale;
            repaint(); // 新しいスケールで再描画
        }
    }

    /**
     * 現在のスケール係数を取得
     *
     * @return 現在のスケール係数
     */
    public double getScale() {
        return scale;
    }

    /**
     * スケール係数を直接設定
     *
     * @param newScale 設定したいスケール係数（0.5〜2.0）
     */
    public void setScale(double newScale) {
        if (newScale >= MIN_SCALE && newScale <= MAX_SCALE) {
            scale = newScale;
            repaint(); // 新しいスケールで再描画
        }
    }

    /**
     * スケールのパーセント表示を取得（表示用）
     *
     * @return スケールのパーセント表示（例: "100%"）
     */
    public String getScalePercent() {
        return (int) (scale * 100) + "%";
    }

    /**
     * ファイル保存場所を選択する
     *
     * @return 選択されたファイルパス（Fileオブジェクト）
     */
    public File chooseSaveFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }

    /**
     * ファイル読み込み場所を選択する
     *
     * @return 選択されたファイルパス（Fileオブジェクト）
     */
    public File chooseLoadFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }

    /**
     * 画面座標をワールド座標に変換する。
     *
     * @param screenPoint 画面座標
     * @return ワールド座標
     */
    public Point2D.Double screenToWorld(Point screenPoint) {
        // オフセットとスケールを考慮して変換
        double worldX = (screenPoint.getX() / scale) - (viewOffset.x / scale);
        double worldY = (screenPoint.getY() / scale) - (viewOffset.y / scale);
        return new Point2D.Double(worldX, worldY);
    }

    /**
     * スパーギアの半径定義モードを設定する。
     *
     * @param defining trueの場合、定義モードを有効にする。
     */
    public void setDefiningSpurGear(boolean defining) {
        this.isDefiningSpurGear = defining;
        if (!defining) {
            clearSpurGearDefinition(); // 定義モード終了時に状態をクリア
        }
        repaint(); // 描画を更新
    }

    /**
     * スパーギアの中心点（画面座標）を設定する。
     *
     * @param p 中心点（画面座標）
     */
    public void setSpurGearCenterScreen(Point p) {
        this.spurGearCenterScreen = p;
        this.currentDragPointScreen = p; // ドラッグ開始時は中心点と同じ
        repaint();
    }

    /**
     * 現在のドラッグ点（画面座標）を設定する。
     *
     * @param p 現在のドラッグ点（画面座標）
     */
    public void setCurrentDragPointScreen(Point p) {
        this.currentDragPointScreen = p;
        repaint();
    }

    /**
     * スパーギア定義に関する状態をクリアする。
     */
    public void clearSpurGearDefinition() {
        this.isDefiningSpurGear = false;
        this.spurGearCenterScreen = null;
        this.currentDragPointScreen = null;
        repaint();
    }

    /**
     * ピニオンギアの半径定義モードを設定する。
     *
     * @param defining trueの場合、定義モードを有効にする。
     */
    public void setDefiningPinionGear(boolean defining) {
        this.isDefiningPinionGear = defining;
        if (!defining) {
            clearPinionGearDefinition(); // 定義モード終了時に状態をクリア
        }
        repaint(); // 描画を更新
    }

    /**
     * ピニオンギアの中心点（画面座標）を設定する。
     *
     * @param p 中心点（画面座標）
     */
    public void setPinionGearCenterScreen(Point p) {
        this.pinionGearCenterScreen = p;
        this.currentDragPointScreenForPinion = p; // ドラッグ開始時は中心点と同じ
        repaint();
    }

    /**
     * 現在のドラッグ点（ピニオンギア用、画面座標）を設定する。
     *
     * @param p 現在のドラッグ点（画面座標）
     */
    public void setCurrentDragPointScreenForPinion(Point p) {
        this.currentDragPointScreenForPinion = p;
        repaint();
    }

    /**
     * ピニオンギア定義に関する状態をクリアする。
     */
    public void clearPinionGearDefinition() {
        this.isDefiningPinionGear = false;
        this.pinionGearCenterScreen = null;
        this.currentDragPointScreenForPinion = null;
        repaint();
    }

    /**
     * 現在のViewの描画オフセットを取得する。
     * Controllerがパンニング計算に利用する。
     *
     * @return 現在の描画オフセット
     */
    public Point2D.Double getViewOffset() {
        return viewOffset;
    }

    /**
     * Viewの描画オフセットを設定する。
     * Controllerがパンニング計算後にViewを更新する。
     *
     * @param offset 新しい描画オフセット
     */
    public void setViewOffset(Point2D.Double offset) {
        this.viewOffset = offset;
        repaint();
    }

    /**
     * Modelからロードされた軌跡データを受け取り、Viewに設定する。
     * このメソッドは、Modelの notifyViewsLoading() から呼び出されることを想定している。
     *
     * @param locus ロードされた軌跡データのリスト
     */
    public void getLocus(List<Point2D.Double> locus) {
        this.loadedLocusData = locus;
        // ロードされた軌跡が表示されるように再描画を促す
        repaint();
    }

    /**
     * ロードされた軌跡データをクリアする。
     * これにより、動的に生成される軌跡が再び描画されるようになる。
     */
    public void clearLoadedLocusData() {
        this.loadedLocusData = null;
        repaint();
    }
}
