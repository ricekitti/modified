package dev.rice.module.impl;

import dev.rice.client.ClientContext;
import dev.rice.client.ItemPolicyService;
import dev.rice.client.KeyState;
import dev.rice.client.ModuleActionService;
import dev.rice.client.RecoveredMath;
import dev.rice.client.SemanticEvent;
import dev.rice.module.EvidenceModule;
import dev.rice.module.ModuleCategory;
import dev.rice.setting.StringListSetting;
import java.util.Collections;
import net.minecraft.block.Block;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;

public final class AutoToolModule extends EvidenceModule {
   public final AutoToolModule.State state = new AutoToolModule.State();
   private InventoryPlayer changedInventory;
   private boolean suspended;

   public AutoToolModule() {
      super("autotool", "Auto Tool", ModuleCategory.WORLD);
      this.itemPolicy("allowed_blocks");
   }

   @Override
   public boolean listens(int type) {
      return type == 25 || type == 26;
   }

   @Override
   public boolean settingVisible(String key) {
      return !key.equals("allowed_blocks") || this.flag("restrict_allowed_blocks");
   }

   @Override
   public void onEvent(final ClientContext context, SemanticEvent event) {
      if (event.type == 26) {
         this.cleanup(context);
         ((StringListSetting)this.findSetting("allowed_blocks")).set(Collections.emptyList());
      } else if (context.ready()) {
         // Suspend entirely while any inventory/container GUI is open: GuiInventory,
         // GuiChest and every other GuiContainer extend this type. While suspended,
         // undo any in-flight auto-switch once, send no held-item-change packets, and
         // stay out of the way of manual clicks and other inventory modules. On the
         // close, recompute from scratch rather than applying a stale pending switch.
         if (context.minecraft().currentScreen instanceof GuiContainer) {
            if (!this.suspended) {
               this.cleanup(context);
               this.suspended = true;
            }

            return;
         }

         if (this.suspended) {
            this.suspended = false;
            this.state.cooldown = 0;
         }

         final InventoryPlayer inventory = context.player().inventory;
         AutoToolModule.Access access = new AutoToolModule.Access() {
            private Block block;

            @Override
            public int current() {
               return inventory.currentItem;
            }

            @Override
            public boolean heldAllowed() {
               ItemStack stack = inventory.getCurrentItem();
               // Empty hand counts as a fist, not "other": some setups return an air stack (id 0) instead of null,
               // which would otherwise fall through to typeAllowed and be classified as other.
               if (stack == null || stack.getItem() == null || Item.getIdFromItem(stack.getItem()) == 0) {
                  return AutoToolModule.this.flag("allow_using_fists");
               }
               // Never auto-switch away from a stick (a knockback stick is just an enchanted stick): keep it in
               // hand so looking at a block while holding the KB stick does not swap you to a pickaxe.
               if (stack.getItem() == net.minecraft.init.Items.stick) {
                  return false;
               }
               return AutoToolModule.typeAllowed(
                  Item.getIdFromItem(stack.getItem()),
                  AutoToolModule.this.flag("allow_using_sword"),
                  AutoToolModule.this.flag("allow_using_tools"),
                  AutoToolModule.this.flag("allow_using_other")
               );
            }

            @Override
            public boolean usingItem() {
               return context.player().isUsingItem();
            }

            @Override
            public boolean attackDown() {
               return KeyState.physical(context.minecraft().gameSettings.keyBindAttack);
            }

            @Override
            public boolean sneakDown() {
               return context.minecraft().gameSettings.keyBindSneak.isKeyDown();
            }

            @Override
            public boolean acquireBlock() {
               MovingObjectPosition hit = context.minecraft().objectMouseOver;
               if (hit != null && hit.typeOfHit == MovingObjectType.BLOCK) {
                  this.block = context.world().getBlockState(hit.getBlockPos()).getBlock();
                  return this.block != null;
               } else {
                  return false;
               }
            }

            @Override
            public boolean blockAllowed() {
               return ItemPolicyService.contains(AutoToolModule.this.names("allowed_blocks"), Block.getIdFromBlock(this.block));
            }

            @Override
            public Float score(int index) {
               ItemStack stack = inventory.getStackInSlot(index);
               return stack == null ? null : stack.getStrVsBlock(this.block);
            }

            @Override
            public boolean claim() {
               return ModuleActionService.claim(AutoToolModule.this);
            }

            @Override
            public void release() {
               ModuleActionService.release(AutoToolModule.this);
            }

            @Override
            public void select(int slot) {
               inventory.currentItem = slot;
               AutoToolModule.this.changedInventory = inventory;
            }
         };
         this.state
            .update(true, this.number("activation_time"), this.flag("switch_back"), this.flag("require_sneak"), this.flag("restrict_allowed_blocks"), access);
         if (this.state.originalSlot == -1) {
            this.changedInventory = null;
         }
      }
   }

