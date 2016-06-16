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
		// 构造S3的对象列举请求对象
		ListObjectsRequest req = new ListObjectsRequest();

		req.setMaxKeys(new Integer(numItems));// 返回记录数限制
		req.setBucketName(bucketName);// 存储桶名字
		req.setPrefix(prefix + "/");// 用户目录名字（同用户名）

		ObjectListing objects = null;

		try {
			objects = s3Client.listObjects(req);

		} catch (Exception e) {
			System.out.println("getObjectNamesForBucket（）获取S3上的对象名称列表操作执行失败.");

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
			System.out.println("创建S3对象" + bucketName + "/" + objectName + "操作失败");

			exception.printStackTrace();

		}
	}

}
