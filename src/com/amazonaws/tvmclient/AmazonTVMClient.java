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
package com.amazonaws.tvmclient;

import java.security.SecureRandom;

import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;

import android.content.SharedPreferences;
import android.util.Log;

/**
* This class is uzsed to communicate with the Token Vending Machine specific for this application. 
*/
public class AmazonTVMClient 
{
	
    private static final String LOG_TAG = "AmazonTVMClient";

    /**
     * The endpoint for the Token Vending Machine to connect to.
     * 
     * 目前是TVM EC2服务器的公网IP地址
     */
    private String endpoint;
    
    /**
     * The appName declared by the Token Vending Machine.
     * TVM EC2服务器上配置的应用名称
     */
    private String appName;
    
    /**
     * Use SSL when making connections to the Token Vending Machine.
     * 
     * 是否启用SSL连接（目前没有启用）
     * 
     */
    private boolean useSSL;
    
    /**
     * The shared preferences where credentials are other aws access information is stored.
     * 
     * 本地保存了获取的临时证书
     * 
     */
    private SharedPreferences sharedPreferences;
    
    private static final Logger log4j = Logger.getLogger(AmazonTVMClient.class);
    
    
    public AmazonTVMClient(SharedPreferences sharedPreferences, 
                           String endpoint, 
                           String appName, 
                           boolean useSSL ) 
    {
        this.endpoint = this.getEndpointDomainName( endpoint.toLowerCase() );
        this.appName  = appName.toLowerCase();
        this.useSSL   = useSSL;
        
        this.sharedPreferences = sharedPreferences;
    }
        
    /**
     * Gets a token from the Token Vending Machine.  The registered key is used to secure the communication.
     */
    public Response getToken() 
    {
        String uid = AmazonSharedPreferencesWrapper.getUidForDevice( this.sharedPreferences );
        String key = AmazonSharedPreferencesWrapper.getKeyForDevice( this.sharedPreferences );
        
        //构造请求对象
        Request getTokenRequest = new GetTokenRequest( this.endpoint, this.useSSL, uid, key );
        
        log4j.debug("getTokenRequest;{" + getTokenRequest + "}");
        
        //获取临时证书响应处理器
        ResponseHandler handler = new GetTokenResponseHandler( key );

        //执行具体的远程请求，获取响应结果对象
        GetTokenResponse getTokenResponse = 
                (GetTokenResponse)this.processRequest( getTokenRequest, handler );
        
        log4j.debug("getTokenResponse;{" + getTokenResponse + "}");
        
        if ( getTokenResponse.requestWasSuccessful() ) 
        {
            
            log4j.info("getToken()" + "获取临时证书操作成功，将临时证书保存本地");
            
            AmazonSharedPreferencesWrapper.storeCredentialsInSharedPreferences(this.sharedPreferences, 
                                                                               getTokenResponse.getAccessKey(), 
                                                                               getTokenResponse.getSecretKey(), 
                                                                               getTokenResponse.getSecurityToken(), 
                                                                               getTokenResponse.getExpirationDate() );
        }

        return getTokenResponse;
    }

    /**
     * Using the given username and password, securily communictes the Key for the user's account.
     */
    public Response login( String username, String password ) 
    {
        Response response = Response.SUCCESSFUL;
        
        if ( AmazonSharedPreferencesWrapper.getUidForDevice( this.sharedPreferences ) == null ) 
        {
            String uid = AmazonTVMClient.generateRandomString();
            
            LoginRequest loginRequest = new LoginRequest(this.endpoint,
                                                         this.useSSL, 
                                                         this.appName, 
                                                         uid, 
                                                         username, 
                                                         password );
            
            
            log4j.debug("login() " + "loginRequest;{" + loginRequest + "}");
            
            ResponseHandler handler = new LoginResponseHandler( loginRequest.getDecryptionKey() );
            
            
            log4j.debug("login() " + "response;{" + response + "}");

            response = this.processRequest( loginRequest, handler );
            
            if ( response.requestWasSuccessful() ) 
            {
                AmazonSharedPreferencesWrapper.registerDeviceId(this.sharedPreferences, 
                                                                uid, 
                                                                ((LoginResponse)response).getKey());
                
                AmazonSharedPreferencesWrapper.storeUsername( this.sharedPreferences, username );                        
            }
            
        }

        return response;
    }
    
    /**
     *  Process Request
     */
    protected Response processRequest( Request request, ResponseHandler handler ) 
    {
        Response response = null;
        int retries = 2;
        
        //发送最多预定次数的远程请求
        do 
        {            
            response = TokenVendingMachineService.sendRequest( request, handler );
            
            if ( response.requestWasSuccessful() ) 
            {   
                log4j.debug("processRequest()" + "远程操作成功， response = {" + response + "}");
                
                return response;
            }
            else 
            {
                
                log4j.error("processRequest()" + "远程操作失败， response = {" + response + "}");
                
                Log.w( LOG_TAG, "Request to Token Vending Machine failed with Code: " 
                              + "[" + response.getResponseCode() + "] " 
                              + "Message: [" + response.getResponseMessage() + "]" );
            }
        }
        while ( retries-- > 0 );       
        
        return response;                     
    } 
     
    
    /**
     * Creates a 128 bit random string..
     */
    public static String generateRandomString() 
    {
		SecureRandom random = new SecureRandom();
		byte[] randomBytes = random.generateSeed( 16 );
		String randomString = new String( Hex.encodeHex( randomBytes ) );
		return randomString;
	}
    
    //从URL中解析出域名或者IP
    private String getEndpointDomainName( String endpoint ) 
    {
    	int startIndex = 0;
    	int endIndex = 0;
    	
    	if ( endpoint.startsWith( "http://") || endpoint.startsWith( "https://") ) 
    	{
    		startIndex = endpoint.indexOf( "://" ) + 3;
    	}
    	else 
    	{
    		startIndex = 0;
    	}
    	
    	if ( endpoint.charAt( endpoint.length() - 1 ) == '/' ) 
    	{
    		endIndex = endpoint.length() - 1;
    	}
    	else 
    	{
    		endIndex = endpoint.length();
    	}
    	
    	return endpoint.substring( startIndex, endIndex );
    }

}
