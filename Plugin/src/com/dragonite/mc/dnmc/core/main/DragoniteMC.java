package com.dragonite.mc.dnmc.core.main;


import com.dragonite.mc.dnmc.core.DragoniteMCAPI;
import com.dragonite.mc.dnmc.core.ModuleImplmentor;
import com.dragonite.mc.dnmc.core.caxerx.ItemBuilderEventListener;
import com.dragonite.mc.dnmc.core.chatformat.ChatFormatListener;
import com.dragonite.mc.dnmc.core.chatformat.FormatDatabaseManager;
import com.dragonite.mc.dnmc.core.chatformat.NameTagHandler;
import com.dragonite.mc.dnmc.core.command.DNCoreCommand;
import com.dragonite.mc.dnmc.core.command.HelpCommand;
import com.dragonite.mc.dnmc.core.config.implement.DNMCoreConfig;
import com.dragonite.mc.dnmc.core.ericlam.ChatRunnerHandler;
import com.dragonite.mc.dnmc.core.factory.CoreFactory;
import com.dragonite.mc.dnmc.core.listener.EventListener;
import com.dragonite.mc.dnmc.core.listener.VersionUpdateListener;
import com.dragonite.mc.dnmc.core.listener.WorldListeners;
import com.dragonite.mc.dnmc.core.listener.cancelevent.OptionalListener;
import com.dragonite.mc.dnmc.core.managers.*;
import com.dragonite.mc.dnmc.core.misc.world.WorldLoadedException;
import com.dragonite.mc.dnmc.core.misc.world.WorldNonExistException;
import com.dragonite.mc.dnmc.core.updater.DragoniteResourceManager;
import com.dragonite.mc.dnmc.core.updater.SpigotResourceManager;
import com.dragonite.mc.dnmc.core.worlds.BukkitWorldHandler;
import com.dragonite.mc.dnmc.core.listener.UpdateFormatListener;
import com.google.inject.Guice;
import com.google.inject.Injector;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public class DragoniteMC extends JavaPlugin implements DragoniteMCAPI {
    public static Plugin plugin;
    private static DragoniteMCAPI api;
    private static DNMCoreConfig dnmCoreConfig;
    private static SkinDatabaseManager skinDatabaseManager;
    private static HelpPagesManager helpPagesManager;
    private static FormatDatabaseManager formatDatabaseManager;
    private static BukkitWorldHandler dnmcWorldManager;
    private static ChatRunnerHandler chatRunnerHandler;
    private static ItemBuilderEventListener itemEventManager;
    private final ModuleImplmentor moduleImplmentor = new ModuleImplmentor();
    private BungeeManager bungeeManager;
    private ChatFormatManager chatFormatManager;
    private CoreConfig coreConfig;
    private CoreScheduler coreScheduler;
    private NameTagManager nameTagManager;
    private PlayerSkinManager playerSkinManager;
    private SQLDataSource sqlDataSource;
    private TabListManager tabListManager;
    private CommandRegister commandRegister;
    private EventCancelManager eventCancelManager;
    private WorldManager worldManager;
    private CoreFactory coreFactory;
    private RedisDataSource redisDataSource;
    private Injector injector;
    private SpigotResourceManager spigotResourceManager;
    private DragoniteResourceManager dragoniteResourceManager;

    public static DNMCoreConfig getDnmCoreConfig() {
        return dnmCoreConfig;
    }

    public static SkinDatabaseManager getSkinDatabaseManager() {
        return skinDatabaseManager;
    }

    public static ChatRunnerHandler getChatRunnerHandler() {
        return chatRunnerHandler;
    }

    public static ItemBuilderEventListener getItemEventManager() {
        return itemEventManager;
    }

    public static HelpPagesManager getHelpPagesManager() {
        return helpPagesManager;
    }

    public static FormatDatabaseManager getFormatDatabaseManager() {
        return formatDatabaseManager;
    }

    public static DragoniteMCAPI getAPI() {
        return api;
    }

    public static BukkitWorldHandler getDnmcWorldManager() {
        return dnmcWorldManager;
    }

    @Override
    public void onLoad() {
        moduleImplmentor.register(Plugin.class, this);
        injector = Guice.createInjector(moduleImplmentor);
        api = this;

        plugin = injector.getInstance(Plugin.class);
        coreConfig = injector.getInstance(CoreConfig.class);
        bungeeManager = injector.getInstance(BungeeManager.class);
        coreScheduler = injector.getInstance(CoreScheduler.class);
        nameTagManager = injector.getInstance(NameTagManager.class);
        chatFormatManager = injector.getInstance(ChatFormatManager.class);
        playerSkinManager = injector.getInstance(PlayerSkinManager.class);
        sqlDataSource = injector.getInstance(SQLDataSource.class);
        tabListManager = injector.getInstance(TabListManager.class);
        commandRegister = injector.getInstance(CommandRegister.class);
        worldManager = injector.getInstance(WorldManager.class);
        chatRunnerHandler = injector.getInstance(ChatRunnerHandler.class);
        itemEventManager = injector.getInstance(ItemBuilderEventListener.class);
        coreFactory = injector.getInstance(CoreFactory.class);
        helpPagesManager = injector.getInstance(HelpPagesManager.class);
        formatDatabaseManager = injector.getInstance(FormatDatabaseManager.class);
        skinDatabaseManager = injector.getInstance(SkinDatabaseManager.class);
        eventCancelManager = injector.getInstance(EventCancelManager.class);
        spigotResourceManager = injector.getInstance(SpigotResourceManager.class);
        dragoniteResourceManager = injector.getInstance(DragoniteResourceManager.class);

        dnmCoreConfig = (DNMCoreConfig) coreConfig;
        dnmcWorldManager = (BukkitWorldHandler) worldManager;

        if (dnmCoreConfig.getDatabase().redis.enabled) {
            redisDataSource = injector.getInstance(RedisDataSource.class);
        }


    }

    @Override
    public void onEnable() {
        ((Format) chatFormatManager).setup(LuckPermsProvider.get());
        ((NameTagHandler) nameTagManager).setup();
        Bukkit.getScheduler().runTask(plugin, () -> {
            getServer().getPluginManager().registerEvents(injector.getInstance(EventListener.class), this);
            Optional.ofNullable(this.getCommand("help")).ifPresent(c -> c.setExecutor(injector.getInstance(HelpCommand.class)));
            getServer().getPluginManager().registerEvents(injector.getInstance(ChatFormatListener.class), this);
        });
        new UpdateFormatListener(this, LuckPermsProvider.get()).register();
        ConsoleCommandSender console = getServer().getConsoleSender();
        console.sendMessage(ChatColor.GREEN + "Initializing DragoniteMC Libraries");
        if (getServer().getPluginManager().isPluginEnabled("Vault")) {
            console.sendMessage(ChatColor.AQUA + "Successfully hooked Vault plugin.");
        }
        getServer().getPluginManager().registerEvents(chatRunnerHandler, this);
        getServer().getPluginManager().registerEvents(itemEventManager, this);
        Random random = new Random();
        var enableds = Arrays.stream(getServer().getPluginManager().getPlugins()).filter(Plugin::isEnabled).toArray(Plugin[]::new);
        int index = random.nextInt(enableds.length);
        getServer().getPluginManager().registerEvents(new WorldListeners(), this);

        commandRegister.registerCommand(this, new DNCoreCommand());

        if (dnmCoreConfig.getCancel().cancelEventsEnabled) {
            getServer().getPluginManager().registerEvents(new OptionalListener(), this);
        }

        console.sendMessage(ChatColor.YELLOW + "Connecting to mysql to get skin database.......");

        if (dnmCoreConfig.getDatabase().host == null) {
            getLogger().warning("Seems the Database.yml hasn't been loaded properly, restarting server...");
            getServer().spigot().restart();
            return;
        }

        console.sendMessage(ChatColor.GREEN + "Successfully connected to mysql.");

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            formatDatabaseManager.getChatformat(); //Get the chatformat from mysql
            helpPagesManager.getPages(); //Get the help pages from mysql
        });

        this.getServer().getPluginManager().registerEvents(new VersionUpdateListener(this), this);


        dnmcWorldManager.loadDefaultWorld();
        if (dnmCoreConfig.getConfig().autoLoadExtraWorlds) {
            dnmcWorldManager.getWorldList().entrySet().stream()
                    .filter(Map.Entry::getValue)
                    .filter(en -> Bukkit.getWorld(en.getKey()) != null)
                    .map(Map.Entry::getKey)
                    .forEach(world -> {
                        try {
                            dnmcWorldManager.loadWorld(world).whenComplete((w, ex) -> {
                                if (ex != null) {
                                    ex.printStackTrace();
                                }
                                var result = w != null;
                                getLogger().info("世界 " + world + " 加載 " + (result ? "成功" : "失敗") + "。");
                            });
                        } catch (WorldNonExistException e) {
                            getLogger().warning("加載世界 " + e.getWorld() + " 失敗, 世界不存在。");
                        } catch (WorldLoadedException e) {
                            getLogger().warning("加載世界 " + e.getWorld() + " 失敗, 世界已加載。");
                        }
                    });
        }
    }

    @Override
    public void onDisable() {
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        getLogger().info("DragoniteMC Libraries Disabled");
        dnmcWorldManager.saveAll();
    }

    @Override
    public BungeeManager getBungeeManager() {
        return bungeeManager;
    }

    @Override
    public ChatFormatManager getChatFormatManager() {
        return chatFormatManager;
    }

    @Override
    public CoreFactory getFactory() {
        return coreFactory;
    }

    @Override
    public CoreScheduler getCoreScheduler() {
        return coreScheduler;
    }

    @Override
    public CommandRegister getCommandRegister() {
        return commandRegister;
    }

    @Override
    public NameTagManager getNameTagManager() {
        return nameTagManager;
    }

    @Override
    public PlayerSkinManager getPlayerSkinManager() {
        return playerSkinManager;
    }

    @Override
    public SQLDataSource getSQLDataSource() {
        return sqlDataSource;
    }

    @Override
    public RedisDataSource getRedisDataSource() {
        return Optional.ofNullable(redisDataSource).orElseThrow(() -> new IllegalStateException("Redis has not enabled in config"));
    }

    @Override
    public TabListManager getTabListManager() {
        return tabListManager;
    }

    @Override
    public CoreConfig getCoreConfig() {
        return coreConfig;
    }

    @Override
    public WorldManager getWorldManager() {
        return worldManager;
    }

    @Override
    public EventCancelManager getEventCancelManager() {
        return eventCancelManager;
    }

    @Override
    public ResourceManager getResourceManager(ResourceManager.Type type) {
        return type == ResourceManager.Type.SPIGOT ? spigotResourceManager : dragoniteResourceManager;
    }
}
