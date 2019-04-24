package com.mrtotem.letsheetbe;

import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import io.reactivex.internal.schedulers.NewThreadWorker;

class AudioProcessorManager {

    private static AudioProcessorManager instance;
    /**
     * FREQUENCY
     */
    private static final int FREQUENCY = 44100; //22050;
    /**
     * BUFFER_SIZE
     */
    private static final int BUFFER_SIZE = 2048;
    /**
     * BUFFER_SIZE
     */
    private static final float PITCH_TOLERANCE = 10;
    /**
     * note keys
     */
    private static final int C = 0;
    private static final int CD = 1;
    private static final int D = 2;
    private static final int DE = 3;
    private static final int E = 4;
    private static final int F = 5;
    private static final int FG = 6;
    private static final int G = 7;
    private static final int GA = 8;
    private static final int A = 9;
    private static final int AB = 10;
    private static final int B = 11;

    /**
     * flags
     */
    private static boolean flagC = true;
    private static boolean flagCD = true;
    private static boolean flagD = true;
    private static boolean flagDE = true;
    private static boolean flagE = true;
    private static boolean flagF = true;
    private static boolean flagFG = true;
    private static boolean flagG = true;
    private static boolean flagGA = true;
    private static boolean flagA = true;
    private static boolean flagAB = true;
    private static boolean flagB = true;

    /**
     * audioProcessorService
     */
    private AudioProcessorService audioProcessorService;
    /**
     * pitchDetectionHandler
     */
    private PitchDetectionHandler pitchDetectionHandler = this::processAudioResource;
    /**
     * audioDispatcher
     */
    private AudioDispatcher audioDispatcher = AudioDispatcherFactory.fromDefaultMicrophone(FREQUENCY, BUFFER_SIZE, 0);
    /**
     * audioProcessor
     */
    private AudioProcessor audioProcessor = new PitchProcessor(
            PitchProcessor.PitchEstimationAlgorithm.FFT_YIN,
            FREQUENCY,
            BUFFER_SIZE,
            pitchDetectionHandler);
    /**
     * audioThreadWorker
     */
    private NewThreadWorker audioThreadWorker = new NewThreadWorker(Thread::new);
    /**
     *
     */
    private Float pitch = -1f;
    /**
     * detectedNotes
     */
    private final List<String> detectedNotes = new ArrayList<>();

    /**
     * notesFrequencies
     */
    private final static Map<Integer, Set<Float>> frequencies = new HashMap<>();

    /**
     * NotesThreadWorker
     */
    private Thread cNoteThreadWorker;
    private Thread aNoteThreadWorker;

    private Runnable cRunnable = () -> Objects.requireNonNull(frequencies.get(C)).stream()
            .filter(c -> c - PITCH_TOLERANCE < pitch &&
                    c + PITCH_TOLERANCE > pitch)
            .forEach(s -> {
                Log.d("PITCH", "is writing? " + flagC);
                synchronized (cNoteThreadWorker) {
                    try {
                        Log.d("PITCH", "It's a C!!");
                        detectedNotes.add("C");
                        audioProcessorService.onNewNoteDetected(detectedNotes);
                        cNoteThreadWorker.wait(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });

    private Runnable aRunnable = () -> Objects.requireNonNull(frequencies.get(A)).stream()
            .filter(a -> a - PITCH_TOLERANCE < pitch &&
                    a + PITCH_TOLERANCE > pitch)
            .forEach(s -> {
                Log.d("PITCH", "is writing? " + flagA);
                if (flagA) {
                    Log.d("PITCH", "It's an A!!");
                    detectedNotes.add("A");
                    audioProcessorService.onNewNoteDetected(detectedNotes);
                    stopWriting(A);
                }
            });

    private AudioProcessorManager() {
    }

    public static AudioProcessorManager getInstance() {
        if (instance == null) {
            instance = new AudioProcessorManager();
            fillNotesFrequenciesTable();
        }
        return instance;
    }

    private static void fillNotesFrequenciesTable() {
        Set<Float> cFreq = new ConcurrentSkipListSet<>();
        cFreq.add(261.626f);
        cFreq.add(523.251f);
        frequencies.put(C, cFreq);

        Set<Float> aFreq = new ConcurrentSkipListSet<>();
        aFreq.add(440f);
        aFreq.add(880f);
        frequencies.put(A, aFreq);
    }

    void startAudioCapture(AudioProcessorService listener) {
        audioProcessorService = Objects.requireNonNull(listener);
        cNoteThreadWorker = new Thread(cRunnable);
        aNoteThreadWorker = new Thread(aRunnable);
        audioDispatcher.addAudioProcessor(audioProcessor);
        audioThreadWorker.schedule(() -> audioDispatcher.run());
    }

    void stopAudioCapture() {
        audioDispatcher.stop();
        audioDispatcher.removeAudioProcessor(audioProcessor);
        audioThreadWorker.shutdown();
    }

    private void processAudioResource(PitchDetectionResult pitchDetectionResult, AudioEvent audioEvent) {
        pitch = pitchDetectionResult.getPitch();
        findNote();
    }

    private void findNote() {
        cNoteThreadWorker.run();
        aNoteThreadWorker.run();
    }

    private void stopWriting(int key) {
        switch (key) {
            case C: {
                flagC = false;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        flagC = true;
                        cNoteThreadWorker.notifyAll();
                    }
                }, 500);
            }
            break;
            case A: {
                flagA = false;
                try {
                    new Thread(() -> flagA = true).join(500);
                } catch (InterruptedException e) {
                    Log.e(AudioProcessorManager.class.getName(), e.getMessage());
                }
            }
            break;
        }
    }
}
