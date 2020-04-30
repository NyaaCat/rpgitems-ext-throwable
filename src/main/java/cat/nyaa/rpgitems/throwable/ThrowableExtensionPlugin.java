package cat.nyaa.rpgitems.throwable;

import org.bukkit.plugin.java.JavaPlugin;
import think.rpgitems.power.PowerManager;

public class ThrowableExtensionPlugin extends JavaPlugin {
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
        PowerManager.registerPowers(this, "cat.nyaa.rpgitems.throwable.impl");
//        PowerManager.registerConditions(this, "cat.nyaa.rpgitems.throwable.impl");
//        PowerManager.registerMarkers(this, "cat.nyaa.rpgitems.throwable.impl");
    }
}
