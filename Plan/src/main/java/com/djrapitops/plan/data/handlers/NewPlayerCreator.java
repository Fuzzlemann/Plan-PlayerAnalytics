package com.djrapitops.plan.data.handlers;

import com.djrapitops.plan.data.cache.DataCacheHandler;
import com.djrapitops.plan.Plan;
import com.djrapitops.plan.database.Database;
import com.djrapitops.plan.data.DemographicsData;
import com.djrapitops.plan.data.UserData;
import java.util.Date;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 *
 * @author Rsl1122
 */
public class NewPlayerCreator {

    private Plan plugin;
    private Database db;
    private DataCacheHandler handler;

    /**
     * Class Constructor.
     *
     * Gets the Database from the plugin.
     *
     * @param plugin Current instance of Plan
     * @param h Current instance of DataCacheHandler
     */
    public NewPlayerCreator(Plan plugin, DataCacheHandler h) {
        this.plugin = plugin;
        db = plugin.getDB();
        handler = h;
    }

    /**
     * Creates a new instance of UserData with default values and saves it to
     * DB.
     *
     * @param player Player the UserData is created for.
     */
    public void createNewPlayer(Player player) {
        createNewPlayer((OfflinePlayer) player, player.getGameMode());
    }

    public void createNewPlayer(OfflinePlayer player) {
        createNewPlayer(player, GameMode.SURVIVAL);
    }

    public void createNewPlayer(OfflinePlayer player, GameMode gm) {
        UserData data = new UserData(player, new DemographicsData(), db);
        data.setLastGamemode(gm);
        data.setLastPlayed(new Date().getTime());
        long zero = Long.parseLong("0");
        data.setPlayTime(zero);
        data.setTimesKicked(0);
        data.setLoginTimes(0);
        data.setLastGmSwapTime(zero);
        db.saveUserData(player.getUniqueId(), data);
    }

}