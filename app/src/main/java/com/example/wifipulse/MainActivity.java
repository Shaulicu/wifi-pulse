package com.example.wifipulse;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.net.Inet4Address;

public class MainActivity extends Activity {
    private static final int BG = Color.rgb(8, 13, 27), CARD = Color.rgb(20, 29, 49);
    private static final int WHITE = Color.rgb(239, 245, 255), MUTED = Color.rgb(148, 164, 191);
    private static final int AQUA = Color.rgb(77, 232, 204);
    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextView ssid, status, subtitle, signalText, bandText, speedText, ipText, bssidText, footer;
    private SignalView signalView;
    private boolean active;
    private final Runnable tick = new Runnable() { @Override public void run() { if (active) { refresh(); handler.postDelayed(this, 3000); } } };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG); getWindow().setNavigationBarColor(BG);
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true); scroll.setBackgroundColor(BG);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(22), dp(28), dp(22), dp(28));
        scroll.addView(root); setContentView(scroll);
        TextView eyebrow = label("NETWORK MONITOR  /  LIVE", 12, AQUA, true); root.addView(eyebrow);
        TextView heading = label("WiFi Pulse", 36, WHITE, true); LinearLayout.LayoutParams head = params(-1,-2,9,0); heading.setLayoutParams(head); root.addView(heading);
        TextView intro = label("Your connection, at a glance.", 15, MUTED, false); root.addView(intro);
        LinearLayout main = new LinearLayout(this); main.setOrientation(LinearLayout.VERTICAL); main.setGravity(Gravity.CENTER_HORIZONTAL); main.setPadding(dp(20), dp(24), dp(20), dp(24)); main.setBackground(panel(CARD, 28));
        main.setLayoutParams(params(-1, -2, 26, 0)); root.addView(main);
        status = label("CHECKING CONNECTION", 11, AQUA, true); status.setGravity(Gravity.CENTER); main.addView(status);
        signalView = new SignalView(this); LinearLayout.LayoutParams circle = new LinearLayout.LayoutParams(dp(220), dp(220)); circle.topMargin=dp(12); circle.bottomMargin=dp(6); main.addView(signalView, circle);
        ssid = label("Scanning…", 25, WHITE, true); ssid.setGravity(Gravity.CENTER); main.addView(ssid);
        subtitle = label("Reading your Wi-Fi", 13, MUTED, false); subtitle.setGravity(Gravity.CENTER); subtitle.setLayoutParams(params(-1,-2,7,0)); main.addView(subtitle);
        TextView section = label("CONNECTION DETAILS", 12, MUTED, true); section.setLayoutParams(params(-1,-2,26,12)); root.addView(section);
        LinearLayout first = new LinearLayout(this); first.setOrientation(LinearLayout.HORIZONTAL); root.addView(first);
        signalText = metric(first,"SIGNAL","—"); bandText = metric(first,"FREQUENCY","—");
        LinearLayout second = new LinearLayout(this); second.setOrientation(LinearLayout.HORIZONTAL); second.setLayoutParams(params(-1,-2,12,0)); root.addView(second);
        speedText=metric(second,"LINK SPEED","—"); ipText=metric(second,"IPv4 ADDRESS","—");
        LinearLayout last = new LinearLayout(this); last.setPadding(dp(16),dp(16),dp(16),dp(16)); last.setOrientation(LinearLayout.VERTICAL); last.setBackground(panel(CARD,20)); last.setLayoutParams(params(-1,-2,12,0)); root.addView(last);
        last.addView(label("ACCESS POINT / BSSID", 11, MUTED, true)); bssidText=label("—",17,WHITE,true); bssidText.setLayoutParams(params(-1,-2,8,0)); last.addView(bssidText);
        Button refresh = new Button(this); refresh.setText("REFRESH CONNECTION ↻"); refresh.setAllCaps(false); refresh.setTextColor(BG); refresh.setTextSize(14); refresh.setTypeface(Typeface.DEFAULT,Typeface.BOLD); refresh.setBackground(panel(AQUA,17)); refresh.setLayoutParams(params(-1,dp(54),24,0)); refresh.setOnClickListener(v -> { requestPermissionIfNeeded(); refresh(); }); root.addView(refresh);
        footer=label("Updates automatically every 3 seconds · Local only",12,MUTED,false); footer.setGravity(Gravity.CENTER); footer.setLayoutParams(params(-1,-2,18,0)); root.addView(footer);
        requestPermissionIfNeeded();
    }
    private TextView metric(LinearLayout row,String caption,String initial) {
        LinearLayout cell = new LinearLayout(this); cell.setOrientation(LinearLayout.VERTICAL); cell.setPadding(dp(15),dp(18),dp(8),dp(18)); cell.setBackground(panel(CARD,20));
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0,dp(102),1); cp.rightMargin=dp(8); row.addView(cell,cp);
        cell.addView(label(caption,10,MUTED,true)); TextView value=label(initial,20,WHITE,true); value.setSingleLine(true); value.setTextSize(19); value.setLayoutParams(params(-1,-2,10,0)); cell.addView(value); return value;
    }
    private LinearLayout.LayoutParams params(int width,int height,int top,int bottom){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(width,height);p.topMargin=dp(top);p.bottomMargin=dp(bottom);return p;}
    private int dp(float d){return (int)(getResources().getDisplayMetrics().density*d+0.5f);}
    private TextView label(String content,int sp,int color,boolean bold){TextView t=new TextView(this); t.setText(content); t.setTextSize(sp); t.setTextColor(color); if(bold)t.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));return t;}
    private GradientDrawable panel(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    @Override protected void onResume(){super.onResume();active=true;handler.removeCallbacks(tick);handler.post(tick);}
    @Override protected void onPause(){active=false;handler.removeCallbacks(tick);super.onPause();}
    private void requestPermissionIfNeeded(){if(Build.VERSION.SDK_INT>=23 && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},10);}
    @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grants){super.onRequestPermissionsResult(requestCode,permissions,grants);refresh();}
    @SuppressWarnings("deprecation") private void refresh(){
        ConnectivityManager cm=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        WifiManager wm=(WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        Network wifiNetwork=null; NetworkCapabilities cap=null;
        if(cm!=null) for(Network n:cm.getAllNetworks()){NetworkCapabilities c=cm.getNetworkCapabilities(n);if(c!=null&&c.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)){wifiNetwork=n;cap=c;break;}}
        if(wifiNetwork==null){ssid.setText("Not connected");status.setText("●  OFFLINE");subtitle.setText("Connect to a Wi-Fi network to see its details");signalView.setStrength(-1);signalText.setText("—");bandText.setText("—");speedText.setText("—");ipText.setText("—");bssidText.setText("—");return;}
        WifiInfo info=null;
        if(Build.VERSION.SDK_INT>=29 && cap!=null && cap.getTransportInfo() instanceof WifiInfo)info=(WifiInfo)cap.getTransportInfo();
        if(info==null && wm!=null)info=wm.getConnectionInfo();
        String name=info!=null?info.getSSID():null;
        boolean locationAllowed=checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED;
        boolean locationEnabled=Build.VERSION.SDK_INT<28 || ((android.location.LocationManager)getSystemService(Context.LOCATION_SERVICE)).isLocationEnabled();
        if(name!=null&&name.length()>=2&&name.startsWith("\"")&&name.endsWith("\""))name=name.substring(1,name.length()-1);
        if(name==null||name.isEmpty()||name.equalsIgnoreCase("<unknown ssid>")){ssid.setText("Wi-Fi connected");subtitle.setText(!locationAllowed?"Allow location permission to show network name":!locationEnabled?"Enable Location in Android settings to show SSID":"Network name unavailable on this device");}
        else {ssid.setText(name);subtitle.setText("Connected to your wireless network");}
        status.setText("●  CONNECTED");
        int rssi=info!=null?info.getRssi():-127;
        boolean valid=rssi>-127 && rssi<=0;
        int level=valid?WifiManager.calculateSignalLevel(rssi,5):-1;
        signalView.setStrength(level);
        signalText.setText(valid?(rssi+" dBm"):"—");
        int f=info!=null?info.getFrequency():0;
        bandText.setText(f>0?(f+" MHz"):"—");
        int mbps=info!=null?info.getLinkSpeed():-1;
        speedText.setText(mbps>=0?(mbps+" Mbps"):"—");
        String bssid=info!=null?info.getBSSID():null;
        bssidText.setText(bssid!=null&&!bssid.equals("02:00:00:00:00:00")?bssid:"Not available");
        String ip="—"; LinkProperties lp=cm.getLinkProperties(wifiNetwork);
        if(lp!=null)for(LinkAddress la:lp.getLinkAddresses())if(la.getAddress() instanceof Inet4Address){ip=la.getAddress().getHostAddress();break;}
        ipText.setText(ip);
    }
    private class SignalView extends View{
        Paint p=new Paint(3); int level=-1;
        SignalView(Context c){super(c);}
        void setStrength(int x){level=x;invalidate();}
        @Override protected void onDraw(Canvas c){super.onDraw(c);float cx=getWidth()/2f,cy=getHeight()/2f;float scale=getResources().getDisplayMetrics().density;
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.1f*scale);
            for(int i=0;i<3;i++){p.setColor(Color.argb(30+i*15,77,232,204));float radius=(65+i*17)*scale;c.drawCircle(cx,cy,radius,p);}
            p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(16,54,65));c.drawCircle(cx,cy,58*scale,p);
            p.setColor(AQUA);p.setStrokeCap(Paint.Cap.ROUND);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(8*scale);
            RectF r=new RectF(cx-34*scale,cy-32*scale,cx+34*scale,cy+36*scale);
            for(int i=0;i<3;i++){p.setColor(level>=i+1?AQUA:Color.rgb(61,97,106));float inset=i*12*scale;c.drawArc(new RectF(r.left+inset,r.top+inset,r.right-inset,r.bottom-inset),222,96,false,p);}
            p.setStyle(Paint.Style.FILL);p.setColor(level>=0?AQUA:MUTED);c.drawCircle(cx,cy+24*scale,5.5f*scale,p);
        }
    }
}
