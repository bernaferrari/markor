#!/usr/bin/env python3
"""Capture Wordkeep's real Android screens for the Play listing (Python 3, no dependencies).

Build first:  ./gradlew :app:assembleFlavorDefaultStoreCapture
Run:          python3 store-listing/capture-emulator.py --serial emulator-5570 [--profile phone] [--locale de-DE]

The storeCapture build seeds fictional notes on first launch (app/src/storeCapture). This script sets the
display size and density per profile, System UI demo mode (9:41, full battery and Wi-Fi), gesture navigation
and the app language, then drives each scene and writes <locale>/raw/<profile>/<scene>.png plus a
.nodes.json layout sidecar. Display overrides and demo mode are reset afterwards, even on failure.
"""
import argparse
import json
import re
import subprocess
import sys
import time
from pathlib import Path

HERE = Path(__file__).resolve().parent
ROOT = HERE.parent
sys.path.insert(0, str(HERE / 'studio'))
from uitree import Navigator, nodes_from_xml, set_app_locale, write_sidecar  # noqa: E402

parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
parser.add_argument('--serial', required=True, help='Dedicated emulator serial; never a physical phone')
parser.add_argument('--profile', choices=['phone', 'tablet7', 'tablet10'], help='Capture one profile only')
parser.add_argument('--locale', help='Play locale, e.g. de-DE (default: the studio default locale)')
parser.add_argument('--scene', action='append', help='Capture only these scenes (repeatable)')
args = parser.parse_args()
if not args.serial.startswith('emulator-'):
    parser.error('Use a dedicated emulator, not a personal device.')

studio = json.loads((HERE / 'studio.json').read_text())
reference = studio.get('defaultLocale', 'en-US')
locale = args.locale or reference
navigator = Navigator(HERE / reference / 'raw/tap-map.json', recording=locale == reference)
package = 'com.bernaferrari.remarkor.storecapture'
activity = f'{package}/com.bernaferrari.remarkor.storecapture.StoreCaptureActivity'
apk = ROOT / 'app/build/outputs/apk/flavorDefault/storeCapture/app-flavorDefault-storeCapture.apk'
if not apk.exists():
    raise SystemExit('Build first: ./gradlew :app:assembleFlavorDefaultStoreCapture')
PALETTE = 'amber'


def adb(*words):
    return subprocess.check_output(['adb', '-s', args.serial, *map(str, words)], stderr=subprocess.STDOUT)


def shell(*words):
    return adb('shell', *words)


def demo(command, **extras):
    words = ['am', 'broadcast', '-a', 'com.android.systemui.demo', '-e', 'command', command]
    for key, value in extras.items():
        words += ['-e', key, str(value)]
    shell(*words)


xml = None


def dump():
    global xml
    shell('uiautomator', 'dump', '--compressed', '/sdcard/wordkeep-capture.xml')
    xml = shell('cat', '/sdcard/wordkeep-capture.xml')
    return nodes_from_xml(xml)


