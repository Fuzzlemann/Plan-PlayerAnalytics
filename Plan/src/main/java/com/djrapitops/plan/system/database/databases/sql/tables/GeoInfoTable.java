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
package com.djrapitops.plan.system.database.databases.sql.tables;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.data.container.GeoInfo;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.processing.ExecStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryAllStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryStatement;
import com.djrapitops.plan.system.database.databases.sql.statements.Column;
import com.djrapitops.plan.system.database.databases.sql.statements.Select;
import com.djrapitops.plan.system.database.databases.sql.statements.Sql;
import com.djrapitops.plan.system.database.databases.sql.statements.TableSqlParser;
import com.djrapitops.plan.utilities.comparators.GeoInfoComparator;
import com.djrapitops.plugin.utilities.Verify;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Table that is in charge of storing common IP and Geolocation data for users.
 * <p>
 * Table Name: plan_ips
 * <p>
 * For contained columns {@see Col}
 *
 * @author Rsl1122
 */
public class GeoInfoTable extends UserIDTable {

    public static final String TABLE_NAME = "plan_ips";

    public GeoInfoTable(SQLDB db) {
        super(TABLE_NAME, db);
        insertStatement = "INSERT INTO " + tableName + " ("
                + Col.USER_ID + ", "
                + Col.IP + ", "
                + Col.IP_HASH + ", "
                + Col.GEOLOCATION + ", "
                + Col.LAST_USED
                + ") VALUES ("
                + usersTable.statementSelectID + ", "
                + "?, ?, ?, ?)";
    }

    private String insertStatement;

    @Override
    public void createTable() throws DBInitException {
        createTable(TableSqlParser.createTable(tableName)
                .column(Col.USER_ID, Sql.INT).notNull()
                .column(Col.IP, Sql.varchar(39)).notNull()
                .column(Col.GEOLOCATION, Sql.varchar(50)).notNull()
                .column(Col.IP_HASH, Sql.varchar(200))
                .column(Col.LAST_USED, Sql.LONG).notNull().defaultValue("0")
                .foreignKey(Col.USER_ID, usersTable.getTableName(), UsersTable.Col.ID)
                .toString()
        );
    }

