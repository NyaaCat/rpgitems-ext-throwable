package cat.nyaa.rpgitems.throwable.utils;

import cat.nyaa.rpgitems.throwable.ThrowableExtensionPlugin;
import cat.nyaa.rpgitems.throwable.lib.wrapper.WrapperPlayServerEntityDestroy;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

import static cat.nyaa.rpgitems.throwable.Hijack.protocolManager;

public class ProjectileRemoveTask extends BukkitRunnable {
    static boolean enabled = false;
    static ProjectileRemoveTask task = null;

    static final Set<Integer> entityToRemove = new HashSet<>();
    static final Set<Integer> entityFired = new HashSet<>();

    public static void start(int interval){
        stop();
        enabled = true;
        task = new ProjectileRemoveTask();
        task.runTaskTimer(ThrowableExtensionPlugin.plugin, 0, interval);
    }

    private static void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        enabled = false;
    }

    public static void submitRemoval(int entityId) {
        entityToRemove.add(entityId);
    }

    public static boolean isFakeArrow(int entityId) {
        return entityFired.contains(entityId);
    }

    public static void registerFiredArrow(int entityId) {
        entityFired.add(entityId);
    }


    @Override
    public void run() {
        if (entityToRemove.isEmpty()){
            return;
        }
        int[] ints = entityToRemove.stream().mapToInt(Integer::intValue).toArray();
        WrapperPlayServerEntityDestroy destroyPacket = new WrapperPlayServerEntityDestroy();
        destroyPacket.setEntityIds(ints);
        protocolManager.broadcastServerPacket(destroyPacket.getHandle());
        entityFired.removeAll(entityToRemove);
        entityToRemove.clear();
    }
}
