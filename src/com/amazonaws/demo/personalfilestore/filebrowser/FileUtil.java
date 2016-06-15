package com.amazonaws.demo.personalfilestore.filebrowser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;

import org.apache.log4j.Logger;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.DisplayMetrics;

import com.amazonaws.demo.personalfilestore.S3PersonalFileStoreApplication;

/**
 * 
 * 文件操作工具类
 * 
 * 
 */
public class FileUtil
{
    private static final Logger log = Logger.getLogger(FileUtil.class);
    
    /**
     * 
     * 静态模式，私有化构造方法
     * 
     */
    private FileUtil()
    {
        
    }
    
    //========================================以下为网络文件操作专用函数=====================================
    
    /**
     * 下载网络文件到本地，
     * 如果保存目录不存在，系统将自动创建，
     * 如果本地文件已经存在，系统将比较网路文件与本地文件大小，如果相同则不下载，目的是避免重复下载
     * 
     * @param urlString 网络文件在服务器上的位置
     * @param savePath 网络文件在本地保存路径
     * @param savePath 网络文件保存名称，可选，如果没有提供，则使用网络文件名称作为保存名称
     * 
     * @return boolean 操作执行结果(成功、失败)
     */
    public static boolean downFile(String urlString, 
                                   String savePath, 
                                   String fileName, 
                                   long remoteFileSize)
            
    {
        log.debug("downFile() IN");
        log.debug("downFile() urlString = " + urlString);
        log.debug("downFile() savePath = " + savePath);
        log.debug("downFile() fileName = " + fileName);
        log.debug("downFile() remoteFileSize = " + remoteFileSize);

        long begin = System.currentTimeMillis();
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        
        if (fileName == null || fileName.equals(""))
        {
            fileName = urlString.substring(urlString.lastIndexOf("/") + 1);
        }    
        
        log.debug("downFile() fileName = " + fileName);
        
        //本地保存目录，如果不存在则自动创建
        File dir = new File(savePath);
        
        if(!dir.exists())
        {
            dir.mkdirs();
        }
        
        //本地保存文件名
        String saveFileName = savePath + fileName;
        
        File fileObj = new File(saveFileName);
        
        //如果本地同名文件存在，检查文件大小，如果大小发生变化，表明文件有更新，才执行下载
        if(fileObj.exists())
        {
            log.debug("远程文件大小:" + remoteFileSize);
            log.debug("本地同名文件大小:" + fileObj.length());
            
            if (remoteFileSize > 0 && fileObj.length() == remoteFileSize)
            {
                log.debug("downFile() 文件" + fileObj.getAbsolutePath() + "已经下载过了");
                
                return true;
            }    
        }
        
        int numTotal = 0;//总共下载的字节数
        
        try 
        {   
            //构造网络访问URL对象
            URL url = new URL(urlString);
        
            //打开网络连接
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.connect();
            
            //如果本地同名文件存在，检查文件大小，如果大小发生变化，表明文件有更新，才执行下载
            if(fileObj.exists())
            {
                int length = Integer.parseInt(conn.getHeaderField("Content-Length")); 
                
                log.debug("网络文件大小:" + length);
                log.debug("本地同名文件大小:" + fileObj.length());
                
                if (fileObj.length() == length)
                {
                    log.debug("downFile() 文件" + fileObj.getAbsolutePath() + "已经下载过了");
                    
                    return true;
                }    
            }
            
            //获取网络输入流
            inputStream = conn.getInputStream();
            
            //打开本地保存文件输出流
            fileOutputStream = new FileOutputStream(fileObj);
            
            byte buf[] = new byte[1024];
            
            //9.循环读取网络传来的数据，并更新进度条
            int numread  = 0;
            
            while((numread = inputStream.read(buf)) != -1)
            {
                fileOutputStream.write(buf,0,numread);
                
                numTotal = numTotal + numread;
            }    
        } 
        catch (MalformedURLException e) 
        {   
            log.error("downFile() URL格式不合法, urlString:" + urlString, e);
            
            return false;
        } 
        catch(IOException e)
        {
            log.error("downFile() 文件操作失败", e);
            
            return false;
        }
        finally
        {
            //关闭网络输入流
            if (inputStream != null)
            {
                try
                {
                    inputStream.close();
                }
                catch (IOException e)
                {
                    log.error("downFile() 网络输入流关闭操作失败", e);
                }
            }  
            
            //关闭文件输出流
            if (fileOutputStream != null)
            {
                try
                {
                    fileOutputStream.close();
                }
                catch (IOException e)
                {
                    log.error("downFile() 文件输出流关闭操作失败", e);
                }
            }    
        }
        
        log.debug("downFile() 成功下载文件" + fileObj.getAbsolutePath() 
                + ", 下载字节总数：" + numTotal 
                + ", 耗时" + (System.currentTimeMillis() - begin) + "毫秒");
        
        log.debug("downFile() OUT");
        
        return true;
    }
    
    //========================================以下为文件操作通用函数=====================================
    
