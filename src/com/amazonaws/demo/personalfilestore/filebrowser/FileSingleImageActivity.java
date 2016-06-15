package com.amazonaws.demo.personalfilestore.filebrowser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import android.app.Activity;
import android.content.Intent;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGestureListener;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.amazonaws.demo.personalfilestore.PropertyLoader;
import com.amazonaws.demo.personalfilestore.R;
import com.amazonaws.demo.personalfilestore.S3PersonalFileStore;
import com.amazonaws.demo.personalfilestore.S3PersonalFileStoreApplication;
import com.amazonaws.demo.personalfilestore.s3.S3;
import com.amazonaws.demo.personalfilestore.s3.S3BucketView;

/**
 *
 * 信息发布画廊中图片的详细大图显示页面
 *
 */

public class FileSingleImageActivity 
    extends Activity 
    implements OnGestureListener, 
               android.view.GestureDetector.OnGestureListener, 
               Constants
{
    private static final Logger log = Logger.getLogger(FileSingleImageActivity.class);

    /**
     * 触摸事件探测器
     */
    private GestureDetector mGestureDetector;

    /**
     * 当前显示图片索引
     */
    private int displayIndex = -1;

    /**
     * 所有的图片文件
     */
    private File[] files;

    /**
     * 当前屏幕分辨率
     */
    private DisplayMetrics dm;


    /**
     * 界面图形控件
     */
    private ImageView singleimage;

    //private TextView page_title;

    private Button selectButton;
    private Button returnButton;

    private Bitmap lastBitmap;
    
    protected String bucketName;
    protected String prefix;

    /**
     * 系统全局上下文对象
     */
    private S3PersonalFileStoreApplication application = S3PersonalFileStoreApplication.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //1. 首先调用父类的同名方法
        super.onCreate(savedInstanceState);
        log.debug("onCreate() IN");

        //2. 获得上页传递的参数
        Bundle extras = this.getIntent().getExtras();
        bucketName = extras.getString(S3.BUCKET_NAME);
        prefix = extras.getString(S3.PREFIX);
        
        log.debug("onCreate() bucketName = " + bucketName);
        log.debug("onCreate() prefix = " + prefix);

        //3. 设置界面布局
        setContentView(R.layout.filebrowser_single_image);

        //4. 获取上页传来的显示图片路径和标题
        String filePath = getIntent().getStringExtra(INTENT_KEY_FILE_PATH);
        String dirPah   = filePath.substring(0, filePath.lastIndexOf("/"));

        File[] tempfiles = new File(dirPah).listFiles();

        List<File> tempFileList = new ArrayList<File>();

        for (File file : tempfiles)
        {
            if (file.isFile())
            {
                if (file.getName().endsWith(".jpg") || file.getName().endsWith(".png"))
                {
                    tempFileList.add(file);
                }
            }
        }

        files = new File[tempFileList.size()];

        //5. 判断初始图片的索引位置
        for (int i = 0 ; i < files.length ; i++)
        {
            files[i] = tempFileList.get(i);

            if (files[i].getAbsolutePath().equals(filePath))
            {
                displayIndex = i;

                log.debug("发现了初始显示的图片位置:" + displayIndex);
            }
        }

        //6. 获取界面控件
        //page_title   = (TextView)findViewById(R.id.page_title);
        singleimage  = (ImageView)findViewById(R.id.fb_single_image_view);

        selectButton = (Button)findViewById(R.id.fb_single_image_button_select);
        returnButton = (Button)findViewById(R.id.fb_single_image_button_return);

        //7. 设置初始标题
//        page_title.setText(files[displayIndex].getName() + "[" +(displayIndex + 1)
//                         + " in " + files.length + "]");

        //8. 屏幕像素
        dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        log.debug("dm.heightPixels = " + dm.heightPixels);

        //9. 设置图片显示对象
        singleimage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        LinearLayout.LayoutParams linearParams =
                (LinearLayout.LayoutParams) singleimage.getLayoutParams();

        linearParams.height = (int)(dm.heightPixels * 0.775);

        singleimage.setLayoutParams(linearParams);

        //10. 显示初始图片
        displayImage(filePath);

        //11. 设置事件
        setEventListener();

        mGestureDetector = new GestureDetector(this);

        log.debug("onCreate() OUT");
    }


    //==================================触屏滑动事件， 实现滑动屏幕切换图片=====================================
    @Override
    public void onGesture(GestureOverlayView overlay, MotionEvent event)
    {
    }

    @Override
    public void onGestureCancelled(GestureOverlayView overlay, MotionEvent event)
    {
    }

    @Override
    public void onGestureEnded(GestureOverlayView overlay, MotionEvent event)
    {
    }

    @Override
    public void onGestureStarted(GestureOverlayView overlay, MotionEvent event)
    {
    }

    @Override
    public void onShowPress(MotionEvent e)
    {
    }

    @Override
    public void onLongPress(MotionEvent e)
    {
    }

    @Override
    public boolean onDown(MotionEvent e)
    {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
    {
        return false;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e)
    {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
    {
        float gapX = e1.getX() - e2.getX();

        if (gapX > FLING_X_LOW_LIMIT)
        {
            log.debug("onFling()用户向左滑动屏幕");

            if (displayIndex > 0)
            {
                displayIndex--;

                displayImage(files[displayIndex].getAbsolutePath());
            }

            return true;
        }

        if (gapX < -FLING_X_LOW_LIMIT)
        {
            log.debug("onFling()用户向右滑动屏幕");

            if (displayIndex < files.length - 1)
            {
                displayIndex++;

                displayImage(files[displayIndex].getAbsolutePath());
            }

            return true;
        }

        return false;
    }

    //====================================以下为私有辅助方法===============================================

    /**
     * 添加按钮事件监听器
     */
    private void setEventListener()
    {
        //删除后索引和图片总数需要重新设置
        selectButton.setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                //上传用户选择的图片。
                S3.createFileObjectForBucket(bucketName, files[displayIndex]);
                
                
                //返回S3内容列表页
                Intent bucketViewIntent = new Intent(FileSingleImageActivity.this, S3BucketView.class);
                
                //手机客户端在获取或上传S3对象时候，使用了用户名作为KEY前缀，暂时无法判断服务器端是否有对不同注册用户可操作权限的进一步控制。
                bucketViewIntent.putExtra(S3.BUCKET_NAME, PropertyLoader.getInstance().getBucketName() );
                bucketViewIntent.putExtra(S3.PREFIX, S3PersonalFileStore.clientManager.getUsername() );
                
                startActivity(bucketViewIntent);
                
                //FileSingleImageActivity.this.finish();

            }
        });

        returnButton.setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                Intent intent =FileSingleImageActivity.this.getIntent();

                FileSingleImageActivity.this.setResult(RESULT_OK, intent);
                FileSingleImageActivity.this.finish();
            }
        });

    }

    /**
     *
     * 显示用户通滑动屏幕选择的图片
     *
     * @param filePath 选择的图片
     *
     */
    private void displayImage(String filePath)
    {
        log.debug("displayImage() 开始显示图片文件" + filePath);

        File file = new File(filePath);
        Uri fileUri = Uri.fromFile(file);

        try
        {
            Bitmap bitmap = FileUtil.safeDecodeStream(false,
                                                      fileUri,
                                                      dm.widthPixels,
                                                      (int)(dm.heightPixels * 0.775),
                                                      this);

            //如果图片方向不对，需要旋转
            int degree = FileUtil.readPictureDegree(file.getAbsolutePath());
            bitmap = FileUtil.rotateBitmap(bitmap, degree);

            singleimage.setImageBitmap(bitmap);

            //清除上一个图片对象
            if (lastBitmap != null)
            {
                lastBitmap.recycle();

                lastBitmap = bitmap;
            }

//            page_title.setText(files[displayIndex].getName() + "[" +(displayIndex + 1)
//                             + " in " + files.length + "]");
        }
        catch (FileNotFoundException e)
        {
            log.error("getView()文件不存在", e);
        }
    }
}


