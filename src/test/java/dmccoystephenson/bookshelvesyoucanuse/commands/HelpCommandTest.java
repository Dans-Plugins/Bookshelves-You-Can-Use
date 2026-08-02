package dmccoystephenson.bookshelvesyoucanuse.commands;

import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
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

    @Test
    void executeSendsBycuHelpUsage() {
        HelpCommand helpCommand = new HelpCommand();
        CommandSender commandSender = mock(CommandSender.class);

        boolean result = helpCommand.execute(commandSender);

        assertTrue(result);
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(commandSender).sendMessage(messageCaptor.capture());
        assertTrue(messageCaptor.getValue().contains("/bycu help"));
    }
}
