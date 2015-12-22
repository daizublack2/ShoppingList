package jp.daizu.shoppinglist;

/*
 * ********************************************* *
 * クラス　：ShoppingListPrepareActivity
 * 機能　：買物準備画面
 * 遷移元：起動時、買物中画面
 * 遷移先：
 * ********************************************* *
 */
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.text.format.DateFormat;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import au.com.bytecode.opencsv.CSVReader;

@SuppressLint("SimpleDateFormat")
public class ShoppingListPrepareActivity extends FragmentActivity {
	private static final String function_name = "ShoppingListPrepareActivity";
	private static final String DATE_FORMAT = "yyyy/MM/dd";

	private List<BuyCategory> buy_category_list = new ArrayList<BuyCategory>(); // カテゴリ格納場所
	private List<BuyItem> buy_item_list = new ArrayList<BuyItem>(); // アイテム格納場所

	private static SharedPreferences pref;
	private static LinearLayout linearLayout;
	private ScrollView scroll_view;
	DrawerLayout drawer_layout;
	private ListView navigation_list;
	private static ProgressDialog dlg = null; //プログレスダイアログを表示する

	static MySQLiteOpenHelper helper;
	private static Context ctxt;

	private static int junle_id = 0;  	// 画面遷移時に引き渡す必要があるため、コード全体で保持
	private static int category_id = 0; // カテゴリ単体選択時使用、すべての時は0
	private static int show_no = 0; 	// 表示順、アイテムクイック追加時に必要
	private static String category_name = null; // カテゴリ単体選択時使用、すべての時はnull

	private boolean save_flag = true; 	// 保存フラグ（出発時、自動保存を回避）
	private Resources resources;

    // =========+=========+=========+=========+=========+=========+
	// 画面読込時処理
    // =========+=========+=========+=========+=========+=========+
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(function_name, "[START]");
		super.onCreate(savedInstanceState);
		ctxt = this;
		pref = getSharedPreferences("preference", Activity.MODE_PRIVATE);
		resources = getResources();

		/* 買い物中なら買い物中画面へ遷移 */
		if ( pref.getString("StartTime", null) != null){
			Intent intent = new Intent(ctxt,ShoppingListUseActivity.class);
			startActivity(intent);
			finish();
		}

		helper = new MySQLiteOpenHelper(this);

		/* 初回起動時処理 */
		if ( pref.getBoolean("init_flag", true) == true){
			/* 買物リストを読み込む */
			Log.d(function_name, "初回起動!");
			int ret = importTable();
			if (ret == 0){
				Log.d(function_name, "テーブル読込正常終了");
			}else{
				Log.d(function_name, "テーブル読込失敗。err(" + ret + ")");
			}

			/* ヘルプ表示 */
			Intent intent_help = new Intent(ctxt,ShoppingListPrepareHelp1Activity.class);
			startActivity(intent_help);

			/* 初回起動フラグをfalseに変更 */
			SharedPreferences.Editor editor;
			editor = pref.edit();
			editor.putBoolean("init_flag", false);
			editor.commit();
		}

