package com.example.myapplication.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.device.Equipment;

public class EquipmentAdapter extends BaseAdapter implements OnClickListener {

    private List<Equipment> mList;
    private Context mContext;
    private InnerItemOnclickListener mListener;

    public EquipmentAdapter(List<Equipment> mList, Context mContext) {
        this.mList = mList;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        // TODO 自动生成的方法存根
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO 自动生成的方法存根
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO 自动生成的方法存根
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.main_lv_item_layout,
                    null);  // 将布局转换成view对象
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // 填充每一个Item数据
        viewHolder.ivEquimpmentSwitch.setOnClickListener(this);
        viewHolder.ivEquimpmentSwitch.setTag(position);
        if ((mList.get(position).getSwitchOfPic() == R.drawable.power_close)
                || (mList.get(position).getSwitchOfPic() == R.drawable.power_open)) {
            viewHolder.ivEquimpmentSwitch.setImageResource(mList.get(position).getSwitchOfPic());
            viewHolder.ivEquimpmentSwitch.setVisibility(View.VISIBLE);
        } else {
            viewHolder.ivEquimpmentSwitch.setVisibility(View.INVISIBLE);
        }

        viewHolder.tvEquipmentId.setText(mList.get(position).getId());
        viewHolder.tvEquipmentName.setText(mList.get(position).getName());

        if ((mList.get(position).getTemperature() != null) || (mList.get(position).getHumidity() != null))
            viewHolder.tvTHData.setText("温度："
                    + mList.get(position).getTemperature() + "℃ 湿度：" + mList.get(position).getHumidity() + "%");

        if (mList.get(position).getStatus() != null)
            viewHolder.tvEquipmentStatus.setText(mList.get(position).getStatus());

        return convertView;
    }

    public final static class ViewHolder {
        ImageView ivEquipmentPic;
        TextView tvEquipmentName, tvEquipmentId, tvEquipmentStatus, tvTHData;
        private ImageView ivEquimpmentSwitch;

        public ViewHolder(View view) {
            ivEquipmentPic = view.findViewById(R.id.iv_equipment_icon);
            tvEquipmentName = view.findViewById(R.id.tv_equipment_name);
            tvEquipmentId = view.findViewById(R.id.tv_equipment_id);
            tvEquipmentStatus = view.findViewById(R.id.tv_equipment_status);
            tvTHData = view.findViewById(R.id.tv_equipment_THumidity);
            ivEquimpmentSwitch = view.findViewById(R.id.ibtn_equipment_switch);
        }
    }

    public interface InnerItemOnclickListener {
        void itemClick(View v);
    }

    public void setOnInnerItemOnClickListener(InnerItemOnclickListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onClick(View v) {
        mListener.itemClick(v);
    }
}

