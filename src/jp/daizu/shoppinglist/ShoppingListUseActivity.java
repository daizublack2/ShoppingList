package jp.daizu.shoppinglist;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
/*
 * ********************************************* *
 * クラス　：ShoppingListUseActivity
 * 機能　：買物中画面
 * 遷移元：
 * 遷移先：
 * ********************************************* *
 */
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/*
 * ********************************************* *
 * クラス　：ShoppingListUseActivity
 * 機能　：買物中画面
 * 遷移元：買物準備画面
 * 遷移先：買物準備画面 / 買物並び換え画面（買物順）
 * ********************************************* *
 */
public class ShoppingListUseActivity extends FragmentActivity {

	//private static boolean test_mode = true;  // テストモード
	private static boolean test_mode = false;  // テストモード解除

	private static LinearLayout linearLayout;
	private static ScrollView scroll_view;
	private static List<BuyCategory> buy_category_list = new ArrayList<BuyCategory>(); // カテゴリ格納場所
	private static List<BuyItem> buy_item_list = new ArrayList<BuyItem>(); // アイテム格納場所
	private static MySQLiteOpenHelper helper;
	private static Context ctxt;
	private static final String function_name = "ShoppingListUseActivity";
	private static ProgressDialog dlg = null;
	private static SharedPreferences pref;
	LinearLayout layout_ad; //広告表示用スペース
	AdView adView;

    // =========+=========+=========+=========+=========+=========+
	// 画面読込時処理
    // =========+=========+=========+=========+=========+=========+
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(function_name, "[START]");
		super.onCreate(savedInstanceState);
		helper = new MySQLiteOpenHelper(this);
		setContentView(R.layout.activity_shopping_list_use);
		ctxt = this;
		dlg = new ProgressDialog(this);

		/* ヘッダ */
		setTitle(getString(R.string.shoppinglist_use));
		/* 準備リスト表示 */
		linearLayout = new LinearLayout(this);
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		new ShowAsyncShoppingList().execute();

		/* 初回起動時ヘルプを表示 */
		pref = getSharedPreferences("preference", Activity.MODE_PRIVATE);
		if ( pref.getBoolean("first_use_flag", true) == true){
			/* ヘルプ表示 */
			Intent intent_help = new Intent(ctxt,ShoppingListUseHelpActivity.class);
			startActivity(intent_help);

			/* 初回起動フラグをfalseに変更 */
			SharedPreferences.Editor editor;
			editor = pref.edit();
			editor.putBoolean("first_use_flag", false);
			editor.commit();
		}

		/* 広告表示 */
		showAdMob();

