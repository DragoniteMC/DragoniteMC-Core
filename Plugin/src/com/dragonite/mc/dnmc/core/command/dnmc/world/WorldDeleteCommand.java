package com.dragonite.mc.dnmc.core.command.dnmc.world;

import com.dragonite.mc.dnmc.core.misc.commands.CommandNode;
import com.dragonite.mc.dnmc.core.misc.world.WorldNonExistException;
import com.dragonite.mc.dnmc.core.main.DragoniteMC;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import java.util.List;

public class WorldDeleteCommand extends WorldCommandNode {


    public WorldDeleteCommand(CommandNode parent) {
        super(parent, "delete", "dragonite.world.delete", "刪除世界", "<world>", "remove", "rm", "del");
    }

    @Override
    public boolean executeCommand(@Nonnull CommandSender sender, @Nonnull List<String> args) {
        String name = args.get(0);
        if (WorldUnloadCommand.validateDefaultWorld(name)) {
            sender.sendMessage(prefix + "§c你無法刪除默認世界。");
            return false;
        }
        try {
            var success = DragoniteMC.getDnmcWorldManager().deleteWorld(name);
            sender.sendMessage(prefix + "世界 " + name + " 刪除 " + (success ? "成功" : "失敗") + "。");
        } catch (WorldNonExistException e) {
            sender.sendMessage(prefix + "§c世界 " + e.getWorld() + " 不存在!");
        }
        return true;
    }
}
