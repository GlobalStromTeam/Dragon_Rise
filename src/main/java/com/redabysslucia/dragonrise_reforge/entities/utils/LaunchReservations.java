package com.redabysslucia.dragonrise_reforge.entities.utils;

import java.util.Map;
import java.util.WeakHashMap;

/** One outstanding launch per platform. Tokens keep an old timeout from releasing a newer launch. */
public final class LaunchReservations<K> {
    private static final class Reservation {
        final long token;
        boolean effectsClaimed;
        Reservation(long token) { this.token = token; }
    }

    private final Map<K, Reservation> pending = new WeakHashMap<>();
    private long generation;

    public long reserve(K key) {
        if (pending.containsKey(key)) return 0;
        long token = ++generation;
        pending.put(key, new Reservation(token));
        return token;
    }

    public void complete(K key) { pending.remove(key); }

    public long currentToken(K key) {
        Reservation current = pending.get(key);
        return current == null ? 0 : current.token;
    }

    public void expire(K key, long token) {
        Reservation current = pending.get(key);
        if (current != null && current.token == token) pending.remove(key);
    }

    public boolean claimEffects(K key) {
        Reservation current = pending.get(key);
        if (current == null) return true;
        if (current.effectsClaimed) return false;
        current.effectsClaimed = true;
        return true;
    }
}
