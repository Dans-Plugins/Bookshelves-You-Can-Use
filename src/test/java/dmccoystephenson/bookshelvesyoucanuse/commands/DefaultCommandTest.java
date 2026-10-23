package dmccoystephenson.bookshelvesyoucanuse.commands;

import dmccoystephenson.bookshelvesyoucanuse.BookshelvesYouCanUse;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Daniel McCoy Stephenson
 * @since August 28th, 2022
 */
class DefaultCommandTest {

    @Test
    void executeSendsPluginNameAndOwnWikiLink() {
        BookshelvesYouCanUse plugin = mock(BookshelvesYouCanUse.class);
        when(plugin.getVersion()).thenReturn("v0.0.1");
        DefaultCommand defaultCommand = new DefaultCommand(plugin);
        CommandSender commandSender = mock(CommandSender.class);

        boolean result = defaultCommand.execute(commandSender);

        assertTrue(result);
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(commandSender, times(3)).sendMessage(messageCaptor.capture());
        List<String> messages = messageCaptor.getAllValues();
        assertTrue(messages.get(0).contains("BookshelvesYouCanUse v0.0.1"));
        assertTrue(messages.get(2).contains("https://github.com/Dans-Plugins/Bookshelves-You-Can-Use/wiki"));
    }

    /**
     * The overload that takes arguments ignores them and shows the same plugin information.
     */
    @Test
    void executeWithArgumentsIgnoresThemAndSendsTheSameInformation() {
        BookshelvesYouCanUse plugin = mock(BookshelvesYouCanUse.class);
        when(plugin.getVersion()).thenReturn("v0.0.1");
        DefaultCommand defaultCommand = new DefaultCommand(plugin);
        CommandSender withoutArguments = mock(CommandSender.class);
        CommandSender withArguments = mock(CommandSender.class);

        defaultCommand.execute(withoutArguments);
        boolean result = defaultCommand.execute(withArguments, new String[]{"unexpected", "words"});

        assertTrue(result);
        ArgumentCaptor<String> expected = ArgumentCaptor.forClass(String.class);
        verify(withoutArguments, times(3)).sendMessage(expected.capture());
        ArgumentCaptor<String> actual = ArgumentCaptor.forClass(String.class);
        verify(withArguments, times(3)).sendMessage(actual.capture());
        assertEquals(expected.getAllValues(), actual.getAllValues());
    }
}
