package com.amazonaws.demo.personalfilestore.filebrowser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.log4j.Logger;

import android.os.Environment;

/**
 * 
 * 文件Activity辅助类
 * 
 */
public class FileActivityHelper 
{
    /**
     * 日志对象
     */
    private static final Logger log = Logger.getLogger(FileActivityHelper.class);
    
    
    /**
     * 静态模式，私有化构造函数
     */
    private FileActivityHelper()
    {
    	
    }

    
    /**
     * 
     * 获取一个文件夹下的所有文件
     *
     * @param dirPath 目录名称
     * 
     * @return 返回FileInfo对象列表
     * 
     */
    public static ArrayList<FileInfo> getFiles(String dirPath) 
    {
        log.debug("getFiles() path = " + dirPath);
        
        File f = new File(dirPath);
        File[] files = f.listFiles();
        
        //如果当前目录为空，提示错误信息
        if (files == null) 
        {
            log.warn("getFiles() 当前路径下没有文件或子目录");
            
            return null;
        }

        ArrayList<FileInfo> fileList = new ArrayList<FileInfo>();
        
        // 获取文件列表
        for (int i = 0; i < files.length; i++) 
        {
            File file = files[i];
            
            if (!checkFile(file))
            {
            	continue;
            }	
            
            FileInfo fileInfo = new FileInfo();
            
            fileInfo.setName(file.getName());
            fileInfo.setDirectory(file.isDirectory());
            fileInfo.setPath(file.getPath());
            fileInfo.setSize(file.length());
            
            fileList.add(fileInfo);
        }

        // 排序
        Collections.sort(fileList, new FileComparator());

        return fileList;
    }

    
    /**
     * 
     * 检查当前文件或者目录的合法性，以下为判断标准：
     * 
     * 1.如果是文件，只能够是以".jpg"或".png"结尾的图片文件
     * 2.如果是文件夹，必须非空而且包含子目录或者图片文件
     * 
     * @param file 被检查的文件或目录
     * 
     * @return boolean 检查结果
     * 
     */
    private static boolean checkFile(File file)
    {
    	//隐藏目录或文件都不允许
    	if (file.getName().startsWith("."))
        {
            return false;
        }
        
        //检查文件
        if (file.isFile()) 
        {			
	        if (file.getName().endsWith(".jpg") 
	         || file.getName().endsWith(".png"))
	        {
	            return true;
	        } 
	        else
	        {
	        	return false;
	        }
        }
        
        //检查目录(无递归检查)
        if (file.isDirectory())
        {
        	if ((file.list() == null || file.list().length == 0))
        	{
        		return false;
        	}	
        	else
        	{
        		File[] files = file.listFiles();
        		
        		boolean found = false;
        		
        	    for (File subFile : files)
        	    {
        	    	if (subFile.getName().startsWith("."))
        	    	{
        	    		continue;
        	    	}	
        	    	
        	    	//有子目录
        	    	if (subFile.isDirectory())
        	    	{
        	    		found = true;
        	    	}	
        	    	
        	    	if (subFile.isFile() 
        	    	 && subFile.getName().endsWith(".jpg") || subFile.getName().endsWith(".png"))
        	    	{
        	    		found = true;
        	    	}	
        	    }	
        	    
        	    return found;
        		
        	}	
        } 
        
    	return false;
    }
    
    /** 获取SD路径 **/
    
    /**
     * 
     * 获取手机SD卡中的拍照图片目录名称
     * 
     * @return 目录名称 
     * 
     */
    public static String getSDPhotoPath()
    {
        String sdPath = "";
        
        // 判断sd卡是否存在
        if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
        {
            File sdDir = Environment.getExternalStorageDirectory();// 获取外存根目录
            
            sdPath = sdDir.getPath();
        }
        
        sdPath = "/sdcard";
        
        //String photoPath = sdPath + "/DCIM";
        
        String photoPath = sdPath;
        
        File photoDirFile = new File(photoPath);
        
        if (photoDirFile.isDirectory() && photoDirFile.exists())
        {
            return photoDirFile.getAbsolutePath();
        }    
        else
        {
            return sdPath;
        }    
    }
	
	
}
