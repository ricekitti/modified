# ============================================================================
# macOS / Apple Silicon (arm64) build of the client as libRiceSlinky.dylib.
#
# Self-contained on purpose: CMakeLists.txt includes this only when APPLE and
# then return()s, so on macOS the Windows build body is never entered, and for
# Windows nothing here is ever seen. Pair with cmake/toolchain-macos-arm64.cmake:
#   cmake -S . -B build/macos -DCMAKE_TOOLCHAIN_FILE=cmake/toolchain-macos-arm64.cmake
#   cmake --build build/macos
#
# The platform-independent trees (src/engine, src/logic, src/modules, NanoVG) are
# reused unchanged. Only the OS leaves differ: the Win32 WndProc input hook and the
# Win32 file/time layer are dropped and replaced by src/platform/mac/{input,files}.cpp;
# the DLL entry becomes the JVMTI agent entry (src/platform/mac/entry.cpp). The
# original image's data/rodata is retargeted from PE/GNU-asm to arm64 Mach-O.
# ============================================================================

set(CMAKE_CXX_FLAGS "" CACHE STRING "" FORCE)
set(CMAKE_C_FLAGS   "" CACHE STRING "" FORCE)

# ---- JDK headers (jni.h + darwin/jni_md.h). Any JDK 8+ compiles the agent; it attaches to Lunar's own
#      JVM at runtime, so the compile-time JDK need not match Lunar's. ----
if(DEFINED ENV{JAVA_HOME})
    set(SLINKY_JAVA_HOME "$ENV{JAVA_HOME}")
else()
    execute_process(COMMAND /usr/libexec/java_home
        OUTPUT_VARIABLE SLINKY_JAVA_HOME OUTPUT_STRIP_TRAILING_WHITESPACE RESULT_VARIABLE _jh_result)
    if(NOT _jh_result EQUAL 0 OR NOT SLINKY_JAVA_HOME)
        message(FATAL_ERROR "Set JAVA_HOME, or install a JDK, so jni.h / darwin/jni_md.h can be found.")
    endif()
endif()
if(NOT EXISTS "${SLINKY_JAVA_HOME}/include/jni.h")
    message(FATAL_ERROR "jni.h not under ${SLINKY_JAVA_HOME}/include -- point JAVA_HOME at a JDK.")
endif()

