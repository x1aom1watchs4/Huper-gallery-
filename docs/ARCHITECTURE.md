# Gallery Mini architecture

## Components

### Android companion (phone/)
- Uses Android's system photo picker / document picker.
- Reads selected images through ContentResolver.
- Creates thumbnails and normalizes image orientation before transfer.
- For motion photos, keeps the original paired asset where available and creates a short compatible animation derivative only after confirming the watch supports that format.
- Enforces the 50-item library limit and reports capacity to the watch.
- Transfers files in small chunks with a stop-and-wait acknowledgement protocol.

### Xiaomi Vela app (watch/)
- Displays a Material You-inspired dark/light gallery UI using English labels.
- Shows a thumbnail grid, full-screen image viewer, and swipe navigation.
- Stores received files incrementally; it must not queue whole files or whole libraries in RAM.
- Counts each still photo or Live Photo pair as one library item.
- Provides clear progress, error, and storage-full states.

## Transfer protocol (v1 draft)

Every message is JSON:

```json
{ "v": 1, "type": "photo.transfer.start", "payload": {} }
```

Planned message types:
- `photo.library.request`
- `photo.library.state`
- `photo.transfer.start`
- `photo.transfer.chunk`
- `photo.transfer.ack`
- `photo.transfer.finish`
- `photo.transfer.complete`
- `photo.transfer.error`
- `photo.delete`

A chunk is acknowledged only after the watch reports a successful storage write. The sender must not send the next chunk before receiving the matching acknowledgement. Message sizes and storage APIs must be checked against the actual XMS and Vela SDK contracts before implementation.

## Limits

- Maximum library entries: 50.
- Still photo + its motion component counts as one entry.
- File-size and pixel-dimension limits will be selected after testing on Watch S4 to avoid memory pressure.
- Initial implementation should use a conservative thumbnail size and load full-resolution images only in the viewer.

## Live Photo compatibility

Do not assume Xiaomi Vela can play Apple's Live Photo container or arbitrary GIF files. The first implementation should store:
1. a normalized still image;
2. an optional short animation derivative, if a tested Vela-supported animation format is available.

If animation playback is unavailable, the still image remains usable and the UI should label the item as a motion photo without pretending it animates.
