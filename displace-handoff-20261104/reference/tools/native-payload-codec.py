"""Windows LZMS resource codec, shared by guarded packaging and resource-chain checks."""
import ctypes
from ctypes import wintypes

MAGIC = b'RSLZ0001'
MAXIMUM = 256 * 1024 * 1024

def transform(data, compress):
    if not data or len(data) > MAXIMUM:
        raise ValueError('Native payload input size is invalid')
    api = ctypes.WinDLL('cabinet.dll', use_last_error=True)
    create = getattr(api, 'CreateCompressor' if compress else 'CreateDecompressor')
    run = getattr(api, 'Compress' if compress else 'Decompress')
    close = getattr(api, 'CloseCompressor' if compress else 'CloseDecompressor')
    create.argtypes = [wintypes.DWORD, ctypes.c_void_p, ctypes.POINTER(ctypes.c_void_p)]
    create.restype = wintypes.BOOL
    run.argtypes = [ctypes.c_void_p, ctypes.c_void_p, ctypes.c_size_t, ctypes.c_void_p, ctypes.c_size_t, ctypes.POINTER(ctypes.c_size_t)]
    run.restype = wintypes.BOOL
    close.argtypes = [ctypes.c_void_p]
    close.restype = wintypes.BOOL
    handle = ctypes.c_void_p()
    if not create(5, None, ctypes.byref(handle)):  # COMPRESS_ALGORITHM_LZMS
        raise ctypes.WinError(ctypes.get_last_error())
    try:
        if compress:
            # LZMS blocks can be 64 KiB..64 MiB. One 16 MiB block shares the
            # repeated content across all three embedded mapping JARs.
            configure = api.SetCompressorInformation
            configure.argtypes = [ctypes.c_void_p, ctypes.c_int, ctypes.c_void_p, ctypes.c_size_t]
            configure.restype = wintypes.BOOL
            block = wintypes.DWORD(16 * 1024 * 1024)
            if not configure(handle, 1, ctypes.byref(block), ctypes.sizeof(block)):
                raise ctypes.WinError(ctypes.get_last_error())
        source = ctypes.create_string_buffer(data)
        size = ctypes.c_size_t()
        measured = run(handle, source, len(data), None, 0, ctypes.byref(size))
        if not measured and ctypes.get_last_error() != 122:
            raise ctypes.WinError(ctypes.get_last_error())
        if not 0 < size.value <= MAXIMUM:
            raise ValueError('Native payload expansion size is invalid')
        output = ctypes.create_string_buffer(size.value)
        written = ctypes.c_size_t()
        if not run(handle, source, len(data), output, size.value, ctypes.byref(written)):
            raise ctypes.WinError(ctypes.get_last_error())
        if not 0 < written.value <= size.value or (not compress and written.value != size.value):
            raise ValueError('Native payload output length mismatch')
        return output.raw[:written.value]
    finally:
        close(handle)

def unpack(data):
    if data.startswith(MAGIC):
        data = transform(data[len(MAGIC):], False)
    if not 64 <= len(data) <= MAXIMUM or not data.startswith(b'MZ'):
        raise ValueError('Native payload is not a PE image')
    return data

def pack(data):
    if unpack(data) != data:
        raise ValueError('Expected an uncompressed native PE image')
    result = MAGIC + transform(data, True)
    if unpack(result) != data:
        raise ValueError('Native payload compression roundtrip failed')
    return result
