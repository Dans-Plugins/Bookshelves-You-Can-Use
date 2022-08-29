package dmccoystephenson.bookshelvesyoucanuse.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import dmccoystephenson.bookshelvesyoucanuse.BookshelvesYouCanUse;
import preponderous.ponder.minecraft.bukkit.abs.AbstractPluginCommand;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Daniel McCoy Stephenson
 * @since August 28th, 2022
 */
public class DefaultCommand extends AbstractPluginCommand {
    private final BookshelvesYouCanUse plugin;

    public DefaultCommand(BookshelvesYouCanUse plugin) {
        super(new ArrayList<>(Arrays.asList("default")), new ArrayList<>(Arrays.asList("epp.default")));
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender commandSender) {
        commandSender.sendMessage(ChatColor.AQUA + "ExamplePonderPlugin " + plugin.getVersion());
        commandSender.sendMessage(ChatColor.AQUA + "Developed by: Daniel Stephenson");
        commandSender.sendMessage(ChatColor.AQUA + "Wiki: https://github.com/Preponderous-Software/ExamplePonderPlugin/wiki");
        return true;
    }

    @Override
    public boolean execute(CommandSender commandSender, String[] strings) {
        return execute(commandSender);
    }
}