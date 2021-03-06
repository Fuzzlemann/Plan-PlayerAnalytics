/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the LGNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  LGNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.system.importing.importers;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.system.cache.GeolocationCache;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.importing.data.ServerImportData;
import com.djrapitops.plan.system.importing.data.UserImportData;
import com.djrapitops.plan.system.info.server.ServerInfo;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @author Fuzzlemann
 * @since 4.0.0
 */
@Singleton
public class OfflinePlayerImporter extends Importer {

    @Inject
    public OfflinePlayerImporter(
            Plan plugin,
            GeolocationCache geolocationCache,
            DBSystem dbSystem,
            ServerInfo serverInfo
    ) {
        super(plugin, geolocationCache, dbSystem, serverInfo, "offline");
    }

    @Override
    public ServerImportData getServerImportData() {
        return null;
    }

    @Override
    public List<UserImportData> getUserImportData() {
        List<UserImportData> dataList = new ArrayList<>();

        Set<OfflinePlayer> operators = Bukkit.getOperators();
        Set<OfflinePlayer> banned = Bukkit.getBannedPlayers();

        Arrays.stream(Bukkit.getOfflinePlayers()).parallel().forEach(player -> {
            UserImportData.UserImportDataBuilder builder = UserImportData.builder(serverUUID.get());
            builder.name(player.getName())
                    .uuid(player.getUniqueId())
                    .registered(player.getFirstPlayed());

            if (operators.contains(player)) {
                builder.op();
            }

            if (banned.contains(player)) {
                builder.banned();
            }

            dataList.add(builder.build());
        });

        return dataList;
    }
}
