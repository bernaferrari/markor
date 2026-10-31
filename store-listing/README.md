# Wordkeep · Play listing

Built with [Screenshot Studio](studio/). Read `~/Documents/screenshot-studio/PLAYBOOK.md` first.

| Change | File |
| --- | --- |
| Cards, order, captures | `gallery.json` |
| Words per language | `<locale>/copy.json`, `<locale>/listing.json` |
| Look | `theme.css`, `theme.js` |
| Canvas and capture sizes | `profiles.json` |
| Scene tour | `capture-emulator.py` (build and runs: `studio.json` → `capture`) |

```sh
node store-listing/studio/recapture.cjs          # build, capture every size on emulator-5578, export
python3 store-listing/capture-emulator.py --profile phone --only home   # the scene tour alone (needs Pillow)
node store-listing/studio/server.cjs 8790        # editor
node store-listing/studio/export.cjs --locale en-US
```

The README's notebook spreads use the same real phone and tablet captures as the store listing, with separate `readme` layouts in `gallery.json`. Edit their titles, labels, and source scenes there; `theme.css` and `theme.js` control the paper, highlighter, and frames. To regenerate only the README images, run:

```sh
node store-listing/studio/export.cjs --locale en-US --kind readme --strict
```

The original phone and tablet store artwork has its own gallery entries and can still be exported independently.

## Play staging

See [the release guide](../docs/PLAY_STORE.md). Run `node store-listing/render-feature.cjs` to render the 1024 × 500 feature graphic, then `node store-listing/prepare-play.mjs` to stage validated translated text and English images. Set `PLAYWRIGHT_MODULE` and `SHARP_MODULE` if those modules are installed outside this project. Staging never uploads.
