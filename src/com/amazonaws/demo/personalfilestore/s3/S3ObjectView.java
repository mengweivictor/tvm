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

//S3内容详细显示页
public class S3ObjectView extends Activity
{
	
    /**
     * 日志对象
     */
    private static final Logger log = Logger.getLogger(S3ObjectView.class);
    
	protected Handler mHandler;
	
	protected TextView loadingText;
	protected TextView bodyText;
	
	/**
     * 界面图形控件
     */
    private ImageView imageView;
    
    /**
     * 当前屏幕分辨率
     */
    private DisplayMetrics dm;
    
    private Bitmap lastBitmap;

	
	protected String bucketName;
	protected String objectName;
	protected String objectData;
	
	//下载的图片文件对象
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
    		    //所有的图片文件都是以image开头
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
    * 显示用户通滑动屏幕选择的图片
    *
    * @param filePath 选择的图片
    *
    */
   private void displayImage(String filePath)
   {

       File file = new File(filePath);
       
       if (!file.exists())
       {
           log.warn("displayImage() 图片文件" + filePath + "不存在");
       }   
       else
       {
           log.info("displayImage() 图片文件" + filePath + "的大小为" + file.length());
       }    
       
       Uri fileUri = Uri.fromFile(file);

       try
       {
           Bitmap bitmap = FileUtil.safeDecodeStream(false,
                                                     fileUri,
                                                     dm.widthPixels,
                                                     (int)(dm.heightPixels * 0.775),
                                                     this);

           //如果图片方向不对，需要旋转
           int degree = FileUtil.readPictureDegree(file.getAbsolutePath());
           bitmap = FileUtil.rotateBitmap(bitmap, degree);

           imageView.setImageBitmap(bitmap);
           
           log.warn("displayImage() 图片文件" + filePath + "完成显示");

           //清除上一个图片对象
           if (lastBitmap != null)
           {
               lastBitmap.recycle();

               lastBitmap = bitmap;
           }

       }
       catch (FileNotFoundException e)
       {
           log.error("displayImage()文件不存在", e);
       }
       catch (Exception e)
       {
           log.error("displayImage()显示文件操作失败", e);
       }
   }
    
	protected void wireOnClick()
	{
	    //删除当前的S3对象
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
