package dmccoystephenson.bookshelvesyoucanuse;

import dmccoystephenson.bookshelvesyoucanuse.commands.HelpCommand;
import dmccoystephenson.bookshelvesyoucanuse.eventhandlers.InteractHandler;
import dmccoystephenson.bookshelvesyoucanuse.exceptions.BookshelfInventoryNotFoundException;
import dmccoystephenson.bookshelvesyoucanuse.objects.BookshelfInventory;
import dmccoystephenson.bookshelvesyoucanuse.services.ConfigService;
import dmccoystephenson.bookshelvesyoucanuse.trace.TraceClient;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.MockedStatic;
import preponderous.ponder.minecraft.bukkit.PonderMC;
import preponderous.ponder.minecraft.bukkit.abs.AbstractPluginCommand;
import preponderous.ponder.minecraft.bukkit.services.CommandService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Daniel McCoy Stephenson
 * @since August 28th, 2022
 */
class BookshelvesYouCanUseTest {
    private static final String ENDPOINT = "https://trace.example.test";
    private static final String KEY = "the-configured-key";
    private static final String PLUGIN_VERSION = "0.3.3-SNAPSHOT";

    private MockedStatic<Bukkit> bukkit;
    private BookshelvesYouCanUse plugin;

    // The collaborators of onEnable and onDisable, set up by givenAPluginReadyToEnable.
    private ConfigService configService;
    private CommandService commandService;
    private PluginManager pluginManager;
    private Logger logger;
    private TraceClient trace;
    private TraceClient.Builder traceBuilder;

    @BeforeEach
    void setUp() throws BookshelfInventoryNotFoundException {
        bukkit = mockStatic(Bukkit.class);
        // BookshelfInventory's constructor creates its inventory through Bukkit, so a distinct
        // inventory per call keeps the bookshelf a lookup returns identifiable.
        bukkit.when(() -> Bukkit.createInventory(any(), anyInt(), anyString()))
                .thenAnswer(invocation -> mock(Inventory.class));

        plugin = mock(BookshelvesYouCanUse.class);
        doCallRealMethod().when(plugin).configFileExists(any());
        doCallRealMethod().when(plugin).getBookshelfInventory(any());
        doCallRealMethod().when(plugin).isVersionMismatched();
        doCallRealMethod().when(plugin).isDebugEnabled();
        doCallRealMethod().when(plugin).onCommand(any(), any(), any(), any());
        doCallRealMethod().when(plugin).onEnable();
        doCallRealMethod().when(plugin).onDisable();
    }

    @AfterEach
    void tearDown() {
        bukkit.close();
    }

    @Test
    void theStartupNoticeSaysWhatIsSentAndWhereToTurnItOff() {
        String on = BookshelvesYouCanUse.usageReportingNotice("BookshelvesYouCanUse", null);

        assertTrue(on.startsWith("Usage reporting is on: BookshelvesYouCanUse sends its name, version and command names to https://trace.danielstephenson.dev"), on);
        assertTrue(on.contains(", plus a random server ID (server-id in plugins/trace/config.yml) - nothing about players."), on);
        assertTrue(on.contains("usage-reporting.enabled: false"), on);
        assertTrue(on.contains("plugins/trace/config.yml"), on);
        assertTrue(on.endsWith("Details: https://github.com/Stephenson-Software/trace#usage-reporting"), on);
    }

    @Test
    void theStartupNoticeSaysWhyReportingIsOff() {
        assertEquals("Usage reporting is off (config.yml).",
                BookshelvesYouCanUse.usageReportingNotice("BookshelvesYouCanUse", TraceClient.REASON_CONFIG));
        assertEquals("Usage reporting is off (server-wide config: plugins/trace/config.yml).",
                BookshelvesYouCanUse.usageReportingNotice("BookshelvesYouCanUse", TraceClient.REASON_SERVER_WIDE));
    }

    @Test
    void configFileExistsIsFalseForADataFolderWithoutAConfigFile(@TempDir Path dataFolder) {
        assertFalse(plugin.configFileExists(dataFolder.toFile()));
    }

    @Test
    void configFileExistsIsTrueOnceTheConfigFileIsPresentInTheDataFolder(@TempDir Path dataFolder) throws IOException {
        assertTrue(new File(dataFolder.toFile(), "config.yml").createNewFile());

        assertTrue(plugin.configFileExists(dataFolder.toFile()));
    }

