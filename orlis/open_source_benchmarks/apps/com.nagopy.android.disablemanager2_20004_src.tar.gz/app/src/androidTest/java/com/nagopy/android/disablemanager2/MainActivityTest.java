package com.nagopy.android.disablemanager2;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * 設定画面のテストクラス.
 */
@SuppressWarnings("JUnit4AnnotatedMethodInJUnit3TestCase")
@RunWith(AndroidJUnit4.class)
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    /**
     * テスト対象Activity
     */
    private MainActivity activity;

    /**
     * コンストラクタ.
     */
    public MainActivityTest() {
        super(MainActivity.class);
    }


    /**
     * テスト実行前に実行されるメソッド.
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        activity = getActivity();
    }


    @Test
    public void test_exist() throws Exception {
        assertNotNull(activity.findViewById(R.id.toolbar));
        assertNotNull(activity.findViewById(R.id.indicator));
        assertNotNull(activity.findViewById(R.id.pager));
    }

}