		/* ジャンルID取得 */
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			junle_id = bundle.getInt("junle_id");
		}

		/* ヘッダ */
		setTitle(getString(R.string.shoppinglist_prepare));

		/* プログレスダイアログを使用するための準備 */
		dlg = new ProgressDialog(this);

		/* スライドメニュー&メイン画面表示 */
		setContentView(R.layout.activity_shopping_list_prepare);
		settingSingleClick();  /* 同時押し回避処理 */
		drawer_layout = (DrawerLayout) findViewById(R.id.drawer_layout);
		navigation_list = (ListView) findViewById(R.id.navigation_list);
		changeJunleId(junle_id);
		Log.d(function_name, "[END]");
	}

    // =========+=========+=========+=========+=========+=========+
	// メイン画面描画
    // =========+=========+=========+=========+=========+=========+
	/* 表示更新（画面起動時/ジャンル選択時） */
	private void changeJunleId(int id){
		junle_id = id;
		/* カテゴリ一覧をDBから取得 */
		buy_category_list = helper.getCategoryPrepare(junle_id);

		/* スライドメニュー描画 */
		showSlideMenu();

		/* ボタンアクティブ/非アクティブ設定 */
		buttonConfig();

		/* メイン画面描画 */
		linearLayout = new LinearLayout(this);
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		Iterator<BuyCategory> ite = buy_category_list.iterator();
		BuyCategory buy_category = (BuyCategory) ite.next();	// [取得]トップのカテゴリ情報
		category_id = buy_category.getId();	// 初期表示するカテゴリのID
		category_name = buy_category.getCategory_name();
		new ShowAsyncMainView().execute();	// 非同期にて処理
	}

	/* スライドメニューを描画 */
	private void showSlideMenu(){
		/* タイトルを表示 */
		TextView slide_title = (TextView)findViewById(R.id.slide_title);
		final String[] JUNLE_NAME = {
				getString(R.string.food),
				getString(R.string.supply),
				getString(R.string.other) };
		slide_title.setText(JUNLE_NAME[junle_id] + getString(R.string.slide_title));

		/* カテゴリ一覧を表示 */
		final IconicAdapter adapter = new IconicAdapter(ctxt, buy_category_list);
		navigation_list.setAdapter(adapter);

		/* カテゴリ選択時 ： 保存-> ドロワを閉じる -> 画面描画*/
		navigation_list.setOnItemClickListener(new AdapterView.OnItemClickListener()  {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				/* 保存 */
				Save(false);
				/* ドロワを閉じる */
				drawer_layout.closeDrawers();
				/* 選択したカテゴリのアイテムをメイン画面に描画 */
				scroll_view.removeAllViews();							// [クリア]メイン画面
				BuyCategory buy_category = adapter.getItem(position);	// [取得]選択したカテゴリ情報
				linearLayout = new LinearLayout(ctxt);
				linearLayout.setOrientation(LinearLayout.VERTICAL);
				category_id = buy_category.getId();
				category_name = buy_category.getCategory_name();
				new ShowAsyncMainView().execute();	// メイン画面描画
			}
		});

		/* 【すべて】ボタン選択時 : 保存 -> ドロワを閉じる -> 画面描画 */
		Button button = (Button)findViewById(R.id.all_category_button);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				/* カテゴリ初期化 */
				category_id = 0;
				category_name = null;
				/* 保存 */
				Save(false);
				/* ドロワを閉じる */
				drawer_layout.closeDrawers();
				/* 選択したカテゴリのアイテムをメイン画面に描画 */
				scroll_view.removeAllViews();
				linearLayout = new LinearLayout(ctxt);
				linearLayout.setOrientation(LinearLayout.VERTICAL);
				new ShowAsyncMainViewAll().execute();
			}
		});

	}

	/* スライドメニューのリストビュー表示用 */
    class IconicAdapter extends ArrayAdapter<BuyCategory>{
        private LayoutInflater inflater;
        private TextView category;

        public IconicAdapter(Context context, List<BuyCategory> objects) {
            super(context, R.layout.sub_shopping_list_prepare_side_row, objects);
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.sub_shopping_list_prepare_side_row, null);
            }
            final BuyCategory buy_category = this.getItem(position);
            if(buy_category != null){
            	String category_name = buy_category.getCategory_name();
            	category = (TextView)convertView.findViewById(R.id.text);
            	category.setText(category_name);
            }
            return convertView;
        }
    }

	// --- 【SUB】 ---
	/* 非同期処理：メイン画面描画 */
	class ShowAsyncMainView extends AsyncTask<Void, Integer, Void>{

		/* コンストラクタ */
		protected ShowAsyncMainView(){
			super();
		}

		/* 非同期処理開始時:プログレスダイアログ表示 */
		@Override
		protected void onPreExecute(){
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
			buy_item_list = helper.getItem(category_id);
			return null;
		}

		/* BG処理完了後：UIに表示 */
		@Override
		protected void onPostExecute(Void result){
			super.onPostExecute(result);
			dlg.setMessage(getString(R.string.making_view));
			showMainCategoryView(category_name);
			showMainItemView(category_id);
			scroll_view = (ScrollView) findViewById(R.id.main_view);
			scroll_view.addView(linearLayout);
			dlg.dismiss();
		}
	}

	/* 非同期処理：メイン画面描画（＜すべて＞選択時） */
	class ShowAsyncMainViewAll extends AsyncTask<Void, Integer, Void>{

		/* コンストラクタ */
		protected ShowAsyncMainViewAll(){
			super();
		}

		/* 非同期処理開始時:プログレスダイアログ表示 */
		@Override
		protected void onPreExecute(){
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
			buy_item_list = helper.getJunleItem(junle_id);
			return null;
		}

		/* BG処理完了後：UIに表示 */
		@Override
		protected void onPostExecute(Void result){
			super.onPostExecute(result);
			dlg.setMessage(getString(R.string.making_view));
			Iterator<BuyCategory> ite_cate = buy_category_list.iterator();
			while (ite_cate.hasNext()) {
				BuyCategory buycategory = (BuyCategory) ite_cate.next(); 	// 次の行を取得
				showMainCategoryView(buycategory.getCategory_name());
				showMainItemView(buycategory.getId());
			}
			scroll_view = (ScrollView) findViewById(R.id.main_view);
			scroll_view.addView(linearLayout);
			dlg.dismiss();
		}
	}

	// --- 【さらにSUB】 ---
	/* メイン画面の＜カテゴリ行＞を描画 */
	private void showMainCategoryView(String category_name){
		/* カテゴリ行表示 */
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View categoryView = inflater.inflate(R.layout.sub_shopping_list_prepare_category, null);	// [取得]カテゴリ行レイアウト
		final TextView textv = (TextView) categoryView.findViewById(R.id.category);						// [取得]カテゴリを表示するtextView
		textv.setText(category_name);																	// [設定]カテゴリ名
		linearLayout.addView(categoryView);
	}

	/* メイン画面の＜アイテム行＞を描画 */
	private void showMainItemView(int category_id){
		/* アイテム行表示 */
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		Iterator<BuyItem> ite_item = buy_item_list.iterator(); // アイテム用イテレータ
		while (ite_item.hasNext()) {
			BuyItem buy_item = (BuyItem) ite_item.next();
			if (buy_item.getCategory_id() != category_id) {
				continue;
			}
			final View itemView = inflater.inflate(R.layout.sub_shopping_list_prepare_item, null);
			linearLayout.addView(itemView);						// [表示]アイテム行
			final CheckBox check_name = (CheckBox) itemView.findViewById(R.id.item);
			final TextView text_day = (TextView) itemView.findViewById(R.id.date);
			final EditText memo = (EditText) itemView.findViewById(R.id.memo);
			final String item = buy_item.getItem_name();
			check_name.setChecked(buy_item.getCheck_flag());	// [設定]チェックの有無
			check_name.setText(item);							// [設定]アイテム名
			memo.setText(buy_item.getMemo());					// [設定]メモ
			buy_item.setCheckbox(check_name);					// [設定]チェック有無情報を保持
			buy_item.setEdit_text(memo);						// [設定]メモ編集情報を保持
			show_no = buy_item.getShow_no();					// [設定]表示順

			/* 前回購入日表示 */
			String from_date = buy_item.getBuy_date();			// [取得]前回購入日
			if (from_date != null) {
				text_day.setText(mmdd(from_date));				// [設定]前回購入日
				Calendar cal = Calendar.getInstance();			// [取得]現在日付
				String to_date = cal2str(cal);					// [変換]現在日付(Calendar->Str)
				long days = dayDiff(from_date, to_date);		// [取得]前回購入日からの経過日数
				text_day.setBackgroundColor(getDaysColor((int)days));	// [設定]経過日数に合わせて色変更
			}
		}
	}

	/* クイック追加時 */
	public void setItem(String item){
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		BuyItem buy_item = new BuyItem();
		final View itemView = inflater.inflate(R.layout.sub_shopping_list_prepare_item, null);
		linearLayout.addView(itemView);						// [表示]アイテム行
		final CheckBox check_name = (CheckBox) itemView.findViewById(R.id.item);
		final EditText memo = (EditText) itemView.findViewById(R.id.memo);
		check_name.setText(item);							// [表示]アイテム名
		check_name.setChecked(true);						// チェック状態にする
		buy_item.setCategory_id(category_id);				// [設定]カテゴリ名
		buy_item.setCheckbox(check_name);					// [設定]チェック有無情報を保持
		buy_item.setEdit_text(memo);						// [設定]メモ編集情報を保持
		buy_item.setItem_name(item);						// [設定]アイテム名
		show_no++;
		buy_item.setShow_no(show_no);
		int id = helper.insertQuickAddItem(buy_item);
		if ( id < 0 ){
			return;
		}
		buy_item.setId(id);
		buy_item_list.add(buy_item);

	}

    // =========+=========+=========+=========+=========+=========+
	// ボタン
    // =========+=========+=========+=========+=========+=========+
	/* タブボタン：保存してスライドメニューとメイン画面更新 */
	public void clickTabButton(View v) {
		Log.d(function_name, "[START]clickTabButton");
		/* 保存 */
		Save(false);

		switch (v.getId()) {
		/* 【食材】タブ */
		case R.id.button_food:
			scroll_view.removeAllViews();					// [クリア]メイン画面
			changeJunleId(0);
			break;

		/* 【消耗品】タブ */
		case R.id.button_supply:
			scroll_view.removeAllViews();					// [クリア]メイン画面
			changeJunleId(1);
			break;

		/* 【その他】タブ */
		case R.id.button_other:
			scroll_view.removeAllViews();					// [クリア]メイン画面
			changeJunleId(2);
			break;

		default:
			break;
		}
	}

	/* 【出発】ボタン：保存して買物出発確認ダイアログに遷移 */
	public void clickGoButton(View v) {
		Log.d(function_name, "[START]clickGoButton");
		/* 保存 */
		Save(false);

		int check_cnt = helper.getCheckCount();
		Log.d(function_name, "check_cnt："+check_cnt);
		if ( check_cnt > 0 ){
			showConfirmDialog( check_cnt );	// 出発確認を行う
		}else{
			showCancelGoDialog();			// 0件の場合、出発できない旨を表示する
		}
	}

	/* 【キーボード】ボタン：キーボード表示時、キーボードを隠す */
	public void clickKeyboadButton(View v){
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	}

	/* 1-4 【メニュー】ボタン：メニュー表示&動作 */
	public void clickMenuButton(View v) {
		Log.d(function_name, "[START]clickMenuButton");
		/* 表示するアイテム */
		String[] items = new String[]{
				getString(R.string.menu_quick_add),
				getString(R.string.menu_all_check_off),
				getString(R.string.menu_list_edit),
				getString(R.string.menu_help),
				getString(R.string.menu_day_color),
				getString(R.string.menu_license),
				getString(R.string.menu_app_exit)};

		/* ダイアログ表示 */
		Builder builder = new AlertDialog.Builder(this);
		builder.setItems(items,new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface d, int id) {
				switch(id){
				/* クイック追加 */
				case 0:
					if ( category_id > 0 ){
						/* ＜カテゴリ＞選択時：クイック追加ダイアログ表示 */
						FragmentManager fm = getSupportFragmentManager();
						createAddDialog dialog = new createAddDialog();
						dialog.show(fm, "dialog_fragment");
					}else{
						/* ＜すべて＞選択時：トースト表示 */
						Toast.makeText(ctxt, getString(R.string.toast_unable_quick_add) , Toast.LENGTH_LONG).show();
					}
					break;

				/* すべてのチェックを外す */
				case 1:
					showCheckOffConfirmDialog();
					break;

				/* リストの追加削除 */
				case 2:
					Intent intent = new Intent(ctxt,ShoppingListEditActivity.class);
					intent.putExtra("junle_id", junle_id);
					startActivity(intent);
					finish();
					break;

				/* ヘルプ */
				case 3:
					Intent intent_help = new Intent(ctxt,ShoppingListPrepareHelp1Activity.class);
					startActivity(intent_help);
					break;

				/* 購入日の色設定について */
				case 4:
					FragmentManager fm = getSupportFragmentManager();
					/* 1-7 日付色設定ダイアログ */
					aboutDayColorialog dialog = new aboutDayColorialog();
					dialog.show(fm, "dialog_fragment");
					break;

				/* ライセンス */
				case 5:
					showLicenseDialog();
					break;

				/* 終了 */
				case 6:
					moveTaskToBack (true);
					// onStopにて変更は保存される
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
		return;

	}

	/*  BACKキー：終了 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ( keyCode == KeyEvent.KEYCODE_BACK){
			moveTaskToBack (true);
		}
		return super.onKeyDown(keyCode, event);
	}

    // =========+=========+=========+=========+=========+=========+
	// ダイアログ
    // =========+=========+=========+=========+=========+=========+
	/* 1-3 出発確認ダイアログ */
	public void showConfirmDialog( int check_cnt ){
		/* 現在時刻取得 */
		final String now = nowTime();

		/* 確認ダイアログを表示 */
		Builder builder = new AlertDialog.Builder(this);
		builder.setMessage( getString(R.string.go_time) + now +
				getString(R.string.check_count) + check_cnt +
				getString(R.string.confirm_message));
		/* 【OK】の場合、買物中画面に遷移する */
		builder.setPositiveButton(getString(R.string.ok),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						save_flag = false;

						/* 買物出発時刻を保存 */
						pref = ctxt.getSharedPreferences("preference", Activity.MODE_PRIVATE);
						SharedPreferences.Editor editor;
						editor = pref.edit();
						editor.putString("StartTime", now);
						editor.commit();

						/* 買物中画面へ遷移 */
						Intent intent = new Intent(ctxt,ShoppingListUseActivity.class);
						startActivity(intent);
						finish();
					}
				});
		/* 【cancel】の場合、何もせずに閉じる */
		builder.setNegativeButton(getString(R.string.cancel), null);
		AlertDialog dialog = builder.create();
		dialog.show(); // ダイアログを表示
	}

	/* 1-5 チェックオフ確認ダイアログ */
	public void showCheckOffConfirmDialog(){
		Log.d(function_name, "[START]showCheckOffConfirmDialog");
		/* 確認ダイアログを表示 */
		Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.toast_all_check_off));
		/* 【OK】の場合、画面の全てのチェックを外す */
		builder.setPositiveButton(getString(R.string.ok),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						/* DBを更新（全チェックOFF）*/
						if (helper.AllCheckOff() == 0) {
							/* 画面のチェックを外す */
							Iterator<BuyItem> ite_item = buy_item_list.iterator(); // 表示されているチェックボックスを検索
							while (ite_item.hasNext()) {
								BuyItem buyitem = (BuyItem) ite_item.next();
								CheckBox check = buyitem.getCheckbox(); 			// チェックボックスを取得
								check.setChecked(false);							// チェックOFFにする
							}
						}else{
							Toast.makeText(ctxt, getString(R.string.toast_save_failed) , Toast.LENGTH_SHORT).show();
						}
					}
				});
		/* 【cancel】の場合、何もせずに閉じる */
		builder.setNegativeButton(getString(R.string.cancel), null);
		AlertDialog alert_dialog = builder.create();
		alert_dialog.show(); // ダイアログを表示
	}

	/* 1-6 クイック追加ダイアログ作成 */
	public static class createAddDialog extends DialogFragment{
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
	        final Dialog dialog = new Dialog(getActivity());
	        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);						// タイトルなし
	        dialog.setContentView(R.layout.dialog_shopping_list_quick_add);					// レイアウト設定
	        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));	// 背景色設定
	        /* ダイアログサイズ設定 */
	        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
	        DisplayMetrics metrics = getResources().getDisplayMetrics();
	        lp.width = (int) (metrics.widthPixels * 0.8);
	        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);

	        TextView category = (TextView) dialog.findViewById(R.id.category);
	        category.setText(category_name);

	        /* 【OK】 ボタン押下時 */
	        dialog.findViewById(R.id.button_ok).setOnClickListener(new OnClickListener() {
	            @Override
	            public void onClick(View v) {
					EditText edit_text = (EditText)dialog.findViewById(R.id.item);
	            	String item_name = edit_text.getText().toString();
	            	/* nullなら追加しない */
	            	if ( item_name.length() > 0 ){
	            		((ShoppingListPrepareActivity) ctxt).setItem(item_name);
	            	}
	                dismiss();
	            }
	        });
	        /* 【Close】 ボタン押下時 */
	        dialog.findViewById(R.id.button_cancel).setOnClickListener(new OnClickListener() {
	            @Override
	            public void onClick(View v) {
	                dismiss();
	            }
	        });

	        return dialog;
		}
	}

	/* 1-7 購入日色設定ダイアログ作成 */
	public static class aboutDayColorialog extends DialogFragment{
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
	        final Dialog dialog = new Dialog(getActivity());
	        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);						// タイトルなし
	        dialog.setContentView(R.layout.dialog_shopping_list_about_color);				// レイアウト設定
	        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));	// 背景色設定
	        /* ダイアログサイズ設定 */
	        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
	        DisplayMetrics metrics = getResources().getDisplayMetrics();
	        lp.width = (int) (metrics.widthPixels * 0.8);
	        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
	        /* 【OK】ボタン押下時 */
	        dialog.findViewById(R.id.button_ok).setOnClickListener(new OnClickListener() {
	            @Override
	            public void onClick(View v) {
	                dismiss();
	            }
	        });
	        return dialog;
		}
	}

	/* 1-8 出発不可ダイアログ */
	public void showCancelGoDialog() {
		/* 確認ダイアログを表示 */
		Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.check_nothing));
		/* 【OK】ボタン押下時閉じる */
		builder.setPositiveButton(getString(R.string.ok), null);
		AlertDialog dialog = builder.create();
		dialog.show(); // ダイアログを表示
	}

	/* 1-9 ライセンスダイアログ */
	public void showLicenseDialog(){
		Builder builder = new AlertDialog.Builder(this);
		/* 【OK】ボタン押下時閉じる */
		builder.setPositiveButton(getString(R.string.ok), null);
		AlertDialog dialog = builder.create();

		LinearLayout linear_layout = new LinearLayout(this);
		linear_layout.setOrientation(LinearLayout.VERTICAL);

		TextView license = new TextView(this);
		license.setText(getString(R.string.license_license));

		TextView apache = new TextView(this);
		apache.setText(getString(R.string.license_apache));

		TextView name = new TextView(this);
		CharSequence link = Html.fromHtml(" <a href=\"https://github.com/bauerca/drag-sort-listview/\">DragSortListView</a>");
		name.setText(link);
		MovementMethod mMethod = LinkMovementMethod.getInstance();
		name.setMovementMethod(mMethod);

		linear_layout.addView(license);
		linear_layout.addView(apache);
		linear_layout.addView(name);
		dialog.setView(linear_layout);
		dialog.show(); // ダイアログを表示
	}

    // =========+=========+=========+=========+=========+=========+
	// 【SUB関数】
    // =========+=========+=========+=========+=========+=========+
	/* タブボタンの非アクティブ化 */
	private void buttonConfig(){
		Button button_food = (Button) findViewById(R.id.button_food);
		Button button_supply = (Button) findViewById(R.id.button_supply);
		Button button_other = (Button) findViewById(R.id.button_other);
		/* 表示されているジャンルのボタンのみ押せないようにする */
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

	/* 同時クリック回避処理 */
	private void settingSingleClick(){
		final Button button_food = (Button) findViewById(R.id.button_food);
		final Button button_supply = (Button) findViewById(R.id.button_supply);
		final Button button_other = (Button) findViewById(R.id.button_other);
		final Button button_go = (Button) findViewById(R.id.button_go);
		final Button button_menu = (Button) findViewById(R.id.button_menu);

		/* 出発ボタン押下時→食材、消耗品、その他、メニュー無効 */
		button_go.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) {
					button_food  .setEnabled(false);
					button_supply.setEnabled(false);
					button_other .setEnabled(false);
					button_menu  .setEnabled(false);
				}
				if(event.getAction() == MotionEvent.ACTION_UP) {
					buttonConfig();
					button_menu  .setEnabled(true);
				}
				return false;
			}
		});

		/* 食材ボタン押下時→出発ボタン無効 */
		button_food.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) {
					button_go.setEnabled(false);
				}
				if(event.getAction() == MotionEvent.ACTION_UP) {
					button_go.setEnabled(true);
				}
				return false;
			}
		});

		/* 消耗品ボタン押下時→出発ボタン無効 */
		button_supply.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) {
					button_go.setEnabled(false);
				}
				if(event.getAction() == MotionEvent.ACTION_UP) {
					button_go.setEnabled(true);
				}
				return false;
			}
		});

		/* その他ボタン押下時→出発ボタン無効 */
		button_other.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) {
					button_go.setEnabled(false);
				}
				if(event.getAction() == MotionEvent.ACTION_UP) {
					button_go.setEnabled(true);
				}
				return false;
			}
		});

		/* メニューボタン押下時→出発ボタン無効 */
		button_menu.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) {
					button_go.setEnabled(false);
				}
				if(event.getAction() == MotionEvent.ACTION_UP) {
					button_go.setEnabled(true);
				}
				return false;
			}
		});

	}

	/* 何らかの動作で画面が見えなくなる場合 → 保存 */
	@Override
	protected void onStop(){
		super.onStop();
		Log.d(function_name, "onStop　START");
		if ( save_flag == false ){
			Log.d(function_name, "onStop　END");
			return;
		}
		Save(true);
		Log.d(function_name, "onStop　END");
	}

	/* ===========================
	*  文字列(yyyy/mm/dd)をCalendar型に変換する
	*  ---------------------------
	*  引数 : (String)yyyy/mm/dd
	*  返却値: (Calendar)指定日
	*  =========================== */
	Calendar str2cal(String ymd) {
		/* "yyyy/mm/dd"形式の文字列からdate型に変換 */
		SimpleDateFormat dateformat = new SimpleDateFormat(DATE_FORMAT);
		Date date = null;
		try {
			date = new java.util.Date(dateformat.parse(ymd).getTime());
		} catch (java.text.ParseException e) {
			Log.d("str2date[ERR]", e.toString());
		}
		/* date型からCalendar型に変換 */
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal;
	}

	/* ===========================
	*  Calendar型を文字列(yyyy/mm/dd)に変換する
	*  ---------------------------
	*  引数 : (Calendar)指定日
	*  返却値: (String)yyyy/mm/dd
	*  =========================== */
	String cal2str(Calendar cal) {
		int y = cal.get(Calendar.YEAR); 		// 年
		int m = cal.get(Calendar.MONTH) + 1; 	// 月
		int d = cal.get(Calendar.DATE); 		// 日
		String date = y + "/" + m + "/" + d;
		return date;
	}

	/* ===========================
	*  開始日付から終了日付までの日数を計算する
	*  ---------------------------
	*  引数1 : (String)yyyy/mm/dd : 開始日付
	*  引数2 : (String)yyyy/mm/dd : 終了日付
	*  返却値: (long)日数
	*  =========================== */
	long dayDiff(String s_from, String s_to) {
		SimpleDateFormat dateformat = new SimpleDateFormat(DATE_FORMAT);
		Date date_to = null;
		Date date_from = null;
		try {
			date_from = dateformat.parse(s_from);
			date_to = dateformat.parse(s_to);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		/* 日付をlong値に変換 */
		long date_time_to = date_to.getTime();
		long date_time_from = date_from.getTime();

		/* 差分の日数を算出 */
		long day_diff = (date_time_to - date_time_from) / (1000 * 60 * 60 * 24);
		return day_diff;
	}

	/* ===========================
	*  日付の色表示を取得する
	*  ---------------------------
	*  引数1 : (int)前回購入日からの経過日数
	*  返却値: (int)カラーコード
	*  =========================== */
	int getDaysColor(int days){
		int color = 0;
		switch ((int) days) {
		case 0:
		case 1:
			color = resources.getColor(R.color.day_green);
			break;
		case 2:
		case 3:
			color = resources.getColor(R.color.day_lightgreen);
			break;
		case 4:
		case 5:
			color = resources.getColor(R.color.day_yellow);
			break;
		case 6:
		case 7:
			color = resources.getColor(R.color.day_orange);
			break;
		case 8:
		case 9:
		case 10:
			color = resources.getColor(R.color.day_red);
			break;
		case 11:
		case 12:
		case 13:
			color = resources.getColor(R.color.day_brown);
			break;

		default:
			color = resources.getColor(R.color.day_gray);
			break;
		}
		return color;
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

		    /* カテゴリテーブル読込 */
		    InputStream input_stream_category = asset_manager.open("category_table.csv");
		    CSVReader csv_category = new CSVReader(new InputStreamReader(input_stream_category), ',');

		    /* アイテムテーブル読込 */
		    InputStream input_stream_item = asset_manager.open("item_table.csv");
		    CSVReader csv_item = new CSVReader(new InputStreamReader(input_stream_item), ',');

		    /* インポート */
		    ret = helper.getTableFromCsv(csv_category,csv_item);

		    /* クローズ処理 */
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

	/* =========+=========+=========+=========+=========+=========+
	*  文字の切り出し
	*  ---------+---------+---------+---------+---------+---------+
	*  引数１：(String)yyyy/mm/dd
	*  返却値：(String)mm/dd
	*  ---------+---------+---------+---------+---------+---------+ */
	private String mmdd(String ymd){
		String md;
		StringTokenizer st = new StringTokenizer(ymd,"/");
		st.nextToken();
		md = st.nextToken();
		md = md + "/" + st.nextToken();
		return md;
	}

	/* 現在時刻 */
	private String nowTime(){
		Calendar cal = Calendar.getInstance();
		return (String) DateFormat.format("kk:mm", cal);
	}

	/* 保存 */
	private void Save(boolean save_message_flag){
		/* 保存 */
		int err_cnt = helper.updateBuyItem(buy_item_list);

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
		/* 保存成功時 */
		else if ( save_message_flag == true ){
			Toast.makeText(ctxt, getString(R.string.toast_prepare_out) , Toast.LENGTH_SHORT).show();
		}
	}

	/* GoogleAnalyticsを利用する */
	@Override
	protected void onStart(){
		super.onStart();
		Tracker t = ((App)getApplication()).getTracker(App.TrackerName.APP_TRACKER);
	    t.setScreenName(this.getClass().getSimpleName());
	    t.send(new HitBuilders.AppViewBuilder().build());
	}

}
