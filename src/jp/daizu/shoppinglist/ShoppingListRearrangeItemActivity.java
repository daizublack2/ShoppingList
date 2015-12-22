package jp.daizu.shoppinglist;

/*
 * ********************************************* *
 * クラス　：ShoppingListRearrangeItemActivity
 * 機能　：買物並び換え画面（アイテム）
 * 遷移元：買物編集画面
 * 遷移先：買物編集画面
 * ********************************************* *
 */

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import jp.daizu.shoppinglist.tlv.TouchListView;

public class ShoppingListRearrangeItemActivity extends ListActivity {
	private IconicAdapter adapter;
	private List<BuyItem> buy_item_list = new ArrayList<BuyItem>(); // アイテム格納場所
	static MySQLiteOpenHelper helper;
	Context ctxt = this;
	private boolean mod_flag;
	int junle_id = 0;
	private static SharedPreferences pref;

    // =========+=========+=========+=========+=========+=========+
	// 画面読込時処理
    // =========+=========+=========+=========+=========+=========+
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		String function_name = "ShoppingListRearrangeItemActivity";
		Log.d(function_name, "START");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shopping_list_rearrange);
		mod_flag = false;				// [設定]変更フラグ(変更なし)
		ctxt = this;

		/* 遷移元より、カテゴリIDを取得 */
		int category_id = 0;
		String category_name = null;;
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			category_id = bundle.getInt("category_id");
			junle_id = bundle.getInt("junle_id");
			category_name = bundle.getString("category_name");
			Log.d(function_name, "category_id:" + category_id);
		}

		/* タイトル */
		setTitle(category_name);
		TextView title = (TextView) findViewById(R.id.title);
		title.setText(category_name);

		/* カテゴリIDに属するアイテムを取得 */
		helper = new MySQLiteOpenHelper(this);
		buy_item_list = helper.getItemRearrange(category_id);	// databaseからアイテム情報を取得しbuy_item_list配列に設定

		/* リスト表示 */
		TouchListView tlv = (TouchListView) getListView();
		adapter = new IconicAdapter(ctxt,buy_item_list);
        setListAdapter(adapter);

        /* 並び換え設定 */
        tlv.setDropListener(onDrop);

		pref = getSharedPreferences("preference", Activity.MODE_PRIVATE);
		Log.d(function_name, "END");
	}

    // =========+=========+=========+=========+=========+=========+
	// 並び替え
    // =========+=========+=========+=========+=========+=========+
	private TouchListView.DropListener onDrop = new TouchListView.DropListener() {
        @Override
        public void drop(int from, int to) {
        	mod_flag = true;			// [設定]変更フラグ (変更あり)
            final BuyItem buy_item_from = adapter.getItem(from);
            adapter.remove(buy_item_from);
            adapter.insert(buy_item_from, to);
        }
    };

    // =========+=========+=========+=========+=========+=========+
	// アダプタ
    // =========+=========+=========+=========+=========+=========+
    class IconicAdapter extends ArrayAdapter<BuyItem>{
        private LayoutInflater inflater;
        private TextView item;

        public IconicAdapter(Context context, List<BuyItem> objects) {
            super(context, R.layout.sub_shopping_list_rearrange, objects);
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.sub_shopping_list_rearrange, null);
            }
            final BuyItem buy_item = this.getItem(position);
            if(buy_item != null){
            	String item_name = buy_item.getItem_name();
            	item = (TextView)convertView.findViewById(R.id.label);
            	item.setText(item_name);
            }
            return convertView;
        }
    }

    // =========+=========+=========+=========+=========+=========+
	// ボタン
    // =========+=========+=========+=========+=========+=========+
	/* 【完了】 -> 保存→編集画面へ */
	public void clickFinishButton(View v) {
		// 変更ありの場合 -> 保存終了
		if (mod_flag == true){
			SaveExit();
		}

		// 変更なしの場合 -> 画面遷移（編集）
		else{
			Intent intent = new Intent(ctxt,ShoppingListEditActivity.class);
			startActivity(intent);
			finish();
		}
	}

	/* バックキー */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ( keyCode == KeyEvent.KEYCODE_BACK){
			showExitDialog();
		}
		return super.onKeyDown(keyCode, event);
	}

    // =========+=========+=========+=========+=========+=========+
	// ダイアログ
    // =========+=========+=========+=========+=========+=========+
	// 10-2 完了確認ダイアログ
	private void showExitDialog() {
		/* 変更なしの場合　→　編集画面へ遷移 */
		if (mod_flag == false){
			Intent intent = new Intent(ctxt,ShoppingListEditActivity.class);
			intent.putExtra("junle_id", junle_id);
			startActivity(intent);
			finish();
			return;
		}

		/* ダイアログ表示 */
		Builder builder_exit = new AlertDialog.Builder(this);
		builder_exit.setMessage(getString(R.string.cancel_rearrange_message));


		/* 【保存終了】 */
		builder_exit.setPositiveButton(getString(R.string.save_exit),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						SaveExit();
					}
				});

		/* 【破棄終了】 */
		builder_exit.setNegativeButton(getString(R.string.break_exit),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						/* 画面遷移(買物編集画面) */
						Intent intent = new Intent(ctxt,ShoppingListEditActivity.class);
						intent.putExtra("junle_id", junle_id);
						startActivity(intent);
						finish();
					}
				});

		/* ダイアログを表示 */
		AlertDialog alert_dialog_exit = builder_exit.create();
		alert_dialog_exit.show();
	}

    // =========+=========+=========+=========+=========+=========+
	// SUB関数
    // =========+=========+=========+=========+=========+=========+
	/* 保存終了 */
	private void SaveExit(){
		/* 保存 */
		int err_cnt = helper.saveRearrangeItem(buy_item_list);

		/* 保存成功時 トースト「保存しました」&画面遷移（編集）*/
		if (err_cnt == 0){
			Toast.makeText(ctxt, getString(R.string.toast_save_success), Toast.LENGTH_LONG).show();
			mod_flag = false;
			Intent intent = new Intent(ctxt,ShoppingListEditActivity.class);
			intent.putExtra("junle_id", junle_id);
			startActivity(intent);
			finish();
		}

		/* 保存失敗時 -> ダイアログ表示 -> OKで画面遷移（編集）*/
		else{
			String msg = pref.getString("dbError", "");
			Builder builder = new AlertDialog.Builder(ctxt);
			builder.setTitle("Sorry..");
			builder.setMessage(err_cnt + getString(R.string.toast_save_failed_cnt) + "\n" + msg);
			builder.setPositiveButton(getString(R.string.ok),
					new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					Intent intent = new Intent(ctxt,ShoppingListEditActivity.class);
					intent.putExtra("junle_id", junle_id);
					startActivity(intent);
					finish();
				}
			});
			AlertDialog alert_dialog = builder.create();
			alert_dialog.show();
		}
	}


}
