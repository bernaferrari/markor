# Wordkeep · Play listing

Built with [Screenshot Studio](studio/). Read `~/Documents/screenshot-studio/PLAYBOOK.md` first.

| Change | File |
| --- | --- |
| Cards, order, captures | `gallery.json` |
| Words per language | `<locale>/copy.json`, `<locale>/listing.json` |
| Look | `theme.css`, `theme.js` |
| Canvas and capture sizes | `profiles.json` |

```sh
node store-listing/studio/server.cjs 8790        # editor
node store-listing/studio/export.cjs --locale en-US
```