    /**
     * The config file is looked for inside the data folder Bukkit provides, rather than at a
     * working-directory-relative path, so two different data folders give two different answers.
     */
    @Test
    void configFileExistsIsResolvedRelativeToTheGivenDataFolder(@TempDir Path populatedFolder, @TempDir Path emptyFolder) throws IOException {
        assertTrue(new File(populatedFolder.toFile(), "config.yml").createNewFile());

        assertTrue(plugin.configFileExists(populatedFolder.toFile()));
        assertFalse(plugin.configFileExists(emptyFolder.toFile()));
    }

    @Test
    void getBookshelfInventoryReturnsTheBookshelfTrackedAtThatLocation() throws BookshelfInventoryNotFoundException {
        BookshelfInventory bookshelf = new BookshelfInventory(location(10, 64, -3, "world"));
        trackBookshelves(bookshelf);

        assertSame(bookshelf, plugin.getBookshelfInventory(location(10, 64, -3, "world")));
    }

    @Test
    void getBookshelfInventoryPicksTheMatchingBookshelfOutOfSeveralTrackedOnes() throws BookshelfInventoryNotFoundException {
        BookshelfInventory wanted = new BookshelfInventory(location(10, 64, -3, "world"));
        trackBookshelves(
                new BookshelfInventory(location(0, 0, 0, "world")),
                wanted,
                new BookshelfInventory(location(10, 64, -4, "world"))
        );

        assertSame(wanted, plugin.getBookshelfInventory(location(10, 64, -3, "world")));
    }

    /**
     * The world name is part of the match, which is what keeps two bookshelves standing at the same
     * coordinates in different worlds from being handed each other's contents.
     */
    @Test
    void getBookshelfInventoryDoesNotMatchTheSameCoordinatesInAnotherWorld() {
        trackBookshelves(new BookshelfInventory(location(10, 64, -3, "world")));

        assertThrows(BookshelfInventoryNotFoundException.class,
                () -> plugin.getBookshelfInventory(location(10, 64, -3, "world_nether")));
    }

    @Test
    void getBookshelfInventoryDoesNotMatchWhenOnlySomeCoordinatesAgree() {
        trackBookshelves(new BookshelfInventory(location(10, 64, -3, "world")));

        assertThrows(BookshelfInventoryNotFoundException.class,
                () -> plugin.getBookshelfInventory(location(11, 64, -3, "world")));
        assertThrows(BookshelfInventoryNotFoundException.class,
                () -> plugin.getBookshelfInventory(location(10, 65, -3, "world")));
        assertThrows(BookshelfInventoryNotFoundException.class,
                () -> plugin.getBookshelfInventory(location(10, 64, -2, "world")));
    }

    @Test
    void getBookshelfInventoryThrowsWhenNoBookshelvesAreTracked() {
        trackBookshelves();

        assertThrows(BookshelfInventoryNotFoundException.class,
                () -> plugin.getBookshelfInventory(location(10, 64, -3, "world")));
    }

    @Test
    void isVersionMismatchedIsFalseWhenTheConfigCarriesNoVersion() {
        givenConfigVersion(null);
        when(plugin.getVersion()).thenReturn("v0.1.0");

        assertFalse(plugin.isVersionMismatched());
    }

    @Test
    void isVersionMismatchedIsFalseWhenThePluginHasNoVersion() {
        givenConfigVersion("v0.1.0");
        when(plugin.getVersion()).thenReturn(null);

        assertFalse(plugin.isVersionMismatched());
    }

    @Test
    void isVersionMismatchedIsFalseWhenTheVersionsAgree() {
        givenConfigVersion("v0.1.0");
        when(plugin.getVersion()).thenReturn("v0.1.0");

        assertFalse(plugin.isVersionMismatched());
    }

    @Test
    void isVersionMismatchedIgnoresCaseWhenComparingTheVersions() {
        givenConfigVersion("V0.1.0-SNAPSHOT");
        when(plugin.getVersion()).thenReturn("v0.1.0-snapshot");

        assertFalse(plugin.isVersionMismatched());
    }

    @Test
    void isVersionMismatchedIsTrueWhenTheVersionsDiffer() {
        givenConfigVersion("v0.0.1");
        when(plugin.getVersion()).thenReturn("v0.1.0");

        assertTrue(plugin.isVersionMismatched());
    }

