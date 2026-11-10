cmake_minimum_required(VERSION 3.21)

project(rice_slinky LANGUAGES CXX
        VERSION 1.8.22
        DESCRIPTION "Rice Slinky client launcher")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

# Multithreaded CRT so <thread> (std::thread / _beginthreadex) is available.
cmake_policy(SET CMP0091 NEW)
set(CMAKE_MSVC_RUNTIME_LIBRARY "MultiThreaded$<$<CONFIG:Debug>:Debug>")

if(NOT WIN32)
    message(FATAL_ERROR "Rice Slinky launcher targets Windows x64 only.")
endif()

# Simple console loader with the Soju authentication client and embedded payload.
add_executable(rice_slinky
    src/main.cpp
    src/AuthManager.cpp
    src/KeyAuthClient.cpp
    src/KeyAuthTransport.cpp
    src/Process.cpp
    src/Loader.cpp
    src/Resources.cpp
    src/Logger.cpp
    src/rice-slinky.rc)

target_include_directories(rice_slinky PRIVATE src)
find_package(Python3 REQUIRED COMPONENTS Interpreter)
set(RICE_NATIVE_PACKED "${CMAKE_CURRENT_BINARY_DIR}/RiceNative.lzms")
add_custom_command(OUTPUT "${RICE_NATIVE_PACKED}"
    COMMAND "${Python3_EXECUTABLE}" "${CMAKE_CURRENT_SOURCE_DIR}/tools/compress_native.py"
        "${CMAKE_CURRENT_SOURCE_DIR}/src/bundled/RiceNative.dll" "${RICE_NATIVE_PACKED}"
    DEPENDS src/bundled/RiceNative.dll tools/compress_native.py VERBATIM)
add_custom_target(rice_native_resource DEPENDS "${RICE_NATIVE_PACKED}")
add_dependencies(rice_slinky rice_native_resource)
set_source_files_properties(src/rice-slinky.rc PROPERTIES OBJECT_DEPENDS
    "${RICE_NATIVE_PACKED};${CMAKE_CURRENT_SOURCE_DIR}/src/app.manifest"
    COMPILE_DEFINITIONS "RICE_NATIVE_RESOURCE_FILE=\"${RICE_NATIVE_PACKED}\"")

target_compile_definitions(rice_slinky PRIVATE
    UNICODE _UNICODE
    WIN32_LEAN_AND_MEAN NOMINMAX
    SOJU_VERSION="${PROJECT_VERSION}")

target_compile_definitions(rice_slinky PRIVATE $<$<CONFIG:Debug>:SOJU_DEBUG=1>)

if(MSVC)
    target_compile_options(rice_slinky PRIVATE /W4 /permissive- /EHsc /utf-8)
    # We embed our own application manifest via rice-slinky.rc; stop the linker adding a second one.
    target_link_options(rice_slinky PRIVATE /MANIFEST:NO)
endif()

target_link_libraries(rice_slinky PRIVATE
    user32 advapi32 shell32 shlwapi bcrypt
    ole32 uuid psapi wininet cabinet)

set_target_properties(rice_slinky PROPERTIES OUTPUT_NAME "rice-loader")

