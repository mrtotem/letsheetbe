package com.mrtotem.letsheetbe;

public class BasePresenter<T> {

    T view;

    void onAttach(T view){
        this.view = view;
    }

    void onDeAttach(){
        view = null;
    }
}
