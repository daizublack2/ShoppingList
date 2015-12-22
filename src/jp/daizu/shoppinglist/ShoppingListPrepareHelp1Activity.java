package jp.daizu.shoppinglist;

/*
 * ********************************************* *
 * クラス　：ShoppingListPrepareHelp1Activity
 * 機能　：買物準備ヘルプ画面1
 * 遷移元：買物準備画面
 * 遷移先：買物準備ヘルプ画面2
 * ********************************************* *
 */
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class ShoppingListPrepareHelp1Activity extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shopping_list_prepare_help1);
	}

	// 次へボタン押下
	public void clickNextButton(View v) {
		Intent intent = new Intent(this, ShoppingListPrepareHelp2Activity.class);
		startActivity(intent);
		finish();
	}

}
