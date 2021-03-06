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
package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.locale.lang.CommandLang;
import com.djrapitops.plan.system.locale.lang.DeepHelpLang;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.Sender;

import javax.inject.Inject;

public class DisableCommand extends CommandNode {

    private final Locale locale;
    private final PlanPlugin plugin;

    @Inject
    public DisableCommand(PlanPlugin plugin, Locale locale) {
        super("disable", "plan.reload", CommandType.ALL);

        this.plugin = plugin;
        this.locale = locale;

        setShortHelp(locale.getString(CmdHelpLang.DISABLE));
        setInDepthHelp(locale.getArray(DeepHelpLang.DISABLE));
    }

    @Override
    public void onCommand(Sender sender, String commandLabel, String[] args) {
        plugin.onDisable();
        sender.sendMessage(locale.getString(CommandLang.DISABLE_DISABLED));
    }
}
