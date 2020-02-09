package com.socialherb.openeyes;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.LargestFaceFocusingProcessor;

import java.io.IOException;

public class MyIntentService extends Service {
    private static final String TAG = "FaceTracker";

    private CameraSourcePreview mPreview;

    public CameraSource mCameraSource = null;

    private com.google.android.gms.analytics.Tracker mTracker;

    //public CameraSourcePreview mPreview = new CameraSourcePreview(getApplicationContext());
    private GraphicOverlay mGraphicOverlay;

    private static final int RC_HANDLE_GMS = 9001;
    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;




    static int x=0;
    //Camera variables
//a surface holder
    private SurfaceHolder sHolder;
    //a variable to control the camera
    private Camera mCamera;
    //the camera parameters
    private Camera.Parameters parameters;

    private static int CAMERA_FACING=CameraSource.CAMERA_FACING_FRONT;

    //private ViewGroup mView;

    /** Called when the activity is first created. */
    @Override
    public void onCreate()
    {
        super.onCreate();
        Log.d("SERVICE", "onCreate" );

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource(CAMERA_FACING);
        } else {
            //requestCameraPermission();
            Toast.makeText(getApplicationContext(), "Need a permission", Toast.LENGTH_LONG).show();
        }


        LayoutInflater li = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                //WindowManager.LayoutParams.TYPE_INPUT_METHOD |
                WindowManager.LayoutParams.TYPE_TOAST,// | WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);



        View mView = li.inflate(R.layout.content_main, null);

        mPreview =  mView.findViewById(R.id.preview);

        PopupWindow popupWindow = new PopupWindow(MyIntentService.this);


        wm.addView(mView, params);


    }

//    private void startCameraSource() {
//
//        // check that the device has play services available.
//        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
//                getApplicationContext());
//        //if (code != ConnectionResult.SUCCESS)
//           // Dialog dlg = GoogleApiAvailability.getInstance().getErrorDialog(getApplication(), code, RC_HANDLE_GMS);
//           // dlg.show();
//
//
//        if (mCameraSource != null) {
//           // try {
//                //mPreview.start(mCameraSource, mGraphicOverlay);
//            //} catch (IOException e) {
//               // Log.e(TAG, "Unable to start camera source.", e);
//                mCameraSource.release();
//                mCameraSource = null;
//            //}
//        }
//    }


    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    public void startCameraSource() {
        Log.d("SERVICE", "startCameraSource" );
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
    /*if (code != ConnectionResult.SUCCESS) {
        Dialog dlg =
                GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
        dlg.show();
    }*/

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }



    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     */
    public void createCameraSource(int CAMERA_FACING) {
        Log.d("SERVICE", "createCameraSource" );

        Context context = getApplicationContext();

        FaceDetector detector = new FaceDetector.Builder(context)
                .setProminentFaceOnly(true)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        detector.setProcessor(new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                .build());

        if (!detector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }

        // Camera Size
        mCameraSource = new CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(640, 480)
                // .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)//using front camera
                .setRequestedFps(30.0f)
                .build();

        Log.i("createCameraSource",mCameraSource.toString());

    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.i(TAG, "Setting screen name: " + TAG);
        mTracker.setScreenName("Image~" + TAG);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        startCameraSource();
    }


    //==============================================================================================
    // Graphic Face Tracker
    //==============================================================================================

    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {

        @Override
        public Tracker<Face> create(Face face) {
            Log.d("GraphicFaceTPPPPPPPP", " THIS PART IS WORKING ");

            return new GraphicFaceTracker(mGraphicOverlay);
        }
    }

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private class GraphicFaceTracker extends Tracker<Face> {
        //private GraphicOverlay mOverlay;
        //private FaceGraphic mFaceGraphic;
        SoundPool pool=null;
        int sound=0;
        int count=0;



        GraphicFaceTracker(GraphicOverlay overlay) {
          //  mOverlay = overlay;
        //    mFaceGraphic = new FaceGraphic(overlay);

            Log.d("GraphicFaceTPPPPPPPP", " THIS PART IS WORKING ");

            pool = new SoundPool(1, AudioManager.STREAM_MUSIC,0);
            sound = pool.load(getApplicationContext(),R.raw.sound_1,1);
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */

        @Override
        public void onNewItem(int faceId, Face item) {
            //mFaceGraphic.setId(faceId);
        }


        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            //mOverlay.add(mFaceGraphic);
           // mFaceGraphic.updateFace(face);

            Log.d("onUpdate", " THIS PART IS WORKING ");


            float left = face.getIsLeftEyeOpenProbability();
            float right = face.getIsRightEyeOpenProbability();

            if ((left == Face.UNCOMPUTED_PROBABILITY) ||
                    (right == Face.UNCOMPUTED_PROBABILITY)) {
                // At least one of the eyes was not detected.
                return;
            }

            if(isEyeOpenProbability(left,right))
                count = 0;

            if(!isEyeOpenProbability(left,right)){
                count ++;

                Log.d("TAG NOT Zero", Integer.toString(count));

                if(count == 30){
                    pool.play(sound, 1, 1, 1, 0, 1);
                }

                if(count == 60 || count == 75 || count == 85){
                    pool.play(sound, 1, 1, 1, 0, 1);
                }

                if(count > 120 && 120%2 == 0 ){
                    pool.play(sound, 1, 1, 1, 0, 1);
                }
            }

//            if(!isEyeOpenProbability(left,right)) {
//
//                //Log.e(TAG, "Close count -> "+count+" left -> "+face.getIsLeftEyeOpenProbability()+", right -> "+face.getIsRightEyeOpenProbability());
//                count++;
//
//
//
//                if(count == 0){
//                    Log.d("TAG", "count 000");
//                }
//
//                else {
//                    Log.d("TAG NOT Zero", Integer.toString(count));
//                }
//
//                if(count>30) {
//                    pool.play(sound, 1, 1, 1, 0, 1);
//
//                    //pool.stop(sound);
//                    // pool.release();
//                    //pool=null;
//
//                }
//
//                count = 0;
//
//            } else {
//
//                count=0;
//                Log.w(TAG, "Open count -> "+count+" left -> "+face.getIsLeftEyeOpenProbability()+", right -> "+face.getIsRightEyeOpenProbability());
//
//            }
        }

        private boolean isEyeOpenProbability(float IsLeftEyeOpenProbability,float IsRightEyeOpenProbability) {
            boolean isEyeOpened=true;
            //Log.e(TAG,"Constants.ALARM_POINT - > "+Constants.ALARM_POINT+"");
            if ((IsLeftEyeOpenProbability < (float)Constants.ALARM_POINT*0.01) || (IsRightEyeOpenProbability < (float)Constants.ALARM_POINT*0.01)) {
                // Both eyes become closed
                isEyeOpened = false;
            }
            return isEyeOpened;
        }




        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            //mOverlay.remove(mFaceGraphic);
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
           // mOverlay.remove(mFaceGraphic);
        }
    }



    /**
     * starts the camera.
     */

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        startCameraSource();

        BlinkTracker bl = new BlinkTracker();
        //bl.onUpdate();

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

