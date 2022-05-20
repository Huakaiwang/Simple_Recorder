package com.example.simple_recorder.notepad;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.recyclerview.widget.RecyclerView;

import com.example.simple_recorder.R;
import com.example.simple_recorder.bean.NotepadBean;
import com.example.simple_recorder.databinding.ItemNotepadBinding;

import java.util.List;

public class NotepadAdapter extends BaseAdapter {
    private LayoutInflater layoutInflater;
    private Context context;
    private List<NotepadBean> list;
    public NotepadAdapter(Context context, List<NotepadBean> list){
        this.layoutInflater = LayoutInflater.from(context);
        this.context = context;
        this.list = list;
    }
    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView==null) {
            convertView = layoutInflater.inflate(R.layout.item_notepad,null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }

        NotepadBean notepadInfo = (NotepadBean) getItem(position);
        holder.nb.itemContent.setText(notepadInfo.getNotepadContent());
        holder.nb.itemTime.setText(notepadInfo.getNotepadTime());
        return convertView;
    }
    class ViewHolder{
        ItemNotepadBinding nb;
        public ViewHolder(View view){
            nb = ItemNotepadBinding.bind(view);
        }
    }
}
