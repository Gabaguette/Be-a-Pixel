package example.gab.beapixel;
import com.example.gab.beapixel.Manifest;
import com.example.gab.beapixel.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import static java.security.AccessController.getContext;


public class ActivityRun extends AppCompatActivity implements View.OnClickListener {
    EditText tf;
    Button go;
    String res;
    int resi;
    private final int AUDIO_REQUEST_CODE = 1;
    // INIT ARRAYS HERE //////////////////////////////////
    ArrayList<Integer> pattOla = new ArrayList<>();
    ArrayList<Integer> pattFrance = new ArrayList<>();
    ArrayList<Integer> pattIrlande = new ArrayList<>();
    ArrayList<Integer> pattMulti = new ArrayList<>();
    ////////////RETRIEVED VARIABLES//////////////////////////////
    Context myContext;
    ArrayList <Integer> colon = new ArrayList<>();
    ArrayList<Integer> c = new ArrayList<>();
    ArrayList<Integer> dyn = new ArrayList<>();
    int rtlon;
    int rtlar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_run);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        Intent i = getIntent();
        rtlon = i.getIntExtra("longueur",0);
        rtlar = i.getIntExtra("largeur",0);

       askPermission(Manifest.permission.RECORD_AUDIO, AUDIO_REQUEST_CODE);
        myContext=getApplicationContext();
        // LOADING FILE HERE ////////////////////
        pattOla = loadFile(myContext,"ola-10x3");
        pattFrance = loadFile(myContext,"france-9x9");
        pattIrlande = loadFile(myContext,"irlande-9x9");
        pattMulti = loadFile(myContext,"multicolor-9x9");
        ////////////////////////////////////////
        tf = (EditText) findViewById(R.id.idNumb);
        go = (Button) findViewById(R.id.idGo);
        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    res = tf.getText().toString();
                    try {
                        resi = Integer.parseInt(res);
                        if (resi==0) Toast.makeText(myContext, "Please Enter a Positive Number", Toast.LENGTH_SHORT).show();
                        else {
                    // FILL PATTERNS HERE ///////////
                    fillPatt(pattFrance);
                    fillPatt(pattIrlande);
                    fillPatt(pattMulti);
                    fillPattDyn(pattOla);
                    /////////////////////////////////
                    Intent i = new Intent(ActivityRun.this, MainActivity.class);
                    i.putExtra("dyn",dyn);
                    i.putExtra("place",resi);
                    i.putExtra("couleurs", c);
                    i.putExtra("colonnes",colon);
                    startActivity(i);
                    }}catch(NumberFormatException nfe){Toast.makeText(myContext, "Please Enter a Positive Number", Toast.LENGTH_SHORT).show();}
            }
        });

    }

    @Override
    public void onClick(View v) {

    }

    /////////////////////////////LOAD FILE//////////////////////////////
    public ArrayList<Integer> loadFile(Context context, String file){
        ArrayList<Integer> loCol = new ArrayList<>();
        AssetManager as = context.getAssets();
        try {
            InputStream instream = as.open(file+".txt");
            if (instream != null) {
                InputStreamReader inputreader = new InputStreamReader(instream);
                BufferedReader buffreader = new BufferedReader(inputreader);
                String line;
               /* int line1;
                line1 = Integer.parseInt(buffreader.readLine());
                if (line1 == rtlon){}*/
                do {
                    line = buffreader.readLine();
                    loCol.add(Integer.parseInt(line));
                } while (line != null);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return loCol;
    }

    /////////////////STATIC PATTERN///////////////////
    public void fillPatt(ArrayList<Integer> patt){
        try {
            c.add(patt.get(Integer.parseInt(res)));
        }catch(IndexOutOfBoundsException e){
            e.printStackTrace();
        }
    }

    /////////////////DYNAMIC PATTERN////////////////////
    public void fillPattDyn(ArrayList<Integer> patt){

        colon.add(patt.get(0));
        try {
            dyn.add(patt.get(Integer.parseInt(res)));
        }catch(IndexOutOfBoundsException e){
            e.printStackTrace();
        }
    }


    //////////////////////////AUDIO PERMISSION/////////////////////////
    private void askPermission(String permission, int requestCode){
        if (ContextCompat.checkSelfPermission(this,permission)!=PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);


        }else{
                //Toast.makeText(this, "Permission is Already Granted", Toast.LENGTH_SHORT).show();

            }
        }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        switch(requestCode){
            case AUDIO_REQUEST_CODE:
                if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Audio Record Permission Granted", Toast.LENGTH_SHORT).show();
                }else if(grantResults[0] == PackageManager.PERMISSION_DENIED){

                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.RECORD_AUDIO)) {


                    }
                }
                else{
                    Toast.makeText(this, "Audio Record Permission Denied", Toast.LENGTH_SHORT).show();
                }
            break;
        }


    }


    //////////////////HANDLE ACTION BARS////////////////////
    public boolean onTouchEvent(MotionEvent event) {
        setImmersiveMode();
        return super.onTouchEvent(event);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setImmersiveMode();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        setImmersiveMode();
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            setImmersiveMode();
        }
    }

    protected void setImmersiveMode() {
        getWindow().getDecorView()
                .setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                | View.INVISIBLE);
    }

}
