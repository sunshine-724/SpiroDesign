package org.example.model;

import org.example.spiroIO.SpiroIO;
import org.example.lib.Pair;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SpiroIOクラスの単体テストを行うクラスである。
 */
public class SpiroIOTest {

    private File testFile;
    private SpiroIO spiroIO;

    @BeforeEach
    public void setUp() {
        spiroIO = new SpiroIO();
        testFile = new File("test_spiro_data.dat");
    }

    @AfterEach
    public void tearDown() {
        if (testFile.exists()) {
            testFile.delete(); // テスト用に生成したファイルを削除する
        }
    }

    @Test
    public void testSaveAndLoadSpiro() {
        // テスト用のModelおよびPenオブジェクトを生成する
        Model originalModel = new Model();
        Pen originalPen = new Pen();

        // モデルとペンの状態をファイルに保存する
        spiroIO.saveSpiro(testFile, originalModel, originalPen);
        assertTrue(testFile.exists(), "ファイルが作成されているべきである");

        // ファイルからモデルとペンを読み込む
        Pair<Model, Pen> loadedPair = spiroIO.loadSpiro(testFile);
        assertNotNull(loadedPair, "読み込み結果がnullであってはならない");

        // クラスの型が一致していることを確認する（内容比較はequals依存）
        assertEquals(originalModel.getClass(), loadedPair.first.getClass());
        assertEquals(originalPen.getClass(), loadedPair.second.getClass());
    }

    @Test
    public void testLoadSpiroFromNonexistentFile() {
        // 存在しないファイルを指定して読み込みを試みる
        File nonexistent = new File("nonexistent_file.dat");
        Pair<Model, Pen> result = spiroIO.loadSpiro(nonexistent);
        assertNull(result, "存在しないファイルの読み込みはnullを返すべきである");
    }
}
