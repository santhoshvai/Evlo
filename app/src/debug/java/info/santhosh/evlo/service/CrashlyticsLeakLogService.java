package info.santhosh.evlo.service;

import com.crashlytics.android.Crashlytics;
import com.squareup.leakcanary.AnalysisResult;
import com.squareup.leakcanary.DisplayLeakService;
import com.squareup.leakcanary.HeapDump;

public class CrashlyticsLeakLogService extends DisplayLeakService {

    // https://github.com/square/leakcanary/wiki/Customizing-LeakCanary#uploading-to-a-server

    @Override
    protected void afterDefaultHandling(HeapDump heapDump, AnalysisResult result, String leakInfo) {
        if (!result.leakFound || result.excludedLeak) {
            return;
        }
        uploadLeakToServer(result, leakInfo);
    }

    private void uploadLeakToServer(AnalysisResult result, String leakInfo) {
        Crashlytics.logException(result.leakTraceAsFakeException());
    }
}
