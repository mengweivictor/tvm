package com.amazonaws.demo.weibologin;

import com.amazonaws.tvmclient.Response;

public class WeiboLoginResponse extends Response{
	private final String key;
    
    public WeiboLoginResponse( final int responseCode, final String responseMessage ) 
    {
        super( responseCode, responseMessage );
        this.key = null;
    }

    public WeiboLoginResponse( final String key ) 
    {
        super( 200, null );
        this.key = key;
    }
        
    public String getKey() 
    {
        return this.key;
    }      
    
    
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        
        sb.append(super.toString()); 
        sb.append("key = " + key).append(", ");
        
        return sb.toString();
    }
}
