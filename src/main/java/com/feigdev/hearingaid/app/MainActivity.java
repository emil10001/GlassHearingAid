package com.feigdev.hearingaid.app;

import android.app.Activity;
import android.media.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;


public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private AsyncTask lpt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);

        if (null == lpt || lpt.isCancelled()) {
            lpt = new ListenPlayTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void onDestroy() {
        if (null != lpt)
            lpt.cancel(true);
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    private class ListenPlayTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            Log.d(TAG, "ListenPlayTask - doInBackground");
            try {
                // http://stackoverflow.com/a/4666421/974800
                boolean isRecording = true;
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                int buffersize = AudioRecord.getMinBufferSize(11025, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                AudioRecord arec = new AudioRecord(MediaRecorder.AudioSource.MIC, 11025, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, buffersize);
                AudioTrack atrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, 11025, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, buffersize, AudioTrack.MODE_STREAM);
                atrack.setPlaybackRate(11025);
                byte[] buffer = new byte[buffersize];
                arec.startRecording();
                atrack.setStereoVolume(AudioTrack.getMaxVolume(), AudioTrack.getMaxVolume());
                atrack.play();

                while (isRecording) {
                    arec.read(buffer, 0, buffersize);
                    atrack.write(buffer, 0, buffer.length);
                    if (isCancelled())
                        break;
                }

                Log.d(TAG, "task cancelled");
                arec.stop();
                atrack.stop();
            } catch (Exception e){
                Log.e(TAG,"something failed",e);
            }
            return null;
        }
    }

}
