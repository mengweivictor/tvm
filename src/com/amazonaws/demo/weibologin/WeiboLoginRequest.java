package com.amazonaws.demo.weibologin;

import com.amazonaws.util.HttpUtils;
import com.amazonaws.tvmclient.Request;
import com.amazonaws.tvmclient.Utilities;

public class WeiboLoginRequest extends Request{
	private final String endpoint;
	private final String uid;/*the random string for deviceid*/
    private final String weiboUid;
    private final String access_token;
    private final String app_key;
    private final boolean useSSL;
    private final String appName;
    
    private final String decryptionKey;
    
    
    public WeiboLoginRequest(
    					final String endpoit,
    					final boolean useSSL,
    					final String appName,
    					final String uid,
    					final String weiboUid, 
                        final String access_token, 
                        final String app_key
                        ) 
    {
    	this.endpoint = endpoit;
        this.weiboUid = weiboUid;
        this.appName = appName;
        this.uid = uid;
        this.access_token = access_token;
        this.app_key = app_key;
        this.useSSL = useSSL;
        
        this.decryptionKey = this.computeDecryptionKey();
    }
    
    public String getDecryptionKey() 
    {
        return this.decryptionKey;
    }
    
    public String buildRequestUrl() 
    {
        StringBuilder builder = new StringBuilder( ( this.useSSL ? "https://" : "http://" ) );
        
        builder.append( this.endpoint );
        builder.append( "/" );
        
        String signature = Utilities.getSignature( this.app_key, this.decryptionKey );
        
        builder.append( "weibologin" );
        builder.append("?uid=" + HttpUtils.urlEncode(this.uid, false));
        builder.append( "&access_token=" + HttpUtils.urlEncode( this.access_token, false ) );
        builder.append( "&signature=" + HttpUtils.urlEncode( signature, false ) );
        return builder.toString();
    }
    protected String computeDecryptionKey() 
    {
        try
        {
            String salt = this.access_token +this.appName+ this.endpoint;
            
            return Utilities.getSignature( salt, this.weiboUid );
        }
        catch ( Exception exception )
        {
            return null;
        }
    }   
    
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append("endpoint = ").append(endpoint).append(", ");
        sb.append("weiboUid = ").append(weiboUid).append(", ");
        sb.append("access_token = ").append(access_token).append(", ");
        sb.append("app_key = ").append(app_key).append(", ");
        sb.append("useSSL = ").append(useSSL).append(", ");
        
        return sb.toString();
    }
    
}
