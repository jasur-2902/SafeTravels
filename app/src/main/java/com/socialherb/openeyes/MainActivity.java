package com.socialherb.openeyes;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.LargestFaceFocusingProcessor;


import org.w3c.dom.Text;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private CameraSource mCameraSource = null;
    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;

    private static final int RC_HANDLE_GMS = 9001;

    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private final Activity activity = this;
    private com.google.android.gms.analytics.Tracker mTracker;
    private static int CAMERA_FACING=CameraSource.CAMERA_FACING_FRONT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);

        Constants.ALARM_POINT =SaveData.getInstance(getApplicationContext()).getAlarmPoint();

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource(CAMERA_FACING);
        } else {
            requestCameraPermission();
        }



        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        // Change Camera Position (Front or Back Camera)
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //         .setAction("Action", null).show();
                if (mCameraSource != null) {
                    mCameraSource.release();
                }
                if (CAMERA_FACING == CameraSource.CAMERA_FACING_FRONT)
                    CAMERA_FACING = CameraSource.CAMERA_FACING_BACK;
                else CAMERA_FACING = CameraSource.CAMERA_FACING_FRONT;
                createCameraSource(CAMERA_FACING);
                startCameraSource();
            }
        });

        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        // Instruction Bottom Toast Menu
//        Common.showSnackBar(findViewById(R.id.main_layout), getString(R.string.intro_msg));


        Button btn =  (Button) findViewById(R.id.button);

        ImageView settingsBtn = findViewById(R.id.settingsBtn);

        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // [START custom_event]
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("MenuItem")
                        .setAction("action_settings")
                        .build());
                // [END custom_event]

                Common.nextScene(activity, SetupActivity.class);

            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Background.class));
            }
        });


    }



    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(activity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     */
    private void createCameraSource(int CAMERA_FACING) {

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
                .setFacing(CAMERA_FACING)
                .setRequestedFps(30.0f)
                .build();
    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();

        Log.i(TAG, "Setting screen name: " + TAG);
        mTracker.setScreenName("Image~" + TAG);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        startCameraSource();
    }



    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
    }

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            createCameraSource(CameraSource.CAMERA_FACING_BACK);
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Face Tracker sample")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }




    //==============================================================================================
    // Camera Source Preview
    //==============================================================================================

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {

        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

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
            return new GraphicFaceTracker(mGraphicOverlay);
        }
    }

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;
        SoundPool pool=null;
        int sound=0;
        int count=0;

        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay);

            pool = new SoundPool(1, AudioManager.STREAM_MUSIC,0);
            sound = pool.load(activity,R.raw.sound_1,1);
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);
        }


        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face);

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

                if(count > 120 && count % 10 == 0  && count < 180){
                    pool.play(sound, 1, 1, 1, 0, 1);
                }

                if (count == 180){

                    Handler mHandler = new Handler(Looper.getMainLooper());
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setMessage("It will automatically send your information to 911 in 00:30 seconds")
                                    .setTitle("Emergency Notification");

                            builder.setPositiveButton("Call 911", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // User clicked OK button

                                    actionCall();

                                }
                            });
                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });

                            final AlertDialog dialog = builder.create();

                            dialog.show();

                            new CountDownTimer(30000, 1000) {
                                @Override
                                public void onTick(long millisUntilFinished) {
                                    dialog.setMessage("00:"+ (millisUntilFinished/1000));

                                    if((millisUntilFinished/1000) % 3 == 0){
                                        pool.play(sound, 1, 1, 1, 0, 1);

                                    }

                                }


                                @Override
                                public void onFinish() {

                                    Toast.makeText(MainActivity.this, "Your information has been sent to 911", Toast.LENGTH_LONG).show();

                                    dialog.dismiss();

                                }
                            }.start();



                        }
                    });



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
            mOverlay.remove(mFaceGraphic);
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
        }
    }

    private void actionCall() {

        Intent i = new Intent(Intent.ACTION_DIAL);
        i.setData(Uri.parse("tel:5165134284"));

        startActivity(i);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            // [START custom_event]
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("MenuItem")
                    .setAction("action_settings")
                    .build());
            // [END custom_event]

            Common.nextScene(activity, SetupActivity.class);

            return true;
        }

        if (id == R.id.contact_us) {

            // [START custom_event]
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("MenuItem")
                    .setAction("contact_us")
                    .build());
            // [END custom_event]


            return true;
        }

        if (id == R.id.app_info) {

            // [START custom_event]
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("MenuItem")
                    .setAction("app_info")
                    .build());
            // [END custom_event]

            Common.nextScene(activity, AppInfoActivity.class);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
