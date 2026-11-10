package dev.rice.inject.transform;

import dev.rice.inject.Mappings;
import dev.rice.shaded.asm.Opcodes;
import dev.rice.shaded.asm.tree.AbstractInsnNode;
import dev.rice.shaded.asm.tree.ClassNode;
import dev.rice.shaded.asm.tree.FieldInsnNode;
import dev.rice.shaded.asm.tree.InsnList;
import dev.rice.shaded.asm.tree.InsnNode;
import dev.rice.shaded.asm.tree.JumpInsnNode;
import dev.rice.shaded.asm.tree.LabelNode;
import dev.rice.shaded.asm.tree.LdcInsnNode;
import dev.rice.shaded.asm.tree.MethodInsnNode;
import dev.rice.shaded.asm.tree.MethodNode;
import dev.rice.shaded.asm.tree.TryCatchBlockNode;
import dev.rice.shaded.asm.tree.VarInsnNode;
import java.util.ArrayList;
import java.util.List;

public final class RicePatches implements Opcodes {
   /** Generated Mayu injector bodies (tools/port-mayu.py); each call below sits at its Mixin injector's own target point. */
   static final String MAYU = "dev/rice/client/hook/MayuHooks";
   static final String GAME_HOOKS = "dev/rice/client/hook/GameHooks";
   static final String RENDER_HOOKS = "dev/rice/client/hook/RenderHooks";
   static final String EVENTS = "dev/rice/client/ClientEventBridge";
   static final String EVENT = "dev/rice/client/SemanticEvent";
   static final String MINECRAFT = "net/minecraft/client/Minecraft";
   static final String ENTITY = "net/minecraft/entity/Entity";
   static final String LIVING = "net/minecraft/entity/EntityLivingBase";
   static final String PLAYER = "net/minecraft/client/entity/EntityPlayerSP";
   static final String CONTROLLER = "net/minecraft/client/multiplayer/PlayerControllerMP";
   static final String NETWORK = "net/minecraft/network/NetworkManager";
   static final String WORLD = "net/minecraft/world/World";
   static final String RENDER_INFO = "net/minecraft/client/renderer/ActiveRenderInfo";
   static final String RENDER_MANAGER = "net/minecraft/client/renderer/entity/RenderManager";
   static final String LIVING_RENDERER = "net/minecraft/client/renderer/entity/RendererLivingEntity";
   static final String RENDER = "net/minecraft/client/renderer/entity/Render";
   static final String ENTITY_RENDERER = "net/minecraft/client/renderer/EntityRenderer";
   static final String CHUNK_PROVIDER = "net/minecraft/client/multiplayer/ChunkProviderClient";
   static final String GL_STATE = "net/minecraft/client/renderer/GlStateManager";
   static final String GUI_SCREEN = "net/minecraft/client/gui/GuiScreen";
   static final String ITEM_RENDERER = "net/minecraft/client/renderer/ItemRenderer";

   private RicePatches() {
   }

   static String cls(String named) {
      return Mappings.mapClass(named);
   }

   static String desc(String named) {
      return Mappings.mapDescriptor(named);
   }

   static boolean isCall(AbstractInsnNode insn, String owner, String name, String desc) {
      if (!(insn instanceof MethodInsnNode)) {
         return false;
      } else {
         MethodInsnNode call = (MethodInsnNode)insn;
         return call.owner.equals(cls(owner)) && call.desc.equals(desc(desc)) && call.name.equals(Mappings.mapMethod(owner, name, desc));
      }
   }

   static boolean isExternalCall(AbstractInsnNode insn, String owner, String name, String desc) {
      if (!(insn instanceof MethodInsnNode)) {
         return false;
      } else {
         MethodInsnNode call = (MethodInsnNode)insn;
         return call.owner.equals(owner) && call.name.equals(name) && call.desc.equals(desc);
      }
   }

   static MethodInsnNode hook(String owner, String name, String namedDesc) {
      return new MethodInsnNode(184, owner, name, desc(namedDesc), false);
   }

   static AbstractInsnNode nextReal(AbstractInsnNode insn) {
      for (AbstractInsnNode node = insn.getNext(); node != null; node = node.getNext()) {
         if (node.getOpcode() >= 0) {
            return node;
         }
      }

      return null;
   }

   static AbstractInsnNode prevReal(AbstractInsnNode insn) {
      for (AbstractInsnNode node = insn.getPrevious(); node != null; node = node.getPrevious()) {
         if (node.getOpcode() >= 0) {
            return node;
         }
      }

      return null;
   }

   static int redirectInputLoop(MethodNode method, String extOwner, String extName, String extDesc, String hookOwner, String hookName) {
      int count = 0;

      for (AbstractInsnNode insn : method.instructions.toArray()) {
         if (isExternalCall(insn, extOwner, extName, extDesc)) {
            AbstractInsnNode cond = nextReal(insn);
            if (cond != null && cond.getOpcode() == 153) {
               int pollAt = method.instructions.indexOf(insn);
               if (method.instructions.indexOf(((JumpInsnNode)cond).label) > pollAt) {
                  AbstractInsnNode body = nextReal(cond);
                  if (body == null || body.getOpcode() != 167 || method.instructions.indexOf(((JumpInsnNode)body).label) > pollAt) {
                     method.instructions.set(insn, hook(hookOwner, hookName, extDesc));
                     count++;
                  }
               }
            }
         }
      }

      return count;
   }

   static boolean isReturn(AbstractInsnNode insn) {
      return insn.getOpcode() >= 172 && insn.getOpcode() <= 177;
   }

   static InsnList list(AbstractInsnNode... nodes) {
      InsnList list = new InsnList();

      for (AbstractInsnNode node : nodes) {
         list.add(node);
      }

      return list;
   }

   static InsnList returnIfTrue() {
      LabelNode proceed = new LabelNode();
      return list(new JumpInsnNode(153, proceed), new InsnNode(177), proceed);
   }

   static int beforeReturns(MethodNode method, RicePatches.InsnFactory factory) {
      int count = 0;

      for (AbstractInsnNode insn : method.instructions.toArray()) {
         if (isReturn(insn)) {
            method.instructions.insertBefore(insn, factory.create());
            count++;
         }
      }

      return count;
   }

   static int scope(MethodNode method, InsnList entry, RicePatches.InsnFactory exit) {
      LabelNode start = new LabelNode();
      LabelNode end = new LabelNode();
      LabelNode handler = new LabelNode();
      int returns = beforeReturns(method, exit);
      InsnList head = new InsnList();
      if (entry != null) {
         head.add(entry);
      }

      head.add(start);
      method.instructions.insert(head);
      method.instructions.add(end);
      method.instructions.add(handler);
      method.instructions.add(exit.create());
      method.instructions.add(new InsnNode(191));
      method.tryCatchBlocks.add(new TryCatchBlockNode(start, end, handler, null));
      return returns;
   }

