/*
 * Copyright 2010-2012 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
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

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.demo.personalfilestore.S3PersonalFileStore;
import com.amazonaws.demo.personalfilestore.S3PersonalFileStoreApplication;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class S3 
{
	
	private static final Logger log4j = Logger.getLogger(S3.class);
	
	public static final String BUCKET_NAME = "_bucket_name";
	public static final String OBJECT_NAME = "_object_name";
	public static final String PREFIX = "prefix";
	
	public static final String TEMP_FILE_PRIFIX = "temp_image";
			
	public static AmazonS3 getInstance() 
	{
        return S3PersonalFileStore.clientManager.s3();
	}

	//获取S3上指定存储桶的内容
	public static List<String> getObjectNamesForBucket(String bucketName, 
			                                           String prefix, 
			                                           int numItems) 
	{
		//构造S3的对象列举请求对象
		ListObjectsRequest req= new ListObjectsRequest();
		
		req.setMaxKeys(new Integer(numItems));//返回记录数限制
		req.setBucketName(bucketName);//存储桶名字
		req.setPrefix(prefix + "/");//用户目录名字（同用户名)
	
		
		ObjectListing objects = null;
		
		try
		{
			objects = getInstance().listObjects( req );
			
		}
		catch (Exception e)
		{
			log4j.error("getObjectNamesForBucket（）获取S3上的对象名称列表操作执行失败.", e);
		}
		
		if (objects == null)
		{
			return new ArrayList<String>();
		}
		
		
		List<String> objectNames = new ArrayList<String>( objects.getObjectSummaries().size());
		Iterator<S3ObjectSummary> oIter = objects.getObjectSummaries().iterator();
		
		while(oIter.hasNext())
		{
			objectNames.add( oIter.next()
					              .getKey()
					              .substring( prefix.length() + 1 ));
		}

		return objectNames;		
	}
	
	//向S3存储桶中添加字符串对象
	public static void createObjectForBucket( String bucketName, String objectName, String data ) 
	{
		try 
		{
			ByteArrayInputStream bais = new ByteArrayInputStream( data.getBytes() );
			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentLength( data.getBytes().length );
			
			getInstance().putObject(bucketName, objectName, bais, metadata );
			
		}
		catch ( Exception exception ) 
		{
			log4j.error("创建S3对象" + bucketName + "/" + objectName + "操作失败", exception);
			
		}
	}
	
	
	//向S3存储桶中添加 文件对象
    public static void createFileObjectForBucket( String bucketName, File dataFile ) 
    {
        
        
        //构造被上传的文件名
        String objectName = S3PersonalFileStore.clientManager.getUsername() 
                          + "/" 
                          + "image" 
                          + System.currentTimeMillis() 
                          + ".jpg";
        
        InputStream input = null;
        
        try 
        {
            
            
            input = new FileInputStream(dataFile);  
          
            byte[] dataBytes = new byte[input.available()];  
            
            input.read(dataBytes);  
            
            ByteArrayInputStream bais = new ByteArrayInputStream(dataBytes );
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength( dataBytes.length );
            
            getInstance().putObject(bucketName, objectName, bais, metadata );
            
            
        }
        catch ( Exception exception ) 
        {
            log4j.error("createFileObjectForBucket（） 创建S3对象" + bucketName + "/" + objectName + "操作失败", exception);
            
        }
        finally
        {
            if (input != null)
            {
                try
                {
                    input.close();
                }
                catch (IOException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }        
        }
    }
	
	public static void deleteObject( String bucketName, String objectName ) 
	{
		
		try
		{
			getInstance().deleteObject( bucketName, objectName );
			
		}
		catch (Exception e)
		{
			log4j.error("删除S3对象" + bucketName + "/" + objectName + "操作失败", e);
		}
		
	}

	public static String getDataForObject( String bucketName, String objectName ) 
	{
		
		
		String data = null;
		try
		{
			data = read( getInstance().getObject( bucketName, objectName ).getObjectContent() );
		}
		catch (Exception e)
		{
			log4j.error("读取S3对象" + bucketName + "/" + objectName + "内容操作失败", e);
		}
		
		return data;
	}
	
	public static File getFileForObject( String bucketName, String objectName ) 
    {
        String tempFileName = S3PersonalFileStoreApplication.getInstance().getImageDir() 
                            + TEMP_FILE_PRIFIX 
                            + System.currentTimeMillis() 
                            + ".jpg";
        
        File tempFile = new File(tempFileName);
        FileOutputStream tempFileOutputStream = null;
        
        if (tempFile.exists())
        {
            log4j.debug("getFileForObject（） 临时图片文件:" + tempFile.getAbsolutePath() + "创建成功");
        } 
        else
        {
            log4j.warn("getFileForObject（） 临时图片文件:" + tempFile.getAbsolutePath() + "创建失败");
        }    
        
        
        try
        {
            
            tempFileOutputStream = new FileOutputStream(tempFile);
            
            InputStream inputStream = getInstance().getObject( bucketName, objectName )
                                                   .getObjectContent();
            
            int length = 0;
            byte[] buffer = new byte[1024];
            
            while ( ( length = inputStream.read( buffer ) ) > 0 ) 
            {
                tempFileOutputStream.write( buffer, 0, length );
            }
            
        }
        catch (Exception e)
        {
            log4j.error("读取S3对象" + bucketName + "/" + objectName + "内容操作失败", e);
        }
        finally
        {
            if (tempFileOutputStream != null)
            {
                try
                {
                    tempFileOutputStream.close();
                }
                catch (IOException e)
                {
                    log4j.error("关闭文件输出流操作失败", e);
                }
            }    
        }
        
        return tempFile;
    }
	
	protected static String read( InputStream stream ) 
	{
		try 
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream( 8196 );
			byte[] buffer = new byte[1024];
			int length = 0;
			
			while ( ( length = stream.read( buffer ) ) > 0 ) 
			{
				baos.write( buffer, 0, length );
			}
			
			return baos.toString();
		}
		catch ( Exception exception ) 
		{
			return exception.getMessage();
		}
	}
}
