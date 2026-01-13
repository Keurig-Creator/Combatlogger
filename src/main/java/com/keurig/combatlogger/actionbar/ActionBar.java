package com.keurig.combatlogger.actionbar;

import com.keurig.combatlogger.CombatLogger;
import com.keurig.combatlogger.utils.Chat;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;

public class ActionBar {

    private static final String nmsVersion = CombatLogger.getInstance().getNsmVersion();

    public static void sendActionBar(final Player player, final String message) {
        if (player == null || !player.isOnline()) return;

        boolean hex = Chat.supportsHex(); // true on 1.16+, false on 1.8
        String colored = Chat.colorize(message, hex);

        if (nmsVersion.equals("v1_8_R3")) {
            try {
                final Class<?> iChatBaseComponent = Class.forName("net.minecraft.server." + nmsVersion + ".IChatBaseComponent");
                final Constructor<?> constructor = Class.forName("net.minecraft.server." + nmsVersion + ".PacketPlayOutChat")
                        .getConstructor(iChatBaseComponent, Byte.TYPE);

                // 1.8 actionbar packet uses plain JSON text, colors must be § codes already
                final Object chatComponent = iChatBaseComponent.getDeclaredClasses()[0]
                        .getMethod("a", String.class)
                        .invoke(null, "{\"text\":\"" + escapeJson(colored) + "\"}");

                final Object packet = constructor.newInstance(chatComponent, (byte) 2);
                final Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
                final Object playerConnection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);

                playerConnection.getClass()
                        .getMethod("sendPacket", Class.forName("net.minecraft.server." + nmsVersion + ".Packet"))
                        .invoke(playerConnection, packet);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            player.spigot().sendMessage(
                    ChatMessageType.ACTION_BAR,
                    net.md_5.bungee.api.chat.TextComponent.fromLegacyText(colored)
            );
        }
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
