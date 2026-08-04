package tradestages;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Supplier;

@IFMLLoadingPlugin.Name("TradeStagesPlugin")
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.SortingIndex(1001)
public class TradeStagesPlugin implements IFMLLoadingPlugin {

    public TradeStagesPlugin() {
        MixinBootstrap.init();
        Mixins.addConfiguration("tradestages.mixins.json");
        try {
            Method method = Class.forName("fermiumbooter.FermiumRegistryAPI").getMethod("enqueueMixin", boolean.class, String.class, Supplier.class);
            method.invoke(null, true, "tradestages.mixins.easiervillagertrading.json", (Supplier<Boolean>) () -> Loader.isModLoaded("easiervillagertrading"));
        } catch (Exception e) {
            System.out.println("[TradeStagesPlugin] Couldn't find FermiumBooter. The compat mixin for EasierVillagerTrading will not be loaded.");
        }
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
