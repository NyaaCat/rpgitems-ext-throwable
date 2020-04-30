package cat.nyaa.rpgitems.throwable;

import cat.nyaa.rpgitems.throwable.utils.ProjectileRemoveTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

import static think.rpgitems.RPGItems.plugin;

public class ExtEvents implements Listener {
    @EventHandler
    public void onThrown(ProjectileHitEvent e){
        Projectile entity = e.getEntity();
        if (ProjectileRemoveTask.isFakeArrow(entity.getEntityId())) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (e.getHitEntity() != null && e.getEntity() instanceof AbstractArrow && ((AbstractArrow) e.getEntity()).getPierceLevel() > 0 ) {
                    return;
                }
                ProjectileRemoveTask.submitRemoval(entity.getEntityId());
                entity.remove();
            });
        }
    }
}
