package org.queercraft.qctaxes;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;
import org.queercraft.qctaxes.Utils.Bracket;
import org.queercraft.qctaxes.commands.TaxCommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public final class QCTaxes extends JavaPlugin {
    // Vault
    private static Economy econ = null;

    // Config
    private FileConfiguration config;

    @Override
    public void onEnable() {
        Logger logger = getLogger();
        logger.info("Enabling QCTaxes...");


        try {
            BukkitScheduler scheduler = Bukkit.getScheduler();
            saveDefaultConfig();
            config = getConfig();

            // vault setup
            if (!setupEconomy() ) {
                logger.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
                getServer().getPluginManager().disablePlugin(this);
                return;
            }

            // read brackets from config
            ConfigurationSection configBrackets = config.getConfigurationSection("brackets");
            List<Bracket> brackets = new ArrayList<>();
            for (String rank : configBrackets.getKeys(true)) {
                brackets.add(new Bracket(config.getDouble("brackets."+rank+".max"), config.getDouble("brackets."+rank+".percent")));
            }

            Objects.requireNonNull(getCommand("tax")).setExecutor(new TaxCommand(this, scheduler, brackets, logger));
        } catch (Exception e) {
            getLogger().severe("An unexpected error occurred while enabling QCTaxes:");
            getLogger().severe("Exception type: " + e.getClass().getName());
            getLogger().severe("Message: " + e.getMessage());
            Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).forEach(stackTraceLine -> getLogger().severe("    at " + stackTraceLine));
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("QCTaxes disabled!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public static Economy getEconomy() {
        return econ;
    }

    public @NotNull FileConfiguration getConfig() {
        return this.config;
    }
}
