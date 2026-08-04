package tradestages.mixins.vanilla;

import net.minecraftforge.fml.common.registry.VillagerRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(VillagerRegistry.VillagerProfession.class)
public interface VillagerProfessionAccessor {
    @Accessor("careers")
    List<VillagerRegistry.VillagerCareer> getCareers();
}