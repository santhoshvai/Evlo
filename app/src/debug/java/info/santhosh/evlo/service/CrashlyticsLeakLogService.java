package info.santhosh.evlo.service;

import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.squareup.leakcanary.AnalysisResult;
import com.squareup.leakcanary.DisplayLeakService;
import com.squareup.leakcanary.HeapDump;
import com.squareup.leakcanary.LeakTraceElement;

import java.util.Locale;

public class CrashlyticsLeakLogService extends DisplayLeakService {

    // https://gist.github.com/pyricau/06c2c486d24f5f85f7f0 (Inspiration)

    private static final String LEAKINFO = "LEAKINFO";
    private static final String TAG = "CrashlyticsLeakLog";

    private static String classSimpleName(String className) {
        int separator = className.lastIndexOf('.');
        return separator == -1 ? className : className.substring(separator + 1);
    }

    @Override
    protected void afterDefaultHandling(HeapDump heapDump, AnalysisResult result, String leakInfo) {
        if (!result.leakFound || result.excludedLeak) {
            return;
        }

        leakInfo = leakInfo.replaceAll("(\r\n|\n)", "\\&#13;\\&#10;");
        Log.d(TAG, leakInfo);
        Crashlytics.setString(LEAKINFO, leakInfo);
//        for(String s : leakInfo.split("\n")) {
//            Crashlytics.log(s);
//        }
        Crashlytics.logException(exception(result));
        Log.i(TAG, "leak sent to crashlytics");
    }

    private RuntimeException exception(AnalysisResult result) {
        final int size = result.leakTrace.elements.size();
        StackTraceElement[] stackTrace = new StackTraceElement[size];
        for (int i = 0; i < size; i++) {
            final LeakTraceElement leakTraceElement = result.leakTrace.elements.get(i);
            String methodName = (i == size - 1) ? "leaking" : leakTraceElement.referenceName;
            // com.example.android.Mainactivity.foo
            // {com.example.android.Mainactivity, foo, Mainactivity.java, 7}
            stackTrace[i] = new StackTraceElement(
                    leakTraceElement.className, // declaring class
                    methodName, // method name
                    classSimpleName(leakTraceElement.className) + ".java", // filename
                    7); // line number (it is not known, so a random number)
        }
        // apiactivity leak from mainthreadenforcer (holder=CLASS, type=STATIC_FIELD)
        final LeakTraceElement firstElement = result.leakTrace.elements.get(0);
        final String title = String.format(Locale.US, "%s leak from %s (holder=%s, type=%s)",
                classSimpleName(result.className),
                classSimpleName(firstElement.className),
                firstElement.holder.name(),
                firstElement.type.name());
        final RuntimeException e = new RuntimeException(title);
        e.setStackTrace(stackTrace);
        return e;
    }
}
