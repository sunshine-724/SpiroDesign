/*
 * View.java - スピログラフ描画アプリケーションのViewコンポーネント
 *
 * このクラスは、MVC（Model-View-Controller）モデルにおけるViewの役割を担う。
 *
 * **変更点履歴:**
 * - ロードされた軌跡データとペン情報（色、太さ）を受け取るための `setLocusData` メソッドを追加した。
 * - `displaySpirographLocus` メソッドでの軌跡描画ロジックを、ロードされたデータとModelの現在のデータで適切に切り替えるように変更した。
 * - スケール変更の範囲チェックにおける論理エラーを修正した。
 * - **保存成功時に一時的なメッセージを画面に表示するための `displaySaveSuccessMessage` メソッドと、関連する描画ロジックを追加した。**
 *
 * **他クラスの必要な変更点:**
 * - **Model.java**:
 * - `loadData()` メソッド内で、ファイルから読み込んだ軌跡データ (`locus`) とペン情報 (`Pen`オブジェクトから色と太さ) を取得し、`View`の`setLocusData(locus, penColor, penSize)`メソッドを呼び出す必要がある。
 * - **`saveData()` メソッドが成功した際に、`View`の`displaySaveSuccessMessage("保存しました！")`のようなメソッドを呼び出す必要がある。**
 * - `Model`クラスと、`Model`が内部に持つ全てのカスタムクラス（`SpurGear`, `PinionGear`など）は、ファイルへの保存・読み込みのために `java.io.Serializable` インターフェースを実装する必要がある。
 * - **Pen.java**:
 * - `Pen`クラスは、ファイルへの保存・読み込みのために `java.io.Serializable` インターフェースを実装する必要がある。
 * - **Controller.java**:
 * - ViewのインスタンスにController自身をマウスリスナーとして追加する。
 * - スパーギア定義モードとピニオンギア定義モードの管理、マウスイベントからの半径計算、Modelへの更新ロジックを追加する。
 * - ピニオンギア定義モードはスパーギア定義モードよりも優先度が高いことを考慮したロジックを実装する。
 * - マウスイベントの `mousePressed` で、スパーギアまたはピニオンギアの半径定義モード、あるいはパンモードを開始するロジックを実装する。
 * - マウスイベントの `mouseDragged` で、現在のモードに応じて以下のいずれかの処理を行う。
 * - スパーギア半径定義中: Viewの `setSpurGearCenterScreen` と `setCurrentDragPointScreen` を利用して仮描画を更新する。
 * - ピニオンギア半径定義中: Viewの `setPinionGearCenterScreen` と `setCurrentDragPointScreenForPinion` を利用して仮描画を更新する。
 * - パンモード: Viewの `getViewOffset()` を取得し、ドラッグ量に基づいて新しいオフセットを計算し、`setViewOffset()` で設定してViewをパンする。
 * - マウスイベントの `mouseReleased` で、現在のモードを終了し、Modelに最終的な半径や位置を通知するロジックを実装する。
 * - マウスホイールイベント `mouseWheelMoved` で、`scaling` メソッドを呼び出す際に、`shift` キーの押下状態に応じて拡大/縮小を制御する。
 * - **`Save`ボタンのアクションリスナー内で、Modelの `saveData()` 呼び出しが成功した場合に、`View`の `displaySaveSuccessMessage()` メソッドを呼び出すようにする。**
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
import java.util.List;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.example.model.Model;
import java.io.File;
import javax.swing.Timer; // Timer をインポート
import java.awt.Font; // Font をインポート
import java.awt.FontMetrics; // FontMetrics をインポート


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
    // ロードされたペンの色と太さを保持する変数
    private Color loadedPenColor = null;
    private double loadedPenSize = -1.0; // 未設定を示す値

    // 保存成功メッセージ表示用の変数
    private String saveMessage = null;
    private Timer messageTimer;
    private static final int MESSAGE_DISPLAY_DURATION = 2000; // メッセージ表示期間 (ミリ秒)

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

        // メッセージ表示タイマーの初期化
        messageTimer = new Timer(MESSAGE_DISPLAY_DURATION, e -> {
            saveMessage = null; // メッセージをクリア
            repaint(); // 再描画を促す
            messageTimer.stop(); // タイマーを停止
        });
        messageTimer.setRepeats(false); // 一度だけ実行

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

        // 保存成功メッセージの描画
        if (saveMessage != null) {
            drawSaveMessage(g2d);
        }
    }

    /**
     * 保存成功メッセージを描画する。
     *
     * @param g2d Graphics2Dオブジェクト
     */
    private void drawSaveMessage(Graphics2D g2d) {
        // フォントと色を設定
        Font font = new Font("SansSerif", Font.BOLD, 24);
        g2d.setFont(font);
        FontMetrics metrics = g2d.getFontMetrics(font);
        int stringWidth = metrics.stringWidth(saveMessage);
        int stringHeight = metrics.getHeight();

        // メッセージボックスのパディング
        int padding = 20;
        int boxWidth = stringWidth + 2 * padding;
        int boxHeight = stringHeight + 2 * padding;

        // 画面中央に配置
        int x = (getWidth() - boxWidth) / 2;
        int y = (getHeight() - boxHeight) / 2;

        // 半透明の背景ボックス
        g2d.setColor(new Color(0, 0, 0, 150)); // 黒色で半透明
        g2d.fillRoundRect(x, y, boxWidth, boxHeight, 20, 20); // 角丸の四角

        // メッセージテキスト
        g2d.setColor(Color.WHITE); // 白色テキスト
        g2d.drawString(saveMessage, x + padding, y + padding + metrics.getAscent());
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

        List<Point2D.Double> locusToDraw;
        Color penColorToUse;
        double penSizeToUse;

        // ロードされた軌跡データとペン情報が存在する場合はそちらを優先
        if (loadedLocusData != null && !loadedLocusData.isEmpty() && loadedPenColor != null && loadedPenSize != -1.0) {
            locusToDraw = loadedLocusData;
            penColorToUse = loadedPenColor;
            penSizeToUse = loadedPenSize;
        } else {
            // ロードされたデータがない場合、Modelの現在の軌跡とペン情報を使用
            locusToDraw = model.getLocus();
            penColorToUse = model.getPenColor();
            penSizeToUse = model.getPenSize();
        }

        g2d.setColor(penColorToUse);
        g2d.setStroke(new BasicStroke((float) penSizeToUse));


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

        if (newScale >= MIN_SCALE && newScale <= MAX_SCALE) {
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
     * Modelからロードされた軌跡データ、ペンの色、ペンの太さを受け取り、Viewに設定する。
     * このデータは、`displaySpirographLocus`メソッドで描画される。
     *
     * @param locus ロードされた軌跡データのリスト
     * @param penColor ロードされたペンの色
     * @param penSize ロードされたペンの太さ
     */
    public void setLocusData(List<Point2D.Double> locus, Color penColor, double penSize) {
        this.loadedLocusData = locus;
        this.loadedPenColor = penColor;
        this.loadedPenSize = penSize;
        // ロードされた軌跡が表示されるように再描画を促す
        repaint();
    }

    /**
     * ロードされた軌跡データとペンの情報をクリアする。
     * これにより、動的に生成される軌跡が再び描画されるようになる。
     */
    public void clearLoadedLocusData() {
        this.loadedLocusData = null;
        this.loadedPenColor = null;
        this.loadedPenSize = -1.0; // 未設定の状態に戻す
        repaint();
    }

    /**
     * 保存成功メッセージを表示する。
     *
     * @param message 表示するメッセージ文字列
     */
    public void displaySaveSuccessMessage(String message) {
        this.saveMessage = message;
        repaint(); // メッセージを表示するために即座に再描画を促す
        if (messageTimer.isRunning()) {
            messageTimer.restart(); // 既に実行中の場合、タイマーをリスタート
        } else {
            messageTimer.start(); // タイマーを開始
        }
    }
}
