package it.near.sdk.reactnative.sdksample;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.facebook.react.ReactActivity;

import it.near.sdk.reactnative.rnnearitsdk.RNNearItModule;

public class MainActivity extends ReactActivity {

    /**
     * Returns the name of the main component registered from JavaScript.
     * This is used to schedule rendering of the component.
     */
    @Override
    protected String getMainComponentName() {
        return "nearsdksample";
    }


    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        RNNearItModule.onPostCreate(getApplicationContext(), getIntent());
    }

}
