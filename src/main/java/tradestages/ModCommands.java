package tradestages;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ModCommands {
   public static void registerCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
      dispatcher.register(Commands.literal("tradestages"));
   }
}
