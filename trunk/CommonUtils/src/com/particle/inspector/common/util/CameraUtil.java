package com.particle.inspector.common.util;

import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.hardware.Camera;

public class CameraUtil extends Activity 
{
	private static int CODE_TAKE_PICTURE = 0xFF;
	public static Camera camera = null;
	public static Bitmap picture = null;
		
	public static boolean hasBackCamera(Context context) 
	{
		PackageManager pm = context.getPackageManager();
		return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
	}
	
	/* Android support two cameras from 2.3 (API9)
	public static boolean hasFrontCamera(Context context)
	{
		PackageManager pm = context.getPackageManager();
		return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);
	}
	*/
	
	// Take a picture by back camera
	public void takeBackPicture() 
	{
		camera = Camera.open();
		Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
			@Override
			public void onShutter() {
		    
			}
		};
		Camera.PictureCallback picCallback = new Camera.PictureCallback() {
			@Override
			public void onPictureTaken(byte[] data, Camera camera) {
				BitmapFactory.Options options=new BitmapFactory.Options();
				options.inSampleSize = 5;

                picture = BitmapFactory.decodeByteArray(data, 0, data.length, options);
			}
		};

		camera.takePicture(shutterCallback, null, picCallback);
		camera.release();
	}
	
}