    /** 
     * 复制单个文件 
     * @param oldPath String 原文件路径 如：c:/fqf.txt 
     * @param newPath String 复制后路径 如：f:/fqf.txt 
     * 
     * @return boolean 
     */ 
    public static boolean copyFile(String oldPath, String newPath)
    {
        FileOutputStream fs  = null;
        InputStream inStream = null;
        
        boolean result = true;
        
        try
        {
            int byteread = 0;
            
            File oldfile = new File(oldPath);
            
            if (oldfile.exists())
            { 
                // 文件存在时
                inStream = new FileInputStream(oldPath); // 读入原文件
                fs = new FileOutputStream(newPath);
                
                byte[] buffer = new byte[1444];
               
                while ((byteread = inStream.read(buffer)) != -1)
                {
                    fs.write(buffer, 0, byteread);
                } 
            }
        }
        catch (IOException e)
        {
            result = false;
            
            log.error("copyFile() 拷贝文件" + oldPath + "到" + newPath + "操作失败", e);
        }
        finally
        {
            if (inStream != null)
            {
                try
                {
                    inStream.close();
                }
                catch (IOException e)
                {
                    log.error("copyFile()关闭文件输入流操作失败", e);
                }
            }   
            
            if (fs != null)
            {
                try
                {
                    fs.close();
                }
                catch (IOException e)
                {
                    log.error("copyFile()关闭文件输出流操作失败", e);
                }
            } 
            
        }
        
        File newfile = new File(newPath);
        
        if (!newfile.exists() || newfile.length() == 0)
        {
            result = false;
        }    
        
        return result;

    }
    
    /** 
     * 删除目录或者文件
     * 如果是文件，直接删除，如果是目录，执行目录的级联删除
     * 
     * @param file 被删除的目录或文件
     * 
     * @return boolean 删除操作执行的结果
     */ 
    public static boolean deleteFile(File file)
    {
    	boolean result = true;
    	
        if (file.isDirectory())
        {
            File[] files = file.listFiles();
            
            if (files != null && files.length > 0)
            {
                for (int i = 0; i < files.length; ++i)
                {
                    if (!deleteFile(files[i]))
                    {
                    	result = false;
                    }	
                }
            }
        }
        
        if (!file.delete())
        {
        	result = false;
        }	
        
        return result;
    }
    
    /** 
     * 移动文件
     * 
     * * @param oldPath String 原文件路径 如：c:/fqf.txt 
     * @param newPath String 移动后路径 如：f:/fqf.txt 
     * 
     * @return boolean 移动操作执行的结果 
     */ 
    public static boolean moveFile(String oldPath, String newPath)
    {
        if (copyFile(oldPath, newPath))
        {
            if (deleteFile(new File(oldPath)))
            {
            	return true;
            }	
        }
        
        return false;
    }
    
    /** 
     * 将文件大小值格式化
     * 
     * @param fileS 以字节为单位的原始文件大小
     * 
     * @return 经过格式化后的文件大小描述字符串 
     */ 
    public static String formetFileSize(long fileS)
    {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        
        if (fileS < 1024)
        {
            fileSizeString = fileS + " B";
        }
        else if (fileS < 1048576)
        {
            fileSizeString = df.format((double) fileS / 1024) + " K";
        }
        else if (fileS < 1073741824)
        {
            fileSizeString = df.format((double) fileS / 1048576) + " M";
        }
        else
        {
            fileSizeString = df.format((double) fileS / 1073741824) + " G";
        }
        
        return fileSizeString;
    }
    
    //========================================以下为图片文件处理专用函数=====================================
    
    /** 
     * 压缩图片文件
     * 
     * @param photoPath 被压缩的图片文件所在的路径 
     */ 
    public static void compressPhoto(File photoPath, DisplayMetrics dm) 
    {  
        
        final BitmapFactory.Options options = new BitmapFactory.Options();  
        options.inJustDecodeBounds = true;  
        Bitmap tempBitmap = BitmapFactory.decodeFile(photoPath.getAbsolutePath(), options);  
        
        if (tempBitmap != null)
        {
            tempBitmap.recycle();
        }    
        
        if (dm.widthPixels < 1000)
        {    
  
            // Calculate inSampleSize  
            options.inSampleSize = calculateInSampleSize(options, dm.widthPixels, dm.heightPixels);
        }
        else
        {
            options.inSampleSize = calculateInSampleSize(options, 
                                                         (int)(dm.widthPixels * 1), 
                                                         (int)(dm.heightPixels * 1));
        }    
        
       
  
        // Decode bitmap with inSampleSize set  
        options.inJustDecodeBounds = false;  
          
        Bitmap bm = BitmapFactory.decodeFile(photoPath.getAbsolutePath(), options);
        
        if (bm == null)
        {
            return;
        }
        
        int degree = readPictureDegree(photoPath.getAbsolutePath());
        bm = rotateBitmap(bm, degree);
        
        FileOutputStream out = null;
        
        try
        {
            
            out = new FileOutputStream(photoPath);
            
            bm.compress(Bitmap.CompressFormat.JPEG, 30, out);

        }
        catch(FileNotFoundException e)
        {
            log.debug("compressPhoto() 文件" + photoPath.getAbsolutePath() + "不存在", e);
        }
        finally
        {
            try
            {
                if (out != null)
                {    
                    out.close();
                }    
            }
            catch (IOException e)
            {
                log.error("compressPhoto() 关闭文件输出流操作失败", e);
            }
            
            bm.recycle();
        }
        
        System.gc(); 
        
  
    }  
    