   @Override
   protected void onDisable(ClientContext context) {
      this.cleanup(context);
   }

   @Override
   public void onWorldUnload(ClientContext context) {
      this.cleanup(context);
   }

   private void cleanup(ClientContext context) {
      ModuleActionService.release(this);
      if (this.state.originalSlot != -1 && this.changedInventory != null) {
         this.changedInventory.currentItem = this.state.originalSlot;
      }

      this.state.originalSlot = -1;
      this.state.cooldown = 0;
      this.changedInventory = null;
      this.suspended = false;
   }

   public static boolean typeAllowed(int id, boolean sword, boolean tool, boolean other) {
      switch (id - 256) {
         case 0:
         case 1:
         case 2:
         case 13:
         case 14:
         case 15:
         case 17:
         case 18:
         case 19:
         case 21:
         case 22:
         case 23:
         case 28:
         case 29:
         case 30:
         case 103:
            return tool;
         case 11:
         case 12:
         case 16:
         case 20:
         case 27:
            return sword;
         default:
            return other;
      }
   }

   public interface Access {
      int current();

      boolean heldAllowed();

      boolean usingItem();

      boolean attackDown();

      boolean sneakDown();

      boolean acquireBlock();

      boolean blockAllowed();

      Float score(int var1);

      boolean claim();

      void release();

      void select(int var1);
   }

   public static final class State {
      public int originalSlot = -1;
      public int lastSelectedSlot;
      public int cooldown;

      public void update(boolean ready, float delay, boolean switchBack, boolean requireSneak, boolean restrictBlocks, AutoToolModule.Access access) {
         if (ready) {
            int current = access.current();
            if (this.originalSlot != -1 && this.lastSelectedSlot != current) {
               this.originalSlot = -1;
               this.restore(access);
            } else if ((this.originalSlot != -1 || access.heldAllowed())
               && !access.usingItem()
               && access.attackDown()
               && (!requireSneak || access.sneakDown())
               && access.acquireBlock()
               && (!restrictBlocks || access.blockAllowed())) {
               if (this.cooldown >= 1) {
                  this.cooldown--;
                  this.restore(access);
               } else {
                  float best = 1.0F;
                  int bestIndex = current;

                  for (int offset = 0; offset < 9; offset++) {
                     int index = (current + offset) % 9;
                     Float score = access.score(index);
                     if (score != null && score > best) {
                        best = score;
                        bestIndex = index;
                     }
                  }

                  if (bestIndex != current) {
                     if (access.claim()) {
                        access.select(bestIndex);
                        this.lastSelectedSlot = bestIndex;
                        if (this.originalSlot == -1 && switchBack) {
                           this.originalSlot = current;
                        }
                     }

                     if (bestIndex == -1) {
                        this.restore(access);
                     }
                  }
               }
            } else {
               this.cooldown = RecoveredMath.truncateInt(delay);
               this.restore(access);
            }
         }
      }

      public void restore(AutoToolModule.Access access) {
         access.release();
         if (this.originalSlot != -1) {
            access.select(this.originalSlot);
            this.originalSlot = -1;
         }
      }
   }
}
