package com.easemob.helpdesk.service;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;

import com.easemob.helpdesk.HDApplication;

/**
 * Android 5.0+ 使用的 JobScheduler.
 * 运行在 :watch 子进程中.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class JobSchedulerService extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        startService(new Intent(HDApplication.getInstance(), WorkService.class));
        try{
            jobFinished(params, false);
        }catch (Exception ignored){}
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