include(CTest)
if(BUILD_TESTING)
    add_executable(LicensePromptTest tests/LicensePromptTest.cpp)
    target_include_directories(LicensePromptTest PRIVATE src)
    add_test(NAME LicensePrompt COMMAND LicensePromptTest)
    add_executable(GuiBrandingTest tests/GuiBrandingTest.cpp)
    target_include_directories(GuiBrandingTest PRIVATE ../../slinkyreadablesource/slinkyreadablesource/include)
    add_test(NAME GuiBranding COMMAND GuiBrandingTest)
    add_executable(PlayerEspCompatibilityTest tests/PlayerEspCompatibilityTest.cpp)
    target_include_directories(PlayerEspCompatibilityTest PRIVATE ../../slinkyreadablesource/slinkyreadablesource/include)
    add_test(NAME PlayerEspCompatibility COMMAND PlayerEspCompatibilityTest)
    add_executable(BlockInLifecycleTest tests/BlockInLifecycleTest.cpp)
    target_include_directories(BlockInLifecycleTest PRIVATE ../../slinkyreadablesource/slinkyreadablesource/include)
    add_test(NAME BlockInLifecycle COMMAND BlockInLifecycleTest)
    add_executable(KeyAuthTransportTest tests/KeyAuthTransportTest.cpp)
    target_include_directories(KeyAuthTransportTest PRIVATE src)
    add_test(NAME KeyAuthTransport COMMAND KeyAuthTransportTest)
    add_executable(KeyAuthClientTest tests/KeyAuthClientTest.cpp)
    target_include_directories(KeyAuthClientTest PRIVATE src)
    target_link_libraries(KeyAuthClientTest PRIVATE bcrypt advapi32)
    add_test(NAME KeyAuthClient COMMAND KeyAuthClientTest)
    add_executable(InjectionNoticeTest tests/InjectionNoticeTest.cpp
        ../../slinkyreadablesource/slinkyreadablesource/src/engine/jni/injection_notice.cpp)
    target_include_directories(InjectionNoticeTest PRIVATE ../../slinkyreadablesource/slinkyreadablesource/include)
    add_test(NAME InjectionNotice COMMAND InjectionNoticeTest)
    add_executable(HudEditorTest tests/HudEditorTest.cpp)
    target_include_directories(HudEditorTest PRIVATE ../../slinkyreadablesource/slinkyreadablesource/include ../../slinkyreadablesource/slinkyreadablesource/third_party/nanovg/src)
    add_test(NAME HudEditor COMMAND HudEditorTest)
    add_executable(HeavyGlowGpuTest tests/HeavyGlowGpuTest.cpp)
    target_include_directories(HeavyGlowGpuTest PRIVATE ../../slinkyreadablesource/slinkyreadablesource/include)
    target_compile_definitions(HeavyGlowGpuTest PRIVATE
        OPENB4_REFERENCE_DIR="${CMAKE_CURRENT_SOURCE_DIR}/../../slinkyreadablesource/slinkyreadablesource/references/openb4-u20plus-4")
    target_link_libraries(HeavyGlowGpuTest PRIVATE opengl32 user32 gdi32)
    add_test(NAME HeavyGlowGpu COMMAND HeavyGlowGpuTest)
    add_test(NAME HeavyGlowTwoTextureUnits COMMAND HeavyGlowGpuTest --two-units)
    add_test(NAME HeavyGlowUnsupportedFallback COMMAND HeavyGlowGpuTest --no-shaders)
    set_tests_properties(HeavyGlowGpu PROPERTIES SKIP_RETURN_CODE 77)
    add_executable(FireballPearlNativeTest tests/FireballPearlNativeTest.cpp
        ../../slinkyreadablesource/slinkyreadablesource/src/logic/combat/anti_fireball_core.cpp)
    target_include_directories(FireballPearlNativeTest PRIVATE ../../slinkyreadablesource/slinkyreadablesource/include)
    add_test(NAME FireballPearlNative COMMAND FireballPearlNativeTest)
    add_executable(GuiStartupTest tests/GuiStartupTest.cpp
        ../../slinkyreadablesource/slinkyreadablesource/src/engine/jni/module_startup.cpp)
    add_test(NAME GuiStartup COMMAND GuiStartupTest)
    add_executable(AntiFireballCoreTest tests/AntiFireballCoreTest.cpp
        ../../slinkyreadablesource/slinkyreadablesource/src/logic/combat/anti_fireball_core.cpp)
    target_include_directories(AntiFireballCoreTest PRIVATE ../../slinkyreadablesource/slinkyreadablesource/include)
    add_test(NAME AntiFireballCore COMMAND AntiFireballCoreTest)
    add_executable(AntiFireballNativeTest tests/AntiFireballNativeTest.cpp
        ../../slinkyreadablesource/slinkyreadablesource/src/logic/combat/anti_fireball_core.cpp)
    target_include_directories(AntiFireballNativeTest PRIVATE ../../slinkyreadablesource/slinkyreadablesource/include)
    add_test(NAME AntiFireballNative COMMAND AntiFireballNativeTest)
    add_executable(SourceFeaturesJvmTest tests/SourceFeaturesJvmTest.cpp
        ../../slinkyreadablesource/slinkyreadablesource/build/source_features_payload.obj)
    target_include_directories(SourceFeaturesJvmTest PRIVATE ../../slinkyreadablesource/slinkyreadablesource/include)
    add_test(NAME SourceFeaturesJvm COMMAND SourceFeaturesJvmTest
        ${CMAKE_CURRENT_SOURCE_DIR}/../../feature-bridge/build/test-classpath.txt)
    foreach(mapping srg official)
        add_test(NAME SourceFeaturesJvm_${mapping} COMMAND SourceFeaturesJvmTest
            ${CMAKE_CURRENT_SOURCE_DIR}/../../feature-bridge/build/test-classpath-${mapping}.txt
            ${CMAKE_CURRENT_SOURCE_DIR}/../../feature-bridge/build/test-mappings-${mapping}.txt)
    endforeach()
    find_program(BEDPLATES_JAVA java PATHS "C:/Program Files/Eclipse Adoptium/jdk-21.0.12.101-hotspot/bin" REQUIRED NO_DEFAULT_PATH)
    foreach(mapping named srg official)
        if(mapping STREQUAL "named")
            set(PLATES_CP_FILE "${CMAKE_CURRENT_SOURCE_DIR}/../../feature-bridge/build/test-classpath.txt")
        else()
            set(PLATES_CP_FILE "${CMAKE_CURRENT_SOURCE_DIR}/../../feature-bridge/build/test-classpath-${mapping}.txt")
        endif()
        file(READ "${PLATES_CP_FILE}" PLATES_CP)
        add_test(NAME Bedplates_${mapping} COMMAND "${BEDPLATES_JAVA}" -Xverify:all -cp "${PLATES_CP}" BedplatesTest)
        set_tests_properties(Bedplates_${mapping} PROPERTIES ENVIRONMENT "JAVA_TOOL_OPTIONS=;_JAVA_OPTIONS=")
        add_test(NAME RiceAnticheat_${mapping} COMMAND "${BEDPLATES_JAVA}" -Xverify:all -cp "${PLATES_CP}" RiceAnticheatTest)
        set_tests_properties(RiceAnticheat_${mapping} PROPERTIES ENVIRONMENT "JAVA_TOOL_OPTIONS=;_JAVA_OPTIONS=")
    endforeach()
    add_executable(ModuleStartupTest tests/ModuleStartupTest.cpp
        ../../slinkyreadablesource/slinkyreadablesource/src/engine/jni/module_toggles.cpp)
    target_include_directories(ModuleStartupTest PRIVATE ../../slinkyreadablesource/slinkyreadablesource/include)
    add_test(NAME ModuleStartup COMMAND ModuleStartupTest)
    add_executable(StasisNativeTest tests/StasisNativeTest.cpp
        ../../slinkyreadablesource/slinkyreadablesource/src/logic/move/stasis.cpp)
    target_include_directories(StasisNativeTest PRIVATE ../../slinkyreadablesource/slinkyreadablesource/include)
    add_test(NAME StasisNative COMMAND StasisNativeTest)
    add_executable(LauncherRegressionTest tests/LauncherRegressionTest.cpp
        src/Loader.cpp src/Integrity.cpp src/Logger.cpp src/JavaRuntime.cpp)
    target_include_directories(LauncherRegressionTest PRIVATE src)
    target_compile_definitions(LauncherRegressionTest PRIVATE UNICODE _UNICODE WIN32_LEAN_AND_MEAN NOMINMAX)
    target_link_libraries(LauncherRegressionTest PRIVATE bcrypt advapi32)
    add_test(NAME LauncherRegression COMMAND LauncherRegressionTest)
    add_executable(NativeLoaderSmokeTest tests/NativeLoaderSmokeTest.cpp src/Loader.cpp src/Logger.cpp)
    target_include_directories(NativeLoaderSmokeTest PRIVATE src)
    target_compile_definitions(NativeLoaderSmokeTest PRIVATE UNICODE _UNICODE WIN32_LEAN_AND_MEAN NOMINMAX)
    target_link_libraries(NativeLoaderSmokeTest PRIVATE bcrypt)
