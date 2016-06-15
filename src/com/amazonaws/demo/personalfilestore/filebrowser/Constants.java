package com.amazonaws.demo.personalfilestore.filebrowser;


/**
 *
 * ȫ��ʹ�õĸ��ֳ���
 *
 */
public interface Constants
{

    /**
     * ������ĳɹ���ʧ��
     *
     */
    public static final int SUCCESS = 0;
    public static final int FAILED =  1;

    /**
     * ����������ͼ�
     *
     */
    public static final int INT_TRUE  = 0;
    public static final int INT_FALSE = 1;

    public static final String STRING_TRUE  = "true";
    public static final String STRING_FALSE = "false";
    
    /**
     * ˮƽ�����¼������У��û����뻬�������ظ���
     *
     */
    public static final int FLING_X_LOW_LIMIT = 100;

    
    /**
     * Intent ���ݴ��ݹؼ���
     *
     */
    public static final String INTENT_KEY_TITLE           = "TITLE";              //Activity����
    public static final String INTENT_KEY_DATA            = "DATA";               //Json�����ַ���
    public static final String INTENT_KEY_FILE_PATH       = "FILE_PATH";          //�ļ�·��
    public static final String INTENT_KEY_FILE_PATH_LIST  = "FILE_PATH_LIST";     //�ļ�·���б�
    public static final String INTENT_KEY_TYPE            = "TYPE";               //һ������
    public static final String INTENT_KEY_SUBTYPE         = "SUBTYPE";            //��������
    public static final String INTENT_KEY_APK_URL         = "APK_URL";            //�����װ������·��
    public static final String INTENT_KEY_PACKAGE_URL     = "PACKAGE_URL";        //�������ݰ�����·��
    public static final String INTENT_KEY_CATEGORY_KEYS   = "CATEGORY_KEYS";      //�����ѯ�ؼ���
    public static final String INTENT_KEY_ORDER_ITEM      = "ORDER_ITEM";         //������

    
}
