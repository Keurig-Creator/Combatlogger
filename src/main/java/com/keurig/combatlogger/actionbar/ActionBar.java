package com.keurig.combatlogger.actionbar;

import com.keurig.combatlogger.CombatLogger;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;

public class ActionBar {

    private static final String nmsVersion = CombatLogger.getInstance().getNsmVersion();

    public static void sendActionBar(final Player player, final String message) {

        if (!player.isOnline()) {
            return;
        }

        if (nmsVersion.equals("v1_8_R3")) {
            try {
                final Class<?> iChatBaseComponent = Class.forName("net.minecraft.server." + nmsVersion + ".IChatBaseComponent");
                final Constructor<?> constructor = Class.forName("net.minecraft.server." + nmsVersion + ".PacketPlayOutChat").getConstructor(iChatBaseComponent, Byte.TYPE);
                final Object chatComponent = iChatBaseComponent.getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + message + "\"}");
                final Object packet = constructor.newInstance(chatComponent, (byte) 2);
                final Object entityPlayer = player.getClass().getMethod("getHandle", new Class[0]).invoke(player);
                final Object playerConnection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);
                playerConnection.getClass().getMethod("sendPacket", Class.forName("net.minecraft.server." + nmsVersion + ".Packet")).invoke(playerConnection, packet);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent.fromLegacyText(message));
        }
    }
}
