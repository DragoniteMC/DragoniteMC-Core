package com.dragonite.mc.dnmc.core.command.dnmc.helplist;

import com.dragonite.mc.dnmc.core.config.implement.DNMCoreConfig;
import com.dragonite.mc.dnmc.core.misc.commands.CommandNode;
import com.dragonite.mc.dnmc.core.main.DragoniteMC;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import java.util.List;

public class HelpListReUploadCommand extends CommandNode {


    public HelpListReUploadCommand(CommandNode parent) {
        super(parent, "reupload", "dragonite.helplist.reupload", "重新上傳內容到指定頁面", "<page> <是否為Staff頁面>");
    }


    @Override
    public boolean executeCommand(@Nonnull CommandSender sender, @Nonnull List<String> args) {
        DNMCoreConfig config = DragoniteMC.getDnmCoreConfig();
        List<String> list = HelpListUploadCommand.getPages(args.toArray(String[]::new), config, sender);
        if (list == null) return false;
        boolean isStaff = Boolean.parseBoolean(args.get(1));
        String page = args.get(0).toLowerCase();
        //Success message
        String success = config.getPrefix() + "§a更改成功。";

        //Fail message
        String fail = config.getPrefix() + "§c更改失敗。";

        DragoniteMC.getAPI().getCoreScheduler().runAsync(() -> {
            boolean done = DragoniteMC.getHelpPagesManager().replacePage(page, list, isStaff);
            sender.sendMessage(done ? success : fail);
        });
        return true;
    }

    @Override
    public List<String> executeTabCompletion(@Nonnull CommandSender sender, @Nonnull List<String> args) {
        return null;
    }
}
