# Gallery Mini

A companion photo gallery for Xiaomi Watch S4 (Xiaomi Vela), with an Android phone app.

## Planned features

- Material You-inspired interface, with English UI text.
- Photo grid and full-screen viewer.
- Swipe navigation between photos.
- A hard library limit of **50 items total** (still photos and Live Photo pairs count as one item each).
- Android-to-watch photo transfer with acknowledgements and bounded memory use.
- Live Photo-style motion, implemented as a still image plus a short animation when supported by the watch runtime.
- Delete items from the watch library.

## Repository layout

- `watch/` — Xiaomi Vela Quick App (RPK).
- `phone/` — Android companion app (APK).
- `.github/workflows/` — CI build workflows.
- `docs/` — protocol, UX, and implementation notes.

## Important implementation notes

The watch's actual image formats, animation support, storage API, and interconnect message limits must be verified against Xiaomi Vela documentation and on a physical Watch S4. Live Photos are not assumed to be natively supported: the Android app will prepare a compatible still image and, where practical, a short animated derivative.

Transfers should be stop-and-wait: send one bounded chunk, wait for the watch to confirm it was written, then continue. Do not buffer an entire photo library in watch memory.

## Status

Project initialized. Device compatibility and APK/RPK builds are not yet verified.
