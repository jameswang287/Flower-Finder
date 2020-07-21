package com.zoomers.flowerfinder.ui.main;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * A Model that contains many pages/fragments
 */
public class PageViewModel extends ViewModel {

    private MutableLiveData<Integer> mIndex = new MutableLiveData<>();

    /**
     * Sets the index of each page
     * @param index the index of the page
     */
    public void setIndex(int index) {
        mIndex.setValue(index);
    }

}