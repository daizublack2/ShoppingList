package jp.daizu.shoppinglist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/*
 * ********************************************* *
 * クラス　：ShoppingListPrepareHelp3Activity
 * 機能　：買物準備ヘルプ画面3
 * 遷移元：買物準備ヘルプ画面2
 * 遷移先：買物準備ヘルプ画面4
 * ********************************************* *
 */
public class ShoppingListPrepareHelp3Activity extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shopping_list_prepare_help3);
	}

	// 次へボタン押下
	public void clickNextButton(View v) {
		Intent intent = new Intent(this, ShoppingListPrepareHelp4Activity.class);
		startActivity(intent);
		finish();

	}

}
