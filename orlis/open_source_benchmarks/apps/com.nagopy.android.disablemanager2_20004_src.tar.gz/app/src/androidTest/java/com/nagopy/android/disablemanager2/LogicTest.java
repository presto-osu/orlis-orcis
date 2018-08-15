package com.nagopy.android.disablemanager2;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.nagopy.android.disablemanager2.support.Logic;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LogicTest {

    @SuppressWarnings("unchecked")
    @Test
    public void test_canLaunchImplicitIntent() throws Exception {
        {
            Context mockContext = mock(Context.class);
            PackageManager mockPackageManager = mock(PackageManager.class);
            when(mockPackageManager.queryIntentActivities(any(Intent.class), eq(PackageManager.MATCH_DEFAULT_ONLY)))
                    .thenReturn(Collections.<ResolveInfo>emptyList());
            when(mockContext.getPackageManager()).thenReturn(mockPackageManager);
            assertFalse(Logic.canLaunchImplicitIntent(mockContext, Intent.ACTION_SEND));
            assertFalse(Logic.canLaunchImplicitIntent(mockContext, mock(Intent.class)));
        }
        {
            Context mockContext = mock(Context.class);
            PackageManager mockPackageManager = mock(PackageManager.class);
            List<ResolveInfo> mockAppList = (List<ResolveInfo>) mock(List.class);
            when(mockAppList.isEmpty()).thenReturn(false);
            when(mockPackageManager.queryIntentActivities(any(Intent.class), eq(PackageManager.MATCH_DEFAULT_ONLY)))
                    .thenReturn(mockAppList);
            when(mockContext.getPackageManager()).thenReturn(mockPackageManager);
            assertTrue(Logic.canLaunchImplicitIntent(mockContext, Intent.ACTION_SEND));
            assertTrue(Logic.canLaunchImplicitIntent(mockContext, mock(Intent.class)));
        }
    }
}