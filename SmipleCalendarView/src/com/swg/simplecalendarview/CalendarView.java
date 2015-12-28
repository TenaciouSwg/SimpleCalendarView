package com.swg.simplecalendarview;

import java.util.Calendar;
import java.util.Date;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * 简易日历控件
 * @author gsw
 */
public class CalendarView extends View implements OnTouchListener{
	
	private Surface surface; 
	
	private Calendar calendar; // 日历
	private Date curShowDate; // 当前日历显示的月
	private Date curDate; // 今日日期
	private int[] date = new int[42]; // 日历显示数字
	private int curMonthStartIndex, curMonthEndIndex; // 当月的日历起始和结束索引;
	
	private Bitmap preMonthBitmap; // 上个月按钮图片
	private Bitmap nextMonthBitmap; // 下个月按钮图片
	private int preMonthBtnX; // 上个月按钮起始位置
	private int nextMonthBtnX; // 下个月按钮起始位置
	
	private Bitmap markBitmap; // 标识图片
	
	private boolean isChangeMonth; // 判断是否切换月份
	
	
	private DayItemClickListener dayItemClickListener;
	
	public CalendarView(Context context) {
		super(context);
		init();
	}

	public CalendarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		
		isChangeMonth = false; // 默认为未切换月份
		surface = new Surface();
		surface.density = getResources().getDisplayMetrics().density;
		
		if(MyApplication.getDate() == null){ // 如果内存中已经有了当前日期,则使用内存中的当前日期。否则使用系统获得的当前日期
			curShowDate = new Date();  // 当前日期
			MyApplication.setDate(curShowDate);
		}else {
			curShowDate = MyApplication.getDate();
		}
		
		curDate = new Date();
		calendar = Calendar.getInstance();   // 创建 Calendar 对象 
		calendar.setTime(curShowDate); // 设置当前日期
		
		setOnTouchListener(this);
		
		markBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.day_selected); // 获取标识图片
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		surface.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.88);
		surface.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.45);
		widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(surface.width,
				View.MeasureSpec.EXACTLY);
		heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(surface.height,
				View.MeasureSpec.EXACTLY);
		setMeasuredDimension(widthMeasureSpec, heightMeasureSpec); // 设置控件的大小
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		if (changed) {
			surface.initSurface();// 重新初始化
		} 
		super.onLayout(changed, left, top, right, bottom);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		
		calendar.setTime(curShowDate); // 重置当前日期
		// 1、画月份
		int monthNum = calendar.get(Calendar.MONTH) + 1; // Java 月份从0开始
		String monthStr = "";
		if(monthNum < 10){
			monthStr += calendar.get(Calendar.YEAR) + "-0" + monthNum;
		}else {
			monthStr = calendar.get(Calendar.YEAR) + "-" + monthNum;
		}
		surface.monthPaint.getTextBounds(monthStr, 0, monthStr.length(), surface.mTextBound); // 计算画月份所需要的范围
		FontMetricsInt monthFontMetrics = surface.monthPaint.getFontMetricsInt();  
	    int monthBaseline = (int) ((surface.monthHeight - monthFontMetrics.bottom + monthFontMetrics.top) / 2 - monthFontMetrics.top);  // 计算文字绘制的 baseline
	    canvas.drawText(monthStr, surface.width/2 - surface.mTextBound.width()/2, monthBaseline, surface.monthPaint); // 文本居中显示
		
	    // 2、画上下月份切换按钮
	    preMonthBitmap = getDefalutPreMonthBitmap(); // 获取上一个月按钮图片
	    nextMonthBitmap = getDefalutNextMontBitmap(); // 获取下一个月按钮图片
	    preMonthBtnX = surface.width/2 - surface.mTextBound.width()/2 -preMonthBitmap.getWidth() + - surface.width/6;
	    nextMonthBtnX = surface.width/2 + surface.mTextBound.width()/2 + surface.width/6;
	    canvas.drawBitmap(preMonthBitmap,preMonthBtnX , surface.monthHeight/2 - preMonthBitmap.getHeight()/2, surface.monthPaint);
	    canvas.drawBitmap(nextMonthBitmap,nextMonthBtnX, surface.monthHeight/2 - preMonthBitmap.getHeight()/2, surface.monthPaint);
	    
		// 3、画框
		canvas.drawPath(surface.boxPath, surface.borderPaint);
		
		// 4、画星期
		// 4.1、画星期背景
		canvas.drawRect(0, surface.monthHeight, surface.width,surface.monthHeight + surface.weekHeight, surface.weekBgPaint);
		// 4.2、画星期文字
		float weekTextX = 0f;
		FontMetricsInt weekFontMetrics = surface.weekTextPaint.getFontMetricsInt();  
	    int weekBaseline =  (int) (surface.monthHeight + (surface.weekHeight - weekFontMetrics.bottom + weekFontMetrics.top) / 2 - weekFontMetrics.top);  // 计算文字绘制的 baseline
		for (int i = 0; i < surface.WeekText.length; i++) {
			// 计算了描绘字体需要的范围  
			surface.weekTextPaint.getTextBounds(surface.WeekText[i], 0, surface.WeekText[i].length(), surface.mTextBound);
			weekTextX = surface.cellWidth/2 - surface.mTextBound.width()/2 + surface.cellWidth * i;
			canvas.drawText(surface.WeekText[i],weekTextX,weekBaseline,surface.weekTextPaint); // 文字居中显示
		}
		
		// 5画日期
		generateDate(); // 计算日期
		
		for (int i = 0; i < date.length; i++) {
			// 设置画笔颜色
			int color;
			if(isNextMonth(i) || isPreMonth(i)){ // 如果是上一个月或者下一个月的日期
				color = surface.preAndNextMonthDayColor;
			}else {
				color = surface.curMonthDayColor;
			}
			drawCellText(canvas, i, date[i] + "", color);
		}
		
		calendar.setTime(curShowDate); // 重置当前日期
		
		// 6、画被选中的日期标识
		if(!isChangeMonth){ // 未切换月份时才显示标识
			drawMarkBg(canvas, getIndex(calendar.get(Calendar.DATE)), markBitmap);
		}else if (isChangeMonth && calendar.get(Calendar.MONTH) == curDate.getMonth()) { // 切换到当月后显示标识今日
			drawMarkBg(canvas, getIndex(curDate.getDate()), markBitmap);
		}
		
		super.onDraw(canvas);
	}
	
	/**
	 * 通过坐标设置被选中的日期、前后一个月份的切换
	 * @param x
	 * @param y
	 */
	private void setSelectedDateByCoor(float x,float y){
		// 判断是否是月份切换
		int changeMonth = 0; // -1 : 切换到上一个月 ； 1：切换到下一个月
		if(y <= surface.monthHeight){
			if(x > preMonthBtnX - 50 && x < preMonthBtnX + preMonthBitmap.getWidth() + 50){
				changeMonth = -1;
			}else if (x > nextMonthBtnX - 50 && x < nextMonthBtnX + nextMonthBitmap.getWidth() + 50) {
				changeMonth = 1;
			}
		}
		
		int xIndex = (int) Math.ceil(x/surface.cellWidth); // 1,2,3,4,5,6,7
		int yIndex = (int) Math.ceil((y-surface.monthHeight - surface.weekHeight)/surface.cellHeight); // 1,2,3,4,5,6 
		int index = (yIndex-1) * 7 + (xIndex - 1); // 获取在 date[] 数组中对应的索引
		if(index >= 0){
			if(isNextMonth(index)){ // 如果是上一个月或者下一个月的日期，则切换月份
				changeMonth = 1;
			}else if (isPreMonth(index)) {
				changeMonth = -1;
			}else { // 当月日期被选中
				isChangeMonth = false; // 未切换月份
				calendar.set(Calendar.DAY_OF_MONTH, date[index]);
			}
		}
		
		// 切换到哪个月份月份
		if(changeMonth == -1){
			calendar.add(Calendar.MONTH, -1);
		}else if(changeMonth == 1){
			calendar.add(Calendar.MONTH, 1);
		}
		
		curShowDate = calendar.getTime(); // 重置当前日期
		
		// 判断是否切换月份
		if(changeMonth != 0){
			isChangeMonth = true;
			MyApplication.setDate(null);
		}else {
			MyApplication.setDate(curShowDate);
		}
		
		invalidate(); // 重绘，只重绘那些需要重绘的部分
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN: // 按下
			setSelectedDateByCoor(event.getX(), event.getY()); // 响应触摸事件
			break;
		case MotionEvent.ACTION_UP:
			if(!isChangeMonth){
				dayItemClickListener.OnDayItemClick(curShowDate); // 触发回调
			}
			break;
		default:
			break;
		}
		return true;
	}
	
	/**
	 * 根据日期获取索引
	 * @param day
	 * @return
	 */
	private int getIndex(int day){
		return curMonthStartIndex + day - 1;
	}

	/**
	 * 画选中日期标识背景图
	 * @param canvas
	 * @param index
	 * @param text
	 */
	private void drawMarkBg(Canvas canvas, int index,Bitmap bitmap) {
		int x = getXByIndex(index);
		int y = getYByIndex(index);
		float cellY = surface.monthHeight + surface.weekHeight + (y - 1)
				* surface.cellHeight;
		float cellX = surface.cellWidth * (x - 1);
		if(bitmap != null){
			Bitmap circle = Bitmap.createScaledBitmap(bitmap, (int)surface.cellWidth,(int)surface.cellHeight, true);
			canvas.drawBitmap(circle, cellX, cellY, surface.datePaint);
		}
	}
	
	/**
	 * 计算日期
	 */
	private void generateDate() {
		calendar.set(Calendar.DAY_OF_MONTH, 1); // 设置日期为 1
		/**
		 * SUNDAY -> SATURDAY (1 -> 7)
		 */
		int curMonthStart = 1;
		int dayInWeek = calendar.get(Calendar.DAY_OF_WEEK); // 获取每月 1 号的为星期几
		int curMonthTotalDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH); // 获取当月有多少天
		// 设置本月日期
		curMonthStartIndex = dayInWeek - 1; // 当月日期起始位置的索引
		curMonthEndIndex = curMonthStartIndex + curMonthTotalDay; //当月日期结束位置的索引
		for (int i = curMonthStartIndex; i < curMonthTotalDay + curMonthStartIndex; i++) { // date[] 坐标从0开始,而星期日坐标为 1
			date[i] = curMonthStart++;
		}
		// 设置上一个月的日期
		if(dayInWeek > 1){
			calendar.set(Calendar.DAY_OF_MONTH, 0);
			int dayInPreMonth = calendar.get(Calendar.DAY_OF_MONTH); // 获取前一个月份的总天数
			for (int i = dayInWeek-2; i >= 0; i--) {
				date[i] = dayInPreMonth--;
			}
		}
		// 设置下一个月的日期
		int nextMonthStart = 1;
		int nextMonthStartIndex = dayInWeek -1 + curMonthTotalDay;
		for (int i = nextMonthStartIndex; i < date.length; i++) { 
			date[i] = nextMonthStart++;
		}
	}
	
	/**
	 * 画日期文字
	 * @param canvas
	 * @param index 日期数组索引
	 * @param text  日期文本
	 * @param color 画笔颜色
	 */
	private void drawCellText(Canvas canvas, int index, String text, int color) {
		
		int x = getXByIndex(index);
		int y = getYByIndex(index);
		float dateTextX = 0;
		FontMetricsInt fontMetrics = surface.datePaint.getFontMetricsInt();  
		int baseline =(int) (surface.monthHeight +  ((surface.cellHeight - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top) + surface.cellHeight * y);  // 计算文字绘制的 baseline
	    surface.datePaint.getTextBounds(text, 0, text.length(),surface.mTextBound);
	    dateTextX = surface.cellWidth/2 - surface.mTextBound.width()/2 + surface.cellWidth * (x-1);
	    surface.datePaint.setColor(color);
		canvas.drawText(text,dateTextX,baseline,surface.datePaint); // 文字居中显示
	}
	
	/**
	 * 获取横向数第几个单元格
	 * @param index 数组索引
	 * @return
	 */
	private int getXByIndex(int index){
		return index % 7 + 1; // 1,2,3,4,5,6,7(index 从 0 开始)
	}
	
	/**
	 * 获取纵向数第几个行
	 * @param index 数组索引
	 * @return
	 */
	private int getYByIndex(int index){
		return index / 7 + 1; // 1,2,3,4,5,6
	}

	/**
	 * 是否是下个月的日期
	 * @param index 日期索引
	 * @return
	 */
	private boolean isNextMonth(int index){
		if(index >= curMonthEndIndex){
			return true;
		}
		return false;
	}
	
	/**
	 * 是否是上个月的索引
	 * @param index 日期索引
	 * @return
	 */
	private boolean isPreMonth(int index){
		if(index < curMonthStartIndex){
			return true;
		}
		return false;
	}
	
	/**
	 * 获取上一个月按钮的图片,如果没有设置图片,则返回默认图片
	 * @return
	 */
	private Bitmap getDefalutPreMonthBitmap(){
		Bitmap bitmap = getPreMonthBitmap();
		if(bitmap == null){
			bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.prev_month);
		}
		return Bitmap.createScaledBitmap(bitmap, (int)surface.monthHeight/3, (int)surface.monthHeight/3, true);
				
	}
	
	/**
	 * 获取下一个月按钮的图片,如果没有设置图片,则返回默认图片
	 * @return
	 */
	private Bitmap getDefalutNextMontBitmap(){
		Bitmap bitmap = getNextMonthBitmap();
		if(bitmap == null){
			bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.next_month);
		}
		return Bitmap.createScaledBitmap(bitmap, (int)surface.monthHeight/3, (int)surface.monthHeight/3, true);
	}
	
	/**
	 * 选中日期回调接口
	 * @author gsw
	 */
	public interface DayItemClickListener{
		public void OnDayItemClick(Date selectedDay);
	}
	
	/**
	 * 设置选中日期回调接口
	 * @param dayItemClickListener
	 */
	public void setDayItemClickListener(DayItemClickListener dayItemClickListener) {
		this.dayItemClickListener = dayItemClickListener;
	}
	
	/**
	 * 画笔等管理类 
	 */
	private class Surface {
		
		// 控件的高度等
		private float density; // 密度
		private int width; // 整个控件宽度
		private int height; // 整个控件高度
		private float weekHeight; // 显示星期的高度
		private float cellWidth; // 日期格宽度
		private float cellHeight; // 日期格高度
		private float monthHeight; // 月份高度、即日历头部高度
		
		private Rect mTextBound; // 文字的所需的范围
		
		// 画月份
		private Paint monthPaint; // 月份画笔
		private int monthColor = Color.BLACK;
		
		// 画框
		private int borderColor = Color.parseColor("#CCCCCC"); // 边框颜色
		private float borderWidth;
		private Paint borderPaint; // 边框画笔
		private Path boxPath; // 边框路径
		
		// 画星期
		private Paint weekBgPaint; // 星期背景画笔
		private int weekBgColor = Color.parseColor("#ff00bcd5"); // 星期背景颜色
		private String[] WeekText = {"Sun","Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
		//private String[] WeekText = {"星期日","星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
		private Paint weekTextPaint; // 星期文字画笔
		
		// 画日期
		private Paint datePaint; // 日期画笔
		private int curMonthDayColor = Color.BLACK; // 当前月日期文字颜色
		private int preAndNextMonthDayColor = Color.parseColor("#CCCCCC"); // 前后一个月的日期文字颜色
		   
		public void initSurface() {
			
			// 初始化边框
			// （1、设置边框画笔）
			borderPaint = new Paint();
			borderPaint.setColor(borderColor);
			borderPaint.setStyle(Style.STROKE); 
			borderWidth = (float)(0.5 * density);
			borderWidth = borderWidth > 1 ? 1 : borderWidth; 
			borderPaint.setStrokeWidth(borderWidth); // 设置边框粗细
			
			// 初始化大小
			float temp = height/7f;
			weekHeight = (float) ((temp + temp * 0.3f) * 0.6);
			monthHeight = (float) (weekHeight * 1.2);
			cellHeight =(height - monthHeight - weekHeight)/6f - borderWidth/7;
			cellWidth = width/7f;
			
			// （2、设置边框路径）
			boxPath = new Path();
			boxPath.moveTo(0, monthHeight);
			boxPath.rLineTo(width, 0); // 将(0,0) 与 (width,0) 连接
			boxPath.moveTo(0, weekHeight + monthHeight);
			boxPath.rLineTo(width, 0);
			for(int i = 0; i < 8; i++){
				boxPath.moveTo(0,monthHeight + weekHeight + i * cellHeight);
				boxPath.rLineTo(width, 0);
				boxPath.moveTo(i * cellWidth, monthHeight);
				boxPath.rLineTo(0, height - monthHeight);
			}
			
			// 初始化星期背景画笔
			weekBgPaint = new Paint();
			weekBgPaint.setColor(weekBgColor);
			weekBgPaint.setAntiAlias(true);
			weekBgPaint.setStyle(Paint.Style.FILL);
			
			// 初始化星期文字画笔
			weekTextPaint = new Paint();
			weekTextPaint.setStyle(Paint.Style.FILL);
			weekTextPaint.setColor(Color.WHITE); 
			weekTextPaint.setAntiAlias(true);
			float weekTextSize = weekHeight * 0.4f;
			weekTextPaint.setTextSize(weekTextSize);
			mTextBound = new Rect();
			
			// 初始化日期画笔
			datePaint = new Paint();
			datePaint.setAntiAlias(true);
			float cellTextSize = cellHeight * 0.4f;
			datePaint.setTextSize(cellTextSize);
			
			// 初始化月份画笔
			monthPaint = new Paint();
			monthPaint.setColor(monthColor);
			monthPaint.setAntiAlias(true);
			float monthTextSize = monthHeight * 0.4f;
			monthPaint.setTextSize(monthTextSize);
			
		}
	}
	
	public Bitmap getPreMonthBitmap() {
		return preMonthBitmap;
	}

	public void setPreMonthBitmap(Bitmap preMonthBitmap) {
		this.preMonthBitmap = preMonthBitmap;
	}

	public Bitmap getNextMonthBitmap() {
		return nextMonthBitmap;
	}

	public void setNextMonthBitmap(Bitmap nextMonthBitmap) {
		this.nextMonthBitmap = nextMonthBitmap;
	}
}
