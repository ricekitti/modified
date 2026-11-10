package dev.rice.forge;

import java.util.Map;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.Name("Rice Modified")
@IFMLLoadingPlugin.MCVersion("1.8.9")
@IFMLLoadingPlugin.SortingIndex(1001)
@IFMLLoadingPlugin.TransformerExclusions({"dev.rice.", "myau."})
public final class RiceLoadingPlugin implements IFMLLoadingPlugin {
    public String[] getASMTransformerClass() {
        return new String[]{"dev.rice.forge.RiceTransformer"};
    }
    public String getModContainerClass() { return null; }
    public String getSetupClass() { return null; }
    public String getAccessTransformerClass() { return null; }
    public void injectData(Map<String, Object> data) {
        ForgeEnvironment.setDevelopment(!Boolean.TRUE.equals(data.get("runtimeDeobfuscationEnabled")));
    }
}
