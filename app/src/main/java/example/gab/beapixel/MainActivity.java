package example.gab.beapixel;

import com.example.gab.beapixel.Manifest;
import com.example.gab.beapixel.R;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import FFT.*;




public class MainActivity extends Activity implements View.OnClickListener {

    int audioSource = MediaRecorder.AudioSource.MIC;    // Audio source is the device MIC
    int channelConfig = AudioFormat.CHANNEL_IN_MONO;    // Recording in mono
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT; // Records in 16bit
    ArrayList<Integer> N = new ArrayList<>();
    ArrayList<Integer> Nd = new ArrayList<>();
    int SEUIL = 7;                                      // Threshold of peak
    ArrayList<Integer> rtc = new ArrayList<>();                                          // Colour printed
    ArrayList<Integer> rtcolon = new ArrayList<>();
    ArrayList<Integer> rtd = new ArrayList<>();
    int rtres;
    ArrayList<Integer> colonUse = new ArrayList<>();
    RealDoubleFFT transformer;
    int blockSize = 2048;                               // deal with this many samples at a time
    int sampleRate = 4000;                             // Sample rate in Hz
    public double frequency = 0.0;                      // the frequency given
    double peak;
    RecordAudio recordTask;                             // Creates a Record Audio command
    TextView tv;                                        // Creates a text view for the frequency
    boolean started = false;
    Button startButton,resetButton;
    ImageView iv;
    String info;                                        // String that will be displayed
    static int dsp=5;                                   // Time before static pattern displays

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frequency);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Intent i = getIntent();
        rtc = i.getIntegerArrayListExtra("couleurs");
        rtd = i.getIntegerArrayListExtra("dyn");
        rtres=i.getIntExtra("place",0);
        rtcolon = i.getIntegerArrayListExtra("colonnes");
        for (int k=0;k<rtcolon.size();k++)
            colonUse.add(rtres%(rtcolon.get(k)));
        addIncr();
        addIncrd();
        tv = (TextView) findViewById(R.id.idR);
        startButton = (Button) findViewById(R.id.start_btn);
        resetButton = (Button) findViewById(R.id.idrst);
        resetButton.setVisibility(Button.INVISIBLE);
        transformer = new RealDoubleFFT(blockSize);
        iv = (ImageView) findViewById(R.id.idiV);
        iv.setVisibility(ImageView.INVISIBLE);
        startButton.setOnClickListener(this);
        resetButton.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
    private class RecordAudio extends AsyncTask<Void, Double, Void> {

        void launchPat(double fmin, double fmax, Double indPatt){
            try {
                if (peak > SEUIL && frequency < fmax && frequency > fmin) {
                    N.set(indPatt.intValue(), N.get(indPatt.intValue()) + 1);
                    if (N.get(indPatt.intValue()) >= dsp)
                        this.publishProgress(frequency, peak, indPatt,0.0);
                } else {
                    N.set(indPatt.intValue(), 0);
                }
            }catch(IndexOutOfBoundsException ioobe) {
                ioobe.printStackTrace();
            }
        }
        void launchPatdyn(double fmin, double fmax, Double indPatt, int disp){
            try {
                if (peak > SEUIL && frequency < fmax && frequency > fmin) {
                    Nd.set(indPatt.intValue(), Nd.get(indPatt.intValue()) + 1);
                    if (Nd.get(indPatt.intValue()) <= colonUse.get(indPatt.intValue()) + disp && Nd.get(indPatt.intValue()) >= colonUse.get(indPatt.intValue()))
                        this.publishProgress(frequency, peak, indPatt,1.0);
                    else if(Nd.get(indPatt.intValue())> colonUse.get(indPatt.intValue()) + disp-disp/2)
                        this.publishProgress(frequency, peak, indPatt, -1.0);
                } else {
                    Nd.set(indPatt.intValue(), 0);
                }
            }catch(IndexOutOfBoundsException ioobe) {
                ioobe.printStackTrace();
            }
        }
        @Override
        protected Void doInBackground(Void... params) {
        /*Calculates the fft and frequency of the input*/
            int bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioEncoding);                // Gets the minimum buffer needed
            AudioRecord audioRecord = new AudioRecord(audioSource, sampleRate, channelConfig, audioEncoding, bufferSize + 100);   // The RAW PCM sample recording
            short[] buffer = new short[blockSize];          // Save the raw PCM samples as short bytes
            double[] audioDataDoubles = new double[(blockSize * 2)]; // Same values as above, as doubles
            double[] re = new double[blockSize];
            double[] im = new double[blockSize];
            double[] magnitude = new double[blockSize];
            double[] toTransform = new double[blockSize];
            //double M = 0.0;
            try {
                audioRecord.startRecording();  //Start
            } catch (Throwable t) {
                Log.e("AudioRecord", "Recording Failed");
            }
            while (started) {
               /* if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                    M = 1.0;
                }*/
                /* Reads the data from the microphone. it takes in data
                 * to the size of the window "blockSize". The data is then
                 * given in to audioRecord. The int returned is the number
                * of bytes that were read*/
                int bufferReadResult = audioRecord.read(buffer, 0, blockSize);
                // Read in the data from the mic to the array
                for (int i = 0; i < blockSize && i < bufferReadResult; i++) {
                    /* dividing the short by 32768.0 gives us the
                     * result in a range -1.0 to 1.0.
                     * Data for the compextForward is given back
                     * as two numbers in sequence. Therefore audioDataDoubles
                     * needs to be twice as large*/
                    audioDataDoubles[2 * i] = (double) buffer[i] / 32768.0; // signed 16 bit
                    audioDataDoubles[(2 * i) + 1] = 0.0;
                    toTransform[i] = (double) buffer[i] / 32768.0; // signed 16 bit
                }
                transformer.ft(toTransform);
                //------------------------------------------------------------------------------------------
                // Calculate the Real and imaginary and Magnitude.
                for (int i = 0; i < blockSize + 1; i++) {
                    try {
                        // real is stored in first part of array
                        re[i] = toTransform[i * 2];
                        // imaginary is stored in the sequential part
                        im[i] = toTransform[(i * 2) + 1];
                        // magnitude is calculated by the square root of (imaginary^2 + real^2)
                        magnitude[i] = Math.sqrt((re[i] * re[i]) + (im[i] * im[i]));
                    } catch (ArrayIndexOutOfBoundsException e) {
                        Log.e("test", "NULL");
                    }
                }
                peak = -1.0;
                double freq = -1.0;
                // Get the largest magnitude peak
                for (int i = 1; i < blockSize; i++) {
                    if (peak < magnitude[i]) {
                        peak = magnitude[i];
                        freq = i;
                    }
                }
                // calculated the frequency
                frequency = (sampleRate * freq) / blockSize;//freq->peak
//----------------------------------------------------------------------------------------------
                /* calls onProgressUpdate
                 * publishes the frequency
                 */
                publishProgress(frequency,peak, -1.0,0.0);
                // LAUNCH YOUR PATTERNS HERE ////////////////////
                launchPat (430,450,0.0); //FRANCE (A)
                launchPat (247,275,1.0); // IRLANDE (C)
                launchPat(375,405,2.0); // MULTICOLORS (G)
                launchPatdyn(810,850,0.0,2); // OLA ( G#)
                ////////////////////////////////////////////////
            }
            audioRecord.stop();
            audioRecord.release();
            return null;
        }

        protected void onProgressUpdate(Double... res) {
            //print the frequency
            info = "FREQUENCE : " + Double.toString(res[0]) + "\nPeak : " + ""+res[1].intValue();
           // if (res[2] == 0.0) {
            try {
                if (res[2] == -1.0) tv.setText(info);
                else {
                    if (res[3] == 1.0) { //DYNAM UP
                        iv.setBackgroundColor(rtd.get(res[2].intValue()));
                        iv.setVisibility(ImageView.VISIBLE);
                        startButton.setVisibility(Button.INVISIBLE);
                        resetButton.setBackgroundColor(rtd.get(res[2].intValue()));
                        resetButton.setVisibility(Button.VISIBLE);
                    }
                    else if (res[3]== -1.0){ //DYNAM DOWN
                        iv.setBackgroundColor(Color.BLACK);
                        iv.setVisibility(ImageView.VISIBLE);
                        startButton.setVisibility(Button.INVISIBLE);
                        resetButton.setBackgroundColor(Color.BLACK);
                        resetButton.setVisibility(Button.VISIBLE);
                    }
                    else // STATIC
                     {
                        iv.setBackgroundColor(rtc.get(res[2].intValue()));
                        iv.setVisibility(ImageView.VISIBLE);
                        startButton.setVisibility(Button.INVISIBLE);
                         resetButton.setBackgroundColor(rtc.get(res[2].intValue()));
                         resetButton.setVisibility(Button.VISIBLE);
                    }
                }
            }catch(IndexOutOfBoundsException ioobe){
                ioobe.printStackTrace();
            }
          //  }

        }
    }
    public void addIncr(){
        for (int k=0;k<rtc.size();k++)
        N.add(0);
    }
    public void addIncrd(){
        for (int k=0;k<rtd.size();k++)
            Nd.add(0);
    }
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub

        switch (v.getId()) {
            case R.id.start_btn:
                if (started) {
                } else {
                    started = true;
                    recordTask = new RecordAudio();
                    recordTask.execute();
                    startButton.setVisibility(Button.INVISIBLE);
                    resetButton.setBackgroundColor(Color.BLACK);
                    resetButton.setVisibility(Button.VISIBLE);
                }
                break;
            case R.id.idrst:
                if (started) {
                    started = false;
                    //iv.setBackgroundColor(Color.WHITE);
                    recordTask.cancel(true);
                    iv.setVisibility(ImageView.INVISIBLE);
                    resetButton.setVisibility(Button.INVISIBLE);
                    startButton.setVisibility(Button.VISIBLE);
                }
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

    @Override
    public void onBackPressed() {
        if (started) {
            started = false;
            iv.setBackgroundColor(Color.WHITE);
            recordTask.cancel(true);
        }
        Intent i = new Intent(MainActivity.this,ActivityRun.class);
            //i.addCategory(Intent.CATEGORY_HOME);
           // i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
           // i.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);   vérifier problème de retour previous activity si on veut changer de place
        startActivity(i);
    }
}