   public static List<RicePatches.ClassEdits> create(boolean forge) {
      List<RicePatches.ClassEdits> patches = new ArrayList<>();
      String doRender = "(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V";
      patches.add(
         new RicePatches.ClassEdits("net/minecraft/client/renderer/entity/RendererLivingEntity")
            .edit(
               "doRender",
               doRender,
               "Chams scope (RenderHooks#beginLiving/endLiving)",
               -1,
               method -> scope(
                     method,
                     list(new VarInsnNode(25, 1), hook("dev/rice/client/hook/RenderHooks", "beginLiving", "(Lnet/minecraft/entity/EntityLivingBase;)V")),
                     () -> list(hook("dev/rice/client/hook/RenderHooks", "endLiving", "()V"))
                  )
            )
            .edit("renderName", "(Lnet/minecraft/entity/EntityLivingBase;DDD)V", "Name Tags hides the vanilla name (cancellable head)", 1, method -> {
               InsnList entry = list(
                  new VarInsnNode(25, 1), hook("dev/rice/client/hook/RenderHooks", "suppressVanillaName", "(Lnet/minecraft/entity/Entity;)Z")
               );
               entry.add(returnIfTrue());
               method.instructions.insert(entry);
               return 1;
            })
      );
      patches.add(
         new RicePatches.ClassEdits("net/minecraft/client/renderer/entity/Render")
            .edit(
               "renderLivingLabel",
               "(Lnet/minecraft/entity/Entity;Ljava/lang/String;DDDI)V",
               "Name Tags hides the vanilla label (cancellable head)",
               1,
               method -> {
                  InsnList entry = list(
                     new VarInsnNode(25, 1), hook("dev/rice/client/hook/RenderHooks", "suppressVanillaName", "(Lnet/minecraft/entity/Entity;)Z")
                  );
                  entry.add(returnIfTrue());
                  method.instructions.insert(entry);
                  return 1;
               }
            )
      );
      patches.add(
         new RicePatches.ClassEdits("net/minecraft/client/multiplayer/ChunkProviderClient")
            .edit(
               "unloadChunk",
               "(II)V",
               "afterUnloadChunk before return",
               -1,
               method -> beforeReturns(
                     method,
                     () -> list(new VarInsnNode(21, 1), new VarInsnNode(21, 2), hook("dev/rice/client/hook/RenderHooks", "afterUnloadChunk", "(II)V"))
                  )
            )
      );
      String blockPos = "net/minecraft/util/BlockPos";
      String blockState = "net/minecraft/block/state/IBlockState";
      patches.add(
         new RicePatches.ClassEdits("net/minecraft/world/World")
            .edit(
               "setBlockState",
               "(L" + blockPos + ";L" + blockState + ";I)Z",
               "beforeSetBlock at head",
               1,
               method -> {
                  method.instructions
                     .insert(
                        list(
                           new VarInsnNode(25, 0),
                           new VarInsnNode(25, 1),
                           new VarInsnNode(25, 2),
                           hook("dev/rice/client/hook/RenderHooks", "beforeSetBlock", "(Lnet/minecraft/world/World;L" + blockPos + ";L" + blockState + ";)V")
                        )
                     );
                  return 1;
               }
            )
            .edit(
               "markBlockRangeForRenderUpdate",
               "(IIIIII)V",
               "afterInvalidate before return",
               -1,
               method -> beforeReturns(
                     method,
                     () -> list(
                           new VarInsnNode(25, 0),
                           new VarInsnNode(21, 1),
                           new VarInsnNode(21, 3),
                           hook("dev/rice/client/hook/RenderHooks", "afterInvalidate", "(Lnet/minecraft/world/World;II)V")
                        )
                  )
            )
            .edit(
               "updateEntities",
               "()V",
               "beforeEntities/afterEntities/abortEntities",
               -1,
               method -> {
                  LabelNode start = new LabelNode();
                  LabelNode end = new LabelNode();
                  LabelNode failure = new LabelNode();
                  int returns = beforeReturns(
                     method, () -> list(new VarInsnNode(25, 0), hook("dev/rice/client/hook/GameHooks", "afterEntities", "(Lnet/minecraft/world/World;)V"))
                  );
                  method.instructions
                     .insert(list(new VarInsnNode(25, 0), hook("dev/rice/client/hook/GameHooks", "beforeEntities", "(Lnet/minecraft/world/World;)V"), start));
                  method.instructions.add(end);
                  method.instructions.add(failure);
                  method.instructions
                     .add(
                        list(
                           new VarInsnNode(25, 0),
                           hook("dev/rice/client/hook/GameHooks", "abortEntities", "(Lnet/minecraft/world/World;)V"),
                           new InsnNode(191)
                        )
                     );
                  method.tryCatchBlocks.add(new TryCatchBlockNode(start, end, failure, null));
                  return returns;
               }
            )
            .edit(
               "spawnEntityInWorld",
               "(Lnet/minecraft/entity/Entity;)Z",
               "entityJoin at head (OpenB4 AntiFireball EntityJoinWorldEvent)",
               1,
               method -> {
                  method.instructions
                     .insert(
                        list(
                           new VarInsnNode(25, 0),
                           new VarInsnNode(25, 1),
                           hook("dev/rice/client/hook/GameHooks", "entityJoin", "(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;)V")
                        )
                     );
                  return 1;
               }
            )
      );
      patches.add(
         new RicePatches.ClassEdits("net/minecraft/client/renderer/EntityRenderer")
            .edit("updateRenderer", "()V", "Mayu MixinEntityRenderer updateRenderer HEAD/RETURN", -1, method -> {
               int returns = beforeReturns(method, () -> list(new VarInsnNode(ALOAD, 0), hook(MAYU, "afterUpdateRenderer", "(Lnet/minecraft/client/renderer/EntityRenderer;)V")));
               method.instructions.insert(list(new VarInsnNode(ALOAD, 0), hook(MAYU, "beforeUpdateRenderer", "(Lnet/minecraft/client/renderer/EntityRenderer;)V")));
               return returns;
            })
            .edit("updateCameraAndRender", "(FJ)V", "Mayu MixinEntityRenderer updateCameraAndRender HEAD/RETURN", -1, method -> {
               int returns = beforeReturns(method, () -> list(new VarInsnNode(FLOAD, 1), new VarInsnNode(LLOAD, 2), new VarInsnNode(ALOAD, 0),
                  hook(MAYU, "afterUpdateCameraAndRender", "(FJLnet/minecraft/client/renderer/EntityRenderer;)V")));
               method.instructions.insert(list(new VarInsnNode(FLOAD, 1), new VarInsnNode(LLOAD, 2), new VarInsnNode(ALOAD, 0),
                  hook(MAYU, "beforeUpdateCameraAndRender", "(FJLnet/minecraft/client/renderer/EntityRenderer;)V")));
               return returns;
            })
            .edit("renderWorldPass", "(IFJ)V", "clearWorld hook + Fog override after setupFog", 2, method -> {
               int count = 0;

               for (AbstractInsnNode insn : method.instructions.toArray()) {
                  if (isCall(insn, "net/minecraft/client/renderer/GlStateManager", "clear", "(I)V")) {
                     method.instructions.set(insn, hook("dev/rice/client/hook/RenderHooks", "clearWorld", "(I)V"));
                     count++;
                  } else if (isCall(insn, "net/minecraft/client/renderer/EntityRenderer", "setupFog", "(IF)V")) {
                     method.instructions.insert(insn, hook("dev/rice/client/hook/RenderHooks", "afterSetupFog", "()V"));
                  }
               }

               return count;
            })
            .edit("updateCameraAndRender", "(FJ)V", "preRender before Display.isActive, endRender scope", 1, method -> {
               int count = 0;

               for (AbstractInsnNode insn : method.instructions.toArray()) {
                  if (isExternalCall(insn, "org/lwjgl/opengl/Display", "isActive", "()Z")) {
                     method.instructions.insertBefore(insn, hook("dev/rice/client/hook/GameHooks", "preRender", "()V"));
                     count++;
                  }
               }

               if (count == 0) {
                  return 0;
               } else {
                  scope(method, null, () -> list(hook(GAME_HOOKS, "endRender", "()V")));
                  if (forge) {
                     // OpenJade forge HUD pass: RenderTickEvent END at updateCameraAndRender returns. Mayu's
                     // RenderOverlayEvent (GuiIngameForge renderTitle) shares this pass on Forge.
                     beforeReturns(method, () -> list(new VarInsnNode(FLOAD, 1), hook(GAME_HOOKS, "jadeRenderHud", "(F)V")));
                     beforeReturns(method, () -> list(new VarInsnNode(FLOAD, 1), hook(GAME_HOOKS, "hudRenderTitle", "(F)V")));
                  }

                  return count;
               }
            })
            .edit(
               "orientCamera",
               "(F)V",
               "beginCamera/endCamera scope",
               -1,
               method -> scope(
                     method,
                     list(hook("dev/rice/client/hook/GameHooks", "beginCamera", "()V")),
                     () -> list(hook("dev/rice/client/hook/GameHooks", "endCamera", "()V"))
                  )
            )
            .edit(
               "getMouseOver",
               "(F)V",
               "beginRay/customRay, entity reach, block reach, hitbox border",
               -1,
               method -> {
                  int count = 0;

                  for (AbstractInsnNode insn : method.instructions.toArray()) {
                     if (insn instanceof LdcInsnNode && Double.valueOf(3.0).equals(((LdcInsnNode)insn).cst)) {
                        method.instructions.insert(insn, hook("dev/rice/client/hook/GameHooks", "entityReach", "(D)D"));
                        count++;
                     } else if (isCall(insn, "net/minecraft/client/multiplayer/PlayerControllerMP", "getBlockReachDistance", "()F")) {
                        method.instructions.insert(insn, hook("dev/rice/client/hook/GameHooks", "queryReach", "(F)F"));
                        count++;
                     } else if (isCall(insn, "net/minecraft/entity/Entity", "getCollisionBorderSize", "()F")) {
                        method.instructions.insert(insn, hook("dev/rice/client/hook/GameHooks", "hitboxBorder", "(F)F"));
                        count++;
                     }
                  }

                  InsnList start = list(
                     new VarInsnNode(23, 1),
                     hook("dev/rice/client/hook/GameHooks", "beginRay", "(F)V"),
                     hook("dev/rice/client/hook/GameHooks", "customRay", "()Z")
                  );
                  start.add(returnIfTrue());
                  method.instructions.insert(start);
                  return count + 1;
               }
            )
            .edit("getMouseOver", "(F)V", "Raven bS mouse-over completion before returns", -1,
               method -> beforeReturns(method, () -> list(new VarInsnNode(FLOAD, 1), hook(GAME_HOOKS, "afterRay", "(F)V"))))
      );
      patches.add(
         new RicePatches.ClassEdits(ITEM_RENDERER)
            .edit("renderItemInFirstPerson", "(F)V", "1.7 blockhit animation (swing while blocking)", -1, method -> {
               int count = 0;
               for (AbstractInsnNode insn : method.instructions.toArray()) {
                  if (isCall(insn, ITEM_RENDERER, "doBlockTransformations", "()V")) {
                     AbstractInsnNode prev1 = prevReal(insn);
                     if (prev1 != null && prev1.getOpcode() == ALOAD) {
                        AbstractInsnNode prev2 = prevReal(prev1);
                        if (prev2 != null && isCall(prev2, ITEM_RENDERER, "transformFirstPersonItem", "(FF)V")) {
                           AbstractInsnNode prev3 = prevReal(prev2);
                           if (prev3 != null && (prev3.getOpcode() == FCONST_0 || prev3.getOpcode() == LDC)) {
                              AbstractInsnNode prev4 = prevReal(prev3);
                              if (prev4 != null && prev4.getOpcode() == FLOAD) {
                                 AbstractInsnNode prev5 = prevReal(prev4);
                                 if (prev5 != null && prev5.getOpcode() == ALOAD) {
                                    method.instructions.set(prev3, new VarInsnNode(FLOAD, 4));
                                    method.instructions.insertBefore(
                                       prev5,
                                       list(
                                          new VarInsnNode(ALOAD, 0),
                                          new VarInsnNode(FLOAD, 4),
                                          new MethodInsnNode(
                                             INVOKESPECIAL,
                                             cls(ITEM_RENDERER),
                                             Mappings.mapMethod(ITEM_RENDERER, "doItemUsedTransformations", "(F)V"),
                                             desc("(F)V"),
                                             false
                                          )
                                       )
                                    );
                                    count++;
                                 }
                              }
                           }
                        }
                     }
                  }
               }
               return count;
            })
      );
      patches.add(
         new RicePatches.ClassEdits("net/minecraft/client/renderer/ActiveRenderInfo")
            .edit(
               "updateRenderInfo",
               "(Lnet/minecraft/entity/player/EntityPlayer;Z)V",
               "beginRenderInfo/endRenderInfo scope",
               -1,
               method -> scope(
                     method,
                     list(hook("dev/rice/client/hook/GameHooks", "beginRenderInfo", "()V")),
                     () -> list(hook("dev/rice/client/hook/GameHooks", "endRenderInfo", "()V"))
                  )
            )
      );
      RicePatches.ClassEdits renderManager = new RicePatches.ClassEdits("net/minecraft/client/renderer/entity/RenderManager");
      renderManager.edit("renderEntityStatic", "(Lnet/minecraft/entity/Entity;FZ)Z",
         "Mayu third-person model rotation HEAD / restored render scope", -1, method -> scope(method,
            list(new VarInsnNode(ALOAD, 1), new VarInsnNode(FLOAD, 2), new VarInsnNode(ILOAD, 3), new VarInsnNode(ALOAD, 0),
               hook(MAYU, "beforeRenderEntityStatic", "(Lnet/minecraft/entity/Entity;FZLnet/minecraft/client/renderer/entity/RenderManager;)V")),
            () -> list(new VarInsnNode(ALOAD, 1), new VarInsnNode(FLOAD, 2), new VarInsnNode(ILOAD, 3), new VarInsnNode(ALOAD, 0),
               hook(MAYU, "afterRenderEntityStatic", "(Lnet/minecraft/entity/Entity;FZLnet/minecraft/client/renderer/entity/RenderManager;)V"))));
      // Lunar adds camera setters. Hook every real write and verify coverage of
      // both axes instead of requiring vanilla's fixed six instructions.
      renderManager.everyMethodExpected = -1;
      renderManager.everyMethodLabel = "playerViewX/playerViewY writes";
      renderManager.validation = node -> {
         boolean pitch = false, yaw = false;
         for (MethodNode method : node.methods) {
            for (AbstractInsnNode insn : method.instructions.toArray()) {
               if (!(insn instanceof MethodInsnNode)) continue;
               MethodInsnNode call = (MethodInsnNode)insn;
               if (call.owner.equals(GAME_HOOKS) && call.desc.equals("(F)F")) {
                  pitch |= call.name.equals("renderViewPitch");
                  yaw |= call.name.equals("renderViewYaw");
               }
            }
         }
         if (!pitch || !yaw) throw new IllegalStateException("Camera hooks must cover both pitch and yaw writes");
      };
      renderManager.everyMethod.add(method -> {
         int count = 0;
         String owner = cls("net/minecraft/client/renderer/entity/RenderManager");
         String pitch = Mappings.mapField("net/minecraft/client/renderer/entity/RenderManager", "playerViewX");
         String yaw = Mappings.mapField("net/minecraft/client/renderer/entity/RenderManager", "playerViewY");

         for (AbstractInsnNode insn : method.instructions.toArray()) {
            if (insn instanceof FieldInsnNode && insn.getOpcode() == 181) {
               FieldInsnNode field = (FieldInsnNode)insn;
               if (field.owner.equals(owner) && field.desc.equals("F")) {
                  String adapter = field.name.equals(pitch) ? "renderViewPitch" : (field.name.equals(yaw) ? "renderViewYaw" : null);
                  if (adapter != null) {
                     method.instructions.insertBefore(insn, hook("dev/rice/client/hook/GameHooks", adapter, "(F)F"));
                     count++;
                  }
               }
            }
         }

         return count;
      });
      patches.add(renderManager);
      String worldClient = "net/minecraft/client/multiplayer/WorldClient";
      RicePatches.ClassEdits minecraft = new RicePatches.ClassEdits("net/minecraft/client/Minecraft")
         .edit("updateDisplay", "()V", "beforeDisplay before Display.update", 1, method -> {
            int count = 0;

            for (AbstractInsnNode insn : method.instructions.toArray()) {
               if (isExternalCall(insn, "org/lwjgl/opengl/Display", "update", "()V")) {
                  method.instructions.insertBefore(insn, hook("dev/rice/client/hook/GameHooks", "beforeDisplay", "()V"));
                  count++;
               }
            }

            return count;
         })
         .edit("shutdownMinecraftApplet", "()V", "shutdown at head", 1, method -> {
            method.instructions.insert(hook("dev/rice/client/hook/GameHooks", "shutdown", "()V"));
            return 1;
         })
         .edit("rightClickMouse", "()V", "beforeInteraction(true)/afterInteraction(true)", -1, method -> interaction(method, true))
         .edit("clickMouse", "()V", "beforeInteraction(false)/afterInteraction(false)", -1, method -> interaction(method, false))
         // Mayu MixinMinecraft cancellable HEAD injectors; applied after Rice's interaction scope, so they run first
         // and a Mayu cancel returns before Rice's beforeInteraction (and therefore without its afterInteraction).
         .edit("rightClickMouse", "()V", "Mayu beforeRightClick (cancellable head)", 1, method -> {
            InsnList entry = list(hook(MAYU, "beforeRightClick", "()Z"));
            entry.add(returnIfTrue());
            method.instructions.insert(entry);
            return 1;
         })
         .edit("clickMouse", "()V", "Mayu beforeLeftClick (cancellable head)", 1, method -> {
            InsnList entry = list(new VarInsnNode(ALOAD, 0), hook(MAYU, "beforeLeftClick", "(Lnet/minecraft/client/Minecraft;)Z"));
            entry.add(returnIfTrue());
            method.instructions.insert(entry);
            return 1;
         })
         .edit("sendClickBlockToController", "(Z)V", "Mayu beforeBlockClick (cancellable head)", 1, method -> {
            InsnList entry = list(new VarInsnNode(ALOAD, 0), hook(MAYU, "beforeBlockClick", "(Lnet/minecraft/client/Minecraft;)Z"));
            entry.add(returnIfTrue());
            method.instructions.insert(entry);
            return 1;
         })
         .edit("clickMouse", "()V", "GUI click isolation at head", 1, method -> {
            InsnList entry = list(hook(GAME_HOOKS, "guiBlocksClicks", "()Z"));
            entry.add(returnIfTrue()); method.instructions.insert(entry); return 1;
         })
         .edit("rightClickMouse", "()V", "GUI click isolation at head", 1, method -> {
            InsnList entry = list(hook(GAME_HOOKS, "guiBlocksClicks", "()Z"));
            entry.add(returnIfTrue()); method.instructions.insert(entry); return 1;
         })
         .edit("sendClickBlockToController", "(Z)V", "GUI click isolation at head", 1, method -> {
            InsnList entry = list(hook(GAME_HOOKS, "guiBlocksClicks", "()Z"));
            entry.add(returnIfTrue()); method.instructions.insert(entry); return 1;
         })
         .edit("runGameLoop", "()V", "OpenJade RenderTickEvent START at game-loop head", 1, method -> {
            method.instructions.insert(list(new VarInsnNode(ALOAD, 0), hook(GAME_HOOKS, "jadeGameLoopStart", "(Lnet/minecraft/client/Minecraft;)V")));
            return 1;
         })
         .edit("runTick", "()V", "ClientTickEvent START/END", -1, method -> {
            int returns = beforeReturns(method, () -> list(new InsnNode(3), hook("dev/rice/client/ClientEventBridge", "clientTick", "(Z)V")));
            method.instructions.insert(list(new InsnNode(4), hook("dev/rice/client/ClientEventBridge", "clientTick", "(Z)V")));
            return returns;
         })
         .edit("runTick", "()V", "Raven bS pre-player-interact before queued keys", 1, method -> {
            // Match bS MixinMinecraft's GETFIELD chatVisibility injection.
            for (AbstractInsnNode insn : method.instructions.toArray()) {
               if (insn instanceof FieldInsnNode && insn.getOpcode() == GETFIELD
                  && ((FieldInsnNode)insn).owner.equals(cls("net/minecraft/client/settings/GameSettings"))
                  && ((FieldInsnNode)insn).name.equals(Mappings.mapField("net/minecraft/client/settings/GameSettings", "chatVisibility"))) {
                  method.instructions.insertBefore(insn, hook(GAME_HOOKS, "prePlayerInteract", "()V"));
                  return 1;
               }
            }
            return 0;
         })
         .edit("runTick", "()V", "Raven bS rotation update before mouse selection", 1, method -> {
            int count = 0;
            for (AbstractInsnNode insn : method.instructions.toArray()) {
               if (isCall(insn, ENTITY_RENDERER, "getMouseOver", "(F)V")) {
                  method.instructions.insertBefore(insn, hook(GAME_HOOKS, "beforeTickMouseOver", "()V"));
                  count++;
               }
            }
            return count;
         })
         .edit("runTick", "()V", "Mayu beforeRunTick HEAD / afterRunTick RETURN (PreTickEvent/PostTickEvent)", -1, method -> {
            int returns = beforeReturns(method, () -> list(new VarInsnNode(ALOAD, 0), hook(MAYU, "afterRunTick", "(Lnet/minecraft/client/Minecraft;)V")));
            // Rice rotation arbitration first (Mayu claims the tick while Scaffold/BedNuker run), then Mayu's PreTickEvent.
            method.instructions.insert(list(hook(GAME_HOOKS, "mayuTickHead", "()V"), new VarInsnNode(ALOAD, 0),
               hook(MAYU, "beforeRunTick", "(Lnet/minecraft/client/Minecraft;)V")));
            return returns;
         })
         .edit("runGameLoop", "()V", "Mayu afterRunGameLoop before returns (ModuleManager.update)", -1,
            method -> beforeReturns(method, () -> list(hook(MAYU, "afterRunGameLoop", "()V"))))
         .edit("runGameLoop", "()V", "RenderTickEvent START/END around updateCameraAndRender", 1, method -> {
            int count = 0;

            for (AbstractInsnNode insn : method.instructions.toArray()) {
               if (isCall(insn, "net/minecraft/client/renderer/EntityRenderer", "updateCameraAndRender", "(FJ)V")) {
                  method.instructions.insertBefore(insn, list(new InsnNode(4), hook("dev/rice/client/ClientEventBridge", "renderTick", "(Z)V")));
                  method.instructions.insert(insn, list(new InsnNode(3), hook("dev/rice/client/ClientEventBridge", "renderTick", "(Z)V")));
                  count++;
               }
            }

            return count;
         })
         .edit(
            "loadWorld",
            "(L" + worldClient + ";Ljava/lang/String;)V",
            "WorldEvent.Unload at head",
            1,
            method -> {
               method.instructions
                  .insert(list(new VarInsnNode(25, 0), hook("dev/rice/client/ClientEventBridge", "loadWorld", "(Lnet/minecraft/client/Minecraft;)V")));
               return 1;
            }
         )
         .edit("loadWorld", "(L" + worldClient + ";Ljava/lang/String;)V", "Mayu beforeLoadWorld HEAD (WorldLoadEvent)", 1, method -> {
            method.instructions.insert(list(new VarInsnNode(ALOAD, 1), new VarInsnNode(ALOAD, 2),
               hook(MAYU, "beforeLoadWorld", "(L" + worldClient + ";Ljava/lang/String;)V")));
            return 1;
         });
      minecraft.everyMethodExpected = 3;
      minecraft.everyMethodLabel = "Keyboard.next/Mouse.next event-loop redirects + Mayu changeCurrentItem redirect (runTick, or OptiFine runInputTick)";
      minecraft.everyMethod
         .add(method -> redirectInputLoop(method, "org/lwjgl/input/Keyboard", "next", "()Z", "dev/rice/client/ClientEventBridge", "keyboardNext"));
      minecraft.everyMethod
         .add(method -> redirectInputLoop(method, "org/lwjgl/input/Mouse", "next", "()Z", "dev/rice/client/ClientEventBridge", "mouseNext"));
      // Mayu MixinMinecraft.redirectHotbarScroll: the wheel's InventoryPlayer.changeCurrentItem(I)V call.
      minecraft.everyMethod.add(method -> {
         int count = 0;
         for (AbstractInsnNode insn : method.instructions.toArray()) {
            if (isCall(insn, "net/minecraft/entity/player/InventoryPlayer", "changeCurrentItem", "(I)V")) {
               method.instructions.set(insn, hook(MAYU, "redirectHotbarScroll", "(Lnet/minecraft/entity/player/InventoryPlayer;I)V"));
               count++;
            }
         }
         return count;
      });
      patches.add(minecraft);
      patches.add(
         new RicePatches.ClassEdits("net/minecraft/client/multiplayer/PlayerControllerMP")
            .edit("attackEntity", "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/entity/Entity;)V",
               "Raven bS entity attack at head (cancellable)", 1, method -> {
                  InsnList entry = list(new VarInsnNode(ALOAD, 1), new VarInsnNode(ALOAD, 2),
                     hook(GAME_HOOKS, "beforeEntityAttack", "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/entity/Entity;)Z"));
                  entry.add(returnIfTrue());
                  method.instructions.insert(entry);
                  return 1;
               })
            .edit(
               "updateController",
               "()V",
               "updateController before return",
               -1,
               method -> beforeReturns(method, () -> list(hook("dev/rice/client/hook/GameHooks", "updateController", "()V")))
            )
      );
      String packet = "net/minecraft/network/Packet";
      patches.add(
         new RicePatches.ClassEdits("net/minecraft/network/NetworkManager")
            .edit(
               "sendPacket",
               "(L" + packet + ";)V",
               "beforeSend at head (cancellable)",
               1,
               method -> {
                  InsnList entry = list(
                     new VarInsnNode(25, 0),
                     new VarInsnNode(25, 1),
                     hook("dev/rice/client/hook/GameHooks", "beforeSend", "(Lnet/minecraft/network/NetworkManager;L" + packet + ";)Z")
                  );
                  entry.add(returnIfTrue());
                  method.instructions.insert(entry);
                  return 1;
               }
            )
            .edit(
               "channelRead0",
               "(Lio/netty/channel/ChannelHandlerContext;L" + packet + ";)V",
               "OpenB4/Mayu/OpenJade inbound packet hook at head (cancellable)",
               1,
               method -> {
                  InsnList entry = list(new VarInsnNode(ALOAD, 1), new VarInsnNode(ALOAD, 2),
                     hook(GAME_HOOKS, "receivePacket", "(Lio/netty/channel/ChannelHandlerContext;L" + packet + ";)Z"));
                  entry.add(returnIfTrue());
                  method.instructions.insert(entry);
                  return 1;
               }
            )
            .edit(
               "sendPacket",
               "(L" + packet + ";Lio/netty/util/concurrent/GenericFutureListener;[Lio/netty/util/concurrent/GenericFutureListener;)V",
               "Mayu onSendPacketWithListeners at head (cancellable)",
               1,
               method -> {
                  InsnList entry = list(new VarInsnNode(ALOAD, 1), new VarInsnNode(ALOAD, 2), new VarInsnNode(ALOAD, 3),
                     hook(GAME_HOOKS, "mayuSendWithListeners",
                        "(L" + packet + ";Lio/netty/util/concurrent/GenericFutureListener;[Lio/netty/util/concurrent/GenericFutureListener;)Z"));
                  entry.add(returnIfTrue());
                  method.instructions.insert(entry);
                  return 1;
               }
            )
      );
      patches.add(
         new RicePatches.ClassEdits("net/minecraft/entity/Entity")
            .edit(
               "setAngles",
               "(FF)V",
               "mouseAngles at head (cancellable)",
               1,
               method -> {
                  InsnList entry = list(
                     new VarInsnNode(25, 0),
                     new VarInsnNode(23, 1),
                     new VarInsnNode(23, 2),
                     hook("dev/rice/client/hook/GameHooks", "mouseAngles", "(Lnet/minecraft/entity/Entity;FF)Z")
                  );
                  entry.add(returnIfTrue());
                  method.instructions.insert(entry);
                  return 1;
               }
            )
            .edit(
               "moveFlying",
               "(FFF)V",
               "beforeMoveFlying/afterMoveFlying",
               -1,
               method -> {
                  int local = method.maxLocals++;
                  InsnList entry = list(
                     new VarInsnNode(25, 0),
                     new VarInsnNode(23, 1),
                     new VarInsnNode(23, 2),
                     new VarInsnNode(23, 3),
                     hook("dev/rice/client/hook/GameHooks", "beforeMoveFlying", "(Lnet/minecraft/entity/Entity;FFF)Ldev/rice/client/SemanticEvent;"),
                     new VarInsnNode(58, local)
                  );
                  String[] fields = new String[]{"sideways", "forward", "factor"};

                  for (int i = 1; i <= 3; i++) {
                     entry.add(new VarInsnNode(25, local));
                     entry.add(new FieldInsnNode(180, "dev/rice/client/SemanticEvent", fields[i - 1], "F"));
                     entry.add(new VarInsnNode(56, i));
                  }

                  int returns = beforeReturns(
                     method,
                     () -> list(
                           new VarInsnNode(25, 0),
                           new VarInsnNode(25, local),
                           hook("dev/rice/client/hook/GameHooks", "afterMoveFlying", "(Lnet/minecraft/entity/Entity;Ldev/rice/client/SemanticEvent;)V")
                        )
                  );
                  method.instructions.insert(entry);
                  return returns;
               }
            )


























            .edit("moveEntity", "(DDD)V", "isSneaking -> GameHooks#safeWalk (first call)", 1, method -> {
               for (AbstractInsnNode insn : method.instructions.toArray()) {
                  if (isCall(insn, "net/minecraft/entity/Entity", "isSneaking", "()Z")) {
                     method.instructions.set(insn, hook("dev/rice/client/hook/GameHooks", "safeWalk", "(Lnet/minecraft/entity/Entity;)Z"));
                     return 1;
                  }
               }

               return 0;
            })
            .edit("moveEntity", "(DDD)V", "Mayu modifySafeWalkFlag at the STORE of flag (first boolean local, after the sneak test)", 1, method -> {
               boolean sneakSeen = false;
               for (AbstractInsnNode insn : method.instructions.toArray()) {
                  if (isCall(insn, "net/minecraft/entity/Entity", "isSneaking", "()Z")
                     || isExternalCall(insn, "dev/rice/client/hook/GameHooks", "safeWalk", desc("(Lnet/minecraft/entity/Entity;)Z"))) {
                     sneakSeen = true;
                  } else if (insn.getOpcode() == ISTORE) {
                     if (!sneakSeen) {
                        return 0;
                     }

                     method.instructions.insertBefore(insn, list(new VarInsnNode(ALOAD, 0),
                        hook(MAYU, "modifySafeWalkFlag", "(ZLnet/minecraft/entity/Entity;)Z")));
                     return 1;
                  }
               }

               return 0;
            })
            .edit("setVelocity", "(DDD)V", "Mayu beforeSetVelocity at head (cancellable)", 1, method -> {
               InsnList entry = list(new VarInsnNode(DLOAD, 1), new VarInsnNode(DLOAD, 3), new VarInsnNode(DLOAD, 5), new VarInsnNode(ALOAD, 0),
                  hook(MAYU, "beforeSetVelocity", "(DDDLnet/minecraft/entity/Entity;)Z"));
               entry.add(returnIfTrue());
               method.instructions.insert(entry);
               return 1;
            })
      );
      patches.add(
         new RicePatches.ClassEdits("net/minecraft/client/entity/EntityPlayerSP")
            .edit("onUpdate", "()V", "Mayu MixinEntityPlayerSP: HEAD, isRiding redirect, before onUpdateWalkingPlayer, RETURN", 1, method -> {
               int riding = 0;
               int walking = 0;
               for (AbstractInsnNode insn : method.instructions.toArray()) {
                  if (isCall(insn, PLAYER, "isRiding", "()Z")) {
                     method.instructions.insertBefore(insn, new InsnNode(DUP));
                     method.instructions.set(insn, hook(MAYU, "applySpoofedRotationBeforeRidingCheck",
                        "(Lnet/minecraft/client/entity/EntityPlayerSP;Lnet/minecraft/client/entity/EntityPlayerSP;)Z"));
                     riding++;
                  } else if (isCall(insn, PLAYER, "onUpdateWalkingPlayer", "()V")) {
                     method.instructions.insertBefore(prevReal(insn), hook(MAYU, "beforeWalkingPlayerUpdate", "()V"));
                     walking++;
                  }
               }
               if (riding != 1 || walking != 1) throw new IllegalStateException("Expected one onUpdate riding check and one walking update");
               beforeReturns(method, () -> list(new VarInsnNode(ALOAD, 0), hook(MAYU, "afterPlayerUpdate", "(Lnet/minecraft/client/entity/EntityPlayerSP;)V")));
               method.instructions.insert(list(new VarInsnNode(ALOAD, 0), hook(GAME_HOOKS, "mayuPlayerUpdateHead", "(Lnet/minecraft/client/entity/EntityPlayerSP;)V")));
               return 1;
            })
            .edit("onUpdateWalkingPlayer", "()V", "OpenB4 Hooks.preMotion at head; returns early when the OpenB4 packet pipeline sent the packets", 1, method -> {
               InsnList head = list(new VarInsnNode(ALOAD, 0), hook(GAME_HOOKS, "walkingPlayerHead", "(Lnet/minecraft/client/entity/EntityPlayerSP;)Z"));
               head.add(returnIfTrue());
               method.instructions.insert(head);
               return 1;
            })
            .edit("onUpdateWalkingPlayer", "()V", "Snowify Block In silent packet construction and last-reported rotations", -1, method -> {
               int yaw = 0, pitch = 0;
               for (AbstractInsnNode insn : method.instructions.toArray()) {
                  if (!(insn instanceof FieldInsnNode) || insn.getOpcode() != GETFIELD) continue;
                  FieldInsnNode field = (FieldInsnNode)insn;
                  if (!field.desc.equals("F")) continue;
                  String hookName;
                  if (field.name.equals(Mappings.mapField(ENTITY, "rotationYaw"))) { hookName="spoofYaw"; yaw++; }
                  else if (field.name.equals(Mappings.mapField(ENTITY, "rotationPitch"))) { hookName="spoofPitch"; pitch++; }
                  else continue;
                  method.instructions.set(insn, hook("dev/rice/client/blockin/SuppliedBlockInHost", hookName, "(Lnet/minecraft/entity/Entity;)F"));
               }
               // Lunar may delegate packet construction, so direct field reads can be absent or partial.
               // A scoped pose exposes the same values to delegates and restores the real view even on throw.
               if (yaw == 0 || pitch == 0) return scope(method,
                  list(new VarInsnNode(ALOAD, 0), hook("dev/rice/client/blockin/SuppliedBlockInHost", "beginWalking", "(Lnet/minecraft/entity/Entity;)V")),
                  () -> list(new VarInsnNode(ALOAD, 0), hook("dev/rice/client/blockin/SuppliedBlockInHost", "endWalking", "(Lnet/minecraft/entity/Entity;)V")));
               return yaw + pitch;
            })
            .edit("onLivingUpdate", "()V", "Mayu beforeLivingUpdate before superclass living update", 1, method -> {
               int count = 0;
               for (AbstractInsnNode insn : method.instructions.toArray()) {
                  if (isCall(insn, "net/minecraft/client/entity/AbstractClientPlayer", "onLivingUpdate", "()V")) {
                     method.instructions.insertBefore(prevReal(insn), list(hook(MAYU, "beforeLivingUpdate", "()V")));
                     count++;
                  }
               }
               return count;
            })
            .edit("onLivingUpdate", "()V", "Mayu afterMovementInputUpdate, then Rice movement input, after vanilla refresh", 1, method -> {
               int count = 0;
               for (AbstractInsnNode insn : method.instructions.toArray()) {
                  if (isCall(insn, "net/minecraft/util/MovementInput", "updatePlayerMoveState", "()V")) {
                     method.instructions.insert(insn, list(hook(MAYU, "afterMovementInputUpdate", "()V"), new VarInsnNode(ALOAD, 0),
                        hook(GAME_HOOKS, "afterMovementInput", "(Lnet/minecraft/client/entity/EntityPlayerSP;)V")));
                     count++;
                  }
               }
               return count;
            })
            .edit("swingItem", "()V", "return after super.swingItem() for GameHooks#baseSwing", 1, method -> {
               String name = Mappings.mapMethod("net/minecraft/client/entity/EntityPlayerSP", "swingItem", "()V");
               int count = 0;

               for (AbstractInsnNode insn : method.instructions.toArray()) {
                  if (insn.getOpcode() == 183 && ((MethodInsnNode)insn).name.equals(name) && ((MethodInsnNode)insn).desc.equals("()V")) {
                     InsnList after = list(hook("dev/rice/client/hook/GameHooks", "baseSwingOnly", "()Z"));
                     after.add(returnIfTrue());
                     method.instructions.insert(insn, after);
                     count++;
                  }
               }

               return count;
            })
            .edit("sendChatMessage", "(Ljava/lang/String;)V", "client command at head (cancellable)", 1, method -> {
               InsnList entry = list(new VarInsnNode(25, 1), hook("dev/rice/client/ClientEventBridge", "sendChatMessage", "(Ljava/lang/String;)Z"));
               entry.add(returnIfTrue());
               method.instructions.insert(entry);
               return 1;
            })
      );
      patches.add(
         new RicePatches.ClassEdits(LIVING)
            .edit("jump", "()V", "Mayu modifyJumpYawRadians at the STORE of the sprint-jump yaw (first float local)", 1, method -> {
               for (AbstractInsnNode insn : method.instructions.toArray()) {
                  if (insn.getOpcode() == FSTORE) {
                     method.instructions.insertBefore(insn, list(new VarInsnNode(ALOAD, 0),
                        hook(MAYU, "modifyJumpYawRadians", "(FLnet/minecraft/entity/EntityLivingBase;)F")));
                     return 1;
                  }
               }

               return 0;
            })
            .edit("moveEntityWithHeading", "(FF)V", "Mayu redirectMoveFlying at each moveFlying call (water, lava, ground/air)", 3, method -> {
               int count = 0;
               for (AbstractInsnNode insn : method.instructions.toArray()) {
                  if (isCall(insn, LIVING, "moveFlying", "(FFF)V")) {
                     method.instructions.insertBefore(insn, new VarInsnNode(ALOAD, 0));
                     method.instructions.set(insn, hook(MAYU, "redirectMoveFlying",
                        "(Lnet/minecraft/entity/EntityLivingBase;FFFLnet/minecraft/entity/EntityLivingBase;)V"));
                     count++;
                  }
               }

               return count;
            })
      );
      // Mayu MixinKeyBinding.onIsPressed (RETURN, returnable): number-key HotbarChangeEvent.
      patches.add(
         new RicePatches.ClassEdits("net/minecraft/client/settings/KeyBinding")
            .edit("isPressed", "()Z", "Mayu onIsPressed before returns (returnable)", -1,
               method -> beforeReturns(method, () -> list(new VarInsnNode(ALOAD, 0), hook(MAYU, "onIsPressed", "(ZLnet/minecraft/client/settings/KeyBinding;)Z"))))
      );
      // GuiIngame.updateTick also feeds the held-item name. Forge inherits this method.
      RicePatches.ClassEdits guiIngame = new RicePatches.ClassEdits("net/minecraft/client/gui/GuiIngame")
         .edit("updateTick", "()V", "Mayu MixinGuiIngame.redirectCurrentHotbarItem (getCurrentItem redirect)", 1, method -> {
            int count = 0;
            for (AbstractInsnNode insn : method.instructions.toArray()) {
               if (isCall(insn, "net/minecraft/entity/player/InventoryPlayer", "getCurrentItem", "()Lnet/minecraft/item/ItemStack;")) {
                  method.instructions.set(insn, hook(MAYU, "redirectCurrentHotbarItem", "(Lnet/minecraft/entity/player/InventoryPlayer;)Lnet/minecraft/item/ItemStack;")); count++;
               }
            }
            return count;
         });
      if (!forge) {
         guiIngame.edit("renderGameOverlay", "(F)V", "OpenJade RenderTickEvent END (HUD pass) before returns", -1,
            method -> beforeReturns(method, () -> list(new VarInsnNode(FLOAD, 1), hook(GAME_HOOKS, "jadeRenderHud", "(F)V"))));
         // Mayu MixinGuiIngameForge.afterRenderTitle: Forge renders the title, then the scoreboard. Vanilla inlines the
         // title block, so the same point is the start of the scoreboard statement (this.mc.theWorld.getScoreboard()).
         guiIngame.edit("renderGameOverlay", "(F)V", "Mayu afterRenderTitle before the scoreboard statement", 1, method -> {
            for (AbstractInsnNode insn : method.instructions.toArray()) {
               if (isCall(insn, "net/minecraft/client/multiplayer/WorldClient", "getScoreboard", "()Lnet/minecraft/scoreboard/Scoreboard;")
                  || isCall(insn, "net/minecraft/world/World", "getScoreboard", "()Lnet/minecraft/scoreboard/Scoreboard;")) {
                  AbstractInsnNode world = prevReal(insn);
                  AbstractInsnNode mc = world == null ? null : prevReal(world);
                  AbstractInsnNode self = mc == null ? null : prevReal(mc);
                  if (world == null || world.getOpcode() != GETFIELD || mc.getOpcode() != GETFIELD || self.getOpcode() != ALOAD || ((VarInsnNode)self).var != 0) {
                     return 0;
                  }

                  method.instructions.insertBefore(self, list(new VarInsnNode(FLOAD, 1), hook(GAME_HOOKS, "hudRenderTitle", "(F)V")));
                  return 1;
               }
            }

            return 0;
         });
      }
      patches.add(guiIngame);

      String slot = "net/minecraft/inventory/Slot";
      String renderItem = "net/minecraft/client/renderer/entity/RenderItem";
      String itemStack = "net/minecraft/item/ItemStack";
      patches.add(
         new RicePatches.ClassEdits("net/minecraft/client/gui/inventory/GuiContainer")
            .edit("drawSlot", "(L" + slot + ";)V", "OpenJade Opsec slot-draw capture + displayed-stack substitution", -1, method -> {
               int count = 0;
               int stackLocal = method.maxLocals++;
               int xLocal = method.maxLocals++;
               int yLocal = method.maxLocals++;
               int textLocal = method.maxLocals++;
               for (AbstractInsnNode insn : method.instructions.toArray()) {
                  if (isCall(insn, renderItem, "renderItemAndEffectIntoGUI", "(L" + itemStack + ";II)V")) {
                     method.instructions.insertBefore(insn, list(
                        new VarInsnNode(ISTORE, yLocal), new VarInsnNode(ISTORE, xLocal), new VarInsnNode(ASTORE, stackLocal),
                        new VarInsnNode(ALOAD, 0), new VarInsnNode(ALOAD, stackLocal),
                        hook(GAME_HOOKS, "jadeContainerDisplayedStack", "(Ljava/lang/Object;L" + itemStack + ";)L" + itemStack + ";"),
                        new VarInsnNode(ILOAD, xLocal), new VarInsnNode(ILOAD, yLocal)));
                     count++;
                  } else if (isCall(insn, renderItem, "renderItemOverlayIntoGUI", "(Lnet/minecraft/client/gui/FontRenderer;L" + itemStack + ";IILjava/lang/String;)V")) {
                     method.instructions.insertBefore(insn, list(
                        new VarInsnNode(ASTORE, textLocal), new VarInsnNode(ISTORE, yLocal), new VarInsnNode(ISTORE, xLocal), new VarInsnNode(ASTORE, stackLocal),
                        new VarInsnNode(ALOAD, 0), new VarInsnNode(ALOAD, stackLocal),
                        hook(GAME_HOOKS, "jadeContainerDisplayedStack", "(Ljava/lang/Object;L" + itemStack + ";)L" + itemStack + ";"),
                        new VarInsnNode(ILOAD, xLocal), new VarInsnNode(ILOAD, yLocal), new VarInsnNode(ALOAD, textLocal)));
                     count++;
                  }
               }

               beforeReturns(method, () -> list(new VarInsnNode(ALOAD, 0), hook(GAME_HOOKS, "jadeContainerDrawSlotEnd", "(Ljava/lang/Object;)V")));
               method.instructions.insert(list(new VarInsnNode(ALOAD, 0), new VarInsnNode(ALOAD, 1),
                  hook(GAME_HOOKS, "jadeContainerDrawSlotBegin", "(Ljava/lang/Object;Ljava/lang/Object;)V")));
               return count;
            })
            .edit("drawScreen", "(IIF)V", "OpenJade Opsec setup text before returns", -1,
               method -> beforeReturns(method, () -> list(new VarInsnNode(ALOAD, 0), new VarInsnNode(ILOAD, 1), new VarInsnNode(ILOAD, 2),
                  hook(GAME_HOOKS, "jadeContainerDrawScreen", "(Ljava/lang/Object;II)V"))))
            .edit("keyTyped", "(CI)V", "OpenJade manual-input block (cancellable head)", 1, method -> containerManualInput(method))
            .edit("mouseClicked", "(III)V", "OpenJade manual-input block (cancellable head)", 1, method -> containerManualInput(method))
            .edit("mouseClickMove", "(IIIJ)V", "OpenJade manual-input block (cancellable head)", 1, method -> containerManualInput(method))
            .edit("mouseReleased", "(III)V", "OpenJade manual-input block (cancellable head)", 1, method -> containerManualInput(method))
            .edit("handleMouseClick", "(L" + slot + ";III)V", "OpenJade Opsec click redirect (cancellable head)", 1, method -> {
               InsnList entry = list(new VarInsnNode(ALOAD, 0), new VarInsnNode(ALOAD, 1), new VarInsnNode(ILOAD, 2), new VarInsnNode(ILOAD, 3), new VarInsnNode(ILOAD, 4),
                  hook(GAME_HOOKS, "jadeContainerClick", "(Ljava/lang/Object;Ljava/lang/Object;III)Z"));
               entry.add(returnIfTrue());
               method.instructions.insert(entry);
               return 1;
            })
      );
      // Forge GUI input is handled through GuiScreenEvent.Pre. Preserve the
      // original GuiScreen call sites for Lunar's own input/scroll mixins.
      if (!forge) patches.add(
         new RicePatches.ClassEdits("net/minecraft/client/gui/GuiScreen")
            .edit(
               "handleInput",
               "()V",
               "handleKeyboardInput -> ClientEventBridge#handleKeyboardInput",
               1,
               method -> redirectSelfCall(method, "handleKeyboardInput")
            )
            .edit("handleInput", "()V", "handleMouseInput -> ClientEventBridge#handleMouseInput", 1, method -> redirectSelfCall(method, "handleMouseInput"))
      );
      patches.add(
         new RicePatches.ClassEdits("net/minecraft/client/network/NetHandlerPlayClient")
            .edit("onDisconnect", "(Lnet/minecraft/util/IChatComponent;)V", "disconnect at head", 1, method -> {
               method.instructions.insert(hook("dev/rice/client/ClientEventBridge", "disconnect", "()V"));
               return 1;
            })
      );
      patches.add(new ClassEdits("net/minecraft/entity/player/EntityPlayer")
         .edit("getDisplayName", "()Lnet/minecraft/util/IChatComponent;", "Snowify anticheat nametag marker before return", -1,
            method -> beforeReturns(method, () -> list(new VarInsnNode(ALOAD,0), hook("dev/rice/client/anticheat/SnowifyAntiCheatHost","displayName","(Lnet/minecraft/util/IChatComponent;Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/util/IChatComponent;")))));
      patches.add(new ClassEdits("net/minecraft/client/gui/GuiPlayerTabOverlay")
         .edit("getPlayerName", "(Lnet/minecraft/client/network/NetworkPlayerInfo;)Ljava/lang/String;", "Snowify anticheat tab marker before return", -1,
            method -> beforeReturns(method, () -> list(new VarInsnNode(ALOAD,1), hook("dev/rice/client/anticheat/SnowifyAntiCheatHost","playerName","(Ljava/lang/String;Lnet/minecraft/client/network/NetworkPlayerInfo;)Ljava/lang/String;")))));
      return patches;
   }

