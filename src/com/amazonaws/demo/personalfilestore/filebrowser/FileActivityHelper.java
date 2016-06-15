package com.amazonaws.demo.personalfilestore.filebrowser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.log4j.Logger;

import android.os.Environment;

/**
 * 
 * �ļ�Activity������
 * 
 */
public class FileActivityHelper 
{
    /**
     * ��־����
     */
    private static final Logger log = Logger.getLogger(FileActivityHelper.class);
    
    
    /**
     * ��̬ģʽ��˽�л����캯��
     */
    private FileActivityHelper()
    {
    	
    }

    
    /**
     * 
     * ��ȡһ���ļ����µ������ļ�
     *
     * @param dirPath Ŀ¼����
     * 
     * @return ����FileInfo�����б�
     * 
     */
    public static ArrayList<FileInfo> getFiles(String dirPath) 
    {
        log.debug("getFiles() path = " + dirPath);
        
        File f = new File(dirPath);
        File[] files = f.listFiles();
        
        //�����ǰĿ¼Ϊ�գ���ʾ������Ϣ
        if (files == null) 
        {
            log.warn("getFiles() ��ǰ·����û���ļ�����Ŀ¼");
            
            return null;
        }

        ArrayList<FileInfo> fileList = new ArrayList<FileInfo>();
        
        // ��ȡ�ļ��б�
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

        // ����
        Collections.sort(fileList, new FileComparator());

        return fileList;
    }

    
    /**
     * 
     * ��鵱ǰ�ļ�����Ŀ¼�ĺϷ��ԣ�����Ϊ�жϱ�׼��
     * 
     * 1.������ļ���ֻ�ܹ�����".jpg"��".png"��β��ͼƬ�ļ�
     * 2.������ļ��У�����ǿն��Ұ�����Ŀ¼����ͼƬ�ļ�
     * 
     * @param file �������ļ���Ŀ¼
     * 
     * @return boolean �����
     * 
     */
    private static boolean checkFile(File file)
    {
    	//����Ŀ¼���ļ���������
    	if (file.getName().startsWith("."))
        {
            return false;
        }
        
        //����ļ�
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
        
        //���Ŀ¼(�޵ݹ���)
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
        	    	
        	    	//����Ŀ¼
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
    
    /** ��ȡSD·�� **/
    
    /**
     * 
     * ��ȡ�ֻ�SD���е�����ͼƬĿ¼����
     * 
     * @return Ŀ¼���� 
     * 
     */
    public static String getSDPhotoPath()
    {
        String sdPath = "";
        
        // �ж�sd���Ƿ����
        if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
        {
            File sdDir = Environment.getExternalStorageDirectory();// ��ȡ����Ŀ¼
            
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
