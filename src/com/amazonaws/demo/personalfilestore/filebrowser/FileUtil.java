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
 * �ļ�����������
 * 
 * 
 */
public class FileUtil
{
    private static final Logger log = Logger.getLogger(FileUtil.class);
    
    /**
     * 
     * ��̬ģʽ��˽�л����췽��
     * 
     */
    private FileUtil()
    {
        
    }
    
    //========================================����Ϊ�����ļ�����ר�ú���=====================================
    
    /**
     * ���������ļ������أ�
     * �������Ŀ¼�����ڣ�ϵͳ���Զ�������
     * ��������ļ��Ѿ����ڣ�ϵͳ���Ƚ���·�ļ��뱾���ļ���С�������ͬ�����أ�Ŀ���Ǳ����ظ�����
     * 
     * @param urlString �����ļ��ڷ������ϵ�λ��
     * @param savePath �����ļ��ڱ��ر���·��
     * @param savePath �����ļ��������ƣ���ѡ�����û���ṩ����ʹ�������ļ�������Ϊ��������
     * 
     * @return boolean ����ִ�н��(�ɹ���ʧ��)
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
        
        //���ر���Ŀ¼��������������Զ�����
        File dir = new File(savePath);
        
        if(!dir.exists())
        {
            dir.mkdirs();
        }
        
        //���ر����ļ���
        String saveFileName = savePath + fileName;
        
        File fileObj = new File(saveFileName);
        
        //�������ͬ���ļ����ڣ�����ļ���С�������С�����仯�������ļ��и��£���ִ������
        if(fileObj.exists())
        {
            log.debug("Զ���ļ���С:" + remoteFileSize);
            log.debug("����ͬ���ļ���С:" + fileObj.length());
            
            if (remoteFileSize > 0 && fileObj.length() == remoteFileSize)
            {
                log.debug("downFile() �ļ�" + fileObj.getAbsolutePath() + "�Ѿ����ع���");
                
                return true;
            }    
        }
        
        int numTotal = 0;//�ܹ����ص��ֽ���
        
        try 
        {   
            //�����������URL����
            URL url = new URL(urlString);
        
            //����������
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.connect();
            
            //�������ͬ���ļ����ڣ�����ļ���С�������С�����仯�������ļ��и��£���ִ������
            if(fileObj.exists())
            {
                int length = Integer.parseInt(conn.getHeaderField("Content-Length")); 
                
                log.debug("�����ļ���С:" + length);
                log.debug("����ͬ���ļ���С:" + fileObj.length());
                
                if (fileObj.length() == length)
                {
                    log.debug("downFile() �ļ�" + fileObj.getAbsolutePath() + "�Ѿ����ع���");
                    
                    return true;
                }    
            }
            
            //��ȡ����������
            inputStream = conn.getInputStream();
            
            //�򿪱��ر����ļ������
            fileOutputStream = new FileOutputStream(fileObj);
            
            byte buf[] = new byte[1024];
            
            //9.ѭ����ȡ���紫�������ݣ������½�����
            int numread  = 0;
            
            while((numread = inputStream.read(buf)) != -1)
            {
                fileOutputStream.write(buf,0,numread);
                
                numTotal = numTotal + numread;
            }    
        } 
        catch (MalformedURLException e) 
        {   
            log.error("downFile() URL��ʽ���Ϸ�, urlString:" + urlString, e);
            
            return false;
        } 
        catch(IOException e)
        {
            log.error("downFile() �ļ�����ʧ��", e);
            
            return false;
        }
        finally
        {
            //�ر�����������
            if (inputStream != null)
            {
                try
                {
                    inputStream.close();
                }
                catch (IOException e)
                {
                    log.error("downFile() �����������رղ���ʧ��", e);
                }
            }  
            
            //�ر��ļ������
            if (fileOutputStream != null)
            {
                try
                {
                    fileOutputStream.close();
                }
                catch (IOException e)
                {
                    log.error("downFile() �ļ�������رղ���ʧ��", e);
                }
            }    
        }
        
        log.debug("downFile() �ɹ������ļ�" + fileObj.getAbsolutePath() 
                + ", �����ֽ�������" + numTotal 
                + ", ��ʱ" + (System.currentTimeMillis() - begin) + "����");
        
        log.debug("downFile() OUT");
        
        return true;
    }
    
    //========================================����Ϊ�ļ�����ͨ�ú���=====================================
    
    /** 
     * ���Ƶ����ļ� 
     * @param oldPath String ԭ�ļ�·�� �磺c:/fqf.txt 
     * @param newPath String ���ƺ�·�� �磺f:/fqf.txt 
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
                // �ļ�����ʱ
                inStream = new FileInputStream(oldPath); // ����ԭ�ļ�
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
            
            log.error("copyFile() �����ļ�" + oldPath + "��" + newPath + "����ʧ��", e);
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
                    log.error("copyFile()�ر��ļ�����������ʧ��", e);
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
                    log.error("copyFile()�ر��ļ����������ʧ��", e);
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
     * ɾ��Ŀ¼�����ļ�
     * ������ļ���ֱ��ɾ���������Ŀ¼��ִ��Ŀ¼�ļ���ɾ��
     * 
     * @param file ��ɾ����Ŀ¼���ļ�
     * 
     * @return boolean ɾ������ִ�еĽ��
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
     * �ƶ��ļ�
     * 
     * * @param oldPath String ԭ�ļ�·�� �磺c:/fqf.txt 
     * @param newPath String �ƶ���·�� �磺f:/fqf.txt 
     * 
     * @return boolean �ƶ�����ִ�еĽ�� 
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
     * ���ļ���Сֵ��ʽ��
     * 
     * @param fileS ���ֽ�Ϊ��λ��ԭʼ�ļ���С
     * 
     * @return ������ʽ������ļ���С�����ַ��� 
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
    
    //========================================����ΪͼƬ�ļ�����ר�ú���=====================================
    
    /** 
     * ѹ��ͼƬ�ļ�
     * 
     * @param photoPath ��ѹ����ͼƬ�ļ����ڵ�·�� 
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
            log.debug("compressPhoto() �ļ�" + photoPath.getAbsolutePath() + "������", e);
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
                log.error("compressPhoto() �ر��ļ����������ʧ��", e);
            }
            
            bm.recycle();
        }
        
        System.gc(); 
        
  
    }  
    
    /** 
     * ��תͼƬ������Ҫ�Ǵ�����ЩͼƬ���򲻶Ե�����
     * 
     * @param photoPath ��ѹ����ͼƬ�ļ����ڵ�·�� 
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
     * ��ȡͼƬ������ʾ��Ҫ�������ĽǶ�
     * 
     * @param path ͼƬ�ļ����ڵ�·�� 
     * 
     * @return ��Ҫ�������ĽǶ�
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
            log.error("��ȡͼƬ����ת�Ƕ�ʧ��", e);
        }

        return degree;
    }
    
    /** 
     * �ڲ���ͼƬ��ǰ���£�����ͼƬ�ĳߴ� 
     * 
     * @param path ͼƬ�ļ����ڵ�·�� 
     * 
     * @return �������ֵ
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
     * �ڼ��ش�ͼƬ�ļ�ʱ���һ����԰�ȫ�Ķ�ȡ��. 
     *  
     * @param uri �ļ�����λ��URL
     * @param width ͼƬ��ʾ���
     * @param height ͼƬ��ʾ�߶�
     * @return �����ߴ������ͼƬ����
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
        
        //����ȷ�ϻ�����û�вŶ�ȡ
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
