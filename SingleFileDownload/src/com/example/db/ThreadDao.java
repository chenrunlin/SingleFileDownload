package com.example.db;

import java.util.List;

import com.example.pojo.ThreadInfo;

public interface ThreadDao{
	/**
	 * 插入线程
	 * @param threadInfo
	 */
	void insertThread(ThreadInfo threadInfo);
	/**
	 * 更新线程下载进度
	 * @param url
	 * @param thread_id
	 * @param finished
	 */
	void updateThread(String url, int thread_id, int finished);
	/**
	 * 删除线程
	 * @param url
	 * @param thread_id
	 */
	void deleteThread(String url, int thread_id);
	/**
	 * 查询文件的线程信息
	 * @param url
	 * @return
	 */
	List<ThreadInfo> getThread(String url);
	/**
	 * 线程信息是否存在
	 * @param url
	 * @param thread_id
	 * @return
	 */
	boolean isExists(String url, int thread_id);

}
