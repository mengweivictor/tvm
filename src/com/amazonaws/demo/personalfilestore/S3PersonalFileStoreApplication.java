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
     * 全局共享对象存储区域
     */
    private Map<String, Object> context;

    /**
     *  加载的图片的缓存区域
     */
    private Map<String, MySoftRef> imageCache;

    /**
     * 垃圾Reference的队列（所引用的对象已经被回收，则将该引用存入队列中）
     *
     **/
    private ReferenceQueue<Bitmap> queue;

    
	/**
     * 应用保存数据的根目录
     *
     */
    private String baseDir;


    /**
     * 日志目录
     *
     */

    private String logDir;

    /**
     * 拍照图片目录
     *
     */
    private String imageDir;
    
    /**
     * 实例
     *
     */
    private static S3PersonalFileStoreApplication instance;
    
    public S3PersonalFileStoreApplication()
    {
        
        context    = new HashMap<String, Object>();
        imageCache = new HashMap<String, MySoftRef>();
        queue      = new ReferenceQueue<Bitmap>();
        
        //1.检查有无外部附加存储，优先使用附加的SD卡存储
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>手机有SD卡,将使用SD卡存储");

            baseDir = Environment.getExternalStorageDirectory() 
            		+ File.separator 
            		+ "S3PersonalFileStore";
        }
        else
        {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>手机没有SD卡,将使用系统自带存储");

            baseDir = "/data/data/com.amazonaws.demo.personalfilestore/" 
                    + File.separator 
                    + "S3PersonalFileStore";
        }

        logDir      = baseDir + File.separator + "logs"   + File.separator;
        imageDir    = baseDir + File.separator + "image" + File.separator;
        baseDir     = baseDir + File.separator;
        
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>获得的日志目录为" + logDir);
    }
    
    

    /**
     *
     * WeekBuyMobile Application的初始化方法，将被Android调用
     *
     */
    @Override
    public void onCreate()
    {
        super.onCreate();

        //1.构造Log4j日志配置对象
        LogConfigurator logConfigurator = new LogConfigurator();

        //2.设置日志信息保存文件名格式
        logConfigurator.setFileName(logDir + "log4j.txt");

        //3.日志参数设置
        logConfigurator.setRootLevel(Level.DEBUG);
        logConfigurator.setFilePattern("[%d][%p][%t][%c:%L] %m%n") ;
        logConfigurator.setMaxFileSize(1024 * 1024 * 5) ;
        logConfigurator.setImmediateFlush(true) ;
        logConfigurator.configure();

        Logger log = Logger.getLogger(S3PersonalFileStoreApplication.class);

        boolean result = false;


        //4.创建日志目录
        File logDirFile = new File(logDir);

        if (!logDirFile.exists())
        {
            result = logDirFile.mkdirs();

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String now = dateFormat.format(new java.util.Date());
            String logPrefix = "[" + now + "]" + "[com.amazonaws.demo.personalfilestore.S3PersonalFileStoreApplication]";

            if (result == false)
            {
                System.out.println(logPrefix + "目录" + logDirFile + "创建失败!");
            }
            else
            {
                System.out.println(logPrefix + "目录" + logDirFile + "创建成功!");
            }
        }

        //5.创建图片目录
        File imageDirFile = new File(imageDir);

        if (!imageDirFile.exists())
        {
            result = imageDirFile.mkdirs();

            logDirCreationResult(imageDirFile, result);
        }

        

        log.info("onCreate() S3文件管理应用已经成功启动") ;
    }
    
    public String getImageDir()
    {
        return this.imageDir;
    }
    
    /**
    *
    * 返回WeekBuyMobile的实例
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
   * 添加全局共享对象
   *
   * @param key 全局对象保存关键字（字符串）
   * @param value 被保存的全局对象（任意Object）
   *
   */
  public void put(String key, Object value)
  {
      context.put(key, value);
  }

  /**
   *
   * 获取保存的全局共享对象
   *
   * @param key 全局对象保存关键字（字符串）
   *
   * @return Object 被保存全局对象的或空
   *
   */
  public Object get(String key)
  {
      return context.get(key);
  }


  /**
   *
   * 添加保存的图像对象，最多保存10个
   *
   * @param key 图像对象保存关键字（字符串）
   * @param value 被保存的图像对象
   *
   */
  public void putImage(String key, Bitmap value)
  {
      cleanCache();// 清除垃圾引用
      MySoftRef ref = new MySoftRef(value, queue, key);
      imageCache.put(key, ref);
  }


  /**
   *
   * 获取保存的图像对象，
   *
   * @param key 图像对象保存关键字（字符串）
   *
   * @return Bitmap 被保存的图像对象或空
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
    * 用日志记录各种系统目录的创建结果
    *
    */
   private void logDirCreationResult(File dir, boolean result)
   {
       if (result == false)
       {
    	   log4j.warn("onCreate() 创建目录" + dir.getAbsolutePath() + "操作失败");
       }
       else
       {
    	   log4j.debug("onCreate() 创建目录" + dir.getAbsolutePath() + "操作成功");
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
