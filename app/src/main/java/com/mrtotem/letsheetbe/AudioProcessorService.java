package com.mrtotem.letsheetbe;

import java.util.List;

interface AudioProcessorService {

    void onNewNoteDetected(List<String> notes);
}
