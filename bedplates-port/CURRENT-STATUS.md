# v1.8.11 - Bedplates enable fix

- Fixed Bedplates switching itself off after enabling near a covered bed. The client log identified an IllegalArgumentException from LWJGL: its glGetFloat wrapper requires at least 16 remaining float elements, even though the current color contains only four components. Bedplates allocated four.
- Increased only Bedplates' render-state color buffer to 16 floats. Existing error handling, settings, display modes, defaults and bed-defense detection are unchanged. Other client feature sources match v1.8.10.
- Strengthened the headless GL binding to enforce the real LWJGL buffer requirement. The old implementation reproduces the client failure; the corrected implementation renders icons and counts and restores GL stacks under the stricter binding.
- Launcher and Windows file/product versions are 1.8.11.0. Previous versioned releases preserved.

Validation: 32/32 CTest tests passed. Bedplates passed 39 checks in each of named, SRG and official mappings, including explicit rejection of undersized query buffers and the corrected production render query. Extracted embedded DLL matches the rebuilt DLL exactly. Previous v1.8.10 feature source hashes were verified unchanged; Bedplates' source difference is the buffer allocation/comment (with normalized line endings).

The original live failure was confirmed in the local client log. The fixed build has not been retested in live gameplay or on the actual GPU. Restart Minecraft before injecting, use this versioned EXE, then enable Bedplates under Visual. If the running root slinky-modified.exe is locked, that copy remains older.

For the original feature/settings summary, see ../releases/rice-loader-v1.8.8-bedplates.changes.md. Build: feature-bridge/build.py, native CMake, update launcher bundled DLL, build launcher/tests, CTest, --check-runtime.
