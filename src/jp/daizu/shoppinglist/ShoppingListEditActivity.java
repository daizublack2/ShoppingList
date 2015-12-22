package jp.daizu.shoppinglist;

/*
 * ********************************************* *
 * クラス　：ShoppingListEditActivity
 * 機能　：買物リスト編集画面
 * 遷移元：買物準備画面
 * 遷移先：アイテム並び換え画面/カテゴリ並び換え画面
 * ********************************************* *
 */
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;
import au.com.bytecode.opencsv.CSVReader;

public class ShoppingListEditActivity extends Activity {
	private static int junle_id=0;
	private static List<BuyCategory> buy_category_list = new ArrayList<BuyCategory>(); // カテゴリ格納場所
	private static List<BuyItem> buy_item_list = new ArrayList<BuyItem>(); // アイテム格納場所
	private static MySQLiteOpenHelper helper;
	private static Context ctxt;
	private static LinearLayout linear_layout;
	private static ScrollView scroll_view;
	private static ProgressDialog dlg = null; //プログレスダイアログを表示する
	private static SharedPreferences pref;
	private static final String function_name = "ShoppingListEditActivity";

	// =========+=========+=========+=========+=========+=========+
	// 画面読込時処理
	// ---------+---------+---------+---------+---------+---------+
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(function_name, "[START]");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shopping_list_edit);
		helper = new MySQLiteOpenHelper(this);
		dlg = new ProgressDialog(this);
		ctxt = this;

		/* 呼び出し元から【ジャンルID】を取得 */
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
		    junle_id = bundle.getInt("junle_id");
		}

		/* タイトル */
		setTitle(getString(R.string.shoppinglist_edit));

		/* ボタンのアクティブ/非アクティブ設定 */
		buttonConfig();
		settingSingleClick();

		/* 買物編集リスト */
		new ShowAsyncMainView().execute();

		/* 初回起動時ヘルプを表示 */
		pref = getSharedPreferences("preference", Activity.MODE_PRIVATE);
		if ( pref.getBoolean("first_edit_flag", true) == true){
			// ヘルプ表示
			Intent intent_help = new Intent(ctxt,ShoppingListEditHelpActivity.class);
			startActivity(intent_help);

			// 初回起動フラグをfalseに変更
			SharedPreferences.Editor editor;
			editor = pref.edit();
			editor.putBoolean("first_edit_flag", false);
			editor.commit();
		}

		Log.d(function_name, "END");
	}

	// =========+=========+=========+=========+=========+=========+
	// 画面表示部分
	// ---------+---------+---------+---------+---------+---------+
	/* 非同期処理：メイン画面描画 */
	class ShowAsyncMainView extends AsyncTask<Void, Integer, Void>{

		/* 非同期処理開始時:プログレスダイアログ表示 */
		@Override
		protected void onPreExecute(){
			super.onPreExecute();
			Log.d(function_name, "[START]ShowAsyncMainView");
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
			buy_category_list = helper.getCategoryEdit(junle_id);	// databaseからカテゴリ情報を取得しbuy_category_list配列に設定
			buy_item_list = helper.getJunleItem(junle_id);			// databaseからアイテム情報を取得しbuy_item_list配列に設定
			return null;
		}

		/* BG処理完了後：UIに表示 */
		@Override
		protected void onPostExecute(Void result){
			super.onPostExecute(result);
			dlg.setMessage(getString(R.string.making_view));
			showList();
			dlg.dismiss();
		}
	}

	// 買物編集リスト表示
	private void showList() {
		/* ScrollView上に準備リストを表示 */
		linear_layout = new LinearLayout(this);
		linear_layout.setOrientation(LinearLayout.VERTICAL);

		/* 親リニアレイアウト設定 */
		final LinearLayout linear_layout_parent = new LinearLayout(this);
		linear_layout_parent.setOrientation(LinearLayout.VERTICAL);
		final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// =========+=========+
		// カテゴリ行表示設定
		// ---------+---------+
		Iterator<BuyCategory> ite_cate = buy_category_list.iterator();
		while (ite_cate.hasNext()) {
			final BuyCategory buycategory = (BuyCategory) ite_cate.next(); // 次の行を取得

			final LinearLayout linear_layout_child = new LinearLayout(this);
			final LinearLayout linear_layout_grandchild = new LinearLayout(this);
			linear_layout_child.setOrientation(LinearLayout.VERTICAL);
			linear_layout_grandchild.setOrientation(LinearLayout.VERTICAL);

			/* カテゴリ行表示 */
			final View categoryView = add_category_line(buycategory,2);	// [作成]カテゴリ行のビュー
			linear_layout_grandchild.addView(categoryView);				// [表示]孫ビューへ

			// =========+=========+
			// アイテム行表示設定
			// ---------+---------+
			final int category_id = buycategory.getId(); // カテゴリID取得
			Iterator<BuyItem> ite_item = buy_item_list.iterator(); // アイテム用イテレータ
			while (ite_item.hasNext()) {
				final BuyItem buyitem = (BuyItem) ite_item.next(); // 次の行を取得
				if (buyitem.getCategory_id() != category_id) {
					continue; // カテゴリに属しているアイテム以外はとばす
				}
				final View itemView = add_item_line(buyitem);	// [作成]アイテム行のビュー
				//Log.d(function_name, "item [" + buycategory.getCategory_name() + "][" + buyitem.getItem_name() + "] category_no[" + buyitem.getCategory_no() + "]");
				linear_layout_grandchild.addView(itemView);		// [表示]孫ビューへ
			}
			// カテゴリ＋アイテムリストを親リニアレイアウトに設定
			linear_layout_child.addView(linear_layout_grandchild); // [表示]孫ビューを子ビューへ

			// =========+=========+
			// アイテムの[追加]ボタン行表示設定
			// ---------+---------+
			final View item_add_view = inflater.inflate(R.layout.sub_shopping_list_edit_item_add, null);	// [作成]アイテム追加ボタン行のビュー
			linear_layout_child.addView(item_add_view);											// [表示]子ビューへ
			/* アイテム追加ボタン押下時 -> 直前にアイテム行を表示 */
			Button item_add_button = (Button) item_add_view.findViewById(R.id.button_add);
			item_add_button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// 配列に追加
	                BuyItem buyitem = new BuyItem();
					buyitem.setCategory_id(category_id);	// [設定]カテゴリID (追加したアイテムの属しているカテゴリID)
					buyitem.setMod_type(1);					// [設定]変更タイプ=追加
					buy_item_list.add(buyitem);					// [追加]BuyItemを配列に追加
					final View itemView = add_item_line(buyitem); // [作成]アイテム行のビュー
					itemView.findViewById(R.id.item).requestFocus();	// 追加したアイテムを即編集可能にする
					linear_layout_grandchild.addView(itemView);	// [表示]孫ビューへ
				}
			});
			// 1カテゴリぶんのビューをリニアレイアウトに追加
			linear_layout_parent.addView(linear_layout_child); // [表示]子ビュー(1カテゴリ分)を親ビュー(カテゴリ追加ボタンのぞくビュー)へ

		}
		linear_layout.addView(linear_layout_parent); // [表示]親ビューを全体ビューへ

		// =========+=========+
		// カテゴリ追加ボタン行表示設定
		// ---------+---------+
		final View category_add_view = inflater.inflate(R.layout.sub_shopping_list_edit_category_add, null);	// [作成]カテゴリ追加ボタン行のビュー
		linear_layout.addView(category_add_view);													// [表示]全体ビューへ
		/* カテゴリ追加ボタン押下時 */
		Button item_reset_button = (Button) category_add_view.findViewById(R.id.button_aadd);
		item_reset_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// レイアウト作成
				final LinearLayout linear_layout_child = new LinearLayout(ctxt);
				final LinearLayout linear_layout_grandchild = new LinearLayout(ctxt);
				linear_layout_child.setOrientation(LinearLayout.VERTICAL);
				linear_layout_grandchild.setOrientation(LinearLayout.VERTICAL);
				linear_layout_parent.addView(linear_layout_child);
				linear_layout_child.addView(linear_layout_grandchild);

				/* カテゴリ配列追加 */
                BuyCategory buycategory = new BuyCategory();				// [作成]新カテゴリ
                buycategory.setMod_type(1);									// [設定]変更タイプ=追加
                final int category_no=buy_category_list.size()+1;		// screen_idは1から連番のため要素数+1
                buycategory.setCategory_no(category_no);					// [設定]カテゴリ表示順
				buy_category_list.add(buycategory); 						// [追加]カテゴリ情報をカテゴリリストへ
				/* カテゴリ行追加 */
				final View categoryView = add_category_line(buycategory,1);	// [作成]カテゴリ行のビュー
				categoryView.findViewById(R.id.category).requestFocus();	// 追加したカテゴリを即編集可能にする
				linear_layout_grandchild.addView(categoryView);				// [表示]孫ビューへ

				// =========+=========+
				// アイテムの[追加]ボタン行表示設定
				// ---------+---------+
				final View item_add_view = inflater.inflate(R.layout.sub_shopping_list_edit_item_add, null);	// [作成]アイテム追加ボタン行のビュー
				linear_layout_child.addView(item_add_view);											// [表示]子ビューへ
				/* アイテム追加ボタン押下時 */
				Button item_add_button = (Button) item_add_view.findViewById(R.id.button_add);
				item_add_button.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// 配列に追加
		                BuyItem buyitem = new BuyItem();
						buyitem.setMod_type(1);							// [設定]変更タイプ=追加
						buyitem.setCategory_no(category_no);			// [設定]カテゴリ表示順
						buy_item_list.add(buyitem);						// [追加]BuyItemを配列に追加
						final View itemView = add_item_line(buyitem); 	// [作成]アイテム行のビュー
						itemView.findViewById(R.id.item).requestFocus();	// 追加したアイテムを即編集可能にする
						linear_layout_grandchild.addView(itemView);		// [表示]孫ビューへ
					}
				});
			}
		});
		scroll_view = (ScrollView) findViewById(R.id.scroll_view);
		scroll_view.addView(linear_layout);
	}

	// ■【showListのSUB】[追加]ボタンにて拡張可能な「カテゴリ行」「アイテム行」の関数化■
	/* =========+=========+=========+=========+=========+=========+
	* 【SUB】アイテム行を追加する
	*  ---------+---------+---------+---------+---------+---------+
	*  機能 ： アイテム行のviewを作成する。以下の状態が完了している
	*      ・buyitemに設定されたアイテム名を表示済
	*      ・アイテム削除ボタンの動作設定
	*      ・保存するEditTextをbuyItemに設定
	*  引数：表示したいアイテム名が設定されたBuyItem
	*  返却値：生成したアイテム行のview
	* ---------+---------+---------+---------+---------+---------+ */
	public View add_item_line(final BuyItem buyitem){
		final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View itemView = inflater.inflate(R.layout.sub_shopping_list_edit_item, null);
		final EditText item = (EditText) itemView.findViewById(R.id.item);
		final Button del_button = (Button) itemView.findViewById(R.id.button_del);
		/* アイテム名を表示 */
		item.setText(buyitem.getItem_name());
		/* 削除ボタン押下時の動作を設定 */
		del_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(function_name, "[START]del_button.setOnClick");
				item.setEnabled(false);				// [非アクティブ化]入力ボックス
				del_button.setEnabled(false);		// [非アクティブ化]ボタン
				TextPaint paint = item.getPaint();	// テキストに取り消し線を引く
				paint.setFlags(item.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
				buyitem.setMod_type(3);				// [設定] 変更タイプ=削除
			}
		});
		/* 保存時にアイテム名(編集可)を取得できるようにする */
		buyitem.setEdit_text(item);
		return itemView;
	}

	/* =========+=========+=========+=========+=========+=========+
	* 【SUB】カテゴリ行を追加する
	*  ---------+---------+---------+---------+---------+---------+
	*  機能 ：カテゴリ行のviewを作成する。以下の状態が完了している
	*       ・BuyCategoryに設定されたアイテム名を表示済
	*       ・並び換えボタンの動作設定
	*       ・保存するEditTextをBuyCategoryに設定
	*  引数：表示したいカテゴリ名が設定されたBuyCategory
	*  返却値：生成したカテゴリ行のview
	*  ---------+---------+---------+---------+---------+---------+ */
	public View add_category_line(final BuyCategory buycategory, int mod_type){
		final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View categoryView = inflater.inflate(R.layout.sub_shopping_list_edit_category, null);	// [作成]カテゴリ行のビュー
		if ( mod_type == 1 ){
			categoryView = inflater.inflate(R.layout.sub_shopping_list_edit_category2, null);
		}

		/* カテゴリ名を表示 */
		final EditText category_title = (EditText) categoryView.findViewById(R.id.category);		// [取得]カテゴリ名
		category_title.setText(buycategory.getCategory_name());
		buycategory.setName_mod(category_title);  // 保存時にカテゴリ名(編集可)を取得できるようにしておく

		if ( mod_type == 1 ){
			return categoryView;
		}

		/* 並び換えボタン押下時の動作を設定 */
		final Button rearrange_button = (Button) categoryView.findViewById(R.id.button_rearrange);	// [取得]並び換えボタン
		rearrange_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(function_name, "category:"+buycategory.getCategory_name());
				showSelectRearrangeDialog(buycategory);
			}
		});
		return categoryView;
	}

	// =========+=========+=========+=========+=========+=========+
	// ボタン
	// ---------+---------+---------+---------+---------+---------+
	//  [終了] 終了確認ダイアログを開く
	public void clickExitButton(View v) {
		Log.d(function_name, "[START]clickExitButton");
		showExitDialog();
		return;
	}

	//  [保存] 保存して画面更新
	public void clickSaveButton(View v) {
		Log.d(function_name, "[START]clickSaveButton");
		// 保存
		int err_cnt = helper.save(junle_id,buy_category_list,buy_item_list);

		// 保存成功時 トースト「保存しました」& 画面再描画
		if (err_cnt == 0){
			Toast.makeText(ctxt, getString(R.string.toast_save_success), Toast.LENGTH_LONG).show();
			scroll_view.removeAllViews();	// [クリア]メイン画面
			new ShowAsyncMainView().execute();
		}

		// 保存失敗時 -> ダイアログ表示 -> OKで 画面再描画
		else{
			String msg = pref.getString("dbError", "");
			Builder builder = new AlertDialog.Builder(ctxt);
			builder.setTitle("Sorry..");
			builder.setMessage(err_cnt + getString(R.string.toast_save_failed_cnt) + "\n" + msg);
			builder.setPositiveButton(getString(R.string.ok),
					new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					scroll_view.removeAllViews();	// [クリア]メイン画面
					new ShowAsyncMainView().execute();
				}
			});
			AlertDialog alert_dialog = builder.create();
			alert_dialog.show();
		}
		return;
	}

	/* [タブボタン] */
	public void clickTabButton(View v){
		Log.d(function_name, "[START]clickTabButton");
		// キーボードがあれば非表示
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

		/* ダイアログ表示 */
		Builder builder_exit = new AlertDialog.Builder(this);
		builder_exit.setMessage(getString(R.string.save_confirm_message));

		/* 【OK】->保存 */
		final int jid = v.getId();
		builder_exit.setPositiveButton(getString(R.string.ok),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						// 保存
						int err_cnt = helper.save(junle_id,buy_category_list,buy_item_list);

						// 保存成功時 トースト「保存しました」& タブ切替
						if (err_cnt == 0){
							Toast.makeText(ctxt, getString(R.string.toast_save_success), Toast.LENGTH_LONG).show();
							switchTab(jid);
						}

						// 保存失敗時 -> ダイアログ表示 -> タブ切替
						else{
							String msg = pref.getString("dbError", "");
							Builder builder = new AlertDialog.Builder(ctxt);
							builder.setTitle("Sorry..");
							builder.setMessage(err_cnt + getString(R.string.toast_save_failed_cnt) + "\n" + msg);
							builder.setPositiveButton(getString(R.string.ok),
									new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int id) {
									switchTab(jid);
								}
							});
							AlertDialog alert_dialog = builder.create();
							alert_dialog.show();
						}
					}
				});

		/* 【CANCEL】->閉じる */
		builder_exit.setNegativeButton(getString(R.string.cancel), null);

		/* ダイアログを表示 */
		AlertDialog alert_dialog_exit = builder_exit.create();
		alert_dialog_exit.show();
	}

	/* [SUB]タブボタン : 画面描画 */
	private void switchTab(int jid){
		switch (jid) {
		/* 食材 */
		case R.id.button_food:
			scroll_view.removeAllViews();					// [クリア]メイン画面
			junle_id = 0;
			buttonConfig();
			new ShowAsyncMainView().execute();
			break;

		/* 消耗品 */
		case R.id.button_supply:
			scroll_view.removeAllViews();					// [クリア]メイン画面
			junle_id = 1;
			buttonConfig();
			new ShowAsyncMainView().execute();
			break;

		/* その他 */
		case R.id.button_other:
			scroll_view.removeAllViews();					// [クリア]メイン画面
			junle_id = 2;
			buttonConfig();
			new ShowAsyncMainView().execute();
			break;

		default:
			break;
		}
	}

	/* [キーボードボタン] */
	public void clickKeyboadButton(View v) {
		Log.d(function_name, "[START]clickKeyboadButton");
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        //imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	}

	/* [メニューボタン] */
	public void clickMenuButton(View v) {
		Log.d(function_name, "[START]clickMenuButton");
		// 表示するアイテム
		String[] items = new String[]{
				getString(R.string.menu_read_csv_file),
				getString(R.string.menu_help)};

		/* ダイアログ表示 */
		Builder builder = new AlertDialog.Builder(this);
		builder.setItems(items,new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int id) {
				switch(id){
				/* データ初期化 */
				case 0:
		        	showImportDialog();
					break;

				/* ヘルプ */
				case 1:
					Intent intent_help = new Intent(ctxt, ShoppingListEditHelpActivity.class);
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

	/* 現在表示中の画面に遷移するボタンの非アクティブ化 */
	private void buttonConfig(){
		Button button_food = (Button) findViewById(R.id.button_food);
		Button button_supply = (Button) findViewById(R.id.button_supply);
		Button button_other = (Button) findViewById(R.id.button_other);

		switch (junle_id){
		case 0:	// 食材
			button_food  .setEnabled(false);
			button_supply.setEnabled(true );
			button_other .setEnabled(true );
			break;
		case 1:	// 消耗品
			button_food  .setEnabled(true );
			button_supply.setEnabled(false);
			button_other .setEnabled(true );
			break;
		case 2:	// その他
			button_food  .setEnabled(true );
			button_supply.setEnabled(true );
			button_other .setEnabled(false);
			break;
		}
	}

	/* [バックキー]　→　破棄終了確認ダイアログ表示 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ( keyCode == KeyEvent.KEYCODE_BACK){
			showExitDialog();
		}
		return super.onKeyDown(keyCode, event);
	}

	/* 同時クリック回避処理 */
	private void settingSingleClick(){

		final Button button_exit = (Button) findViewById(R.id.button_exit);
		final Button button_save = (Button) findViewById(R.id.button_save);
		final Button button_food = (Button) findViewById(R.id.button_food);
		final Button button_supply = (Button) findViewById(R.id.button_supply);
		final Button button_other = (Button) findViewById(R.id.button_other);
		final Button button_menu = (Button) findViewById(R.id.button_menu);

		// 終了ボタン押下時
		button_exit.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) {
					button_food  .setEnabled(false);
					button_supply.setEnabled(false);
					button_other .setEnabled(false);
					button_menu  .setEnabled(false);
				}
				if(event.getAction() == MotionEvent.ACTION_UP) {
					button_menu .setEnabled(true);
					buttonConfig();
				}
				return false;
			}
		});

		// 保存ボタン押下時
		button_save.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) {
					button_food  .setEnabled(false);
					button_supply.setEnabled(false);
					button_other .setEnabled(false);
					button_menu  .setEnabled(false);
				}
				if(event.getAction() == MotionEvent.ACTION_UP) {
					button_menu .setEnabled(true);
					buttonConfig();
				}
				return false;
			}
		});

		// 食材ボタン押下時→出発ボタン無効
		button_food.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) {
					button_menu.setEnabled(false);
					button_exit.setEnabled(false);
					button_save.setEnabled(false);
				}
				if(event.getAction() == MotionEvent.ACTION_UP) {
					button_menu.setEnabled(true);
					button_exit.setEnabled(true);
					button_save.setEnabled(true);
				}
				return false;
			}
		});

		// 消耗品ボタン押下時→出発ボタン無効
		button_supply.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) {
					button_menu.setEnabled(false);
					button_exit.setEnabled(false);
					button_save.setEnabled(false);
				}
				if(event.getAction() == MotionEvent.ACTION_UP) {
					button_menu.setEnabled(true);
					button_exit.setEnabled(true);
					button_save.setEnabled(true);
				}
				return false;
			}
		});

		// その他ボタン押下時→出発ボタン無効
		button_other.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) {
					button_menu.setEnabled(false);
					button_exit.setEnabled(false);
					button_save.setEnabled(false);
				}
				if(event.getAction() == MotionEvent.ACTION_UP) {
					button_menu.setEnabled(true);
					button_exit.setEnabled(true);
					button_save.setEnabled(true);
				}
				return false;
			}
		});

	}

	// =========+=========+=========+=========+=========+=========+
	//  ダイアログ
	// ---------+---------+---------+---------+---------+---------+
	/* 7-3 終了確認ダイアログ */
	private void showExitDialog(){
		Log.d(function_name, "[START]showExitDialog");
		/* ダイアログ表示 */
		Builder builder_exit = new AlertDialog.Builder(this);
		builder_exit.setMessage(getString(R.string.save_confirm_message));

		/* 【保存終了】 */
		builder_exit.setPositiveButton(getString(R.string.save_exit),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						// 保存
						int err_cnt = helper.save(junle_id,buy_category_list,buy_item_list);

						// 保存成功時 トースト「保存しました」& 画面遷移（買物準備）
						if (err_cnt == 0){
							Toast.makeText(ctxt, getString(R.string.toast_save_success), Toast.LENGTH_LONG).show();
							Intent intent = new Intent(ctxt,ShoppingListPrepareActivity.class);
							startActivity(intent);
							finish();
						}

						// 保存失敗時 -> ダイアログ表示 -> OKで画面遷移（買物準備）
						else{
							String msg = pref.getString("dbError", "");
							Builder builder = new AlertDialog.Builder(ctxt);
							builder.setTitle("Sorry..");
							builder.setMessage(err_cnt + getString(R.string.toast_save_failed_cnt) + "\n" + msg);
							builder.setPositiveButton(getString(R.string.ok),
									new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int id) {
									Intent intent = new Intent(ctxt,ShoppingListPrepareActivity.class);
									startActivity(intent);
									finish();
								}
							});
							AlertDialog alert_dialog = builder.create();
							alert_dialog.show();
						}
					}
				});

		/* 【破棄終了】 */
		builder_exit.setNegativeButton(getString(R.string.break_exit),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						// 画面遷移
						Intent intent = new Intent(ctxt,ShoppingListPrepareActivity.class);
						startActivity(intent);
						finish();
					}
				});

		/* ダイアログを表示 */
		AlertDialog alert_dialog_exit = builder_exit.create();
		alert_dialog_exit.show();
	}

	/* =========+=========+=========+=========+=========+=========+
	* 7-5 並び換え確認ダイアログ表示
	* ---------+---------+---------+---------+---------+---------+
	* 引数１：(int)カテゴリID
	* ---------+---------+---------+---------+---------+---------+
	* ダイアログを表示
	* 【カテゴリ】→変更を保存してカテゴリ並び換え画面
	* 【品目】→変更を保存して品目並び換え画面
	* 【キャンセル】→何もせず閉じる
	* ---------+---------+---------+---------+---------+---------+ */
	public void showSelectRearrangeDialog(BuyCategory buy_category) {
		Log.d(function_name, "[START]showSelectRearrangeDialog");
		Log.d(function_name, "category_name["+buy_category.getCategory_name()+"]");

		/* ダイアログ表示 */
		Builder builder_rearrange = new AlertDialog.Builder(this);
		builder_rearrange.setMessage(getString(R.string.rearranege_message));

		/* 【カテゴリ】-> カテゴリ並び換え画面へ遷移 */
		String[] JUNLE_NAME = {
				getString(R.string.food),
				getString(R.string.supply),
				getString(R.string.other) };
		builder_rearrange.setPositiveButton(JUNLE_NAME[junle_id],
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						// 保存
						int err_cnt = helper.save(junle_id,buy_category_list,buy_item_list);

						// 保存成功時 トースト「保存しました」&画面遷移（カテゴリ並び換え画面）
						if (err_cnt == 0){
							Toast.makeText(ctxt, getString(R.string.toast_save_success), Toast.LENGTH_LONG).show();
							Intent intent = new Intent(ctxt,ShoppingListRearrangeCategoryActivity.class);
							intent.putExtra("junle_id", junle_id);
							startActivity(intent);
							finish();
						}

						// 保存失敗時 -> ダイアログ表示 -> OKで画面遷移（カテゴリ並び換え画面）
						else{
							String msg = pref.getString("dbError", "");
							Builder builder = new AlertDialog.Builder(ctxt);
							builder.setTitle("Sorry..");
							builder.setMessage(err_cnt + getString(R.string.toast_save_failed_cnt) + "\n" + msg);
							builder.setPositiveButton(getString(R.string.ok),
									new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int id) {
									Intent intent = new Intent(ctxt,ShoppingListRearrangeCategoryActivity.class);
									intent.putExtra("junle_id", junle_id);
									startActivity(intent);
									finish();
								}
							});
							AlertDialog alert_dialog = builder.create();
							alert_dialog.show();
						}
					}
				});

		/* 【品目】-> 品目並び換え画面へ遷移 */
		final int category_id = buy_category.getId();
		String category_name = buy_category.getName_mod().getText().toString();			// 編集後カテゴリ名を取得
		if (( category_name.length() == 0 ) && (buy_category.getMod_type() == 2)){		// 既存カテゴリが空文字に修正されている場合
			category_name = buy_category.getCategory_name();;							// 保存済を取得
		}
		final String category_name_button = category_name;

		if ( category_name_button.length() > 0 ){			// 新規追加カテゴリが空文字の場合は表示しない
			builder_rearrange.setNeutralButton(category_name_button,
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					// 保存
					int err_cnt = helper.save(junle_id,buy_category_list,buy_item_list);

					// 保存成功時 トースト「保存しました」&画面遷移（品目並び換え画面）
					if (err_cnt == 0){
						Toast.makeText(ctxt, getString(R.string.toast_save_success), Toast.LENGTH_LONG).show();
						Intent intent = new Intent(ctxt,ShoppingListRearrangeItemActivity.class);
						intent.putExtra("junle_id", junle_id);
						intent.putExtra("category_id", category_id);
						intent.putExtra("category_name", category_name_button);
						startActivity(intent);
						finish();
					}

					// 保存失敗時 -> ダイアログ表示
					else{
						String msg = pref.getString("dbError", "");
						Builder builder = new AlertDialog.Builder(ctxt);
						builder.setTitle("Sorry..");
						builder.setMessage(err_cnt + getString(R.string.toast_save_failed_cnt) + "\n" + msg);
						builder.setPositiveButton(getString(R.string.ok),null);
						AlertDialog alert_dialog = builder.create();
						alert_dialog.show();
					}
				}
			});
		}

		/* 【キャンセル】-> なにもせず閉じる */
		builder_rearrange.setNegativeButton(getString(R.string.cancel), null);

		/* ダイアログを表示 */
		AlertDialog alert_dialog_rearrange = builder_rearrange.create();
		alert_dialog_rearrange.show();
		return;

	}

	/* 7-6 初期化確認ダイアログ */
	private void showImportDialog(){
		Log.d(function_name, "[START]showImportDialog");
		/* ダイアログ表示 */
		Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.import_confirm_message));

		/* 【読込開始】- */
		builder.setPositiveButton(getString(R.string.ok),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						int ret = importTable();	// テーブルインポート
						String message = null;
					    if (ret == 0){
					    	message = getString(R.string.import_success);
					    }else {
					    	message = getString(R.string.import_failed);
					    }
					    showMessageDialog(message);
					    // 画面再表示
						scroll_view.removeAllViews();					// [クリア]メイン画面
						new ShowAsyncMainView().execute();
					}
				});

		/* 【CANCEL】 */
		builder.setNegativeButton(getString(R.string.cancel), null);

		/* ダイアログを表示 */
		AlertDialog alert_dialog = builder.create();
		alert_dialog.show();
	}

	/* =========+=========+=========+=========+=========+=========+
	*  7-7 メッセージダイアログを表示する
	*  ---------+---------+---------+---------+---------+---------+
	*  ダイアログを表示する。ボタンは[OK]ボタンのみ。
	* 引数１：(String)メッセージ
	*  ---------+---------+---------+---------+---------+---------+ */
	private void showMessageDialog(String message){
		Log.d(function_name, "[START]showMessageDialog");
		Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message);
		builder.setPositiveButton(getString(R.string.ok),null);
		AlertDialog alert_dialog = builder.create();
		alert_dialog.show();
	}

	// =========+=========+=========+=========+=========+=========+
	//  SUB関数
	// ---------+---------+---------+---------+---------+---------+
	/* =========+=========+=========+=========+=========+=========+
	* 【保存】：編集内容保存処理
	* ---------+---------+---------+---------+---------+---------+
	*        [カテゴリ]　  			[アイテム]
	*　[追加] save_add_category	save_add_item
	* [変更] save_mod_category	save_mod_item
	* [削除] -                 	save_del_item
	* ---------+---------+---------+---------+---------+---------+ */
	private int save() {
		/* 保存 */
		Log.d("save","[START]save");
		int err_cnt = helper.save(junle_id,buy_category_list,buy_item_list);
		Log.d("save","SAVE END");

		/* 保存成功/失敗をユーザに通知 */
		if ( err_cnt == 0 ){	// 保存成功
			Toast.makeText(this, getString(R.string.toast_save_success), Toast.LENGTH_LONG).show();
			return 0;
		}else{					// 保存失敗
			Toast.makeText(this, err_cnt + getString(R.string.toast_save_failed_cnt), Toast.LENGTH_LONG).show();
			return -1;
		}
		// ※ 追加Ｘ件、変更Ｘ件、削除Ｘ件保存成功しました←【変更】は変更あり/なしにかかわらずかけるのでこのようなログは表示しない
	}

	/* =========+=========+=========+=========+=========+=========+
	*  テーブルをインポートする
	*  ---------+---------+---------+---------+---------+---------+
	*  返却値：(int)0:成功/0以外:異常(-1:失敗/1以上:失敗件数)
	*  ---------+---------+---------+---------+---------+---------+ */
	private int importTable(){
		String function_name = "importTable";
		int ret = 0;
		try{
		    AssetManager asset_manager = getResources().getAssets();

		    // カテゴリテーブル読込
		    InputStream input_stream_category = asset_manager.open("category_table.csv");
		    CSVReader csv_category = new CSVReader(new InputStreamReader(input_stream_category), ',');

		    // アイテムテーブル読込
		    InputStream input_stream_item = asset_manager.open("item_table.csv");
		    CSVReader csv_item = new CSVReader(new InputStreamReader(input_stream_item), ',');

		    // インポート
		    ret = helper.getTableFromCsv(csv_category,csv_item);

		    // クローズ処理
		    csv_category.close();
		    csv_item.close();
		    input_stream_category.close();
		    input_stream_item.close();

		    // asset_managerをcloseすると画面遷移時にエラーにな
		    //asset_manager.close();

		} catch (FileNotFoundException e) {
			Log.e(function_name, "FileNotFoundException:" + e.getMessage());
		    e.printStackTrace();
		    return -1;

		} catch (UnsupportedEncodingException e) {
			Log.e(function_name, "UnsupportedEncodingException:" + e.getMessage());
		    e.printStackTrace();
		    return -1;

		} catch (IOException e) {
			Log.e(function_name, "IOException:" + e.getMessage());
		    e.printStackTrace();
		    return -1;

		}catch (Exception e) {
			Log.e(function_name, "IOException:" + e.getMessage());
		    e.printStackTrace();
		    return -1;
		}
		return ret;
	}

}