# ---- sources ----
file(GLOB_RECURSE ENGINE_SRC CONFIGURE_DEPENDS ${CMAKE_SOURCE_DIR}/src/engine/*.cpp)
list(REMOVE_ITEM ENGINE_SRC
    ${CMAKE_SOURCE_DIR}/src/engine/protected/input.cpp     # Win32 WndProc hook  -> src/platform/mac/input.cpp
    ${CMAKE_SOURCE_DIR}/src/engine/protected/files.cpp)    # Win32 file/time     -> src/platform/mac/files.cpp
file(GLOB_RECURSE LOGIC_SRC CONFIGURE_DEPENDS ${CMAKE_SOURCE_DIR}/src/logic/*.cpp)
list(REMOVE_ITEM LOGIC_SRC                                 # modified tree: these are excluded here too
    ${CMAKE_SOURCE_DIR}/src/logic/combat/displace.cpp
    ${CMAKE_SOURCE_DIR}/src/logic/combat/displace_core.cpp)
file(GLOB_RECURSE MODULE_SRC CONFIGURE_DEPENDS ${CMAKE_SOURCE_DIR}/src/modules/*.cpp)
# platform: the portable top-level files (gl_slots.cpp, nvg_names.cpp) + the macOS leaves (no src/platform/win/*)
file(GLOB PLATFORM_SRC CONFIGURE_DEPENDS
    ${CMAKE_SOURCE_DIR}/src/platform/*.cpp
    ${CMAKE_SOURCE_DIR}/src/platform/mac/*.cpp)
set(NANOVG_SRC ${CMAKE_SOURCE_DIR}/third_party/nanovg/src/nanovg.c ${CMAKE_SOURCE_DIR}/src/platform/nanovg_impl.c)

# ---- the original's data/rodata, retargeted to arm64 Mach-O then assembled (tools/retarget_data_macho.py:
#      .rdata/.data -> __DATA,__const/__data and '_'-prefixed symbols). iat.s is empty and chkstk.s is x86
#      only, so neither is used; arm64 needs no __chkstk stack probe. ----
find_program(SLINKY_PY    NAMES python3 python REQUIRED)
find_program(SLINKY_CLANG NAMES clang          REQUIRED)
set(_data_triple arm64-apple-macos11)
set(DATA_OBJ)
foreach(part rdata data)
    set(_macho_s ${CMAKE_BINARY_DIR}/data_${part}_macho.s)
    set(_obj     ${CMAKE_BINARY_DIR}/data_${part}.o)
    add_custom_command(OUTPUT ${_macho_s}
        COMMAND ${SLINKY_PY} ${CMAKE_SOURCE_DIR}/tools/retarget_data_macho.py
                ${CMAKE_SOURCE_DIR}/src/data/${part}.s ${_macho_s}
        DEPENDS ${CMAKE_SOURCE_DIR}/src/data/${part}.s ${CMAKE_SOURCE_DIR}/tools/retarget_data_macho.py VERBATIM)
    add_custom_command(OUTPUT ${_obj}
        COMMAND ${SLINKY_CLANG} -c -target ${_data_triple} ${_macho_s} -o ${_obj}
        DEPENDS ${_macho_s} VERBATIM)
    list(APPEND DATA_OBJ ${_obj})
endforeach()

# ---- feature-bridge payload (modified tree only): the Java feature jars embedded as a data object, same
#      retarget. Absent in the regular tree, so guard on its presence. ----
set(FEATURE_BUILD ${CMAKE_SOURCE_DIR}/../../feature-bridge/build)
if(EXISTS ${FEATURE_BUILD}/payloads.s)
    set(_feat_s ${CMAKE_BINARY_DIR}/source_features_payload_macho.s)
    set(_feat_o ${CMAKE_BINARY_DIR}/source_features_payload.o)
    add_custom_command(OUTPUT ${_feat_s}
        COMMAND ${SLINKY_PY} ${CMAKE_SOURCE_DIR}/tools/retarget_data_macho.py
                ${FEATURE_BUILD}/payloads.s ${_feat_s}
        DEPENDS ${FEATURE_BUILD}/payloads.s ${CMAKE_SOURCE_DIR}/tools/retarget_data_macho.py VERBATIM)
    add_custom_command(OUTPUT ${_feat_o}
        COMMAND ${SLINKY_CLANG} -c -target ${_data_triple} ${_feat_s} -o ${_feat_o}
        DEPENDS ${_feat_s} VERBATIM)
    list(APPEND DATA_OBJ ${_feat_o})
endif()

# ---- the library ----
add_library(RiceSlinky SHARED ${ENGINE_SRC} ${LOGIC_SRC} ${MODULE_SRC} ${PLATFORM_SRC} ${NANOVG_SRC} ${DATA_OBJ})
set_target_properties(RiceSlinky PROPERTIES OUTPUT_NAME RiceSlinky LINKER_LANGUAGE CXX)
set_source_files_properties(${DATA_OBJ} PROPERTIES EXTERNAL_OBJECT TRUE GENERATED TRUE)

# -GS- /Gy- on Windows == no stack cookies + function-level GC; the arm64 equivalents are below. The MSVC
# codegen-matching flags (asmdiff) and the per-file `// cflags:` tweaks only existed to byte-match the
# original x86 image and are intentionally not reproduced on a fresh architecture -- correctness, not identity.
target_compile_options(RiceSlinky PRIVATE
    -O2 -fno-stack-protector -ffunction-sections
    -fshort-wchar                     # wchar_t == 2 bytes to match the UTF-16 data tables (see mac/files.cpp)
    -fvisibility=hidden -w)
target_include_directories(RiceSlinky PRIVATE
    ${CMAKE_SOURCE_DIR}/include ${CMAKE_SOURCE_DIR}/third_party/nanovg/src ${CMAKE_SOURCE_DIR}/third_party/gl
    ${SLINKY_JAVA_HOME}/include ${SLINKY_JAVA_HOME}/include/darwin)
# glcorearb.h would include <windows.h> just to define APIENTRY; there is none on macOS, so define it empty.
target_compile_definitions(RiceSlinky PRIVATE APIENTRY=)

# OpenGL.framework supplies the legacy GL 2.1 symbols the NanoVG GL backend links; the engine also resolves
# entry points at runtime through slinky_gl_get_proc (mac/gl.cpp). libjvm is NOT linked: JNI goes through the
# JavaVM function table and JNI_GetCreatedJavaVMs is dlsym'd at load (mac/sys.cpp).
target_link_libraries(RiceSlinky PRIVATE "-framework OpenGL" pthread)
target_link_options(RiceSlinky PRIVATE -Wl,-install_name,@rpath/libRiceSlinky.dylib)
