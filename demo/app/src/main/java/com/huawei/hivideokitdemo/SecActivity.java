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
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import com.huawei.hwvideokit.R;

import java.io.IOException;

import androidx.annotation.Nullable;

/**
 * Used for testing protected plans of jumping into other activity
 *
 * @since 2020-12-03
 */
public class SecActivity extends Activity {
    private static final String TAG = "SecActivity";

    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sec);

        initMediaPlayer();

        TextureView textureview = findViewById(R.id.textureview_sec);
        textureview.setSurfaceTextureListener(new MySurfaceListener());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mediaPlayer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.pause();
    }

    private void openMediaPlayer(SurfaceTexture surface) {
        String path = "android.resource://" + getPackageName() + "/" + R.raw.test;
        try {
            mediaPlayer.setDataSource(SecActivity.this, Uri.parse(path));
            mediaPlayer.setSurface(new Surface(surface));
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            Log.d(TAG, "openMediaPlayer: IOException" + e.getMessage());
        }
    }

    private void initMediaPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });
            mediaPlayer.setLooping(true);
        }
    }

    /**
     * Add SurfaceTextureListener.
     */
    private class MySurfaceListener implements TextureView.SurfaceTextureListener {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openMediaPlayer(surface);
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
    }
}
