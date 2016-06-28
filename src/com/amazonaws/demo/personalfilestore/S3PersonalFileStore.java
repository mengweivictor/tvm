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

import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.demo.personalfilestore.s3.S3;
import com.amazonaws.demo.personalfilestore.s3.S3BucketView;
import com.amazonaws.demo.weibologin.AccessTokenKeeper;
import com.amazonaws.demo.weibologin.Constants;
import com.amazonaws.tvmclient.AmazonSharedPreferencesWrapper;
import com.amazonaws.tvmclient.Response;
import com.amazonaws.tvmclient.Utilities;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.widget.LoginButton;

public class S3PersonalFileStore extends Activity 
{
	private static final Logger log4j = Logger.getLogger(S3PersonalFileStore.class);
	
	private static final String success = "Welcome to The S3 Personal File Store!";
	private static final String fail = "Load Failed. Please Try Restarting the Application.";
	
	protected Button s3Button;
	protected Button loginButton;
	protected Button logoutButton;
	
	protected TextView welcomeText;
	
	private LoginButton mLoginBtnDefault;
	private TextView mTokenView;//for the test purpose 
	
	/** 登陆认证对应的listener */
    private AuthListener mLoginListener = new AuthListener();
    
    private AuthInfo mAuthInfo;
	
	
    public static AmazonClientManager clientManager = null;
    	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        
        setContentView(R.layout.main);
        
        
        s3Button     = (Button)findViewById(R.id.main_storage_button);
        logoutButton = (Button)findViewById(R.id.main_logout_button);
        loginButton  = (Button)findViewById(R.id.main_login_button);
        welcomeText = (TextView)findViewById(R.id.main_into_text);
        
        mAuthInfo = new AuthInfo(this, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE);
        mLoginBtnDefault = (LoginButton)findViewById(R.id.loginButtond_defalut);
        mLoginBtnDefault.setWeiboAuthInfo(mAuthInfo, mLoginListener);
      
        mTokenView = (TextView)findViewById(R.id.weibologin_status);
        
        

        clientManager = 
            new AmazonClientManager( 
                getSharedPreferences( 
                    "com.amazon.aws.demo.AWSDemo", Context.MODE_PRIVATE ), getApplicationContext() );
        
