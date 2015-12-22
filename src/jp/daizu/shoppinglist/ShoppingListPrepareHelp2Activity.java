package jp.daizu.shoppinglist;

/*
 * ********************************************* *
 * クラス　：ShoppingListPrepareHelp2Activity
 * 機能　：買物準備ヘルプ画面2
 * 遷移元：買物準備ヘルプ画面1
 * 遷移先：買物準備ヘルプ画面3
 * ********************************************* *
 */
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class ShoppingListPrepareHelp2Activity extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shopping_list_prepare_help2);
	}

	// 次へボタン押下
	public void clickNextButton(View v) {
		Intent intent = new Intent(this, ShoppingListPrepareHelp3Activity.class);
		startActivity(intent);
		finish();

	}

}
