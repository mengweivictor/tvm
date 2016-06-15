package com.amazonaws.demo.personalfilestore.filebrowser;

import java.util.Comparator;


/**
 * 
 * FileComparator�ıȽ�������
 * 
 */
public class FileComparator implements Comparator<FileInfo>
{

    public int compare(FileInfo file1, FileInfo file2)
    {
        // �ļ�������ǰ��
        if (file1.isDirectory() && !file2.isDirectory())
        {
            return -1000;
        }
        else if (!file1.isDirectory() && file2.isDirectory())
        {
            return 1000;
        }
        
        // ��ͬ���Ͱ���������
        return file1.getName().compareTo(file2.getName());
    }
}