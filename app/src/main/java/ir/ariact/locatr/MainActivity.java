package ir.ariact.locatr;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.Builder;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 4;
    private static final int LOCATION_PERMISSION_REQUEST = 3;
    private ImageView imageView;
    private GoogleApiClient googleApiClient;
    private String TAG = "dev";
    private static final String[] LOCATION_PERMISSION_GROUP = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.activity_image);
        googleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {
                invalidateOptionsMenu();
            }

            @Override
            public void onConnectionSuspended(int i) {

            }
        }).build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_munu, menu);
        MenuItem item = menu.findItem(R.id.action_location);
        item.setEnabled(googleApiClient.isConnected());
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int error = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (error != ConnectionResult.SUCCESS){
            Dialog dialog = googleApiAvailability.getErrorDialog(this, error, REQUEST_CODE, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    finish();
                }
            });
            dialog.show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
        invalidateOptionsMenu();
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }

    public void findImage(){
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setNumUpdates(1).setInterval(0);
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, request, new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                Log.i(TAG, "onLocationChanged: " + location.getLatitude() + " " + location.getLongitude());
                new SearchTask(imageView).execute(location);
            }
        });

    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_location){
            if (hasLocationPerission())
            findImage();
            else {
                requestPermissions(LOCATION_PERMISSION_GROUP, LOCATION_PERMISSION_REQUEST);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean hasLocationPerission(){
        int resault = ContextCompat.checkSelfPermission(this, LOCATION_PERMISSION_GROUP[0]);
        return resault == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST)
            if (hasLocationPerission())
                findImage();
    }
}
