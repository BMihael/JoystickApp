package com.example.androidjoystickapp;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class MainActivity extends Activity {

    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    String address = null, name = null;
    String data = "0";
    RelativeLayout layout_joystick;
    TextView textView5;
    ImageView imageTank;
    JoyStick js;


    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    Set<BluetoothDevice> pairedDevices;


    @SuppressLint("ClickableViewAccessibility")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try{
            bluetooth_connect_device();
        }catch(IOException e){
            e.printStackTrace();
        }

        setResourcesAndView();

        js = createJoyStick();

        sendDataToBluetoothModule();

        layout_joystick.setOnTouchListener(new OnTouchListener() {
            @SuppressLint("SetTextI18n")
            public boolean onTouch(View arg0, MotionEvent arg1) {

                js.drawStick(arg1);

                if(js.getY()>0){ js.setPositionY(0); }
                if(js.getY() == 0 && ( js.getX() >1 || js.getX() < -1) ){ js.setPositionX(0); }


                if(arg1.getAction() == MotionEvent.ACTION_DOWN
                        || arg1.getAction() == MotionEvent.ACTION_MOVE) {
                    int direction = js.get4Direction();
                    if(direction == JoyStick.STICK_UP) {
                        textView5.setText("Smjer : Gore");
                        data="1";
                        layout_joystick.setBackgroundResource(R.drawable.image_button_bg_up_green);
                        imageTank.setImageResource(R.drawable.tank_forward);
                    } else if(direction == JoyStick.STICK_RIGHT) {
                        textView5.setText("Smjer : Desno");
                        data="3";
                        layout_joystick.setBackgroundResource(R.drawable.image_button_bg_right_green);
                        imageTank.setImageResource(R.drawable.tank_forward);
                    }else if(direction == JoyStick.STICK_DOWN) {
                        textView5.setText("Smjer : Dolje");
                        data="9";
                        layout_joystick.setBackgroundResource(R.drawable.image_button_bg_down_green);
                        imageTank.setImageResource(R.drawable.tank_back_red);
                    } else if(direction == JoyStick.STICK_LEFT) {
                        textView5.setText("Smjer : Lijevo");
                        data="2";
                        layout_joystick.setBackgroundResource(R.drawable.image_button_bg_left_green);
                        imageTank.setImageResource(R.drawable.tank_forward);
                    } else if(direction == JoyStick.STICK_NONE) {
                        textView5.setText("Smjer : Centar");
                        data="0";
                        layout_joystick.setBackgroundResource(R.drawable.image_button_bg);
                        imageTank.setImageResource(R.drawable.tank);
                    }
                } else if(arg1.getAction() == MotionEvent.ACTION_UP) {
                    textView5.setText("Smjer :");
                    data="0";
                    layout_joystick.setBackgroundResource(R.drawable.image_button_bg);
                    imageTank.setImageResource(R.drawable.tank);
                }
                return true;
            }
        });


    }


    public String getData(){
        return this.data;
    }


    private void bluetooth_connect_device() throws IOException {
        try
        {
            myBluetooth = BluetoothAdapter.getDefaultAdapter();
            address = myBluetooth.getAddress();
            pairedDevices = myBluetooth.getBondedDevices();
            if (pairedDevices.size()>0)
            {
                for(BluetoothDevice bt : pairedDevices)
                {
                    address=bt.getAddress();name = bt.getName();
                    Toast.makeText(getApplicationContext(),"Connected", Toast.LENGTH_SHORT).show();
                }
            }else {
                Toast.makeText(this, "No connection established!", Toast.LENGTH_SHORT).show();
            }

        } catch(Exception e){
            e.printStackTrace();

        }
        myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
        BluetoothDevice bluetoothDevice = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
        btSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
        btSocket.connect();
    }

    public void setResourcesAndView(){
        textView5 = findViewById(R.id.textView5);
        imageTank = findViewById(R.id.imageTank);
        layout_joystick = findViewById(R.id.layout_joystick);
        imageTank.setImageResource(R.drawable.tank);
    }


    public JoyStick createJoyStick(){
        JoyStick joyStick = new JoyStick(getApplicationContext() , layout_joystick, R.drawable.image_button);
        joyStick.setStickSize(150, 150);
        joyStick.setLayoutSize(1275, 1275);
        joyStick.setLayoutAlpha(150);
        joyStick.setStickAlpha(100);
        joyStick.setOffset(90);
        joyStick.setMinimumDistance(50);

        return joyStick;
    }


    private void sendDataToBluetoothModule() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask()
        {

            @Override
            public void run()
            {
                try {
                    if (btSocket!=null) {
                        btSocket.getOutputStream().write(getData().getBytes());
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }, 0, 200);
    }


}
