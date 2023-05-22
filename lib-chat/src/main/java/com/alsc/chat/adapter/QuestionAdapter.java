package com.alsc.chat.adapter;


import com.alsc.chat.R;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.common.lib.bean.QuestionBean;

public class QuestionAdapter extends BaseQuickAdapter<QuestionBean, BaseViewHolder> {


    public QuestionAdapter() {
        super(R.layout.item_question);
    }

    @Override
    protected void convert(BaseViewHolder helper, QuestionBean item) {
        helper.setText(R.id.tvQuestion, item.getName());
    }
}
