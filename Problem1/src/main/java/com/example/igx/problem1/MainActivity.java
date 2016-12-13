package com.example.igx.problem1;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener, LocationListener /* implements Something1, Something2 */ {
    SensorManager sensorManager;
    Sensor sensor_gravity, sensor_accelerometer, sensor_proximity, sensor_gyroscope;
    float[] sensorGravity = new float[3];
    float[] sensorAcc = new float[3];
    float[] sensorProximity = new float[1];
    float[] sensorGyro = new float[3];

    Double latitude;        //위도
    Double longitude;       //경도

    String sensorMSG, locationMSG;

    int flag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);

        sensor_gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        sensor_accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensor_proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        sensor_gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        Button btn_getLocation = (Button) findViewById(R.id.btn_getLocation);
        Button btn_getSensors = (Button) findViewById(R.id.btn_getSensors);
        Button btn_sendMessage = (Button) findViewById(R.id.btn_sendMessage);

        final TextView text_selectedData = (TextView) findViewById(R.id.text_selectedData);
        final TextView text_selectedType = (TextView) findViewById(R.id.text_selectedType);
        final EditText edit_phoneNumber = (EditText) findViewById(R.id.edit_phoneNumber);

        //권한을 확인하기 위한 함수를 호출
        checkDangerousPermissions();

        btn_getLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text_selectedType.setText("Location");
                startLocationService();
                locationMSG = "위도 : " + latitude + "\n" + "경도 : " + longitude;
                text_selectedData.setText(locationMSG);
                flag = 0;
            }
        });

        btn_getSensors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text_selectedType.setText("Sensors");
                sensorMSG = "SENSOR_PROXIMITY : " + sensorProximity[0] + "\n"
                        + "SENSOR_ACCELEROMETER : " + sensorAcc[0] + ", " + sensorAcc[1] + ", " + sensorAcc[2] + "\n"
                        + "SENSOR_GYRO : " + sensorGyro[0] + ", " + sensorGyro[1] + ", " + sensorGyro[2] + "\n"
                        + "SENSOR_GRAVITY : " + sensorGravity[0] + ", " + sensorGravity[1] + ", " + sensorGravity[2] + "\n";
                text_selectedData.setText(sensorMSG);
                flag = 1;
            }
        });

        btn_sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = edit_phoneNumber.getText().toString();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                if(flag == 0)
                    intent.putExtra("sms_body", locationMSG);
                if(flag == 1)
                    intent.putExtra("sms_body", sensorMSG);

                intent.putExtra("address", phoneNumber);
                intent.setType("vnd.android-dir/mms-sms");
                startActivity(intent);
            }
        });
    }

    public void inputSensorValue(){

    }

    //센서 implement해서 나오는 것
    @Override
    public void onSensorChanged(SensorEvent event) {
        switch(event.sensor.getType()){
            case Sensor.TYPE_PROXIMITY:
                sensorProximity[0] = event.values[0];
                break;
            case Sensor.TYPE_GYROSCOPE:
                sensorGyro[0] = event.values[0];
                sensorGyro[1] = event.values[1];
                sensorGyro[2] = event.values[2];
                break;
            case Sensor.TYPE_ACCELEROMETER:
                sensorAcc[0] = event.values[0];
                sensorAcc[1] = event.values[1];
                sensorAcc[2] = event.values[2];
                break;
            case Sensor.TYPE_GRAVITY:
                sensorGravity[0] = event.values[0];
                sensorGravity[1] = event.values[1];
                sensorGravity[2] = event.values[2];
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();

        //onResume 상태주기에 센서를 다시 작동시키기 위해 설정하는 코드
        sensorManager.registerListener(this, sensor_gravity, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensor_accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensor_gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensor_proximity, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    protected void onPause() {
        super.onPause();
        //onPause 상태주기에 센서를 잠시 끊어주기 위해 설정하는 코드
        sensorManager.unregisterListener(this);

    }

    //권한을 확인하기위한 함수 구현
    private void checkDangerousPermissions() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        for (int i = 0; i < permissions.length; i++) {
            permissionCheck = ContextCompat.checkSelfPermission(this, permissions[i]);
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                break;
            }
        }
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "권한 있음", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "권한 없음", Toast.LENGTH_LONG).show();

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                Toast.makeText(this, "권한 설명 필요함.", Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions(this, permissions, 1);
            }
        }
    }

    //권한이 모두 확인되었을 때를 확인해주는 함수 구현
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, permissions[i] + "권한이 승인됨.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, permissions[i] + "권한이 승인되지 않음.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    //위치 정보 확인을 위해 정의한 메소드
    private void startLocationService() {
        //위치 관리자 객체 참조
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //위치 정보를 받을 리스너 생성
//        GPSListener gpsListener = new GPSListener();
        long minTime = 10000; //1000 = 1초, 1분
        float minDistance = 10; //10미터

        try {
            //gps를 이용한 위치 요청(주기적으로)
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, this);
            //네트워크를 이용한 위치 요청(주기적으로)
            manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, this);
            //위치 확인이 안되는 경우에도 최근에 확인된 위치 정보 먼저 확인
            Location lastLocation = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastLocation != null) {
                latitude = lastLocation.getLatitude();
                longitude = lastLocation.getLongitude();


            }
        } catch (SecurityException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}


