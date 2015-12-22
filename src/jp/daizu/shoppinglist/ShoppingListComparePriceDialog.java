package jp.daizu.shoppinglist;

/*
 * ********************************************* *
 * クラス　：ShoppingListComparePriceDialog
 * 機能　：価格比較ダイアログ
 * 遷移元：買物中画面
 * ********************************************* *
 */
import java.math.BigDecimal;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("SimpleDateFormat")
public class ShoppingListComparePriceDialog extends Dialog {
	private static Context ctxt;
	private static boolean numeration_g_flag = false;	// 単位がグラム

	/* ウィジェット */
	TextView text_numeration1;	// [商品1]単位表示
	TextView text_numeration2;	// [商品2]単位表示
	EditText edit_count1;		// [商品1]数量
	EditText edit_count2;		// [商品2]数量
	EditText edit_price1;		// [商品1]価格
	EditText edit_price2;		// [商品2]価格
	TextView text_result1;		// [商品1]計算結果
	TextView text_result2;		// [商品2]計算結果
	TextView text_low_price1; 	// [商品1]「安い」表示
	TextView text_low_price2;	// [商品2]「安い」表示
	LinearLayout layout_item1;	// [商品1]枠
	LinearLayout layout_item2;	// [商品2]枠

	/* コンストラクタ */
	public ShoppingListComparePriceDialog(Context context) {
		super(context);
		ctxt = context;
	}

