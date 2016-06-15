package com.amazonaws.demo.personalfilestore.filebrowser;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.demo.personalfilestore.R;

/**
 * 列表浏览文件时候的是文件内容适配器类
 *
 */
public class FileAdapter extends BaseAdapter
{

    private LayoutInflater inflater;
    private List<FileInfo> fileInfos;

    public FileAdapter(Context context, List<FileInfo> fileInfos)
    {
        this.fileInfos = fileInfos;
        this.inflater  = LayoutInflater.from(context);
    }

    @Override
    public int getCount()
    {
        return fileInfos.size();
    }

    @Override
    public Object getItem(int position)
    {
        return fileInfos.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder = null;

        if (convertView == null)
        {
            // convertView 可利用，如果传入为null，执行初始化操作载入xml文件为View
            convertView = inflater.inflate(R.layout.filebrowser_item, null);

            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.file_name);
            holder.icon = (ImageView) convertView.findViewById(R.id.file_icon);

            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        // 更新View信息
        FileInfo fileInfo = fileInfos.get(position);
        holder.name.setText(fileInfo.getName());
        holder.icon.setImageResource(fileInfo.getIconResourceId());

        return convertView;
    }

    /* class ViewHolder */
    private class ViewHolder
    {
        TextView name;
        ImageView icon;
    }
}
