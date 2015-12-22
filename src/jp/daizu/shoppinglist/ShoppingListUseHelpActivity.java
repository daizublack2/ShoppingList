package jp.daizu.shoppinglist;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

/*
 * ********************************************* *
 * クラス　：ShoppingListUseHelpActivity
 * 機能　：買物中画面
 * 遷移元：買物画面
 * ********************************************* *
 */
public class ShoppingListUseHelpActivity extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shopping_list_use_help);
	}

	// 閉じるボタン押下
	public void clickNextButton(View v) {
		finish();
	}

}
