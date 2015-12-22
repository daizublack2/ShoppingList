package jp.daizu.shoppinglist;

/*
 * ********************************************* *
 * クラス　：ShoppingListEditHelpActivity
 * 機能　：編集ヘルプ画面
 * 遷移元：編集画面
 * ********************************************* *
 */

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class ShoppingListEditHelpActivity extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shopping_list_edit_help);
	}

	// 閉じるボタン押下
	public void clickNextButton(View v) {
		finish();
	}

}
