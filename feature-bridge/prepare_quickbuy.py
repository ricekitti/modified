"""Recover independent Quick Buy helpers. Deliberately exclude the blocked purchase loop."""
from pathlib import Path
import re,json,hashlib
from prepare_pearl import body

def prepare(root,adapted):
    ref=root.parent/'valen-selected-ref-20261105/extracted/valen-selected-src/src/main/java/valen/module/impl/minigames'
    out=adapted/'quickbuy';out.mkdir(exist_ok=True);paths=[];hashes={}
    for name in ['BedWarsShopItem','QuickBuyPresetsSetting','QuickBuyPresetScreen']:
        p=ref/('quickbuy/'+name+'.java');hashes[p.name]=hashlib.sha256(p.read_bytes()).hexdigest()
        text=p.read_text();text=text[text.index('package valen.'):]
        text=re.sub(r'^import valen\..*;\n','',text,flags=re.M).replace('package valen.module.impl.minigames.quickbuy;','package dev.rice.quickbuy;')
        if name=='BedWarsShopItem':
            text=text.replace('List list = ((ItemPotion)itemStack.getItem()).getEffects', 'List<PotionEffect> list = ((ItemPotion)itemStack.getItem()).getEffects')
        if name=='QuickBuyPresetsSetting':
            text=text.replace('extends Setting {','{').replace('        super("Presets");\n','').replace('        this.visible = false;\n','').replace('    @Override\n','')
            text=body(text,'private void changed()', '        this.revision++;')
            text=text.rsplit('}',1)[0]+'''    private long revision;
    public long getRevision(){return revision;}
    public String getName(){return "Presets";}
    // Missing inner class is a data holder; synthetic accessor behavior follows its exact call sites.
    public static final class Purchase {
        private final String itemId;private int count;
        public Purchase(String itemId,int count){this.itemId=itemId;this.count=count;}
        public String getItemId(){return itemId;}public int getCount(){return count;}
        public Purchase copy(){return new Purchase(itemId,count);}
        static String access$000(Purchase p){return p.itemId;}
        static int access$100(Purchase p){return p.count;}
        static int access$102(Purchase p,int count){return p.count=count;}
        static int access$108(Purchase p){return p.count++;}
        static int access$110(Purchase p){return p.count--;}
    }
}
'''
        if name=='QuickBuyPresetScreen':
            text=text.replace('Shape.','QuickBuyUi.').replace('ClickGuiStyle.','QuickBuyUi.')
            text=text.replace('"Auto Quick Buy"','"Quick Buy Presets"')
            # CFR leaves the unused false-branch temp unassigned on the true branch; n6 is the selected color.
            text=text.replace('QuickBuyUi.capsule(n, n2, n3, n4, n5);','QuickBuyUi.capsule(n, n2, n3, n4, n6);')
        p=out/(name+'.java');p.write_text(text,encoding='utf-8');paths.append(p)
    module=ref/'AutoQuickBuy.java';original=module.read_text();hashes[module.name]=hashlib.sha256(module.read_bytes()).hexdigest()
    methods=[]
    for signature in ['private ContainerChest currentItemShop()','private int findItemSlot(ContainerChest containerChest, BedWarsShopItem bedWarsShopItem)','private int findCategorySlot(ContainerChest containerChest, BedWarsShopItem.Category category2)']:
        left=original.index(signature);start=original.index('{',left);depth=1;right=start+1
        while depth:depth+=(original[right]=='{')-(original[right]=='}');right+=1
        methods.append(original[left:right].replace('private ','public static ',1).replace('AutoQuickBuy.mc','mc'))
    p=out/'QuickBuyShopMatcher.java';p.write_text('''package dev.rice.quickbuy;
import net.minecraft.client.Minecraft;import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;import net.minecraft.inventory.IInventory;import net.minecraft.item.ItemStack;
public final class QuickBuyShopMatcher {
private static final Minecraft mc=Minecraft.getMinecraft();
'''+ '\n'.join(methods)+'\n}\n');paths.append(p)
    (out/'source-hashes.json').write_text(json.dumps(hashes,indent=2)+'\n')
    return paths
