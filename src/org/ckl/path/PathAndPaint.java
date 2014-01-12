package org.ckl.path;

import android.graphics.Paint;
import android.graphics.Path;

public class PathAndPaint {
	private Path mPath;
	private Paint mPaint;
	
	private int mColor;
	private int mWidth;
	
	public PathAndPaint(Path path, Paint paint) {
		mPath = path;
		mPaint = paint;
	}
	
	public PathAndPaint(Path path, Paint paint, int color) {
		mPath = path;
		mPaint = paint;
		mColor=color;
	}
	
	public PathAndPaint(Path path, Paint paint, int color, int width) {
		mPath = path;
		mPaint = paint;
		mColor=color;
		mWidth= width;
	}
	
	public Path getPath() {
		return mPath;
	}
	public Paint getPaint() {
		return mPaint;
	}
	
	public int getColor()
	{
		return mColor;
	}
	
	public int getWidth()
	{
		return mWidth;
	}
}
