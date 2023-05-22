package com.chs.filepicker.filepicker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chs.filepicker.R;
import com.chs.filepicker.filepicker.adapter.CommonFileAdapter;
import com.chs.filepicker.filepicker.adapter.OnFileItemClickListener;
import com.chs.filepicker.filepicker.model.FileEntity;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * 作者：chs on 2017-08-24 11:04
 * 邮箱：657083984@qq.com
 * 常用文件
 */

public class FileCommonFragment extends Fragment implements FileScannerTask.FileScannerListener {
    private RecyclerView mRecyclerView;
    private TextView mEmptyView;
    private ProgressBar mProgressBar;
    private CommonFileAdapter mCommonFileAdapter;
    private OnUpdateDataListener mOnUpdateDataListener;

    public void setOnUpdateDataListener(OnUpdateDataListener onUpdateDataListener) {
        mOnUpdateDataListener = onUpdateDataListener;
    }

    public static FileCommonFragment newInstance() {
        return new FileCommonFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_normal, null);
        initView(view);
        initData();
        return view;
    }

    private void initView(View view) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.rl_normal_file);
        mRecyclerView.setLayoutManager(layoutManager);
        mEmptyView = (TextView) view.findViewById(R.id.empty_view);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void initData() {
        AndPermission.with(this)
                .runtime()
                .permission(Permission.Group.STORAGE)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        new FileScannerTask(getContext(), FileCommonFragment.this).execute();
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        Toast.makeText(getContext(), getString(R.string.write_premission_decline), Toast.LENGTH_LONG).show();
                    }
                })
                .start();
    }

    private void iniEvent(final List<FileEntity> entities) {
        mCommonFileAdapter.setOnItemClickListener(new OnFileItemClickListener() {
            @Override
            public void click(int position) {
                FileEntity entity = entities.get(position);
                ArrayList<FileEntity> files = PickerManager.getInstance().files;
                files.add(entity);
                getActivity().setResult(RESULT_OK);
                getActivity().finish();
//                if(files.contains(entity)){
//                    files.remove(entity);
//                    if(mOnUpdateDataListener!=null){
//                        mOnUpdateDataListener.update(-Long.parseLong(entity.getSize()));
//                    }
//                    entity.setSelected(!entity.isSelected());
//                    mCommonFileAdapter.notifyDataSetChanged();
//                }else {
//                    if(PickerManager.getInstance().files.size()<PickerManager.getInstance().maxCount){
//                        files.add(entity);
//                        if(mOnUpdateDataListener!=null){
//                            mOnUpdateDataListener.update(Long.parseLong(entity.getSize()));
//                        }
//                        entity.setSelected(!entity.isSelected());
//                        mCommonFileAdapter.notifyDataSetChanged();
//                    }else {
//                        Toast.makeText(getContext(),getString(R.string.file_select_max,PickerManager.getInstance().maxCount),Toast.LENGTH_SHORT).show();
//                    }
                //               }
            }
        });
    }

    @Override
    public void scannerResult(List<FileEntity> entities) {
        mProgressBar.setVisibility(View.GONE);
        if (entities.size() > 0) {
            mEmptyView.setVisibility(View.GONE);
        } else {
            mEmptyView.setVisibility(View.VISIBLE);
        }
        mCommonFileAdapter = new CommonFileAdapter(getContext(), entities);
        mRecyclerView.setAdapter(mCommonFileAdapter);
        iniEvent(entities);
    }
}
