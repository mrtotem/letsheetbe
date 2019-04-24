package com.mrtotem.letsheetbe;

import java.util.concurrent.atomic.AtomicLong;

public class MainPresenter extends BasePresenter<MainMvp> {

    private boolean tapTempoPressed = false;
    private AtomicLong start = new AtomicLong();
    private AtomicLong end = new AtomicLong();

    void startTapTempo() {
        if (!tapTempoPressed) {
            tapTempoPressed = true;
            start.set(System.currentTimeMillis());
        } else {
            tapTempoPressed = false;
            end.set(System.currentTimeMillis());
            view.showTapTempo(String.valueOf(end.get() - start.get()));
            AudioProcessorManager.getInstance().setTapTempo((float) (end.get() - start.get()));
        }
    }

    void startRecording(){
        AudioProcessorManager.getInstance().startAudioCapture(note -> view.showNewNote(String.valueOf(note.key)));
    }

    void stopRecording(){
        AudioProcessorManager.getInstance().stopAudioCapture();
    }
}
