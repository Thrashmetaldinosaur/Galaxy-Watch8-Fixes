package app.revanced.extension.watch8;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;

public final class DensityPatch {
    private static final String TAG = "Watch8AppDensity";
    private static volatile int targetDpi = 170;
    private static boolean initialized = false;

    private DensityPatch() {}

    public static synchronized void init(final Application app, int dpi) {
        if (initialized) return;
        if (dpi < 96 || dpi > 640) return;
        targetDpi = dpi;
        try {
            apply(app.getResources());
            app.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
                @Override public void onActivityPreCreated(Activity a, Bundle state) {
                    applyActivity(a);
                }
                @Override public void onActivityCreated(Activity a, Bundle state) {
                    applyActivity(a);
                }
                @Override public void onActivityStarted(Activity a) {}
                @Override public void onActivityResumed(Activity a) {
                    applyActivity(a);
                }
                @Override public void onActivityPaused(Activity a) {}
                @Override public void onActivityStopped(Activity a) {}
                @Override public void onActivitySaveInstanceState(Activity a, Bundle state) {}
                @Override public void onActivityDestroyed(Activity a) {}
            });
            app.registerComponentCallbacks(new ComponentCallbacks2() {
                @Override public void onConfigurationChanged(Configuration configuration) {
                    apply(app.getResources());
                }
                @Override public void onLowMemory() {}
                @Override public void onTrimMemory(int level) {}
            });
            initialized = true;
            Log.i(TAG, "Per-app density initialized: " + targetDpi);
        } catch (Throwable t) {
            Log.e(TAG, "Density hook init failed", t);
        }
    }

    private static void applyActivity(Activity activity) {
        try {
            Resources resources = activity.getResources();
            apply(resources);
            Context base = activity.getBaseContext();
            if (base != null && base.getResources() != resources) {
                apply(base.getResources());
            }
        } catch (Throwable t) {
            Log.e(TAG, "Density hook activity failed", t);
        }
    }

    @SuppressWarnings("deprecation")
    private static void apply(Resources resources) {
        try {
            if (resources == null) return;
            DisplayMetrics current = resources.getDisplayMetrics();
            if (current.densityDpi == targetDpi) return;
            float scale = (float) targetDpi / 160.0f;
            DisplayMetrics metrics = new DisplayMetrics();
            metrics.setTo(current);
            metrics.densityDpi = targetDpi;
            metrics.density = scale;
            metrics.scaledDensity = scale * resources.getConfiguration().fontScale;
            Configuration config = new Configuration(resources.getConfiguration());
            config.densityDpi = targetDpi;
            resources.updateConfiguration(config, metrics);
        } catch (Throwable t) {
            Log.e(TAG, "Density hook update failed", t);
        }
    }
}
