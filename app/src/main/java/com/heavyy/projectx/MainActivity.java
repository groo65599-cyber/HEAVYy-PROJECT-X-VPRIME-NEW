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
    final int CURRENT_VERSION_CODE = 4;
    final String UPDATE_CONFIG_URL = "https://raw.githubusercontent.com/groo65599-cyber/HEAVYy-PROJECT-X-VPRIME/main/update.json";
    final String UPDATE_CHANNEL_URL = "https://whatsapp.com/channel/0029Vb8jf279MF9APjV0cq25";

    int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}

    TextView tv(String s,float size){
        TextView t=new TextView(this); t.setText(s); t.setTextColor(Color.WHITE); t.setTextSize(size);
        t.setPadding(dp(2),dp(2),dp(2),dp(2)); return t;
    }

    Button btn(String text){
        Button b=new Button(this); b.setText(text); b.setTextColor(Color.WHITE); b.setTextSize(13);
        b.setAllCaps(false); b.setBackgroundResource(R.drawable.bg_button);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(52));
        p.setMargins(0,dp(6),0,dp(6)); b.setLayoutParams(p); return b;
    }

    TextView cardTitle(String s){TextView t=tv(s,18);t.setTypeface(null,1);return t;}

    LinearLayout card(){
        LinearLayout c=new LinearLayout(this); c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(18),dp(16),dp(18),dp(16)); c.setBackgroundResource(R.drawable.bg_glass);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);
        p.setMargins(0,0,0,dp(14)); c.setLayoutParams(p); return c;
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
        LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(dp(28),dp(20),dp(28),dp(28)); l.setGravity(Gravity.CENTER); l.setBackgroundResource(R.drawable.bg_root);
        Space top=new Space(this);l.addView(top,new LinearLayout.LayoutParams(1,0,1));
        TextView logo=tv("HEAVYy",36);logo.setTypeface(null,1);
        TextView sub=tv("PROJECT X  •  V4",15);sub.setGravity(Gravity.CENTER);
        EditText u=new EditText(this);u.setHint("Username");u.setGravity(Gravity.CENTER);u.setTextColor(Color.WHITE);u.setHintTextColor(0xFF9E9EAA);
        EditText p=new EditText(this);p.setHint("Password");p.setGravity(Gravity.CENTER);p.setInputType(0x81);p.setTextColor(Color.WHITE);p.setHintTextColor(0xFF9E9EAA);
        Button login=btn("Masuk ke Dashboard");
        l.addView(logo);l.addView(sub);space(l,18);l.addView(u);l.addView(p);l.addView(login);
        Space bottom=new Space(this);l.addView(bottom,new LinearLayout.LayoutParams(1,0,1));
        login.setOnClickListener(v->{String role=authenticate(u.getText().toString().trim(),p.getText().toString());if(role!=null){prefs.edit().putString("role",role).apply();showMain();}else Toast.makeText(this,"Username atau password salah",Toast.LENGTH_SHORT).show();});
        setContentView(l);
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
        animatedBackground=new AnimatedBackground(this);
        frame.addView(animatedBackground,new FrameLayout.LayoutParams(-1,-1));
        root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(Color.TRANSPARENT);root.setPadding(dp(16),dp(18),dp(16),dp(8));
        frame.addView(root,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);
        TextView title=tv("HEAVYy PROJECT X",22);title.setTypeface(null,1);roleText=tv("  •  "+role(),12);roleText.setTextColor(0xFFBFA9FF);
        top.addView(title,new LinearLayout.LayoutParams(0,dp(55),1));top.addView(roleText);root.addView(top);
        ScrollView sv=new ScrollView(this);content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);content.setPadding(0,dp(8),0,dp(20));sv.addView(content);root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout nav=new LinearLayout(this);nav.setPadding(0,dp(4),0,0);
        String[] ns={"Dashboard","Launcher","Accounts"};
        for(String n:ns){Button x=btn(n);x.setTextSize(12);nav.addView(x,new LinearLayout.LayoutParams(0,dp(48),1));x.setOnClickListener(v->{if(n.equals("Dashboard"))dashboard();else if(n.equals("Launcher"))launcher();else accounts();});}
        root.addView(nav);setContentView(frame);dashboard();
    }

    void clear(){content.removeAllViews();}

    void dashboard(){
        clear();
        LinearLayout pc=card();pc.addView(cardTitle("Account • "+role()));powerText=tv("",14);pc.addView(powerText);
        Button add=btn("＋ Tambahkan Power");pc.addView(add);add.setOnClickListener(v->addPower());content.addView(pc);

        LinearLayout c=card();c.addView(cardTitle("Live Monitor"));monitorText=tv("Mengambil data perangkat…",14);c.addView(monitorText);content.addView(c);

        LinearLayout a=card();a.addView(cardTitle("Game Launcher"));
        Button f=btn("▶  Run Free Fire");a.addView(f);f.setOnClickListener(v->runGameResolved("Free Fire", false));
        Button fm=btn("▶  Run Free Fire MAX");a.addView(fm);fm.setOnClickListener(v->runGameResolved("Free Fire MAX", true));
        if(role().equals("PRESS") || role().equals("DEVELOPER")){
            Button cache=btn("🧹  Clear Cache FF / FF MAX"); a.addView(cache); cache.setOnClickListener(v->clearCacheMenu());
        }
        content.addView(a);

        LinearLayout features=card();features.addView(cardTitle("V1 FEATURES"));
        features.addView(tv("Kontrol ON/OFF untuk modul yang tampil di dashboard. Toggle ini tidak menyuntik atau mengubah proses game.",12));
        addFeatureToggle(features,"DRAG HS V4","feature_drag",false);
        addFeatureToggle(features,"GRAFIK MC","feature_grafik",false);
        addFeatureToggle(features,"ANTI LAG PERFORMANCE","feature_lag",false);
        addFeatureToggle(features,"MONITORING LIFE TIME","feature_monitor",true);
        addFeatureToggle(features,"CROSHAIR","feature_crosshair",false);
        content.addView(features);

        LinearLayout set=card();set.addView(cardTitle("Settings"));
        Button settings=btn("⚙  Pengaturan Android");set.addView(settings);settings.setOnClickListener(v->startActivity(new Intent(Settings.ACTION_SETTINGS)));content.addView(set);

        if(role().equals("FREE")){LinearLayout info=card();info.addView(cardTitle("Akun FREE"));info.addView(tv("Akun FREE membutuhkan Power untuk launcher dan tidak memiliki akses Grafik MC.",13));content.addView(info);}
        updatePowerText();updateMonitor();
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
