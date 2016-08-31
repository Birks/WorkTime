package com.projectsweetwish.worktime;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;


public class MyActivity extends ActionBarActivity {

    private Chronometer mChronometer;
    private TextView countBack;
    private TextView lastTime;
    private TextView workstatus;

    public enum Status {STARTED, STOPPED}

    private Status startState;
    private long timeAtPause;
    private DBHelper mydb;
    private int countdownstart;
    private long durationSeconds;
    private CountDownTimer cdt;
    private ImageButton start;
    private PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        mChronometer = (Chronometer) findViewById(R.id.chronometer);
        lastTime = (TextView) findViewById(R.id.lastTime);
        countBack = (TextView) findViewById(R.id.countBack);
        countBack.setText("0:00:00");
        startState = Status.STOPPED;
        workstatus = (TextView) findViewById(R.id.workstatus);
        workstatus.setText("Inactive");
        Typeface robotoLight = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        mChronometer.setTypeface(robotoLight);
        countBack.setTypeface(robotoLight);
        lastTime.setTypeface(robotoLight);
        start = (ImageButton) findViewById(R.id.add_button);

        mydb = new DBHelper(this);
        setCountdownstart();


        Calendar calendar = Calendar.getInstance();


        calendar.set(Calendar.HOUR_OF_DAY, 21);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        Intent myIntent = new Intent(MyActivity.this, MyReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(MyActivity.this, 0, myIntent, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);


    }


    public void startStop(View v) {
        Calendar c = Calendar.getInstance();
        int thisTime = c.get(Calendar.HOUR_OF_DAY);
        if (thisTime >= 21 || thisTime <= 7)
            Toast.makeText(getApplicationContext(), "Can't start between 21h and 7h", Toast.LENGTH_SHORT).show();

        else

            switch (startState) {
                case STARTED: // elso inditas utan amikor fut es lepauzaljuk
                    pauseChrono();
                    break;
                case STOPPED: // amikor eloszor indul a program es startoljuk
                    startChrono();
                    break;
            }


    }

    public void startChrono() {
        startState = Status.STARTED;
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.start();
        workstatus.setText("Work in progress");
        workstatus.setTextColor(Color.GREEN);
        start.setImageResource(android.R.drawable.ic_media_pause);
        setCountdownstart();
        cdt = new CountDownTimer(countdownstart, 1000) {

            public void onTick(long millisUntilFinished) {
                durationSeconds = millisUntilFinished / 1000;
                countBack.setText(String.format("%02d:%02d:%02d", durationSeconds / 3600,
                        (durationSeconds % 3600) / 60, (durationSeconds % 60)));
            }

            public void onFinish() {
                countBack.setText("Done!");
                pauseChrono();

            }
        }.start();


    }

    public void pauseChrono() {

        mChronometer.stop();
        startState = Status.STOPPED;
        workstatus.setText("Inactive");
        workstatus.setTextColor(Color.WHITE);
        start.setImageResource(android.R.drawable.ic_media_play);
        timeAtPause = mChronometer.getBase() - SystemClock.elapsedRealtime();
        Log.v("worktime_app", String.valueOf(timeAtPause));  // TODO oda kell figyelni mert negativ az erteke
        Log.v("worktime_app", String.valueOf(durationSeconds));

        //countdownstart= (int) (durationSeconds*1000); // TODO ezt at kell majd irni hogy az adatbazisbol olvassa ki

        cdt.cancel();
        if (mydb.insertTime(String.valueOf(timeAtPause), String.valueOf(durationSeconds))) {
            Log.v("worktime_app", "done");
        }

        setCountdownstart();


    }

    public void setCountdownstart() {
        Cursor rs = mydb.getData(mydb.numberOfRows());
        rs.moveToFirst();
        String lasttime = rs.getString(rs.getColumnIndex(DBHelper.TIMES_COLUMN_WORKLENGTH));
        String remaining = rs.getString(rs.getColumnIndex(DBHelper.TIMES_COLUMN_REMAINING));

        if (!rs.isClosed()) {
            rs.close();
        }

        int tlasttime = Integer.parseInt(lasttime);
        tlasttime /= -1000;
        int tremaining = Integer.parseInt(remaining);
        countdownstart = Integer.parseInt(remaining) * 1000;
        countBack.setText(String.format("%02d:%02d:%02d", tremaining / 3600,
                (tremaining % 3600) / 60, (tremaining % 60)));
        lastTime.setText(String.format("%02d:%02d:%02d", tlasttime / 3600,
                (tlasttime % 3600) / 60, (tlasttime % 60)));


    }


    public void getdbdata(View v) {
        Toast.makeText(getApplicationContext(), String.valueOf(mydb.numberOfRows()), Toast.LENGTH_SHORT).show();
    }

    public void resetDB(View v) {
        mydb.resetTable();
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            mydb.resetTable();
            Intent intent = getIntent();
            finish();
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
