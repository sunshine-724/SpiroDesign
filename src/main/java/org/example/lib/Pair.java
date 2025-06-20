package org.example.lib;

/**
 * Pairクラスは、SpiroGearとPenの2つのオブジェクトをペアとして保持するための汎用クラスです。
 * 
 * @param <A> 1つ目の要素の型(SpiroGearの型)
 * @param <B> 2つ目の要素の型(Penの型)
 * 
 */

public class Pair<A, B> {
    /**
     * 1つ目の要素（SpiroGear）
     */
    public final A first;
    /**
     * 2つ目の要素（Pen）
     */
    public final B second;

    public Pair(A first, B second) {
        this.first = first; // SpiroGear
        this.second = second; // Pen
    }
}