    /**
     * The option name is asserted here rather than matched loosely: debug output is gated on
     * debugMode specifically, and a lookup of some other key would read as disabled on every server.
     */
    @Test
    void isDebugEnabledReadsTheDebugModeOptionThroughTheConfigService() {
        ConfigService configService = mock(ConfigService.class);
        when(configService.getBoolean("debugMode")).thenReturn(true);
        plantOnPluginMock("configService", configService);

        assertTrue(plugin.isDebugEnabled());
    }

    @Test
    void isDebugEnabledIsFalseWhenTheDebugModeOptionIsUnset() {
        ConfigService configService = mock(ConfigService.class);
        when(configService.getBoolean("debugMode")).thenReturn(false);
        plantOnPluginMock("configService", configService);

        assertFalse(plugin.isDebugEnabled());
    }

    @Test
    void aCommandWithNoArgumentsShowsThePluginInformationWithoutConsultingTheCommandService() {
        CommandService commandService = mock(CommandService.class);
        plantOnPluginMock("commandService", commandService);
        plantOnPluginMock("trace", mock(TraceClient.class));
        when(plugin.getVersion()).thenReturn("v0.2.1-SNAPSHOT");
        CommandSender sender = mock(CommandSender.class);

        assertTrue(plugin.onCommand(sender, command("bycu"), "bycu", new String[0]));

        verify(sender).sendMessage(ChatColor.AQUA + "BookshelvesYouCanUse v0.2.1-SNAPSHOT");
        verifyNoInteractions(commandService);
    }

    @Test
    void aCommandWithArgumentsIsHandedToTheCommandServiceAndReturnsItsResult() {
        CommandService commandService = mock(CommandService.class);
        plantOnPluginMock("commandService", commandService);
        plantOnPluginMock("trace", mock(TraceClient.class));
        CommandSender sender = mock(CommandSender.class);
        String[] args = {"help"};
        when(commandService.interpretAndExecuteCommand(sender, "bycu", args)).thenReturn(true, false);

        assertTrue(plugin.onCommand(sender, command("bycu"), "bycu", args));
        assertFalse(plugin.onCommand(sender, command("bycu"), "bycu", args));
    }

    /**
     * The report carries the command's name and nothing else the sender typed, which is what the
     * README promises ("nothing typed after a command"), and it is sent for the no-argument command too.
     */
    @Test
    void everyCommandReportsOnlyTheCommandNameNotTheLabelOrArguments() {
        plantOnPluginMock("commandService", mock(CommandService.class));
        TraceClient trace = mock(TraceClient.class);
        plantOnPluginMock("trace", trace);
        Player sender = mock(Player.class);

        plugin.onCommand(sender, command("bookshelvesyoucanuse"), "bycu", new String[]{"help", "secret"});
        plugin.onCommand(sender, command("bookshelvesyoucanuse"), "bycu", new String[0]);

        verify(trace, times(2)).report("command", null, Collections.singletonMap("name", "bookshelvesyoucanuse"));
        verifyNoMoreInteractions(trace);
    }

    /**
     * Only commands a player runs are reported: console, command-block and plugin-dispatched commands are not.
     */
    @Test
    void aCommandNotRunByAPlayerIsNotReported() {
        plantOnPluginMock("commandService", mock(CommandService.class));
        TraceClient trace = mock(TraceClient.class);
        plantOnPluginMock("trace", trace);
        CommandSender console = mock(CommandSender.class);

        plugin.onCommand(console, command("bookshelvesyoucanuse"), "bycu", new String[]{"help"});

        verifyNoInteractions(trace);
    }

    /**
     * The jar's config.yml is copied first because it carries the comments: the programmatic options
     * are then added to that file rather than the file being written without them.
     */
    @Test
    void aFirstStartWritesTheBundledConfigAndThenAddsTheProgrammaticOptions(@TempDir Path pluginsFolder) {
        givenAPluginReadyToEnable(pluginsFolder);

        enablePlugin();

        InOrder order = inOrder(plugin, configService);
        order.verify(plugin).saveDefaultConfig();
        order.verify(configService).saveMissingConfigDefaultsIfNotPresent();
        order.verify(configService).copyBundledUsageReportingBlockIfAbsent();
        verify(plugin, never()).reloadConfig();
    }

