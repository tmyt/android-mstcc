package net.refy.android.mstcc.ui;


import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.SwitchPreferenceCompat;

import net.refy.android.mstcc.receivers.BootReceiver;
import net.refy.android.mstcc.services.MsTccService;
import net.refy.android.mstcc.R;

/**
 * Created by yutaka on 10/15/2016.
 */
public class AppPreferenceFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);

        // set current value
        ((SwitchPreferenceCompat) findPreference("pref_enabled")).setChecked(isServiceRunning(MsTccService.class));
        ((SwitchPreferenceCompat) findPreference("pref_enable_on_boot")).setChecked(isComponentEnabled(BootReceiver.class));

        // set handler
        ((SwitchPreferenceCompat) findPreference("pref_enabled")).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Intent i = new Intent(getContext().getApplicationContext(), MsTccService.class);
                if ((boolean) newValue) {
                    getContext().getApplicationContext().startService(i);
                } else {
                    getContext().getApplicationContext().stopService(i);
                }
                return true;
            }
        });
        ((SwitchPreferenceCompat) findPreference("pref_enable_on_boot")).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int flag = ((boolean) newValue ?
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
                ComponentName component = new ComponentName(getContext(), BootReceiver.class);
                getContext().getPackageManager().setComponentEnabledSetting(component, flag, PackageManager.DONT_KILL_APP);
                return true;
            }
        });
    }

    private boolean isComponentEnabled(Class<?> componentClass){
        ComponentName component = new ComponentName(getContext(), componentClass);
        return getContext().getPackageManager().getComponentEnabledSetting(component) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
