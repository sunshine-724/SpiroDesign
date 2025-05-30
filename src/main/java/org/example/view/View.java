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
 * - ユーザー画面移動（パン）機能を追加した。
 * - マウスドラッグによる描画領域の移動を可能にするため、`viewOffset`変数を導入した。
 * - `MouseListener`および`MouseMotionListener`を追加し、マウスイベントを処理する。
 * - `paintComponent`メソッド内で、描画前に`viewOffset`を`Graphics2D`の変換に適用する。
 * - コード全体で「Path」という用語を「Locus」に変更した。
 *
 * **主要機能:**
 * - **UIコンポーネントの表示**:
 * - ギア、ペン、スピログラフの軌跡を描画するキャンバスを提供する。
 * - ユーザー操作用のボタン（ペン、ギア、開始/停止、クリア、保存/読み込み）、
 * 速度入力フィールド、カラーピッカーなどのUI要素を配置する。
 * - 現在のスケール表示を行う。
 * - 注意: 現在、UIコンポーネントの配置にはnullレイアウトを使用しているが、
 * 柔軟性と保守性のためにSwingのレイアウトマネージャー（BorderLayout, GridLayout, GridBagLayoutなど）
 * の利用を強く推奨する。
 * - **Modelからのデータ取得と描画**:
 * - Modelクラスから描画に必要なデータ（ギアの位置、半径、ペンの位置、色、サイズ、軌跡データなど）
 * を取得し、それに基づいて画面にスピログラフを描画する。
 * - 特にスピログラフの軌跡（Locus）を描画するためには、Modelがペン位置の履歴を
 * `List<Point2D.Double>` として保持し、`public List<Point2D.Double> getLocus()`
 * メソッドで提供する必要がある。Modelがこのデータを提供しない場合、軌跡は正しく描画されない。
 * - **ユーザー入力の受付（間接的）**:
 * - UIコンポーネント（ボタン、テキストフィールドなど）を公開し、
 * Controllerがこれらのコンポーネントにアクションリスナーを設定できるようにする。
 * View自身は直接的なイベント処理を行わず、Controllerに処理を委譲する。
 * - **画面の拡大縮小機能**:
 * - スピログラフの描画領域を拡大・縮小する機能を提供する。
 * - **ユーザー画面移動（パン）機能**:
 * - マウスドラッグによって描画領域を移動させる機能を提供する。
 * - **ファイル選択ダイアログの提供**:
 * - ファイルの保存・読み込み時に使用するJFileChooserダイアログを提供する。
 *
 * **MVCモデルにおける役割:**
 * - **Modelとの関係性**:
 * - ViewはModelを参照し、Modelのgetterメソッドを通じて描画に必要なデータを読み取る。
 * - ViewはModelの状態を直接変更することはない。Modelの状態変更はControllerを介して行われる。
 * - **Controllerとの関係性**:
 * - ControllerはViewの公開されたUIコンポーネントにイベントリスナーを設定し、
 * ユーザーの操作（ボタンクリック、テキスト入力など）をViewから受け取る。
 * - Controllerは受け取ったユーザー操作に基づいてModelの状態を変更し、
 * 必要に応じてViewの`repaint()`メソッドを呼び出して再描画を指示する。
 */

package org.example.view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.RenderingHints;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Point2D;
import java.awt.geom.AffineTransform;
import java.util.Map;
import java.util.HashMap;
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

    // ユーザー画面移動（パン）用の変数
    private Point2D.Double viewOffset = new Point2D.Double(0, 0); // 描画内容のオフセット（ワールド座標系）
    private Point lastMousePoint; // マウスドラッグ開始時の画面座標
    private boolean isDragging = false; // ドラッグ中かどうか

    public View(Model model) {
        this.model = model;

        this.setLayout(null);
        this.setBackground(Color.WHITE);

        this.MenuDisplay = new JPanel();
        MenuDisplay.setLayout(null);

        this.subButton = new HashMap<>();
        this.penSizeDisplay = new HashMap<>();

        String[] buttonNames = { "Pen", "SpurGear", "PinionGear", "Start", "Stop", "Clear", "Save", "Load" };
        for (String name : buttonNames) {
            JButton button = new JButton(name);
            subButton.put(name, button);
            MenuDisplay.add(button);
        }

        String[] penSizes = { "Small", "Medium", "Large" };
        for (String size : penSizes) {
            JButton button = new JButton(size);
            penSizeDisplay.put(size, button);
            MenuDisplay.add(button);
        }

        this.speedDisplay = new JTextField("0.0");
        speedDisplay.setEditable(true);
        MenuDisplay.add(speedDisplay);

        this.colorPalletDisplay = new JColorChooser();
        MenuDisplay.add(colorPalletDisplay);

        this.add(MenuDisplay);

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

        // マウスイベントリスナーの追加
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastMousePoint = e.getPoint();
                isDragging = true;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isDragging = false;
            }
        });

        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDragging) {
                    // マウスの移動量を計算（画面ピクセル単位）
                    double dx = e.getX() - lastMousePoint.getX();
                    double dy = e.getY() - lastMousePoint.getY();

                    // 画面上の移動量をワールド座標系に変換してオフセットに加算
                    // スケールが適用されているため、移動量をスケールで割る
                    viewOffset.setLocation(viewOffset.x + dx / scale, viewOffset.y + dy / scale);

                    lastMousePoint = e.getPoint(); // 現在のマウス位置を更新
                    repaint(); // 再描画
                }
            }
        });
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

        // Modelから軌跡の全データを取得する。
        // 重要: Penクラスのmoveメソッドで計算されたペン位置をModelが適切に収集し、
        // その履歴を List<Point2D.Double> として保持し、
        // public List<Point2D.Double> getLocus() メソッドで提供するよう
        // Modelクラスを変更する必要がある。
        // Modelが現在の実装のままだと、この描画は正しく機能しない。
        java.util.List<Point2D.Double> locus = model.getLocus();

        if (locus != null && locus.size() > 1) {
            // 軌跡の点を線で結んで描画
            for (int i = 0; i < locus.size() - 1; i++) {
                Point2D.Double p1 = locus.get(i);
                Point2D.Double p2 = locus.get(i + 1);
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
}
