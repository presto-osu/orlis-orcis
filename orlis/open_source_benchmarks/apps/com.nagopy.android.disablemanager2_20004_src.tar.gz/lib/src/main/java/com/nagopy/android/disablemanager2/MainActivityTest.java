package com.nagopy.android.disablemanager2;

import android.widget.TextView;

import com.android.uiautomator.core.Configurator;
import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiScrollable;
import com.android.uiautomator.core.UiSelector;
import com.android.uiautomator.testrunner.UiAutomatorTestCase;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

// Import the uiautomator libraries

public class MainActivityTest extends UiAutomatorTestCase {


    public void testDemo() throws Exception {
        Configurator configurator = Configurator.getInstance();
        System.out.println(configurator.getWaitForSelectorTimeout());
        configurator.setWaitForSelectorTimeout(1000);

        System.out.println(this.getClass().getName() + " start");
        getUiDevice().pressHome();

        UiObject allAppsButton = new UiObject(new UiSelector().description("アプリ"));
        allAppsButton.clickAndWaitForNewWindow();

        UiScrollable appViews = new UiScrollable(new UiSelector().scrollable(true));
        appViews.setAsHorizontalList();
        UiObject settingsApp = appViews.getChildByText(
                new UiSelector().className(android.widget.TextView.class.getName()),
                "無効化マネージャー");
        settingsApp.clickAndWaitForNewWindow();

        UiObject settingsValidation = new UiObject(new UiSelector().packageName("com.nagopy.android.disablemanager2"));
        assertTrue("アプリ起動に失敗", settingsValidation.exists());

        UiScrollable listView = new UiScrollable(new UiSelector().scrollable(true));
        appViews.setAsVerticalList();
        listView.scrollToBeginning(10);
        Set<String> testedPackages = new HashSet<String>();

        String ssDir = "/data/local/tmp/ss/" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        new File(ssDir).mkdirs();

        do {
            for (int i = 0; ; i++) {
                try {
                    UiObject clickable = listView.getChild(new UiSelector().clickable(true).index(i));
                    UiObject item = clickable.getChild(new UiSelector().className(TextView.class)
                            .resourceId("com.nagopy.android.disablemanager2:id/list_title"));
                    System.out.println(item.getText());
                    String packageName = clickable.getChild(new UiSelector().className(TextView.class)
                            .resourceId("com.nagopy.android.disablemanager2:id/list_package_name")).getText();
                    System.out.println(packageName);
                    if (testedPackages.contains(packageName)) {
                        continue;
                    }
                    testedPackages.add(packageName);
                    item.clickAndWaitForNewWindow();
                    Thread.sleep(1000); // ちょっとWaitしてちゃんとSSに映るようにする
                    File ss = new File(ssDir, packageName + ".jpg");
                    System.out.println("takeScreenshot:" + getUiDevice().takeScreenshot(ss));

                    UiObject uninstallButton = new UiObject(new UiSelector().resourceId("com.android.settings:id/right_button").text("無効にする"));
                    System.out.println(uninstallButton.exists());
                    if(uninstallButton.exists()){return;}

                    getUiDevice().pressBack();
                } catch (UiObjectNotFoundException e) {
                    break;
                }
            }
        } while (listView.scrollForward());
        System.out.println(this.getClass().getName() + " finish");
    }

}
