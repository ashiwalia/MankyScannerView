package me.ashish.mankyscannerviewtest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.widget.FrameLayout;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.util.Arrays;
import java.util.EnumMap;

/**
 * Created by Ashish on 5/13/2017.
 */

public abstract class MankyScannerViewHelper extends FrameLayout {


    private static final String TAG = "CAMERA";
    private Context mContext;
    private TextureView mTextureView;
    private String mCameraID;
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private CaptureRequest mCaptureRequest;
    private MultiFormatReader multiFormatReader = new MultiFormatReader();
    private Result var22 = null;

    public MankyScannerViewHelper(Context context) {
        super(context);
        mContext = context;
    }

    public MankyScannerViewHelper(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public MankyScannerViewHelper(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }


    private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            mLog.getInstance().MyLog("TEXTURE", "onSurfaceTextureAvailable", "called");
                    fireUpCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            mLog.getInstance().MyLog("TEXTURE", "onSurfaceTextureSizeChanged", "called");
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            mLog.getInstance().MyLog("TEXTURE", "onSurfaceTextureDestroyed", "called");
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            mLog.getInstance().MyLog("TEXTURE", "onSurfaceTextureUpdated", "called");

        }
    };


    private final CameraDevice.StateCallback mCameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            mLog.getInstance().MyLog(TAG, "onOpened", "called");
            SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
            Surface surface = new Surface(surfaceTexture);
            final CaptureRequest.Builder builder;
            try {
                builder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                builder.addTarget(surface);
                builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                mCaptureRequest = builder.build();
                camera.createCaptureSession(Arrays.asList(new Surface[]{surface}), CameraCaptureSessionCallbacks, mBackgroundHandler);
            } catch (CameraAccessException e) {
                mLog.getInstance().MyLog(TAG, "mCameraStateCallback", "CameraAccessException");
                e.printStackTrace();
            }

        }

        @Override
        public void onDisconnected(CameraDevice camera) {

        }

        @Override
        public void onError(CameraDevice camera, int error) {

        }
    };


    private final CameraCaptureSession.StateCallback CameraCaptureSessionCallbacks = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession session) {
            try {
                session.setRepeatingRequest(mCaptureRequest, mCaptureCallback, mBackgroundHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {

        }
    };


    final CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
            getImageFrame();

        }
    };

    abstract void onReceivedResult(Result result);


    public void startCamera() {
        startBackgroundThread();
        setUpTextureView();
    }


    private void setUpTextureView() {
        mLog.getInstance().MyLog(TAG, "setUpTextureView", "called");
        mTextureView = new TextureView(mContext);
        this.addView(mTextureView);
        if (mTextureView.isAvailable()) {
            fireUpCamera();
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }


    private void fireUpCamera() {
        mLog.getInstance().MyLog(TAG, "fireUpCamera", "called");
        CameraManager cameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : cameraManager.getCameraIdList()){
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }
                mCameraID = cameraId;
                break;

            }
        } catch (CameraAccessException e) {
            mLog.getInstance().MyLog(TAG, "CameraAccessException", e.toString());
            e.printStackTrace();

        }

        if(mCameraID != null){
            try {
                cameraManager.openCamera(mCameraID, mCameraStateCallback, mBackgroundHandler);
            } catch (CameraAccessException e) {
                mLog.getInstance().MyLog(TAG, "CameraAccessException", e.toString());
                e.printStackTrace();
            }
        }
    }



    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }


    private void getImageFrame() {
        multiFormatReader.setHints(new EnumMap(DecodeHintType.class));

        byte[]  data = getNV21(mTextureView.getWidth(), mTextureView.getHeight(), mTextureView.getBitmap());

       /* ByteBuffer buffer = ByteBuffer.allocate(mTextureView.getBitmap().getAllocationByteCount());
        mTextureView.getBitmap().copyPixelsToBuffer(buffer);


        byte[] data = buffer.array();*/
        Log.d("OKAY", String.valueOf(data.length));


        int width = mTextureView.getWidth();
        int height = mTextureView.getHeight()+100;
        /*if (DisplayUtils.getScreenOrientation(getActivity()) == 1) {
            byte[] rawResult = new byte[data.length];
            int source = 0;

            while (true) {
                if (source >= height) {
                    source = width;
                    width = height;
                    height = source;
                    data = rawResult;
                    break;
                }

                for (int finalRawResult = 0; finalRawResult < width; ++finalRawResult) {
                    rawResult[finalRawResult * height + height - source - 1] = data[finalRawResult + source * width];
                }

                ++source;
            }
        }*/


        PlanarYUVLuminanceSource var23 = buildLuminanceSource(data);
        if (var23 != null) {
            BinaryBitmap var24 = new BinaryBitmap(new HybridBinarizer(var23));

            try {
                var22 = multiFormatReader.decodeWithState(var24);
            } catch (Exception e) {
                Log.d("OKAY VAR 22", e.toString());
            } finally {
                multiFormatReader.reset();
            }
        }

        if (var22 != null) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                public void run() {
                    //onReceivedResult(var22);
                    Log.d("OKAY RESULT", var22.getText());

                }
            });
        }


    }



    public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data) {
        Rect rect = new Rect(mTextureView.getLeft(), mTextureView.getTop(), mTextureView.getRight(), mTextureView.getBottom());
        if (rect == null) {
            return null;
        } else {
            PlanarYUVLuminanceSource source = null;

            try {
                source = new PlanarYUVLuminanceSource(data, mTextureView.getWidth(), mTextureView.getHeight(), rect.left, rect.top, rect.width(), rect.height(), false);
            } catch (Exception var7) {
                ;
            }

            return source;
        }
    }


    byte [] getNV21(int inputWidth, int inputHeight, Bitmap scaled) {

        int [] argb = new int[inputWidth * inputHeight];

        scaled.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight);

        byte [] yuv = new byte[inputWidth*inputHeight*3/2];
        encodeYUV420SP(yuv, argb, inputWidth, inputHeight);

        scaled.recycle();

        return yuv;
    }

    void encodeYUV420SP(byte[] yuv420sp, int[] argb, int width, int height) {
        final int frameSize = width * height;

        int yIndex = 0;
        int uvIndex = frameSize;

        int a, R, G, B, Y, U, V;
        int index = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {

                a = (argb[index] & 0xff000000) >> 24; // a is not used obviously
                R = (argb[index] & 0xff0000) >> 16;
                G = (argb[index] & 0xff00) >> 8;
                B = (argb[index] & 0xff) >> 0;

                // well known RGB to YUV algorithm
                Y = ( (  66 * R + 129 * G +  25 * B + 128) >> 8) +  16;
                U = ( ( -38 * R -  74 * G + 112 * B + 128) >> 8) + 128;
                V = ( ( 112 * R -  94 * G -  18 * B + 128) >> 8) + 128;

                // NV21 has a plane of Y and interleaved planes of VU each sampled by a factor of 2
                //    meaning for every 4 Y pixels there are 1 V and 1 U.  Note the sampling is every other
                //    pixel AND every other scanline.
                yuv420sp[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv420sp[uvIndex++] = (byte)((V<0) ? 0 : ((V > 255) ? 255 : V));
                    yuv420sp[uvIndex++] = (byte)((U<0) ? 0 : ((U > 255) ? 255 : U));
                }

                index ++;
            }
        }
    }
}
