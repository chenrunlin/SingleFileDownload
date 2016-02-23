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
 * 下载任务
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
		//读取数据库的线程信息
		List<ThreadInfo> threadInfos = mDao.getThread(mFileInfo.getUrl());
		ThreadInfo threadInfo = null;
		if (threadInfos.size() == 0){
			//初始化线程信息
			threadInfo= new ThreadInfo(0, mFileInfo.getUrl(), 0, mFileInfo.getLength(), 0);
		} else {
			threadInfo = threadInfos.get(0);
		}
		//创建子线程下载
		new DownLoadThread(threadInfo).start();
	}
	
	class DownLoadThread extends Thread{
		private ThreadInfo mThreadInfo;

		public DownLoadThread(ThreadInfo mThreadInfo) {
			this.mThreadInfo = mThreadInfo;
		}
		
		@Override
		public void run() {
			//1、向数据库插入线程信息
			if(!mDao.isExists(mThreadInfo.getUrl(), mThreadInfo.getId())){
				mDao.insertThread(mThreadInfo);
			}
			//2、设置下载位置
			HttpURLConnection conn = null;
			RandomAccessFile raf = null;
			InputStream input = null;
			
			try {
				URL url = new URL(mThreadInfo.getUrl());
				conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(5000);
				conn.setRequestMethod("GET");
				//设置下载位置
				int start = mThreadInfo.getStart() + mThreadInfo.getFinished();
				conn.setRequestProperty("Range", "bytes=" + start + "-" + mThreadInfo.getEnd());
				
				//3、设置文件写入位置
				File file = new File(DownloadService.DOWNLOAD_PATH, mFileInfo.getFileName());
				raf = new RandomAccessFile(file, "rwd");
				//在读写的时候跳过设置好的字节数，从下一个字节数开始读写
				//例如：seek(100)，从第101个字节开始读写
				raf.seek(start);
				
				//定义广播
				Intent intent = new Intent(DownloadService.ACTION_UPDATE);
				mFinished += mThreadInfo.getFinished();
				//4、开始下载
				if(conn.getResponseCode() == HttpStatus.SC_PARTIAL_CONTENT){
					input = conn.getInputStream();
					byte[] buffer = new byte[1024 * 4];
					int len = -1;
					long time = System.currentTimeMillis();
					//4.1、读取数据
					while((len = input.read(buffer)) != -1){
						//4.2、把读取的数据写入文件
						raf.write(buffer, 0, len);
						//4.3、把下载的进度发送广播给activity
						mFinished += len;
						//500ms刷新一次界面
				//		if(System.currentTimeMillis() - time > 500){
							time = System.currentTimeMillis();
							intent.putExtra("fileInfo", mFileInfo);
							intent.putExtra("finished", mFinished * 100 / mFileInfo.getLength());
							intent.putExtra("fileName", mFileInfo.getFileName());
							mContext.sendBroadcast(intent);
				//		}
						//4.4、在下载暂停时，把下载进度保存到数据库
						if(isPause){
							mDao.updateThread(mThreadInfo.getUrl(), mThreadInfo.getId(), mFinished);
							//结束while循环
							return ;
						}
					}
					//下载完成
					Intent finish_intent = new Intent(DownloadService.ACTION_FINISHED);
					intent.putExtra("fileInfo", mFileInfo);
					intent.putExtra("finished", mFinished * 100 / mFileInfo.getLength());
					intent.putExtra("fileName", mFileInfo.getFileName());
					mContext.sendBroadcast(finish_intent);
					//删除线程信息
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
