package com.x1aom1watchs4.gallerymini;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public final class MainActivity extends Activity {
    private static final int MAX_PHOTOS = 50;
    private static final int PICK_PHOTOS = 1001;
    private static final int MAX_EDGE = 1024;
    private static final int JPEG_QUALITY = 82;

    private final ArrayList<Uri> selectedPhotos = new ArrayList<>();
    private LinearLayout previews;
    private TextView status;
    private TextView connection;
    private Button sendButton;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        buildUi();
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(28, 32, 28, 32);
        root.setBackgroundColor(0xFFF4F1FA);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("Gallery Mini");
        title.setTextSize(28);
        title.setTextColor(0xFF191B22);
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("A pocket-sized photo gallery for Xiaomi Watch S4");
        subtitle.setTextSize(14);
        subtitle.setTextColor(0xFF444653);
        root.addView(subtitle);

        connection = new TextView(this);
        connection.setText("WATCH CONNECTION · SDK NOT CONFIGURED");
        connection.setTextSize(12);
        connection.setTextColor(0xFF6750A4);
        connection.setPadding(0, 20, 0, 8);
        root.addView(connection);

        status = new TextView(this);
        status.setText("0 / 50 photos selected");
        status.setTextSize(16);
        status.setTextColor(0xFF191B22);
        status.setPadding(0, 12, 0, 16);
        root.addView(status);

        Button choose = new Button(this);
        choose.setText("SELECT PHOTOS");
        choose.setOnClickListener(v -> openPhotoPicker());
        root.addView(choose);

        sendButton = new Button(this);
        sendButton.setText("PREPARE PHOTOS");
        sendButton.setEnabled(false);
        sendButton.setOnClickListener(v -> preparePhotos());
        root.addView(sendButton);

        previews = new LinearLayout(this);
        previews.setOrientation(LinearLayout.VERTICAL);
        previews.setPadding(0, 12, 0, 0);
        root.addView(previews);

        TextView note = new TextView(this);
        note.setText("Photos are selected through Android's system picker. The companion currently prepares JPEG copies; watch transfer will be enabled when the Xiaomi XMS SDK is supplied to the build.");
        note.setTextSize(12);
        note.setTextColor(0xFF5C5E6A);
        note.setGravity(Gravity.CENTER_VERTICAL);
        note.setPadding(0, 24, 0, 0);
        root.addView(note);

        setContentView(scroll);
    }

    private void openPhotoPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, PICK_PHOTOS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PICK_PHOTOS || resultCode != RESULT_OK || data == null) return;

        selectedPhotos.clear();
        if (data.getClipData() != null) {
            int count = Math.min(data.getClipData().getItemCount(), MAX_PHOTOS);
            for (int i = 0; i < count; i++) addSelected(data.getClipData().getItemAt(i).getUri(), data);
        } else if (data.getData() != null) {
            addSelected(data.getData(), data);
        }
        updateSelection();
    }

    private void addSelected(Uri uri, Intent result) {
        if (uri == null || selectedPhotos.contains(uri) || selectedPhotos.size() >= MAX_PHOTOS) return;
        try {
            getContentResolver().takePersistableUriPermission(uri,
                result.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (SecurityException ignored) {
            // The temporary picker grant remains valid for this activity session.
        }
        selectedPhotos.add(uri);
    }

    private void updateSelection() {
        status.setText(selectedPhotos.size() + " / " + MAX_PHOTOS + " photos selected");
        sendButton.setEnabled(!selectedPhotos.isEmpty());
        previews.removeAllViews();
        int shown = Math.min(selectedPhotos.size(), 8);
        for (int i = 0; i < shown; i++) {
            ImageView image = new ImageView(this);
            image.setImageURI(selectedPhotos.get(i));
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(112, 112);
            params.setMargins(0, 0, 8, 8);
            previews.addView(image, params);
        }
        if (selectedPhotos.size() > shown) {
            TextView more = new TextView(this);
            more.setText("+" + (selectedPhotos.size() - shown) + " more");
            previews.addView(more);
        }
    }

    private void preparePhotos() {
        sendButton.setEnabled(false);
        status.setText("Preparing JPEG previews…");
        new Thread(() -> {
            int prepared = 0;
            long totalBytes = 0;
            for (Uri uri : new ArrayList<>(selectedPhotos)) {
                try (InputStream input = getContentResolver().openInputStream(uri)) {
                    if (input == null) continue;
                    BitmapFactory.Options bounds = new BitmapFactory.Options();
                    bounds.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream(input, null, bounds);
                    int sample = 1;
                    while (Math.max(bounds.outWidth / sample, bounds.outHeight / sample) > MAX_EDGE * 2) sample *= 2;
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = sample;
                    try (InputStream second = getContentResolver().openInputStream(uri);
                         ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                        if (second == null) continue;
                        Bitmap bitmap = BitmapFactory.decodeStream(second, null, options);
                        if (bitmap == null) continue;
                        Bitmap scaled = bitmap;
                        int edge = Math.max(bitmap.getWidth(), bitmap.getHeight());
                        if (edge > MAX_EDGE) {
                            float factor = (float) MAX_EDGE / edge;
                            scaled = Bitmap.createScaledBitmap(bitmap,
                                Math.max(1, Math.round(bitmap.getWidth() * factor)),
                                Math.max(1, Math.round(bitmap.getHeight() * factor)), true);
                            if (scaled != bitmap) bitmap.recycle();
                        }
                        scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output);
                        totalBytes += output.size();
                        prepared++;
                        scaled.recycle();
                    }
                } catch (Exception ignored) {
                    // Skip unreadable or unsupported items and continue preparing the library.
                }
            }
            final int count = prepared;
            final long bytes = totalBytes;
            runOnUiThread(() -> {
                status.setText("Prepared " + count + " / " + selectedPhotos.size()
                    + " JPEG previews · " + (bytes / 1024) + " KB total. Watch transfer is not connected yet.");
                sendButton.setEnabled(!selectedPhotos.isEmpty());
            });
        }).start();
    }
}
