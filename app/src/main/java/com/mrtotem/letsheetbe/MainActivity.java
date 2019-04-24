package com.mrtotem.letsheetbe;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import butterknife.BindView;
import butterknife.ButterKnife;

class MainActivity extends AppCompatActivity {

    private static final int AUDIO_PERMISSION = 101;

    @BindView(R.id.notes_sheet)
    TextView notesSheet;

    @BindView(R.id.start_listening_button)
    Button startAudioListening;

    @BindView(R.id.end_listening_button)
    Button endAudioListening;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        startAudioListening.setOnClickListener(view -> {
            Snackbar.make(view, "Recording..", Snackbar.LENGTH_SHORT).show();
            AudioProcessorManager.getInstance().startAudioCapture(notes -> {
                StringBuilder text = new StringBuilder();
                for (String note : notes) {
                    text.append(String.format("%s ", note));
                }
                notesSheet.setText(text.toString());
            });
        });
        endAudioListening.setOnClickListener(view -> {
            Snackbar.make(view, "Stopped", Snackbar.LENGTH_SHORT).show();
            AudioProcessorManager.getInstance().stopAudioCapture();
        });

        requestAudioPermission();
    }

    private void requestAudioPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {

            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        AUDIO_PERMISSION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
