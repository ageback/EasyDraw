package org.ckl.path;

import org.ckl.path.HandWriteView.OnPaintListener;
import org.ckl.path.PathInfo.OnUndoRedoListener;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class HandWriteActivity extends Activity implements OnUndoRedoListener, OnPaintListener {
	private PathInfo mPathInfo;
	private HandWriteView mMyView;
	private Button mClean, mUndo, mRedo, mColor;
	private ColorPickerDialog dialog;  
	private Spinner mStrokeSpinner;
	
	private String[] mStrokeSize = new String[] {"5","7","9","11","13","15","17","19","21","23"};
	ActionBar actionBar;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
//        actionBar = getActionBar();
//        actionBar.show();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, mStrokeSize); 
        
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mStrokeSpinner = (Spinner) findViewById(R.id.strokesize);
        mStrokeSpinner.setAdapter(adapter);
        mStrokeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            	mMyView.setPaintStrokeWidth(Integer.parseInt(mStrokeSpinner.getSelectedItem().toString()));
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
//        mPathInfo = PathInfo.load();
        mPathInfo = PathInfo.getInstance().loadText();
        mPathInfo.setmListener(this);

        mMyView = (HandWriteView)findViewById(R.id.myview);
        mMyView.setPathInfo(mPathInfo);
        
        mMyView.setmListener(this);
        
        mColor = (Button)findViewById(R.id.color);
        mColor.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				dialog = new ColorPickerDialog(HandWriteActivity.this,
						Color.BLACK, "Ñ¡ÔñÑÕÉ«",
						new ColorPickerDialog.OnColorChangedListener() {
							public void colorChanged(int color) {
								mMyView.setPaintColor(color);
							}

						});
				dialog.show();
			}
		});
        
        mClean = (Button)findViewById(R.id.clean);
        mClean.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mMyView.saveImage();
				mPathInfo.clean();
				mUndo.setEnabled(false);
				mRedo.setEnabled(false);
				mMyView.setPathInfo(mPathInfo);
			}
		});
        
        mRedo = (Button)findViewById(R.id.redo);
        mRedo.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				mMyView.redo();
				mUndo.setEnabled(true);
			}
		});
        
        mUndo = (Button)findViewById(R.id.back);
        mUndo.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
//				SavePathActivity.this.finish();
				mMyView.undo();
				mRedo.setEnabled(true);
			}
		});
    }
    
    protected void onDestroy() {
    	if (mPathInfo != null) {
    		mPathInfo.saveText();
    		mPathInfo = null;
    	}
    	super.onDestroy();
    }

	public void onUndoLimit() {
		mUndo.setEnabled(false);
	}

	public void onRedoLimit() {
		mRedo.setEnabled(false);
		
	}

	public void onPaint() {
		// TODO Auto-generated method stub
		mUndo.setEnabled(true);
	}
}