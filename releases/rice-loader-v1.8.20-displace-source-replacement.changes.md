# v1.8.20 - Displace source replacement

- Uses the supplied 2026-11-04 KnockbackDisplaceModule and RavenBsVoidScanner sources verbatim; source bytes verified against the supplied ZIP.
- Removes the previous native Displace implementation and Void backend from active source/build, and excludes archived native Displace from compilation. Removes old descriptor callback pointers; the supplied implementation supplies all active callbacks.
- Removes the Legit Void build modification, native correction, setting and obsolete tests. Excludes old Displace backend classes from all three Java payloads.
- Keeps other current client features. Prior versioned EXEs are preserved.
- Launcher project version 1.8.20; Windows file/product versions 1.8.20.0.

Validation: 31/31 CTest checks passed, including supplied Displace lifecycle, rotation, packet and Void checks under named/SRG/official mappings. Embedded runtime extraction matches rebuilt DLL. No live gameplay test performed.
