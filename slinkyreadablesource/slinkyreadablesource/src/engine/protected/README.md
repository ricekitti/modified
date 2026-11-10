# Readable versions of protector-obfuscated functions

The functions here are mutated or virtualised by the protector in the original image, so they cannot be
byte-verified like the rest of `src/engine`. They are written from what survives: the code around each
hidden call, the strings and globals they use, their callers, and emulation traces of the original.

* `files.cpp`  timer (`fn_4c270`) and the profile-folder file helpers (`fn_4c590` .. `fn_4cc40`)
* `input.cpp`  GetModuleHandle/GetProcAddress, the game-window input hook (`fn_4bda0`, `fn_4be60`,
  `fn_4bcd0`), synthetic clicks (`fn_4c390`) and the unload thread (`fn_4bd30`, `fn_4bd70`)
* `cjson.cpp`  cJSON parse_value / print_value / get_object_item (upstream cJSON 1.7.x)
* `hooks.cpp`  the `getMouseOver` native hook (`fn_7410`), plus notes on `fn_6e10` and the CRT DLL entry

Windows APIs are named from their arguments and use; places marked "inferred" or "NOT RECOVERED" say so.
They are compile-checked by `build.sh` (clang-cl, see include/slinky/win32.h) but excluded from the
byte comparison, and the image's protected copies stay listed in build/stubbed.json.
