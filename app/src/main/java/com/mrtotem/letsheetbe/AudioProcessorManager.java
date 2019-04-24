package com.mrtotem.letsheetbe;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
    private static final float PITCH_TOLERANCE = 4;
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
     * Note pitch
     */
    private Float pitch = -1f;
    /**
     * Tap tempo selected
     * 300 millis by default
     */
    private Float tapTempo = 300f;
    /**
     * notesFrequencies
     */
    private final static Map<Integer, Set<Float>> frequencies = new HashMap<>();
    /**
     * NotesThreadWorker
     */
    private Thread cNoteThread;
    private Thread aNoteThread;
    /**
     * concurred map with detected notes
     */
    private volatile ConcurrentHashMap<Integer, Long> detectedNotes = new ConcurrentHashMap<>();

    private Runnable cRunnable = () -> {
        boolean found = Objects.requireNonNull(frequencies.get(C)).stream().anyMatch(note ->
                note - PITCH_TOLERANCE < pitch &&
                        note + PITCH_TOLERANCE > pitch);
        if (found) {
            if (!detectedNotes.isEmpty()) {
                int lastElement = 0;

                for (Integer key : detectedNotes.keySet()) {
                    lastElement = key;
                }

                audioProcessorService.onNewNoteDetected(
                        new Note(
                                lastElement,
                                tapTempo,
                                detectedNotes.get(lastElement),
                                System.currentTimeMillis())
                );
            }
            detectedNotes.put(C, System.currentTimeMillis());
            Log.d(AudioProcessorManager.class.getName(), detectedNotes.toString());
        }
    };

    private Runnable aRunnable = () -> {
        boolean found = Objects.requireNonNull(frequencies.get(A)).stream().anyMatch(note ->
                note - PITCH_TOLERANCE < pitch &&
                        note + PITCH_TOLERANCE > pitch);
        if (found) {
            if (!detectedNotes.isEmpty()) {
                int lastElement = 0;

                for (Integer key : detectedNotes.keySet()) {
                    lastElement = key;
                }

                audioProcessorService.onNewNoteDetected(
                        new Note(
                                lastElement,
                                tapTempo,
                                detectedNotes.get(lastElement),
                                System.currentTimeMillis())
                );
            }
            detectedNotes.put(A, System.currentTimeMillis());
            Log.d(AudioProcessorManager.class.getName(), detectedNotes.toString());
        }
    };

    private AudioProcessorManager() {
    }

    static AudioProcessorManager getInstance() {
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
        aFreq.add(220f);
        aFreq.add(440f);
        frequencies.put(A, aFreq);
    }

    void startAudioCapture(AudioProcessorService listener) {
        audioProcessorService = Objects.requireNonNull(listener);
        cNoteThread = new Thread(cRunnable);
        aNoteThread = new Thread(aRunnable);
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
        //Log.d(AudioProcessorManager.class.getName(), String.valueOf(pitch));
        // starts note process behavior.
        // calculates the duration of the note and the note itself.
        processNote();
    }

    private void processNote() {
        cNoteThread.run();
        aNoteThread.run();
    }

    void setTapTempo(Float tapTempo) {
        this.tapTempo = tapTempo;
    }
}
