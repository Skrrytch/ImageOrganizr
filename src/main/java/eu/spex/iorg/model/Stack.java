package eu.spex.iorg.model;

import java.util.ArrayList;
import java.util.List;

public class Stack<T> {

    private final List<T> list;

    public Stack() {
        list = new ArrayList<>();
    }

    public void push(T element) {
        list.add(0, element);
    }

    public T pull() {
        if (list.isEmpty()) {
            return null;
        }
        T element = list.get(0);
        list.remove(0);
        return element;
    }

    public T peek() {
        return list.size() > 0 ? list.get(0) : null;
    }

    public void clear() {
        list.clear();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public int size() {
        return list.size();
    }
}
