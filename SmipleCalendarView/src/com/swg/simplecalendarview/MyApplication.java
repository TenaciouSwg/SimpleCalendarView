package com.swg.simplecalendarview;

import java.util.Date;

import android.app.Application;

public class MyApplication extends Application {

	public static Date date;
	
	public static Date getDate() {
		return date;
	}

	public static void setDate(Date date) {
		MyApplication.date = date;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}
}