    /**
     * The save has to come first: reloadConfig discards what is in memory, so the other order
     * would lose the updated version and the added options.
     */
    @Test
    void anExistingConfigFromAnotherVersionIsUpdatedAndThenReloaded(@TempDir Path pluginsFolder) throws IOException {
        givenAPluginReadyToEnable(pluginsFolder);
        givenAnExistingConfigFile();
        givenConfigVersion("v0.0.1");

        enablePlugin();

        InOrder order = inOrder(plugin, configService);
        order.verify(configService).saveMissingConfigDefaultsIfNotPresent();
        order.verify(plugin).reloadConfig();
        order.verify(configService).copyBundledUsageReportingBlockIfAbsent();
        verify(plugin, never()).saveDefaultConfig();
    }

    @Test
    void anExistingConfigFromThisVersionIsOnlyReloaded(@TempDir Path pluginsFolder) throws IOException {
        givenAPluginReadyToEnable(pluginsFolder);
        givenAnExistingConfigFile();
        givenConfigVersion("v" + PLUGIN_VERSION);

        enablePlugin();

        InOrder order = inOrder(plugin, configService);
        order.verify(plugin).reloadConfig();
        order.verify(configService).copyBundledUsageReportingBlockIfAbsent();
        verify(plugin, never()).saveDefaultConfig();
        verify(configService, never()).saveMissingConfigDefaultsIfNotPresent();
    }

    /**
     * This is the path an operator's opt-out takes: a false here that did not reach the client would
     * leave reporting on for a server that had turned it off.
     */
    @Test
    void theUsageReportingOptionsReachTheClient(@TempDir Path pluginsFolder) {
        givenAPluginReadyToEnable(pluginsFolder);
        when(configService.getUsageReportingKey()).thenReturn(KEY);
        when(configService.isUsageReportingEnabled()).thenReturn(false);

        enablePlugin();

        verify(traceBuilder).key(KEY);
        verify(traceBuilder).enabled(false);
        verify(traceBuilder).serverWideConfig(pluginsFolder.toFile());
        verify(traceBuilder).logger(logger);
    }

    /**
     * The block has to be on disk before anything is sent, so that the switch can already be found
     * in config.yml by the time the first report goes out.
     */
    @Test
    void theUsageReportingBlockIsCopiedBeforeTheClientIsBuilt(@TempDir Path pluginsFolder) {
        givenAPluginReadyToEnable(pluginsFolder);

        enablePlugin();

        InOrder order = inOrder(configService, traceBuilder, trace);
        order.verify(configService).copyBundledUsageReportingBlockIfAbsent();
        order.verify(traceBuilder).build();
        order.verify(trace).report("startup");
    }

    @Test
    void theStartupNoticeIsLoggedBeforeTheStartupEventIsSent(@TempDir Path pluginsFolder) {
        givenAPluginReadyToEnable(pluginsFolder);
        when(trace.disabledReason()).thenReturn(TraceClient.REASON_CONFIG);

        enablePlugin();

        InOrder order = inOrder(logger, trace);
        order.verify(logger).info("Usage reporting is off (config.yml).");
        order.verify(trace).report("startup");
    }

    /**
     * Nothing else makes a bookshelf open: without this registration the plugin loads, reports no
     * error, and bookshelves stay ordinary blocks.
     */
    @Test
    void enablingRegistersTheInteractHandler(@TempDir Path pluginsFolder) {
        givenAPluginReadyToEnable(pluginsFolder);

        enablePlugin();

        verify(pluginManager).registerEvents(isA(InteractHandler.class), eq(plugin));
    }

    @Test
    @SuppressWarnings("unchecked")
    void enablingHandsTheHelpCommandToTheCommandService(@TempDir Path pluginsFolder) {
        givenAPluginReadyToEnable(pluginsFolder);
        ArgumentCaptor<ArrayList<AbstractPluginCommand>> commands = ArgumentCaptor.forClass(ArrayList.class);

        enablePlugin();

        verify(commandService).initialize(commands.capture(), eq("That command wasn't found."));
        assertTrue(commands.getValue().stream().anyMatch(HelpCommand.class::isInstance));
    }

    /**
     * The tests that follow run the real onEnable, onCommand and Ponder CommandService against the
     * built plugin.yml: Ponder only answers to the labels declared there, so the mocked command service
     * in the tests above could not notice a label or the help command's wiring going missing.
     */
    @ParameterizedTest
    @ValueSource(strings = {"bycu", "bookshelvesyoucanuse"})
    void theHelpCommandAnswersOnBothDocumentedLabels(String label, @TempDir Path pluginsFolder) {
        givenAnEnabledPluginWithTheRealCommandService(pluginsFolder);
        CommandSender sender = senderWithHelpPermission(true);

        assertTrue(plugin.onCommand(sender, command(label), label, new String[]{"help"}));

        verify(sender).sendMessage(ChatColor.AQUA + "/bycu");
        verify(sender).sendMessage(ChatColor.AQUA + "/bycu help");
    }

