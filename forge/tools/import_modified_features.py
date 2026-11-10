"""Refresh editable feature sources from the modified client's preserved references."""
from pathlib import Path
import shutil

root = Path(__file__).resolve().parents[1]
target = root / 'src/main/java'
references = root.parent / 'slinkyreadablesource/slinkyreadablesource/references'
source = references / 'displace-bow-20261031/java/src/main/java'
names = ['dev/rice/module/impl/KnockbackDisplaceModule.java',
         'dev/rice/module/impl/BowAimbotModule.java', 'dev/rice/module/impl/WeaponsModule.java',
         'dev/rice/client/combat/RavenBsVoidScanner.java']
names += [p.relative_to(source).as_posix() for p in (source / 'dev/rice/module/impl/bowaimbot').glob('*.java')]
for name in names:
    destination = target / name
    destination.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(source / name, destination)
shutil.copy2(root.parent / 'feature-bridge/java/LegitVoidPolicy.java', target / 'dev/rice/client/combat/LegitVoidPolicy.java')
shutil.copy2(references / 'fog-20261101/FogModule.java', target / 'dev/rice/module/impl/FogModule.java')
shutil.copy2(references / 'shaders-20261101/ShaderModule.java', target / 'dev/rice/module/impl/ShaderModule.java')
path = target / 'dev/rice/module/impl/ShaderModule.java'
path.write_text(path.read_text().replace('scan_pixel', 'scan_pincushion').replace('Scan Pixel', 'Scan Pincushion'))
path = target / 'dev/rice/module/impl/KnockbackDisplaceModule.java'
text = path.read_text().replace('      BooleanSetting wall =',
    '      BooleanSetting legit = add(new BooleanSetting("legit_void", "Legit Void", false));\n'
    '      legit.set(false); legit.saveCurrent();\n      BooleanSetting wall =')
text = text.replace('key.equals("void_scan_radius") ||', 'key.equals("legit_void") || key.equals("void_scan_radius") ||')
text = text.replace('this.flag("void_ignore_one_block_wall"));',
                    'this.flag("void_ignore_one_block_wall"), this.flag("legit_void"));')
path.write_text(text)
path = target / 'dev/rice/client/combat/RavenBsVoidScanner.java'
text = path.read_text().replace('    private final Minecraft mc;',
    '    private final Minecraft mc;\n    private boolean legitVoid;\n    private float referenceYaw;')
text = text.replace('        if (!c.ready() || target == null) return null;\n'
    '        return new RavenBsVoidScanner(c, angle, radius, ignoreOneBlockWall).findBestVoidYaw(target, playerYaw);',
    '        return findYaw(c, target, playerYaw, angle, radius, ignoreOneBlockWall, false);\n'
    '    }\n'
    '    public static Float findYaw(ClientContext c, Entity target, float playerYaw, double angle,\n'
    '                                double radius, boolean ignoreOneBlockWall, boolean legitVoid) {\n'
    '        if (!c.ready() || target == null) return null;\n'
    '        RavenBsVoidScanner scanner = new RavenBsVoidScanner(c, angle, radius, ignoreOneBlockWall);\n'
    '        scanner.legitVoid = legitVoid;\n        scanner.referenceYaw = playerYaw;\n'
    '        return scanner.findBestVoidYaw(target, playerYaw);', 1)
text = text.replace('        double radians = Math.toRadians(candidateYaw);',
    '        if (legitVoid && !LegitVoidPolicy.allows(referenceYaw, candidateYaw)) return currentBest;\n'
    '        double radians = Math.toRadians(candidateYaw);', 1)
path.write_text(text)
print('Imported modified feature sources with portable Legit Void setting.')
