package com.heavyy.projectx;

import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.net.Uri;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.util.*;
import java.text.DecimalFormat;

public class MainActivity extends Activity {
    LinearLayout root, content;
    TextView roleText, monitorText, accountText, powerText;
    LinearLayout accountList;
    AnimatedBackground animatedBackground;
    Handler handler = new Handler(Looper.getMainLooper());
    SharedPreferences prefs;

    final String DEV_USER = "DEVELOPER HPX KYY_1";
    final String DEV_PASS = "HPX ADMIN";
    final int MAX_POWER = 700;
    final long POWER_INTERVAL = 20000L;
    final long RUN_FREE_DURATION = 60 * 60 * 1000L;
    final int CURRENT_VERSION_CODE = 7;
    final String UPDATE_CONFIG_URL = "https://raw.githubusercontent.com/groo65599-cyber/HEAVYy-PROJECT-X-VPRIME/main/update.json";
    final String UPDATE_CHANNEL_URL = "https://whatsapp.com/channel/0029Vb8jf279MF9APjV0cq25";

    int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}

    TextView tv(String s,float size){
        TextView t=new TextView(this); t.setText(s); t.setTextColor(Color.WHITE); t.setTextSize(size);
        t.setPadding(dp(2),dp(2),dp(2),dp(2)); return t;
    }

    Button btn(String text){
        Button b=new Button(this); b.setText(text); b.setTextColor(Color.WHITE); b.setTextSize(12); b.setTypeface(null,Typeface.BOLD); b.setAllCaps(false); b.setGravity(Gravity.CENTER);
        b.setPadding(dp(10),0,dp(10),0); b.setBackground(round(0x1CFFFFFF,0x385E5CEB,16));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(48)); p.setMargins(0,dp(5),0,dp(5)); b.setLayoutParams(p);
        return b;
    }

    GradientDrawable round(int fill,int stroke,int radius){GradientDrawable g=new GradientDrawable();g.setColor(fill);g.setCornerRadius(dp(radius));if(stroke!=0)g.setStroke(dp(1),stroke);return g;}

    TextView cardTitle(String s){TextView t=tv(s,18);t.setTypeface(null,1);return t;}

    LinearLayout card(){
        LinearLayout c=new LinearLayout(this); c.setOrientation(LinearLayout.VERTICAL); c.setPadding(dp(18),dp(16),dp(18),dp(16));
        c.setBackground(round(0x161B1F2B,0x253D4354,22));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.setMargins(0,0,0,dp(12)); c.setLayoutParams(p); return c;
    }

    @Override public void onCreate(Bundle b){super.onCreate(b);prefs=getSharedPreferences("hpx",MODE_PRIVATE);checkForUpdate();showLoadingSequence();}

    /**
     * Reference-based opening sequence supplied by the user. The stills are shown
     * as a short cinematic frame sequence before the Username/Password screen.
     */
    void showLoadingSequence(){
        FrameLayout frame=new FrameLayout(this);
        frame.setBackgroundColor(Color.BLACK);

        ImageView hero=new ImageView(this);
        hero.setScaleType(ImageView.ScaleType.CENTER_CROP);
        hero.setBackgroundColor(Color.BLACK);
        FrameLayout.LayoutParams hp=new FrameLayout.LayoutParams(-1,dp(230));
        hp.gravity=Gravity.CENTER;
        frame.addView(hero,hp);

        LinearLayout overlay=new LinearLayout(this);
        overlay.setOrientation(LinearLayout.VERTICAL);
        overlay.setGravity(Gravity.CENTER_HORIZONTAL);
        FrameLayout.LayoutParams op=new FrameLayout.LayoutParams(-1,-2);
        op.gravity=Gravity.BOTTOM;
        op.setMargins(dp(28),0,dp(28),dp(42));
        frame.addView(overlay,op);

        TextView brand=tv("HEAVYy PROJECT X",18);
        brand.setTypeface(null,1);
        brand.setGravity(Gravity.CENTER);
        TextView status=tv("INITIALIZING...",11);
        status.setTextColor(0xFFBEBEBE);
        status.setGravity(Gravity.CENTER);
        ProgressBar bar=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
        bar.setMax(100);
        bar.setProgress(0);
        LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,dp(3));
        bp.setMargins(0,dp(10),0,dp(6));
        overlay.addView(brand);overlay.addView(status);overlay.addView(bar,bp);

        setContentView(frame);

        final int[] frames={
            R.drawable.heavy_loading_1,
            R.drawable.heavy_loading_2,
            R.drawable.heavy_loading_3,
            R.drawable.heavy_loading_4,
            R.drawable.heavy_loading_5
        };
        final String[] labels={
            "INITIALIZING...",
            "LOADING VISUAL...",
            "SYNCHRONIZING...",
            "PREPARING LOGIN...",
            "HEAVYy PROJECT X • READY"
        };
        final int[] durations={520,520,520,520,850};
        final int[] index={0};

        Runnable[] play={new Runnable(){@Override public void run(){
            int i=index[0];
            hero.animate().cancel();
            hero.setAlpha(0f);
            hero.setScaleX(1.04f);hero.setScaleY(1.04f);
            hero.setImageResource(frames[i]);
            status.setText(labels[i]);
            bar.setProgress((int)(((i+1)*100f)/frames.length));
            hero.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(380).start();
            if(i==frames.length-1){
                handler.postDelayed(()->{
                    hero.animate().alpha(0f).setDuration(300).withEndAction(()->showLogin()).start();
                },durations[i]);
            }else{
                index[0]=i+1;
                handler.postDelayed(this,durations[i]);
            }
        }}};
        play[0].run();
    }

    void showLogin(){
        FrameLayout frame=new FrameLayout(this);
        frame.setBackgroundResource(R.drawable.bg_root);

        AnimatedBackground bg=new AnimatedBackground(this);
        frame.addView(bg,new FrameLayout.LayoutParams(-1,-1));

        ScrollView scroll=new ScrollView(this);
        LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(dp(24),dp(24),dp(24),dp(28));

        ImageView logo=new ImageView(this);
        logo.setImageResource(R.drawable.heavyy_icon); logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        LinearLayout.LayoutParams lpLogo=new LinearLayout.LayoutParams(dp(112),dp(112)); lpLogo.gravity=Gravity.CENTER_HORIZONTAL; l.addView(logo,lpLogo);

        TextView brand=tv("HEAVYy",34); brand.setTypeface(null,Typeface.BOLD); brand.setGravity(Gravity.CENTER); l.addView(brand);
        TextView sub=tv("PROJECT X",14); sub.setTypeface(null,Typeface.BOLD); sub.setTextColor(0xFFBCA7FF); sub.setGravity(Gravity.CENTER); l.addView(sub);
        TextView badge=tv("PRIVATE GAMING CONTROL • V5.1",10); badge.setTextColor(0xFF67E8F9); badge.setGravity(Gravity.CENTER); l.addView(badge);
        space(l,22);

        LinearLayout panel=card(); panel.setPadding(dp(20),dp(20),dp(20),dp(20));
        TextView title=tv("WELCOME BACK",12); title.setTypeface(null,Typeface.BOLD); title.setTextColor(0xFF67E8F9); panel.addView(title);
        TextView desc=tv("Masuk untuk membuka dashboard gaming kamu.",13); desc.setTextColor(0xFFB9BAC6); panel.addView(desc); space(panel,12);

        EditText u=new EditText(this); styleInput(u,"Username"); panel.addView(u,new LinearLayout.LayoutParams(-1,dp(54)));
        space(panel,8);
        EditText pw=new EditText(this); styleInput(pw,"Password"); pw.setInputType(0x81); panel.addView(pw,new LinearLayout.LayoutParams(-1,dp(54)));
        space(panel,14);
        Button login=btn("ENTER  •  OPEN DASHBOARD"); panel.addView(login);
        login.setOnClickListener(v->{String role=authenticate(u.getText().toString().trim(),pw.getText().toString());if(role!=null){prefs.edit().putString("role",role).apply();showMain();}else Toast.makeText(this,"Username atau password salah",Toast.LENGTH_SHORT).show();});
        l.addView(panel);
        space(l,16);
        TextView foot=tv("SECURE SESSION  •  HEAVYy PROJECT X",10); foot.setTextColor(0xFF737786); foot.setGravity(Gravity.CENTER); l.addView(foot);

        scroll.addView(l); frame.addView(scroll,new FrameLayout.LayoutParams(-1,-1)); setContentView(frame);
    }

    void styleInput(EditText e,String hint){
        e.setHint(hint); e.setHintTextColor(0xFF777A88); e.setTextColor(Color.WHITE); e.setTextSize(14); e.setSingleLine(true);
        e.setPadding(dp(16),0,dp(16),0); e.setBackground(round(0x181FFFFFF,0x385E5CEB,16));
    }

    void checkForUpdate(){
        new Thread(()->{
            try{
                HttpURLConnection c=(HttpURLConnection)new URL(UPDATE_CONFIG_URL).openConnection();
                c.setConnectTimeout(5000); c.setReadTimeout(5000);
                BufferedReader br=new BufferedReader(new InputStreamReader(c.getInputStream()));
                StringBuilder body=new StringBuilder(); String line;
                while((line=br.readLine())!=null) body.append(line);
                br.close(); c.disconnect();
                String json=body.toString();
                int remote=readUpdateInt(json,"versionCode",CURRENT_VERSION_CODE);
                String url=readUpdateString(json,"updateUrl",UPDATE_CHANNEL_URL);
                if(remote>CURRENT_VERSION_CODE) runOnUiThread(()->showUpdateRequired(url));
            }catch(Exception ignored){}
        }).start();
    }

    int readUpdateInt(String json,String key,int fallback){
        try{
            java.util.regex.Matcher m=java.util.regex.Pattern.compile("\""+key+"\"\\s*:\\s*(\\d+)").matcher(json);
            return m.find()?Integer.parseInt(m.group(1)):fallback;
        }catch(Exception e){return fallback;}
    }

    String readUpdateString(String json,String key,String fallback){
        try{
            java.util.regex.Matcher m=java.util.regex.Pattern.compile("\""+key+"\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
            return m.find()?m.group(1):fallback;
        }catch(Exception e){return fallback;}
    }

    void showUpdateRequired(String updateUrl){
        new AlertDialog.Builder(this)
            .setTitle("HEAVYy PROJECT X")
            .setMessage("MAAF HEAVYy PROJECT X SUDAH UPDATE\n\nSILAHKAN UPDATE APLIKASI KE VERSI TERBARU")
            .setCancelable(false)
            .setPositiveButton("UPDATE HEAVYy PROJECT X KE SINI",(d,w)->{
                try{startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl)));}
                catch(Exception e){startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(UPDATE_CHANNEL_URL)));}
            }).show();
    }

    String authenticate(String u,String p){
        if(u.equals(DEV_USER)&&p.equals(DEV_PASS))return"DEVELOPER";
        if(validStoredAccount(u,p,"developer2_user","developer2_pass"))return"DEVELOPER";
        if(validStoredAccount(u,p,"reseller_user","reseller_pass"))return"RESELLER";
        if(validStoredAccount(u,p,"premium_user","premium_pass"))return"PREMIUM";
        if(u.equals(prefs.getString("press_user",""))&&p.equals(prefs.getString("press_pass","")))return"PRESS";
        if(u.equals(prefs.getString("free_user",""))&&p.equals(prefs.getString("free_pass","")))return"FREE";
        return null;
    }

    boolean validStoredAccount(String u,String p,String uk,String pk){
        if(!u.equals(prefs.getString(uk,""))||!p.equals(prefs.getString(pk,"")))return false;
        long exp=prefs.getLong(uk+"_expiry",0);
        return exp==0 || System.currentTimeMillis()<exp;
    }

    void space(LinearLayout l,int h){Space s=new Space(this);l.addView(s,new LinearLayout.LayoutParams(1,dp(h)));}
    String role(){return prefs.getString("role","FREE");}
    boolean unlimited(){String r=role();return r.equals("PREMIUM")||r.equals("RESELLER")||r.equals("PRESS")||r.equals("DEVELOPER");}
    int power(){return prefs.getInt("power",100);}
    void setPower(int p){prefs.edit().putInt("power",Math.max(0,Math.min(MAX_POWER,p))).apply();}

    void showMain(){
        FrameLayout frame=new FrameLayout(this);
        animatedBackground=new AnimatedBackground(this); frame.addView(animatedBackground,new FrameLayout.LayoutParams(-1,-1));
        root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(16),dp(12),dp(16),dp(8));
        frame.addView(root,new FrameLayout.LayoutParams(-1,-1));

        LinearLayout header=new LinearLayout(this); header.setGravity(Gravity.CENTER_VERTICAL); header.setPadding(dp(4),dp(4),dp(4),dp(8));
        ImageView icon=new ImageView(this); icon.setImageResource(R.drawable.heavyy_icon); icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        header.addView(icon,new LinearLayout.LayoutParams(dp(48),dp(48)));
        LinearLayout htxt=new LinearLayout(this); htxt.setOrientation(LinearLayout.VERTICAL); htxt.setPadding(dp(10),0,0,0);
        TextView title=tv("HEAVYy PROJECT X",20); title.setTypeface(null,Typeface.BOLD); htxt.addView(title);
        roleText=tv(role()+"  •  V5.1 FINAL",10); roleText.setTextColor(0xFF67E8F9); htxt.addView(roleText); header.addView(htxt,new LinearLayout.LayoutParams(0,dp(52),1));
        TextView dot=tv("●",18); dot.setTextColor(0xFF22D3A5); header.addView(dot);
        root.addView(header);

        LinearLayout nav=new LinearLayout(this); nav.setPadding(0,dp(4),0,dp(8));
        String[] ns={"HOME","LAUNCH","ACCOUNTS"};
        for(String n:ns){Button x=btn(n);x.setTextSize(10);x.setAllCaps(true);nav.addView(x,new LinearLayout.LayoutParams(0,dp(42),1));x.setOnClickListener(v->{if(n.equals("HOME"))dashboard();else if(n.equals("LAUNCH"))launcher();else accounts();});}
        root.addView(nav);

        ScrollView sv=new ScrollView(this); content=new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL); content.setPadding(0,dp(2),0,dp(22)); sv.addView(content); root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(frame); dashboard();
    }

    void heroBanner(String kicker,String title,String subtitle){
        LinearLayout h=card(); h.setPadding(dp(20),dp(18),dp(20),dp(18));
        TextView k=tv(kicker,10); k.setTypeface(null,Typeface.BOLD); k.setTextColor(0xFF67E8F9); h.addView(k);
        TextView t=tv(title,25); t.setTypeface(null,Typeface.BOLD); h.addView(t);
        TextView s=tv(subtitle,12); s.setTextColor(0xFFB8BAC7); h.addView(s); content.addView(h);
    }

    void clear(){content.removeAllViews();}

    void dashboard(){
        clear();
        heroBanner("HEAVYy CONTROL CENTER","READY TO GAME","Performance tools • overlay • launcher dalam satu panel");

        LinearLayout stats=card();
        TextView st=cardTitle("SYSTEM STATUS"); stats.addView(st);
        LinearLayout grid=new LinearLayout(this); grid.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout a=miniStat("POWER",unlimited()?"∞":""+power()+"/700",0xFF67E8F9); LinearLayout b=miniStat("ROLE",role(),0xFFA78BFA);
        grid.addView(a,new LinearLayout.LayoutParams(0,dp(78),1)); spaceRow(grid,8); grid.addView(b,new LinearLayout.LayoutParams(0,dp(78),1)); stats.addView(grid);
        powerText=tv("",11); powerText.setTextColor(0xFF9CA3AF); stats.addView(powerText); content.addView(stats);

        LinearLayout mon=card();
        TextView mt=cardTitle("LIVE DEVICE"); mon.addView(mt); monitorText=tv("Mengambil data perangkat…",12); monitorText.setTextColor(0xFFCBD5E1); mon.addView(monitorText); content.addView(mon);

        overlayCard();

        LinearLayout launch=card(); launch.addView(cardTitle("QUICK LAUNCH"));
        Button f=btn("▶   FREE FIRE"); launch.addView(f); f.setOnClickListener(v->runGameResolved("Free Fire",false));
        Button fm=btn("▶   FREE FIRE MAX"); launch.addView(fm); fm.setOnClickListener(v->runGameResolved("Free Fire MAX",true));
        if(role().equals("PRESS")||role().equals("DEVELOPER")){Button cache=btn("⌫   CLEAR CACHE");launch.addView(cache);cache.setOnClickListener(v->clearCacheMenu());}
        content.addView(launch);

        LinearLayout modes=card(); modes.addView(cardTitle("HPX PERFORMANCE MODES"));
        modes.addView(tv("Pilih profil performa. Mode bekerja dengan pengaturan Android yang tersedia; tidak mengubah atau menyuntik proses game.",11));
        LinearLayout modeRow=new LinearLayout(this); modeRow.setOrientation(LinearLayout.HORIZONTAL);
        Button extreme=btn("⚡ HPX EKSTREM"); Button balance=btn("⚖ HPX BALANCE"); Button battery=btn("🔋 HPX BATTERY");
        modeRow.addView(extreme,new LinearLayout.LayoutParams(0,dp(54),1)); spaceRow(modeRow,6); modeRow.addView(balance,new LinearLayout.LayoutParams(0,dp(54),1)); spaceRow(modeRow,6); modeRow.addView(battery,new LinearLayout.LayoutParams(0,dp(54),1));
        modes.addView(modeRow);
        TextView modeStatus=tv("Mode aktif: "+prefs.getString("hpx_mode","HPX BALANCE"),11); modeStatus.setTextColor(0xFF67E8F9); modes.addView(modeStatus);
        extreme.setOnClickListener(v->{setPerformanceMode("HPX EKSTREM");modeStatus.setText("Mode aktif: "+prefs.getString("hpx_mode","HPX EKSTREM"));});
        balance.setOnClickListener(v->{setPerformanceMode("HPX BALANCE");modeStatus.setText("Mode aktif: "+prefs.getString("hpx_mode","HPX BALANCE"));});
        battery.setOnClickListener(v->{setPerformanceMode("HPX BATTERY");modeStatus.setText("Mode aktif: "+prefs.getString("hpx_mode","HPX BATTERY"));});
        content.addView(modes);

        LinearLayout features=card(); features.addView(cardTitle("FEATURE MODULES"));
        addDevelopmentFeature(features,"DRAG HS V4"); addDevelopmentFeature(features,"GRAFIK MC");
        addFeatureToggle(features,"ANTI LAG PERFORMANCE","feature_lag",false); addFeatureToggle(features,"MONITORING LIFE TIME","feature_monitor",true); addFeatureToggle(features,"CROSHAIR","feature_crosshair",false); content.addView(features);

        LinearLayout set=card(); set.addView(cardTitle("SYSTEM")); Button settings=btn("⚙   ANDROID SETTINGS");set.addView(settings);settings.setOnClickListener(v->startActivity(new Intent(Settings.ACTION_SETTINGS)));
        Button wireless=btn("⌁   WIRELESS DEBUGGING / DEVELOPER OPTIONS"); set.addView(wireless); wireless.setOnClickListener(v->{try{startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));}catch(Exception e){startActivity(new Intent(Settings.ACTION_SETTINGS));}});
        content.addView(set);
        if(role().equals("FREE")){LinearLayout info=card();TextView q=cardTitle("FREE ACCESS");info.addView(q);info.addView(tv("Akun FREE tetap dapat melihat fitur V1. Tombol V1 menampilkan status pengembangan dan tidak menjalankan fungsi game.",12));content.addView(info);}
        updatePowerText();updateMonitor();
    }

    LinearLayout miniStat(String label,String value,int accent){
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(dp(12),dp(9),dp(12),dp(8)); box.setBackground(round(0x141FFFFFF,0x244B4F63,16));
        TextView l=tv(label,9);l.setTextColor(0xFF8F93A3);box.addView(l);TextView v=tv(value,17);v.setTypeface(null,Typeface.BOLD);v.setTextColor(accent);box.addView(v);return box;
    }
    void spaceRow(LinearLayout l,int w){Space s=new Space(this);l.addView(s,new LinearLayout.LayoutParams(dp(w),1));}

    void addDevelopmentFeature(LinearLayout parent,String label){
        LinearLayout row=new LinearLayout(this); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(0,dp(4),0,dp(4));
        TextView name=tv(label,14); row.addView(name,new LinearLayout.LayoutParams(0,dp(52),1));
        Button open=btn("COMING SOON"); open.setTextSize(9); row.addView(open,new LinearLayout.LayoutParams(dp(120),dp(46)));
        open.setOnClickListener(v->showFeatureDevelopmentOverlay());
        parent.addView(row);
    }

    void showFeatureDevelopmentOverlay(){
        new AlertDialog.Builder(this).setTitle("HEAVYy PROJECT X").setMessage("FITUR INI DALAM MASA PENGEMBANGAN\n\nFitur akan tersedia pada update berikutnya.").setPositiveButton("OK",null).show();
    }

    void setPerformanceMode(String mode){
        prefs.edit().putString("hpx_mode",mode).apply();
        if(mode.equals("HPX BATTERY") && Build.VERSION.SDK_INT>=23 && Settings.System.canWrite(this)){
            try{int old=Settings.System.getInt(getContentResolver(),Settings.System.SCREEN_BRIGHTNESS,128);prefs.edit().putInt("pre_battery_brightness",old).apply();Settings.System.putInt(getContentResolver(),Settings.System.SCREEN_BRIGHTNESS,Math.max(20,old*55/100));}catch(Exception ignored){}
        }else if(mode.equals("HPX BALANCE") && Build.VERSION.SDK_INT>=23 && Settings.System.canWrite(this)){
            int old=prefs.getInt("pre_battery_brightness",-1);if(old>=0){try{Settings.System.putInt(getContentResolver(),Settings.System.SCREEN_BRIGHTNESS,old);}catch(Exception ignored){}}
        }
        Toast.makeText(this,mode+" aktif",Toast.LENGTH_SHORT).show();
    }

    void addFeatureToggle(LinearLayout parent,String label,String key,boolean defaultOn){
        LinearLayout row=new LinearLayout(this); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(0,dp(4),0,dp(4));
        TextView name=tv(label,14); row.addView(name,new LinearLayout.LayoutParams(0,dp(52),1));
        Switch sw=new Switch(this); sw.setText("ON / OFF"); sw.setTextColor(Color.WHITE); sw.setTextSize(11);
        sw.setChecked(prefs.getBoolean(key,defaultOn));
        sw.setOnCheckedChangeListener((button,checked)->{prefs.edit().putBoolean(key,checked).apply(); if(key.equals("feature_monitor")){if(checked) updateMonitor(); else {handler.removeCallbacks(this::updateMonitor); if(monitorText!=null) monitorText.setText("Monitoring Life Time: OFF");}} Toast.makeText(this,label+" • "+(checked?"ON":"OFF"),Toast.LENGTH_SHORT).show();});
        row.addView(sw); parent.addView(row);
    }

    void updatePowerText(){
        if(powerText==null)return;
        if(unlimited())powerText.setText("Power: UNLIMITED");
        else{long next=prefs.getLong("next_power_time",0);if(power()<MAX_POWER&&next>System.currentTimeMillis()){long sec=(next-System.currentTimeMillis()+999)/1000;powerText.setText("Power: "+power()+"/"+MAX_POWER+"  •  +100 dalam "+sec+" detik");}else powerText.setText("Power: "+power()+"/"+MAX_POWER);}
    }

    void addPower(){
        if(unlimited()){Toast.makeText(this,"Power akun "+role()+" adalah UNLIMITED.",Toast.LENGTH_SHORT).show();return;}
        if(power()>=MAX_POWER){Toast.makeText(this,"Power sudah maksimum 700.",Toast.LENGTH_SHORT).show();return;}
        long now=System.currentTimeMillis(),next=prefs.getLong("next_power_time",0);
        if(next>now){long sec=(next-now+999)/1000;Toast.makeText(this,"Tunggu "+sec+" detik untuk 100 Power berikutnya.",Toast.LENGTH_SHORT).show();return;}
        setPower(power()+100);prefs.edit().putLong("next_power_time",System.currentTimeMillis()+POWER_INTERVAL).apply();
        updatePowerText();Toast.makeText(this,"+100 Power ditambahkan.",Toast.LENGTH_SHORT).show();
    }

    void runGame(String pkg,String name){
        if(!unlimited()){
            long expiry=prefs.getLong("free_run_expiry",0);
            if(expiry<=System.currentTimeMillis()){
                if(power()<100){new AlertDialog.Builder(this).setTitle("Power diperlukan").setMessage("Kamu harus memiliki minimal 100 Power untuk menjalankan "+name+".\n\nPower kamu sekarang: "+power()).setPositiveButton("OK",null).show();return;}
                setPower(power()-100);prefs.edit().putLong("free_run_expiry",System.currentTimeMillis()+RUN_FREE_DURATION).apply();
            }
        }
        launchPackage(pkg);updatePowerText();
    }

    void runGameResolved(String name,boolean max){
        String[] candidates=max ? new String[]{"com.dts.freefiremax","com.dts.freefiremaxth","com.dts.freefiremax.official"} : new String[]{"com.dts.freefire","com.dts.freefireth","com.dts.freefireth.official"};
        String pkg=findInstalledPackage(candidates);
        if(pkg==null){
            new AlertDialog.Builder(this).setTitle(name+" tidak ditemukan").setMessage("HEAVYy PROJECT X tidak menemukan package game yang cocok di perangkat ini.\n\nBuka Play Store/App Info lalu pastikan "+name+" benar-benar terpasang.").setPositiveButton("OK",null).show();
            return;
        }
        runGame(pkg,name);
    }

    String findInstalledPackage(String[] candidates){
        PackageManager pm=getPackageManager();
        for(String p:candidates){try{pm.getPackageInfo(p,0);return p;}catch(Exception ignored){}}
        return null;
    }

    void updateMonitor(){
        if(monitorText==null)return;
        if(!prefs.getBoolean("feature_monitor",true)){ monitorText.setText("Monitoring Life Time: OFF"); return; }
        ActivityManager am=(ActivityManager)getSystemService(ACTIVITY_SERVICE);ActivityManager.MemoryInfo mi=new ActivityManager.MemoryInfo();am.getMemoryInfo(mi);
        double used=(mi.totalMem-mi.availMem)/1073741824.0,total=mi.totalMem/1073741824.0;
        monitorText.setText("RAM  "+fmt(used)+" / "+fmt(total)+" GB\nRAM tersedia  "+fmt(mi.availMem/1073741824.0)+" GB\nSDK  "+Build.VERSION.SDK_INT+"\nDevice  "+Build.MANUFACTURER+" "+Build.MODEL);
        handler.postDelayed(this::updateMonitor,1000);updatePowerText();
    }
    String fmt(double x){return new DecimalFormat("0.00").format(x);}


    void overlayCard(){
        LinearLayout o=card();
        TextView title=cardTitle("GAMING OVERLAY"); o.addView(title);
        TextView desc=tv("Crosshair + live hardware panel. Dibuat untuk tampilan gaming yang clean dan tidak mengganggu layar.",11); desc.setTextColor(0xFF9FA4B2); o.addView(desc); space(o,10);

        LinearLayout actions=new LinearLayout(this); actions.setOrientation(LinearLayout.HORIZONTAL);
        Button perm=btn("PERMISSION"); actions.addView(perm,new LinearLayout.LayoutParams(0,dp(46),1)); spaceRow(actions,8);
        Button start=btn("●  START"); actions.addView(start,new LinearLayout.LayoutParams(0,dp(46),1)); spaceRow(actions,8);
        Button stop=btn("■  STOP"); actions.addView(stop,new LinearLayout.LayoutParams(0,dp(46),1)); o.addView(actions);
        perm.setOnClickListener(v->{if(Build.VERSION.SDK_INT>=23){try{startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName())));}catch(Exception e){startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));}}});
        start.setOnClickListener(v->startGamingOverlay()); stop.setOnClickListener(v->stopGamingOverlay());

        space(o,8); TextView modules=tv("OVERLAY MODULES",10);modules.setTypeface(null,Typeface.BOLD);modules.setTextColor(0xFF67E8F9);o.addView(modules);
        addFeatureToggle(o,"CUSTOM CROSSHAIR","overlay_crosshair",true); addFeatureToggle(o,"CPU INFORMATION","overlay_cpu",true); addFeatureToggle(o,"RAM INFORMATION","overlay_ram",true); addFeatureToggle(o,"TEMPERATURE INFORMATION","overlay_temp",true); addFeatureToggle(o,"BATTERY INFORMATION","overlay_battery",true); addFeatureToggle(o,"FPS / REFRESH RATE","overlay_fps",true); addFeatureToggle(o,"TIME INFORMATION","overlay_time",false);
        TextView colorTitle=tv("CROSSHAIR COLOR",10);colorTitle.setTypeface(null,Typeface.BOLD);colorTitle.setTextColor(0xFF67E8F9);o.addView(colorTitle);
        LinearLayout colors=new LinearLayout(this); colors.setGravity(Gravity.CENTER_VERTICAL);
        int[] cs={0xFF67E8F9,0xFF22D3A5,0xFFFF4D6D,0xFFA78BFA,0xFFFFB84D,0xFFFFFF66};
        for(int col:cs){TextView b=tv("●",25);b.setGravity(Gravity.CENTER);b.setTextColor(col);b.setBackground(round(0x101FFFFFF,0x204B4F63,14));colors.addView(b,new LinearLayout.LayoutParams(0,dp(48),1));b.setOnClickListener(v->{prefs.edit().putInt("overlay_color",col).apply();Toast.makeText(this,"Crosshair color updated",Toast.LENGTH_SHORT).show();});}
        o.addView(colors);content.addView(o);
    }

    boolean overlayAllowed(){return Build.VERSION.SDK_INT<23 || Settings.canDrawOverlays(this);}
    void startGamingOverlay(){
        if(!overlayAllowed()){Toast.makeText(this,"Izinkan 'tampil di atas aplikasi' terlebih dahulu.",Toast.LENGTH_LONG).show();try{startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName())));}catch(Exception ignored){}return;}
        Intent i=new Intent(this,OverlayService.class);if(Build.VERSION.SDK_INT>=26)startForegroundService(i);else startService(i);Toast.makeText(this,"Gaming Overlay aktif",Toast.LENGTH_SHORT).show();
    }
    void stopGamingOverlay(){stopService(new Intent(this,OverlayService.class));Toast.makeText(this,"Gaming Overlay dihentikan",Toast.LENGTH_SHORT).show();}

    void clearCacheMenu(){
        final String[] names={"Free Fire","Free Fire MAX"};
        final String[] pkgs={findInstalledPackage(new String[]{"com.dts.freefire","com.dts.freefireth","com.dts.freefireth.official"}),findInstalledPackage(new String[]{"com.dts.freefiremax","com.dts.freefiremaxth","com.dts.freefiremax.official"})};

        new AlertDialog.Builder(this)
            .setTitle("HEAVYy PROJECT X")
            .setItems(names,(dialog,which)->{ if(pkgs[which]==null){ Toast.makeText(this,names[which]+" tidak ditemukan.",Toast.LENGTH_SHORT).show(); } else { showCacheOverlay(names[which],pkgs[which]); } })
            .setNegativeButton("Batal",null)
            .show();
    }

    void showCacheOverlay(String gameName,String pkg){
        final Dialog d=new Dialog(this);
        LinearLayout box=new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(28),dp(24),dp(28),dp(24));

        GradientDrawable bg=new GradientDrawable();
        bg.setColor(0xF21A1826);
        bg.setCornerRadius(dp(26));
        bg.setStroke(dp(1),0x55FFFFFF);
        box.setBackground(bg);

        TextView title=tv("HEAVYy PROJECT X",22);
        title.setTypeface(null,1);
        title.setGravity(Gravity.CENTER);

        TextView msg=tv("Menyiapkan halaman Clear Cache\n\n"+gameName,14);
        msg.setGravity(Gravity.CENTER);

        ProgressBar progress=new ProgressBar(this);
        progress.setIndeterminate(true);

        TextView note=tv("Android akan membuka App Info.\\nCache hanya dapat dihapus setelah kamu mengonfirmasi tindakan di halaman Android.",12);
        note.setGravity(Gravity.CENTER);

        box.addView(title);
        box.addView(msg);
        box.addView(progress);
        box.addView(note);

        d.setContentView(box);
        Window w=d.getWindow();
        if(w!=null){
            w.setBackgroundDrawableResource(android.R.color.transparent);
            w.setDimAmount(0.65f);
            w.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            WindowManager.LayoutParams lp=new WindowManager.LayoutParams();
            lp.copyFrom(w.getAttributes());
            lp.width=(int)(getResources().getDisplayMetrics().widthPixels*0.86);
            lp.height=WindowManager.LayoutParams.WRAP_CONTENT;
            w.setAttributes(lp);
        }
        d.setCancelable(false);
        d.show();

        new Handler(Looper.getMainLooper()).postDelayed(()->{
            try{
                Intent i=new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                i.setData(android.net.Uri.parse("package:"+pkg));
                startActivity(i);
            }catch(Exception e){
                Toast.makeText(this,"Halaman App Info tidak tersedia.",Toast.LENGTH_SHORT).show();
            }
            d.dismiss();
        },1800);
    }

    void launcher(){
        clear();LinearLayout c=card();c.addView(cardTitle("Game Launcher"));
        Button f=btn("▶  Free Fire");c.addView(f);f.setOnClickListener(v->runGameResolved("Free Fire", false));
        Button fm=btn("▶  Free Fire MAX");c.addView(fm);fm.setOnClickListener(v->runGameResolved("Free Fire MAX", true));
        c.addView(tv("Launcher hanya membuka aplikasi yang terpasang; tidak mengubah proses game.",12));content.addView(c);
    }

    void launchPackage(String pkg){
        PackageManager pm=getPackageManager();Intent i=pm.getLaunchIntentForPackage(pkg);
        if(i!=null)startActivity(i);else Toast.makeText(this,"Aplikasi tidak ditemukan: "+pkg,Toast.LENGTH_SHORT).show();
    }

    void accounts(){
        clear();String r=role();
        if(!r.equals("DEVELOPER")&&!r.equals("RESELLER")){LinearLayout c=card();c.addView(cardTitle("Account"));c.addView(tv("Role: "+r+"\n\nAkses Account Manager tersedia untuk Developer dan Reseller.",14));content.addView(c);return;}
        LinearLayout c=card();c.addView(cardTitle(r+" • Account Manager"));
        accountText=tv("",14);c.addView(accountText);
        accountList=new LinearLayout(this); accountList.setOrientation(LinearLayout.VERTICAL); c.addView(accountList);
        if(r.equals("DEVELOPER")){
            Button d=btn("＋ Create Developer #2");c.addView(d);d.setOnClickListener(v->createAccount("DEVELOPER #2","developer2_user","developer2_pass","Dev"));
            Button rr=btn("＋ Create Reseller");c.addView(rr);rr.setOnClickListener(v->createAccount("RESELLER","reseller_user","reseller_pass","Res"));
            Button pp=btn("＋ Create Premium");c.addView(pp);pp.setOnClickListener(v->createAccount("PREMIUM","premium_user","premium_pass","Pre"));
        }else{Button pp=btn("＋ Create Premium");c.addView(pp);pp.setOnClickListener(v->createAccount("PREMIUM","premium_user","premium_pass","Pre"));}
        Button logout=btn("Keluar");c.addView(logout);logout.setOnClickListener(v->{prefs.edit().remove("role").apply();showLogin();});
        content.addView(c);refreshAccount();
    }

    void refreshAccount(){
        if(accountText==null)return; if(accountList!=null)accountList.removeAllViews(); StringBuilder s=new StringBuilder();
        addAcctCard(s,"DEVELOPER #2","developer2_user","developer2_pass");
        addAcctCard(s,"RESELLER","reseller_user","reseller_pass");
        addAcctCard(s,"PREMIUM","premium_user","premium_pass");
        accountText.setText(s.length()==0?"Belum ada akun tambahan.":s.toString());
    }

    void addAcctCard(StringBuilder s,String label,String uk,String pk){
        String u=prefs.getString(uk,""); if(u.isEmpty())return; String p=prefs.getString(pk,""); long exp=prefs.getLong(uk+"_expiry",0);
        String status=exp==0?"AKTIF • tanpa expired":(System.currentTimeMillis()<exp?"AKTIF • expired "+formatDate(exp):"EXPIRED • "+formatDate(exp));
        s.append("\n").append(label).append("\nUsername: ").append(u).append("\nPassword: ").append(p).append("\nStatus: ").append(status).append("\n");
        Button copyU=btn("📋 Copy Username"); accountList.addView(copyU); copyU.setOnClickListener(v->copyText("Username",u));
        Button copyP=btn("📋 Copy Password"); accountList.addView(copyP); copyP.setOnClickListener(v->copyText("Password",p));
        Button expiry=btn("📅 Atur / Ubah Expired"); accountList.addView(expiry); expiry.setOnClickListener(v->pickExpiry(uk));
        Button del=btn("🗑 Hapus Akun"); accountList.addView(del); del.setOnClickListener(v->deleteAccount(label,uk,pk));
    }

    String formatDate(long ms){return new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm",java.util.Locale.getDefault()).format(new java.util.Date(ms));}

    void copyText(String label,String value){ClipboardManager cm=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);cm.setPrimaryClip(ClipData.newPlainText(label,value));Toast.makeText(this,label+" disalin.",Toast.LENGTH_SHORT).show();}

    void pickExpiry(String userKey){
        Calendar cal=Calendar.getInstance(); long old=prefs.getLong(userKey+"_expiry",0); if(old>0)cal.setTimeInMillis(old);
        DatePickerDialog dp=new DatePickerDialog(this,(view,y,m,d)->{Calendar c=Calendar.getInstance();c.set(y,m,d,23,59,59); prefs.edit().putLong(userKey+"_expiry",c.getTimeInMillis()).apply(); refreshAccount();},cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH));
        dp.setTitle("Tanggal expired akun"); dp.setButton(DatePickerDialog.BUTTON_NEGATIVE,"Batal",(dialog,which)->dialog.dismiss()); dp.show();
    }

    void deleteAccount(String label,String uk,String pk){
        new AlertDialog.Builder(this).setTitle("Hapus "+label+"?").setMessage("Username dan password akun ini akan dihapus dari perangkat.").setNegativeButton("Batal",null).setPositiveButton("Hapus",(d,w)->{prefs.edit().remove(uk).remove(pk).remove(uk+"_expiry").apply();refreshAccount();}).show();
    }

    void createAccount(String type,String uk,String pk,String prefix){
        String u="HPX_"+prefix.toUpperCase()+"_"+random(6),p=prefix+"@"+random(5)+"#"+random(3);
        prefs.edit().putString(uk,u).putString(pk,p).putLong(uk+"_expiry",0).apply(); refreshAccount(); showCred(type,u,p);
    }

    void showCred(String type,String u,String p){
        new AlertDialog.Builder(this).setTitle(type+" dibuat").setMessage("Username: "+u+"\nPassword: "+p+"\n\nKredensial juga tersedia di Account Manager untuk Copy Username/Password dan pengaturan expired.").setPositiveButton("OK",null).show();
    }

    String random(int n){String chars="ABCDEFGHJKLMNPQRSTUVWXYZ23456789";Random r=new Random();StringBuilder s=new StringBuilder();for(int i=0;i<n;i++)s.append(chars.charAt(r.nextInt(chars.length())));return s.toString();}
    static class AnimatedBackground extends View{
        Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG); float t=0f;
        public AnimatedBackground(Context c){super(c);paint.setStyle(Paint.Style.FILL);}
        @Override protected void onDraw(Canvas c){super.onDraw(c); int w=getWidth(),h=getHeight();
            c.drawColor(Color.rgb(7,8,14));
            paint.setShader(new LinearGradient(0,0,w,h,new int[]{0xFF090B12,0xFF171022,0xFF090B12},null,Shader.TileMode.CLAMP)); c.drawRect(0,0,w,h,paint); paint.setShader(null);
            paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(2); paint.setColor(0x2230C8FF);
            for(int i=-h;i<w;i+=dpStatic(48)){float off=(t*28)%48;c.drawLine(i+off,0,i+h+off,h,paint);} 
            paint.setStyle(Paint.Style.FILL);
            for(int i=0;i<5;i++){float x=(float)((Math.sin(t*.55+i*1.7)+1)*0.5*w);float y=(float)((Math.cos(t*.42+i*1.1)+1)*0.5*h);paint.setColor(0x142D9CFF);c.drawCircle(x,y,dpStatic(70+i*18),paint);} 
            t+=0.012f; postInvalidateDelayed(16);
        }
        int dpStatic(float v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
    }

}
