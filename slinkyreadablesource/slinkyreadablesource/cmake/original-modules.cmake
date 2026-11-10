# Build the archived module implementations verbatim. Only identifier aliases
# bridge the renamed ZIP symbols to this client's existing engine ABI.
set(OG_MODULE_ROOT "${CMAKE_SOURCE_DIR}/original/slinky-main")
# Prevent accidental drift in the archived module reference.
file(READ "${OG_MODULE_ROOT}/source-manifest.json" OG_SOURCE_MANIFEST)
string(JSON OG_SOURCE_COUNT LENGTH "${OG_SOURCE_MANIFEST}")
math(EXPR OG_SOURCE_LAST "${OG_SOURCE_COUNT} - 1")
foreach(index RANGE 0 ${OG_SOURCE_LAST})
    string(JSON relative MEMBER "${OG_SOURCE_MANIFEST}" ${index})
    string(JSON expected GET "${OG_SOURCE_MANIFEST}" "${relative}")
    file(SHA256 "${OG_MODULE_ROOT}/${relative}" actual)
    if(NOT actual STREQUAL expected)
        message(FATAL_ERROR "Original Slinky source changed: ${relative}. Keep Rice changes in the extension code.")
    endif()
endforeach()

file(GLOB_RECURSE OG_MODULE_SOURCES CONFIGURE_DEPENDS
    "${OG_MODULE_ROOT}/src/logic/*.cpp"
    "${OG_MODULE_ROOT}/src/modules/*.cpp")
list(FILTER OG_MODULE_SOURCES EXCLUDE REGEX "/combat/knockback_displace\\.cpp$")
foreach(og IN LISTS OG_MODULE_SOURCES)
    file(RELATIVE_PATH relative "${OG_MODULE_ROOT}" "${og}")
    set(previous "${CMAKE_SOURCE_DIR}/${relative}")
    list(REMOVE_ITEM LOGIC_SRC "${previous}")
    list(REMOVE_ITEM MODULE_SRC "${previous}")
    if(relative MATCHES "^src/logic/")
        list(APPEND LOGIC_SRC "${og}")
    else()
        list(APPEND MODULE_SRC "${og}")
    endif()
endforeach()
function(original_modules_apply_compiler_options)
    foreach(og IN LISTS OG_MODULE_SOURCES)
        set_property(SOURCE "${og}" PROPERTY INCLUDE_DIRECTORIES "${OG_MODULE_ROOT}/include")
        set_property(SOURCE "${og}" APPEND PROPERTY COMPILE_OPTIONS "/FI${OG_MODULE_ROOT}/compat.h")
    endforeach()
    # Keep the unmodified Block In code for comparison; its runtime callback
    # uses the separate null-safe lifecycle implementation.
    set_property(SOURCE "${OG_MODULE_ROOT}/src/logic/block/block_in.cpp" APPEND PROPERTY
        COMPILE_DEFINITIONS "block_in_enable_remember_facing=og_block_in_enable_remember_facing")
    if(EXISTS "${CMAKE_SOURCE_DIR}/src/logic/visual/player_esp_extensions.cpp")
        set_property(SOURCE "${OG_MODULE_ROOT}/src/logic/visual/player_esp.cpp" APPEND PROPERTY
            COMPILE_DEFINITIONS "player_esp_render_mode_1=og_player_esp_render_mode_1")
        set_property(SOURCE "${OG_MODULE_ROOT}/src/modules/visual/player_esp.cpp" APPEND PROPERTY
            COMPILE_DEFINITIONS "player_esp_gui=og_player_esp_gui;player_esp_status=og_player_esp_status")
    endif()
endfunction()

