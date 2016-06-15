package com.amazonaws.demo.personalfilestore.filebrowser;

import com.amazonaws.demo.personalfilestore.R;

/**
 *
 * 表示一个文件实体
 *
 */
public class FileInfo
{
    private String name;
    private String path;

    private long size;

    private boolean directory = false;

    private int fileCount   = 0;
    private int folderCount = 0;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public long getSize()
    {
        return size;
    }

    public void setSize(long size)
    {
        this.size = size;
    }

    public boolean isDirectory()
    {
        return directory;
    }

    public void setDirectory(boolean isDirectory)
    {
        directory = isDirectory;
    }

    public int getFileCount()
    {
        return fileCount;
    }

    public void setFileCount(int fileCount)
    {
        this.fileCount = fileCount;
    }

    public int getFolderCount()
    {
        return folderCount;
    }

    public void setFolderCount(int folderCount)
    {
        this.folderCount = folderCount;
    }

    public int getIconResourceId()
    {
        if (directory)
        {
            return R.drawable.folder;
        }

        return R.drawable.doc;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("Name = ").append(name).append(", ")
          .append("Path = ").append(path).append(", ")
          .append("Size = ").append(size).append(", ")
          .append("FileCount = ").append(fileCount).append(", ")
          .append("FolderCount = ").append(folderCount).append(", ")
          .append("\n");

        return sb.toString();
    }
}