package jp.daizu.shoppinglist;

/*
 * ********************************************* *
 * クラス　：ShoppingListFinishDialog
 * 機能　：買物完了ダイアログ
 * 遷移元：買物中画面
 * 遷移先：買い物準備画面
 * ********************************************* *
 */
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

@SuppressLint("SimpleDateFormat")
public class ShoppingListFinishDialog extends Dialog {
	private Context ctxt;
	static MySQLiteOpenHelper helper;

	/* コンストラクタ */
	public ShoppingListFinishDialog(Context context) {
		super(context);
		ctxt = context;
	}

    // =========+=========+=========+=========+=========+=========+
	// 画面読込時処理
    // =========+=========+=========+=========+=========+=========+
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		String function_name = "ShoppingListFinishDialog";
		Log.d(function_name, "START");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_shopping_finish);

		/* メッセージ表示 */
		final SharedPreferences pref = ctxt.getSharedPreferences("preference", Activity.MODE_PRIVATE);
		final String start_time = pref.getString("StartTime", null);
		final String finish_time = nowTime();
		final TextView message = (TextView) findViewById(R.id.message);
		String span = timeDiff(start_time, finish_time); // 経過時間
		if ( span == null ){
			// 16.12.22add 日付またいでいる場合、経過時間を表示しない
			message.setText(
					  ctxt.getString(R.string.time_start) + start_time + "\n"
					+ ctxt.getString(R.string.time_finish) + finish_time);
		}else{
			message.setText(
				  ctxt.getString(R.string.time_start) + start_time + "\n"
				+ ctxt.getString(R.string.time_finish) + finish_time +"\n"
				+ ctxt.getString(R.string.time_span)+ span);
		}

		/* 【OKボタン】押下時 */
		findViewById(R.id.button_ok).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						/* 買物開始時間をnullに戻す */
						SharedPreferences.Editor editor;
						editor = pref.edit();
						editor.putString("StartTime", null);
						editor.commit();

						/* 買物準備画面に遷移 */
						Intent intent = new Intent(ctxt,ShoppingListPrepareActivity.class);
						ctxt.startActivity(intent);
					}
			});
		Log.d(function_name, "END");
	}

    // =========+=========+=========+=========+=========+=========+
	// 【SUB関数】
    // =========+=========+=========+=========+=========+=========+
	/* 現在時刻 */
	private String nowTime(){
		Calendar cal = Calendar.getInstance();
		return (String) DateFormat.format("kk:mm", cal);
	}

	/* 経過時刻を取得する(hh:mm, to-from逆転の場合null) */
	String timeDiff(String s_from, String s_to) {
	    SimpleDateFormat dateformat = new SimpleDateFormat("hh:mm");
	    Date date_from = null;
	    Date date_to   = null;
	    try {
	        date_from = dateformat.parse(s_from);
	        date_to = dateformat.parse(s_to);
	    } catch (ParseException e) {
	        e.printStackTrace();
	    }
	    /* 日付をlong値に変換 */
	    long date_time_from = date_from.getTime();
	    long date_time_to   =   date_to.getTime();
	    long time = date_time_to - date_time_from;
	    if ( time < 0 ){
	    	return null;
	    }

	    long min = ((time  % ( 1000 * 60 * 60)) / 1000 / 60) % 60;
	    long hour = ( time / ( 1000 * 60 * 60));

	    return hour + ":" + String.format("%02d", min);
	}

}



