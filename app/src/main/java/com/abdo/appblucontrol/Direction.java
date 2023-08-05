package com.abdo.appblucontrol;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class Direction extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle mtoggle;
    Button forward_btn, left_btn, right_btn, reverse_btn, stop ,manual_btn,auto_btn,voice_Btn;
    SeekBar speedBar;
    private NavigationView navigationView;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    String address = null;
    public static final String EXTRA_ADDRESS = "Device_Address" ;
    private ProgressDialog progress;
    private BluetoothAdapter myBluetooth = null;
    private BluetoothDevice myBlu;
    private BluetoothSocket socket = null;
    private OutputStream outputStream;
    private InputStream inputStream;
    private boolean isBtConnected = false;
    String TAG;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    //
    private void msg(String s) {
        Toast.makeText(getApplicationContext(),s, Toast.LENGTH_LONG).show();
    }
    private class ConnectBT extends AsyncTask<Void, Void, Void>  {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected
        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(Direction.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }
        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        { try { if (socket == null || !isBtConnected) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    myBlu = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    socket = myBlu.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    socket.connect();//start connection
                }
            } catch (IOException e) { ConnectSuccess = false;//if the try failed, you can check the exception here
            }return null; }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        { super.onPostExecute(result);
            if (!ConnectSuccess) { msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            }
            else { msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }}
    private BluetoothSocket createBluetoothSocket(BluetoothDevice myBlu) throws IOException {
        return myBlu.createRfcommSocketToServiceRecord(myUUID);
    }
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent newint = getIntent();
        address = newint.getStringExtra(Main.EXTRA_ADDRESS);
        setContentView(R.layout.activity_direction);
        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        myBlu = myBluetooth.getRemoteDevice(address);
        try { if (BluetoothAdapter.checkBluetoothAddress(address)) {
                //It is a valid MAC address.
                myBlu = myBluetooth.getRemoteDevice(address);
                socket = createBluetoothSocket(myBlu);
            } else { msg("Invalid MAC: Address"); }
        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            e.printStackTrace();
        }
        //
        new ConnectBT().execute();
        //Direction Icons
        forward_btn=findViewById(R.id.forward_btn);
        reverse_btn=findViewById(R.id.reverse_btn);
        stop=findViewById(R.id.stop);
        right_btn=findViewById(R.id.right_btn);
        left_btn=findViewById(R.id.left_btn);
        manual_btn=findViewById(R.id.manual_btn);
        auto_btn=findViewById(R.id.auto_btn);
        speedBar = findViewById(R.id.speedBar);
        voice_Btn= findViewById(R.id.voice_Btn);

        // Thiết lập sự kiện cho SeekBar
        speedBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Gọi function changeSpeed() ở đây với tham số là giá trị mới của progress
                changeSpeed(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Không cần xử lý ở đây
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Không cần xử lý ở đây
            }
        });


        //Button Action
        forward_btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Xử lý khi ngón tay chạm vào nút
                        if (isBtConnected) {
                            forward();
                        } else {
                            msg("First Connect to Arduino");
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        // Xử lý khi ngón tay rời khỏi nút
                        stop();
                        return true;
                }
                return false;
            }
        });
        reverse_btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Xử lý khi ngón tay chạm vào nút
                        if (isBtConnected) {
                            reserve();
                        } else {
                            msg("First Connect to Arduino");
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        // Xử lý khi ngón tay rời khỏi nút
                        stop();
                        return true;
                }
                return false;
            }
        });
        left_btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Xử lý khi ngón tay chạm vào nút
                        if (isBtConnected) {
                            left();
                        } else {
                            msg("First Connect to Arduino");
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        // Xử lý khi ngón tay rời khỏi nút
                        stop();
                        return true;
                }
                return false;
            }
        });
        right_btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Xử lý khi ngón tay chạm vào nút
                        if (isBtConnected) {
                            right();
                        } else {
                            msg("First Connect to Arduino");
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        // Xử lý khi ngón tay rời khỏi nút
                        stop();
                        return true;
                }
                return false;
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isBtConnected==true){
                    stop();
                }
                else{ msg("First Connect to Arduino"); }
            }
        });

        manual_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isBtConnected==true){
                    manualMode();
                }
                else{ msg("First Connect to Arduino"); }
            }
        });

        auto_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isBtConnected==true){
                    autoMode();
                }
                else{ msg("First Connect to Arduino"); }
            }
        });

        voice_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isBtConnected==true){
                    promptSpeechInput();
                }
                else{ msg("First Connect to Arduino"); }
            }
        });
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 50);

        try { startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) { msg("Thiết bị không hỗ trợ"); }

    }

    private void forward() {
        if (socket!=null)
        {
            try
            {
//                return int 1 to arduino
                socket.getOutputStream().write(1);
            }
            catch (IOException e)
            {
                msg("Error occurred when sending Data");
            }
        }
    }
    private void reserve() {
        if (socket!=null)
        {
            try
            {
                socket.getOutputStream().write(2);

            }
            catch (IOException e)
            {
                msg("Error occurred when sending Data");
            }
        }
    }
    private void left() {
        if (socket!=null)
        {
            try
            {
                socket.getOutputStream().write(3);

            }
            catch (IOException e)
            {
                msg("Error occurred when sending Data");
            }
        }
    }
    private void right() {
        if (socket!=null)
        {
            try
            {
                socket.getOutputStream().write(4);

            }
            catch (IOException e)
            {
                msg("Error occurred when sending Data");
            }
        }
    }

    private void stop() {
        if (socket!=null)
        {
            try
            {
                socket.getOutputStream().write(5    );

            }
            catch (IOException e)
            {
                msg("Error occurred when sending Data");
            }
        }
    }


    private void manualMode() {
        if (socket!=null)
        {
            try
            {
                socket.getOutputStream().write(9    );

            }
            catch (IOException e)
            {
                msg("Error occurred when sending Data");
            }
        }
    }
    private void turnLeft() {
        if (socket!=null)
        {
            try
            {
                socket.getOutputStream().write(6    );

            }
            catch (IOException e)
            {
                msg("Error occurred when sending Data");
            }
        }
    }

    private void turnRight() {
        if (socket!=null)
        {
            try
            {
                socket.getOutputStream().write(7    );

            }
            catch (IOException e)
            {
                msg("Error occurred when sending Data");
            }
        }
    }
    private void autoMode(){
        if (socket!=null)
        {
            try
            {
                socket.getOutputStream().write(8   );

            }
            catch (IOException e)
            {
                msg("Error occurred when sending Data");
            }
        }
    }

    public void changeSpeed(int progress){

        try {
            socket.getOutputStream().write(progress);
        } catch (IOException e) {
            msg("Error occurred when sending Data");
        }

    }
        @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(mtoggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE_SPEECH_INPUT && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            if (spokenText.contains("đi thẳng")){
                forward();
            }
            else if (spokenText.contains("lùi lại")){
                reserve();
            }
            else if (spokenText.contains("rẽ phải")){
                turnRight();
            }
            else if (spokenText.contains ("rẽ trái")){
                turnLeft();
            }
            else if (spokenText.contains("dừng lại")){
                stop();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
