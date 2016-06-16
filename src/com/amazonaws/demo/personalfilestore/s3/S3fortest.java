package com.amazonaws.demo.personalfilestore.s3;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.amazonaws.demo.personalfilestore.S3PersonalFileStore;
import com.amazonaws.demo.personalfilestore.S3PersonalFileStoreApplication;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class S3fortest {

	public static List<String> getObjectNamesForBucket(String bucketName, String prefix, int numItems,
			AmazonS3Client s3Client) {
		// ����S3�Ķ����о��������
		ListObjectsRequest req = new ListObjectsRequest();

		req.setMaxKeys(new Integer(numItems));// ���ؼ�¼������
		req.setBucketName(bucketName);// �洢Ͱ����
		req.setPrefix(prefix + "/");// �û�Ŀ¼���֣�ͬ�û�����

		ObjectListing objects = null;

		try {
			objects = s3Client.listObjects(req);

		} catch (Exception e) {
			System.out.println("getObjectNamesForBucket������ȡS3�ϵĶ��������б����ִ��ʧ��.");

			e.printStackTrace();
		}

		if (objects == null) {
			return new ArrayList<String>();
		}

		List<String> objectNames = new ArrayList<String>(objects.getObjectSummaries().size());
		Iterator<S3ObjectSummary> oIter = objects.getObjectSummaries().iterator();

		while (oIter.hasNext()) {
			objectNames.add(oIter.next().getKey().substring(prefix.length() + 1));
		}

		return objectNames;
	}

	public static void createObjectForBucket(String bucketName, String objectName, String data,
			AmazonS3Client s3Client) {
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(data.getBytes());
			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentLength(data.getBytes().length);

			s3Client.putObject(bucketName, objectName, bais, metadata);

		} catch (Exception exception) {
			System.out.println("����S3����" + bucketName + "/" + objectName + "����ʧ��");

			exception.printStackTrace();

		}
	}

}
