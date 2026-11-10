#pragma once
extern "C" void hud_editor_install();
extern "C" bool hud_editor_frame(void* state,void* renderer,void* manager,void* profiles);
extern "C" void hud_editor_button(void* state,void* renderer);
extern "C" bool hud_editor_input(void* event);

// In: normalized counter width/height, center Y gap, count. Out: X/Y offsets.
extern "C" void hud_editor_scaffold(float* layout);
