package dev.rice.module.impl;

import dev.rice.client.ClientContext;
import dev.rice.client.RelationshipService;
import dev.rice.client.SemanticEvent;
import dev.rice.module.EvidenceModule;
import dev.rice.module.ModuleCategory;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

public final class TeamsModule extends EvidenceModule {
   public static final String[] COLORS = new String[]{
      "Black",
      "Dark blue",
      "Dark green",
      "Dark aqua",
      "Dark red",
      "Dark purple",
      "Gold",
      "Light gray",
      "Dark gray",
      "Light blue",
      "Light green",
      "Light aqua",
      "Light red",
      "Light purple",
      "Yellow",
      "White"
   };

   public TeamsModule() {
      super("teams", "Teams", ModuleCategory.UTILITY);
   }

   private static boolean colorCode(char c) {
      return (c - '0' & 65535) < 10 || (c - 'a' & 65535) < 6 || c == 'r';
   }

   private static boolean nameCharacter(char c) {
      return c == '_' || (c - '0' & 65535) < 10 || ((c & '\uffdf') - 65 & 65535) < 26;
   }

   public static char prefixColor(String text) {
      char result = 0;
      boolean section = false;

      for (int i = 0; i < text.length(); i++) {
         char c = text.charAt(i);
         boolean escaped = section;
         section = c == 167;
         if (c != 167) {
            if (escaped) {
               if (colorCode(c)) {
                  result = c;
               }
            } else if (nameCharacter(c)) {
               return result;
            }
         }
      }

      return '\u0000';
   }

   public static char trailingColor(String text) {
      char selected = 0;
      boolean valid = false;

      for (int i = text.length() - 1; i >= 0; i--) {
         char c = text.charAt(i);
         if (c == 167) {
            if (valid) {
               return selected;
            }

            valid = false;
         } else {
            valid = colorCode(c);
            if (valid) {
               selected = c;
            }
         }
      }

      return 'f';
   }

   /** Effective colour of the username, excluding rank prefixes and trailing resets. */
   public static char nameColor(String text, String name) {
      if (text == null || name == null || name.isEmpty()) return 0;
      StringBuilder visible = new StringBuilder();
      char[] colors = new char[text.length()];
      char active = 0;
      for (int i = 0; i < text.length(); i++) {
         char c = text.charAt(i);
         if (c == 167 && i + 1 < text.length()) {
            char code = Character.toLowerCase(text.charAt(++i));
            if ("0123456789abcdef".indexOf(code) >= 0) active = code;
            else if (code == 'r') active = 0;
         } else {
            colors[visible.length()] = active;
            visible.append(c);
         }
      }
      char found = 0;
      for (int at = 0; at + name.length() <= visible.length(); at++) {
         if (visible.substring(at, at + name.length()).equalsIgnoreCase(name)
            && (at == 0 || !nameCharacter(visible.charAt(at - 1)))
            && (at + name.length() == visible.length() || !nameCharacter(visible.charAt(at + name.length())))) {
            found = colors[at];
         }
      }
      return found;
   }

   private static char teamColor(net.minecraft.scoreboard.Team team) {
      if (team == null) return 0;
      // A marker avoids mistaking a rank's text or the team's suffix for the username colour.
      return nameColor(team.formatString("RiceTeamColorMarker"), "RiceTeamColorMarker");
   }

   public static char entityColor(Entity entity) {
      String name = net.minecraft.util.EnumChatFormatting.getTextWithoutFormattingCodes(entity.getName());
      IChatComponent display = entity.getDisplayName();
      char color = display == null ? 0 : nameColor(display.getFormattedText(), name);
      if (color != 0) return color;
      return nameColor(entity.getName(), name);
   }

   private static char playerColor(ClientContext context, EntityPlayer player) {
      char color = teamColor(player.getTeam());
      if (color != 0) return color;
      NetworkPlayerInfo info = context.minecraft().getNetHandler() == null ? null
         : context.minecraft().getNetHandler().getPlayerInfo(player.getUniqueID());
      if (info != null) {
         color = teamColor(info.getPlayerTeam());
         if (color != 0) return color;
         IChatComponent display = info.getDisplayName();
         if (display != null) {
            color = nameColor(display.getFormattedText(), player.getName());
            if (color != 0) return color;
         }
      }
      return entityColor(player);
   }

   @Override
   public boolean listens(int type) {
      return type == 0;
   }

   @Override
   public void onEvent(ClientContext context, SemanticEvent event) {
      if (!context.ready()) return;
      char owner = playerColor(context, context.player());
      for (EntityPlayer player : context.world().playerEntities) {
         if (player == context.player()) continue;
         char code = playerColor(context, player);
         boolean member = this.flag("match_self_color") && owner != 0 && code == owner;
         int index = "0123456789abcdef".indexOf(code);
         if (!member && index >= 0) member = this.names("team_colors").contains(COLORS[index]);
         RelationshipService.set(player, 1, member);
      }
   }

   @Override
   protected void onDisable(ClientContext context) {
      RelationshipService.clear(1);
   }

   @Override
   public void onWorldUnload(ClientContext context) {
      RelationshipService.clear(1);
   }
}
