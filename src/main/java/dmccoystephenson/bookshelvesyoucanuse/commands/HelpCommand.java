package dmccoystephenson.bookshelvesyoucanuse.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import preponderous.ponder.minecraft.bukkit.abs.AbstractPluginCommand;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel McCoy Stephenson
 * @since August 28th, 2022
 */
public class HelpCommand extends AbstractPluginCommand {

    public HelpCommand() {
        super(new ArrayList<>(List.of("help")), new ArrayList<>(List.of("bycu.help")));
    }

    @Override
    public boolean execute(CommandSender commandSender) {
        // one line per command documented in COMMANDS.md, in the order it lists them
        commandSender.sendMessage(ChatColor.AQUA + "/bycu");
        commandSender.sendMessage(ChatColor.AQUA + "/bycu help");
        return true;
    }

    @Override
    public boolean execute(CommandSender commandSender, String[] strings) {
        return execute(commandSender);
    }
}
