package tradestages;

import fermiumbooter.FermiumRegistryAPI;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;

import javax.annotation.Nullable;
import java.util.Map;

@IFMLLoadingPlugin.Name("TradeStagesPlugin")
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.SortingIndex(1001)
public class TradeStagesPlugin implements IFMLLoadingPlugin {

    public TradeStagesPlugin() {
        MixinBootstrap.init();
        FermiumRegistryAPI.enqueueMixin(false, "tradestages.mixins.json");
        FermiumRegistryAPI.enqueueMixin(true, "tradestages.mixins.easiervillagertrading.json", () -> Loader.isModLoaded("easiervillagertrading"));
    }

    @Override public String[] getASMTransformerClass() {
        return new String[0];
    }
    @Override @Nullable public String getModContainerClass() {
        return null;
    }
    @Override @Nullable public String getSetupClass() {
        return null;
    }
    @Override public void injectData(Map<String, Object> data) {}
    @Override @Nullable public String getAccessTransformerClass() {return null;}
}