		Log.d(function_name, "[END]");
	}

    // =========+=========+=========+=========+=========+=========+
	// メイン画面描画
    // =========+=========+=========+=========+=========+=========+
	/* 非同期で買物リストを表示 */
	class ShowAsyncShoppingList extends AsyncTask<Void, Integer, Void>{
		int category_id;
		String category_name;

		/* プログレスダイアログの表示内容を更新(非同期処理開始時) */
		@Override
		protected void onPreExecute(){
			super.onPreExecute();
			super.onPreExecute();
			dlg.setTitle(getString(R.string.waiting)); // ダイアログ上部に表示される文字列
			dlg.setMessage(getString(R.string.now_loading));     // ユーザに進捗状況をお知らせする箇所
			dlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);    // くるくるで処理中であることを表現
			dlg.setMax(1);           // 処理の最大値
			dlg.setCancelable(false);
			dlg.show();
		}

		/* バックグラウンド処理：DBからアイテムを取得 */
		@Override
		protected Void doInBackground(Void... params) {
			/* databaseからカテゴリ情報を取得 */
			buy_category_list = helper.getCategoryShopping();

			/* databaseからアイテム情報を取得しbuyitem配列に設定 */
			buy_item_list = helper.getItemUse();
			return null;
		}

		/* BG処理完了後：UIに表示 */
		@Override
		protected void onPostExecute(Void result){
			super.onPostExecute(result);
			dlg.setMessage(getString(R.string.making_view));
			int category_id;
			Iterator<BuyCategory> ite_cate = buy_category_list.iterator();
			while (ite_cate.hasNext()) {
				/* カテゴリ出力 */
				final BuyCategory buycategory = (BuyCategory) ite_cate.next(); 	// [取得]次のカテゴリ情報
				final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

				int junle_id = buycategory.getJunle_id();
				int resource = R.layout.sub_shopping_list_use_category_food;
				switch(junle_id){
				case 0:
					resource = R.layout.sub_shopping_list_use_category_food;
					break;
				case 1:
					resource = R.layout.sub_shopping_list_use_category_supply;
					break;
				case 2:
					resource = R.layout.sub_shopping_list_use_category_other;
					break;
				default:
					break;

				}
				View categoryView = inflater.inflate(resource, null);
				final TextView textv = (TextView) categoryView.findViewById(R.id.category);					// [取得]カテゴリ名表示位置
				textv.setText(buycategory.getCategory_name());	// [設定]カテゴリ名
				linearLayout.addView(categoryView);				// [表示]カテゴリ

				/* アイテム出力 */
				category_id = buycategory.getId(); 								// [取得]カテゴリID
				final Iterator<BuyItem> ite_item = buy_item_list.iterator(); 	// アイテム用イテレータ
				while (ite_item.hasNext()) {
					BuyItem buyitem = (BuyItem) ite_item.next();				// [取得]次のアイテム情報
					if (buyitem.getCategory_id() != category_id) {
						continue;
					}
					final View itemView = inflater.inflate(R.layout.sub_shopping_list_use_item, null);	// [取得]アイテム行のレイアウト
					Resources resources = getResources();
					switch(junle_id){
					case 0:
						itemView.setBackgroundColor(resources.getColor(R.color.foodpale_color));
						break;
					case 1:
						itemView.setBackgroundColor(resources.getColor(R.color.supplypale_color));
						break;
					case 2:
						itemView.setBackgroundColor(resources.getColor(R.color.otherpale_color));
						break;
					default:
						break;

					}
					final CheckBox check_name = (CheckBox) itemView.findViewById(R.id.item);	// [取得]アイテム名表示位置
					final TextView memo = (TextView) itemView.findViewById(R.id.memo);			// [取得]メモ表示位置
					check_name.setText(buyitem.getItem_name());	// [設定]アイテム名
					memo.setText(buyitem.getMemo());			// [設定]メモ
					buyitem.setCheckbox(check_name);			// [設定]チェックボックスのチェック有無を取得するため
					linearLayout.addView(itemView);				// [表示]アイテム行
				}
			}
			scroll_view = (ScrollView) findViewById(R.id.scroll_view);
			scroll_view.addView(linearLayout);
			dlg.dismiss();
		}
	}

    // =========+=========+=========+=========+=========+=========+
	// ボタン
    // =========+=========+=========+=========+=========+=========+
	/* 購入確定&買物完了ボタン */
	public void clickButton(View v) {
		/* 保存 */
		int err_cnt = helper.updateShoppingItem(buy_item_list);
		/* 保存失敗時 */
		if ( err_cnt > 0 ){
			String msg = pref.getString("dbError", "");
			Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Sorry..");
			builder.setMessage(err_cnt + getString(R.string.toast_save_failed_cnt) + "\n" + msg);
			builder.setPositiveButton(getString(R.string.ok),null);
			AlertDialog alert_dialog = builder.create();
			alert_dialog.show();
		}

		switch (v.getId()) {
		/* 購入確定 -> 画面再読込 */
		case R.id.button_fix:
			Log.d(function_name, "[START]button_fix");
			scroll_view.removeAllViews(); 	// [クリア]メイン画面
			linearLayout = new LinearLayout(this);
			linearLayout.setOrientation(LinearLayout.VERTICAL);
			new ShowAsyncShoppingList().execute();
			break;

		/* 購入完了　-> 買物完了確認ダイアログ */
		case R.id.button_finish:
			Log.d(function_name, "[START]button_finish");
			showBeforeExitDialog();
			break;
		}
	}

	/* 3-3 メニューボタン */
	public void clickMenuButton(View v) {
		Log.d(function_name, "[START]clickMenuButton");
		/* 表示するアイテム */
		String[] items = new String[]{
				getString(R.string.menu_rearrange_shopping), // 買物順変更
				getString(R.string.menu_share),				 // 共有
				getString(R.string.menu_compare_price),		 // 価格比較
				getString(R.string.menu_help)};				 // ヘルプ

		/* ダイアログ表示 */
		Builder builder = new AlertDialog.Builder(this);
		builder.setItems(items,new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int id) {
				switch(id){
				/* 買物順変更 */
				case 0:
					Log.d(function_name, "[START]menu_rearrange_shopping");
					showShoppingRearrangeDialog();
					break;
				/* 共有 */
				case 1:
					Log.d(function_name, "[START]menu_share");
					/* 共有先に渡すテキスト */
					String text = "";
					Iterator<BuyCategory> ite_cate = buy_category_list.iterator();
					while (ite_cate.hasNext()) {
						BuyCategory buycategory = (BuyCategory) ite_cate.next(); 	// 次の行を取得
						int category_id = buycategory.getId();						// カテゴリID
						/* アイテム出力 */
						Iterator<BuyItem> ite_item = buy_item_list.iterator(); 		// アイテム用イテレータ
						while (ite_item.hasNext()) {
							BuyItem buyitem = (BuyItem) ite_item.next();
							if (buyitem.getCategory_id() != category_id) {
								continue;
							}
							String memo = buyitem.getMemo();
							text = text + "\n" + buyitem.getItem_name() ;
							if ( memo.length() > 0 ){
								text = text + " (" + memo + ")";
							}
						}
					}
					/* 共有 */
					Intent intent = new Intent(Intent.ACTION_SEND);
					intent.setType("text/plain");
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 別Taskで起動
					intent.putExtra(Intent.EXTRA_TEXT, text);
					startActivity(intent);
					break;

				/* 価格比較 */
				case 2:
					Log.d(function_name, "[START]menu_compare_price");
					ShoppingListComparePriceDialog compare_dialog = new ShoppingListComparePriceDialog(ctxt);
					compare_dialog.show();
					break;

				/* ヘルプ */
				case 3:
					Log.d(function_name, "[START]menu_help");
					Intent intent_help = new Intent(ctxt, ShoppingListUseHelpActivity.class);
					startActivity(intent_help);
					break;

				/* その他　※とおらない */
				default:
					break;
				}
			}

		});

		/* ダイアログを表示 */
		AlertDialog alert_dialog = builder.create();
		alert_dialog.show();

	}

	/* バックキー ->買い物中画面破棄終了ダイアログ表示 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ( keyCode == KeyEvent.KEYCODE_BACK){
			showShoppingCancelDialog();
		}
		return super.onKeyDown(keyCode, event);
	}

    // =========+=========+=========+=========+=========+=========+
	// ダイアログ
    // =========+=========+=========+=========+=========+=========+
	/* 3-2 買物完了確認ダイアログ　[OK]->買物完了ダイアログを表示 */
	private void showBeforeExitDialog(){
		/* 確認ダイアログを表示 */
		Builder builder = new AlertDialog.Builder(this);

		/* メッセージ */
		builder.setMessage(getString(R.string.exit_message));

		/* 【OK】の場合、終了ダイアログ表示 */
		builder.setPositiveButton(getString(R.string.ok),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface d, int id) {
						/* 買物終了ダイアログ表示 */
						ShoppingListFinishDialog dialog = new ShoppingListFinishDialog(ctxt);
						dialog.show();
						/* 戻る/ダイアログ外でキャンセルされても画面遷移 */
						dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
					        public void onCancel(DialogInterface dialog) {
								/* 買物開始時間をnullに戻す */
								final SharedPreferences pref = getSharedPreferences("preference", Activity.MODE_PRIVATE);
								SharedPreferences.Editor editor;
								editor = pref.edit();
								editor.putString("StartTime", null);
								editor.commit();

								/* 画面遷移(買物準備画面) */
								Intent intent = new Intent(ctxt, ShoppingListPrepareActivity.class);
								startActivity(intent);
								finish();
					        }
					    });
					}
				});

		/* 【CANCEL】->閉じる */
		builder.setNegativeButton(getString(R.string.cancel), null);

		/* ダイアログを表示 */
		AlertDialog alert_dialog = builder.create();
		alert_dialog.show();
	}

	/* 3-4 破棄終了ダイアログ表示（【OK】->保存せずに買物準備画面へ）　*/
	private void showShoppingCancelDialog(){
		/* 確認ダイアログを表示 */
		Builder builder = new AlertDialog.Builder(this);

		/* メッセージ */
		builder.setMessage(getString(R.string.exit_cancel_message));

		/* 【OK】 */
		builder.setPositiveButton(getString(R.string.ok),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						/* 買物開始時間をnullに戻す */
						final SharedPreferences pref = getSharedPreferences("preference", Activity.MODE_PRIVATE);
						SharedPreferences.Editor editor;
						editor = pref.edit();
						editor.putString("StartTime", null);
						editor.commit();

						/* 画面遷移(買物準備画面) */
						Intent intent = new Intent(ctxt, ShoppingListPrepareActivity.class);
						startActivity(intent);
						finish();
					}
				});

		/* 【CANCEL】->閉じる */
		builder.setNegativeButton(getString(R.string.cancel), null);

		/* ダイアログを表示 */
		AlertDialog alert_dialog = builder.create();
		alert_dialog.show();

	}

	/* 3-5 買物順変更前確認ダイアログ */
	private void showShoppingRearrangeDialog(){
		/* 確認ダイアログを表示 */
		Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.show_rearrange_message));
		/* 【OK】の場合、買物順変更画面に遷移する */
		builder.setPositiveButton(getString(R.string.ok),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						Intent intent = new Intent(ctxt,ShoppingListRearrangeShoppingActivity.class);
						startActivity(intent);
						finish();
					}
				});
		/* 【cancel】の場合、何もせずに閉じる */
		builder.setNegativeButton(getString(R.string.cancel), null);
		AlertDialog dialog = builder.create();
		dialog.show(); // ダイアログを表示
	}

    // =========+=========+=========+=========+=========+=========+
	// SUB関数
    // =========+=========+=========+=========+=========+=========+
	/* 広告表示 */
	private void showAdMob(){
	      adView = new AdView(this);
	      adView.setAdUnitId(getString(R.string.google_adsense_id));
	      adView.setAdSize(AdSize.BANNER);

	      layout_ad = (LinearLayout) findViewById(R.id.layout_ad);
	      layout_ad.addView(adView);

	      AdRequest adRequest;
	      if ( test_mode == true ){
		        // テストモード
		         adRequest = new AdRequest.Builder()
		        		 .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
		        		 .addTestDevice(getDeviceID(ctxt)).build();
	      }else{
		      adRequest = new AdRequest.Builder().build();
	      }

	      /* 広告表示 */
	      adView.loadAd(adRequest);
	}

	/* [SUB]テスト広告表示用ID取得 */
	private String getDeviceID(Context context){
		Log.d(function_name, "[START]getDeviceID");

		String aid = Settings.Secure.getString(context.getContentResolver(), "android_id");

		Object obj = null;
		try {
			((MessageDigest) (obj = MessageDigest.getInstance("MD5"))).update(aid.getBytes(), 0, aid.length());
			obj = String.format("%032X", new Object[] { new BigInteger(1,((MessageDigest) obj).digest()) });
		} catch (NoSuchAlgorithmException localNoSuchAlgorithmException) {
			obj = aid.substring(0, 32);
		}
		Log.d(function_name, "getDeviceID:" + (String)obj);
		return (String)obj;
	}

	   @Override
	   public void onPause() {
	      adView.pause();
	      super.onPause();
	   }

	   @Override
	   public void onResume() {
	      super.onResume();
	      adView.resume();
	   }

	   @Override
	   public void onDestroy() {
	      adView.destroy();
	      super.onDestroy();
	   }

}
