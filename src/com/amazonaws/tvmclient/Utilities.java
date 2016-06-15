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

import java.util.Date;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;

import com.amazonaws.AmazonClientException;
import com.amazonaws.Request;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSSessionCredentials;
import com.amazonaws.auth.AbstractAWSSigner;
import com.amazonaws.util.DateUtils;

public class Utilities 
{
    //获取一个标准格式的日期，这个日期将被服务器和客户端用来产生认证签名
    public static String getTimestamp() 
    {
        //return new DateUtils().formatIso8601Date( new Date() );
        return new DateUtils().formatISO8601Date(new Date());
    }

    //取出json指定标签内部的内容
    public static String extractElement( String json, String element ) 
    {
        boolean hasElement = ( json.indexOf( element ) != -1 );
        
        if ( hasElement ) 
        {        
            int elementIndex = json.indexOf( element );
            int startIndex   = json.indexOf( "\"", elementIndex );
            int endIndex     = json.indexOf( "\"", startIndex + 1 );

            return json.substring( startIndex + 1, endIndex );
        }
        
        return null;            
    }
   
    public static String getSignature( String dataToSign, String key ) 
    {
        return new Signer().getSignature( dataToSign, key );
    }
     
    static class Signer extends AbstractAWSSigner 
    {
        public void sign(Request request, AWSCredentials credentials) throws AmazonClientException 
        {
        }
        
        protected void addSessionCredentials(Request request, AWSSessionCredentials credentials) 
        {
        }
        
        //利用Key产生签名
        public String getSignature( String dataToSign, String key ) 
        {
        	try 
        	{
    			byte[] data = dataToSign.getBytes( "UTF8" );
    			
    			Mac mac = Mac.getInstance( "HmacSHA256" );
    			mac.init( new SecretKeySpec( key.getBytes( "UTF8" ), "HmacSHA256" ) );
    			char[] signature = Hex.encodeHex( mac.doFinal( data ) );
    			
    			return new String( signature );
    		}
    		catch ( Exception exception ) 
        	{
    			return null;
    		}
        }
    }
    
}
