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
 * ����ͼƬ�ļ����Activity
 *
 * @see com.weekbuy.mobile.filebrowser.*
 *
 */
public class FileBrowserActivity extends Activity implements Constants
{
    /**
     * ��־����
     */
    private static final Logger log = Logger.getLogger(FileBrowserActivity.class);

    //private TextView page_title;

    private ListView fileListView;

    /**
     * ��ǰĿ¼���µ������ļ�
     */
    private List<FileInfo> fileInfos = new ArrayList<FileInfo>();

    /**
     * SD���Ӵ洢��·��
     */
    private String rootPath = FileActivityHelper.getSDPhotoPath();

    /**
     * ��ǰ·��
     */
    private String currentPath = rootPath;

    /**
     * �ļ����ݹ�Ӧ������
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

        //1.���ò���[����·��������Ϊ��ǰ·���µ�����Ŀ¼���ļ��б�]
        setContentView(R.layout.filebrowser_main);

        //2.��ȡҳ��ؼ�
        //page_title   = (TextView) findViewById(R.id.page_title);
        fileListView = (ListView) findViewById(R.id.fb_main_list);

        //3.Ϊ�б�ؼ�ע�������Ĳ˵�
        registerForContextMenu(fileListView);

        //4.������(�����Ǹ�ListActivity)
        adapter = new FileAdapter(this, fileInfos);
        fileListView.setAdapter(adapter);

        //5.��ȡ��ǰĿ¼���ļ��б�
        viewFiles(currentPath);

        //6.���ñ���
        //page_title.setText("ѡ�񱾵�ͼƬ");


        //8.�����¼�������
        setEventListener();
    }

    /** ��ȡ��ͼƬѡ��ҳ�淵�ص��ļ�·�� **/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        log.debug("onActivityResult() in");
        if (Activity.RESULT_OK == resultCode)
        {

            Intent intent =FileBrowserActivity.this.getIntent();

            FileBrowserActivity.this.setResult(RESULT_OK, intent);
            FileBrowserActivity.this.finish();

            log.warn("onActivityResult()�û����ͼƬѡ�񣬹ر�ͼƬ�������");
        }
        else
        {
            log.warn("onActivityResult()�û�û��ѡ��һ��ͼƬ");
        }

        log.debug("onActivityResult() out");
    }

    /** �ض��巵�ؼ��¼� **/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        // ����back����
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
     * �����¼�������
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
                log.debug("onItemClick() item ����¼�����");

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
     * ��ȡ��Ŀ¼�������ļ�
     *
     * @param filePath Ŀ¼·��
     *
     */
    private void viewFiles(String filePath)
    {
        //���ð�����ķ�����ȡָ��Ŀ¼�µ������ļ�   TODO ��Ҫ�޸�Ϊֻ��ȡͼƬ�ļ�
        ArrayList<FileInfo> tmp = FileActivityHelper.getFiles(filePath);

        if (tmp != null)
        {
            //�������
            fileInfos.clear();
            fileInfos.addAll(tmp);

            tmp.clear();

            //���õ�ǰĿ¼
            currentPath = filePath;

            //page_title.setText(filePath);

            //֪ͨ�����������ݸı�
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * �鿴ͼƬ
     *
     * TODO ��Ҫ�޸�Ϊ����ϵͳ�ڲ��ķ�ʽ�鿴��ѡ��ͼƬ
     *
     * @param filePath Ŀ¼·��
     *
     */
    private void openFile(String path)
    {
        Intent intent = new Intent(FileBrowserActivity.this,
                                   FileSingleImageActivity.class);

        //���ݻ����е�λ�ã���ʾ��Ӧ����Ƭ
        intent.putExtra(INTENT_KEY_TITLE, "�򿪵�ͼƬ�ļ�");
        intent.putExtra(INTENT_KEY_FILE_PATH, path);
        
        intent.putExtra(S3.BUCKET_NAME, bucketName);
        intent.putExtra(S3.PREFIX, prefix);

        startActivityForResult(intent, 0);
    }
}


