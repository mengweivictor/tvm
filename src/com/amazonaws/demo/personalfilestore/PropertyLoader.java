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

import java.util.Properties;

import org.apache.log4j.Logger;

import android.util.Log;

public class PropertyLoader 
{
	private static final Logger log4j = Logger.getLogger(PropertyLoader.class);
	
    private boolean hasCredentials = false;       
    private String tokenVendingMachineURL = null;  
    private String appName = null;  
    private String bucketName = null;
    private boolean useSSL = false;  
       
    private static PropertyLoader instance = null;
    
    public static PropertyLoader getInstance() 
    {
        if ( instance == null ) 
        {
            instance = new PropertyLoader();
        }
        
        return instance;
    }       
              
    public PropertyLoader() 
    {
        try 
        {
    	    Properties properties = new Properties();
    	    properties.load( this.getClass().getResourceAsStream( "AwsCredentials.properties" ) );

    	    this.tokenVendingMachineURL = properties.getProperty( "tokenVendingMachineURL" );
    	    this.appName = properties.getProperty( "appName" );
    	    this.bucketName = properties.getProperty( "bucketName" );
    	    this.useSSL = Boolean.parseBoolean( properties.getProperty( "useSSL" ) );

            if ( this.tokenVendingMachineURL == null 
              || this.tokenVendingMachineURL.equals( "" ) 
              || this.tokenVendingMachineURL.equals( "CHANGE ME" ) ) 
            {
                this.tokenVendingMachineURL = null;
                this.appName = null;
                this.bucketName = null;
                this.useSSL = false;
                this.hasCredentials = false;
            }
            else 
            {
                this.hasCredentials = true;            
            }
        }
        catch ( Exception exception ) 
        {
            Log.e( "PropertyLoader", "Unable to read property file." );
            log4j.error("加载本地数据文件操作失败", exception);
        }
    }
    
    public boolean hasCredentials() 
    {
        return this.hasCredentials;
    }
    
    public String getTokenVendingMachineURL() 
    {
        return this.tokenVendingMachineURL;
    }
     
    public String getBucketName() 
    {
    	return this.bucketName;
    }
    
    public String getAppName() 
    {
        return this.appName;
    }
        
    public boolean useSSL() 
    {
        return this.useSSL;
    }
    
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append("hasCredentials = ").append(hasCredentials).append(", ");
        sb.append("tokenVendingMachineURL = ").append(tokenVendingMachineURL).append(", ");
        sb.append("bucketName = ").append(bucketName).append(", ");
        sb.append("appName = ").append(appName).append(", ");
        sb.append("useSSL = ").append(useSSL).append(", ");
        
        return sb.toString();
    }
        
}
