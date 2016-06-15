package com.amazonaws.demo.personalfilestore;

import java.io.File;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import android.app.Application;
import android.graphics.Bitmap;
import android.os.Environment;
import de.mindpipe.android.logging.log4j.LogConfigurator;

public class S3PersonalFileStoreApplication extends Application 
{
	
    private static final Logger log4j = 
    		Logger.getLogger(S3PersonalFileStoreApplication.class);
    
    
    /**
     * ȫ�ֹ������洢����
     */
    private Map<String, Object> context;

    /**
     *  ���ص�ͼƬ�Ļ�������
     */
    private Map<String, MySoftRef> imageCache;

    /**
     * ����Reference�Ķ��У������õĶ����Ѿ������գ��򽫸����ô�������У�
     *
     **/
    private ReferenceQueue<Bitmap> queue;

    
	/**
     * Ӧ�ñ������ݵĸ�Ŀ¼
     *
     */
    private String baseDir;


    /**
     * ��־Ŀ¼
     *
     */

    private String logDir;

    /**
     * ����ͼƬĿ¼
     *
     */
    private String imageDir;
    
    /**
     * ʵ��
     *
     */
    private static S3PersonalFileStoreApplication instance;
    
    public S3PersonalFileStoreApplication()
    {
        
        context    = new HashMap<String, Object>();
        imageCache = new HashMap<String, MySoftRef>();
        queue      = new ReferenceQueue<Bitmap>();
        
        //1.��������ⲿ���Ӵ洢������ʹ�ø��ӵ�SD���洢
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>�ֻ���SD��,��ʹ��SD���洢");

            baseDir = Environment.getExternalStorageDirectory() 
            		+ File.separator 
            		+ "S3PersonalFileStore";
        }
        else
        {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>�ֻ�û��SD��,��ʹ��ϵͳ�Դ��洢");

            baseDir = "/data/data/com.amazonaws.demo.personalfilestore/" 
                    + File.separator 
                    + "S3PersonalFileStore";
        }

        logDir      = baseDir + File.separator + "logs"   + File.separator;
        imageDir    = baseDir + File.separator + "image" + File.separator;
        baseDir     = baseDir + File.separator;
        
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>��õ���־Ŀ¼Ϊ" + logDir);
    }
    
    

    /**
     *
     * WeekBuyMobile Application�ĳ�ʼ������������Android����
     *
     */
    @Override
    public void onCreate()
    {
        super.onCreate();

        //1.����Log4j��־���ö���
        LogConfigurator logConfigurator = new LogConfigurator();

        //2.������־��Ϣ�����ļ�����ʽ
        logConfigurator.setFileName(logDir + "log4j.txt");

        //3.��־��������
        logConfigurator.setRootLevel(Level.DEBUG);
        logConfigurator.setFilePattern("[%d][%p][%t][%c:%L] %m%n") ;
        logConfigurator.setMaxFileSize(1024 * 1024 * 5) ;
        logConfigurator.setImmediateFlush(true) ;
        logConfigurator.configure();

        Logger log = Logger.getLogger(S3PersonalFileStoreApplication.class);

        boolean result = false;


        //4.������־Ŀ¼
        File logDirFile = new File(logDir);

        if (!logDirFile.exists())
        {
            result = logDirFile.mkdirs();

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String now = dateFormat.format(new java.util.Date());
            String logPrefix = "[" + now + "]" + "[com.amazonaws.demo.personalfilestore.S3PersonalFileStoreApplication]";

            if (result == false)
            {
                System.out.println(logPrefix + "Ŀ¼" + logDirFile + "����ʧ��!");
            }
            else
            {
                System.out.println(logPrefix + "Ŀ¼" + logDirFile + "�����ɹ�!");
            }
        }

        //5.����ͼƬĿ¼
        File imageDirFile = new File(imageDir);

        if (!imageDirFile.exists())
        {
            result = imageDirFile.mkdirs();

            logDirCreationResult(imageDirFile, result);
        }

        

        log.info("onCreate() S3�ļ�����Ӧ���Ѿ��ɹ�����") ;
    }
    
    public String getImageDir()
    {
        return this.imageDir;
    }
    
    /**
    *
    * ����WeekBuyMobile��ʵ��
    *
    *
    */
   public static S3PersonalFileStoreApplication getInstance()
   {
       if(instance == null)
       {
           instance = new S3PersonalFileStoreApplication();
       }

       return instance;
   }
   
   /**
   *
   * ���ȫ�ֹ������
   *
   * @param key ȫ�ֶ��󱣴�ؼ��֣��ַ�����
   * @param value �������ȫ�ֶ�������Object��
   *
   */
  public void put(String key, Object value)
  {
      context.put(key, value);
  }

  /**
   *
   * ��ȡ�����ȫ�ֹ������
   *
   * @param key ȫ�ֶ��󱣴�ؼ��֣��ַ�����
   *
   * @return Object ������ȫ�ֶ���Ļ��
   *
   */
  public Object get(String key)
  {
      return context.get(key);
  }


  /**
   *
   * ��ӱ����ͼ�������ౣ��10��
   *
   * @param key ͼ����󱣴�ؼ��֣��ַ�����
   * @param value �������ͼ�����
   *
   */
  public void putImage(String key, Bitmap value)
  {
      cleanCache();// �����������
      MySoftRef ref = new MySoftRef(value, queue, key);
      imageCache.put(key, ref);
  }


  /**
   *
   * ��ȡ�����ͼ�����
   *
   * @param key ͼ����󱣴�ؼ��֣��ַ�����
   *
   * @return Bitmap �������ͼ�������
   *
   */
  public Bitmap getImage(String key)
  {
      Bitmap bmp = null;

      if (imageCache.containsKey(key))
      {
          MySoftRef ref = (MySoftRef) imageCache.get(key);
          bmp = (Bitmap) ref.get();
      }

      return bmp;
  }
    
    /**
    *
    * ����־��¼����ϵͳĿ¼�Ĵ������
    *
    */
   private void logDirCreationResult(File dir, boolean result)
   {
       if (result == false)
       {
    	   log4j.warn("onCreate() ����Ŀ¼" + dir.getAbsolutePath() + "����ʧ��");
       }
       else
       {
    	   log4j.debug("onCreate() ����Ŀ¼" + dir.getAbsolutePath() + "�����ɹ�");
       }
   }
   
   private void cleanCache()
   {
       MySoftRef ref = null;

       while ((ref = (MySoftRef) queue.poll()) != null)
       {
           imageCache.remove(ref._key);
       }
   }
   
   private class MySoftRef extends SoftReference<Bitmap>
   {
       private String _key;

       public MySoftRef(Bitmap bmp, ReferenceQueue<Bitmap> q, String key)
       {
           super(bmp, q);

           _key = key;
       }
   }


}
