#pragma once
namespace guiviewport {
// Edit HUD occupies [height-56,height-22]; keep twenty pixels above it clear.
inline float contentHeight(float height) { return height > 76.f ? height - 76.f : 0.f; }
inline bool contains(float screenY, float height) { return screenY >= 0.f && screenY < contentHeight(height); }
}
