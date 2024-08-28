package io.xylex.starterkit;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

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
            starterKit.add(itemString.contains("[") ? createCustomItem(itemString) : createSimpleItem(itemString));
        }

        for (ItemStack item : starterKit) {
            player.getInventory().addItem(item);
        }
    }

    private ItemStack createSimpleItem(String itemString) {
        String[] parts = itemString.split(":");
        Material material = Material.valueOf(parts[0].toUpperCase());
        int amount = Integer.parseInt(parts[1]);
        return new ItemStack(material, amount);
    }

    private ItemStack createCustomItem(String itemString) {
        String[] mainParts = itemString.split("\\[|\\]");
        String itemType = mainParts[0];
        String[] amountPart = mainParts[mainParts.length - 1].split(":");
        int amount = Integer.parseInt(amountPart[1]);

        Material material = Material.valueOf(itemType.toUpperCase());
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();

        if (mainParts.length > 1) {
            String nbtData = mainParts[1];
            for (String nbtPart : nbtData.split(",")) {
                String[] keyValue = nbtPart.split(":");
                setDynamicAttribute(meta, keyValue[0].trim(), keyValue[1].trim().replace("\"", ""));
            }
        }

        item.setItemMeta(meta);
        return item;
    }

    private void setDynamicAttribute(ItemMeta meta, String key, String value) {
        try {
            String methodName = "set" + key.substring(0, 1).toUpperCase() + key.substring(1);
            Method method = meta.getClass().getMethod(methodName, String.class);
            method.invoke(meta, value);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            try {
                String methodName = "set" + key.substring(0, 1).toUpperCase() + key.substring(1);
                Method method = meta.getClass().getMethod(methodName,
                        Boolean.parseBoolean(value) ? boolean.class : int.class);
                method.invoke(meta, Boolean.parseBoolean(value) ? Boolean.parseBoolean(value) : Integer.parseInt(value));
            } catch (Exception ex) {
                getLogger().warning("Failed to set attribute: " + key + " with value: " + value);
            }
        }
    }
}
