package com.heavyy.projectx;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.os.*;
import android.provider.Settings;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.*;

/**
 * HEAVYy PROJECT X gaming overlay.
 * Uses public Android overlay APIs only; it does not inject or modify game processes.
 */
public class OverlayService extends Service {
    static final String CHANNEL="heavyy_overlay";
    final Handler h=new Handler(Looper.getMainLooper());
    WindowManager wm;
    FrameLayout controlLayer, crosshairLayer;
    WindowManager.LayoutParams controlLp, monitorLp;
    TextView monitorHud;
    CrosshairView crosshair;
    long lastTotal=-1,lastIdle=-1;
    float downX,downY;
    boolean panelOpen=false;
    final LinkedHashSet<String> monitorKeys=new LinkedHashSet<>();
    final HashMap<String,TextView> monitorValueViews=new HashMap<>();

    @Override public void onCreate(){
        super.onCreate();
        createChannel();
        startForeground(77,notification());
        loadDefaults();
        showOverlay();
    }

    void loadDefaults(){
        SharedPreferences p=getSharedPreferences("hpx",MODE_PRIVATE);
        if(p.getBoolean("overlay_fps",true)) monitorKeys.add("FPS");
        if(p.getBoolean("overlay_battery",true)) monitorKeys.add("BATTERY");
        if(p.getBoolean("overlay_cpu",true)) monitorKeys.add("CPU");
        if(p.getBoolean("overlay_ram",true)) monitorKeys.add("RAM");
    }

    Notification notification(){
        Intent stop=new Intent(this,OverlayService.class);stop.setAction("STOP");
        PendingIntent pi=PendingIntent.getService(this,0,stop,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        return new Notification.Builder(this,CHANNEL)
            .setContentTitle("HEAVYy PROJECT X")
            .setContentText("Gaming Overlay aktif")
            .setSmallIcon(R.drawable.heavyy_icon)
            .setOngoing(true)
            .addAction(new Notification.Action.Builder(null,"Hentikan",pi).build())
            .build();
    }

    void createChannel(){
        if(Build.VERSION.SDK_INT>=26){
            NotificationManager nm=getSystemService(NotificationManager.class);
            nm.createNotificationChannel(new NotificationChannel(CHANNEL,"HEAVYy Gaming Overlay",NotificationManager.IMPORTANCE_LOW));
        }
    }

    int overlayType(){return Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_PHONE;}
    int dp(float x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}

    GradientDrawable bg(int fill,int stroke,int radius){
        GradientDrawable g=new GradientDrawable();g.setColor(fill);g.setCornerRadius(dp(radius));
        if(stroke!=0)g.setStroke(dp(1),stroke);return g;
    }
    TextView label(String text,float size){
        TextView t=new TextView(this);t.setText(text);t.setTextColor(Color.WHITE);t.setTextSize(size);t.setPadding(dp(4),dp(3),dp(4),dp(3));return t;
    }
    Button action(String text){
        Button b=new Button(this);b.setText(text);b.setTextColor(Color.WHITE);b.setTextSize(10);b.setAllCaps(false);b.setTypeface(null,Typeface.BOLD);
        b.setPadding(dp(6),0,dp(6),0);b.setBackground(bg(0x241B1F2B,0x4A5E5CEB,14));
        return b;
    }

    void showOverlay(){
        if(Build.VERSION.SDK_INT>=23 && !Settings.canDrawOverlays(this)){stopSelf();return;}
        wm=(WindowManager)getSystemService(WINDOW_SERVICE);
        showCrosshair();
        showControlWindow();
        showMonitorHud();
        h.post(tick);
    }

    void showCrosshair(){
        crosshairLayer=new FrameLayout(this);
        crosshair=new CrosshairView(this);
        crosshairLayer.addView(crosshair,new FrameLayout.LayoutParams(dp(180),dp(180),Gravity.CENTER));
        int flags=WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE|WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        WindowManager.LayoutParams lp=new WindowManager.LayoutParams(-1,-1,overlayType(),flags,PixelFormat.TRANSLUCENT);
        lp.gravity=Gravity.TOP|Gravity.START;
        wm.addView(crosshairLayer,lp);
    }

    void showControlWindow(){
        controlLayer=new FrameLayout(this);
        controlLp=new WindowManager.LayoutParams(dp(34),-1,overlayType(),WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,PixelFormat.TRANSLUCENT);
        controlLp.gravity=Gravity.TOP|Gravity.START;controlLp.x=0;controlLp.y=0;
        wm.addView(controlLayer,controlLp);
        buildHandle();
    }

    void buildHandle(){
        controlLayer.removeAllViews();
        TextView handle=label("›",25);handle.setGravity(Gravity.CENTER);handle.setTextColor(0xFFE8E8F0);handle.setBackground(bg(0xCC121522,0x5567E8F9,18));
        FrameLayout.LayoutParams hp=new FrameLayout.LayoutParams(dp(30),dp(82),Gravity.CENTER_VERTICAL|Gravity.START);hp.leftMargin=dp(2);
        controlLayer.addView(handle,hp);
        handle.setOnTouchListener(new View.OnTouchListener(){
            float sx;
            @Override public boolean onTouch(View v,MotionEvent e){
                if(e.getAction()==MotionEvent.ACTION_DOWN){sx=e.getRawX();return true;}
                if(e.getAction()==MotionEvent.ACTION_UP && e.getRawX()-sx>dp(40)){openPanel();return true;}
                return true;
            }
        });
    }

    void openPanel(){
        if(panelOpen)return;panelOpen=true;
        controlLp.width=dp(350);
        wm.updateViewLayout(controlLayer,controlLp);
        buildPanel();
    }

    void closePanel(){
        if(!panelOpen)return;panelOpen=false;buildHandle();controlLp.width=dp(34);wm.updateViewLayout(controlLayer,controlLp);
    }

    void buildPanel(){
        controlLayer.removeAllViews();
        LinearLayout panel=new LinearLayout(this);panel.setOrientation(LinearLayout.VERTICAL);panel.setPadding(dp(14),dp(16),dp(14),dp(16));panel.setBackground(bg(0xEE0A0D16,0x665E5CEB,24));
        FrameLayout.LayoutParams pp=new FrameLayout.LayoutParams(dp(340),-1,Gravity.START);pp.leftMargin=dp(6);controlLayer.addView(panel,pp);

        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);
        ImageView icon=new ImageView(this);icon.setImageResource(R.drawable.heavyy_icon);icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);head.addView(icon,new LinearLayout.LayoutParams(dp(42),dp(42)));
        LinearLayout ht=new LinearLayout(this);ht.setOrientation(LinearLayout.VERTICAL);ht.setPadding(dp(8),0,0,0);
        TextView title=label("HEAVYy PROJECT X",18);title.setTypeface(null,Typeface.BOLD);ht.addView(title);
        TextView sub=label("GAME OVERLAY  •  LIVE CONTROL",9);sub.setTextColor(0xFF67E8F9);ht.addView(sub);head.addView(ht,new LinearLayout.LayoutParams(0,dp(50),1));
        Button close=action("✕");head.addView(close,new LinearLayout.LayoutParams(dp(48),dp(44)));close.setOnClickListener(v->closePanel());panel.addView(head);

