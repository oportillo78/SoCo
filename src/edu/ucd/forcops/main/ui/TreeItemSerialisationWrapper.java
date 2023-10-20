package edu.ucd.forcops.main.ui;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Stack;

import javafx.scene.control.TreeItem;

public class TreeItemSerialisationWrapper<T extends Serializable> implements Serializable {
    private static final long serialVersionUID = 1L;

    private transient TreeItem<T> item;

    public TreeItemSerialisationWrapper(TreeItem<T> item) {
        if (item == null) {
            throw new IllegalArgumentException();
        }
        this.item = item;
    }

    /**
     * Custom way of writing the TreeItem structure
     */
    private void writeObject(ObjectOutputStream out)
             throws IOException {
        Stack<TreeItem<T>> stack = new Stack<>();
        stack.push(item);

        out.defaultWriteObject();
        do {
            TreeItem<T> current = stack.pop();

            int size = current.getChildren().size();
            out.writeInt(size);

            // write all the data that needs to be restored here
            out.writeObject(current.getValue());

            // "schedule" serialisation of children.
            // the first one is inserted last, since the top one from the stack is
            // retrieved first
            for (int i = size - 1; i >= 0; --i) {
                stack.push(current.getChildren().get(i));
            }
        } while (!stack.isEmpty());
    }

    /**
     * happens before readResolve; recreates the TreeItem structure
     */
    private void readObject(ObjectInputStream in)
             throws IOException, ClassNotFoundException {
        class Container {
            int count;
            final TreeItem<T> item;
            Container(ObjectInputStream in) throws ClassNotFoundException, IOException {
                // read the data for a single TreeItem here
                this.count = in.readInt();
                this.item = new TreeItem<>((T) in.readObject());
            }
        }
        in.defaultReadObject();
        Container root = new Container(in);
        this.item = root.item;

        if (root.count > 0) {
            Stack<Container> stack = new Stack<>();
            stack.push(root);
            do {
                Container current = stack.peek();
                --current.count;
                if (current.count <= 0) {
                    // we're done with this item
                    stack.pop();
                }

                Container newContainer = new Container(in);
                current.item.getChildren().add(newContainer.item);
                if (newContainer.count > 0) {
                    //schedule reading children of non-leaf
                    stack.push(newContainer);
                }
            } while(!stack.isEmpty());
        }
    }

    /** 
     * We're not actually interested in this object but the treeitem
     * @return the treeitem
     * @throws ObjectStreamException
     */
    public Object readResolve() throws ObjectStreamException {
        return item;
    }

}