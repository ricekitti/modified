Open the menu and select Edit HUD at the bottom of any tab.

Drag Module list or Notifications to move that HUD element. Previews appear
even when an element is disabled; editing does not enable modules. Done or Esc
saves the positions and returns to the menu. Reset positions restores both
default locations. Closing the menu also keeps the current layout.

The button icon, border, hover tint, preview outlines and editor controls use
the current Visual > GUI accent color. Positions are stored with the GUI
settings as normalized offsets so they adapt to resolution and GUI scale.

Previews are drawn by the original in-game HUD renderers, using current font,
text/background colors, rounding, padding, suffixes and notification styling.
Live module names and the latest queued notification are used when available;
empty widgets use sample text rendered with the same gameplay code. Selection
outlines appear only while hovered or dragged. Drag regions are measured from
the actual rendered contents instead of generic placeholder cards.

The editor covers the native client's two existing anchored HUD elements.
Rendering changes are wrappers around the original callbacks; the original
module sources remain unchanged. Startup injection notification rendering is
unchanged. Automated checks cover input scaling, dragging and screen bounds,
both renderer offsets, live theme changes, save/reset/Esc behavior, popup
blocking and original callback/profile registration.
