#!/usr/bin/env python3
"""Capture Wordkeep's real Android screens for the Play listing (needs adb and Pillow).

`node store-listing/studio/recapture.cjs` builds the storeCapture flavour, runs this tour for every device
size and exports. Run alone after `./gradlew :app:assembleFlavorDefaultStoreCapture`:
python3 store-listing/capture-emulator.py [--profile phone] [--locale de-DE] [--only home,editor]

The storeCapture build seeds fictional notes on first launch (app/src/storeCapture). The studio's
capture.py sets the display size and density per profile, System UI demo mode (9:41, full battery and
Wi-Fi), gesture navigation and the app language; this tour drives each scene and writes
<locale>/raw/<profile>/<scene>.png plus a .nodes.json layout sidecar.
"""
import sys
from pathlib import Path

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE / 'studio'))
from capture import Device, parser  # noqa: E402

args = parser(__doc__).parse_args()
apk = HERE.parent / 'app/build/outputs/apk/flavorDefault/storeCapture/app-flavorDefault-storeCapture.apk'
if not apk.exists():
    raise SystemExit('Build first: ./gradlew :app:assembleFlavorDefaultStoreCapture')
PALETTE = 'amber'
d = Device(args, 'com.bernaferrari.remarkor.storecapture', 'com.bernaferrari.remarkor.storecapture.StoreCaptureActivity', apk=apk)


def tap(label, key=None):
    d.tap(label, key=key, attempts=6, pause=1)


def launch(clear=False, **extras):
    d.launch(clear=clear, **extras)


def shot(name):
    d.shot(name, settle=1)


def back():
    d.back(1)


def scenes():
    phone = d.profile == 'phone'
    # A fresh install shows the real intro; the storage choice is the local-first promise.
    launch(clear=True, seed=False)
    tap('Get started')
    shot('storage')

    launch(clear=True, palette=PALETTE)  # seeds the fictional notebook
    launch(palette=PALETTE)
    shot('home')

    tap('Lisbon in May')
    shot('editor')
    tap('Preview')
    shot('preview')
    tap('Color')
    shot('color')
    back()
    tap('More options')
    tap('Document Outline')
    shot('outline')
    back()
    back()
    if not phone:
        back()  # tablets show the editor as a dialog; make sure it is closed

    launch(palette=PALETTE)
    tap('todo')
    tap('Preview')
    shot('todo')

    launch(palette=PALETTE)
    tap('Journal')
    shot('journal')

    # Search runs through every note, folders included, and marks each match.
    launch(palette=PALETTE)
    tap('Search notes')
    d.type('coffee', wait=2)
    back()  # hide the keyboard; the results stay
    shot('search')

    # The same notebook as a list: real file names, extensions and folders.
    launch(palette=PALETTE)
    tap('Switch to list view')
    shot('files')

    launch(palette=PALETTE)
    tap('Settings')
    shot('settings')

    launch(theme='dark', palette=PALETTE)
    shot('dark')
    tap('Lisbon in May', key='dark|Lisbon in May')
    shot('dark-editor')


with d:
    for profile in d.profiles():
        d.scope = profile  # taps are keyed by profile (the dark scene gives its own key)
        scenes()
