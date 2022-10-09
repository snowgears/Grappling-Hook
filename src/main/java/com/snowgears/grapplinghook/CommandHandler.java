package com.snowgears.grapplinghook;

import com.snowgears.grapplinghook.utils.HookSettings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandHandler extends BukkitCommand {

    private GrapplingHook plugin;

    public CommandHandler(GrapplingHook instance, String permission, String name, String description, String usageMessage, List<String> aliases) {
        super(name, description, usageMessage, aliases);
        this.setPermission(permission);
        plugin = instance;
        try {
            register();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                //these are commands only operators have access to
                if (player.hasPermission("grapplinghook.operator") || player.isOp()) {
                    player.sendMessage("/"+this.getName()+" give <hook_id> - give yourself a hook");
                    player.sendMessage("/"+this.getName()+" give <hook_id> <player> - give player a hook");
                    return true;
                }
            }
            //these are commands that can be executed from the console
            else{
                sender.sendMessage("/"+this.getName()+" give <hook_id> <player> - give player a hook");
                return true;
            }
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if ((plugin.usePerms() && !player.hasPermission("grapplinghook.operator")) || (!plugin.usePerms() && !player.isOp())) {
                        player.sendMessage(ChatColor.RED+"You are not authorized to use this command.");
                        return true;
                    }
                    plugin.reload();
                    player.sendMessage(ChatColor.GREEN+"GrapplingHook has been reloaded.");
                } else {
                    plugin.reload();
                    sender.sendMessage("[GrapplingHook] Reloaded plugin.");
                    return true;
                }
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("give")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if ((plugin.usePerms() && !player.hasPermission("grapplinghook.operator")) || (!plugin.usePerms() && !player.isOp())) {
                        player.sendMessage(ChatColor.RED+"You are not authorized to use this command.");
                        return true;
                    }

                    String hookID = args[1];
                    HookSettings hookSettings = plugin.getGrapplingListener().getHookSettings(hookID);
                    if(hookSettings == null){
                        player.sendMessage(ChatColor.RED+"No grappling hook found with id: "+hookID);
                        return true;
                    }
                    player.getInventory().addItem(hookSettings.getHookItem());
                    player.sendMessage(ChatColor.GREEN+"Gave grappling hook <"+hookID+"> to "+player.getName());
                    return true;
                }
            }
        }
        else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("give")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if ((plugin.usePerms() && !player.hasPermission("grapplinghook.operator")) || (!plugin.usePerms() && !player.isOp())) {
                        player.sendMessage(ChatColor.RED+"You are not authorized to use this command.");
                        return true;
                    }

                    String hookID = args[1];
                    HookSettings hookSettings = plugin.getGrapplingListener().getHookSettings(hookID);
                    if(hookSettings == null){
                        player.sendMessage(ChatColor.RED+"No grappling hook found with id: "+hookID);
                        return true;
                    }

                    Player playerToGive = plugin.getServer().getPlayer(args[2]);
                    if(playerToGive == null){
                        player.sendMessage(ChatColor.RED+"No player found online with name: "+args[2]);
                        return true;
                    }
                    playerToGive.getInventory().addItem(hookSettings.getHookItem());
                    player.sendMessage(ChatColor.GREEN+"Gave grappling hook <"+hookID+"> to "+args[2]);
                    playerToGive.sendMessage(ChatColor.GREEN+player.getName()+" gave you a grappling hook <"+hookID+">");
                    return true;
                }
                else {
                    String hookID = args[1];
                    HookSettings hookSettings = plugin.getGrapplingListener().getHookSettings(hookID);
                    if(hookSettings == null){
                        sender.sendMessage("No grappling hook found with id: "+hookID);
                        return true;
                    }

                    Player playerToGive = plugin.getServer().getPlayer(args[2]);
                    if(playerToGive == null){
                        sender.sendMessage("No player found online with name: "+args[2]);
                        return true;
                    }
                    playerToGive.getInventory().addItem(hookSettings.getHookItem());
                    sender.sendMessage("Gave grappling hook <"+hookID+"> to "+args[2]);
                    playerToGive.sendMessage(ChatColor.GREEN+"The server has given you a grappling hook <"+hookID+">");
                    return true;
                }
            }
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        List<String> results = new ArrayList<>();
        if (args.length == 0) {
            results.add(this.getName());
        }
        else if (args.length == 1) {

            boolean showOperatorCommands = false;
            if(sender instanceof Player){
                Player player = (Player)sender;

                if(player.hasPermission("grapplinghook.operator") || player.isOp()) {
                    showOperatorCommands = true;
                }
            }
            else{
                showOperatorCommands = true;
            }

            if(showOperatorCommands){
                results.add("give");
                results.add("reload");
            }
            return sortedResults(args[0], results);
        }
        else if (args.length == 2) {

            boolean showOperatorCommands = false;
            if(sender instanceof Player){
                Player player = (Player)sender;

                if(player.hasPermission("grapplinghook.operator") || player.isOp()) {
                    showOperatorCommands = true;
                }
            }
            else{
                showOperatorCommands = true;
            }

            if(showOperatorCommands && args[0].equalsIgnoreCase("give")){
                for(String hookID : plugin.getGrapplingListener().getHookIDs()) {
                    results.add(hookID);
                }
            }
            return sortedResults(args[1], results);
        }
        else if (args.length == 3) {

            boolean showOperatorCommands = false;
            if(sender instanceof Player){
                Player player = (Player)sender;

                if(player.hasPermission("grapplinghook.operator") || player.isOp()) {
                    showOperatorCommands = true;
                }
            }
            else{
                showOperatorCommands = true;
            }

            HookSettings hookSettings = plugin.getGrapplingListener().getHookSettings(args[1]);

            if(showOperatorCommands && hookSettings != null){
                results.add("<player name>");
            }
            return sortedResults(args[1], results);
        }
        return results;
    }

    private void register()
            throws ReflectiveOperationException {
        final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
        bukkitCommandMap.setAccessible(true);

        CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
        commandMap.register(this.getName(), this);
    }

    // Sorts possible results to provide true tab auto complete based off of what is already typed.
    public List <String> sortedResults(String arg, List<String> results) {
        final List <String> completions = new ArrayList < > ();
        StringUtil.copyPartialMatches(arg, results, completions);
        Collections.sort(completions);
        results.clear();
        for (String s: completions) {
            results.add(s);
        }
        return results;
    }
}
