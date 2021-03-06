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
package com.djrapitops.plan.modules.server.bukkit;

import com.djrapitops.plan.system.database.BukkitDBSystem;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.importing.BukkitImportSystem;
import com.djrapitops.plan.system.importing.ImportSystem;
import com.djrapitops.plan.system.info.server.BukkitServerInfo;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.listeners.BukkitListenerSystem;
import com.djrapitops.plan.system.listeners.ListenerSystem;
import com.djrapitops.plan.system.settings.config.BukkitConfigSystem;
import com.djrapitops.plan.system.settings.config.ConfigSystem;
import com.djrapitops.plan.system.tasks.BukkitTaskSystem;
import com.djrapitops.plan.system.tasks.TaskSystem;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * Module for binding Bukkit specific classes to the interface implementations.
 *
 * @author Rsl1122
 */
@Module
public class BukkitSuperClassBindingModule {

    @Provides
    @Singleton
    ServerInfo provideBukkitServerInfo(BukkitServerInfo bukkitServerInfo) {
        return bukkitServerInfo;
    }

    @Provides
    @Singleton
    DBSystem provideBukkitDatabaseSystem(BukkitDBSystem dbSystem) {
        return dbSystem;
    }

    @Provides
    @Singleton
    ConfigSystem provideBukkitConfigSystem(BukkitConfigSystem bukkitConfigSystem) {
        return bukkitConfigSystem;
    }

    @Provides
    @Singleton
    TaskSystem provideBukkitTaskSystem(BukkitTaskSystem bukkitTaskSystem) {
        return bukkitTaskSystem;
    }

    @Provides
    @Singleton
    ListenerSystem provideBukkitListenerSystem(BukkitListenerSystem bukkitListenerSystem) {
        return bukkitListenerSystem;
    }

    @Provides
    @Singleton
    ImportSystem provideImportSsytem(BukkitImportSystem bukkitImportSystem) {
        return bukkitImportSystem;
    }

}