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

import org.apache.log4j.Logger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.demo.personalfilestore.s3.S3;
import com.amazonaws.demo.personalfilestore.s3.S3BucketView;
import com.amazonaws.tvmclient.AmazonSharedPreferencesWrapper;
import com.amazonaws.tvmclient.Response;

public class S3PersonalFileStore extends Activity 
{
	private static final Logger log4j = Logger.getLogger(S3PersonalFileStore.class);
	
	private static final String success = "Welcome to The S3 Personal File Store!";
	private static final String fail = "Load Failed. Please Try Restarting the Application.";
	
	protected Button s3Button;
	protected Button loginButton;
	protected Button logoutButton;
	
	protected TextView welcomeText;
	
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

        clientManager = 
            new AmazonClientManager( 
                getSharedPreferences( 
                    "com.amazon.aws.demo.AWSDemo", Context.MODE_PRIVATE ) );
        
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
    		this.wireButtons();
        }
        else 
        {
    		welcomeText.setText(success);
    		s3Button.setVisibility(View.VISIBLE);
            logoutButton.setVisibility(View.VISIBLE);
            
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
    		this.wireButtons();
        }
        else 
        {
            loginButton.setVisibility(View.INVISIBLE);
        
    		welcomeText.setText(success);
    		s3Button.setVisibility(View.VISIBLE);
            logoutButton.setVisibility(View.VISIBLE);
            
    		this.wireButtons();
    	} 
        
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
                	S3PersonalFileStore.this.displayErrorAndExit( response );                    
                }
			}
		});
        
		logoutButton.setOnClickListener(new View.OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
                clientManager.clearCredentials();  
                clientManager.wipe();   
                
                displayLogoutSuccess();
                
    		    s3Button.setVisibility(View.INVISIBLE);
                logoutButton.setVisibility(View.INVISIBLE);
                loginButton.setVisibility(View.VISIBLE);                
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
}
