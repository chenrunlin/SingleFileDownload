package com.example.activity;

import com.example.pojo.FileInfo;
import com.example.service.DownloadService;
import com.example.singlefiledownload.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private TextView mTvFileName = null;
	private TextView mtvFileProgress = null;
	private ProgressBar mPbProgress = null;
	private Button mBtStart = null;
	private Button mBtStop = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		initComponent();
		mPbProgress.setMax(100);
		
		//创建文件对象
		final FileInfo fileInfo = new FileInfo(0, "http://www.imooc.com/mobile/imooc.apk", "imooc.apk", 0, 0);
		mTvFileName.setText(fileInfo.getFileName());
		//添加事件监听
		mBtStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent service = new Intent(MainActivity.this, DownloadService.class);
				service.setAction(DownloadService.ACTION_START);
				service.putExtra("fileInfo", fileInfo);
				startService(service);
				mTvFileName.setText(fileInfo.getFileName() + " ( 正在下载，请稍后...  )");
			}
		});
		mBtStop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent service = new Intent(MainActivity.this, DownloadService.class);
				service.setAction(DownloadService.ACTION_STOP);
				service.putExtra("fileInfo", fileInfo);
				startService(service);
				mTvFileName.setText(fileInfo.getFileName() + " ( 已停止下载...  )");
			}
		});
		
		//注册广播接收器
		IntentFilter filter = new IntentFilter();
		filter.addAction(DownloadService.ACTION_UPDATE);
		registerReceiver(mReceiver, filter);
	}
	
	/**
	 * 更新UI的广播接收器
	 */
	BroadcastReceiver mReceiver = new BroadcastReceiver(){
		
		long time = System.currentTimeMillis();
		
		public void onReceive(Context context, Intent intent) {
			
			String action = intent.getAction();
			FileInfo mFileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
			
			if(DownloadService.ACTION_UPDATE.equals(action)){
				// 更新UI
				int finished = intent.getIntExtra("finished", 0);
				String fileName = intent.getStringExtra("fileName");
				if(System.currentTimeMillis() - time > 500){
					mPbProgress.setProgress(finished);
					mTvFileName.setText(fileName + " ( 正在下载，请稍后...  )");
					mtvFileProgress.setText(finished + "%");
				}
			} else if (DownloadService.ACTION_FINISHED.equals(action)) {
				// 下载结束后重置，并弹出Toast提示
				String fileName = intent.getStringExtra("fileName");
				mBtStart.setText("Restart");
				mTvFileName.setText(fileName + " ( 下载完成  )");
				mtvFileProgress.setText("");
				Toast.makeText(MainActivity.this,
						mFileInfo.getFileName() + " 下载完成！", Toast.LENGTH_SHORT)
						.show();
			}
		}
	};
	
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mReceiver);
	}

	private void initComponent() {
		mTvFileName = (TextView) findViewById(R.id.tvFile);
		mtvFileProgress = (TextView) findViewById(R.id.tvFile_progress);
		mPbProgress = (ProgressBar) findViewById(R.id.pbFile);
		mBtStart = (Button) findViewById(R.id.btStart);
		mBtStop = (Button) findViewById(R.id.btStop);
	}

}
