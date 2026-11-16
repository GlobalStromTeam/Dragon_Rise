package com.redabysslucia.dragonrise_reforge.entities.utils;

import java.util.HashMap;
import java.util.Map;

/** Prediction may time out; an acknowledged launch stays locked until server completion. */
public final class ClientLaunchReservations<K> {
    private record Pending(boolean confirmed, long deadline) {}
    private final Map<K, Pending> pending = new HashMap<>();
    private final Map<K, Long> tokens = new HashMap<>();

    public boolean predict(K key, long now, long graceTicks) {
        Pending current = pending.get(key);
        if (current != null && (current.confirmed || current.deadline > now)) return false;
        pending.put(key, new Pending(false, now + graceTicks));
        return true;
    }

    public void synchronize(K key, long token, boolean active) {
        if (token < tokens.getOrDefault(key, 0L)) return;
        tokens.put(key, token);
        if (active) pending.put(key, new Pending(true, Long.MAX_VALUE));
        else pending.remove(key);
    }

    public void clear() {
        pending.clear();
        tokens.clear();
    }
}
