package com.alsc.chat.adapter;

import com.alsc.chat.R;
import com.alsc.chat.bean.LocationBean;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LocationAdapter extends BaseQuickAdapter<LocationBean, BaseViewHolder> {


    public LocationAdapter() {
        super(R.layout.item_location);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder helper, @Nullable LocationBean bean) {
        helper.setText(R.id.tvName, bean.getPoiItem().getTitle())
                .setText(R.id.tvAddress, bean.getPoiItem().getCityName()+bean.getPoiItem().getAdName() + bean.getPoiItem().getSnippet())
                .setVisible(R.id.ivCheck, bean.isCheck());
    }


    public void resetSelected(int index) {
        List<LocationBean> list = getData();
        int pos = 0;
        for (LocationBean bean : list) {
            bean.setCheck(pos++ == index);
        }
        notifyDataSetChanged();
    }
}
