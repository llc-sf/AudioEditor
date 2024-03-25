/**
 * StackBlur v1.0 for Android
 *
 * @Author: Enrique López Mañas <eenriquelopez@gmail.com>
 * http://www.lopez-manas.com
 *
 * Author of the original algorithm: Mario Klingemann <mario.quasimondo.com>
 *
 * This is a compromise between Gaussian Blur and Box blur
 * It creates much better looking blurs than Box Blur, but is
 * 7x faster than my Gaussian Blur implementation.
 *
 * I called it Stack Blur because this describes best how this
 * filter works internally: it creates a kind of moving stack
 * of colors whilst scanning through the image. Thereby it
 * just has to add one new block of color to the right side
 * of the stack and remove the leftmost color. The remaining
 * colors on the topmost layer of the stack are either added on
 * or reduced by one, depending on if they are on the right or
 * on the left side of the stack.
 *
 * @copyright: Enrique López Mañas
 * @license: Apache License 2.0
 */


package com.beautycamera.stackblur;

import android.graphics.Bitmap;
import android.os.Build;

import java.io.FileOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class StackBlurManager {
	static final int EXECUTOR_THREADS = Runtime.getRuntime().availableProcessors();

	private static final AtomicLong sTaskId = new AtomicLong(0);
	static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(EXECUTOR_THREADS, r -> {
        Thread thread = new Thread(r);
        thread.setName("BlurManager-" + sTaskId.getAndIncrement());
        return thread;
    });

	private static volatile boolean hasRS = true;

	/**
	 * Original image
	 */
	private final Bitmap _image;

	/**
	 * Most recent result of blurring
	 */
	private Bitmap _result;

	/**
	 * Method of blurring
	 */
	private BlurProcess _blurProcess;

	/**
	 * Constructor method (basic initialization and construction of the pixel array)
	 * @param image The image that will be analyed
	 */
	public StackBlurManager(Bitmap image) {
		_image = image;
		if (NativeBlurProcess.isNativeBlurAvaible() && Build.MODEL.equals("HM NOTE 1W")) {
			_blurProcess = new NativeBlurProcess();
		} else {
			_blurProcess = new JavaBlurProcess();
		}
	}

	/**
	 * Process the image on the given radius. Radius must be at least 1
	 * @param radius
	 */
	public Bitmap process(int radius) {
		try{
		_result = _blurProcess.blur(_image, radius);
		}
		catch(Exception e){
			e.printStackTrace();
			if(_blurProcess instanceof NativeBlurProcess){
				_blurProcess = new JavaBlurProcess();
				_result = _blurProcess.blur(_image, radius);
			}
		}
		return _result;
	}

	/**
	 * Returns the blurred image as a bitmap
	 * @return blurred image
	 */
	public Bitmap returnBlurredImage() {
		return _result;
	}

	/**
	 * Save the image into the file system
	 * @param path The path where to save the image
	 */
	public void saveIntoFile(String path) {
		try {
			FileOutputStream out = new FileOutputStream(path);
			_result.compress(Bitmap.CompressFormat.PNG, 90, out);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the original image as a bitmap
	 * @return the original bitmap image
	 */
	public Bitmap getImage() {
		return this._image;
	}

	/**
	 * Process the image using a native library
	 */
	public Bitmap processNatively(int radius) {
		NativeBlurProcess blur = new NativeBlurProcess();
		_result = blur.blur(_image, radius);
		return _result;
	}
}