    @Test
    void theHelpCommandIgnoresAnythingTypedAfterIt(@TempDir Path pluginsFolder) {
        givenAnEnabledPluginWithTheRealCommandService(pluginsFolder);
        CommandSender sender = senderWithHelpPermission(true);

        assertTrue(plugin.onCommand(sender, command("bycu"), "bycu", new String[]{"help", "extra"}));

        verify(sender).sendMessage(ChatColor.AQUA + "/bycu help");
    }

    /**
     * This is the check USER_GUIDE.md's permissions table describes. bycu.help defaults to true, so
     * it only ever refuses a player whose permissions have been set to deny it.
     */
    @Test
    void theHelpCommandIsRefusedWithoutItsPermission(@TempDir Path pluginsFolder) {
        givenAnEnabledPluginWithTheRealCommandService(pluginsFolder);
        CommandSender sender = senderWithHelpPermission(false);
        ArgumentCaptor<String> refusal = ArgumentCaptor.forClass(String.class);

        assertFalse(plugin.onCommand(sender, command("bycu"), "bycu", new String[]{"help"}));

        verify(sender).hasPermission("bycu.help");
        verify(sender).sendMessage(refusal.capture());
        assertTrue(refusal.getValue().contains("bycu.help"), refusal.getValue());
    }

    /**
     * Sub-command names are matched exactly, so a capitalized one is as unknown as a misspelled one.
     * Neither reaches the permission check.
     */
    @ParameterizedTest
    @ValueSource(strings = {"nonsense", "HELP"})
    void anUnknownSubCommandIsAnsweredWithTheNotFoundMessage(String subCommand, @TempDir Path pluginsFolder) {
        givenAnEnabledPluginWithTheRealCommandService(pluginsFolder);
        CommandSender sender = senderWithHelpPermission(true);

        assertFalse(plugin.onCommand(sender, command("bycu"), "bycu", new String[]{subCommand}));

        verify(sender).sendMessage(ChatColor.RED + "That command wasn't found.");
        verify(sender, never()).hasPermission(anyString());
    }

    @Test
    void aLabelPluginYmlDoesNotDeclareIsLeftUnanswered(@TempDir Path pluginsFolder) {
        givenAnEnabledPluginWithTheRealCommandService(pluginsFolder);
        CommandSender sender = senderWithHelpPermission(true);

        assertFalse(plugin.onCommand(sender, command("bycu"), "somethingelse", new String[]{"help"}));

        verifyNoInteractions(sender);
    }

