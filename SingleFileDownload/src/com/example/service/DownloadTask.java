package com.example.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.Buffer;
import java.util.List;

import org.apache.http.HttpStatus;

import android.content.Context;
import android.content.Intent;

import com.example.db.ThreadDao;
import com.example.db.ThreadDaoImpl;
import com.example.pojo.FileInfo;
import com.example.pojo.ThreadInfo;

/**
 * ��������
 * @author net
 */
public class DownloadTask {

	private Context mContext;
	private FileInfo mFileInfo;
	private ThreadDao mDao;
	
	private int mFinished;
	public boolean isPause = false;
	
	public DownloadTask(Context mContext, FileInfo mFileInfo) {
		super();
		this.mContext = mContext;
		this.mFileInfo = mFileInfo;
		mDao = new ThreadDaoImpl(mContext);
	}
	
	public void download(){
		//��ȡ���ݿ���߳���Ϣ
		List<ThreadInfo> threadInfos = mDao.getThread(mFileInfo.getUrl());
		ThreadInfo threadInfo = null;
		if (threadInfos.size() == 0){
			//��ʼ���߳���Ϣ
			threadInfo= new ThreadInfo(0, mFileInfo.getUrl(), 0, mFileInfo.getLength(), 0);
		} else {
			threadInfo = threadInfos.get(0);
		}
		//�������߳�����
		new DownLoadThread(threadInfo).start();
	}
	
	class DownLoadThread extends Thread{
		private ThreadInfo mThreadInfo;

		public DownLoadThread(ThreadInfo mThreadInfo) {
			this.mThreadInfo = mThreadInfo;
		}
		
		@Override
		public void run() {
			//1�������ݿ�����߳���Ϣ
			if(!mDao.isExists(mThreadInfo.getUrl(), mThreadInfo.getId())){
				mDao.insertThread(mThreadInfo);
			}
			//2����������λ��
			HttpURLConnection conn = null;
			RandomAccessFile raf = null;
			InputStream input = null;
			
			try {
				URL url = new URL(mThreadInfo.getUrl());
				conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(5000);
				conn.setRequestMethod("GET");
				//��������λ��
				int start = mThreadInfo.getStart() + mThreadInfo.getFinished();
				conn.setRequestProperty("Range", "bytes=" + start + "-" + mThreadInfo.getEnd());
				
				//3�������ļ�д��λ��
				File file = new File(DownloadService.DOWNLOAD_PATH, mFileInfo.getFileName());
				raf = new RandomAccessFile(file, "rwd");
				//�ڶ�д��ʱ���������úõ��ֽ���������һ���ֽ�����ʼ��д
				//���磺seek(100)���ӵ�101���ֽڿ�ʼ��д
				raf.seek(start);
				
				//����㲥
				Intent intent = new Intent(DownloadService.ACTION_UPDATE);
				mFinished += mThreadInfo.getFinished();
				//4����ʼ����
				if(conn.getResponseCode() == HttpStatus.SC_PARTIAL_CONTENT){
					input = conn.getInputStream();
					byte[] buffer = new byte[1024 * 4];
					int len = -1;
					long time = System.currentTimeMillis();
					//4.1����ȡ����
					while((len = input.read(buffer)) != -1){
						//4.2���Ѷ�ȡ������д���ļ�
						raf.write(buffer, 0, len);
						//4.3�������صĽ��ȷ��͹㲥��activity
						mFinished += len;
						//500msˢ��һ�ν���
				//		if(System.currentTimeMillis() - time > 500){
							time = System.currentTimeMillis();
							intent.putExtra("fileInfo", mFileInfo);
							intent.putExtra("finished", mFinished * 100 / mFileInfo.getLength());
							intent.putExtra("fileName", mFileInfo.getFileName());
							mContext.sendBroadcast(intent);
				//		}
						//4.4����������ͣʱ�������ؽ��ȱ��浽���ݿ�
						if(isPause){
							mDao.updateThread(mThreadInfo.getUrl(), mThreadInfo.getId(), mFinished);
							//����whileѭ��
							return ;
						}
					}
					//�������
					Intent finish_intent = new Intent(DownloadService.ACTION_FINISHED);
					intent.putExtra("fileInfo", mFileInfo);
					intent.putExtra("finished", mFinished * 100 / mFileInfo.getLength());
					intent.putExtra("fileName", mFileInfo.getFileName());
					mContext.sendBroadcast(finish_intent);
					//ɾ���߳���Ϣ
					mDao.deleteThread(mThreadInfo.getUrl(), mThreadInfo.getId());
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					conn.disconnect();
					raf.close();
					input.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
