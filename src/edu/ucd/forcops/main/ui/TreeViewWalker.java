package edu.ucd.forcops.main.ui;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

/**
 * Tree table item walker.
 *
 * @author bvissy
 *
 * @param <T>
 *            The type of the tree items.
 */
public class TreeViewWalker<T> {

    /**
     * Utility class to hold a tuple
     */
    public class Tuple<E, F> {
        E first;
        F second;

        public Tuple(E first, F second) {
            this.first = first;
            this.second = second;
        }

        public E getFirst() {
            return first;
        }

        public Tuple<E, F> setFirst(E first) {
            return new Tuple<>(first, second);
        }

        public F getSecond() {
            return second;
        }

        public Tuple<E, F> setSecond(F second) {
            return new Tuple<>(first, second);
        }

        @Override
        public String toString() {
            return "Tuple [first=" + first + ", second=" + second + "]";
        }
    }

    // The walk state stack
    private Deque<Tuple<TreeItem<T>, Integer>> stack = new ArrayDeque<>();

    /**
     * Initialize the walker.
     *
     * @param tree
     *            The tree to walk
     */
    public TreeViewWalker(TreeView<T> tree) {
        super();
        if (tree.getRoot() != null) {
            stack.push(new Tuple<>(tree.getRoot(), -1));
        }
    }

    /**
     * @return True if has unserved items.
     */
    public boolean hasNext() {
        return !stack.isEmpty();
    }

    /**
     * @return The next tree item in depth walk order. The parent is returned
     *         before any of its children.
     */
    public TreeItem<T> next() {
        if (!hasNext()) {
            throw new IllegalStateException("");
        }
        TreeItem<T> nxt = stack.peek().getFirst();
        move();
        return nxt;
    }

    private void move() {
        Tuple<TreeItem<T>, Integer> n = stack.pop();
        ObservableList<TreeItem<T>> ch = n.getFirst().getChildren();
        int idx = n.getSecond() + 1;
        if (ch.size() <= idx) {
            if (stack.isEmpty()) {
                return;
            } else {
                move();
            }
        } else {
            stack.push(n.setSecond(idx));
            stack.push(new Tuple<>(ch.get(idx), -1));
        }
    }

    /**
     * @return A stream of all (remaining) items. Note, that the walker could
     *         traverse only once over items.
     */
    public Stream<TreeItem<T>> stream() {
        return StreamSupport.stream(new Spliterator<TreeItem<T>>() {

            @Override
            public int characteristics() {
                return 0;
            }

            @Override
            public long estimateSize() {
                return Long.MAX_VALUE;
            }

            @Override
            public boolean tryAdvance(Consumer<? super TreeItem<T>> action) {
                if (hasNext()) {
                    action.accept(next());
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public Spliterator<TreeItem<T>> trySplit() {
                return null;
            }
        }, false);
    }

    /**
     * Walks over the tree and calls the consumer for each tree item.
     *
     * @param tree
     *            The tree to visit.
     * @param visitor
     *            The visitor.
     */
    public static <T> void visit(TreeView<T> tree, Consumer<TreeItem<T>> visitor) {
        TreeViewWalker<T> tw = new TreeViewWalker<>(tree);
        while (tw.hasNext()) {
            visitor.accept(tw.next());
        }
    }

    /**
     * Walks over the tree and calls the consumer for each item value.
     *
     * @param tree
     *            The tree to visit.
     * @param visitor
     *            The visitor.
     */
    public static <T> void visitItems(TreeView<T> tree, Consumer<T> visitor) {
        TreeViewWalker<T> tw = new TreeViewWalker<>(tree);
        while (tw.hasNext()) {
            visitor.accept(tw.next().getValue());
        }
    }

}
