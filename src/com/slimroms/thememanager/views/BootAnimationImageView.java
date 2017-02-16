package com.slimroms.thememanager.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class BootAnimationImageView extends ImageView {
    private static final String TAG = BootAnimationImageView.class.getName();

    private static final int MAX_BUFFERS = 2;

    private Bitmap[] mBuffers = new Bitmap[MAX_BUFFERS];
    private int mReadBufferIndex = 0;
    private int mWriteBufferIndex = 0;
    private ZipFile mBootAniZip;

    private List<AnimationPart> mAnimationParts;
    private int mCurrentPart;
    private int mCurrentFrame;
    private int mCurrentPartPlayCount;
    private int mFrameDuration;

    private boolean mActive = false;

    public BootAnimationImageView(Context context) {
        this(context, null);
    }

    public BootAnimationImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BootAnimationImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mActive = false;
        removeCallbacks(mUpdateAnimationRunnable);

        if (mBootAniZip != null) {
            try {
                mBootAniZip.close();
            } catch (IOException e) {
                // ignore
            }
        }

    }

    public boolean setBootAnimation(ZipFile bootAni) {
        mBootAniZip = bootAni;
        try {
            mAnimationParts = parseAnimation(mBootAniZip);
        } catch (IOException e) {
            return false;
        }

        final AnimationPart part = mAnimationParts.get(0);
        mCurrentPart = 0;
        mCurrentPartPlayCount = part.playCount;
        mFrameDuration = part.frameRateMillis;

        getNextFrame();

        return true;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private void getNextFrame() {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inBitmap = mBuffers[mWriteBufferIndex];
        opts.inPreferredConfig = Bitmap.Config.RGB_565;
        opts.inMutable = true;
        final AnimationPart part = mAnimationParts.get(mCurrentPart);
        try {
            mBuffers[mWriteBufferIndex] =
                    BitmapFactory.decodeStream(mBootAniZip.getInputStream(mBootAniZip.getEntry(
                            part.frames.get(mCurrentFrame++))), null, opts);
        } catch (Exception e) {
            Log.w(TAG, "Unable to get next frame", e);
        }
        mWriteBufferIndex = (mWriteBufferIndex + 1) % MAX_BUFFERS;
        if (mCurrentFrame >= part.frames.size()) {
            if (mCurrentPartPlayCount > 0) {
                if (--mCurrentPartPlayCount == 0) {
                    mCurrentPart++;
                    mCurrentFrame = 0;
                    mCurrentPartPlayCount = mAnimationParts.get(mCurrentPart).playCount;
                }
            } else {
                mCurrentFrame = 0;
            }
        }
    }

    public void start() {
        mActive = true;
        post(mUpdateAnimationRunnable);
    }

    private Runnable mUpdateAnimationRunnable = new Runnable() {
        @Override
        public void run() {
            if (!mActive) return;
            BootAnimationImageView.this.postDelayed(mUpdateAnimationRunnable, mFrameDuration);
            BootAnimationImageView.this.post(mUpdateImageRunnable);
            mReadBufferIndex = (mReadBufferIndex + 1) % MAX_BUFFERS;
            getNextFrame();
        }
    };

    private Runnable mUpdateImageRunnable = new Runnable() {
        @Override
        public void run() {
            setImageBitmap(mBuffers[mReadBufferIndex]);
        }
    };

    private static class AnimationPart {
        /**
         * Number of times to play this part
         */
        int playCount;
        /**
         * If non-zero, pause for the given # of seconds before moving on to next part.
         */
        int pause;
        /**
         * The name of this part
         */
        String partName;
        /**
         * Time each frame is displayed
         */
        int frameRateMillis;
        /**
         * List of file names for the given frames in this part
         */
        List<String> frames;
        /**
         * width of the animation
         */
        int width;
        /**
         * height of the animation
         */
        public int height;

        AnimationPart(int playCount, int pause, String partName, int frameRateMillis,
                             int width, int height) {
            this.playCount = playCount;
            this.pause = pause;
            this.partName = partName;
            this.frameRateMillis = frameRateMillis;
            this.width = width;
            this.height = height;
            frames = new ArrayList<>();
        }

        void addFrame(String frame) {
            frames.add(frame);
        }
    }

    private List<AnimationPart> parseAnimation(ZipFile zip) throws IOException {
        List<AnimationPart> animationParts = null;

        ZipEntry ze = zip.getEntry("desc.txt");
        if (ze != null) {
            Log.d("TEST", "ze=" + ze.getName());
            animationParts = parseDescription(zip.getInputStream(ze));
        }

        if (animationParts == null) return null;

        for (AnimationPart a : animationParts) {
            for (Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements();) {
                ze = e.nextElement();
                if (!ze.isDirectory() && ze.getName().contains(a.partName)) {
                    a.addFrame(ze.getName());
                }
            }
            Collections.sort(a.frames);
        }

        return animationParts;
    }

    /**
     * Parses the desc.txt of the boot animation
     * @param in InputStream to the desc.txt
     * @return A list of the parts as given in desc.txt
     * @throws IOException
     */
    private List<AnimationPart> parseDescription(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line = reader.readLine();
        String previousLine = line;
        String[] details = line.split(" ");
        Log.d("TEST", "details=" + line);
        final int width = Integer.parseInt(details[0]);
        final int height = Integer.parseInt(details[1]);
        final int frameRateMillis = 1000 / Integer.parseInt(details[2]);

        List<AnimationPart> animationParts = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            String[] info = line.split(" ");
            Log.d("TEST", "previousLine=" + previousLine);
            Log.d("TEST", "line=" + line);
            if (info.length == 4 && (info[0].equals("p") || info[0].equals("c"))) {
                Log.d("TEST", "legth4");
                int playCount = Integer.parseInt(info[1]);
                int pause = Integer.parseInt(info[2]);
                String name = info[3];
                AnimationPart ap = new AnimationPart(playCount, pause, name, frameRateMillis,
                        width, height);
                animationParts.add(ap);
            } else if (info.length == 3 && (previousLine.contains("p") || previousLine.contains("c"))) {
                Log.d("TEST", "length3");
                int playCount = Integer.parseInt(info[0]);
                int pause = Integer.parseInt(info[1]);
                String name = info[2];
                AnimationPart ap = new AnimationPart(playCount, pause, name, frameRateMillis,
                        width, height);
                animationParts.add(ap);
            }
            previousLine = line;
        }

        return animationParts;
    }
}