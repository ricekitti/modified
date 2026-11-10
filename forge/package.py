"""Package the verified JAR, plus a Windows EXE installer when building on Windows."""
from pathlib import Path
import hashlib
import json
import os
import shutil
import subprocess
import tempfile
import zipfile

ROOT = Path(__file__).resolve().parent
BUILD = ROOT / 'build'
DIST = ROOT / 'dist'
jar = DIST / 'rice-modified-forge-1.8.9.jar'
mac_jar = DIST / 'rice-mac-1.8.9.jar'
report_path = DIST / 'build-report.json'
report = json.loads(report_path.read_text())
digest = hashlib.sha256(jar.read_bytes()).hexdigest()
if digest != report['sha256']:
    raise RuntimeError('The JAR no longer matches the verified build report.')
if not mac_jar.is_file() or hashlib.sha256(mac_jar.read_bytes()).hexdigest() != digest:
    raise RuntimeError('The Mac JAR alias does not match the verified build.')
if not report.get('obfuscation', {}).get('changed_classes') or not report.get('licensing'):
    raise RuntimeError('Protected release checks are missing; refusing to package an unprotected build.')
if os.name == 'nt':
    csc = Path(os.environ.get('WINDIR', 'C:/Windows')) / 'Microsoft.NET/Framework64/v4.0.30319/csc.exe'
    if not csc.is_file():
        raise RuntimeError('The .NET Framework C# compiler is required to build the Windows installer.')
    checksum = BUILD / 'installer-payload.sha256'
    checksum.write_text(digest, encoding='ascii')
    exe = DIST / 'RiceModified-Forge-Setup.exe'
    subprocess.run([str(csc), '/nologo', '/target:winexe', '/platform:anycpu', '/optimize+',
                    '/reference:System.Windows.Forms.dll', '/reference:System.Drawing.dll',
                    '/resource:' + str(jar) + ',RiceModified.Forge.jar',
                    '/resource:' + str(checksum) + ',RiceModified.Forge.sha256',
                    '/out:' + str(exe), str(ROOT / 'installer/ForgeInstaller.cs')], check=True)
    with tempfile.TemporaryDirectory(prefix='installer-smoke-', dir=BUILD) as test:
        subprocess.run([str(exe), '--extract', test], check=True)
        if hashlib.sha256((Path(test) / jar.name).read_bytes()).hexdigest() != digest:
            raise RuntimeError('Windows installer extracted a different mod.')
    report['windows_installer'] = {'file': exe.name, 'sha256': hashlib.sha256(exe.read_bytes()).hexdigest(), 'payload_extraction_verified': True}
shutil.copy2(ROOT / 'README.md', DIST / 'README.md')
report_path.write_text(json.dumps(report, indent=2), encoding='utf-8')
with zipfile.ZipFile(DIST / 'RiceModified-Forge-1.8.9.zip', 'w', zipfile.ZIP_DEFLATED) as bundle:
    for name in (jar.name, mac_jar.name, 'README.md', 'build-report.json', 'module-inventory.tsv'):
        bundle.write(DIST / name, name)
print('Packaged verified Forge JAR' + (' and tested Windows EXE installer.' if os.name == 'nt' else '.'))
