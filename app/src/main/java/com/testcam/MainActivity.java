package com.testcam;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import java.io.IOException;
import java.util.List;

public class MainActivity extends Activity {
    private static String TAG = "MainActivity";

    Camera mCamera;
    SurfaceTexture mSurfaceTexture;
    SurfaceView mSurfaceView;
    SurfaceHolder mHolder;
    Button mBtn, mBtn2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //无title
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        mBtn = (Button) findViewById(R.id.button);
        mBtn2 = (Button) findViewById(R.id.button2);
        mSurfaceView = (SurfaceView)findViewById(R.id.surfaceView);

        // 按钮
        {
            View.OnClickListener callback = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (view == mBtn) {
                        closeCamera();
                        mCamera = openCamera(null, mSurfaceTexture); /** 打开不预览 */
                    } else if (view == mBtn2) {
                        closeCamera();
                        mCamera = openCamera(mHolder, null); /** 打开并预览 */
                    }
                }
            };

            mBtn.setOnClickListener(callback);
            mBtn2.setOnClickListener(callback);
        }

        mSurfaceTexture = new SurfaceTexture(10);
        mHolder = mSurfaceView.getHolder();
    }


    /**
     * @param holder 预览的surface, 可以为null
     * @param surfaceTexture 不预览时使用
     * */
    private static Camera openCamera(SurfaceHolder holder, SurfaceTexture surfaceTexture) {
        int preViewWidth = 1080;
        int preViewHeight = 1920;

        try {
            Camera camera = Camera.open();

            // 打印支持的分辨率
            {
                List<Camera.Size> sizes = camera.getParameters().getSupportedPreviewSizes();
                for (Camera.Size s: sizes) {
                    Log.e(TAG, String.format("support preview size: %dx%d", s.width, s.height));
                }
            }

            if (holder != null) {
                camera.setPreviewDisplay(holder);
            } else {
                camera.setPreviewTexture(surfaceTexture);
            }

            camera.setDisplayOrientation(90);

            {
                Camera.Parameters parameters = camera.getParameters();

                parameters.setPreviewSize(preViewHeight, preViewWidth);
                //parameters.set("3dnr-mode", "on");
                parameters.setPreviewFormat(ImageFormat.NV21);

                camera.setParameters(parameters);
            }

            if (false) {
                // 无法处理数据
                camera.setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] bytes, Camera camera) {
                        Log.e(TAG, String.format("setPreviewCallback onPreviewFrame %d", bytes.length));
                    }
                });
            } else {
                // 可以处理视频数据, 回显(要配合addCallbackBuffer)
                byte[] bytes = new byte[preViewWidth * preViewHeight * 3 / 2];
                camera.addCallbackBuffer(bytes);
                camera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] bytes, Camera camera) {
                        Log.e(TAG, String.format("setPreviewCallbackWithBuffer onPreviewFrame %d", bytes.length));

                        camera.addCallbackBuffer(bytes);
                    }
                });
            }

            camera.startPreview();

            return camera;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void closeCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();

            mCamera = null;
        }
    }
}
