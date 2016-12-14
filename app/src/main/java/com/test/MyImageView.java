package com.test;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

public class MyImageView extends ImageView {
        Paint paintScaleDisplay;
        Bitmap bitmapScale;
        Canvas canvasScale;
        int width =513;
        public MyImageView(Context context) {
            super(context);


            // TODO Auto-generated constructor stub
            if(width >512){
                bitmapScale = Bitmap.createBitmap((int)512,(int)50,Bitmap.Config.ARGB_8888);
            }
            else{
                bitmapScale =  Bitmap.createBitmap((int)256,(int)50,Bitmap.Config.ARGB_8888);
            }

            paintScaleDisplay = new Paint();
            paintScaleDisplay.setColor(Color.WHITE);
            paintScaleDisplay.setStyle(Paint.Style.FILL);

            canvasScale = new Canvas(bitmapScale);

            setImageBitmap(bitmapScale);
            invalidate();
        }
    public MyImageView(Context context,AttributeSet attributeSet) {
        super(context);


        // TODO Auto-generated constructor stub
        if(width >512){
            bitmapScale = Bitmap.createBitmap((int)512,(int)50,Bitmap.Config.ARGB_8888);
        }
        else{
            bitmapScale =  Bitmap.createBitmap((int)256,(int)50,Bitmap.Config.ARGB_8888);
        }

        paintScaleDisplay = new Paint();
        paintScaleDisplay.setColor(Color.WHITE);
        paintScaleDisplay.setStyle(Paint.Style.FILL);

        canvasScale = new Canvas(bitmapScale);

        setImageBitmap(bitmapScale);
        invalidate();
    }
        @Override
        protected void onDraw(Canvas canvas)
        {
            // TODO Auto-generated method stub
            super.onDraw(canvas);

            if(width > 512){
                canvasScale.drawLine(0, 30,  512, 30, paintScaleDisplay);
                for(int i = 0,j = 0; i< 512; i=i+128, j++){
                    for (int k = i; k<(i+128); k=k+16){
                        canvasScale.drawLine(k, 30, k, 25, paintScaleDisplay);
                    }
                    canvasScale.drawLine(i, 40, i, 25, paintScaleDisplay);
                    String text = Integer.toString(j) + " KHz";
                    canvasScale.drawText(text, i, 45, paintScaleDisplay);
                }
                canvas.drawBitmap(bitmapScale, 0, 0, paintScaleDisplay);
            }
            else if ((width >320) && (width<512)){
                canvasScale.drawLine(0, 30, 0 + 256, 30, paintScaleDisplay);
                for(int i = 0,j = 0; i<256; i=i+64, j++){
                    for (int k = i; k<(i+64); k=k+8){
                        canvasScale.drawLine(k, 30, k, 25, paintScaleDisplay);
                    }
                    canvasScale.drawLine(i, 40, i, 25, paintScaleDisplay);
                    String text = Integer.toString(j) + " KHz";
                    canvasScale.drawText(text, i, 45, paintScaleDisplay);
                }
                canvas.drawBitmap(bitmapScale, 0, 0, paintScaleDisplay);
            }
            else if (width <320){
                canvasScale.drawLine(0, 30,  256, 30, paintScaleDisplay);
                for(int i = 0,j = 0; i<256; i=i+64, j++){
                    for (int k = i; k<(i+64); k=k+8){
                        canvasScale.drawLine(k, 30, k, 25, paintScaleDisplay);
                    }
                    canvasScale.drawLine(i, 40, i, 25, paintScaleDisplay);
                    String text = Integer.toString(j) + " KHz";
                    canvasScale.drawText(text, i, 45, paintScaleDisplay);
                }
                canvas.drawBitmap(bitmapScale, 0, 0, paintScaleDisplay);
            }
        }
    }