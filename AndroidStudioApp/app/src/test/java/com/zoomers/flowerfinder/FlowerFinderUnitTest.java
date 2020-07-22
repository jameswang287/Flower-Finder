package com.zoomers.flowerfinder;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * A series of unit tests for FlowerFinder. These test are to be run after a minimum of one
 * detection; whether it be loading a picture or taking a picture.
 */
public class FlowerFinderUnitTest {
    /**
     * Test for if the directory is successfully created
     */
    @Test
    public void directoryValid() {
        MainActivity m = new  MainActivity();
        boolean pathOK = false;
        File pictureDir = new File("/storage/emulated/0/flowerfinder/pictures");
        if (pictureDir.exists()){
            pathOK = true;
        }
        assertTrue(pathOK);
    }

    /**
     * Test for if the directory is successfully created
     */
    @Test
    public void historyCreated() {
        MainActivity m = new  MainActivity();
        boolean historyStored = false;
        File hist = new File("/storage/emulated/0/flowerfinder/history.csv");
        if (hist.exists()){
            historyStored = true;
        }
        assertTrue(historyStored);
    }

}