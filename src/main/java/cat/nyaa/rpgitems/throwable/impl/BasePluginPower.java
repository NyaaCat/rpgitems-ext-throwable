package cat.nyaa.rpgitems.throwable.impl;

import cat.nyaa.rpgitems.throwable.ThrowableExtensionPlugin;
import org.bukkit.NamespacedKey;
import think.rpgitems.power.BasePower;

public abstract class BasePluginPower extends BasePower {
    @Override
    public NamespacedKey getNamespacedKey() {
        return new NamespacedKey(ThrowableExtensionPlugin.plugin, getName());
    }
}
