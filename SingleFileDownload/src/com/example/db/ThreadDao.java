package com.example.db;

import java.util.List;

import com.example.pojo.ThreadInfo;

public interface ThreadDao{
	/**
	 * �����߳�
	 * @param threadInfo
	 */
	void insertThread(ThreadInfo threadInfo);
	/**
	 * �����߳����ؽ���
	 * @param url
	 * @param thread_id
	 * @param finished
	 */
	void updateThread(String url, int thread_id, int finished);
	/**
	 * ɾ���߳�
	 * @param url
	 * @param thread_id
	 */
	void deleteThread(String url, int thread_id);
	/**
	 * ��ѯ�ļ����߳���Ϣ
	 * @param url
	 * @return
	 */
	List<ThreadInfo> getThread(String url);
	/**
	 * �߳���Ϣ�Ƿ����
	 * @param url
	 * @param thread_id
	 * @return
	 */
	boolean isExists(String url, int thread_id);

}
