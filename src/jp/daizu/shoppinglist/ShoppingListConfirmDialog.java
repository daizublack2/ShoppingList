package jp.daizu.shoppinglist;


/*      ***  実装中止 ***           */

/*
 * ********************************************* *
 * クラス　：ShoppingListConfirmDialog
 * 機能　：買物出発確認ダイアログ
 * 遷移元：買物準備画面(BuyFoodShowActivity)
 * 遷移先：[出発]買い物中画面　[買物順変更]買物順並び換え画面
 * ********************************************* *
 */
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class ShoppingListConfirmDialog extends Dialog {
	private static LinearLayout linearLayout;
	private static ScrollView scrollView;
	private static Context ctxt;
	private static SharedPreferences pref;
	private static List<BuyCategory> buy_category = new ArrayList<BuyCategory>(); // カテゴリ格納場所
	private static MySQLiteOpenHelper helper;
	private static String function_name = "ShoppingListConfirmDialog";
	private String now = null;

	/* コンストラクタ */
	public ShoppingListConfirmDialog(Context context) {
		super(context);
		ctxt = context;
	}

    // =========+=========+=========+=========+=========+=========+
	// 画面読込時処理
    // =========+=========+=========+=========+=========+=========+
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		String function_name = "ShoppingListBeforeUseDialog";
		Log.d(function_name, "START");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		helper = new MySQLiteOpenHelper(ctxt);
		setContentView(R.layout.dialog_shopping_list_confirm);

		/* 買物順リスト表示 */
        linearLayout = new LinearLayout(ctxt);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

		/* 現在時刻取得 */
		now = nowTime();

		/* ボタンの設定 */
		// 【出発】->買い物中画面
		findViewById(R.id.button_go).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						pref = ctxt.getSharedPreferences("preference", Activity.MODE_PRIVATE);
						SharedPreferences.Editor editor;
						editor = pref.edit();
						editor.putString("StartTime", now);
						editor.commit();

						Intent intent = new Intent(ctxt,ShoppingListUseActivity.class);
						ctxt.startActivity(intent);
					}
			});

		// 【買物順変更】->買物順並び換え画面
		findViewById(R.id.button_change_buy_no).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						Intent intent = new Intent(ctxt,ShoppingListRearrangeShoppingActivity.class);
						ctxt.startActivity(intent);
					}
			});

		// 【戻る】：ダイアログを閉じる
		findViewById(R.id.button_cancel).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						cancel();
					}
			});

		/* 非同期処理 */
		new ShowAsyncShoppingList().execute();

		Log.d(function_name, "END");
	}

    // =========+=========+=========+=========+=========+=========+
	// 買物順リスト表示
    // =========+=========+=========+=========+=========+=========+
	class ShowAsyncShoppingList extends AsyncTask<Void, Integer, Void>{
		int category_id;
		String category_name;

		/* バックグラウンド処理：DBからアイテムを取得 */
		@Override
		protected Void doInBackground(Void... params) {
			buy_category = helper.getCategoryShopping();
			Log.d(function_name, "サイズ：" + buy_category.size());
			return null;
		}

		/* BG処理完了後：UIに表示 */
		@Override
		protected void onPostExecute(Void result){
			super.onPostExecute(result);
			int buy_no = 0;
			Iterator<BuyCategory> ite_cate = buy_category.iterator();
			while (ite_cate.hasNext()) {
				BuyCategory buy_category = (BuyCategory) ite_cate.next(); // 次の行を取得

				int junle_id = buy_category.getJunle_id();				// [取得]ジャンルID
	            buy_no++;												// [取得]買物順
	            String category_name = buy_category.getCategory_name();	// [取得]カテゴリ名

				// [設定]レイアウト
	            int category_layout_id = 0;
	            switch(junle_id){
	            case 0:
	            	category_layout_id = R.layout.sub_shopping_list_confirm_food;
	            	break;
	            case 1:
	            	category_layout_id = R.layout.sub_shopping_list_confirm_supply;
	            	break;
	            case 2:
	            	category_layout_id = R.layout.sub_shopping_list_confirm_other;
	            	break;

	            default:
	            		break;
	            }
	            LayoutInflater inflater = getLayoutInflater();
	            final View itemView = inflater.inflate(category_layout_id,null);
	            final TextView category = (TextView) itemView.findViewById(R.id.category);
	            final TextView number = (TextView) itemView.findViewById(R.id.number);

	            number.setText(Integer.toString(buy_no));			// [設定]買物順
	            category.setText(category_name);					// [設定]カテゴリ名
	            linearLayout.addView(itemView);
			}

	        // 表示
	        scrollView = (ScrollView) findViewById(R.id.scroll_view);
	        scrollView.addView(linearLayout);

			/* メッセージ表示 */
			TextView message = (TextView) findViewById(R.id.message);
			if ( buy_category.size() != 0 ){
				message.setText("["+ now +"] " + ctxt.getString(R.string.confirm_message));
			}else{
				message.setText("品物が選択されていません。買いたい品物にチェックを入れてから出発ボタンを押してください。");
				findViewById(R.id.button_change_buy_no).setEnabled(false);
				findViewById(R.id.button_go).setEnabled(false);
			}
		}
	}

    // =========+=========+=========+=========+=========+=========+
	// 【SUB関数】
    // =========+=========+=========+=========+=========+=========+
	/* 現在時刻 */
	private String nowTime(){
		Calendar cal = Calendar.getInstance();
		return (String) DateFormat.format("kk:mm", cal);
	}


}
