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

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.log4j.Logger;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.demo.personalfilestore.R;
import com.amazonaws.demo.personalfilestore.filebrowser.FileUtil;

//S3������ϸ��ʾҳ
public class S3ObjectView extends Activity
{
	
    /**
     * ��־����
     */
    private static final Logger log = Logger.getLogger(S3ObjectView.class);
    
	protected Handler mHandler;
	
	protected TextView loadingText;
	protected TextView bodyText;
	
	/**
     * ����ͼ�οؼ�
     */
    private ImageView imageView;
    
    /**
     * ��ǰ��Ļ�ֱ���
     */
    private DisplayMetrics dm;
    
    private Bitmap lastBitmap;

	
	protected String bucketName;
	protected String objectName;
	protected String objectData;
	
	//���ص�ͼƬ�ļ�����
	protected File imageFile;

	protected Button delete;
	
	private final Runnable postResults = new Runnable() 
	{
		@Override
		public void run()
		{
			updateUi();
		}
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_view);
        
        Bundle extras = this.getIntent().getExtras();
        
        dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        log.debug("onCreate() dm.heightPixels = " + dm.heightPixels);
        
        bucketName = extras.getString(S3.BUCKET_NAME);
        objectName = extras.getString(S3.OBJECT_NAME);
        
        mHandler = new Handler();
        
        loadingText = (TextView) findViewById(R.id.item_view_loading_text);
        bodyText    = (TextView) findViewById(R.id.item_view_body_text);
        
        imageView = (ImageView)findViewById(R.id.item_view_body_image);
        
        delete = (Button)findViewById(R.id.delete_item);   
        
        wireOnClick();
        startPopulateText();
    }
    
    private void startPopulateText()
    {
    	Thread t = new Thread() 
    	{
    		@Override
    		public void run()
    		{
    		    //���е�ͼƬ�ļ�������image��ͷ
    		    if (objectName.contains("image"))
    		    {
    		        imageFile = S3.getFileForObject(bucketName, objectName);
    		    } 
    		    else
    		    {    
    		        objectData = S3.getDataForObject(bucketName, objectName);
    		    }
    			
    	        mHandler.post(postResults);
    		}
    	};
    	
    	t.start();
    }
    
    private void updateUi()
    {
        if (objectName.contains("image"))
        {    
            displayImage(imageFile.getAbsolutePath());
            loadingText.setText(objectName);
            loadingText.setTextSize(16);
        }
        else
        {
            loadingText.setText(objectName);
            bodyText.setText(objectData);
            loadingText.setTextSize(16);
        }    
        
    }	
    
    /**
    *
    * ��ʾ�û�ͨ������Ļѡ���ͼƬ
    *
    * @param filePath ѡ���ͼƬ
    *
    */
   private void displayImage(String filePath)
   {

       File file = new File(filePath);
       
       if (!file.exists())
       {
           log.warn("displayImage() ͼƬ�ļ�" + filePath + "������");
       }   
       else
       {
           log.info("displayImage() ͼƬ�ļ�" + filePath + "�Ĵ�СΪ" + file.length());
       }    
       
       Uri fileUri = Uri.fromFile(file);

       try
       {
           Bitmap bitmap = FileUtil.safeDecodeStream(false,
                                                     fileUri,
                                                     dm.widthPixels,
                                                     (int)(dm.heightPixels * 0.775),
                                                     this);

           //���ͼƬ���򲻶ԣ���Ҫ��ת
           int degree = FileUtil.readPictureDegree(file.getAbsolutePath());
           bitmap = FileUtil.rotateBitmap(bitmap, degree);

           imageView.setImageBitmap(bitmap);
           
           log.warn("displayImage() ͼƬ�ļ�" + filePath + "�����ʾ");

           //�����һ��ͼƬ����
           if (lastBitmap != null)
           {
               lastBitmap.recycle();

               lastBitmap = bitmap;
           }

       }
       catch (FileNotFoundException e)
       {
           log.error("displayImage()�ļ�������", e);
       }
       catch (Exception e)
       {
           log.error("displayImage()��ʾ�ļ�����ʧ��", e);
       }
   }
    
	protected void wireOnClick()
	{
	    //ɾ����ǰ��S3����
		this.delete.setOnClickListener( new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				S3.deleteObject(S3ObjectView.this.bucketName, 
				                S3ObjectView.this.objectName );
				finish();
			}
		});
	}
    
}
