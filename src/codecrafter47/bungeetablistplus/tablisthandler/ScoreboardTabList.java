/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package codecrafter47.bungeetablistplus.tablisthandler;

import codecrafter47.bungeetablistplus.tablist.Slot;
import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.config.TabListProvider;
import codecrafter47.bungeetablistplus.managers.ConfigManager;
import codecrafter47.bungeetablistplus.tablist.TabList;
import codecrafter47.bungeetablistplus.util.ColorParser;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.tab.TabListAdapter;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.Team;

/**
 *
 * @author florian
 */
public class ScoreboardTabList extends CustomTabListHandler implements
        IMyTabListHandler {

    private static String getSlotID(int n) {
        String hex = Integer.toHexString(n + 1);
        char[] alloc = new char[hex.length() * 2];
        for (int i = 0; i < alloc.length; i++) {
            if (i % 2 == 0) {
                alloc[i] = ChatColor.COLOR_CHAR;
            } else {
                alloc[i] = hex.charAt(i / 2);
            }
        }
        return new StringBuilder().append(ChatColor.MAGIC).append(alloc).append(
                ChatColor.RESET).toString();
    }

    private final int[] slots_ping = new int[ConfigManager.getTabSize()];

    private int sendSlots = 0;

    private final String send[] = new String[ConfigManager.getTabSize()];

    public ScoreboardTabList(ProxiedPlayer player) {
        this.init(player);
    }

    @Override
    public void recreate() {
        if (getPlayer().getServer() != null) {
            if (BungeeTabListPlus.getInstance().getConfigManager().
                    getMainConfig().excludeServers.contains(getPlayer().
                            getServer().getInfo().getName()) || isExcluded) {
                unload();
                return;
            }
        }

        TabListProvider tlp = BungeeTabListPlus.getInstance().
                getTabListManager().getTabListForPlayer(super.getPlayer());
        if (tlp == null) {
            exclude();
            unload();
            return;
        }
        TabList tabList = tlp.getTabList(super.getPlayer());

        resize(tabList.getUsedSlots());

        int charLimit = BungeeTabListPlus.getInstance().getConfigManager().
                getMainConfig().charLimit;

        for (int i = 0; i < tabList.getUsedSlots(); i++) {
            Slot line = tabList.getSlot(i);
            if (line == null) {
                line = new Slot("");
            }
            String text = BungeeTabListPlus.getInstance().getVariablesManager().
                    replacePlayerVariables(line.text, super.getPlayer());
            text = BungeeTabListPlus.getInstance().getVariablesManager().
                    replaceVariables(text);
            text = ChatColor.translateAlternateColorCodes('&', text);
            if (charLimit > 0) {
                text = ColorParser.substringIgnoreColors(text, charLimit);
            }

            String old = send[i];
            if (old == null || !old.equals(text) || line.ping != slots_ping[i]) {
                updateSlot(i, text, line.ping);
            }
        }
    }

    private void resize(int size) {
        if (size == sendSlots) {
            return;
        }
        if (size > sendSlots) {
            for (int i = sendSlots; i < size; i++) {
                sendSlot(i);
                createSlot(i);
            }
            sendSlots = size;
        } else if (size < sendSlots) {
            for (int i = size; i < sendSlots; i++) {
                removeSlot(i);
            }
        }
        sendSlots = size;
    }

    private void sendSlot(int i) {

        if (i >= ConfigManager.getTabSize()) {
            return;
        }
        BungeeTabListPlus.getInstance().getPacketManager().createOrUpdatePlayer(
                getPlayer().unsafe(), getSlotID(i), slots_ping[i]);
        send[i] = "";
    }

    private void removeSlot(int i) {
        BungeeTabListPlus.getInstance().getPacketManager().removePlayer(
                getPlayer().unsafe(), getSlotID(i));
        BungeeTabListPlus.getInstance().getPacketManager().removeTeam(
                getPlayer().unsafe(), getSlotID(i));
    }

    private void updateSlot(int row, String text, int ping) {
        if (ping != slots_ping[row]) {
            BungeeTabListPlus.getInstance().getPacketManager().
                    createOrUpdatePlayer(getPlayer().unsafe(), getSlotID(row),
                            ping);
        }
        send[row] = text;
        slots_ping[row] = ping;
        String split[] = splitText(text);
        BungeeTabListPlus.getInstance().getPacketManager().updateTeam(
                getPlayer().unsafe(), getSlotID(row), split[0], /*split[1]*/ "", split[1]);
    }

    private void createSlot(int row) {
        BungeeTabListPlus.getInstance().getPacketManager().createTeam(
                getPlayer().unsafe(), getSlotID(row));
    }

    private String[] splitText(String s) {
        String ret[] = new String[3];
        int left = s.length();
        if (left <= 16) {
            ret[0] = s;
            ret[1] = "";
            ret[2] = "";
        } else {
            int end = s.charAt(15) == ChatColor.COLOR_CHAR ? 15 : 16;
            ret[0] = s.substring(0, end);
            int start = ColorParser.endofColor(s, end);
            String colors = ColorParser.extractColorCodes(s.substring(0, start));
            end = start + 16 - colors.length();
            if (end >= s.length()) {
                end = s.length();
            }
            ret[1] = colors + s.substring(start, end);
            start = end;
            end += 16;
            if (end >= s.length()) {
                end = s.length();
            }
            ret[2] = s.substring(start, end);
        }
        return ret;
    }

    public void unload() {
        resize(0);
    }
}