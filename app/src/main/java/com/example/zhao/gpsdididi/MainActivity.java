package com.example.zhao.gpsdididi;

import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap.OnMapLongClickListener;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.AMap;
import com.amap.api.location.AMapLocation;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;
import java.text.DecimalFormat;

import static android.content.Context.VIBRATOR_SERVICE;
import static com.example.zhao.gpsdididi.R.id.textView;

public class MainActivity extends AppCompatActivity implements  LocationSource, AMapLocationListener {

    private MapView mapView;
    private AMap aMap;
    private Button button1;
    private Button button2;
    private TextView tv1;
    private TextView tv2;
    private int setalarm = 0;
    private double sLatitude;
    private double sLongitude;
    private double tLatitude;
    private double tLongitude;
    private Vibrator vibrator;
    private boolean vibrated=false;

    private AMapLocationClient mLocationClient = null;//定位发起端
    private AMapLocationClientOption mLocationOption = null;//定位参数
    private OnLocationChangedListener mListener = null;//定位监听器

    //标识，用于判断是否只显示一次定位信息和用户重新定位
    private boolean isFirstLoc = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button2 = (Button) findViewById(R.id.cancel);
        button1 = (Button) findViewById(R.id.setting);
        tv1=(TextView) findViewById(textView);
        tv2=(TextView) findViewById(R.id.textView2);
        button1.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "闹钟设置成功", Toast.LENGTH_SHORT).show();
                setalarm = 1;
            }
        });
        button2.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                setalarm = 0;
                if(vibrated) vibrator.cancel();
                aMap.clear();
                tLatitude=0;
                tLongitude=0;
                tv2.setText(tLatitude + "," + tLongitude);
            }
        });
        //显示地图
        mapView = (MapView) findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        aMap = mapView.getMap();

        //设置显示定位按钮 并且可以点击
        UiSettings settings = aMap.getUiSettings();
        //设置定位监听
        aMap.setLocationSource(this);
        // 显示定位按钮
        settings.setMyLocationButtonEnabled(true);
        // 可触发定位并显示定位层
        aMap.setMyLocationEnabled(true);

        //定位的小图标 默认是蓝点
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.radiusFillColor(android.R.color.transparent);
        myLocationStyle.strokeColor(android.R.color.transparent);
        aMap.setMyLocationStyle(myLocationStyle);

        initLoc();
    }

    //定位
    private void initLoc() {
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(this);
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置是否只定位一次,默认为false
        mLocationOption.setOnceLocation(false);
        //设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setWifiActiveScan(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(false);
        //设置定位间隔,单位毫秒,默认为4000ms
        mLocationOption.setInterval(4000);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
    }

    //定位回调函数
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                //定位成功回调信息，设置相关消息
                amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见官方定位类型表
                sLatitude = amapLocation.getLatitude();//获取纬度
                sLongitude = amapLocation.getLongitude();//获取经度
                amapLocation.getAccuracy();//获取精度信息
                amapLocation.getAddress();//地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
                amapLocation.getCountry();//国家信息
                amapLocation.getProvince();//省信息
                amapLocation.getCity();//城市信息
                amapLocation.getDistrict();//城区信息
                amapLocation.getStreet();//街道信息
                amapLocation.getStreetNum();//街道门牌号信息
                amapLocation.getCityCode();//城市编码
                amapLocation.getAdCode();//地区编码

                tv1.setText(sLatitude+","+sLongitude);

                aMap.setOnMapLongClickListener(new OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(LatLng latLng) {
                        tLatitude = latLng.latitude;
                        tLongitude = latLng.longitude;
                        tv2.setText( tLatitude + "," + tLongitude);
                        aMap.addMarker(new MarkerOptions().position(latLng).title("目的地").visible(true));
                        aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng));
                    }
                });

                if (setalarm == 1) {
                    double d = Distance(sLongitude, sLatitude, tLongitude, tLatitude);
                    DecimalFormat fmt=new DecimalFormat("0.000");
                    String dis=""+fmt.format(d);
                    Toast.makeText(getApplicationContext(),"相距"+dis+"米",Toast.LENGTH_LONG).show();
                    if (d <= 500) {
                        setalarm=0;
                        Toast.makeText(MainActivity.this, "到达目的地附近\n相距"+dis+"米", Toast.LENGTH_SHORT).show();
                        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                        vibrator.vibrate( new long[]{1000,2000,1000,2000,1000} ,-1);
                        vibrated=true;
                    }
                }
                // 如果不设置标志位，此时再拖动地图时，它会不断将地图移动到当前的位置
                if (isFirstLoc) {
                    //设置缩放级别
                    aMap.moveCamera(CameraUpdateFactory.zoomTo(17));
                    //将地图移动到定位点
                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude())));
                    //点击定位按钮 能够将地图的中心移动到定位点
                    mListener.onLocationChanged(amapLocation);
                    //添加图钉
//                    aMap.addMarker(getMarkerOptions(amapLocation));
                    //获取定位信息
                    StringBuffer buffer = new StringBuffer();
                    buffer.append(amapLocation.getCountry() + "" + amapLocation.getProvince() + "" + amapLocation.getCity() + "" + amapLocation.getProvince() + "" + amapLocation.getDistrict() + "" + amapLocation.getStreet() + "" + amapLocation.getStreetNum());
                    Toast.makeText(getApplicationContext(), buffer.toString(), Toast.LENGTH_LONG).show();
                    isFirstLoc = false;
                }

            } else {
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Log.e("AmapError", "location Error, ErrCode:"
                        + amapLocation.getErrorCode() + ", errInfo:"
                        + amapLocation.getErrorInfo());

                Toast.makeText(getApplicationContext(), "定位失败", Toast.LENGTH_LONG).show();
            }
        }
    }
    //算距离
    public static double Distance(double long1, double lat1, double long2, double lat2) {
        double a, b, R;
        R = 6378137;
        lat1 = lat1 * Math.PI / 180.0;
        lat2 = lat2 * Math.PI / 180.0;
        a = lat1 - lat2;
        b = (long1 - long2) * Math.PI / 180.0;
        double d;
        double sa2, sb2;
        sa2 = Math.sin(a / 2.0);
        sb2 = Math.sin(b / 2.0);
        d = 2 * R * Math.asin(Math.sqrt(sa2 * sa2 + Math.cos(lat1) * Math.cos(lat2) * sb2 * sb2));
        return d;
    }

    //激活定位
    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
    }
    //停止定位
    @Override
    public void deactivate() {
        mListener = null;
    }
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}
