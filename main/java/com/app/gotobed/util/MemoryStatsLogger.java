package com.app.gotobed.util;

import java.text.DecimalFormat;

import android.os.Debug;
import android.util.Log;

import com.app.gotobed.CordovaApp;

public class MemoryStatsLogger {

	public static void logMemory() {
		Double allocated = Double.valueOf(
				Debug.getNativeHeapAllocatedSize())
				/ Double.valueOf((1048576));
		Double available = Double.valueOf(Debug.getNativeHeapSize()) / 1048576.0;
		Double free = Double.valueOf(Debug.getNativeHeapFreeSize()) / 1048576.0;
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		df.setMinimumFractionDigits(2);

		Log.d(CordovaApp.BCG,
				"debug.heap native: allocated "
						+ df.format(allocated)
						+ "MB of "
						+ df.format(available)
						+ "MB ("
						+ df.format(free)
						+ "MB free) ... "
						+ "debug.memory: allocated: "
						+ df.format(Double.valueOf(Runtime.getRuntime()
								.totalMemory() / 1048576))
						+ "MB of "
						+ df.format(Double.valueOf(Runtime.getRuntime()
								.maxMemory() / 1048576))
						+ "MB ("
						+ df.format(Double.valueOf(Runtime.getRuntime()
								.freeMemory() / 1048576)) + "MB free)");
	}
	
}
