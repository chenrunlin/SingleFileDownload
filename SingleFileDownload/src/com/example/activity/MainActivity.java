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
		
		//�����ļ�����
		final FileInfo fileInfo = new FileInfo(0, "http://www.imooc.com/mobile/imooc.apk", "imooc.apk", 0, 0);
		mTvFileName.setText(fileInfo.getFileName());
		//����¼�����
		mBtStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent service = new Intent(MainActivity.this, DownloadService.class);
				service.setAction(DownloadService.ACTION_START);
				service.putExtra("fileInfo", fileInfo);
				startService(service);
				mTvFileName.setText(fileInfo.getFileName() + " ( �������أ����Ժ�...  )");
			}
		});
		mBtStop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent service = new Intent(MainActivity.this, DownloadService.class);
				service.setAction(DownloadService.ACTION_STOP);
				service.putExtra("fileInfo", fileInfo);
				startService(service);
				mTvFileName.setText(fileInfo.getFileName() + " ( ��ֹͣ����...  )");
			}
		});
		
		//ע��㲥������
		IntentFilter filter = new IntentFilter();
		filter.addAction(DownloadService.ACTION_UPDATE);
		registerReceiver(mReceiver, filter);
	}
	
	/**
	 * ����UI�Ĺ㲥������
	 */
	BroadcastReceiver mReceiver = new BroadcastReceiver(){
		
		long time = System.currentTimeMillis();
		
		public void onReceive(Context context, Intent intent) {
			
			String action = intent.getAction();
			FileInfo mFileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
			
			if(DownloadService.ACTION_UPDATE.equals(action)){
				// ����UI
				int finished = intent.getIntExtra("finished", 0);
				String fileName = intent.getStringExtra("fileName");
				if(System.currentTimeMillis() - time > 500){
					mPbProgress.setProgress(finished);
					mTvFileName.setText(fileName + " ( �������أ����Ժ�...  )");
					mtvFileProgress.setText(finished + "%");
				}
			} else if (DownloadService.ACTION_FINISHED.equals(action)) {
				// ���ؽ��������ã�������Toast��ʾ
				String fileName = intent.getStringExtra("fileName");
				mBtStart.setText("Restart");
				mTvFileName.setText(fileName + " ( �������  )");
				mtvFileProgress.setText("");
				Toast.makeText(MainActivity.this,
						mFileInfo.getFileName() + " ������ɣ�", Toast.LENGTH_SHORT)
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
