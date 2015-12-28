package com.swg.simplecalendarview;

import java.text.SimpleDateFormat;
import java.util.Date;
import android.app.Activity;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;
import com.swg.simplecalendarview.CalendarView.DayItemClickListener;

public class CalendarActivity extends Activity{
	
	private CalendarView calendar;
	
	private SimpleDateFormat format;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_day_choose);
		
		// 设置窗口的显示位置等
		WindowManager m = getWindowManager();
		Display d = m.getDefaultDisplay(); // 为获取屏幕宽、高
		LayoutParams p = getWindow().getAttributes(); // 获取对话框当前的参数值
		p.height = (int) (d.getHeight() * 0.52); // 高度设置为屏幕的1.0
		p.width = (int) (d.getWidth() * 1.0); // 宽度设置为屏幕的0.8
		p.alpha = 1.0f; // 设置本身透明度
		p.dimAmount = 0.0f; // 设置黑暗度
		getWindow().setAttributes(p); // 设置生效
		getWindow().setGravity(Gravity.LEFT); //设置靠右对齐
		
		format = new SimpleDateFormat("yyyy-MM-dd");
		//获取日历控件对象
		calendar = (CalendarView)findViewById(R.id.calendar);
		//设置控件监听，可以监听到点击的每一天
		calendar.setDayItemClickListener(new DayItemClickListener() {
			
			@Override
			public void OnDayItemClick(Date selectedDay) {
				Toast.makeText(CalendarActivity.this, format.format(selectedDay), Toast.LENGTH_SHORT).show();
				finish();
			}
		});
		
	}
}
