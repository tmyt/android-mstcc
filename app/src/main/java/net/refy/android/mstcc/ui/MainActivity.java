package net.refy.android.mstcc.ui;

import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialogFragment;

import net.refy.android.mstcc.R;

public class MainActivity extends AppCompatActivity {

    private static int PERMISSION_REQUEST_WRITE_SETTINGS = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(Build.VERSION.SDK_INT >= 23 && !Settings.System.canWrite(this)){
            AppCompatDialogFragment newFragment = new PermissionRequiredDialogActivity();
            newFragment.show(getSupportFragmentManager(), "test");
        }

        getSupportFragmentManager().beginTransaction()
            .add(R.id.fragment_host, new AppPreferenceFragment())
            .commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        grantResults.toString();
    }
}
