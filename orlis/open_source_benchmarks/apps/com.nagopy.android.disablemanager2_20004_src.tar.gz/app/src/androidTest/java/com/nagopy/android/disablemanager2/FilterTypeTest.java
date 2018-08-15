package com.nagopy.android.disablemanager2;


import org.junit.Test;
import org.mockito.Mockito;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class FilterTypeTest {

    @Test
    public void test_indexOf() throws Exception {
        assertEquals(FilterType.DISABLABLE, FilterType.indexOf(0));
        assertEquals(FilterType.DISABLED, FilterType.indexOf(1));
        assertEquals(FilterType.UNDISABLABLE, FilterType.indexOf(2));
        assertEquals(FilterType.USER, FilterType.indexOf(3));
    }

    @Test
    public void test_filter() throws Exception {
        {
            // 無効になっているユーザーアプリ（rootedならありうる）
            AppData appData = Mockito.mock(AppData.class);
            appData.isEnabled = false;
            appData.isSystem = false;
            appData.isDisableable = false;

            assertFalse(FilterType.DISABLABLE.isTarget(appData));
            assertFalse(FilterType.DISABLED.isTarget(appData));
            assertFalse(FilterType.UNDISABLABLE.isTarget(appData));
            assertTrue(FilterType.USER.isTarget(appData));
        }
        {
            // ユーザーアプリ
            AppData appData = Mockito.mock(AppData.class);
            appData.isEnabled = true;
            appData.isSystem = false;
            appData.isDisableable = false;

            assertFalse(FilterType.DISABLABLE.isTarget(appData));
            assertFalse(FilterType.DISABLED.isTarget(appData));
            assertFalse(FilterType.UNDISABLABLE.isTarget(appData));
            assertTrue(FilterType.USER.isTarget(appData));
        }
        {
            // 無効にできないはずだけど無効になっているシステムアプリ（rootedなど？）
            AppData appData = Mockito.mock(AppData.class);
            appData.isEnabled = false;
            appData.isSystem = true;
            appData.isDisableable = false;

            assertFalse(FilterType.DISABLABLE.isTarget(appData));
            assertTrue(FilterType.DISABLED.isTarget(appData));
            assertFalse(FilterType.UNDISABLABLE.isTarget(appData));
            assertFalse(FilterType.USER.isTarget(appData));
        }
        {
            // 無効にできるけどシステムアプリではない（通常ありえない）
            AppData appData = Mockito.mock(AppData.class);
            appData.isEnabled = false;
            appData.isSystem = false;
            appData.isDisableable = true;

            assertFalse(FilterType.DISABLABLE.isTarget(appData));
            assertFalse(FilterType.DISABLED.isTarget(appData));
            assertFalse(FilterType.UNDISABLABLE.isTarget(appData));
            assertTrue(FilterType.USER.isTarget(appData));
        }
        {
            //無効化できないシステムアプリ
            AppData appData = Mockito.mock(AppData.class);
            appData.isEnabled = true;
            appData.isSystem = true;
            appData.isDisableable = false;

            assertFalse(FilterType.DISABLABLE.isTarget(appData));
            assertFalse(FilterType.DISABLED.isTarget(appData));
            assertTrue(FilterType.UNDISABLABLE.isTarget(appData));
            assertFalse(FilterType.USER.isTarget(appData));
        }
        {
            // 無効化できる非システムアプリ（通常ありえない）
            AppData appData = Mockito.mock(AppData.class);
            appData.isEnabled = true;
            appData.isSystem = false;
            appData.isDisableable = true;

            assertFalse(FilterType.DISABLABLE.isTarget(appData));
            assertFalse(FilterType.DISABLED.isTarget(appData));
            assertFalse(FilterType.UNDISABLABLE.isTarget(appData));
            assertTrue(FilterType.USER.isTarget(appData));
        }
        {
            // 無効化されたアプリ
            AppData appData = Mockito.mock(AppData.class);
            appData.isEnabled = false;
            appData.isSystem = true;
            appData.isDisableable = true;

            assertFalse(FilterType.DISABLABLE.isTarget(appData));
            assertTrue(FilterType.DISABLED.isTarget(appData));
            assertFalse(FilterType.UNDISABLABLE.isTarget(appData));
            assertFalse(FilterType.USER.isTarget(appData));
        }
        {
            // 無効化可能アプリ
            AppData appData = Mockito.mock(AppData.class);
            appData.isEnabled = true;
            appData.isSystem = true;
            appData.isDisableable = true;

            assertTrue(FilterType.DISABLABLE.isTarget(appData));
            assertFalse(FilterType.DISABLED.isTarget(appData));
            assertFalse(FilterType.UNDISABLABLE.isTarget(appData));
            assertFalse(FilterType.USER.isTarget(appData));
        }
    }
}
