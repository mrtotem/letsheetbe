package com.mrtotem.letsheetbe;

class Note {

    enum NoteType {
        WHOLE,
        SEMI_BREVE,
        QUARTER,
        EIGHT,
        SIXTEENTH
    }

    int key;
    NoteType duration;

    Note(int key, float taptempo, float starts, float ends) {
        this.key = key;
        //this.duration = getDuration(taptempo, starts, ends);
    }

    private NoteType getDuration(float taptempo, float starts, float ends) {
        Math.floorMod((long) (taptempo), (long) (ends - starts));
        return NoteType.WHOLE;
    }
}
