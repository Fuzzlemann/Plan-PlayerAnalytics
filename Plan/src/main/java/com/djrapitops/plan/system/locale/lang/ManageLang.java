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
package com.djrapitops.plan.system.locale.lang;

/**
 * {@link Lang} implementation for Manage command related subcommand language.
 *
 * @author Rsl1122
 */
public enum ManageLang implements Lang {

    HOTSWAP_REMINDER("Manage - Remind HotSwap", "§eRemember to swap to the new database (/plan m hotswap ${0}) & reload the plugin."),
    PROGRESS_START("Manage - Start", "> §2Processing data.."),
    PROGRESS_SUCCESS("Manage - Success", "> §aSuccess!"),
    PROGRESS_FAIL("Manage - Fail", "> §cSomething went wrong: ${0}"),

    CONFIRMATION("Manage - Fail, Confirmation", "> §cAdd '-a' argument to confirm execution: ${0}"),
    IMPORTERS("Manage - List Importers", "Importers: "),

    CON_NO_SERVERS("Manage - Fail, No Servers", "§cNo Servers found in the database."),
    CON_EXCEPTION("Manage - Fail, Unexpected Exception", "§eOdd Exception: ${0}"),
    CON_UNAUTHORIZED("Manage - Fail, Unauthorized", "§eFail reason: Unauthorized. Server might be using different database."),
    CON_GENERIC_FAIL("Manage - Fail, Connection Exception", "§eFail reason: "),
    CON_EXTERNAL_URL("Manage - Notify External Url", "§eNon-local address, check that port is open"),
    CON_OLD_VERSION("Manage - Fail, Old version", "§eFail reason: Older Plan version on receiving server"),

    CONFIRM_OVERWRITE("Manage - Confirm Overwrite", "Data in ${0} will be overwritten!"),
    CONFIRM_REMOVAL("Manage - Confirm Removal", "Data in ${0} will be removed!"),

    FAIL_SAME_DB("Manage - Fail Same Database", "> §cCan not operate on to and from the same database!"),
    FAIL_INCORRECT_DB("Manage - Fail Incorrect Database", "> §c'${0}' is not a supported database."),
    FAIL_FILE_NOT_FOUND("Manage - Fail File not found", "> §cNo File found at ${0}"),
    FAIL_IMPORTER_NOT_FOUND("Manage - Fail No Importer", "§eImporter '${0}' doesn't exist"),
    NO_SERVER("Manage - Fail No Server", "No server found with given parameters."),
    UNINSTALLING_SAME_SERVER("Manage - Fail Same server", "Can not mark this server as uninstalled (You are on it)");

    private final String identifier;
    private final String defaultValue;

    ManageLang(String identifier, String defaultValue) {
        this.identifier = identifier;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getDefault() {
        return defaultValue;
    }
}