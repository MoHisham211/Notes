package mo.zain.notes.drawing;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;



public class DrawingView extends View {
    public static final String TAG = "paint";

    private Path drawPath; //drawing path
    private Paint drawPaint, canvasPaint;
    private int paintColor = 0xff660000;
    private Canvas c;
    private Bitmap canvasBitmap;
    private float brushSize,lastBrushSize;
    private boolean erase = false;

    public DrawingView(Context ctx, AttributeSet attrs){
        super(ctx,attrs);
        setUpDrawing();
    }

    private void setUpDrawing(){
        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(20);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG);

    }

    public boolean onTouchEvent(MotionEvent event){
        float touchX = event.getX();
        float touchY = event.getY();
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                drawPath.moveTo(touchX,touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX,touchY);
                break;
            case MotionEvent.ACTION_UP:
                c.drawPath(drawPath,drawPaint);
                drawPath.reset();
                break;
            default:
                return false;
        }//end switch
        invalidate();

        return true;

    }//end ontouchevent

    protected void onDraw(Canvas canvas){
        try {
            canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
            canvas.drawPath(drawPath, drawPaint);
        }
        catch (NullPointerException npe){
            Log.d(TAG, "error");

        }

    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight){
        super.onSizeChanged(width,height,oldWidth,oldHeight);
        canvasBitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        c = new Canvas(canvasBitmap);
    }


    public void setColor(String newColor){
        invalidate();
        paintColor = Color.parseColor(newColor);
        drawPaint.setColor(paintColor);
    }//end set color

    public void setBrushSize(float newSize){
        float pixelAmount = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                newSize, getResources().getDisplayMetrics());
        brushSize = pixelAmount;
        drawPaint.setStrokeWidth(brushSize);
    }//end setbrushsize

    public void setLastBrushSize(float lastSize){
        lastBrushSize = lastSize;
    }//end setlastbrushsize

    public float getLastBrushSize(){
        return lastBrushSize;
    }


    public void setErase(boolean isErase){
        erase = isErase;
        if (isErase){
            drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        }
        else {
            drawPaint.setXfermode(null);
        }
    }//end set erase

    public void startNew(){
        c.drawColor(0,PorterDuff.Mode.CLEAR);
        invalidate();
    }



}