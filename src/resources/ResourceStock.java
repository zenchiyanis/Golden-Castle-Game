package resources;

import java.util.EnumMap;

public class ResourceStock {
    private final EnumMap<ResourceType, Integer> values;

    public ResourceStock(EnumMap<ResourceType, Integer> initial) {
        this.values = new EnumMap<>(ResourceType.class);
        for (ResourceType t : ResourceType.values()) {
            this.values.put(t, initial.getOrDefault(t, 0));
        }
    }

    public int get(ResourceType t) { return values.getOrDefault(t, 0); }

    public void add(ResourceType t, int amount) {
        values.put(t, Math.max(0, get(t) + amount));
    }

    public boolean canAfford(EnumMap<ResourceType, Integer> cost) {
        for (var e : cost.entrySet()) {
            if (get(e.getKey()) < e.getValue()) return false;
        }
        return true;
    }

    public void spend(EnumMap<ResourceType, Integer> cost) {
        for (var e : cost.entrySet()) {
            add(e.getKey(), -e.getValue());
        }
    }

    public EnumMap<ResourceType, Integer> snapshot() {
        return new EnumMap<>(values);
    }
}