    /** 
     * 旋转图片方向，主要是处理有些图片方向不对的问题
     * 
     * @param photoPath 被压缩的图片文件所在的路径 
     */ 
    public static Bitmap rotateBitmap(Bitmap bitmap, int rotate)
    {
        if (bitmap == null)
        {    
            return null;
        } 
        
        if (rotate == 0)
        {
        	return bitmap;
        }	
        

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        // Setting post rotate to 90
        Matrix mtx = new Matrix();
        mtx.postRotate(rotate);
        
        Bitmap newMap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
        
        if (newMap != bitmap)
        {
            bitmap.recycle();
        }    
        
        return newMap;
    }
    
    /** 
     * 获取图片正常显示需要被调整的角度
     * 
     * @param path 图片文件所在的路径 
     * 
     * @return 需要被调整的角度
     */ 
    public static int readPictureDegree(String path)
    {
        int degree = 0;
        
        try
        {
            ExifInterface exifInterface = new ExifInterface(path);
            
            int orientation = 
                    exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                                                  ExifInterface.ORIENTATION_NORMAL);
            
            switch (orientation)
            {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                    
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                    
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        }
        catch (IOException e)
        {
            log.error("获取图片的旋转角度失败", e);
        }

        return degree;
    }
    
    /** 
     * 在不打开图片的前提下，计算图片的尺寸 
     * 
     * @param path 图片文件所在的路径 
     * 
     * @return 长或宽的最长值
     */ 
    private static int calculateInSampleSize(BitmapFactory.Options options,  
                                             int reqWidth, 
                                             int reqHeight) 
    {  
        // Raw height and width of image  
        final int height = options.outHeight;  
        final int width  = options.outWidth;
        
        int inSampleSize = 1;  
  
        if (height > reqHeight || width > reqWidth) 
        {  
            final int heightRatio = Math.round((float) height / (float) reqHeight);  
            final int widthRatio  = Math.round((float) width / (float) reqWidth);  
  
            inSampleSize = heightRatio < widthRatio ? widthRatio : heightRatio;  
        }  
  
        return inSampleSize;  
    } 
    
    /** 
     * 在加载大图片文件时候的一个相对安全的读取类. 
     *  
     * @param uri 文件所在位置URL
     * @param width 图片显示宽度
     * @param height 图片显示高度
     * @return 经过尺寸调整的图片对象
     * 
     * @throws FileNotFoundException 
     */  
    public static Bitmap safeDecodeStream(boolean cached, 
    		                              Uri uri, 
    		                              int width, 
    		                              int height, 
    		                              Activity ctx) 
            throws FileNotFoundException
    {
        
        //首先确认缓存中没有才读取
        String key = uri.toString() + width + height; 
        Bitmap bitmap = null;
        
        if (cached == true)
        {    
            bitmap = S3PersonalFileStoreApplication.getInstance().getImage(key);
            
            if (bitmap != null)
            {
                return bitmap;
            }    
        }
        
        int scale = 1;
        
        BitmapFactory.Options options = new BitmapFactory.Options();
        
        android.content.ContentResolver resolver = ctx.getContentResolver();

        
        if (width > 0 || height > 0)
        {
            // Decode image size without loading all data into memory
            options.inJustDecodeBounds = true;
            
            BitmapFactory.decodeStream(
                    new BufferedInputStream(resolver.openInputStream(uri), 16 * 1024), 
                    null, 
                    options);

            int w = options.outWidth;
            int h = options.outHeight;
            
            log.debug("safeDecodeStream() options.outWidth  = " + w);
            log.debug("safeDecodeStream() options.outHeight = " + h);
            
            if (width < w || height < h)
            {
                while (true)
                {
                    if (w / 2 < width || h / 2 < height)
                    {
                        break;
                    }
                    
                    w /= 2;
                    h /= 2;
                    
                    scale *= 2;
                }
            }
        }

        // Decode with inSampleSize option
        options.inJustDecodeBounds = false;
        options.inDither = false;
        
        options.inPreferredConfig = Bitmap.Config.ARGB_8888; 
        //options.inPreferredConfig = Bitmap.Config.ARGB_4444;
        
        options.inSampleSize = scale;
        
        log.debug("safeDecodeStream() options.inSampleSize = " + options.inSampleSize);
        
        bitmap = BitmapFactory.decodeStream(
                    new BufferedInputStream(resolver.openInputStream(uri), 16 * 1024), 
                    null, 
                    options);
        
        if (cached == true)
        {    
            S3PersonalFileStoreApplication.getInstance().putImage(key, bitmap);
        }
        
        return bitmap;
    } 

}
