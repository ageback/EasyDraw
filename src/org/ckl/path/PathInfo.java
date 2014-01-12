package org.ckl.path;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Environment;
import android.util.Log;

public class PathInfo implements Serializable {
	private static final long serialVersionUID = -5568568529548959041L;
	private static final String TAG = "PathInfo";
	
	private OnUndoRedoListener mListener;
	
	public interface OnUndoRedoListener
	{
		void onUndoLimit();
		
		void onRedoLimit();
	}

	class SerPoint implements Serializable {
		private static final long serialVersionUID = -2262755099592284491L;

		private float x;
		private float y;

		public SerPoint(float x, float y) {
			this.x = x;
			this.y = y;
		}
	}
	
	class SerPath implements Serializable {
		private static final long serialVersionUID = -900016536427010833L;
		private int mColor = Color.BLACK;
		private float mStrokeWidth = 8;
		private List<SerPoint> mPoints = new ArrayList<SerPoint>();
	}
	
	List<SerPath> mSerPaths = new ArrayList<PathInfo.SerPath>();
	List<SerPath> mRemovedSerPaths = new ArrayList<PathInfo.SerPath>();
	
	private SerPath mCurPath;
	
	private static PathInfo instance;
	
	public static PathInfo getInstance()
	{
		if (instance == null) {
			instance = new PathInfo();
		}
		return instance;
	}
	
	public void undo() {
		if (mSerPaths != null && mSerPaths.size() >0)
		{
			SerPath removedPath = mSerPaths.remove(mSerPaths.size() - 1);
			if (mRemovedSerPaths == null) {
				mRemovedSerPaths = new ArrayList<PathInfo.SerPath>();
			}
			mRemovedSerPaths.add(removedPath);
			if (mSerPaths.size() == 0) {
				mListener.onUndoLimit();
			}
		}
	}
	
	public void redo() {
		if (mRemovedSerPaths.size() > 0) {
			mSerPaths.add(mRemovedSerPaths.remove(mRemovedSerPaths.size() - 1));
			if (mRemovedSerPaths.size() == 0) {
				mListener.onRedoLimit();
			}
		}
	}
	
	public void lineStart(float x, float y) {
		mCurPath = new SerPath();
		mCurPath.mPoints.add(new SerPoint(x, y));
	}
	public void lineMove(float x, float y) {
		mCurPath.mPoints.add(new SerPoint(x, y));
	}
	public void lineEnd(float x, float y, int color, float width) {
		mCurPath.mPoints.add(new SerPoint(x, y));
		mCurPath.mColor = color;
		mCurPath.mStrokeWidth = width;
		mSerPaths.add(mCurPath);
	}
	
	
	private PathInfo() {
		instance = this;
	}
	//-0-0--------------------------------------------
	private Paint transferPaint(SerPath sp) {
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(sp.mStrokeWidth);
		paint.setColor(sp.mColor);
		return paint;
	}
	private Path transferPath(SerPath sp) {
		Path path = new Path();
		SerPoint p;
		int size = sp.mPoints.size();
		
		if (size < 3) {
			return path;
		}
		
		p = sp.mPoints.get(0);
		path.moveTo(p.x, p.y);
		
		float ox = p.x;
		float oy = p.y;
		
		for (int i = 1; i < size-1; i++) {
			p = sp.mPoints.get(i);
			path.quadTo(ox, oy, (ox + p.x)/2, (oy + p.y)/2);
			ox = p.x;
			oy = p.y;
		}
		
		p = sp.mPoints.get(size-1);
		path.lineTo(p.x, p.y);
		
		return path;
	}
	public List<PathAndPaint> transfer() {
		List<PathAndPaint> pps = new ArrayList<PathAndPaint>();
//		Log.i(TAG, "mSerPaths.size() = " + mSerPaths.size());
		for (SerPath sp : mSerPaths) {
			Paint paint = transferPaint(sp);
			Path path = transferPath(sp);
			pps.add(new PathAndPaint(path, paint));
		}
		return pps;
	}

	//-1-1--------------------------------------------
	private static String mSavePath = "/sdcard/pathinfo";
	public static PathInfo load() {
		PathInfo pi = null;
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new FileInputStream(mSavePath));
			pi = (PathInfo)ois.readObject();
			Log.i(TAG, "load ok, size = " + pi.mSerPaths.size());
		} catch (StreamCorruptedException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (ois != null) {
				try {
					ois.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				ois = null;
			}
			if (pi == null) {
				pi = new PathInfo();
			}
		}
		return pi;
	}
	
	public PathInfo loadText()
	{
		try {
			BufferedReader  in = new BufferedReader(new FileReader(textFilePath));
			String line;
			while ((line=in.readLine()) != null) {
				String[] paths = line.split(";");
				for (int i = 0; i < paths.length; i++) {
					String[] linePart1 = paths[i].split("\\*");
					float strokeWidth = Float.parseFloat(linePart1[0].substring(0, linePart1[0].length()-2));
					String[] linePart2 = linePart1[1].split("@");
					int color = Color.parseColor(linePart2[0]);
					String points= linePart2[1].split(";")[0];
					String[] pointsArray = points.split("\\|");
					SerPath sp  = new SerPath();
					sp.mColor = color;
					sp.mStrokeWidth = strokeWidth;
					
					for (int j = 0; j < pointsArray.length; j++) {
						int x = Integer.parseInt(pointsArray[j].split(",")[0]);
						int y = Integer.parseInt(pointsArray[j].split(",")[1]);
						sp.mPoints.add(new SerPoint(x,y));
					}
					mSerPaths.add(sp);
				}
			}
			in.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			Log.e("PathInfo", "文件解析错误");
		}
		
		return this;
	}
	
	String textFilePath = Environment.getExternalStorageDirectory()+"/pathinfo.txt";
	public void saveText() {
		DecimalFormat df = new DecimalFormat("#");
		StringBuilder sb = new StringBuilder();
	    //在SD卡目录下创建文件smsLog.txt文件，true表示当文件存在时，信息追加在文件尾   
		try {
			FileWriter fw = new FileWriter(textFilePath, false);
			for (SerPath path : mSerPaths) {
				sb.append(path.mStrokeWidth);
				sb.append("px*#");
				sb.append(Integer.toHexString(path.mColor).substring(2));
				sb.append("@");
				for (int i = 0; i < path.mPoints.size(); i++) {
					sb.append(df.format(path.mPoints.get(i).x));
					sb.append(",");
					sb.append(df.format(path.mPoints.get(i).y));
					if (i != path.mPoints.size() -1) {
						sb.append("|");
					} else {
						sb.append(";");
					}
				}
			}
			fw.write(sb.toString());
			fw.flush();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    
		
	}
	
	public void save() {
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(mSavePath));
			oos.writeObject(this);
			Log.i(TAG, "save ok, size = " + mSerPaths.size());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (oos != null) {
				try {
					oos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				oos = null;
			}	
		}
	}
	
	public void clean() {
		File f = new File(mSavePath);
		if (f.exists()) {
			f.delete();
		}
		mSerPaths.clear();
		mRemovedSerPaths.clear();
	}

	public OnUndoRedoListener getmListener() {
		return mListener;
	}

	public void setmListener(OnUndoRedoListener mListener) {
		this.mListener = mListener;
	}
}
