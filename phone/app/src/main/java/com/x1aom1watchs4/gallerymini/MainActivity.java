package com.x1aom1watchs4.gallerymini;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public final class MainActivity extends Activity {
    private static final int MAX_PHOTOS = 50;
    private static final int PICK_PHOTOS = 1001;
    private final ArrayList<Uri> selectedPhotos = new ArrayList<>();
    private TextView status;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(28, 36, 28, 28);
        root.setBackgroundColor(0xFFF4F1FA);

        TextView title = new TextView(this);
        title.setText("Gallery Mini");
        title.setTextSize(28);
        title.setTextColor(0xFF191B22);
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Select up to 50 photos for your Xiaomi Watch S4.");
        subtitle.setTextSize(15);
        subtitle.setTextColor(0xFF444653);
        root.addView(subtitle);

        status = new TextView(this);
        status.setText("0 / 50 photos selected");
        status.setTextSize(16);
        status.setPadding(0, 24, 0, 18);
        root.addView(status);

        Button choose = new Button(this);
        choose.setText("SELECT PHOTOS");
        choose.setOnClickListener(v -> openPhotoPicker());
        root.addView(choose);

        Button send = new Button(this);
        send.setText("SEND TO WATCH");
        send.setOnClickListener(v -> status.setText(
            selectedPhotos.isEmpty()
                ? "Select photos first."
                : "Photos selected. Watch connection and acknowledged transfer are being added."
        ));
        root.addView(send);

        TextView note = new TextView(this);
        note.setText("Live Photo motion support will be enabled after verifying compatible Vela image/animation formats.");
        note.setTextSize(12);
        note.setTextColor(0xFF5C5E6A);
        note.setGravity(Gravity.CENTER_VERTICAL);
        note.setPadding(0, 24, 0, 0);
        root.addView(note);

        setContentView(root);
    }

    private void openPhotoPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICK_PHOTOS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PICK_PHOTOS || resultCode != RESULT_OK || data == null) return;

        selectedPhotos.clear();
        if (data.getClipData() != null) {
            int count = Math.min(data.getClipData().getItemCount(), MAX_PHOTOS);
            for (int i = 0; i < count; i++) {
                Uri uri = data.getClipData().getItemAt(i).getUri();
                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                selectedPhotos.add(uri);
            }
        } else if (data.getData() != null) {
            Uri uri = data.getData();
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            selectedPhotos.add(uri);
        }
        status.setText(selectedPhotos.size() + " / " + MAX_PHOTOS + " photos selected");
    }
}