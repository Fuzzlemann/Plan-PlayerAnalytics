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
package com.djrapitops.plan.system.database.databases.sql.tables.move;

import com.djrapitops.plan.data.container.UserInfo;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.tables.ServerTable;
import com.djrapitops.plan.system.database.databases.sql.tables.Table;
import com.djrapitops.plan.system.database.databases.sql.tables.UsersTable;
import com.djrapitops.plan.system.info.server.Server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A Fake table used to store a lot of big table operations.
 * <p>
 * To use this table create a new BatchOperationTable with both SQLDB objects.
 * {@code SQLDB from; SQLDB to;}
 * {@code fromT = new BatchOperationTable(from);}
 * {@code toT = new BatchOperationTable(to);}
 * {@code fromT.copy(toT);}
 * <p>
 * The copy methods assume that the table has been cleared, or that no duplicate data will be entered for a user.
 * <p>
 * clearTable methods can be used to clear the table beforehand.
 * <p>
 * Server and User tables should be copied first.
 *
 * @author Rsl1122
 * @since 4.0.0
 */
public class BatchOperationTable extends Table {

    /**
     * Constructor.
     * <p>
     * Call to access copy functionality.
     *
     * @param database Database to copy things from
     * @throws IllegalStateException if database.init has not been called.
     * @throws ClassCastException    if database is not SQLDB.
     */
    public BatchOperationTable(SQLDB database) {
        super("", database);
        if (!db.isOpen()) {
            throw new IllegalStateException("Given Database had not been initialized.");
        }
    }

    @Override
    public void createTable() {
        throw new IllegalStateException("Method not supposed to be used on this table.");
    }

    public void clearTable(Table table) {
        table.removeAllData();
    }

    @Override
    public void removeAllData() {
        db.remove().everything();
    }

    public void copyEverything(BatchOperationTable toDB) {
        if (toDB.equals(this)) {
            return;
        }
        toDB.removeAllData();

        copyServers(toDB);
        copyUsers(toDB);
        copyWorlds(toDB);
        copyTPS(toDB);
        copyWebUsers(toDB);
        copyCommandUse(toDB);
        copyIPsAndGeolocs(toDB);
        copyNicknames(toDB);
        copySessions(toDB);
        copyUserInfo(toDB);
        copyPings(toDB);
    }

    public void copyPings(BatchOperationTable toDB) {
        if (toDB.equals(this)) {
            return;
        }
        toDB.db.getPingTable().insertAllPings(db.getPingTable().getAllPings());
    }

    public void copyCommandUse(BatchOperationTable toDB) {
        if (toDB.equals(this)) {
            return;
        }
        toDB.db.getCommandUseTable().insertCommandUsage(db.getCommandUseTable().getAllCommandUsages());
    }

    public void copyIPsAndGeolocs(BatchOperationTable toDB) {
        if (toDB.equals(this)) {
            return;
        }
        toDB.db.getGeoInfoTable().insertAllGeoInfo(db.getGeoInfoTable().getAllGeoInfo());
    }

    public void copyNicknames(BatchOperationTable toDB) {
        if (toDB.equals(this)) {
            return;
        }
        toDB.db.getNicknamesTable().insertNicknames(db.getNicknamesTable().getAllNicknames());
    }

    public void copyWebUsers(BatchOperationTable toDB) {
        if (toDB.equals(this)) {
            return;
        }
        toDB.db.getSecurityTable().addUsers(db.getSecurityTable().getUsers());
    }

    public void copyServers(BatchOperationTable toDB) {
        if (toDB.equals(this)) {
            return;
        }
        ServerTable serverTable = db.getServerTable();
        List<Server> servers = new ArrayList<>(serverTable.getBukkitServers().values());
        serverTable.getBungeeInfo().ifPresent(servers::add);
        toDB.db.getServerTable().insertAllServers(servers);
    }

    public void copyTPS(BatchOperationTable toDB) {
        if (toDB.equals(this)) {
            return;
        }
        toDB.db.getTpsTable().insertAllTPS(db.getTpsTable().getAllTPS());
    }

    public void copyUserInfo(BatchOperationTable toDB) {
        if (toDB.equals(this)) {
            return;
        }
        toDB.db.getUserInfoTable().insertUserInfo(db.getUserInfoTable().getAllUserInfo());
    }

    public void copyWorlds(BatchOperationTable toDB) {
        if (toDB.equals(this)) {
            return;
        }
        toDB.db.getWorldTable().saveWorlds(db.getWorldTable().getAllWorlds());
    }

    public void copyUsers(BatchOperationTable toDB) {
        if (toDB.equals(this)) {
            return;
        }
        UsersTable fromTable = db.getUsersTable();
        UsersTable toTable = toDB.db.getUsersTable();
        Map<UUID, UserInfo> users = fromTable.getUsers();
        toTable.insertUsers(users);
        toTable.updateKicked(fromTable.getAllTimesKicked());
    }

    public void copySessions(BatchOperationTable toDB) {
        if (toDB.equals(this)) {
            return;
        }
        toDB.db.getSessionsTable().insertSessions(db.getSessionsTable().getAllSessions(true), true);
    }
}
