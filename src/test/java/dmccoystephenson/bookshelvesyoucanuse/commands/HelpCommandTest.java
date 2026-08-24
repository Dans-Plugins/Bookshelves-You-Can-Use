package dmccoystephenson.bookshelvesyoucanuse.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Daniel McCoy Stephenson
 * @since August 28th, 2022
 */
class HelpCommandTest {

    @Test
    void registersHelpBycuPermission() {
        HelpCommand helpCommand = new HelpCommand();

        assertTrue(helpCommand.getNames().contains("help"));
        assertTrue(helpCommand.getPermissions().contains("bycu.help"));
    }

    /**
     * The help output is the in-game equivalent of the General Commands section of COMMANDS.md,
     * so every command documented there is expected, in the order that document lists them.
     */
    @Test
    void executeSendsEveryDocumentedCommand() {
        HelpCommand helpCommand = new HelpCommand();
        CommandSender commandSender = mock(CommandSender.class);

        boolean result = helpCommand.execute(commandSender);

        assertTrue(result);
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(commandSender, times(2)).sendMessage(messageCaptor.capture());
        List<String> messages = messageCaptor.getAllValues();
        assertEquals(ChatColor.AQUA + "/bycu", messages.get(0));
        assertEquals(ChatColor.AQUA + "/bycu help", messages.get(1));
    }
}