        TextView modeTitle=label("HPX PERFORMANCE MODES",11);modeTitle.setTypeface(null,Typeface.BOLD);modeTitle.setTextColor(0xFF67E8F9);panel.addView(modeTitle);
        LinearLayout modes=new LinearLayout(this);modes.setOrientation(LinearLayout.HORIZONTAL);
        Button extreme=action("⚡\nHPX EKSTREM");Button balance=action("⚖\nHPX BALANCE");Button battery=action("🔋\nHPX BATTERY");
        modes.addView(extreme,new LinearLayout.LayoutParams(0,dp(62),1));Space sp1=new Space(this);modes.addView(sp1,new LinearLayout.LayoutParams(dp(5),1));modes.addView(balance,new LinearLayout.LayoutParams(0,dp(62),1));Space sp2=new Space(this);modes.addView(sp2,new LinearLayout.LayoutParams(dp(5),1));modes.addView(battery,new LinearLayout.LayoutParams(0,dp(62),1));panel.addView(modes);
        extreme.setOnClickListener(v->setMode("HPX EKSTREM"));balance.setOnClickListener(v->setMode("HPX BALANCE"));battery.setOnClickListener(v->setMode("HPX BATTERY"));

        TextView mt=label("REAL-TIME MONITORING  •  tekan + untuk tampilkan HUD",11);mt.setTypeface(null,Typeface.BOLD);mt.setTextColor(0xFF67E8F9);panel.addView(mt);
        addMonitorRow(panel,"FPS","FPS");addMonitorRow(panel,"BATTERY","BATTERY");addMonitorRow(panel,"CPU","CPU");addMonitorRow(panel,"GPU","GPU");addMonitorRow(panel,"RAM","RAM");addMonitorRow(panel,"TEMP","TEMP");addMonitorRow(panel,"DISPLAY","DISPLAY");addMonitorRow(panel,"TIME","TIME");