   private static int containerManualInput(MethodNode method) {
      InsnList entry = list(new VarInsnNode(ALOAD, 0), hook(GAME_HOOKS, "jadeContainerManualInput", "(Ljava/lang/Object;)Z"));
      entry.add(returnIfTrue());
      method.instructions.insert(entry);
      return 1;
   }

   private static int interaction(MethodNode method, boolean use) {
      int returns = beforeReturns(method, () -> list(new InsnNode(use ? 4 : 3), hook("dev/rice/client/hook/GameHooks", "afterInteraction", "(Z)V")));
      InsnList start = list(new InsnNode(use ? 4 : 3), hook("dev/rice/client/hook/GameHooks", "beforeInteraction", "(Z)Z"));
      start.add(returnIfTrue());
      method.instructions.insert(start);
      return returns;
   }

   private static int redirectSelfCall(MethodNode method, String name) {
      int count = 0;

      for (AbstractInsnNode insn : method.instructions.toArray()) {
         if (isCall(insn, "net/minecraft/client/gui/GuiScreen", name, "()V")) {
            method.instructions.set(insn, hook("dev/rice/client/ClientEventBridge", name, "(Lnet/minecraft/client/gui/GuiScreen;)V"));
            count++;
         }
      }

      return count;
   }

   public static final class ClassEdits {
      public final String target;
      public final List<RicePatches.MethodEdit> edits = new ArrayList<>();
      final List<RicePatches.Edit> everyMethod = new ArrayList<>();
      int everyMethodExpected;
      String everyMethodLabel;
      ClassCheck validation;

