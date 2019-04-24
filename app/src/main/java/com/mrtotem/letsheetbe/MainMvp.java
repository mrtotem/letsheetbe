package com.mrtotem.letsheetbe;

public interface MainMvp {

    MainPresenter getPresenter();

    void showTapTempo(String tempo);

    void showNewNote(String note);
}