    // =========+=========+=========+=========+=========+=========+
	// 画面読込時処理
    // =========+=========+=========+=========+=========+=========+
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		String function_name = "ShoppingListComparePriceDialog";
		Log.d(function_name, "START");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_shopping_list_compare_price);

		text_numeration1 = (TextView)findViewById(R.id.numeration1);	// [商品1]単位表示
		text_numeration2 = (TextView)findViewById(R.id.numeration2);    // [商品2]単位表示
		edit_count1 = (EditText)findViewById(R.id.count1);              // [商品1]数量
		edit_count2 = (EditText)findViewById(R.id.count2);              // [商品2]数量
		edit_price1 = (EditText)findViewById(R.id.price1);              // [商品1]価格
		edit_price2 = (EditText)findViewById(R.id.price2);              // [商品2]価格
		text_result1 = (TextView)findViewById(R.id.result1);            // [商品1]計算結果
		text_result2 = (TextView)findViewById(R.id.result2);            // [商品2]計算結果
		text_low_price1 = (TextView)findViewById(R.id.moderate1);       // [商品1]「安い」表示
		text_low_price2 = (TextView)findViewById(R.id.moderate2);       // [商品2]「安い」表示
		layout_item1 = (LinearLayout)findViewById(R.id.item1);          // [商品1]枠
		layout_item2 = (LinearLayout)findViewById(R.id.item2);          // [商品2]枠

		/* g/個の単位選択動作設定 */
		setNumeration();

		/* 【価格を比較する！】ボタン押下 */
		findViewById(R.id.button_compare).	setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						compare();
					}
			});

		/* 【戻る】：ダイアログを閉じる */
		findViewById(R.id.button_back).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						cancel();
					}
			});
		Log.d(function_name, "END");
	}

	/* スピナー動作設定 */
	private void setNumeration(){
		Spinner spinner = (Spinner) findViewById(R.id.numeration);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(ctxt,android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		/* アイテムを追加する */
		adapter.add(ctxt.getString(R.string.unit));
		adapter.add(ctxt.getString(R.string.gram));
		spinner.setAdapter(adapter);

		// スピナーが選択されたら
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {
                Spinner spinner = (Spinner) parent;
                String item = (String) spinner.getSelectedItem();
        		text_numeration1.setText(item);
        		text_numeration2.setText(item);
        		if( item == ctxt.getString(R.string.gram) ){
        			numeration_g_flag = true;
        		}else{
        			numeration_g_flag = false;
        		}
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
	}

	/* 価格比較ボタン押下時 */
	private void compare(){
		/* 割る金額( 1個あたり金額 / 100gあたり金額 ) */
		String function_name ="compare";
		/* キーボードを閉じる */
        InputMethodManager imm = (InputMethodManager)ctxt.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

		/* 計算 */
		int count1 = getNumber(edit_count1);	// [取得]商品1/量
		int price1 = getNumber(edit_price1);	// [取得]商品1/価格
		Log.d(function_name, "count1:" + count1 + "  price1:" + price1);
		if ((count1 <= 0)||(price1<=0)){
			Toast.makeText(ctxt, ctxt.getString(R.string.input_err) , Toast.LENGTH_LONG).show();
			return;
		}
		BigDecimal result1 = calcPrice(count1, price1);	// [計算]商品1/量当たりの金額

		int count2 = getNumber(edit_count2);	// [取得]商品2/量
		int price2 = getNumber(edit_price2);	// [取得]商品2/価格
		if ((count2 <= 0)||(price2<=0)){
			Toast.makeText(ctxt, ctxt.getString(R.string.input_err) , Toast.LENGTH_LONG).show();
			return;
		}
		BigDecimal result2 = calcPrice(count2, price2);	// [計算]商品2/量当たりの金額

		/* 計算結果表示 */
		if ( numeration_g_flag == true ){
			text_result1.setText(100 + ctxt.getString(R.string.gram) + result1 + ctxt.getString(R.string.currency));
			text_result2.setText(100 + ctxt.getString(R.string.gram) + result2 + ctxt.getString(R.string.currency));
		}else{
			text_result1.setText(1 + ctxt.getString(R.string.unit) + result1 + ctxt.getString(R.string.currency));
			text_result2.setText(1 + ctxt.getString(R.string.unit) + result2 + ctxt.getString(R.string.currency));
		}
		/* ＜安い＞側のハイライト表示 */
		if ( result2.compareTo(result1) > 0 ){
			text_low_price1.setVisibility(View.VISIBLE);	// [表示]商品1/安い
			text_low_price2.setVisibility(View.GONE);		// [非表示]商品2/安い
			layout_item1.setBackgroundResource(R.drawable.compare_price_frame_win);
			layout_item2.setBackgroundResource(R.drawable.compare_price_frame);
		}else if ( result1.compareTo(result2) > 0 ){
			text_low_price1.setVisibility(View.GONE);		// [非表示]商品1/安い
			text_low_price2.setVisibility(View.VISIBLE);	// [表示]商品2/安い
			layout_item2.setBackgroundResource(R.drawable.compare_price_frame_win);
			layout_item1.setBackgroundResource(R.drawable.compare_price_frame);
		}else{
			text_low_price1.setVisibility(View.VISIBLE);	// [表示]商品1/安い
			text_low_price2.setVisibility(View.VISIBLE);	// [表示]商品2/安い
			layout_item1.setBackgroundResource(R.drawable.compare_price_frame);
			layout_item2.setBackgroundResource(R.drawable.compare_price_frame);
		}
	}

    // =========+=========+=========+=========+=========+=========+
	// 【SUB関数】
    // =========+=========+=========+=========+=========+=========+
	/* 価格計算 */
	private BigDecimal calcPrice(int count, int price ){
		// 割る金額( 1個あたり金額 / 100gあたり金額 )
		int div_num = 1;
		if ( numeration_g_flag == true ){
			div_num = 100;
		}
		BigDecimal bc_count = new BigDecimal(count);									// [取得]商品/量
		BigDecimal bc_price = new BigDecimal(price*div_num);							// [取得]商品/価格
		BigDecimal result1 = bc_price.divide(bc_count, 1, BigDecimal.ROUND_HALF_UP);	// [計算]商品/量当たりの金額
		return result1;
	}

	/* EditTextに入力された文字を取得し数字に変換（nullの場合は-1を返却）*/
	private int getNumber(EditText edit_text){
		int ret = 0;
		String s = edit_text.getText().toString();
		if ( s.length() == 0 ){
			return ret;
		}
		ret = Integer.parseInt(s);
		return ret;
	}

}



