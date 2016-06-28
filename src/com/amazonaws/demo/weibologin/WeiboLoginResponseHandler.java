package com.amazonaws.demo.weibologin;

import com.amazonaws.tvmclient.AESEncryption;
import com.amazonaws.tvmclient.LoginResponse;
import com.amazonaws.tvmclient.Response;
import com.amazonaws.tvmclient.ResponseHandler;
import com.amazonaws.tvmclient.Utilities;

public class WeiboLoginResponseHandler extends ResponseHandler{
	private final String decryptionKey;
	
	public WeiboLoginResponseHandler(final String decryptionKey) {
		this.decryptionKey = decryptionKey;
	}
	
	public Response handleResponse( int responseCode, String responseBody ) 
    {
        if ( responseCode == 200 ) 
        {
            try 
            {
                //���յ�����Ϣ���ܳ�json��Ϣ
                String json = AESEncryption.unwrap(responseBody, 
                                                   this.decryptionKey.substring(0, 32) );
                //ȡ��json��Ϣ�е�key���ݲ���
                return new LoginResponse( Utilities.extractElement( json, "key" ) );
            }
            catch ( Exception exception ) 
            {
                return new LoginResponse( 500, exception.getMessage() );
            }
        }
        else 
        {
            return new LoginResponse( responseCode, responseBody );
        }
    }        
}
