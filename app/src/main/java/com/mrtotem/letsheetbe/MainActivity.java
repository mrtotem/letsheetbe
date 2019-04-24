package com.mrtotem.letsheetbe;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements MainMvp {

    private static final int AUDIO_PERMISSION = 101;

    @BindView(R.id.notes_sheet)
    TextView notesSheet;

    @BindView(R.id.tap_tempo_label)
    TextView tapTempo;

    @BindView(R.id.tap_tempo_button)
    Button tapTempoButton;

    @BindView(R.id.start_listening_button)
    Button startAudioListening;

    @BindView(R.id.end_listening_button)
    Button endAudioListening;

    MainPresenter presenter;
    String sheetMusic = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        startAudioListening.setOnClickListener(view -> {
            Snackbar.make(getWindow().getDecorView().getRootView(), "Recording..", Snackbar.LENGTH_SHORT).show();
            getPresenter().startRecording();
        });
        endAudioListening.setOnClickListener(view -> {
            Snackbar.make(getWindow().getDecorView().getRootView(), "Stopped", Snackbar.LENGTH_SHORT).show();
            getPresenter().stopRecording();
        });

        tapTempoButton.setOnClickListener(v -> {
            Snackbar.make(getWindow().getDecorView().getRootView(), "Tap starts..", Snackbar.LENGTH_SHORT).show();
            getPresenter().startTapTempo();
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

    @Override
    public MainPresenter getPresenter() {
        if (presenter == null) {
            presenter = new MainPresenter();
            presenter.onAttach(this);
        }
        return presenter;
    }

    @Override
    public void showTapTempo(String tempo) {
        Snackbar.make(getWindow().getDecorView().getRootView(), "Tap ends..", Snackbar.LENGTH_SHORT).show();
        tapTempo.setText(String.format(Locale.getDefault(), "%.2s millis", tempo));
    }

    @Override
    public void showNewNote(String note) {
        sheetMusic = sheetMusic.concat(" " + note);
        notesSheet.setText(sheetMusic);
    }
}