      ClassEdits(String target) {
         this.target = target;
      }

      public String getRuntimeTarget() {
         return Mappings.mapClass(this.target);
      }

      public void validate(ClassNode node) {
         if (this.validation != null) this.validation.verify(node);
      }

      RicePatches.ClassEdits edit(String name, String desc, String label, int expected, RicePatches.Edit edit) {
         this.edits.add(new RicePatches.MethodEdit(name, desc, label, expected, edit));
         return this;
      }

      public MethodNode find(ClassNode node, RicePatches.MethodEdit edit) {
         String name = Mappings.mapMethod(this.target, edit.name, edit.desc);
         String desc = Mappings.mapDescriptor(edit.desc);

         for (MethodNode method : node.methods) {
            if (method.name.equals(name) && method.desc.equals(desc)) {
               return method;
            }
         }

         return null;
      }

      public int applyEveryMethod(ClassNode node) {
         int points = 0;

         for (RicePatches.Edit edit : this.everyMethod) {
            for (MethodNode method : node.methods) {
               points += edit.apply(method);
            }
         }

         return points;
      }

      public int getEveryMethodExpected() {
         return this.everyMethodExpected;
      }

      public String getEveryMethodLabel() {
         return this.everyMethodLabel;
      }

      public boolean hasEveryMethodEdits() {
         return !this.everyMethod.isEmpty();
      }
   }

   public interface Edit {
      int apply(MethodNode var1);
   }

   public interface ClassCheck {
      void verify(ClassNode node);
   }

   interface InsnFactory {
      InsnList create();
   }

   public static final class MethodEdit {
      public final String name;
      public final String desc;
      public final String label;
      public final int expected;
      final RicePatches.Edit edit;

      MethodEdit(String name, String desc, String label, int expected, RicePatches.Edit edit) {
         this.name = name;
         this.desc = desc;
         this.label = label;
         this.expected = expected;
         this.edit = edit;
      }

      public int apply(MethodNode method) {
         return this.edit.apply(method);
      }

      public String describe() {
         return this.name + this.desc + " " + this.label;
      }
   }
}
