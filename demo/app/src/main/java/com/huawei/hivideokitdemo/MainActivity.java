/**
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.hivideokitdemo;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.hwvideokit.R;
import com.huawei.multimedia.hivideokit.HiVideoKitDisplaySdk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Use MediaPlayer to play video.
 * Get information of supported filters.
 * Show filters by input type and level.
 *
 * @since 2020-12-03
 */
public class MainActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "videokitdemo";

    private static final String PLAY = "play";

    private static final String PAUSE = "pause";

    // To compare with difference between the whole surface area and the available area.
    private static final int HIGHT = 200;

    // The max valid level of filters.
    private static final int TOP_LEVEL = 100;

    // Array for spinner.
    private static final String[] ARRAY =
        {"Default", "Sunny", "Cool", "Warm", "Sentimental", "Caramel", "Vintage", "Olive", "Amber", "Black and white"};

    private Button playOrPause;

    private Button stop;

    private Button feature;

    private Button apply;

    private Button apply3d;

    private Button version;

    private Button stopeffect;

    private Button go;

    private TextView textView;

    // The parent layout of EditText.
    private LinearLayout linearLayout;

    private Spinner spinner;

    private EditText level;

    // The position of selected option in spinner which beginning with 0.
    private int pos;

    // Input filter level.
    private int applyFilterLevel;

    private boolean isStop;

    private boolean isPause = false;

    private MediaPlayer mediaPlayer;

    private Surface mSurface;

    private TextureView textureView;

    private ArrayAdapter<String> adapter;

    private String input;

    private HiVideoKitDisplaySdk mHwVideoKit;

    private List<String> filters = new ArrayList<>(TOP_LEVEL);

    /*
     * Add listener for condition whether the input method is existing or not. if it is existing , EditText should
     * befocused， otherwise，EditText should be clean focus.
     */
    private ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener =
        new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Avalavle area.
                Rect rect = new Rect();
                linearLayout.getWindowVisibleDisplayFrame(rect);
                // The height of the whole surface.
                int screenHeight = linearLayout.getRootView().getHeight();
                Log.d(TAG, "onGlobalLayout: b=" + rect.bottom + "s" + screenHeight);
                // heightDifference is the height of soft keyboard.
                // If there is not a keyboard, heightDifference would be 0.
                int heightDifference = screenHeight - (rect.bottom);
                if (heightDifference > HIGHT) {
                    Log.d(TAG, "input method is existing");
                    level.requestFocus();
                } else {
                    Log.d(TAG, "input method is not existing");
                    level.clearFocus();
                }
            }
        };

    private MediaPlayer.OnPreparedListener preparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            mediaPlayer.start();
        }
    };

    /**
     * Add SurfaceTextureListener for TextureView.
     * Start playing when the SurfaceTexture is available.
     */
    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            mSurface = new Surface(surface);
            initVideoPlayer();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playOrPause = findViewById(R.id.playorpause);
        stop = findViewById(R.id.stop);
        version = findViewById(R.id.version);
        feature = findViewById(R.id.feature);
        textView = findViewById(R.id.textView);
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());
        apply = findViewById(R.id.apply);
        apply3d = findViewById(R.id.apply3d);
        stopeffect = findViewById(R.id.stopeffect);
        go = findViewById(R.id.go);
        spinner = findViewById(R.id.spinner1);
        linearLayout = findViewById(R.id.linearlayout);
        mediaPlayer = new MediaPlayer();
        mHwVideoKit = new HiVideoKitDisplaySdk(this);
        level = findViewById(R.id.level);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ARRAY);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new SpinnerSelectedListener());
        textureView = findViewById(R.id.textureview);
        textureView.setSurfaceTextureListener(mSurfaceTextureListener);
        filters = mHwVideoKit.getEffectList();
        linearLayout.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
        setOnClickListener();
    }

    /**
     * Init button clickListener
     */
    private void setOnClickListener() {
        playOrPause.setOnClickListener(this);
        stop.setOnClickListener(this);
        version.setOnClickListener(this);
        feature.setOnClickListener(this);
        go.setOnClickListener(this);
        apply.setOnClickListener(this);
        apply3d.setOnClickListener(this);
        stopeffect.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.playorpause:
                playorpause();
                break;
            case R.id.stop:
                stop();
                break;
            case R.id.version:
                version();
                break;
            case R.id.feature:
                feature();
                break;
            case R.id.go:
                go();
                break;
            case R.id.apply:
                apply();
                break;
            case R.id.apply3d:
                apply3d();
                break;
            case R.id.stopeffect:
                stopEffect();
                break;
            default:
                break;
        }
    }

    private void playorpause() {
        if (mediaPlayer.isPlaying()) {
            Log.d(TAG, PAUSE);
            mediaPlayer.pause();
            playOrPause.setText(PLAY);
            isPause = true;
            return;
        }
        if (isStop) {
            Log.d(TAG, "replay");
            initVideoPlayer();
            isStop = false;
        } else {
            mediaPlayer.start();
        }
        isPause = false;
        playOrPause.setText(PAUSE);
    }

    /**
     * Stop playing test video
     */
    private void stop() {
        Log.d(TAG, "stop");
        mediaPlayer.stop();
        isPause = false;
        isStop = true;
        playOrPause.setText(PLAY);
    }

    private void version() {
        boolean isRet = mHwVideoKit.checkHiVideoKitStatus();
        textView.setText("checkHiVideoKitStatus: " + isRet + System.lineSeparator());
    }

    private void feature() {
        boolean isSupport = mHwVideoKit.getSupported();
        Log.i(TAG, "isSupport: " + isSupport);
        textView.setText("isSupport: " + isSupport + System.lineSeparator());
        int filterRangeMax = mHwVideoKit.getEffectRangeMax();
        if (filters == null || filters.size() == 0 || filterRangeMax == 0) {
            Log.e(TAG, "getEffect is empty. ");
            return;
        }
        textView.append("getEffect out filters is:" + System.lineSeparator());
        for (String oneFilter : filters) {
            StringBuilder toPrint = new StringBuilder();
            toPrint.append("filter: ");
            toPrint.append(oneFilter);
            toPrint.append(", range: [");
            toPrint.append(0);
            toPrint.append(", ");
            toPrint.append(filterRangeMax);
            toPrint.append("]" + System.lineSeparator());
            textView.append(toPrint);
        }
    }

    /*
     * Method for skipping to SecActivity.
     */
    private void go() {
        Intent intent = new Intent(MainActivity.this, SecActivity.class);
        startActivity(intent);
    }

    /**
     * Apply default filmfilter effects
     */
    private void apply() {
        // Get input level.Default value is 90.
        input = level.getText().toString();
        if ("".equals(input)) {
            input = "90";
        }
        applyFilterLevel = Integer.valueOf(input);
        if (applyFilterLevel > TOP_LEVEL) {
            Toast
                .makeText(MainActivity.this,
                    "If the value of level is greater than MAXRANGE," + " the level is invalid.",
                    Toast.LENGTH_SHORT)
                .show();
        }
        int applyFilterNo = pos;
        if (applyFilterNo < filters.size() && applyFilterNo >= 0) {
            int ret = mHwVideoKit.setDefaultEffect(filters.get(applyFilterNo), applyFilterLevel);
            textView.setText(
                "setData: " + filters.get(applyFilterNo) + ", level: " + applyFilterLevel + ", result is: " + ret);
            if (ret != 0) {
                Log.e(TAG, "Failed to set filter!");
                return;
            }
        } else {
            textView.setText("invalid input index of filter");
            Log.e(TAG, "invalid input index of filter!");
        }
    }

    /**
     * Apply 3D-LUT filmfilter effects
     */
    private void apply3d() {
        int ret = mHwVideoKit.set3DLutEffect(Init3dLutHigh.gmpLutHigh, Init3dLutLow.gmpLutLow);
        textView.setText("set3DLutEffect result is: " + ret);
        if (ret != 0) {
            Log.e(TAG, "Failed to set 3D-LUT! " + ret);
        }
    }

    /**
     * Stop filmfilter effects
     */
    private void stopEffect() {
        int ret = mHwVideoKit.stopEffect();
        textView.setText("stopDefaultEffect result is: " + ret);
        if (ret != 0) {
            Log.e(TAG, "Failed to stop DefaultEffect! " + ret);
        }
    }

    /**
     * Get position of selected option in spinner.
     */
    private class SpinnerSelectedListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            pos = arg2;
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    private void destroyPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void initVideoPlayer() {
        if (mSurface == null) {
            return;
        }
        try {
            if (mediaPlayer != null) {
                mediaPlayer.reset();
            } else {
                mediaPlayer = new MediaPlayer();
            }
            Log.d(TAG, "initPlayVideo: ");
            mediaPlayer.setSurface(mSurface);
            AssetFileDescriptor afd = getApplicationContext().getResources().openRawResourceFd(R.raw.test);
            if (afd != null) {
                mediaPlayer.setDataSource(afd);
            }
            mediaPlayer.setLooping(true);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(preparedListener);
        } catch (IOException e) {
            Log.d(TAG, "initPlayVideo: IOException");
        }
    }

    @Override
    protected void onResume() {
        if (mediaPlayer != null) {
            if (!mediaPlayer.isPlaying() && !isPause) {
                mediaPlayer.start();
                isPause = false;
                playOrPause.setText(PAUSE);
            }
        }
        super.onResume();
    }

    @Override
    protected void onStop() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                isPause = false;
                Log.d(TAG, "onStop: pause");
                playOrPause.setText(PLAY);
            }
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            isPause = false;
            mediaPlayer.release();
        }
        super.onDestroy();
    }
}
