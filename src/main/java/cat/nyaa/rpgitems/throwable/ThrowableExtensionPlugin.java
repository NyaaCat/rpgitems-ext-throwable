package cat.nyaa.rpgitems.throwable;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import think.rpgitems.event.RPGItemsReloadEvent;
import think.rpgitems.power.PowerManager;

public class ThrowableExtensionPlugin extends JavaPlugin implements Listener {
    public static ThrowableExtensionPlugin plugin;
    ExtEvents infExtEvent;
    Hijack hijack;

    @Override
    public void onEnable() {
        super.onEnable();
        plugin = this;
        hijack = new Hijack();
        infExtEvent = new ExtEvents();

        getServer().getPluginManager().registerEvents(infExtEvent, this);
        getServer().getPluginManager().registerEvents(this, this);
        registerPowers();
    }

    @EventHandler
    public void onRPGItemsReload(RPGItemsReloadEvent event) {
        registerPowers();
    }

    private void registerPowers() {
        PowerManager.registerPowers(this, "cat.nyaa.rpgitems.throwable.impl");
//        PowerManager.registerConditions(this, "cat.nyaa.rpgitems.throwable.impl");
//        PowerManager.registerMarkers(this, "cat.nyaa.rpgitems.throwable.impl");
    }
}
