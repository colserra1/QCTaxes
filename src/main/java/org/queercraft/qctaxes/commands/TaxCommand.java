package org.queercraft.qctaxes.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.queercraft.qctaxes.QCTaxes;
import org.queercraft.qctaxes.Utils.Bracket;

import java.util.List;
import java.util.logging.Logger;

public class TaxCommand extends SafeCommandExecutor{

    private final JavaPlugin plugin;
    private final BukkitScheduler scheduler;
    List<Bracket> brackets;

    public TaxCommand(JavaPlugin plugin, BukkitScheduler scheduler, List<Bracket> brackets, Logger logger) {
        super(logger);
        this.plugin = plugin;
        this.scheduler = scheduler;
        this.brackets = brackets;
    }

    protected boolean execute(CommandSender sender, Command cmd, String label, String[] args) {
        scheduler.runTaskAsynchronously(plugin, () -> command(sender, args));
        return true;
    }

    public void command(CommandSender sender, String[] args) {
        String arg = args.length > 0 ? args[0].toLowerCase() : "";

        switch (arg) {
            case "":
                sender.sendMessage("Usage: /tax <command>");
                break;
            case "reload":
                handleReloadCommand(sender);
                break;
            case "all":
                handleTaxAllCommand(sender);
                break;
            default:
                break;
        }
    }
    public void handleReloadCommand(CommandSender sender){
        if (sender.hasPermission("tax.reload")) {
            plugin.reloadConfig();
            sender.sendMessage("The config has been reloaded.");
        } else {
            sender.sendMessage("You do not have permission to use this command.");
        }
    }

    public void handleTaxAllCommand(CommandSender sender) {
        if (sender.hasPermission("tax.all")) {
            if (plugin.getConfig().getBoolean("config.enabled")) {
                sender.sendMessage("Collecting Taxes from all online players...");
                int taxedPlayers = 0;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    taxPlayer(player);
                    taxedPlayers++;
                }
                sender.sendMessage("Taxed " + taxedPlayers + " players.");
            }else sender.sendMessage("Plugin not enabled.");
        }else {
            sender.sendMessage("You do not have permission to use this command.");
        }
    }

    public void taxPlayer(Player player) {
        if (player.getName() == null) {
            logger.info("Unable to find vault account for: " + player.getUniqueId());
            return;
        }
        double playerBal = QCTaxes.getEconomy().getBalance(player);
        double subtractAmount = 0;
        double taxedBal = 0;

        player.sendMessage("§aTax collection in progress, taxing a balance of §f"+String.format("%.2f", playerBal)+"M");

        for (Bracket bracket : brackets) {
            if (playerBal < bracket.getLimit()){
                subtractAmount += (playerBal-taxedBal)*(bracket.getTaxRate()/100);
                break;
            }else{
                subtractAmount += (bracket.getLimit()-taxedBal)*(bracket.getTaxRate()/100);
                taxedBal = bracket.getLimit();
            }
        }

        if (subtractAmount == 0) {
            player.sendMessage("§aNothing to tax!");
            return;
        }

        try {
            if (subtractAmount > 0) {
                QCTaxes.getEconomy().withdrawPlayer(player, subtractAmount);
            } else {
                QCTaxes.getEconomy().depositPlayer(player, Math.abs(subtractAmount));
            }
        } catch (IllegalStateException e) {
            if (subtractAmount > 0) {
                double finalSubtractAmount = subtractAmount;
                Bukkit.getScheduler().runTask(plugin, () -> QCTaxes.getEconomy().withdrawPlayer(player, finalSubtractAmount));
            } else {
                double finalSubtractAmount1 = subtractAmount;
                Bukkit.getScheduler().runTask(plugin, () -> QCTaxes.getEconomy().depositPlayer(player, Math.abs(finalSubtractAmount1)));
            }
        }

        player.sendMessage("§aTax collection finished, taxed §f"+String.format("%.2f", subtractAmount)+"M");
    }
}
