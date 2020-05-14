package com.example.countdowntimer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private EditText mEditTextInput;
    private Button mButtonSet;
    private long mStartTimeInMillis;
    private TextView mTextViewCountDown;
    private Button mButtonStartPause;
    private Button mButtonReset;

    private CountDownTimer mCountDownTimer;

    private boolean mTimerRunning;
    private long mTimerLeftInMIllis;
    private long mEndTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mButtonSet=findViewById(R.id.button_set);
        mEditTextInput=findViewById(R.id.edit_text_input);
        mTextViewCountDown=findViewById(R.id.text_view_countdown);
        mButtonStartPause=findViewById(R.id.button_start_pause);
        mButtonReset=findViewById(R.id.button_reset);
        mButtonStartPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if(mTimerRunning){
                pauseTimer();
            }
            else{
                startTimer();
            }
            }
        });
        mButtonSet.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input=mEditTextInput.getText().toString();
                if(input.length()==0){
                    Toast.makeText(MainActivity.this,"Field can't be empty",Toast.LENGTH_SHORT).show();
                    return;
                }
                long millisInput = Long.parseLong(input) *60000;
                if(millisInput==0){
                    Toast.makeText(MainActivity.this,"Please enter positive number",Toast.LENGTH_SHORT).show();
                    return;
                }
                setTime(millisInput);
                mEditTextInput.setText("");
            }
        }));

        mButtonReset.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTimer();
            }
        }));
    }
    private void setTime(long millliseconds){
        mStartTimeInMillis =millliseconds;
        resetTimer();
        closeKeyboard();
    }
    private void startTimer(){
        mEndTime=System.currentTimeMillis() +mTimerLeftInMIllis;
        mCountDownTimer=new CountDownTimer(mTimerLeftInMIllis,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimerLeftInMIllis=millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                mTimerRunning=false;
                updateWatchInterface();
            }
        }.start();
        mTimerRunning=true;
        updateWatchInterface();
    }
    private void pauseTimer(){
        mCountDownTimer.cancel();
        mTimerRunning=false;
        updateWatchInterface();
    }
    private void resetTimer(){
        mTimerLeftInMIllis=mStartTimeInMillis;
       updateCountDownText();
        updateWatchInterface();

    };
    private void updateCountDownText(){
        int hours= (int) (mTimerLeftInMIllis/1000)/3600;
        int minutes=(int) ((mTimerLeftInMIllis/1000)%3600)/60;
        int seconds=(int) (mTimerLeftInMIllis/1000)%60;
        String timeLeftFormatted;
        if(hours>0){
            timeLeftFormatted=String.format(Locale.getDefault(),"%d:%02d:%02d",hours,minutes,seconds);
        }else {
            timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        }
        mTextViewCountDown.setText(timeLeftFormatted);

    }
    private void updateWatchInterface(){
        if(mTimerRunning){
            mEditTextInput.setVisibility(View.INVISIBLE);
            mButtonSet.setVisibility(View.INVISIBLE);
            mButtonReset.setVisibility(View.INVISIBLE);
            mButtonStartPause.setText("Pause");
        }else{
            mEditTextInput.setVisibility(View.VISIBLE);
            mButtonSet.setVisibility(View.VISIBLE);
            mButtonStartPause.setText("start");
            if(mTimerLeftInMIllis<1000){
                mButtonStartPause.setVisibility(View.INVISIBLE);
            }else{
                mButtonStartPause.setVisibility(View.VISIBLE);
            }
            if(mTimerLeftInMIllis<mStartTimeInMillis){
                mButtonReset.setVisibility(View.VISIBLE);
            }else{
                mButtonReset.setVisibility(View.INVISIBLE);
            }
        }
    }
    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences prefs=getSharedPreferences("prefs",MODE_PRIVATE);
        SharedPreferences.Editor editor =prefs.edit();

        editor.putLong("startTimeInMillis",mStartTimeInMillis);
        editor.putLong("millisLeft",mTimerLeftInMIllis);
        editor.putBoolean("timerRunnig",mTimerRunning);
        editor.putLong("endTime",mEndTime);
        editor.apply();
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs =getSharedPreferences("prefs",MODE_PRIVATE);

        mStartTimeInMillis=prefs.getLong("startTimeInMillis",600000);
        mTimerLeftInMIllis=prefs.getLong("millisLeft",mStartTimeInMillis);
        mTimerRunning=prefs.getBoolean("timerRunning",false);

        updateCountDownText();
        updateWatchInterface();

        if(mTimerRunning){
            mEndTime=prefs.getLong("endTime",0);
            mTimerLeftInMIllis=mEndTime-System.currentTimeMillis();

            if(mTimerLeftInMIllis<0){
                mTimerLeftInMIllis=0;
                mTimerRunning=false;
                updateCountDownText();
                updateWatchInterface();
            }else {
                startTimer();
            }
        }
    }

}
