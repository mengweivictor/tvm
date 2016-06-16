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

	//��ȡS3��ָ���洢Ͱ������
	public static List<String> getObjectNamesForBucket(String bucketName, 
			                                           String prefix, 
			                                           int numItems) 
	{
		//����S3�Ķ����о��������
		ListObjectsRequest req= new ListObjectsRequest();
		
		req.setMaxKeys(new Integer(numItems));//���ؼ�¼������
		req.setBucketName(bucketName);//�洢Ͱ����
		req.setPrefix(prefix + "/");//�û�Ŀ¼���֣�ͬ�û���)
	
		
		ObjectListing objects = null;
		
		try
		{
			objects = getInstance().listObjects( req );
			
		}
		catch (Exception e)
		{
			log4j.error("getObjectNamesForBucket������ȡS3�ϵĶ��������б����ִ��ʧ��.", e);
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
	
	//��S3�洢Ͱ������ַ�������
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
			log4j.error("����S3����" + bucketName + "/" + objectName + "����ʧ��", exception);
			
		}
	}
	
	
	//��S3�洢Ͱ����� �ļ�����
    public static void createFileObjectForBucket( String bucketName, File dataFile ) 
    {
        
        
        //���챻�ϴ����ļ���
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
            log4j.error("createFileObjectForBucket���� ����S3����" + bucketName + "/" + objectName + "����ʧ��", exception);
            
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
			log4j.error("ɾ��S3����" + bucketName + "/" + objectName + "����ʧ��", e);
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
			log4j.error("��ȡS3����" + bucketName + "/" + objectName + "���ݲ���ʧ��", e);
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
            log4j.debug("getFileForObject���� ��ʱͼƬ�ļ�:" + tempFile.getAbsolutePath() + "�����ɹ�");
        } 
        else
        {
            log4j.warn("getFileForObject���� ��ʱͼƬ�ļ�:" + tempFile.getAbsolutePath() + "����ʧ��");
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
            log4j.error("��ȡS3����" + bucketName + "/" + objectName + "���ݲ���ʧ��", e);
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
                    log4j.error("�ر��ļ����������ʧ��", e);
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
