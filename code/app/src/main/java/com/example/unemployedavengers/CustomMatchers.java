/**
 * CustomMatchers - A utility class that defines custom matchers and helper methods for UI testing.
 *
 * Purpose:
 * - Provides utility functions for UI testing, particularly in the context of Espresso and UI Automator.
 * - Includes a custom matcher to verify the text color of a TextView.
 * - Includes a helper method to handle location permission popups by interacting with the appropriate UI buttons.
 *
 * Key Features:
 * - `hasTextColor`: A custom matcher that checks if a `TextView` has the expected text color.
 * - `handleLocationPermissionPopup`: A helper method that automates the interaction with location permission popups, by clicking the "Allow" button when it appears.
 *
 * Outstanding Issues:
 * - The `handleLocationPermissionPopup` method currently does not account for cases where the location permission popup text might differ across different devices or configurations. Additional handling might be required.
 * - The custom matcher `hasTextColor` may not work correctly if the TextView's color is changed dynamically (e.g., due to themes or other factors).
 */

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
import org.hamcrest.TypeSafeMatcher;

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
    public static Matcher<View> withDrawable() {
        return new TypeSafeMatcher<View>() {
            @Override
            protected boolean matchesSafely(View item) {
                if (!(item instanceof ImageView)) {
                    return false;
                }
                ImageView imageView = (ImageView) item;
                return imageView.getDrawable() != null; // Check if drawable exists
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("ImageView should have a drawable");
            }
        };
    }



}
