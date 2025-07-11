package org.example.model;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.example.Main; // Mainクラスをインポート

public class MainTest {

    /**
     * Mainクラスのインスタンスが正常に作成できることを確認する。
     * これは、デフォルトコンストラクタが正しく機能し、
     * 初期化時に予期せぬ例外が発生しないことを保証する。
     */
    @Test
    @DisplayName("Mainクラスのインスタンス化テスト")
    void testMainInstantiation() {
        // Mainクラスのインスタンスを作成
        // この操作が例外を投げずに完了することを確認する
        assertDoesNotThrow(() -> {
            new Main();
        }, "Mainクラスのインスタンス化は例外を投げるべきではない。");

        // インスタンスがnullでないことを確認 (assertDoesNotThrowが成功すれば暗黙的に確認されるが、明示的に)
        Main mainInstance = new Main();
        assertNotNull(mainInstance, "Mainクラスのインスタンスはnullであるべきではない。");
    }

    // mainメソッドやcreateAndShowGUIメソッドの直接的な単体テストは、
    // SwingのEDT依存性やGUIコンポーネントの操作を伴うため、
    // 単体テストの範囲を超え、統合テストやE2Eテストでカバーすべきである。
    // そのため、ここではそれらのメソッドのテストは行わない。
}