def tap(label, key=None):
    pattern = re.compile(label)
    for attempt in range(6):
        dump()
        try:
            node = navigator.resolve(f'{profile}|{key or label}', xml,
                                     lambda n: bool(pattern.fullmatch(n.get('text', '')) or pattern.fullmatch(n.get('desc', ''))))
            break
        except AssertionError:
            if attempt == 5:
                raise
            time.sleep(1)
    l, t, r, b = node['b']
    shell('input', 'tap', (l + r) // 2, (t + b) // 2)
    time.sleep(1.5)


def back():
    shell('input', 'keyevent', 'KEYCODE_BACK')
    time.sleep(1)


def launch(clear=False, **extras):
    shell('am', 'force-stop', package)
    if clear:
        shell('pm', 'clear', package)
        set_app_locale(adb, package, locale)
    words = ['am', 'start', '-W', '-n', activity]
    for key, value in extras.items():
        words += ['--ez' if isinstance(value, bool) else '--es', key, str(value).lower() if isinstance(value, bool) else value]
    shell(*words)
    time.sleep(4)


def settle_grid(key):
    # The grid can draw before note metadata arrives (pinned notes not yet first); switching the
    # filter away and back redraws it with pins and labels in place.
    tap('Favorites', key=f'{key}|Favorites')
    tap('All', key=f'{key}|All')
    time.sleep(1)


def capture(name):
    if args.scene and name not in args.scene:
        return
    time.sleep(1)
    nodes = dump()
    if not any(n.get('pkg') == package for n in nodes):
        raise RuntimeError(f'{profile}/{name}: Wordkeep is not in the foreground')
    if any(n.get('pkg') == 'android' and re.search("isn't responding|keeps stopping", n.get('text', '')) for n in nodes):
        raise RuntimeError(f'{profile}/{name}: a system dialog is on screen')
    png = adb('exec-out', 'screencap', '-p')
    png = png[png.index(b'\x89PNG\r\n\x1a\n'):]  # some emulators print a multi-display warning first
    size = (int.from_bytes(png[16:20], 'big'), int.from_bytes(png[20:24], 'big'))
    if size != (width, height):
        raise RuntimeError(f'{profile}/{name}: got {size}, expected {(width, height)}')
    file = HERE / locale / 'raw' / profile / f'{name}.png'
    file.parent.mkdir(parents=True, exist_ok=True)
    file.write_bytes(png)
    write_sidecar(file, xml, width, height)
    print(file.relative_to(HERE), flush=True)


def scenes():
    phone = profile == 'phone'
    # A fresh install shows the real intro; the storage choice is the local-first promise.
    launch(clear=True, seed=False)
    tap('Get started')
    capture('storage')

    launch(clear=True, palette=PALETTE)  # seeds the fictional notebook
    launch(palette=PALETTE)
    settle_grid('home')
    capture('home')

    tap('Lisbon in May')
    capture('editor')
    tap('Preview')
    capture('preview')
    tap('Color')
    capture('color')
    back()
    tap('More options')
    tap('Document Outline')
    capture('outline')
    back()
    back()
    if not phone:
        back()  # tablets show the editor as a dialog; make sure it is closed

    launch(palette=PALETTE)
    tap('todo')
    tap('Preview')
    capture('todo')

    launch(palette=PALETTE)
    tap('Journal')
    capture('journal')

    launch(palette=PALETTE)
    tap('Settings')
    capture('settings')

    launch(theme='dark', palette=PALETTE)
    settle_grid('dark')
    capture('dark')
    tap('Lisbon in May', key='dark|Lisbon in May')
    capture('dark-editor')


profiles = {name: (p['captureWidth'], p['captureHeight'], p['captureDensity'])
            for name, p in json.loads((HERE / 'profiles.json').read_text()).items() if 'captureWidth' in p}
assert 'Override' not in (shell('wm', 'size') + shell('wm', 'density')).decode(), \
    'Start from an emulator with no display overrides (adb shell wm size reset; wm density reset).'
adb('install', '-r', apk)
profile = None
try:
    shell('input', 'keyevent', 'KEYCODE_WAKEUP')
    shell('wm', 'dismiss-keyguard')
    shell('cmd', 'overlay', 'enable-exclusive', '--category', 'com.android.internal.systemui.navbar.gestural')
    shell('settings', 'put', 'system', 'font_scale', '1.0')
    shell('settings', 'put', 'global', 'sysui_demo_allowed', '1')
    shell('cmd', 'connectivity', 'airplane-mode', 'enable')  # fixtures only; nothing needs the network
    for profile, (width, height, density) in profiles.items():
        if args.profile and args.profile != profile:
            continue
        demo('exit')
        shell('wm', 'size', f'{width}x{height}')
        shell('wm', 'density', density)
        time.sleep(2)
        demo('enter')
        demo('clock', hhmm='0941')
        demo('notifications', visible='false')
        demo('battery', level=100, plugged='false')
        demo('network', wifi='show', level=4, fully='true', mobile='hide', airplane='hide')
        scenes()
finally:
    navigator.save()
    shell('am', 'force-stop', package)
    set_app_locale(adb, package, None)
    demo('exit')
    shell('cmd', 'connectivity', 'airplane-mode', 'disable')
    shell('wm', 'size', 'reset')
    shell('wm', 'density', 'reset')
