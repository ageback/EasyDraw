package org.ckl.path;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

public class HandWriteView extends ImageView {

	private static final String TAG = "MyView";
	private List<PathAndPaint> mPens = new ArrayList<PathAndPaint>();
	private Path mPath = new Path();
	private Paint mPaint = new Paint();
	private PathInfo mPathInfo;
//	private int[] mColors = new int[]{Color.BLACK, Color.RED, Color.GREEN, Color.BLUE, Color.CYAN, Color.YELLOW};
	private Bitmap obmp;
	private int mColor;
	private int mWidth;
//	private Map<Path, Integer> colorsMap = new HashMap<Path, Integer>();    
	private ArrayList<Path> mPaths = new ArrayList<Path>();
	private OnPaintListener mListener;
	
	public interface OnPaintListener{
		void onPaint();
	}
	
	private void init() {
		Log.i(TAG, "init()");
		
		mPaint.setAntiAlias(true);
//		mPaint.setColor(Color.BLACK);
		setPaintColor(Color.BLACK);
		mPaint.setStyle(Paint.Style.STROKE);
		setPaintStrokeWidth(8);
	}
	public void setPaintColor(int color)
	{
		mPaint.setColor(color);
		mColor = color;
	}
	
	public void setPaintStrokeWidth(int width)
	{
		mPaint.setStrokeWidth(width);
		mWidth=width;
	}
	
	public void saveImage()
	 {
		setDrawingCacheEnabled(true);
		obmp = Bitmap.createBitmap(getDrawingCache());
		setDrawingCacheEnabled(false);
		FileOutputStream m_fileOutPutStream = null;

		String filepath = Environment.getExternalStorageDirectory()
				+ File.separator + "tempPhoto.png";
		try
		{
			m_fileOutPutStream = new FileOutputStream(filepath);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		obmp.compress(CompressFormat.PNG, 70, m_fileOutPutStream);
		try
		{
			m_fileOutPutStream.flush();
			m_fileOutPutStream.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public HandWriteView(Context context) {
		super(context);
		init();
	}

	public HandWriteView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public HandWriteView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	public void setPathInfo(PathInfo info) {
		mPath = new Path();
		mPathInfo = info;
		mPens = mPathInfo.transfer();
		invalidate();
	}
	
	private int getColor() {
//		int index = (int) (Math.round(Math.random() * mColors.length) % mColors.length);
//		return mColors[index];
		return Color.BLACK;
	}
	/**
	 * 撤销的核心思想就是将画布清空， 将保存下来的Path路径最后一个移除掉， 重新将路径画在画布上面。
	 */
	public void undo() {
		mPathInfo.undo();
		setPathInfo(mPathInfo);
		invalidate();// 刷新
	}
	
	public void redo()
	{
		mPathInfo.redo();
		setPathInfo(mPathInfo);
		invalidate();// 刷新
	}
	

	
	private boolean mHasMove = false;
	private float mX,mY;
	public boolean onTouchEvent(MotionEvent e) {
		boolean ret = false;
		switch (e.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mHasMove = false;
			mX = e.getX();
			mY = e.getY();
			mPath.reset();
			mPath.moveTo(mX, mY);
			if (mPathInfo != null) {
				mPathInfo.lineStart(mX, mY);
			}
			invalidate();
			ret = true;
			break;
		case MotionEvent.ACTION_UP:
			mX = e.getX();
			mY = e.getY();
			mPath.lineTo(mX, mY);
			if (mPathInfo != null && mHasMove) {
				mPathInfo.lineEnd(mX, mY, mPaint.getColor(), mPaint.getStrokeWidth());
			}
//			Log.i(TAG, "mPath.lineTo("+mX+"f,"+mY+"f);");
//			Path newPath=new Path(mPath);
//			Paint newPaint=new Paint(mPaint);
			mPaint.setColor(mColor);
			mPens.add(new PathAndPaint(mPath, mPaint, mColor, mWidth));
			mPaths.add(mPath);
//			colorsMap.put(mPath,mColor);
			mPath = new Path();
			mPath.reset();
			invalidate();
			ret = true;
			break;
		case MotionEvent.ACTION_MOVE:
			mHasMove = true;
			float x = e.getX();
			float y = e.getY();
			mPath.quadTo(mX, mY, (mX + x)/2, (mY + y)/2);
			if (mPathInfo != null) {
				mPathInfo.lineMove(x, y);
			}
//			Log.i(TAG, "mPath.quadTo("+mX+"f,"+mY+"f,"+(mX + x)/2+"f,"+(mY + y)/2+"f);");
			mX = x;
			mY = y;
			ret = true;
			invalidate();
			mListener.onPaint();
			break;
		default:
			ret = super.onTouchEvent(e);
			break;
		}
		return ret;
	}

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		for (PathAndPaint pp : mPens) {
			mPaint.setColor(pp.getColor());
			mPaint.setStrokeWidth(pp.getWidth());
			canvas.drawPath(pp.getPath(), pp.getPaint());
		}
		mPaint.setColor(mColor);
		mPaint.setStrokeWidth(mWidth);
		canvas.drawPath(mPath, mPaint);
	}
	public OnPaintListener getmListener() {
		return mListener;
	}
	public void setmListener(OnPaintListener mListener) {
		this.mListener = mListener;
	}
}
