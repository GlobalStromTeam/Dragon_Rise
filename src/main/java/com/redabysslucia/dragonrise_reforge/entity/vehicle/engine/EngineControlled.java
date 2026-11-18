// Adapted from Frontline Combat Pack (Commissar_Mado), commit e4188472.
// Original license: META-INF/licenses/frontline-combat-pack.txt.
package com.redabysslucia.dragonrise_reforge.entity.vehicle.engine;

/** Vehicles carrying the synced engine state used by {@link EngineControl}. */
public interface EngineControlled {

    byte dragonrise$getEngineState();

    void dragonrise$setEngineState(byte state);
}
