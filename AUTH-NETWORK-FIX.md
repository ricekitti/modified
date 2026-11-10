# Soju authentication rebuild

Both regular and modified loaders use the supplied KeyAuth configuration:
name Soju, ownerid UVLPXieCUS, version 1.0.

The old WinHTTP implementation has been replaced with WinINet using the current
Windows user's Internet/proxy settings. The native C++ launcher uses the same
KeyAuth init-then-license API flow as the supplied C# configuration; it does not
require .NET or an external authentication helper.

Requests use HTTPS POST directly to https://keyauth.win/api/1.3/. The alternate
keyauth.uk endpoint was investigated but currently returns HTTP 307 to
keyauth.win, so it is not used. Requests do not follow redirects, persist cookies,
or cache responses. Windows certificate validation remains enabled.

Application ownership and the complete JSON response are validated before any
license success is accepted. Missing sessions, rejected keys, malformed/partial
responses, duplicate keys, string/nested success fields, and network failures
cannot authenticate. Network errors identify WinINet and the Windows error code.

Existing saved keys and MachineGuid/computer-name HWID bindings are preserved.
The C# SDK uses a different HWID format; switching that format would invalidate
existing device bindings, so this rebuild deliberately preserves compatibility.

Verification: regular 5/5 and modified 15/15 automated tests passed. The manual
connection probe initializes only the public app; it sends no license or HWID
and cannot inject. Final live initialization and embedded payload checks are
recorded alongside the release build.

Use rice-loader-auth-remade.exe for this build. Existing older named loaders
are retained. A remote PC's Windows certificate/trust problem may still require
repair on that PC; a successful test here does not prove that PC is fixed.

