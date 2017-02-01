package com.djrapitops.plan.command.commands;

//import com.djrapitops.plan.Phrase;
import com.djrapitops.plan.Phrase;
import com.djrapitops.plan.Plan;
import com.djrapitops.plan.command.CommandType;
import com.djrapitops.plan.command.PlanCommand;
import com.djrapitops.plan.command.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Rsl1122
 */
public class HelpCommand extends SubCommand {

    private final Plan plugin;
    private final PlanCommand command;

    /**
     * Subcommand Constructor.
     *
     * @param plugin Current instance of Plan
     * @param command Current instance of PlanCommand
     */
    public HelpCommand(Plan plugin, PlanCommand command) {
        super("help,?", "plan.?", "Show command list.", CommandType.CONSOLE, "");

        this.plugin = plugin;
        this.command = command;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

        ChatColor oColor = Phrase.COLOR_MAIN.color();
        ChatColor tColor = Phrase.COLOR_SEC.color();
        ChatColor hColor = Phrase.COLOR_TER.color();

        // Header
        sender.sendMessage(hColor + Phrase.ARROWS_RIGHT.toString() + oColor + " Player Analytics - Help");
        // Help results
        for (SubCommand command : this.command.getCommands()) {
            if (command.getName().equalsIgnoreCase(getName())) {
                continue;
            }

            if (!sender.hasPermission(command.getPermission())) {
                continue;
            }

            if (!(sender instanceof Player) && command.getCommandType() == CommandType.PLAYER) {
                continue;
            }

            sender.sendMessage(tColor + " " + Phrase.BALL.toString() + oColor
                    + " /plan " + command.getFirstName() + " " + command.getArguments() + tColor + " - " + command.getUsage());
        }
        // Footer
        sender.sendMessage(hColor + Phrase.ARROWS_RIGHT.toString());
        return true;
    }

}