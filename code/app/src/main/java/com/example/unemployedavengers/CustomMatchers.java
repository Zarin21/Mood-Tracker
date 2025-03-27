package com.example.unemployedavengers;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;
import androidx.test.espresso.matcher.BoundedMatcher;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class CustomMatchers {
    public static void handleLocationPermissionPopup(){
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        // Look for "While using the app" or "Allow" button
        UiObject allowButton = device.findObject(new UiSelector().text("While using the app"));
        UiObject allowButton2 = device.findObject(new UiSelector().text("Allow"));

        try {
            if (allowButton.exists() && allowButton.isEnabled()) {
                allowButton.click();
            } else if (allowButton2.exists() && allowButton2.isEnabled()) {
                allowButton2.click();
            }
        } catch (UiObjectNotFoundException e) {
        }
    }
    public static Matcher<View> hasTextColor(final int expectedColor) {
        return new BoundedMatcher<View, TextView>(TextView.class) {
            @Override
            protected boolean matchesSafely(TextView textView) {
                return textView.getCurrentTextColor() == expectedColor;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with text color: ").appendValue(expectedColor);
            }
        };
    }

}
