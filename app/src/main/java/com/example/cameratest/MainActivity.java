package com.example.cameratest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity implements SensorEventListener {

	private ImageView mImageView;
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    float x,y,z,e,lx,ly,lz;
    String string;
    float angle;
    boolean xy;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        xy=true;
		setContentView(R.layout.activity_main);
		mImageView = (ImageView) findViewById(R.id.imageView1);
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
	}
	public void onClick(View view){
		if(view.getId()==R.id.button1) {
			dispatchTakePictureIntent();
            x=lx;
            y=ly;
            z=lz;
            string = "x=" + Float.toString(x) +"y=" + Float.toString(y) + "z=" + Float.toString(z);
            if (xy) {
                angle = (float) ((Math.atan(y / x) * 180) / Math.PI);
            } else {
                angle = (float) ((Math.atan(x / y) * 180) / Math.PI);
            }
        }
        if(view.getId()==R.id.button2) {
            TextView tv =(TextView) findViewById(R.id.textView);
            tv.setText("Put your phone parallel to the wall");
            if (x>y) {
                xy=true;
            } else {
                xy=false;
            }

        }
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
	        Bundle extras = data.getExtras();
	        Bitmap imageBitmap = rotate((Bitmap) extras.get("data"), (float) angle);
	        mImageView.setImageBitmap(imageBitmap);
            TextView tv =(TextView) findViewById(R.id.textView);
            tv.setText(Float.toString(angle));
            writeToFile(string);

           // galleryAddPic();
           // Bitmap bitmap = Bitmap.createBitmap(mCurrentPhotoPath);
	}
}
    public static Bitmap rotate(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        source = Bitmap.createBitmap(source, 0, 0, source.getWidth(),source.getHeight(), matrix, false);
        Matrix m = new Matrix();

        RectF inRect = new RectF(0, 0, source.getWidth(), source.getHeight());
        RectF outRect = new RectF(0, 0, source.getWidth()/8, source.getHeight()/8);
        m.setRectToRect(inRect, outRect, Matrix.ScaleToFit.CENTER);
        float[] values = new float[9];
        m.getValues(values);

        //resize bitmap
        source = Bitmap.createScaledBitmap(source, (int) (source.getWidth() * values[0]), (int) (source.getHeight() * values[4]), true);

        // save image
       /* try
        {
            FileOutputStream out = new FileOutputStream(path);
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
        }
        catch (Exception e)
        {
            Log.e("Image", e.getMessage(), e);
        }
    }
    catch (IOException e)
    {
        Log.e("Image", e.getMessage(), e);
    }*/
        return source;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }
    private void writeToFile(String string) {
        String filename = mCurrentPhotoPath +".txt";
        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(string.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    protected void onPause() {
        super.onPause();
        senSensorManager.unregisterListener(this);
    }
    protected void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

	static final int REQUEST_IMAGE_CAPTURE = 1;

	private void dispatchTakePictureIntent() {
	    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        //IOException trouble in creating file
        /*if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }*/
	}
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;


        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            lx = sensorEvent.values[0];
            ly = sensorEvent.values[1];
            lz = sensorEvent.values[2];
        }
        TextView tv2 =(TextView) findViewById(R.id.textView2);
        String string2;
        string2 = "x=" + Float.toString(lx) +"y=" + Float.toString(ly) + "z=" + Float.toString(lz);
        tv2.setText(string2);
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        Log.d("MY_TAG", imageFileName);
        Log.d("MY_TAG", storageDir.getAbsolutePath());
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
               storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        Log.d("MY_TAG", mCurrentPhotoPath);
        return image;
    }
}
