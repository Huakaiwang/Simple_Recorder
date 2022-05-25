package com.example.simple_recorder.expandelist;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import com.example.simple_recorder.R;
import com.example.simple_recorder.bean.NoteGroupBean;
import com.example.simple_recorder.bean.NotepadBean;
import com.example.simple_recorder.databinding.ActivityParentBinding;
import com.example.simple_recorder.databinding.ItemNotepadBinding;
import com.example.simple_recorder.notepad.NotepadAdapter;

import java.util.List;

public class ExpandeAdapter extends BaseExpandableListAdapter {
    private LayoutInflater layoutInflater;
    private Context context;
    private List<NotepadBean> list;
    private List<NoteGroupBean> gList;
    private List<List<NotepadBean>> checkList;

    public ExpandeAdapter(Context context,List<NoteGroupBean> glist ,List<List<NotepadBean>> checkList){
        this.layoutInflater = LayoutInflater.from(context);
        this.gList = glist;
        this.checkList = checkList;
    }
    @Override
    public int getGroupCount() {
        return gList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        Log.d("TAG", "getChildrenCount: "+checkList.get(groupPosition).size());
        return checkList.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return gList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        Log.d("TAG", "getChild: "+checkList.get(groupPosition).get(childPosition).getNotepadContent());
        return checkList.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupHolder holder  = null;
        if (convertView==null) {
            convertView = layoutInflater.inflate(R.layout.activity_parent,null);
            holder = new GroupHolder(convertView);
            convertView.setTag(holder);
        }else {
            holder = (GroupHolder)convertView.getTag();
        }
        NoteGroupBean group = (NoteGroupBean) gList.get(groupPosition);
        holder.pb.exTv.setText(group.getGroupName());
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView==null) {
            convertView = layoutInflater.inflate(R.layout.item_notepad,null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        NotepadBean notepadInfo = (NotepadBean) getChild(groupPosition,childPosition);
        holder.nb.itemContent.setText(notepadInfo.getNotepadContent());
        holder.nb.itemTime.setText(notepadInfo.getNotepadTime());
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
    class GroupHolder{
        ActivityParentBinding pb;
        public GroupHolder(View view){ pb = ActivityParentBinding.bind(view);}
    }
    class ViewHolder{
        ItemNotepadBinding nb;
        public ViewHolder(View view){
            nb = ItemNotepadBinding.bind(view);
        }
    }
}