    public List<GeoInfo> getGeoInfo(UUID uuid) {
        String sql = "SELECT DISTINCT * FROM " + tableName +
                " WHERE " + Col.USER_ID + "=" + usersTable.statementSelectID;

        return query(new QueryStatement<List<GeoInfo>>(sql, 100) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
            }

            @Override
            public List<GeoInfo> processResults(ResultSet set) throws SQLException {
                List<GeoInfo> geoInfo = new ArrayList<>();
                while (set.next()) {
                    String ip = set.getString(Col.IP.get());
                    String geolocation = set.getString(Col.GEOLOCATION.get());
                    String ipHash = set.getString(Col.IP_HASH.get());
                    long lastUsed = set.getLong(Col.LAST_USED.get());
                    geoInfo.add(new GeoInfo(ip, geolocation, lastUsed, ipHash));
                }
                return geoInfo;
            }
        });
    }

    private void updateGeoInfo(UUID uuid, GeoInfo info) {
        String sql = "UPDATE " + tableName + " SET "
                + Col.LAST_USED + "=?" +
                " WHERE " + Col.USER_ID + "=" + usersTable.statementSelectID +
                " AND " + Col.IP_HASH + "=?" +
                " AND " + Col.GEOLOCATION + "=?";

        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setLong(1, info.getDate());
                statement.setString(2, uuid.toString());
                statement.setString(3, info.getIpHash());
                statement.setString(4, info.getGeolocation());
            }
        });
    }

    public void saveGeoInfo(UUID uuid, GeoInfo info) {
        List<GeoInfo> geoInfo = getGeoInfo(uuid);
        if (geoInfo.contains(info)) {
            updateGeoInfo(uuid, info);
        } else {
            insertGeoInfo(uuid, info);
        }
    }

    private void insertGeoInfo(UUID uuid, GeoInfo info) {
        execute(new ExecStatement(insertStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
                statement.setString(2, info.getIp());
                statement.setString(3, info.getIpHash());
                statement.setString(4, info.getGeolocation());
                statement.setLong(5, info.getDate());
            }
        });
    }

    public Optional<String> getGeolocation(String ip) {
        String sql = Select.from(tableName, Col.GEOLOCATION)
                .where(Col.IP + "=?")
                .toString();

        return query(new QueryStatement<Optional<String>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, ip);
            }

            @Override
            public Optional<String> processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return Optional.of(set.getString(Col.GEOLOCATION.get()));
                }
                return Optional.empty();
            }
        });
    }

    public Map<UUID, List<GeoInfo>> getAllGeoInfo() {
        String usersIDColumn = usersTable + "." + UsersTable.Col.ID;
        String usersUUIDColumn = usersTable + "." + UsersTable.Col.UUID + " as uuid";
        String sql = "SELECT " +
                Col.IP + ", " +
                Col.GEOLOCATION + ", " +
                Col.LAST_USED + ", " +
                Col.IP_HASH + ", " +
                usersUUIDColumn +
                " FROM " + tableName +
                " INNER JOIN " + usersTable + " on " + usersIDColumn + "=" + Col.USER_ID;

        return query(new QueryAllStatement<Map<UUID, List<GeoInfo>>>(sql, 50000) {
            @Override
            public Map<UUID, List<GeoInfo>> processResults(ResultSet set) throws SQLException {
                Map<UUID, List<GeoInfo>> geoLocations = new HashMap<>();
                while (set.next()) {
                    UUID uuid = UUID.fromString(set.getString("uuid"));

                    List<GeoInfo> userGeoInfo = geoLocations.getOrDefault(uuid, new ArrayList<>());

                    String ip = set.getString(Col.IP.get());
                    String geolocation = set.getString(Col.GEOLOCATION.get());
                    String ipHash = set.getString(Col.IP_HASH.get());
                    long lastUsed = set.getLong(Col.LAST_USED.get());
                    userGeoInfo.add(new GeoInfo(ip, geolocation, lastUsed, ipHash));

                    geoLocations.put(uuid, userGeoInfo);
                }
                return geoLocations;
            }
        });
    }

    public List<String> getNetworkGeolocations() {
        List<String> geolocations = new ArrayList<>();

        Map<UUID, List<GeoInfo>> geoInfo = getAllGeoInfo();
        for (List<GeoInfo> userGeoInfos : geoInfo.values()) {
            if (userGeoInfos.isEmpty()) {
                continue;
            }
            userGeoInfos.sort(new GeoInfoComparator());
            geolocations.add(userGeoInfos.get(0).getGeolocation());
        }

        return geolocations;
    }

    public void insertAllGeoInfo(Map<UUID, List<GeoInfo>> allIPsAndGeolocations) {
        if (Verify.isEmpty(allIPsAndGeolocations)) {
            return;
        }

        executeBatch(new ExecStatement(insertStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                // Every User
                for (UUID uuid : allIPsAndGeolocations.keySet()) {
                    // Every GeoInfo
                    for (GeoInfo info : allIPsAndGeolocations.get(uuid)) {
                        String ip = info.getIp();
                        String ipHash = info.getIpHash();
                        String geoLocation = info.getGeolocation();
                        long lastUsed = info.getDate();

                        statement.setString(1, uuid.toString());
                        statement.setString(2, ip);
                        statement.setString(3, ipHash);
                        statement.setString(4, geoLocation);
                        statement.setLong(5, lastUsed);

                        statement.addBatch();
                    }
                }
            }
        });
    }

    public enum Col implements Column {
        USER_ID(UserIDTable.Col.USER_ID.get()),
        IP("ip"),
        IP_HASH("ip_hash"),
        GEOLOCATION("geolocation"),
        LAST_USED("last_used");

        private final String column;

        Col(String column) {
            this.column = column;
        }

        @Override
        public String get() {
            return toString();
        }

        @Override
        public String toString() {
            return column;
        }
    }
}
