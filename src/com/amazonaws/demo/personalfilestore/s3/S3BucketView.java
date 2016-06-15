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


import java.util.List;

import org.apache.log4j.Logger;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.amazonaws.demo.personalfilestore.CustomListActivity;
import com.amazonaws.demo.personalfilestore.filebrowser.FileBrowserActivity;

//S3文件列表视图的实现子类
public class S3BucketView extends CustomListActivity 
{
    private static final Logger log4j = Logger.getLogger(S3BucketView.class);
	
	protected List<String> objectNameList;
	protected String bucketName;
	protected String prefix;
	
	private static final String SUCCESS = "Object List";
	private static final int NUM_OBJECTS = 30;
	
	private final Runnable postResults = new Runnable() 
	{
		@Override
		public void run()
		{
			updateUi(objectNameList, SUCCESS);
		}
	};
		
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        Bundle extras = this.getIntent().getExtras();
        
        bucketName = extras.getString(S3.BUCKET_NAME);
        prefix = extras.getString(S3.PREFIX);
        
        
        //启动后台线程获取S3对象
        startPopulateList();
    }

    @Override
    public void onResume() 
    {
    	super.onResume();
        startPopulateList();
    }

    protected void obtainListItems()
    {
        //调用S3客户端方法获取存储桶列表
		objectNameList = 
		    S3.getObjectNamesForBucket(bucketName, 
		                               prefix, 
		                               NUM_OBJECTS);
		
        getHandler().post(postResults);
    }
        
	protected void wireOnListClick()
	{
	    //点击S3文件名，启动文件下载和显示视图
		getItemList().setOnItemClickListener(new OnItemClickListener() 
		{
		    @Override
		    public void onItemClick(AdapterView<?> list, View view, int position, long id) 
		    {
		        final String objectName = ((TextView)view).getText().toString();
		        
				Intent objectViewIntent = new Intent(S3BucketView.this, S3ObjectView.class);
					
				    objectViewIntent.putExtra( S3.BUCKET_NAME, bucketName);
				    objectViewIntent.putExtra( S3.OBJECT_NAME, S3BucketView.this.prefix + "/" + objectName );
					
				startActivity(objectViewIntent);
		    }
		 });
	}
    		
	protected void wireOnClick()
	{
	    //添加文本按钮事件
		this.addText.setOnClickListener( new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				Intent addObjectViewIntent = new Intent( S3BucketView.this, S3AddObjectView.class);
				
				    addObjectViewIntent.putExtra( S3.BUCKET_NAME, bucketName);
				    addObjectViewIntent.putExtra( S3.PREFIX, S3BucketView.this.prefix);
				    
				startActivity(addObjectViewIntent);
			}
		});
		
		
		//选择图片按钮事件
        this.selectImage.setOnClickListener( new OnClickListener() 
        {
            @Override
            public void onClick(View v) 
            {
                log4j.warn("onCreate() 进入选择图片按钮事件方法");
                
                Intent selectImageViewIntent = new Intent( S3BucketView.this, FileBrowserActivity.class);
                    selectImageViewIntent.putExtra( S3.BUCKET_NAME, bucketName);
                    selectImageViewIntent.putExtra( S3.PREFIX, S3BucketView.this.prefix);
                    
                startActivity(selectImageViewIntent);
            }
        });
	}
}
