package com.example.simple_recorder.audio;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.example.simple_recorder.R;
import com.example.simple_recorder.bean.AudioBean;
import com.example.simple_recorder.databinding.ItemAudioBinding;

import java.util.List;

public class AudioListAdapter extends BaseAdapter {

    private Context context;
    private List<AudioBean>mDatas;
    //点击每一个itemView当中的playIv都能够回调的接口
    public interface OnItemPlayClickListener{
        void onItemPlayClick(AudioListAdapter adapter,View convertView,View playView,int position);
    }
    private OnItemPlayClickListener onItemPlayClickListener;

    public void setOnItemPlayClickListener(OnItemPlayClickListener onItemPlayClickListener) {
        this.onItemPlayClickListener = onItemPlayClickListener;
    }

    public AudioListAdapter(Context context, List<AudioBean> mDatas) {
        this.context = context;
        this.mDatas = mDatas;
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int i) {
        return mDatas.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_audio,viewGroup,false);
            view.setTag(holder);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }else {
            holder = (ViewHolder) view.getTag();
        }
        //获取指定位置的数据对于控件进行设置
        AudioBean audioBean = mDatas.get(i);
        holder.ab.tvTime.setText(audioBean.getTime());
        holder.ab.tvDuration.setText(audioBean.getDuration());
        holder.ab.tvTitle.setText(audioBean.getTitle());
        if (audioBean.isPlaying()==true) {//当前这条正在播放
            holder.ab.lvControll.setVisibility(View.VISIBLE);
            holder.ab.pb.setMax(100);
            holder.ab.pb.setProgress(audioBean.getCurrentProgress());
            holder.ab.ivPlay.setImageResource(R.mipmap.red_pause);
        }else{
            holder.ab.ivPlay.setImageResource(R.mipmap.red_play);
            holder.ab.lvControll.setVisibility(View.GONE);
        }
        View itemView = view;
        //点击播放的图标可以播放或者暂停录音的内容
        holder.ab.ivPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemPlayClickListener!=null) {
                    onItemPlayClickListener.onItemPlayClick(AudioListAdapter.this,itemView,v,i);
                }
            }
        });
        return view;
    }
    class ViewHolder{
        ItemAudioBinding ab;
        public ViewHolder(View v){
            ab = ItemAudioBinding.bind(v);
        }
    }
}
