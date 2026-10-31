# Wordkeep Play Store release

Package: `com.bernaferrari.remarkor`. Default language: English (United States). Category: Productivity. App type: app; pricing: free. The current Android release is 0.1 (version code 1).

## Listing assets

`store-listing/en-US/listing.json` is the default listing. The other locales listed in `store-listing/studio.json` have translated title, short description and full description. Current localized screenshots use English app captures; upload English artwork only and let Play fall back to it for translated listings until localized captures have been reviewed.

The default English artwork includes eight phone images and five images for each tablet size. Tablet images are fitted onto Play-compatible 9:16 or 16:9 canvases without cropping. The icon is `store-listing/brand/icon.png`; the feature graphic is `store-listing/en-US/feature-graphic.png`, rendered from `feature-graphic.html`.

```sh
PLAYWRIGHT_MODULE=/path/to/playwright node store-listing/studio/export.cjs --locale en-US --strict
PLAYWRIGHT_MODULE=/path/to/playwright node store-listing/render-feature.cjs
SHARP_MODULE=/path/to/sharp node store-listing/prepare-play.mjs
```

The module overrides can be omitted if the dependencies resolve locally. Staging validates text limits, image dimensions and counts, and writes ignored `store-listing/play-staging/`; it never uploads. Inspect artwork before upload. Screenshot Studio itself is synced from the shared master; edit the app's theme and gallery, not `studio/`.

## Privacy and declarations

Privacy URL after pushing this policy: `https://github.com/bernaferrari/markor/blob/main/PRIVACY.md`. Contact: `bernaferrari2@gmail.com`. The same policy is linked from Settings → About.

Evidence for the declarations:

- No account, advertising SDK, advertising ID, developer analytics or developer-operated note collection is implemented. Notes and settings are processed locally.
- Private storage requires no shared-storage permission. Shared storage uses all-files access on Android 11+ and legacy storage permission on Android 10 and below. The unused APK-install permission has been removed.
- Users can send text/files to other apps, open links and use compatible Android App Functions integrations. Those external apps/services have their own policies. Do not claim the app has no Internet permission.
- Data safety answers must match the shipped dependencies and behavior. Device-local processing is distinct from collection by a developer; user-directed sharing must be evaluated using Play's current questionnaire and exemptions.
- Notes are private user-created text. The app has no built-in social network, user-to-user messaging service, ads, purchases, financial services, health functionality or government affiliation. Complete the content-rating questionnaire from the actual app behavior; do not reuse another app's answers.

### All files access

The bundle declares `MANAGE_EXTERNAL_STORAGE`. Google requires a permissions declaration and approval for this permission. Wordkeep's shared-notebook feature reads, edits, searches and organizes plain Markdown/text documents in real folders; the declaration must describe that document-management functionality accurately. App-private storage is also available. Do not claim that all-files access is mandatory for the private notebook, or that a narrower storage API is technically impossible. Google may require a demonstration video or a change to Storage Access Framework before approving the release.

Policy references: [All files access](https://support.google.com/googleplay/android-developer/answer/10467955), [User data and privacy policy](https://support.google.com/googleplay/android-developer/answer/10144311).

## Signing and final release

Signing credentials are local and excluded from Git. See DEVELOPMENT.md for the signed AAB build command. Verify the AAB signature and merged release permissions before uploading it. Never upload a capture build or the test flavor.

Creating the Console app requires the account owner's confirmation of Developer Program Policies and US export compliance. App signing, permissions review, content rating, Data safety, target audience, country availability and the final production submission must be verified in Console before calling the app ready. Build success alone does not establish policy approval or publication.

## Console staging status (2026-10-30)

Wordkeep has been created in Play Console (app ID `4975952302249452990`). All 46 translated listings and the 20 English images (icon, feature graphic, 18 screenshots) were committed and verified through a fresh API read, including SHA-256 checks. The signed 0.1 / version code 1 bundle was uploaded and its original hash verified. Production release, English/Portuguese release notes, 177 countries plus rest of world, category and app-content declarations are saved in Publishing overview.

Nothing has been submitted for review or published. The current publishing blocker is the content-rating questionnaire: accepting the separate IARC Terms of Service requires the account owner's confirmation. All-files-access declaration is saved truthfully, without a demo-video link; saving it is not Google policy approval. Release validation also recommends native debug symbols. See `store-listing/upload-status.json` for the recorded state.

## Review submission (2026-10-31)

With the account owner's authorization, IARC terms were accepted and the content-rating questionnaire completed. Ratings include Everyone (ESRB), PEGI 3, and All ages (Brazil). All 56 changes, including production release 0.1, were submitted. Play Console shows **Changes in review**, with automatic quick checks running before Google review. Managed publishing is off. The app is not yet confirmed public; Google policy review, including all-files access, remains pending. The earlier content-rating authorization blocker above is resolved.