/*
@Override
public void onStart(Intent intent, int startId) {
    // TODO Auto-generated method stub
    super.onStart(intent, startId);

    Log.d("SERVICE", "onStart" );
    startCameraSource();
    //startCameraSource();

//    mCamera = Camera.open(1);
}*/




//==============================================================================================
// Camera Source Preview
//==============================================================================================



    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }


//==============================================================================================
// Eye Blink Tracker
//==============================================================================================

    public class BlinkTracker extends Tracker<Face> {
        private final double OPEN_THRESHOLD = 0.85;
        private final double CLOSE_THRESHOLD = 0.50;

        private int state = 0;

        public void onUpdate(Detector.Detections<Face> detections, Face face) {
            Log.i("BlinkTracker", "eye tracker start");
            float left = face.getIsLeftEyeOpenProbability();
            float right = face.getIsRightEyeOpenProbability();
            if ((left == Face.UNCOMPUTED_PROBABILITY) ||
                    (right == Face.UNCOMPUTED_PROBABILITY)) {
                // At least one of the eyes was not detected.
                return;
            }

            Toast.makeText(getApplicationContext(), "It's working", Toast.LENGTH_SHORT).show();
            switch (state) {
                case 0:
                    if ((left > OPEN_THRESHOLD) && (right > OPEN_THRESHOLD)) {
                        // Both eyes are initially open
                        Log.i("BlinkTracker", "eye open");
                        state = 1;
                    }
                    break;

                case 1:
                    if ((left < CLOSE_THRESHOLD) && (right < CLOSE_THRESHOLD)) {
                        // Both eyes become closed
                        Log.i("BlinkTracker", "blink occurred!");
                        state = 0;
                    }
                    break;

            /*case 2:
                if ((left > OPEN_THRESHOLD) && (right > OPEN_THRESHOLD)) {
                    // Both eyes are open again
                    Log.i("BlinkTracker", "blink occurred!");
                    state = 0;
                }
                break;*/
            }
        }

    }
}