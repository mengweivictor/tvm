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
 * ��Ϣ����������ͼƬ����ϸ��ͼ��ʾҳ��
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
     * �����¼�̽����
     */
    private GestureDetector mGestureDetector;

    /**
     * ��ǰ��ʾͼƬ����
     */
    private int displayIndex = -1;

    /**
     * ���е�ͼƬ�ļ�
     */
    private File[] files;

    /**
     * ��ǰ��Ļ�ֱ���
     */
    private DisplayMetrics dm;


    /**
     * ����ͼ�οؼ�
     */
    private ImageView singleimage;

    //private TextView page_title;

    private Button selectButton;
    private Button returnButton;

    private Bitmap lastBitmap;
    
    protected String bucketName;
    protected String prefix;

    /**
     * ϵͳȫ�������Ķ���
     */
    private S3PersonalFileStoreApplication application = S3PersonalFileStoreApplication.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //1. ���ȵ��ø����ͬ������
        super.onCreate(savedInstanceState);
        log.debug("onCreate() IN");

        //2. �����ҳ���ݵĲ���
        Bundle extras = this.getIntent().getExtras();
        bucketName = extras.getString(S3.BUCKET_NAME);
        prefix = extras.getString(S3.PREFIX);
        
        log.debug("onCreate() bucketName = " + bucketName);
        log.debug("onCreate() prefix = " + prefix);

        //3. ���ý��沼��
        setContentView(R.layout.filebrowser_single_image);

        //4. ��ȡ��ҳ��������ʾͼƬ·���ͱ���
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

        //5. �жϳ�ʼͼƬ������λ��
        for (int i = 0 ; i < files.length ; i++)
        {
            files[i] = tempFileList.get(i);

            if (files[i].getAbsolutePath().equals(filePath))
            {
                displayIndex = i;

                log.debug("�����˳�ʼ��ʾ��ͼƬλ��:" + displayIndex);
            }
        }

        //6. ��ȡ����ؼ�
        //page_title   = (TextView)findViewById(R.id.page_title);
        singleimage  = (ImageView)findViewById(R.id.fb_single_image_view);

        selectButton = (Button)findViewById(R.id.fb_single_image_button_select);
        returnButton = (Button)findViewById(R.id.fb_single_image_button_return);

        //7. ���ó�ʼ����
//        page_title.setText(files[displayIndex].getName() + "[" +(displayIndex + 1)
//                         + " in " + files.length + "]");

        //8. ��Ļ����
        dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        log.debug("dm.heightPixels = " + dm.heightPixels);

        //9. ����ͼƬ��ʾ����
        singleimage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        LinearLayout.LayoutParams linearParams =
                (LinearLayout.LayoutParams) singleimage.getLayoutParams();

        linearParams.height = (int)(dm.heightPixels * 0.775);

        singleimage.setLayoutParams(linearParams);

        //10. ��ʾ��ʼͼƬ
        displayImage(filePath);

        //11. �����¼�
        setEventListener();

        mGestureDetector = new GestureDetector(this);

        log.debug("onCreate() OUT");
    }


    //==================================���������¼��� ʵ�ֻ�����Ļ�л�ͼƬ=====================================
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
            log.debug("onFling()�û����󻬶���Ļ");

            if (displayIndex > 0)
            {
                displayIndex--;

                displayImage(files[displayIndex].getAbsolutePath());
            }

            return true;
        }

        if (gapX < -FLING_X_LOW_LIMIT)
        {
            log.debug("onFling()�û����һ�����Ļ");

            if (displayIndex < files.length - 1)
            {
                displayIndex++;

                displayImage(files[displayIndex].getAbsolutePath());
            }

            return true;
        }

        return false;
    }

    //====================================����Ϊ˽�и�������===============================================

    /**
     * ��Ӱ�ť�¼�������
     */
    private void setEventListener()
    {
        //ɾ����������ͼƬ������Ҫ��������
        selectButton.setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                //�ϴ��û�ѡ���ͼƬ��
                S3.createFileObjectForBucket(bucketName, files[displayIndex]);
                
                
                //����S3�����б�ҳ
                Intent bucketViewIntent = new Intent(FileSingleImageActivity.this, S3BucketView.class);
                
                //�ֻ��ͻ����ڻ�ȡ���ϴ�S3����ʱ��ʹ�����û�����ΪKEYǰ׺����ʱ�޷��жϷ��������Ƿ��жԲ�ͬע���û��ɲ���Ȩ�޵Ľ�һ�����ơ�
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
     * ��ʾ�û�ͨ������Ļѡ���ͼƬ
     *
     * @param filePath ѡ���ͼƬ
     *
     */
    private void displayImage(String filePath)
    {
        log.debug("displayImage() ��ʼ��ʾͼƬ�ļ�" + filePath);

        File file = new File(filePath);
        Uri fileUri = Uri.fromFile(file);

        try
        {
            Bitmap bitmap = FileUtil.safeDecodeStream(false,
                                                      fileUri,
                                                      dm.widthPixels,
                                                      (int)(dm.heightPixels * 0.775),
                                                      this);

            //���ͼƬ���򲻶ԣ���Ҫ��ת
            int degree = FileUtil.readPictureDegree(file.getAbsolutePath());
            bitmap = FileUtil.rotateBitmap(bitmap, degree);

            singleimage.setImageBitmap(bitmap);

            //�����һ��ͼƬ����
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
            log.error("getView()�ļ�������", e);
        }
    }
}


