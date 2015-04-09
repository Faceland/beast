package info.faceland.beast;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public final class BeastRunnable extends BukkitRunnable {

    private final BeastPlugin plugin;

    public BeastRunnable(BeastPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (final World w : Bukkit.getWorlds()) {
            if (plugin.getSettings().getInt("config.enabled-worlds." + w.getName() + ".starting-level", -1) < 0) {
                continue;
            }
            w.setAmbientSpawnLimit(plugin.getSettings().getInt("config.enabled-worlds." + w.getName() + ".ambient-spawn-limit"));
            w.setMonsterSpawnLimit(plugin.getSettings().getInt("config.enabled-worlds." + w.getName() + ".monster-spawn-limit"));
            w.setAnimalSpawnLimit(plugin.getSettings().getInt("config.enabled-worlds." + w.getName() + ".animal-spawn-limit"));
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    w.setAmbientSpawnLimit(0);
                    w.setMonsterSpawnLimit(0);
                    w.setAnimalSpawnLimit(0);
                }
            }, plugin.getSettings().getInt("config.enabled-worlds." + w.getName() + ".wave-length"));
        }
    }

}
