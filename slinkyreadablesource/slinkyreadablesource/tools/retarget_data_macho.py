#!/usr/bin/env python3
# Retarget the generated Windows data objects (src/data/rdata.s, src/data/data.s) to arm64 macOS Mach-O.
#
# Those files are produced by tools/gen_data.py from the decrypted image and are target-neutral except for
# two things:
#   1. PE section directives:   .section .rdata,"dr"  /  .section .data,"dw"
#   2. symbol naming: Mach-O prefixes C symbols with '_', so every label, every .globl, and every
#      symbol-valued .quad/.long operand must be prefixed with '_' so the dylib's C references
#      (extern "C" rdata_* / data_*) resolve against them.
# Numeric data (.byte / .quad 0x.. / .asciz / .zero / .balign) is copied verbatim; the pointer tables use
# `.quad <symbol>`, which become ordinary Mach-O pointer relocations that dyld rebases into a dylib.
#
# gen_data.py and the decrypted image are not present in this source tree, so this converts the committed
# .s files directly. iat.s is empty on this image and chkstk.s is x86-only, so neither is converted or used.
#
#   python3 retarget_data_macho.py <in.s> <out.s>
import re
import sys

# .rdata holds const tables (strings, doubles, pointer arrays set once at load) -> __DATA,__const so dyld
# can rebase the internal pointers. .data holds globals the client writes at runtime -> __DATA,__data.
SECTION_MAP = (
    ('.rdata', '.section __DATA,__const'),
    ('.data', '.section __DATA,__data'),
)

LABEL_RE = re.compile(r'^[A-Za-z_][A-Za-z0-9_]*:$')
POINTER_RE = re.compile(r'^\s*(\.(?:quad|long|xword))\s+(.+?)\s*$')
SYMBOL_OPERAND_RE = re.compile(r'^([A-Za-z_][A-Za-z0-9_]*)\s*([+-]\s*(?:0[xX][0-9a-fA-F]+|\d+))?$')


def prefix_symbol_operand(operand):
    """Prefix a symbol operand with '_' (keeping an optional +N / -N addend); leave numerics untouched."""
    operand = operand.strip()
    if not operand or operand[0].isdigit():
        return operand                      # 0x.., decimal: a literal value, not a symbol
    m = SYMBOL_OPERAND_RE.match(operand)
    if not m:
        return operand                      # expression we do not understand: leave it (no false rewrite)
    name, addend = m.group(1), m.group(2) or ''
    return '_' + name + addend.replace(' ', '')


def convert_line(line):
    stripped = line.strip()
    if not stripped or stripped.startswith('#'):
        return line
    if stripped.startswith('.section'):
        for pe, macho in SECTION_MAP:
            if pe in stripped:
                return macho
        return line
    if stripped.startswith('.globl'):
        parts = stripped.split(None, 1)
        return '.globl _' + parts[1].strip() if len(parts) == 2 else line
    if LABEL_RE.match(stripped):
        return '_' + stripped
    m = POINTER_RE.match(line)
    if m:
        directive, rest = m.group(1), m.group(2)
        comment = ''
        hash_pos = rest.find('#')
        if hash_pos >= 0:
            comment = '   ' + rest[hash_pos:]
            rest = rest[:hash_pos].strip()
        return directive + ' ' + prefix_symbol_operand(rest) + comment
    return line


def main():
    if len(sys.argv) != 3:
        sys.stderr.write('usage: retarget_data_macho.py <in.s> <out.s>\n')
        return 2
    with open(sys.argv[1], 'r', encoding='utf-8', errors='surrogateescape') as f:
        lines = f.read().splitlines()
    converted = [convert_line(ln) for ln in lines]
    with open(sys.argv[2], 'w', encoding='utf-8', errors='surrogateescape') as f:
        f.write('\n'.join(converted) + '\n')
    return 0


if __name__ == '__main__':
    sys.exit(main())
