package jp.daizu.shoppinglist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/*
 * ********************************************* *
 * クラス　：ShoppingListPrepareHelp4Activity
 * 機能　：買物準備ヘルプ画面4
 * 遷移元：買物準備ヘルプ画面3
 * 遷移先：買物準備画面 / 買物編集画面
 * ********************************************* *
 */
public class ShoppingListPrepareHelp4Activity extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shopping_list_prepare_help4);
	}

	/* 閉じるボタン押下 */
	public void clickNextButton(View v) {
		finish();
	}

	/* 編集ボタン押下 */
	public void clickEditButton(View v) {
		Intent intent = new Intent(this, ShoppingListEditActivity.class);
		startActivity(intent);
		finish();
	}
}
