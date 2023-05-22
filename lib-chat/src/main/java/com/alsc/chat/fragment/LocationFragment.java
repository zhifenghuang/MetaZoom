package com.alsc.chat.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alsc.chat.R;
import com.alsc.chat.adapter.LocationAdapter;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.alsc.chat.bean.LocationBean;
import com.alsc.chat.utils.Utils;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.Projection;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.alsc.chat.http.ChatHttpMethods;
import com.alsc.chat.http.HttpObserver;
import com.alsc.chat.http.SubscriberOnNextListener;
import com.alsc.chat.activity.ChatBaseActivity;
import com.common.lib.bean.*;
import com.common.lib.dialog.MyDialogFragment;
import com.common.lib.manager.DataManager;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LocationFragment extends ChatBaseFragment implements LocationSource, AMapLocationListener {


    private MapView mMapView;
    private AMap mAMap;
    public AMapLocationClient mLocationClient = null;
    public AMapLocationClientOption mLocationOption = null;
    private long mStartTime;
    private LocationSource.OnLocationChangedListener mOnLocationChangedListener;
    private LocationAdapter mAdapter;
    private MarkerOptions mLocationMarkerOptions;

    private PoiItem mSelectPoiItem;

    private boolean mIsSelectLocation;

    private AMapLocation mCurrentLocation;


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_location;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onViewCreated(view, savedInstanceState);
        mMapView = view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);// 此方法必须重写
        initMap();
        startLocation();
        initGPS();

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        getAdapter().onAttachedToRecyclerView(recyclerView);
        recyclerView.setAdapter(getAdapter());
        setViewsOnClickListener(R.id.tvCancel, R.id.tvSend, R.id.llSearch, R.id.tvCancelSearch);
        mIsSelectLocation = false;

        EditText etSearch = view.findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                doSearchQuery(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    public void initGPS() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showOpenGpsDialog();
        }
    }

    private void showOpenGpsDialog() {
        final MyDialogFragment dialogFragment = new MyDialogFragment(R.layout.layout_two_btn_dialog);
        dialogFragment.setOnMyDialogListener(new MyDialogFragment.OnMyDialogListener() {
            @Override
            public void initView(View view) {
                ((TextView) view.findViewById(R.id.tv1)).setText(getString(R.string.chat_tip));
                ((TextView) view.findViewById(R.id.tv2)).setText(getString(R.string.chat_please_open_gps));
                ((TextView) view.findViewById(R.id.btn1)).setText(getString(R.string.chat_cancel));
                ((TextView) view.findViewById(R.id.btn2)).setText(getString(R.string.chat_ok));
                dialogFragment.setDialogViewsOnClickListener(view, R.id.btn1, R.id.btn2);
            }

            @Override
            public void onViewClick(int viewId) {
                if (viewId == R.id.btn2) {
                    if (mLocationClient != null) {
                        mLocationClient.stopLocation();
                        mLocationClient.onDestroy();
                    }
                    mLocationClient = null;
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(intent, 0);
                }
            }
        });
        dialogFragment.show(getChildFragmentManager(), "MyDialogFragment");
    }

    private LocationAdapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = new LocationAdapter();
            mAdapter.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    getAdapter().resetSelected(position);
                    mIsSelectLocation = true;
                    mSelectPoiItem = getAdapter().getItem(position).getPoiItem();
                    mAMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(mSelectPoiItem.getLatLonPoint().getLatitude(), mSelectPoiItem.getLatLonPoint().getLongitude())));
                }
            });
        }
        return mAdapter;
    }

    @Override
    protected void onViewCreated(View view) {

    }

    /**
     * 初始化AMap对象
     */
    private void initMap() {

        if (mAMap == null) {
            mAMap = mMapView.getMap();
            UiSettings settings = mAMap.getUiSettings();
            settings.setZoomControlsEnabled(false);
            mAMap.setLocationSource(this);//设置了定位的监听,这里要实现LocationSource接口
            // 是否显示定位按钮
            settings.setMyLocationButtonEnabled(false);
            mAMap.setMyLocationEnabled(true);
            mAMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);

            MyLocationStyle myLocationStyle = new MyLocationStyle();
            myLocationStyle.strokeColor(Color.TRANSPARENT);
            myLocationStyle.radiusFillColor(Color.TRANSPARENT);
            myLocationStyle.myLocationIcon(BitmapDescriptorFactory
                    .fromResource(R.drawable.chat_gps_d_1));
            mAMap.setMyLocationStyle(myLocationStyle);

            mAMap.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {
                @Override
                public void onCameraChange(CameraPosition cameraPosition) {
                    int x = mMapView.getWidth() / 2;
                    int y = mMapView.getHeight() / 2;
                    Projection projection = mAMap.getProjection();
                    LatLng pt = projection.fromScreenLocation(new Point(x, y));
                    showLocation(pt);
                }

                @Override
                public void onCameraChangeFinish(CameraPosition cameraPosition) {
                    int x = mMapView.getWidth() / 2;
                    int y = mMapView.getHeight() / 2;
                    Projection projection = mAMap.getProjection();
                    LatLng pt = projection.fromScreenLocation(new Point(x, y));
                    showLocation(pt);
                    if (!mIsSelectLocation) {
                        doSearchQuery(pt.latitude, pt.longitude);
                    }
                    mIsSelectLocation = false;
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getView() == null) {
            return;
        }
        if (mLocationClient == null) {
            startLocation();
        } else {
            mLocationClient.startLocation();
        }
        if (mMapView != null) {
            mMapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
        }
        if (mMapView != null) {
            mMapView.onPause();
        }
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMapView != null) {
            mMapView.onSaveInstanceState(outState);
        }
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMapView != null) {
            mMapView.onDestroy();
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();//销毁定位客户端。
        }
    }

    private void startLocation() {
        //初始化定位
        mLocationClient = new AMapLocationClient(getActivity().getApplicationContext());
        //设置定位回调监听，这里要实现AMapLocationListener接口，AMapLocationListener接口只有onLocationChanged方法可以实现，用于接收异步返回的定位结果，参数是AMapLocation类型。
        mLocationClient.setLocationListener(this);
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为Hight_Accuracy高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置是否只定位一次,默认为false
        mLocationOption.setOnceLocation(false);
        //设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setWifiActiveScan(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(false);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(5 * 1000);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
        mStartTime = System.currentTimeMillis();
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                if (System.currentTimeMillis() - mStartTime < 5 * 1000) {
                    //设置缩放级别
                    mAMap.moveCamera(CameraUpdateFactory.zoomTo(mAMap.getMaxZoomLevel() - 3));
                    //将地图移动到定位点
                    mAMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude())));
                    showLocation(new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude()));
                    if (mOnLocationChangedListener != null) {
                        mOnLocationChangedListener.onLocationChanged(aMapLocation);// 显示系统小蓝点
                    }
                }
                mCurrentLocation = aMapLocation;
            } else {
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Log.e("AmapError", "location Error, ErrCode:"
                        + aMapLocation.getErrorCode() + ", errInfo:"
                        + aMapLocation.getErrorInfo());
            }
        }
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mOnLocationChangedListener = onLocationChangedListener;
    }

    @Override
    public void deactivate() {

    }

    private void showLocation(LatLng location) {
        if (mLocationMarkerOptions == null) {
            mLocationMarkerOptions = new MarkerOptions()
                    .position(location)
                    .draggable(true)
                    .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.chat_gps_1)));
            mAMap.addMarker(mLocationMarkerOptions);
        } else {
            mAMap.clear();
            mLocationMarkerOptions.position(location);
            mAMap.addMarker(mLocationMarkerOptions);
        }

    }


    private void doSearchQuery(double lat, double lon) {
        getAdapter().setNewData(new ArrayList<>());
        View view = fv(R.id.tvSend);
        view.setEnabled(false);
        view.setAlpha(0.5f);
        mSelectPoiItem = null;
        GeocodeSearch geocodeSearch = new GeocodeSearch(getActivity());
        LatLonPoint point = new LatLonPoint(lat, lon);
        geocodeSearch.getFromLocationAsyn(new RegeocodeQuery(point, 1000, geocodeSearch.AMAP));
        geocodeSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
            @Override
            public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int rCode) {
                if (getView() == null) {
                    return;
                }
                if (1000 == rCode) {
                    RegeocodeAddress address = regeocodeResult.getRegeocodeAddress();
                    ArrayList<LocationBean> list = new ArrayList<>();
                    List<PoiItem> items = address.getPois();
                    if (items != null && !items.isEmpty()) {
                        LocationBean bean;
                        for (PoiItem item : items) {
                            item.setCityName(address.getDistrict());
                            bean = new LocationBean();
                            bean.setPoiItem(item);
                            list.add(bean);
                        }
                        list.get(0).setCheck(true);
                        mSelectPoiItem = list.get(0).getPoiItem();
                        View view = fv(R.id.tvSend);
                        view.setEnabled(true);
                        view.setAlpha(1f);
                    }
                    getAdapter().setNewData(list);
                }
            }

            @Override
            public void onGeocodeSearched(GeocodeResult geocodeResult, int rCode) {

            }
        });

    }


    private void doSearchQuery(String searchText) {
        if (TextUtils.isEmpty(searchText)) {
            if (mCurrentLocation != null) {
                mIsSelectLocation = true;
                doSearchQuery(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                mAMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude())));
            }
            return;
        }
        getAdapter().setNewData(new ArrayList<>());
        View view = fv(R.id.tvSend);
        view.setEnabled(false);
        view.setAlpha(0.5f);
        mSelectPoiItem = null;
        PoiSearch.Query query = new PoiSearch.Query(searchText, "", mCurrentLocation == null ? "" : mCurrentLocation.getCity());
        query.setPageSize(50);
        query.setPageNum(1);

        //构造 PoiSearch 对象，并设置监听
        PoiSearch poiSearch = new PoiSearch(getActivity(), query);
        poiSearch.setOnPoiSearchListener(new PoiSearch.OnPoiSearchListener() {
            @Override
            public void onPoiSearched(PoiResult poiResult, int rCode) {
                if (1000 == rCode) {
                    ArrayList<PoiItem> items = poiResult.getPois();
                    ArrayList<LocationBean> list = new ArrayList<>();
                    if (items != null && !items.isEmpty()) {
                        LocationBean bean;
                        for (PoiItem item : items) {
                            item.setCityName(item.getCityName());
                            bean = new LocationBean();
                            bean.setPoiItem(item);
                            list.add(bean);
                        }
                        list.get(0).setCheck(true);
                        mSelectPoiItem = list.get(0).getPoiItem();
                        mIsSelectLocation = true;
                        mAMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(mSelectPoiItem.getLatLonPoint().getLatitude(), mSelectPoiItem.getLatLonPoint().getLongitude())));
                        View view = fv(R.id.tvSend);
                        view.setEnabled(true);
                        view.setAlpha(1f);
                    }
                    getAdapter().setNewData(list);
                }
            }

            @Override
            public void onPoiItemSearched(PoiItem poiItem, int i) {

            }
        });
        poiSearch.searchPOIAsyn();

    }

    /**
     * 对地图进行截屏
     */
    private void shotMap() {
        mAMap.getMapScreenShot(new AMap.OnMapScreenShotListener() {
            @Override
            public void onMapScreenShot(Bitmap bitmap) {

            }

            @Override
            public void onMapScreenShot(Bitmap bitmap, int status) {
                if (bitmap != null) {
                    FileBean bean = new FileBean();
                    bean.setType(MessageType.TYPE_LOCATION);
                    int startY = (int) (bitmap.getHeight() * 0.1f + 0.5f);
                    Bitmap newBmp = Bitmap.createBitmap(bitmap, 0, startY, bitmap.getWidth(), (int) (bitmap.getHeight() * 0.75f + 0.5f));
                    bitmap.recycle();
                    File file = new File(Utils.saveJpeg(newBmp, getActivity()));
                    newBmp.recycle();
                    HashMap<String, String> map = new HashMap<>();
                    map.put("fileName", file.getName());
                    map.put("width", String.valueOf(bitmap.getWidth()));
                    map.put("height", String.valueOf(bitmap.getHeight()));
                    map.put("fileSize", String.valueOf(file.length()));
                    map.put("title", mSelectPoiItem.getTitle());
                    map.put("address", mSelectPoiItem.getCityName() + mSelectPoiItem.getAdName() + mSelectPoiItem.getSnippet());
                    map.put("lat", String.valueOf(mSelectPoiItem.getLatLonPoint().getLatitude()));
                    map.put("lon", String.valueOf(mSelectPoiItem.getLatLonPoint().getLongitude()));
                    bean.setFile(file);
                    bean.setExtra(map);
                    EventBus.getDefault().post(bean);
                    finish();
                }
            }
        });
    }


    @Override
    public void updateUIText() {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tvCancel) {
            finish();
        } else if (id == R.id.tvSend) {
            if (mSelectPoiItem != null) {
                shotMap();
            }
        } else if (id == R.id.llSearch) {
            setViewGone(R.id.llSearch);
            setViewVisible(R.id.llSearch2);
            EditText etSearch = fv(R.id.etSearch);
            etSearch.setFocusable(true);
            etSearch.setFocusableInTouchMode(true);
            etSearch.requestFocus();
            showKeyBoard(etSearch);
        } else if (id == R.id.tvCancelSearch) {
            setViewVisible(R.id.llSearch);
            setViewGone(R.id.llSearch2);
            setText(R.id.etSearch, "");
            hideKeyBoard(fv(R.id.etSearch));
            if (mCurrentLocation != null) {
                mIsSelectLocation = true;
                doSearchQuery(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            }
        }
    }

}