endif()

# Deliberately manual: service initialization only, not a license/auth bypass.
add_executable(KeyAuthConnectionCheck tests/KeyAuthConnectionCheck.cpp src/KeyAuthTransport.cpp)
target_include_directories(KeyAuthConnectionCheck PRIVATE src)
target_link_libraries(KeyAuthConnectionCheck PRIVATE wininet)

# Standalone resource-decoder regression; never starts a launcher or game.
add_executable(NativePayloadCheck tests/NativePayloadCheck.cpp)
target_include_directories(NativePayloadCheck PRIVATE src)
target_compile_definitions(NativePayloadCheck PRIVATE UNICODE _UNICODE WIN32_LEAN_AND_MEAN NOMINMAX)
target_link_libraries(NativePayloadCheck PRIVATE cabinet)
if(BUILD_TESTING)
    add_test(NAME NativePayloadRoundtrip COMMAND NativePayloadCheck
        "${CMAKE_CURRENT_SOURCE_DIR}/src/bundled/RiceNative.dll" "${RICE_NATIVE_PACKED}")
endif()

if(BUILD_TESTING)
    add_executable(ScaffoldWalkOffTest tests/ScaffoldWalkOffTest.cpp)
    target_include_directories(ScaffoldWalkOffTest PRIVATE ../../slinkyreadablesource/slinkyreadablesource/include)
    add_test(NAME ScaffoldWalkOff COMMAND ScaffoldWalkOffTest)
endif()

if(BUILD_TESTING)
    add_executable(ScaffoldModesTest tests/ScaffoldModesTest.cpp)
    target_include_directories(ScaffoldModesTest PRIVATE ../../slinkyreadablesource/slinkyreadablesource/include)
    add_test(NAME ScaffoldModes COMMAND ScaffoldModesTest)
endif()

if(BUILD_TESTING)
    add_executable(ScaffoldPlacementTest tests/ScaffoldPlacementTest.cpp)
    target_include_directories(ScaffoldPlacementTest PRIVATE ../../slinkyreadablesource/slinkyreadablesource/include)
    add_test(NAME ScaffoldPlacement COMMAND ScaffoldPlacementTest)
endif()
