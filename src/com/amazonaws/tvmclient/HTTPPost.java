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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.log4j.Logger;

import android.util.Log;

public class HTTPPost 
{
    private static final String LOG_TAG = "HTTPPost";
    private static final String ERROR = "Internal Server Error";
    
    private static final Logger log4j = 
    		Logger.getLogger(HTTPPost.class);
    
    
    public static String sendRequest( String url_conn,String param) 
    {
        int responseCode = 0;
        
        String responseBody = null;
        String requestUrl = url_conn;
        
        try 
        {
            log4j.debug("sendRequest() ��Զ�̷������������� : [" + requestUrl + "]");
            
            URL url = new URL( requestUrl );
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            // �����Ƿ���httpUrlConnection�������Ϊ�����post���󣬲���Ҫ����  
            // http�����ڣ������Ҫ��Ϊtrue, Ĭ���������false;  
            connection.setDoOutput(true);          
            // �����Ƿ��httpUrlConnection���룬Ĭ���������true;  
            connection.setDoInput(true);  
            // Post ������ʹ�û���  
            connection.setUseCaches(false);  
            connection.setRequestMethod("POST"); 
            OutputStream os = connection.getOutputStream();   
            os.write(param.getBytes());  
            responseCode = connection.getResponseCode();
            responseBody = HTTPPost.getResponse( connection );
            log4j.debug("sendRequest() ��ȡ������Զ�̷���������ӦΪ : [" + responseBody + "]");
            
            return responseBody;
        }
        catch ( IOException exception ) 
        {
            Log.w( LOG_TAG, exception );  
            
            log4j.error("HTTPԶ�̲�������ʧ��", exception);
            
           return null;
        }        
        catch ( Exception exception ) 
        {
            Log.w( LOG_TAG, exception );  
            log4j.error("HTTPԶ�̲�������ʧ��", exception);
            
            return null;
        }
    }  
    
    public static String sendRequestGet(String url_conn)
    {
    	int responseCode = 0;
        
        String responseBody = null;
        String requestUrl = url_conn;
        
        try 
        {
            log4j.debug("sendRequest() ��Զ�̷������������� : [" + requestUrl + "]");
            
            URL url = new URL( requestUrl );
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
           
            responseCode = connection.getResponseCode();
            responseBody = HTTPPost.getResponse( connection );
            log4j.debug("sendRequest() ��ȡ������Զ�̷���������ӦΪ : [" + responseBody + "]");
            
            return responseBody;
        }
        catch ( IOException exception ) 
        {
            Log.w( LOG_TAG, exception );  
            
            log4j.error("HTTPԶ�̲�������ʧ��", exception);
            
           return null;
        }        
        catch ( Exception exception ) 
        {
            Log.w( LOG_TAG, exception );  
            log4j.error("HTTPԶ�̲�������ʧ��", exception);
            return null;
        }
    }
    
    protected static String getResponse( HttpURLConnection connection ) 
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream( 1024 );
        InputStream inputStream = null;
        
        try 
        {
            baos = new ByteArrayOutputStream( 1024 );
            int length = 0;
            byte[] buffer = new byte[1024];
            
        	if ( connection.getResponseCode() == 200) 
        	{
            	inputStream  = connection.getInputStream(); 
            } 
            else 
            {
            	inputStream = connection.getErrorStream();
            }            
            
            while ( ( length = inputStream.read( buffer ) ) != -1 ) 
            {
                baos.write( buffer, 0, length );
            }

            return baos.toString();
        }
        catch ( Exception exception ) 
        {
            Log.w( LOG_TAG, exception );
            log4j.error("getResponse() ��ȡ���ص�����������ʧ��", exception);
            
            return ERROR;
        }
        finally 
        {
            try 
            {
                baos.close();
            }
            catch ( Exception exception ) 
            {
                Log.w( LOG_TAG, exception );
                log4j.error("getResponse() �ر�IO������ʧ��", exception);
            }
        }
    }           
}
