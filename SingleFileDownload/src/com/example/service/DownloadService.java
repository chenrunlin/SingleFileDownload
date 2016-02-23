package com.example.service;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpStatus;

import com.example.pojo.FileInfo;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class DownloadService extends Service {
	
	public static final String DOWNLOAD_PATH = 
			Environment.getExternalStorageDirectory().getAbsolutePath() + "/download/";
	public static final String ACTION_START = "ACTION_START";
	public static final String ACTION_STOP = "ACTION_STOP";
	public static final String ACTION_UPDATE = "ACTION_UPDATE";
	public static final String ACTION_FINISHED = "ACTION_FINISHED";
	public static final int MSG_INIT = 0;
	private DownloadTask mTask = null;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//��ȡactivity�����Ĳ���
		if(ACTION_START.equals(intent.getAction())){
			FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
			Log.i("test", "start : " + fileInfo.toString());
			//������ʼ������
			new InitThread(fileInfo).start();
		}
		if(ACTION_STOP.equals(intent.getAction())){
			FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
			Log.i("test", "stop : " + fileInfo.toString());
			if(mTask != null){
				mTask.isPause = true;
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_INIT:
				FileInfo fileInfo = (FileInfo) msg.obj;
				Log.i("test", "Init : " + fileInfo);
				//������������
				mTask = new DownloadTask(DownloadService.this, fileInfo);
				mTask.download();
				break;
			}
		}
	};
	
	/**
	 * ��ʼ�������߳�
	 */
	class InitThread extends Thread{
		
		private FileInfo mFileInfo;
		
		public InitThread(FileInfo mFileInfo) {
			this.mFileInfo = mFileInfo;
		}
		
		@Override
		public void run() {
			HttpURLConnection conn = null;
			RandomAccessFile raf = null;
			try{
				//1�������ļ�
				URL url = new URL(mFileInfo.getUrl());
				conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(3000);
				conn.setRequestMethod("GET");
				
				//2����ȡ�ļ�����
				int length = -1;
				//����һ�£����Ƿ����ӳɹ�����Ӧ�����Ƿ���OK
				if(conn.getResponseCode() == HttpStatus.SC_OK){
					//��ȡ�ļ�����
					length = conn.getContentLength();
				}
				if(length <= 0){
					return ;
				}
				//����Ŀ¼
				File dir = new File(DOWNLOAD_PATH);
				if(!dir.exists()){
					dir.mkdir();
				}
				
				//3���ڱ��ش����ļ�
				File file = new File(dir, mFileInfo.getFileName());
				raf = new RandomAccessFile(file, "rwd");
				//4�������ļ����ȣ����ļ����ȷ��ظ�service
				raf.setLength(length);
				mFileInfo.setLength(length);
				handler.obtainMessage(MSG_INIT, mFileInfo).sendToTarget();
			}catch (Exception e) {
				e.printStackTrace();
			}finally {
				try {
					raf.close();
					conn.disconnect();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}