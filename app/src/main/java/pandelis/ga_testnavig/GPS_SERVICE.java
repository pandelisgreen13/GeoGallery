package pandelis.ga_testnavig;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;

/**
 * Created by PadPad on 28/8/2016.
 */
public class GPS_SERVICE extends Service implements SensorEventListener{

    //declare variables
    private LocationListener listener;
    private LocationManager locationManager;
    private Float thermo,humid;

    SensorManager sensorManager;
    Sensor thermoSen,humidSens;
    boolean b=false,c=false;
    String empty="0.0";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //get references of the sensors services
        sensorManager= (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        thermoSen=sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        humidSens=sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        sensorManager.registerListener(this,thermoSen,SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this,humidSens,SensorManager.SENSOR_DELAY_NORMAL);


        //check is the device has the sensors that the device needs
        if (sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE ) != null){
            b=true;
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY ) != null){
            c=true;
        }

        listener = new LocationListener() {
            //when gps get or change a location ,we call a broadcast listener to
            //send the values of sensors  back to activity
            @Override
            public void onLocationChanged(Location location) {

                Intent i = new Intent("location update");
                //send humidity,temp,Longitude,Latitude and speed
                i.putExtra("hu",humid);
                i.putExtra("temperature",thermo);
                i.putExtra("coordinates",location.getLongitude());
                i.putExtra("long",location.getLatitude());
                i.putExtra("speed",location.getSpeed()*3.6);
                sendBroadcast(i);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
            }

            @Override
            public void onProviderEnabled(String s) {
            }
            //when gps is off, open the settings to user in order to open gps
            @Override
            public void onProviderDisabled(String s) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        };
        locationManager= (LocationManager)getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        //noinspection MissingPermission
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1,1,listener);  // Deciding when to start listening for updates

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
        if(locationManager != null){
            //noinspection MissingPermission
            locationManager.removeUpdates(listener);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //if there is sensor return value ,otherwise return 0
        if(event.sensor.getType()==Sensor.TYPE_AMBIENT_TEMPERATURE && b==true) {
            thermo= event.values[0];
        }else if(b==false) {
            thermo=Float.valueOf(empty);
        }
        if(event.sensor.getType()==Sensor.TYPE_RELATIVE_HUMIDITY && c==true ) {
            humid= event.values[0];
        }else if (c==false) {
            humid=Float.valueOf(empty);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


}
