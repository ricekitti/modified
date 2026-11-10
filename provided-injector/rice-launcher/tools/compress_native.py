"""Encode the embedded DLL using the loader's Windows LZMS resource format."""
import ctypes
from ctypes import wintypes
from pathlib import Path
import sys


def compress(source, destination):
    api = ctypes.WinDLL('cabinet', use_last_error=True)
    api.CreateCompressor.argtypes = [wintypes.DWORD, ctypes.c_void_p, ctypes.POINTER(ctypes.c_void_p)]
    api.CreateCompressor.restype = wintypes.BOOL
    api.Compress.argtypes = [ctypes.c_void_p, ctypes.c_void_p, ctypes.c_size_t,
                            ctypes.c_void_p, ctypes.c_size_t, ctypes.POINTER(ctypes.c_size_t)]
    api.Compress.restype = wintypes.BOOL
    api.CloseCompressor.argtypes = [ctypes.c_void_p]
    api.CloseCompressor.restype = wintypes.BOOL
    api.SetCompressorInformation.argtypes = [ctypes.c_void_p, ctypes.c_int, ctypes.c_void_p, ctypes.c_size_t]
    api.SetCompressorInformation.restype = wintypes.BOOL
    raw = Path(source).read_bytes()
    if len(raw) < 64 or raw[:2] != b'MZ':
        raise ValueError('Expected a native PE image')
    handle = ctypes.c_void_p()
    if not api.CreateCompressor(5, None, ctypes.byref(handle)):  # COMPRESS_ALGORITHM_LZMS
        raise ctypes.WinError(ctypes.get_last_error())
    try:
        # Keep all mapping variants in one block so identical embedded classes
        # can share dictionary entries, rather than restarting every 1 MB.
        block_size = wintypes.DWORD(32 * 1024 * 1024)
        if not api.SetCompressorInformation(handle, 1, ctypes.byref(block_size), ctypes.sizeof(block_size)):
            raise ctypes.WinError(ctypes.get_last_error())
        data = ctypes.create_string_buffer(raw)
        size = ctypes.c_size_t()
        measured = api.Compress(handle, data, len(raw), None, 0, ctypes.byref(size))
        if not measured and ctypes.get_last_error() != 122:  # ERROR_INSUFFICIENT_BUFFER
            raise ctypes.WinError(ctypes.get_last_error())
        output = ctypes.create_string_buffer(size.value)
        if not api.Compress(handle, data, len(raw), output, size.value, ctypes.byref(size)):
            raise ctypes.WinError(ctypes.get_last_error())
        packed = b'RSLZ0001' + output.raw[:size.value]
        target = Path(destination)
        target.parent.mkdir(parents=True, exist_ok=True)
        if not target.exists() or target.read_bytes() != packed:
            target.write_bytes(packed)
        print(f'Native resource: {len(raw):,} -> {len(packed):,} bytes')
    finally:
        api.CloseCompressor(handle)


if __name__ == '__main__':
    if len(sys.argv) != 3:
        raise SystemExit('Usage: compress_native.py input.dll output.lzms')
    compress(*sys.argv[1:])
