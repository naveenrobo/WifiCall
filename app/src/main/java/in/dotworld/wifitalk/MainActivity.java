package in.dotworld.wifitalk;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.net.rtp.AudioCodec;
import android.net.rtp.AudioGroup;
import android.net.rtp.AudioStream;
import android.net.rtp.RtpStream;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class MainActivity extends Activity {

    AudioGroup audioGroup;
    AudioStream audioStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audio.setMode(AudioManager.MODE_IN_COMMUNICATION);
            audioGroup = new AudioGroup();
            audioGroup.setMode(AudioGroup.MODE_ECHO_SUPPRESSION);


            audioStream = new AudioStream(InetAddress.getByAddress(getLocalIpAddress().getAddress()));
            int localPort = audioStream.getLocalPort();
            audioStream.setCodec(AudioCodec.PCMU);
            audioStream.setMode(RtpStream.MODE_NORMAL);

            ((TextView) findViewById(R.id.localIP)).setText("This device \n\tIP : "+String.valueOf(getLocalIpAddress().getHostAddress()));
            ((TextView) findViewById(R.id.localPort)).setText("\tPORT : "+String.valueOf(localPort));

            ((Button) findViewById(R.id.button1)).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    String remoteAddress = ((EditText) findViewById(R.id.editText2)).getText().toString();
                    String remotePort = ((EditText) findViewById(R.id.editText1)).getText().toString();
                    Log.i("IP/PORT",remoteAddress+","+remotePort);
                    if(remotePort == null || remotePort.isEmpty() || remoteAddress == null || remoteAddress.isEmpty()) {
                        createToast("Invalid PORT or IP Address");
                    }else {
                        try {
                            audioStream.associate(InetAddress.getByName(remoteAddress), Integer.parseInt(remotePort));
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        }
                        audioStream.join(audioGroup);
                    }
                }
            });

            ((Button) findViewById(R.id.button2)).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                                     audioStream.release();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public static InetAddress getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress;
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}