        AWSCredentials credentials  = 
            AmazonSharedPreferencesWrapper.getCredentialsFromSharedPreferences(
                getSharedPreferences("com.amazon.aws.demo.AWSDemo", Context.MODE_PRIVATE ));
        
        
     	if ( !S3PersonalFileStore.clientManager.hasCredentials() ) 
     	{
    		this.displayCredentialsIssueAndExit();
    		
    		welcomeText.setText(fail);
        }
        else if ( !S3PersonalFileStore.clientManager.isLoggedIn() )
        {
            welcomeText.setText(success);
            loginButton.setVisibility(View.VISIBLE);
            mLoginBtnDefault.setVisibility(View.VISIBLE);
    		this.wireButtons();
        }
        else 
        {
    		welcomeText.setText(success);
    		s3Button.setVisibility(View.VISIBLE);
            logoutButton.setVisibility(View.VISIBLE);
            mLoginBtnDefault.setVisibility(View.INVISIBLE);
    		this.wireButtons();
    	} 
    }
    
    protected void onResume() 
    {
        super.onResume();
        
        if ( !S3PersonalFileStore.clientManager.isLoggedIn() ) 
        {
            welcomeText.setText(success);
            loginButton.setVisibility(View.VISIBLE);
            mLoginBtnDefault.setVisibility(View.VISIBLE);
    		this.wireButtons();
        }
        else 
        {
            loginButton.setVisibility(View.INVISIBLE);
        
    		welcomeText.setText(success);
    		s3Button.setVisibility(View.VISIBLE);
            logoutButton.setVisibility(View.VISIBLE);
            mLoginBtnDefault.setVisibility(View.INVISIBLE);
    		this.wireButtons();
    	} 
        
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);        
        mLoginBtnDefault.onActivityResult(requestCode, resultCode, data);
    }
        
    private void wireButtons()
    {
    	//My Objects
		s3Button.setOnClickListener(new View.OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
                Response response = S3PersonalFileStore.clientManager.validateCredentials();
                
                if ( response != null && response.requestWasSuccessful() ) 
                {
    				Intent bucketViewIntent = new Intent(S3PersonalFileStore.this, S3BucketView.class);
    				
    				
    				bucketViewIntent.putExtra(S3.BUCKET_NAME, PropertyLoader.getInstance().getBucketName() );
    				bucketViewIntent.putExtra(S3.PREFIX, S3PersonalFileStore.clientManager.getUsername() );
    				
    				startActivity(bucketViewIntent);
                }
                else 
                {
                	if(response.getResponseCode() == 888)
                	{
                		logoutAction();
                		//Utilities.MyToast(getApplicationContext(), "weibotoken error please relogin!");
                		
                	}
                	else 
                	{
                		S3PersonalFileStore.this.displayErrorAndExit( response );                    
					}
                	
                }
			}
		});
        
		logoutButton.setOnClickListener(new View.OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
                logoutAction();
			}
		});  
              
		loginButton.setOnClickListener(new View.OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
    			startActivity(new Intent(S3PersonalFileStore.this, Login.class));                
			}
		});        
    }
    private void logoutAction() {
    	
    	clientManager.clearCredentials();  
        clientManager.wipe();
        //清楚weibo AccessToken
        AccessTokenKeeper.clear(getApplicationContext());
        
        //displayLogoutSuccess();
        
	    s3Button.setVisibility(View.INVISIBLE);
        logoutButton.setVisibility(View.INVISIBLE);
        loginButton.setVisibility(View.VISIBLE); 
        mLoginBtnDefault.setVisibility(View.VISIBLE);
	}
        
    protected void displayCredentialsIssueAndExit() 
    {
        AlertDialog.Builder confirm = new AlertDialog.Builder( this );
        
        confirm.setTitle("Credential Problem!");
        confirm.setMessage( "AWS Credentials not configured correctly.  Please review the README file." );
        
        confirm.setNegativeButton( "OK", new DialogInterface.OnClickListener() 
        {
            public void onClick( DialogInterface dialog, int which ) 
            {
            	S3PersonalFileStore.this.finish();
            }
        } );
        
        confirm.show().show();                
    }
    
    protected void displayErrorAndExit( Response response ) 
    {
        AlertDialog.Builder confirm = new AlertDialog.Builder( this );
        
        if ( response == null ) 
        { 
        	confirm.setTitle("Error Code Unkown" );
        	confirm.setMessage( "Please review the README file." );
        } 
        else 
        {
        	confirm.setTitle( "Error Code [" + response.getResponseCode() + "]" );
            confirm.setMessage( response.getResponseMessage() + "\nPlease review the README file."  );
        }
        
        confirm.setNegativeButton( "OK", new DialogInterface.OnClickListener() 
        {
            public void onClick( DialogInterface dialog, int which ) 
            {
            	S3PersonalFileStore.this.finish();
            }
        } );
        
        confirm.show().show();                
    }

    protected void displayLogoutSuccess() 
    {
        AlertDialog.Builder confirm = new AlertDialog.Builder( this );
        
        confirm.setTitle("Logout");
        confirm.setMessage( "You have successfully logged out." );
        
        confirm.setNegativeButton( 
            "OK", 
            new DialogInterface.OnClickListener() 
            {
                public void onClick( DialogInterface dialog, int which ) 
                {
                }
            });
        
        confirm.show().show();                
    }
    
    
    /**
     * 登入按钮的监听器，接收授权结果。
     */
    private class AuthListener implements WeiboAuthListener {
        @Override
        public void onComplete(Bundle values) {
            Oauth2AccessToken accessToken = Oauth2AccessToken.parseAccessToken(values);
            if (accessToken != null && accessToken.isSessionValid()) {
                String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(
                        new java.util.Date(accessToken.getExpiresTime()));
                String format = getString(R.string.weibosdk_demo_token_to_string_format_1);
                mTokenView.setText(String.format(format, accessToken.getToken(), date));
                AccessTokenKeeper.writeAccessToken(getApplicationContext(), accessToken);
                
                
                Response response = 
                		S3PersonalFileStore.clientManager.login(accessToken);
                if ( response != null && response.getResponseCode() == 404 ) 
                {
                	log4j.info("weibologin not found!");
                }
                else if ( response != null && response.getResponseCode() != 200 ) 
                {
                	log4j.info("weibologin error!!!!");
                }
            }  
        }

        @Override
        public void onWeiboException(WeiboException e) {
            Toast.makeText(S3PersonalFileStore.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel() {
            Toast.makeText(S3PersonalFileStore.this, 
                    R.string.weibosdk_demo_toast_auth_canceled, Toast.LENGTH_SHORT).show();
        }
    }
}