    /**
     * Without this the permission would still be checked, but nothing would grant it by default,
     * and the help command would refuse every player who is not an operator.
     */
    @Test
    void pluginYmlGrantsTheHelpPermissionToEveryoneByDefault() {
        // Bukkit's Permission constructor reaches the plugin manager through the server.
        bukkit.when(Bukkit::getServer).thenReturn(mock(Server.class, RETURNS_DEEP_STUBS));

        Permission permission = bundledDescription().getPermissions().stream()
                .filter(declared -> declared.getName().equals("bycu.help"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("plugin.yml does not declare bycu.help."));

        assertEquals(PermissionDefault.TRUE, permission.getDefault());
    }

    /**
     * Commands and shutdown use the client onEnable built, not the no-op one the plugin starts with:
     * close is what sends any repeated command uses still being counted.
     */
    @Test
    void theClientBuiltOnEnableReportsCommandsAndIsClosedOnDisable(@TempDir Path pluginsFolder) {
        givenAPluginReadyToEnable(pluginsFolder);
        enablePlugin();

        plugin.onCommand(mock(Player.class), command("bycu"), "bycu", new String[0]);
        plugin.onDisable();

        InOrder order = inOrder(trace);
        order.verify(trace).report("command", null, Collections.singletonMap("name", "bycu"));
        order.verify(trace).close();
    }

    /**
     * The plugin mock runs the real onEnable, with a temporary folder standing in for the server's
     * plugins folder. The folder holds no config.yml, so by default this is a first start.
     */
    private void givenAPluginReadyToEnable(Path pluginsFolder) {
        File dataFolder = pluginsFolder.resolve("BookshelvesYouCanUse").toFile();
        assertTrue(dataFolder.mkdir());
        when(plugin.getDataFolder()).thenReturn(dataFolder);
        when(plugin.getName()).thenReturn("BookshelvesYouCanUse");
        when(plugin.getDescription()).thenReturn(new PluginDescriptionFile(
                "BookshelvesYouCanUse", PLUGIN_VERSION, BookshelvesYouCanUse.class.getName()));
        when(plugin.getVersion()).thenReturn("v" + PLUGIN_VERSION);

        logger = mock(Logger.class);
        when(plugin.getLogger()).thenReturn(logger);

        // Ponder's EventHandlerRegistry reaches the plugin manager through Bukkit, not the plugin.
        pluginManager = mock(PluginManager.class);
        bukkit.when(Bukkit::getPluginManager).thenReturn(pluginManager);

        configService = mock(ConfigService.class);
        when(configService.getUsageReportingEndpoint()).thenReturn(ENDPOINT);
        plantOnPluginMock("configService", configService);

        commandService = mock(CommandService.class);
        plantOnPluginMock("commandService", commandService);

        trace = mock(TraceClient.class);
        traceBuilder = mock(TraceClient.Builder.class, RETURNS_SELF);
        when(traceBuilder.build()).thenReturn(trace);
    }

    private void givenAnExistingConfigFile() throws IOException {
        assertTrue(new File(plugin.getDataFolder(), "config.yml").createNewFile());
    }

    /** Runs the real onEnable with the client it builds replaced, so nothing is ever sent. */
    private void enablePlugin() {
        try (MockedStatic<TraceClient> client = mockStatic(TraceClient.class)) {
            client.when(() -> TraceClient.builder(ENDPOINT, "BookshelvesYouCanUse", PLUGIN_VERSION))
                    .thenReturn(traceBuilder);

            plugin.onEnable();
        }
    }

    /**
     * Replaces the mocked command service before enabling, so onEnable initializes a real one.
     * Ponder reads the labels it answers to from the description, hence the plugin.yml from the build.
     */
    private void givenAnEnabledPluginWithTheRealCommandService(Path pluginsFolder) {
        givenAPluginReadyToEnable(pluginsFolder);
        PonderMC ponder = mock(PonderMC.class, RETURNS_DEEP_STUBS);
        when(ponder.getPlugin().getDescription()).thenReturn(bundledDescription());
        plantOnPluginMock("commandService", new CommandService(ponder));
        enablePlugin();
    }

    private PluginDescriptionFile bundledDescription() {
        try (InputStream pluginYml = BookshelvesYouCanUse.class.getResourceAsStream("/plugin.yml")) {
            return new PluginDescriptionFile(pluginYml);
        } catch (IOException | InvalidDescriptionException exception) {
            throw new IllegalStateException("Could not read the built plugin.yml.", exception);
        }
    }

    private CommandSender senderWithHelpPermission(boolean granted) {
        CommandSender sender = mock(CommandSender.class);
        when(sender.hasPermission("bycu.help")).thenReturn(granted);
        return sender;
    }

    private Command command(String name) {
        Command command = mock(Command.class);
        when(command.getName()).thenReturn(name);
        return command;
    }

    private void givenConfigVersion(String version) {
        FileConfiguration config = mock(FileConfiguration.class);
        when(config.getString("version")).thenReturn(version);
        when(plugin.getConfig()).thenReturn(config);
    }

    private void trackBookshelves(BookshelfInventory... bookshelves) {
        plantOnPluginMock("bookshelfInventories", new ArrayList<>(Arrays.asList(bookshelves)));
    }

    /**
     * getBookshelfInventory, isDebugEnabled, onCommand, onEnable and onDisable read their collaborators from fields
     * directly rather than through a getter, and Mockito builds the plugin mock without running field initializers,
     * so those fields have to be planted on the mock for the real methods to run against them.
     */
    private void plantOnPluginMock(String fieldName, Object value) {
        try {
            Field field = BookshelvesYouCanUse.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(plugin, value);
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            throw new IllegalStateException("Could not plant " + fieldName + " on the plugin mock.", exception);
        }
    }

    private Location location(int x, int y, int z, String worldName) {
        World world = mock(World.class);
        when(world.getName()).thenReturn(worldName);
        Location location = mock(Location.class);
        when(location.getWorld()).thenReturn(world);
        when(location.getBlockX()).thenReturn(x);
        when(location.getBlockY()).thenReturn(y);
        when(location.getBlockZ()).thenReturn(z);
        return location;
    }
}
