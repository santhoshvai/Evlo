package info.santhosh.evlo;

import android.app.Application;
import android.content.Intent;

import info.santhosh.evlo.common.DebugUtils;
import info.santhosh.evlo.service.GetXmlService;

/**
 * Created by santhoshvai on 28/11/2016.
 */

public class EvloApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        DebugUtils.init(this);
        startService(new Intent(this, GetXmlService.class));
    }
}
