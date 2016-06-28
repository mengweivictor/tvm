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
package com.amazonaws.demo.personalfilestore;


import java.util.List;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;

import android.R.string;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Credentials;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.demo.personalfilestore.s3.S3fortest;
import com.amazonaws.demo.weibologin.AccessTokenKeeper;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.model.NetworkAclEntry;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.tvmclient.AmazonSharedPreferencesWrapper;
import com.amazonaws.tvmclient.AmazonTVMClient;
import com.amazonaws.tvmclient.HTTPPost;
import com.amazonaws.tvmclient.Response;
import com.amazonaws.tvmclient.Utilities;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;

/**
* This class is used to get clients to the various AWS services.  Before accessing a client 
* the credentials should be checked to ensure validity.
* 
* 
*/
public class AmazonClientManager 
{
    private static final String LOG_TAG = "AmazonClientManager";
    private static final Logger log4j = Logger.getLogger(AmazonClientManager.class);

    private AmazonS3Client s3Client = null;
    private SharedPreferences sharedPreferences = null;
    private Context mContext;
	    
    public AmazonClientManager( SharedPreferences settings,Context context) 
    {
        this.sharedPreferences = settings;
        mContext = context;
    }
                
    public AmazonS3Client s3() 
    {
        validateCredentials();
        
        return s3Client;
    }
        
    public boolean hasCredentials() 
    {
        return PropertyLoader.getInstance().hasCredentials();
    }

    public boolean isLoggedIn() 
    {
        return ( AmazonSharedPreferencesWrapper.getUidForDevice( this.sharedPreferences ) != null 
              && AmazonSharedPreferencesWrapper.getKeyForDevice( this.sharedPreferences ) != null );
    }

    public Response login( String username, String password ) 
    {
        AmazonTVMClient tvm = 
            new AmazonTVMClient( this.sharedPreferences, 
                                 PropertyLoader.getInstance().getTokenVendingMachineURL(), 
                                 PropertyLoader.getInstance().getAppName(), 
                                 PropertyLoader.getInstance().useSSL() );
        
        return tvm.login( username, password );
    }
    public Response login(Oauth2AccessToken accessToken) {
    	 AmazonTVMClient tvm = 
    	            new AmazonTVMClient( this.sharedPreferences, 
    	                                 PropertyLoader.getInstance().getTokenVendingMachineURL(), 
    	                                 PropertyLoader.getInstance().getAppName(), 
    	                                 PropertyLoader.getInstance().useSSL() );
    	        
	        return tvm.login( accessToken );
	}
    public boolean validateWeiboAccesstoken() {
    	Oauth2AccessToken accessToken = AccessTokenKeeper.readAccessToken(mContext);
    	String url = "https://api.weibo.com/oauth2/get_token_info";
    	String param = "access_token=" + accessToken.getToken();
    	//String param = "access_token=" + "2.00bhIu2CvqnMbB871cc1f554pStgJC";
    	String response = HTTPPost.sendRequest(url, param);
//    	String url = "https://api.weibo.com/2/account/get_uid.json";
//    	String param = "?access_token=" + "asdfs";
//    	url += param;
//    	String response = HTTPPost.sendRequestGet(url);
    	if (response == null) {
			log4j.error("network error,validateWeiboAccesstoken  error");
			Utilities.MyToast(mContext, "weibo token校验错误！");
			return false;
		}
    	String error_code = Utilities.extractNumber(response, "error_code");
    	if(error_code == null)
    	{
    		//不存在error_code,说明成功
    		return true;
    	}
    	else 
    	{
    		/***
    		 * 此处可以根据返回的错误码分别处理
    		 */
    		Utilities.MyToast(mContext, "weibo token错误,error_code is :" + error_code);
    		return false;
		}
	}
    
    public Response validateCredentials() 
    {
        Response ableToGetToken = Response.SUCCESSFUL;

        if (AmazonSharedPreferencesWrapper.areCredentialsExpired( this.sharedPreferences ) ) 
        {
        	if(!this.sharedPreferences.contains("access_token") || validateWeiboAccesstoken())
        	{
        		Log.i( LOG_TAG, "Credentials were expired." );
                log4j.warn("validateCredentials()证书已经过期");
            
                clearCredentials();        
            
                AmazonTVMClient tvm = 
                    new AmazonTVMClient(this.sharedPreferences, 
                                        PropertyLoader.getInstance().getTokenVendingMachineURL(), 
                                        PropertyLoader.getInstance().getAppName(), 
                                        PropertyLoader.getInstance().useSSL() );
                
                if ( ableToGetToken.requestWasSuccessful() ) 
                {
                    ableToGetToken = tvm.getToken();            
                }
        	}
        	else
        	{
        		ableToGetToken = new Response(888, "weibo token error");
        	}
           
        }

        if (ableToGetToken != null && ableToGetToken.requestWasSuccessful() && s3Client == null ) 
        {        
            AWSCredentials credentials = 
                AmazonSharedPreferencesWrapper.getCredentialsFromSharedPreferences( this.sharedPreferences );
            
		    s3Client = new AmazonS3Client( credentials );
		    s3Client.setRegion(Region.getRegion(Regions.CN_NORTH_1));

        }
        
        return ableToGetToken;
    }
    
    public String getUsername() 
    {
    	return AmazonSharedPreferencesWrapper.getUsername( this.sharedPreferences );
    }
    
    public void clearCredentials() 
    {
        s3Client = null;
    }

    public void wipe() 
    {
        AmazonSharedPreferencesWrapper.wipe( this.sharedPreferences );
    }
}
