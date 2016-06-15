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
     * Ŀǰ��TVM EC2�������Ĺ���IP��ַ
     */
    private String endpoint;
    
    /**
     * The appName declared by the Token Vending Machine.
     * TVM EC2�����������õ�Ӧ������
     */
    private String appName;
    
    /**
     * Use SSL when making connections to the Token Vending Machine.
     * 
     * �Ƿ�����SSL���ӣ�Ŀǰû�����ã�
     * 
     */
    private boolean useSSL;
    
    /**
     * The shared preferences where credentials are other aws access information is stored.
     * 
     * ���ر����˻�ȡ����ʱ֤��
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
        
        //�����������
        Request getTokenRequest = new GetTokenRequest( this.endpoint, this.useSSL, uid, key );
        
        log4j.debug("getTokenRequest;{" + getTokenRequest + "}");
        
        //��ȡ��ʱ֤����Ӧ������
        ResponseHandler handler = new GetTokenResponseHandler( key );

        //ִ�о����Զ�����󣬻�ȡ��Ӧ�������
        GetTokenResponse getTokenResponse = 
                (GetTokenResponse)this.processRequest( getTokenRequest, handler );
        
        log4j.debug("getTokenResponse;{" + getTokenResponse + "}");
        
        if ( getTokenResponse.requestWasSuccessful() ) 
        {
            
            log4j.info("getToken()" + "��ȡ��ʱ֤������ɹ�������ʱ֤�鱣�汾��");
            
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
        
        //�������Ԥ��������Զ������
        do 
        {            
            response = TokenVendingMachineService.sendRequest( request, handler );
            
            if ( response.requestWasSuccessful() ) 
            {   
                log4j.debug("processRequest()" + "Զ�̲����ɹ��� response = {" + response + "}");
                
                return response;
            }
            else 
            {
                
                log4j.error("processRequest()" + "Զ�̲���ʧ�ܣ� response = {" + response + "}");
                
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
    
    //��URL�н�������������IP
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