        TextView actionsTitle=label("QUICK TOOLS",11);actionsTitle.setTypeface(null,Typeface.BOLD);actionsTitle.setTextColor(0xFF67E8F9);panel.addView(actionsTitle);
        LinearLayout colors=new LinearLayout(this);colors.setOrientation(LinearLayout.HORIZONTAL);
        int[] cs={0xFF67E8F9,0xFF22D3A5,0xFFFF4D6D,0xFFA78BFA,0xFFFFB84D,0xFFFFFF66};
        for(int col:cs){TextView cb=label("●",22);cb.setGravity(Gravity.CENTER);cb.setTextColor(col);cb.setBackground(bg(0x10FFFFFF,0x204B4F63,12));colors.addView(cb,new LinearLayout.LayoutParams(0,dp(40),1));cb.setOnClickListener(v->{getSharedPreferences("hpx",MODE_PRIVATE).edit().putInt("overlay_color",col).apply();if(crosshair!=null)crosshair.setColor(col);});}
        panel.addView(colors);
        LinearLayout tools=new LinearLayout(this);tools.setOrientation(LinearLayout.VERTICAL);
        addToolRow(tools,"VOLUME","−","+");addToolRow(tools,"BRIGHTNESS","DIM","UP");
        LinearLayout media=new LinearLayout(this);media.setOrientation(LinearLayout.HORIZONTAL);Button shot=action("▣ SCREENSHOT"),rec=action("● RECORD"),cache=action("⌫ CACHE");media.addView(shot,new LinearLayout.LayoutParams(0,dp(46),1));media.addView(rec,new LinearLayout.LayoutParams(0,dp(46),1));media.addView(cache,new LinearLayout.LayoutParams(0,dp(46),1));tools.addView(media);panel.addView(tools);
        shot.setOnClickListener(v->toast("Screenshot membutuhkan MediaProjection/izin tangkap layar Android."));rec.setOnClickListener(v->toast("Screen Record membutuhkan izin MediaProjection Android."));cache.setOnClickListener(v->toast("Clear Cache membuka App Info; Android tetap meminta konfirmasi pengguna."));

        TextView ftitle=label("V1 FEATURES",11);ftitle.setTypeface(null,Typeface.BOLD);ftitle.setTextColor(0xFF67E8F9);panel.addView(ftitle);
        Button drag=action("DRAG HS V4  •  DEVELOPMENT");Button grafik=action("GRAFIK MC  •  DEVELOPMENT");panel.addView(drag,new LinearLayout.LayoutParams(-1,dp(46)));panel.addView(grafik,new LinearLayout.LayoutParams(-1,dp(46)));
        drag.setOnClickListener(v->devDialog());grafik.setOnClickListener(v->devDialog());

