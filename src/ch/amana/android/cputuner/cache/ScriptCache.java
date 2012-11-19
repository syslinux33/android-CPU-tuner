package ch.amana.android.cputuner.cache;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.content.Context;
import ch.amana.android.cputuner.hw.RootHandler;
import ch.amana.android.cputuner.log.Logger;

public class ScriptCache extends Cache {

	private FileWriter writer = null;
	private boolean clearCache = true;

	/* (non-Javadoc)
	 * @see ch.amana.android.cputuner.cache.ICache#removeScripts(android.content.Context)
	 */
	@Override
	public void clear(Context ctx) {
		if (!RootHandler.execute("rm -rf " + getPath(ctx).getAbsolutePath() + "/*")) {
			RootHandler.execute("rm " + getPath(ctx).getAbsolutePath() + "/*");
		}
	}

	private File getPath(Context ctx) {
		return ctx.getFilesDir();
	}

	protected File getFile(Context ctx, long pid) {
		File file = new File(getPath(ctx), pid + ".sh");
		file.setExecutable(true);
		return file;
	}

	/* (non-Javadoc)
	 * @see ch.amana.android.cputuner.cache.ICache#runScript(android.content.Context, long)
	 */
	@Override
	public boolean execute(Context ctx, long pid) {
		return RootHandler.execute(getFile(ctx, pid).getAbsolutePath());
	}

	/* (non-Javadoc)
	 * @see ch.amana.android.cputuner.cache.ICache#hasScript(android.content.Context, long)
	 */
	@Override
	public boolean exists(Context ctx, long pid) {
		return getFile(ctx, pid).exists();
	}

	/* (non-Javadoc)
	 * @see ch.amana.android.cputuner.cache.ICache#startRecording(android.content.Context, long)
	 */
	@Override
	public void startRecording(Context ctx, long pid) {
		if (clearCache) {
			clear(ctx);
			clearCache = false;
		}
		try {
			writer = new FileWriter(getFile(ctx, pid));
		} catch (IOException e) {
			Logger.e("Cannot open FileWriter to script cache for " + pid, e);
			writer = null;
		}
	}

	/* (non-Javadoc)
	 * @see ch.amana.android.cputuner.cache.ICache#endRecording()
	 */
	@Override
	public void endRecording() {
		if (writer != null) {
			try {
				writer.flush();
				writer.close();
				// chmod 4700 *     
			} catch (IOException e) {
				Logger.w("Cannot flush and close script cache writer", e);
			}
		}
		writer = null;
	}

	/* (non-Javadoc)
	 * @see ch.amana.android.cputuner.cache.ICache#isRecoding()
	 */
	@Override
	public boolean isRecoding() {
		return writer != null;
	}

	/* (non-Javadoc)
	 * @see ch.amana.android.cputuner.cache.ICache#recordLine(java.lang.String)
	 */
	@Override
	public void recordLine(String cmd) {
		if (writer == null || cmd == null) {
			Logger.w("Writer should not be null when writing to script cache");
		}
		try {
			Logger.w("Adding line to script: " + cmd);
			writer.write(cmd);
			writer.write("\n");
		} catch (IOException e) {
			Logger.w("Cannot write to script cache writer: " + cmd, e);
		}
	}

}