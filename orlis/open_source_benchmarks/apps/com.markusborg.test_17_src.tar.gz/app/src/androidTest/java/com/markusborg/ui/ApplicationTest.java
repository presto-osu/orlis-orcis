package com.markusborg.ui;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.markusborg.logic.CourtPosition;

import junit.framework.Assert;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    public void testCourtPosition_L_BACK(){
        CourtPosition testPos = new CourtPosition(CourtPosition.L_BACK);
        Assert.assertEquals(testPos.getPosition(), CourtPosition.L_BACK);
        Assert.assertTrue(testPos.isCornerPos());
    }
}