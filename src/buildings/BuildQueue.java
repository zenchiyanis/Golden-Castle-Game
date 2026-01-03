package buildings;

import java.util.ArrayDeque;
import java.util.Deque;

public class BuildQueue {
    public static class Item {
        public final Building building;
        public int remaining;

        public Item(Building building) {
            this.building = building;
            this.remaining = building.getBuildTime();
        }
    }

    private final Deque<Item> queue = new ArrayDeque<>();

    public void enqueue(Building b) {
        queue.addLast(new Item(b));
    }

    public Item peek() { return queue.peekFirst(); }

    public Building tick() {
        Item it = queue.peekFirst();
        if (it == null) return null;
        if (it.remaining > 0) it.remaining--;
        if (it.remaining <= 0) {
            queue.removeFirst();
            return it.building;
        }
        return null;
    }

    public boolean isEmpty() { return queue.isEmpty(); }
}
