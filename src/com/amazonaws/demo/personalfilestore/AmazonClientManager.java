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

import org.apache.log4j.Logger;

import android.content.SharedPreferences;
import android.net.Credentials;
import android.util.Log;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.demo.personalfilestore.s3.S3fortest;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.tvmclient.AmazonSharedPreferencesWrapper;
import com.amazonaws.tvmclient.AmazonTVMClient;
import com.amazonaws.tvmclient.Response;

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
	    
    public AmazonClientManager( SharedPreferences settings ) 
    {
        this.sharedPreferences = settings;
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
    
    
    public Response validateCredentials() 
    {
        Response ableToGetToken = Response.SUCCESSFUL;

        if (AmazonSharedPreferencesWrapper.areCredentialsExpired( this.sharedPreferences ) ) 
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
            
            
//            /*this code is for test purpose*/
//            AWSCredentials testcredentials = 
//                    AmazonSharedPreferencesWrapper.getCredentialsFromSharedPreferences( this.sharedPreferences );
//	        		
//	        AmazonS3Client s3Client = new AmazonS3Client( testcredentials);
//	        s3Client.setEndpoint("s3.cn-north-1.amazonaws.com.cn");
//	        
//	        List<String> testresult =S3fortest.getObjectNamesForBucket("tvm-examplebucket", 
//													                    "mwtestuser1",
//													                    10,
//													                    s3Client);
//	        log4j.info(testresult);
        }

        if (ableToGetToken.requestWasSuccessful() && s3Client == null ) 
        {        
            AWSCredentials credentials = 
                AmazonSharedPreferencesWrapper.getCredentialsFromSharedPreferences( this.sharedPreferences );
            
		    s3Client = new AmazonS3Client( credentials );
		    s3Client.setRegion(Region.getRegion(Regions.CN_NORTH_1));
		    
		    
		    /*this code is for test purpose*/
	        
//	        List<String> testresult =S3fortest.getObjectNamesForBucket("tvm-examplebucket", 
//													                    "mwtestuser1",
//													                    10,
//													                    s3Client);
//	        log4j.info(testresult);

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