        head.setOnTouchListener(new View.OnTouchListener(){float sx;public boolean onTouch(View v,MotionEvent e){if(e.getAction()==MotionEvent.ACTION_DOWN){sx=e.getRawX();return true;}if(e.getAction()==MotionEvent.ACTION_UP&&sx-e.getRawX()>dp(45)){closePanel();return true;}return false;}});
    }

    void addMonitorRow(LinearLayout parent,String title,String key){
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(0,dp(1),0,dp(1));
        TextView name=label("＋  "+title,12);name.setTypeface(null,Typeface.BOLD);row.addView(name,new LinearLayout.LayoutParams(dp(110),dp(34)));
        TextView value=label(valueFor(key),11);value.setTextColor(0xFFCBD5E1);monitorValueViews.put(key,value);row.addView(value,new LinearLayout.LayoutParams(0,dp(34),1));
        Button add=action(monitorKeys.contains(key)?"✓":"+");row.addView(add,new LinearLayout.LayoutParams(dp(48),dp(34)));add.setOnClickListener(v->{if(monitorKeys.contains(key)){monitorKeys.remove(key);add.setText("+");}else{monitorKeys.add(key);add.setText("✓");}updateHud();});
        parent.addView(row);
    }

    void addToolRow(LinearLayout parent,String title,String left,String right){
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);TextView name=label(title,11);name.setTypeface(null,Typeface.BOLD);row.addView(name,new LinearLayout.LayoutParams(0,dp(44),1));
        Button a=action(left),b=action(right);row.addView(a,new LinearLayout.LayoutParams(dp(58),dp(42)));Space s=new Space(this);row.addView(s,new LinearLayout.LayoutParams(dp(5),1));row.addView(b,new LinearLayout.LayoutParams(dp(58),dp(42)));parent.addView(row);
        if(title.equals("VOLUME")){a.setOnClickListener(v->volume(-1));b.setOnClickListener(v->volume(1));}
        else{a.setOnClickListener(v->brightness(-10));b.setOnClickListener(v->brightness(10));}
    }

    void showMonitorHud(){
        monitorHud=label("",11);monitorHud.setTypeface(null,Typeface.BOLD);monitorHud.setTextColor(Color.WHITE);monitorHud.setPadding(dp(10),dp(7),dp(10),dp(7));monitorHud.setBackground(bg(0xD90B0F18,0x7767E8F9,12));
        monitorLp=new WindowManager.LayoutParams(-2,-2,overlayType(),WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,PixelFormat.TRANSLUCENT);monitorLp.gravity=Gravity.TOP|Gravity.START;monitorLp.x=dp(18);monitorLp.y=dp(120);
        wm.addView(monitorHud,monitorLp);
        monitorHud.setOnTouchListener(new View.OnTouchListener(){float sx,sy;int ox,oy;public boolean onTouch(View v,MotionEvent e){if(e.getAction()==MotionEvent.ACTION_DOWN){sx=e.getRawX();sy=e.getRawY();ox=monitorLp.x;oy=monitorLp.y;return true;}if(e.getAction()==MotionEvent.ACTION_MOVE){monitorLp.x=ox+(int)(e.getRawX()-sx);monitorLp.y=oy+(int)(e.getRawY()-sy);wm.updateViewLayout(monitorHud,monitorLp);return true;}return true;}});
    }

    void updateHud(){
        if(monitorHud==null)return;StringBuilder s=new StringBuilder();for(String k:monitorKeys){String value=valueFor(k);if(monitorValueViews.containsKey(k))monitorValueViews.get(k).setText(value);if(s.length()>0)s.append("\n");s.append(k).append("  ").append(value);}monitorHud.setText(s.toString());monitorHud.setVisibility(monitorKeys.isEmpty()?View.GONE:View.VISIBLE);
    }

    Runnable tick=()->{if(monitorHud==null)return;updateHud();if(crosshair!=null){SharedPreferences p=getSharedPreferences("hpx",MODE_PRIVATE);crosshair.setVisibility(p.getBoolean("overlay_crosshair",true)?View.VISIBLE:View.GONE);crosshair.setColor(p.getInt("overlay_color",0xFF20D7A0));}h.postDelayed(tick,1000);};

    String valueFor(String key){
        switch(key){
            case "CPU":return cpuUsage()+"%";
            case "RAM":return ram();
            case "BATTERY":return batteryLevel()+"%";
            case "TEMP":return batteryTemp();
            case "GPU":return gpuUsage();
            case "DISPLAY":return refreshRate()+"Hz";
            case "TIME":return new java.text.SimpleDateFormat("HH:mm:ss",Locale.getDefault()).format(new Date());
            case "FPS":return "--";
            default:return "--";
        }
    }

    String cpuUsage(){try{BufferedReader br=new BufferedReader(new FileReader("/proc/stat"));String line=br.readLine();br.close();if(line==null||!line.startsWith("cpu "))return "--";String[] a=line.trim().split("\\s+");long idle=Long.parseLong(a[4]);long total=0;for(int i=1;i<a.length;i++)total+=Long.parseLong(a[i]);if(lastTotal<0){lastTotal=total;lastIdle=idle;return "--";}long dt=total-lastTotal,di=idle-lastIdle;lastTotal=total;lastIdle=idle;return String.valueOf(dt<=0?0:Math.round((1f-(float)di/dt)*100f));}catch(Exception e){return "--";}}
    String ram(){ActivityManager.MemoryInfo mi=new ActivityManager.MemoryInfo();((ActivityManager)getSystemService(ACTIVITY_SERVICE)).getMemoryInfo(mi);double u=(mi.totalMem-mi.availMem)/1073741824.0;return String.format(Locale.US,"%.1fG",u);}
    String batteryLevel(){Intent i=registerReceiver(null,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));if(i==null)return "--";int l=i.getIntExtra("level",-1),m=i.getIntExtra("scale",100);return l<0?"--":String.valueOf(Math.round(l*100f/m));}
    String batteryTemp(){Intent i=registerReceiver(null,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));if(i==null)return "--";int t=i.getIntExtra("temperature",-1);return t<0?"--":String.format(Locale.US,"%.1f°C",t/10f);}
    String refreshRate(){try{return String.valueOf(Math.round(((WindowManager)getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getRefreshRate()));}catch(Exception e){return "--";}}

    String gpuUsage(){
        String[] paths={"/sys/class/kgsl/kgsl-3d0/gpubusy","/sys/class/misc/mali0/device/utilization","/sys/devices/platform/gpu.0/gpu_busy_percent"};
        for(String path:paths){try{BufferedReader br=new BufferedReader(new FileReader(path));String s=br.readLine();br.close();if(s==null)continue;s=s.trim();if(path.endsWith("gpubusy")){String[] a=s.split("\\s+");if(a.length>=2){long busy=Long.parseLong(a[0]),total=Long.parseLong(a[1]);if(total>0)return String.valueOf(Math.max(0,Math.min(100,Math.round(busy*100f/total))))+"%";}}else{String digits=s.replaceAll("[^0-9]","");if(!digits.isEmpty())return String.valueOf(Math.max(0,Math.min(100,Integer.parseInt(digits))))+"%";}}catch(Exception ignored){}}
        return "--";
    }

    void volume(int delta){AudioManager am=(AudioManager)getSystemService(AUDIO_SERVICE);int cur=am.getStreamVolume(AudioManager.STREAM_MUSIC),max=am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);am.setStreamVolume(AudioManager.STREAM_MUSIC,Math.max(0,Math.min(max,cur+delta)),0);}
    void brightness(int delta){if(Build.VERSION.SDK_INT>=23&&!Settings.System.canWrite(this)){toast("Izinkan Modify System Settings untuk kontrol brightness.");try{Intent i=new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,Uri.parse("package:"+getPackageName()));i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);startActivity(i);}catch(Exception ignored){}return;}try{int cur=Settings.System.getInt(getContentResolver(),Settings.System.SCREEN_BRIGHTNESS,128);Settings.System.putInt(getContentResolver(),Settings.System.SCREEN_BRIGHTNESS,Math.max(10,Math.min(255,cur+delta*5)));}catch(Exception e){toast("Brightness tidak dapat diubah pada perangkat ini.");}}
    void setMode(String mode){getSharedPreferences("hpx",MODE_PRIVATE).edit().putString("hpx_mode",mode).apply();toast(mode+" aktif");}
    void devDialog(){new AlertDialog.Builder(this).setTitle("HEAVYy PROJECT X").setMessage("FITUR INI DALAM MASA PENGEMBANGAN\n\nFitur akan tersedia pada update berikutnya.").setPositiveButton("OK",null).show();}
    void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}

    @Override public int onStartCommand(Intent intent,int flags,int startId){if(intent!=null&&"STOP".equals(intent.getAction())){stopSelf();return START_NOT_STICKY;}return START_STICKY;}
    @Override public void onDestroy(){h.removeCallbacksAndMessages(null);if(wm!=null){try{if(monitorHud!=null)wm.removeView(monitorHud);}catch(Exception ignored){}try{if(controlLayer!=null)wm.removeView(controlLayer);}catch(Exception ignored){}try{if(crosshairLayer!=null)wm.removeView(crosshairLayer);}catch(Exception ignored){}}monitorHud=null;controlLayer=null;crosshairLayer=null;super.onDestroy();}
    @Override public IBinder onBind(Intent i){return null;}

    class CrosshairView extends View{
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);int color=0xFF20D7A0;CrosshairView(Context c){super(c);p.setStrokeWidth(dp(3));p.setStyle(Paint.Style.STROKE);setBackgroundColor(Color.TRANSPARENT);}void setColor(int c){if(color!=c){color=c;invalidate();}}
        @Override protected void onDraw(Canvas c){super.onDraw(c);float cx=getWidth()/2f,cy=getHeight()/2f,r=dp(45);p.setColor(color);p.setStrokeWidth(dp(2));p.setStyle(Paint.Style.STROKE);c.drawCircle(cx,cy,r,p);c.drawLine(cx-r-dp(12),cy,cx-r+dp(5),cy,p);c.drawLine(cx+r-dp(5),cy,cx+r+dp(12),cy,p);c.drawLine(cx,cy-r-dp(12),cx,cy-r+dp(5),p);c.drawLine(cx,cy+r-dp(5),cx,cy+r+dp(12),p);p.setStyle(Paint.Style.FILL);c.drawCircle(cx,cy,dp(3),p);}
    }
}
