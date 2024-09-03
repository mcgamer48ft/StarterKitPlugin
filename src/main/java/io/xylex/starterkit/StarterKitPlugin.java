package io.xylex.starterkit;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class StarterKitPlugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        loadConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("Starter Kit Plugin enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Starter Kit Plugin disabled!");
    }

    private void loadConfig() {
        saveDefaultConfig();
        reloadConfig();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!getConfig().getBoolean("first-time-only") || !event.getPlayer().hasPlayedBefore()) {
            giveStarterKit(event.getPlayer());
            event.getPlayer().sendMessage(getConfig().getString("welcome-message", "Welcome to the server! Here is your starter kit."));
        }
    }

    private void giveStarterKit(org.bukkit.entity.Player player) {
        List<ItemStack> starterKit = new ArrayList<>();
        for (String itemString : getConfig().getStringList("starter-kit-items")) {
            starterKit.add(createItem(itemString));
        }

        for (ItemStack item : starterKit) {
            player.getInventory().addItem(item);
        }
    }

    private ItemStack createItem(String itemString) {
        String[] parts = itemString.split("\\[");
        String[] itemParts = parts[0].split(":");
        Material material = Material.valueOf(itemParts[0].toUpperCase());
        int amount = itemParts.length > 1 ? Integer.parseInt(itemParts[1]) : 1;
        ItemStack item = new ItemStack(material, amount);

        if (parts.length > 1 && parts[1].contains("]")) {
            String[] enchantments = parts[1].split("\\]")[0].split(",");
            for (String enchantment : enchantments) {
                String[] enchantParts = enchantment.split(":");
                if (enchantParts.length == 2) {
                    @SuppressWarnings("deprecation") Enchantment ench = Enchantment.getByKey(NamespacedKey.minecraft(enchantParts[0].toLowerCase()));
                    if (ench != null) {
                        item.addUnsafeEnchantment(ench, Integer.parseInt(enchantParts[1]));
                    }
                }
            }
        }

        return item;
    }
}
