package com.amazonaws.demo.personalfilestore.filebrowser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.amazonaws.demo.personalfilestore.R;
import com.amazonaws.demo.personalfilestore.s3.S3;


/**
 *
 * 本机图片文件浏览Activity
 *
 * @see com.weekbuy.mobile.filebrowser.*
 *
 */
public class FileBrowserActivity extends Activity implements Constants
{
    /**
     * 日志对象
     */
    private static final Logger log = Logger.getLogger(FileBrowserActivity.class);

    //private TextView page_title;

    private ListView fileListView;

    /**
     * 当前目录需下的所有文件
     */
    private List<FileInfo> fileInfos = new ArrayList<FileInfo>();

    /**
     * SD附加存储根路径
     */
    private String rootPath = FileActivityHelper.getSDPhotoPath();

    /**
     * 当前路径
     */
    private String currentPath = rootPath;

    /**
     * 文件数据供应适配器
     */
    private BaseAdapter adapter = null;
    
    protected String bucketName;
    protected String prefix;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        Bundle extras = this.getIntent().getExtras();
        bucketName = extras.getString(S3.BUCKET_NAME);
        prefix = extras.getString(S3.PREFIX);

        rootPath     = FileActivityHelper.getSDPhotoPath();
        currentPath  = rootPath;

        //1.设置布局[顶部路径，主体为当前路径下的所有目录和文件列表]
        setContentView(R.layout.filebrowser_main);

        //2.获取页面控件
        //page_title   = (TextView) findViewById(R.id.page_title);
        fileListView = (ListView) findViewById(R.id.fb_main_list);

        //3.为列表控件注册上下文菜单
        registerForContextMenu(fileListView);

        //4.绑定数据(本身是个ListActivity)
        adapter = new FileAdapter(this, fileInfos);
        fileListView.setAdapter(adapter);

        //5.获取当前目录的文件列表
        viewFiles(currentPath);

        //6.设置标题
        //page_title.setText("选择本地图片");


        //8.设置事件监听器
        setEventListener();
    }

    /** 获取从图片选择页面返回的文件路径 **/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        log.debug("onActivityResult() in");
        if (Activity.RESULT_OK == resultCode)
        {

            Intent intent =FileBrowserActivity.this.getIntent();

            FileBrowserActivity.this.setResult(RESULT_OK, intent);
            FileBrowserActivity.this.finish();

            log.warn("onActivityResult()用户完成图片选择，关闭图片浏览窗口");
        }
        else
        {
            log.warn("onActivityResult()用户没有选择一个图片");
        }

        log.debug("onActivityResult() out");
    }

    /** 重定义返回键事件 **/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        // 拦截back按键
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            File f = new File(currentPath);
            String parentPath = f.getParent();

            if (parentPath != null)
            {
                viewFiles(parentPath);

                return true;
            }
            else
            {
                return super.onKeyDown(keyCode, event);
            }

        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * 设置事件监听器
     *
     *
     */
    private void setEventListener()
    {
        fileListView.setOnItemClickListener(new OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> view, View child, int position, long id)
            {
                log.debug("onItemClick() item 点击事件触发");

                FileInfo f = fileInfos.get(position);

                if (f.isDirectory())
                {
                    viewFiles(f.getPath());
                }
                else
                {
                    openFile(f.getPath());
                }
            }
        });
    }


    /**
     * 获取该目录下所有文件
     *
     * @param filePath 目录路径
     *
     */
    private void viewFiles(String filePath)
    {
        //调用帮助类的方法获取指定目录下的所有文件   TODO 需要修改为只获取图片文件
        ArrayList<FileInfo> tmp = FileActivityHelper.getFiles(filePath);

        if (tmp != null)
        {
            //清空数据
            fileInfos.clear();
            fileInfos.addAll(tmp);

            tmp.clear();

            //设置当前目录
            currentPath = filePath;

            //page_title.setText(filePath);

            //通知适配器，数据改变
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * 查看图片
     *
     * TODO 需要修改为采用系统内部的方式查看并选择图片
     *
     * @param filePath 目录路径
     *
     */
    private void openFile(String path)
    {
        Intent intent = new Intent(FileBrowserActivity.this,
                                   FileSingleImageActivity.class);

        //根据画廊中的位置，显示对应的相片
        intent.putExtra(INTENT_KEY_TITLE, "打开的图片文件");
        intent.putExtra(INTENT_KEY_FILE_PATH, path);
        
        intent.putExtra(S3.BUCKET_NAME, bucketName);
        intent.putExtra(S3.PREFIX, prefix);

        startActivityForResult(intent, 0);
    }
}


