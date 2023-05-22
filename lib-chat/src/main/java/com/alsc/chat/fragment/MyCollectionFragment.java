package com.alsc.chat.fragment;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alsc.chat.R;
import com.alsc.chat.adapter.MyCollectionAdapter;
import com.alsc.chat.http.ChatHttpMethods;
import com.alsc.chat.http.OnHttpErrorListener;
import com.alsc.chat.manager.ChatManager;
import com.alsc.chat.utils.Constants;
import com.alsc.chat.utils.Utils;
import com.alsc.chat.http.HttpObserver;
import com.alsc.chat.http.SubscriberOnNextListener;
import com.alsc.chat.activity.ChatBaseActivity;
import com.common.lib.activity.BaseActivity;
import com.common.lib.bean.*;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.listener.OnItemLongClickListener;
import com.common.lib.dialog.MyDialogFragment;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;
import java.util.ArrayList;


public class MyCollectionFragment extends ChatBaseFragment implements OnRefreshLoadMoreListener {

    private int mPageNo;
    private MyCollectionAdapter mAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_my_collection;
    }

    @Override
    protected void onViewCreated(View view) {
        setTopStatusBarStyle(R.id.flTop);
        setText(R.id.tvTitle, R.string.chat_my_collection);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        getAdapter().onAttachedToRecyclerView(recyclerView);
        recyclerView.setAdapter(getAdapter());
        SmartRefreshLayout layout = view.findViewById(R.id.smartRefreshLayout);
        layout.setOnRefreshLoadMoreListener(this);
        layout.autoRefresh();
        layout.setEnableLoadMore(false);
    }

    private MyCollectionAdapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = new MyCollectionAdapter(getActivity());
            mAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                    CollectionBean bean = mAdapter.getItem(position);
                    try {
                        final BasicMessage msg;
                        if (TextUtils.isEmpty(bean.getGroupId())) {
                            msg = ChatManager.getInstance().getGson().fromJson(bean.getContent(), MessageBean.class);
                        } else {
                            msg = ChatManager.getInstance().getGson().fromJson(bean.getContent(), GroupMessageBean.class);
                        }
                        if (msg.getMsgType() != MessageType.TYPE_TEXT.ordinal()) {
                            Bundle bundle = new Bundle();
                            bundle.putSerializable(Constants.BUNDLE_EXTRA, ShowMessageFragment.TYPE_SHOW_CHAT_MESSAGE);
                            bundle.putSerializable(Constants.BUNDLE_EXTRA_2, msg);
                            if (msg.getMsgType() == MessageType.TYPE_IMAGE.ordinal()
                                    || msg.getMsgType() == MessageType.TYPE_VIDEO.ordinal()) {
                                ArrayList<BasicMessage> list = new ArrayList<>();
                                list.add(msg);
                                bundle.putSerializable(Constants.BUNDLE_EXTRA_3, list);
                            }
                            gotoPager(ShowMessageFragment.class, bundle);
                        }
                    } catch (Exception e) {

                    }
                }
            });

            mAdapter.setOnItemLongClickListener(new OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(@NonNull BaseQuickAdapter adapter, @NonNull final View locationView, int position) {
                    try {
                        CollectionBean bean = mAdapter.getItem(position);
                        final BasicMessage msg;
                        if (TextUtils.isEmpty(bean.getGroupId())) {
                            msg = ChatManager.getInstance().getGson().fromJson(bean.getContent(), MessageBean.class);
                        } else {
                            msg = ChatManager.getInstance().getGson().fromJson(bean.getContent(), GroupMessageBean.class);
                        }
                        final MyDialogFragment dialogFragment = new MyDialogFragment(R.layout.layout_msg_more_dialog);
                        dialogFragment.setOnMyDialogListener(new MyDialogFragment.OnMyDialogListener() {
                            @Override
                            public void initView(View view) {
                                int msgType = msg.getMsgType();
                                view.findViewById(R.id.tvCollect).setVisibility(View.GONE);
                                view.findViewById(R.id.tvMutiChoose).setVisibility(View.GONE);
                                view.findViewById(R.id.tvTranslate).setVisibility(View.GONE);
                                view.findViewById(R.id.tvSave).setVisibility(View.GONE);
                                if (msgType != MessageType.TYPE_TEXT.ordinal()) {
                                    view.findViewById(R.id.tvCopy).setVisibility(View.GONE);
                                }
                                dialogFragment.setDialogViewsOnClickListener(view, R.id.tvCopy, R.id.tvSendToFriend,
                                        R.id.tvDelete, R.id.ll, R.id.view);
                                view.findViewById(R.id.paddingView).setVisibility(View.GONE);
                                View llRoot = view.findViewById(R.id.llRoot);
                                int heigth = Utils.dip2px(getActivity(), 237);
                                int screenHeight = ((BaseActivity) getActivity()).getDisplayMetrics().heightPixels;
                                int bottom = Utils.dip2px(getActivity(), 50);
                                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) llRoot.getLayoutParams();
                                int[] location = new int[2];
                                locationView.getLocationOnScreen(location);
                                if (heigth + location[1] > screenHeight - bottom) {
                                    lp.topMargin = location[1] - heigth;
                                } else {
                                    lp.topMargin = location[1];
                                }
                                llRoot.setLayoutParams(lp);
                            }

                            @Override
                            public void onViewClick(int viewId) {
                                if (viewId == R.id.tvCopy) {
                                    ClipboardManager cm = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                                    ClipData mClipData = ClipData.newPlainText("Label", msg.getContent());
                                    cm.setPrimaryClip(mClipData);
                                    showToast(R.string.chat_copy_successful);
                                } else if (viewId == R.id.tvSendToFriend) {
                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable(Constants.BUNDLE_EXTRA, msg);
                                    gotoPager(TransferMsgFragment.class, bundle);
                                } else if (viewId == R.id.tvDelete) {
                                    ChatHttpMethods.getInstance().delCollection(bean.getId(), new HttpObserver(new SubscriberOnNextListener<Object>() {
                                        @Override
                                        public void onNext(Object o, String msg) {
                                            if (getView() == null) {
                                                return;
                                            }
                                            getAdapter().remove(bean);
                                        }
                                    }, getActivity(), (ChatBaseActivity) getActivity()));
                                }
                            }
                        });
                        dialogFragment.show(getChildFragmentManager(), "MyDialogFragment");
                    } catch (Exception e) {

                    }
                    return true;
                }
            });
        }
        return mAdapter;
    }


    @Override
    public void updateUIText() {

    }

    @Override
    public void onClick(View v) {
    }

    @Override
    public void onLoadMore(RefreshLayout refreshlayout) {
        getCollections(mPageNo + 1);
    }

    @Override
    public void onRefresh(RefreshLayout refreshlayout) {
        getCollections(1);
    }


    private void getCollections(int pageNo) {
        ChatHttpMethods.getInstance().getCollections(pageNo,
                new HttpObserver(new SubscriberOnNextListener<ArrayList<CollectionBean>>() {
                    @Override
                    public void onNext(ArrayList<CollectionBean> list, String msg) {
                        if (getView() == null) {
                            return;
                        }
                        finishRefreshLoad();
                        mPageNo = pageNo;
                        if (mPageNo == 1) {
                            getAdapter().setNewInstance(list);
                        } else {
                            getAdapter().addData(list);
                        }
                        SmartRefreshLayout smartRefreshLayout = getView().findViewById(R.id.smartRefreshLayout);
                        smartRefreshLayout.setEnableLoadMore(list.size() == 20);
                        getAdapter().notifyDataSetChanged();
                    }
                }, getActivity(), false, new OnHttpErrorListener() {
                    @Override
                    public void onConnectError(Throwable e) {
                        if (getView() == null) {
                            return;
                        }
                        finishRefreshLoad();
                    }

                    @Override
                    public void onServerError(int errorCode, String errorMsg) {
                        if (getView() == null) {
                            return;
                        }
                        finishRefreshLoad();
                    }
                }));
    }

    private void finishRefreshLoad() {
        if (getView() == null) {
            return;
        }
        SmartRefreshLayout smartRefreshLayout = getView().findViewById(R.id.smartRefreshLayout);
        smartRefreshLayout.finishRefresh();
        smartRefreshLayout.finishLoadMore();
    }

    public boolean isNeedSetTopStyle() {
        return false;
    }

}
