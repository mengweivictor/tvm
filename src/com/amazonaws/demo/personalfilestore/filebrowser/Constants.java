package com.amazonaws.demo.personalfilestore.filebrowser;


/**
 *
 * 全局使用的各种常量
 *
 */
public interface Constants
{

    /**
     * 最基本的成功和失败
     *
     */
    public static final int SUCCESS = 0;
    public static final int FAILED =  1;

    /**
     * 整数表达的真和假
     *
     */
    public static final int INT_TRUE  = 0;
    public static final int INT_FALSE = 1;

    public static final String STRING_TRUE  = "true";
    public static final String STRING_FALSE = "false";
    
    /**
     * 水平滑动事件触发中，用户必须滑动的像素个数
     *
     */
    public static final int FLING_X_LOW_LIMIT = 100;

    
    /**
     * Intent 数据传递关键字
     *
     */
    public static final String INTENT_KEY_TITLE           = "TITLE";              //Activity标题
    public static final String INTENT_KEY_DATA            = "DATA";               //Json内容字符串
    public static final String INTENT_KEY_FILE_PATH       = "FILE_PATH";          //文件路径
    public static final String INTENT_KEY_FILE_PATH_LIST  = "FILE_PATH_LIST";     //文件路径列表
    public static final String INTENT_KEY_TYPE            = "TYPE";               //一级分类
    public static final String INTENT_KEY_SUBTYPE         = "SUBTYPE";            //二级分类
    public static final String INTENT_KEY_APK_URL         = "APK_URL";            //软件安装包下载路径
    public static final String INTENT_KEY_PACKAGE_URL     = "PACKAGE_URL";        //本地数据包下载路径
    public static final String INTENT_KEY_CATEGORY_KEYS   = "CATEGORY_KEYS";      //分类查询关键字
    public static final String INTENT_KEY_ORDER_ITEM      = "ORDER_ITEM";         //订单项

    
}
