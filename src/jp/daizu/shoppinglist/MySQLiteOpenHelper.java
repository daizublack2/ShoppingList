package jp.daizu.shoppinglist;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import au.com.bytecode.opencsv.CSVReader;

public class MySQLiteOpenHelper extends SQLiteOpenHelper {
	final static private int DB_VERSION = 1;
	protected static Context ctxt;
	private static SharedPreferences pref;
	private static SharedPreferences.Editor editor;

	public MySQLiteOpenHelper(Context context) {
		super(context, "shopping_db", null, DB_VERSION);
		this.ctxt = context;
	}

	/* -+-+-+-+-+-+-+-+-+-+ DB作成 -+-+-+-+-+-+-+-+-+-+ */
	@Override
	public void onCreate(SQLiteDatabase db) {
		String function_name = "MySQLiteOpenHelper(onCreate)";
		Log.d(function_name, "START");
		/*
		// カテゴリ
		db.execSQL("CREATE TABLE category_table("
				+ "id INTEGER PRIMARY KEY, "   		// id
				+ "category_name TEXT NOT NULL,"   	// カテゴリ名
				+ "show_flag INTEGER NOT NULL, " 	// 0:非表示,1:表示
				+ "show_no INTEGER NOT NULL, " 		// 表示順番
				+ "junle_id INTEGER NOT NULL, " 	// 0:その他,1:食材,2:消耗品
				+ "buy_no INTEGER NOT NULL);" // 買物順
				);

		// アイテム
		db.execSQL("CREATE TABLE item_table("
				+ "id INTEGER PRIMARY KEY,"         // id
				+ "category_id INTEGER NOT NULL,"   // カテゴリID
				+ "item_name TEXT NOT NULL,"        // アイテム名
				+ "show_no INTEGER NOT NULL," 		// 表示順番
				+ "check_flag INTEGER NOT NULL,"    // チェックボックス
				+ "buy_date TEXT,"                  // 購入日
				+ "memo TEXT);"                     // メモ
				);
*/
		createCategoryTable(db);
		createItemTable(db);

/*
		String SQL="INSERT INTO category_table(id, category_name, show_flag, show_no, junle_id, buy_no) ";
		db.execSQL(SQL + " values (1,'野菜',1,1,0,1);");
		db.execSQL(SQL + " values (2,'乳製品',1,2,0,2);");
		db.execSQL(SQL + " values (3,'肉',1,3,0,3);");
		db.execSQL(SQL + " values (4,'生鮮品',1,4,0,4);");
		db.execSQL(SQL + " values (5,'加工食品',1,5,0,5);");
		db.execSQL(SQL + " values (6,'冷凍食品',1,6,0,6);");
		db.execSQL(SQL + " values (7,'惣菜',1,7,0,7);");
		db.execSQL(SQL + " values (8,'調味料',1,8,0,8);");
		db.execSQL(SQL + " values (9,'保存食品',1,9,0,9);");
		db.execSQL(SQL + " values (10,'化粧品',1,10,1,10);");
		db.execSQL(SQL + " values (11,'風呂',1,11,1,11);");
		db.execSQL(SQL + " values (12,'トイレ',1,12,1,12);");
		db.execSQL(SQL + " values (13,'洗濯',1,13,1,13);");
		db.execSQL(SQL + " values (14,'キッチン',1,14,1,14);");
		db.execSQL(SQL + " values (15,'部屋',1,15,1,15);");
		db.execSQL(SQL + " values (16,'趣味',1,16,2,16);");


		SQL="INSERT INTO item_table(id,category_id,check_flag,item_name,show_no) ";
		db.execSQL(SQL + " values (1,1,0,'たまねぎ',1);");
		db.execSQL(SQL + " values (2,1,0,'にんじん',2);");
		db.execSQL(SQL + " values (3,1,0,'じゃがいも',3);");
		db.execSQL(SQL + " values (4,1,0,'トマト',4);");
		db.execSQL(SQL + " values (5,1,0,'ピーマン',5);");
		db.execSQL(SQL + " values (6,1,0,'なす',6);");
		db.execSQL(SQL + " values (7,1,0,'ねぎ',7);");
		db.execSQL(SQL + " values (8,1,1,'ごぼう',8);");
		db.execSQL(SQL + " values (9,1,1,'だいこん',9);");
		db.execSQL(SQL + " values (10,1,1,'ほうれん草',10);");
		db.execSQL(SQL + " values (11,1,1,'小松菜',11);");
		db.execSQL(SQL + " values (12,1,1,'キャベツ',12);");
		db.execSQL(SQL + " values (13,1,1,'レタス',13);");
		db.execSQL(SQL + " values (14,1,1,'きゅうり',14);");
		db.execSQL(SQL + " values (15,1,1,'パプリカ',15);");
		db.execSQL(SQL + " values (16,1,1,'れんこん',16);");
		db.execSQL(SQL + " values (17,1,1,'かぼちゃ',17);");
		db.execSQL(SQL + " values (18,1,1,'しいたけ',18);");
		db.execSQL(SQL + " values (19,2,1,'ヨーグルト',19);");
		db.execSQL(SQL + " values (20,2,1,'牛乳',20);");
		db.execSQL(SQL + " values (21,2,1,'チーズ',21);");
		db.execSQL(SQL + " values (22,2,1,'バター',22);");
		db.execSQL(SQL + " values (23,2,1,'卵',23);");
		db.execSQL(SQL + " values (24,3,1,'ウインナー',24);");
		db.execSQL(SQL + " values (25,3,1,'ベーコン',25);");
		db.execSQL(SQL + " values (26,3,1,'豚',26);");
		db.execSQL(SQL + " values (27,3,1,'牛',27);");
		db.execSQL(SQL + " values (28,3,1,'鶏',28);");
		db.execSQL(SQL + " values (29,3,1,'ひき肉',29);");
		db.execSQL(SQL + " values (30,5,1,'豆腐',30);");
		db.execSQL(SQL + " values (31,5,1,'納豆',31);");
		db.execSQL(SQL + " values (32,6,1,'冷凍食品',32);");
		db.execSQL(SQL + " values (33,9,1,'カップラーメン',33);");
		db.execSQL(SQL + " values (34,9,1,'みそ汁',34);");
		db.execSQL(SQL + " values (35,9,1,'スープ',35);");
		db.execSQL(SQL + " values (36,9,1,'パスタ',36);");
		db.execSQL(SQL + " values (37,9,1,'米',37);");
		db.execSQL(SQL + " values (38,10,1,'洗顔料',38);");
		db.execSQL(SQL + " values (39,10,1,'ローション',39);");
		db.execSQL(SQL + " values (40,10,1,'モイスチャー',40);");
		db.execSQL(SQL + " values (41,10,1,'クレンジング',41);");
		db.execSQL(SQL + " values (42,10,1,'コットン',42);");
		db.execSQL(SQL + " values (43,10,1,'シャンプー',43);");
		db.execSQL(SQL + " values (44,10,1,'リンス',44);");
		db.execSQL(SQL + " values (45,10,1,'ボディソープ',45);");
		db.execSQL(SQL + " values (46,11,1,'ハンドソープ',46);");
		db.execSQL(SQL + " values (47,11,1,'ふろ用洗剤',47);");
		db.execSQL(SQL + " values (48,11,1,'歯磨き粉',48);");
		db.execSQL(SQL + " values (49,11,1,'はぶらし',49);");
		db.execSQL(SQL + " values (50,11,1,'クリーナー',50);");
		db.execSQL(SQL + " values (51,12,1,'洗剤',51);");
		db.execSQL(SQL + " values (52,12,1,'トイレットペーパー',52);");
		db.execSQL(SQL + " values (53,12,1,'生理用品',53);");
		db.execSQL(SQL + " values (54,13,1,'衣料用洗剤',54);");
		db.execSQL(SQL + " values (55,14,1,'洗剤',55);");
		db.execSQL(SQL + " values (56,14,1,'スポンジ',56);");
		db.execSQL(SQL + " values (57,14,1,'ハイター',57);");
		db.execSQL(SQL + " values (58,14,1,'アルコール',58);");
		db.execSQL(SQL + " values (59,14,1,'クレンザー',59);");
		db.execSQL(SQL + " values (60,14,1,'キッチンペーパー',60);");
		db.execSQL(SQL + " values (61,14,1,'水きりネット',61);");
		db.execSQL(SQL + " values (62,14,1,'水きりネット（排水口）',62);");
		db.execSQL(SQL + " values (63,15,1,'ティッシュ',63);");
		db.execSQL(SQL + " values (64,15,1,'ごみ袋',64);");
		db.execSQL(SQL + " values (65,15,1,'消臭剤',65);");
		db.execSQL(SQL + " values (66,15,1,'ラップ',66);");
		db.execSQL(SQL + " values (67,15,1,'フローリングワイパーシート',67);");
		db.execSQL(SQL + " values (68,16,1,'aaa',68);");
		db.execSQL(SQL + " values (69,16,1,'bb',69);");
		*/
		Log.d(function_name, "END");
	}

	/* -+-+-+-+-+-+-+-+ SELECT category-+-+-++-+-+-+-+-+ */

	/* =====================================
	 * [SELECT][CATEGORY]カテゴリのリストを取得(買物中)
	 * -------------------------------------
	 * arg(1):ジャンルコード
	 * return:List<BuyCategory>
	 * ====================================*/
	public List<BuyCategory> getCategoryShopping(){
    	String sql ="SELECT * " +
    			"FROM category_table " +
    			"WHERE EXISTS(" +			// [条件]アイテムが存在するカテゴリのみ取得
    			"SELECT category_id FROM item_table " +
    			"WHERE category_table.id = item_table.category_id " +
    			"AND check_flag = 1 " +
    			") " +
    			"ORDER BY buy_no";			// [ｿｰﾄ]買物順
        return getCategoryList(sql, null);
	}

	/* =====================================
	 * [SELECT][CATEGORY]カテゴリのリストを取得(買物リスト編集/並び換え)
	 * -------------------------------------
	 * arg(1):ジャンルコード
	 * return:List<BuyCategory>
	 * ====================================*/
	public List<BuyCategory> getCategoryEdit(int junle){
    	String sql ="SELECT * " +
    			"FROM category_table " +
    			"WHERE junle_id=? " +		// [条件]ジャンルID = junleのみ取得
    			"ORDER BY show_no";			// [ｿｰﾄ]表示順
    	String[] value = {Integer.toString(junle)};
        return getCategoryList(sql, value);
	}

	/* =====================================
	 * [SELECT][CATEGORY]カテゴリのリストを取得(買物準備)
	 * -------------------------------------
	 * arg(1):ジャンルコード
	 * return:List<BuyCategory>
	 * ====================================*/
	public List<BuyCategory> getCategoryPrepare(int junle){
    	String sql ="SELECT * " +
    			"FROM category_table " +
    			"WHERE EXISTS(" +			// [条件]アイテムが存在するカテゴリのみ取得
    			"SELECT category_id FROM item_table WHERE category_table.id = item_table.category_id" +
    			") " +
    			"AND junle_id=? " +			// [条件]ジャンルID = junleのみ取得
    			"ORDER BY show_no";			// [ｿｰﾄ]表示順
    	String[] value = {Integer.toString(junle)};
        return getCategoryList(sql, value);
	}

	/* =====================================
	 * 【SUB】[SELECT][CATEGORY]カテゴリのリストを取得
	 * -------------------------------------
	 * 引数１:(String)SQL文　(※ SELECT *であること！！)
	 * 引数２：(String配列)SQLの"?"置き換え部分
	 * return:List<BuyCategory>
	 * ====================================*/
	public List<BuyCategory> getCategoryList(String sql, String[] value){
		String function_name = "getCategoryList";
		List<BuyCategory> buy_category_list = new ArrayList<BuyCategory>();  // カテゴリ格納場所
		int category_no = 1;
    	SQLiteDatabase db = this.getReadableDatabase();
        try {
        	Cursor c = db.rawQuery(sql, value);
            c.moveToFirst();
            while( !c.isAfterLast()){
                BuyCategory buy_category = new BuyCategory();
                buy_category.setId(c.getInt(0));				// [設定]カテゴリID
                buy_category.setCategory_name(c.getString(1));	// [設定]カテゴリ名
                buy_category.setCategory_no(category_no);		// [設定]表示ID
                buy_category.setJunle_id(c.getInt(4));			// [設定]カテゴリ名
                buy_category_list.add(buy_category);
				//Log.d("getBuyCategory","id[" + c.getInt(0) + "] name[" + c.getString(1) + "]");
				//Log.d(function_name,"id[" + c.getInt(0) + "] name[" + c.getString(1) + "]"
				//		+ " show_flag[" + c.getInt(2) + "] show_no[" + c.getInt(3) + "] junle_id[" + c.getInt(4) + "] buy_no[" + c.getInt(5) + "]");
				category_no++;
                c.moveToNext();
            }

		}catch(SQLiteException e){
			Log.e(function_name, "SQLiteException:" + e.getMessage());
			e.printStackTrace();

		}catch(SQLException e){
			Log.e(function_name, "SQLException:" + e.getMessage());
			e.printStackTrace();

		}catch(Exception  e){
			Log.e(function_name, "Exception:" + e.getMessage());
			e.printStackTrace();

        } finally {
            db.close();
        }
        return buy_category_list;
	}

	/* -+-+-+-+-+-+-+-+-+ SELECT item -+-+-+-+-+-+-+-+-+ */

	/* =====================================
	 * [SELECT][ITEM]アイテムのリストをカテゴリ単位で取得(買い物準備)
	 * -------------------------------------
	 * 機能：指定ジャンルに属するアイテムを取得
	 * 引数１:ジャンルID
	 * 返却値:List<BuyItem>
	 * ====================================*/
	public List<BuyItem> getItem(int category_id){
    	String sql ="SELECT * " +
    			"FROM item_table " +
    			"WHERE category_id = ? " +			// [条件]カテゴリID = category_idのみ取得
    			"ORDER BY item_table.show_no";		// [ｿｰﾄ]表示順
    	String[] value = {Integer.toString(category_id)};
		return getItemList(sql,value);
	}

	/* =====================================
	 * [SELECT][ITEM]アイテムのリストをジャンル単位で取得(買い物準備/編集)
	 * -------------------------------------
	 * 機能：指定ジャンルに属するアイテムを取得
	 * 引数１:ジャンルID
	 * 返却値:List<BuyItem>
	 * ====================================*/
	public List<BuyItem> getJunleItem(int junle){
    	String sql ="SELECT item_table.* " +
    			"FROM item_table,category_table " +
    			"WHERE item_table.category_id = category_table.id " +
    			"AND junle_id = ?" + 				// [条件]ジャンルID = junleのみ取得
    			"ORDER BY item_table.show_no";		// [ｿｰﾄ]表示順
    	String[] value = {Integer.toString(junle)};
		return getItemList(sql,value);
	}

	/* =====================================
	 * [SELECT][ITEM]アイテムのリストを取得(アイテム並び換え)
	 * -------------------------------------
	 * 機能：指定カテゴリIDに属するアイテムを取得
	 * 引数１:カテゴリID
	 * 返却値:List<BuyItem>
	 * ====================================*/
	public List<BuyItem> getItemRearrange(int category_id){
    	String sql ="SELECT * " +
    			"FROM item_table " +
    			"WHERE category_id = ? " + 	// [条件]カテゴリID = category_idのみ取得
    			"ORDER BY show_no";			// [ｿｰﾄ]表示順
    	String[] value = {Integer.toString(category_id)};
		return getItemList(sql,value);
	}

	/* =====================================
	 * [SELECT][ITEM]アイテムのリストを取得(買い物中)
	 * -------------------------------------
	 * 機能：チェックをONにしたアイテムを取得
	 * 返却値:List<BuyItem>
	 * ====================================*/
	public List<BuyItem> getItemUse(){
    	String sql ="SELECT * " +
    			"FROM item_table " +
    			"WHERE check_flag = 1 " + 	// [条件]フラグ=1のみ取得
    			"ORDER BY show_no";			// [ｿｰﾄ]表示順
		return getItemList(sql,null);
	}

	/* =====================================
	 * 【SUB】[SELECT][ITEM]アイテムのリストを取得
	 * -------------------------------------
	 * 引数１:(String)SQL文　(※ SELECT *であること！！)
	 * 引数２：(String配列)SQLの"?"置き換え部分
	 * return:List<BuyItem>
	 * ====================================*/
	public List<BuyItem> getItemList(String sql, String[] value){
		String function_name = "getItemList";
		List<BuyItem> buy_item_list = new ArrayList<BuyItem>();  // カテゴリ格納場所
		SQLiteDatabase db = this.getReadableDatabase();
        try {
        	Cursor c = db.rawQuery(sql, value);
            c.moveToFirst();
            while( !c.isAfterLast()){
                BuyItem buyitem = new BuyItem();
            	boolean check_flag;
            	if (c.getInt(4) == 1 ){
            		check_flag = true;
            	}else{
            		check_flag = false;
            	}
                buyitem.setId          (c.getInt(0));		// [設定]アイテムID
                buyitem.setCategory_id (c.getInt(1));		// [設定]カテゴリID
                buyitem.setItem_name   (c.getString(2));	// [設定]アイテム名
                buyitem.setShow_no     (c.getInt(3));		// [設定]表示順
                buyitem.setCheck_flag  (check_flag);		// [設定]チェックフラグ
                buyitem.setBuy_date    (c.getString(5));	// [設定]購入日
                buyitem.setMemo        (c.getString(6));	// [設定]メモ
                buy_item_list.add(buyitem);
				//Log.d("getShoppingItem","id[" + c.getInt(0) + "] name[" + c.getString(2) + "] ");
                c.moveToNext();
            }

		}catch(SQLiteException e){
			Log.e(function_name, "SQLiteException:" + e.getMessage());
			e.printStackTrace();

		}catch(SQLException e){
			Log.e(function_name, "SQLException:" + e.getMessage());
			e.printStackTrace();

		}catch(Exception  e){
			Log.e(function_name, "Exception:" + e.getMessage());
			e.printStackTrace();

        } finally {
            db.close();
        }
		return buy_item_list;
	}

	/* チェックのついているアイテムの件数を確認する */
	public int getCheckCount(){
		final String function_name = "getCategoryCount";
    	String sql ="SELECT COUNT(*)" +
    			"FROM item_table " +
    			"WHERE check_flag = 1 ";
    	int count = 0;

    	SQLiteDatabase db = this.getReadableDatabase();
        try{
        	Cursor c = db.rawQuery(sql, null);
        	c.moveToFirst();
        	count = c.getInt(0);

		}catch(SQLiteException e){
			Log.e(function_name, "SQLiteException:" + e.getMessage());
			e.printStackTrace();

		}catch(SQLException e){
			Log.e(function_name, "SQLException:" + e.getMessage());
			e.printStackTrace();

		}catch(Exception  e){
			Log.e(function_name, "Exception:" + e.getMessage());
			e.printStackTrace();

        }
        return count;
	}

	/* -+-+-+-+-+-+-+-+-+ SELECT SUB関数 -+-+-+-+-+-+-+-+-+ */

	/* =====================================
	 * 【SUB】[SELECT]データベースの指定したカラムの最大値を取得する
	 * -------------------------------------
	 * 引数1：テーブル名
	 * 引数2：カラム名
	 * 返却値:最大値
	 * ====================================*/
	public int getMax(SQLiteDatabase db, String table_name, String colmn_name){
		String function_name = "getMax";
        int max = -1;

        //SQLiteDatabase db = this.getReadableDatabase();
    	String[] clmn  = { "MAX(" + colmn_name + ")" };

        try{
        	Cursor c = db.query(table_name, clmn, null, null, null, null, null);
        	c.moveToFirst();
        	max = c.getInt(0);

		}catch(SQLiteException e){
			Log.e(function_name, "SQLiteException:" + e.getMessage());
			e.printStackTrace();

		}catch(SQLException e){
			Log.e(function_name, "SQLException:" + e.getMessage());
			e.printStackTrace();

		}catch(Exception  e){
			Log.e(function_name, "Exception:" + e.getMessage());
			e.printStackTrace();

        }
		return max;
	}

	/* -+-+-+-+-+-+-+-+-+ UPDATE -+-+-+-+-+-+-+-+-+ */

	/* 買物準備画面 */
	/* =====================================
	 * [UPDATE][ITEM]準備リストのチェックボックスのフラグを保存
	 * -------------------------------------
	 * 引数１:(List<BuyItem>)アイテム更新情報
	 * 返却値：エラー項目数
	 * ====================================*/
	public int updateBuyItem(List<BuyItem> buy_item_list){
		SQLiteDatabase db = this.getReadableDatabase();
		String function_name = "updateBuyItem";
		int cnt_err = 0;
		int check_flag;
		int item_id;

		Iterator<BuyItem> ite_item = buy_item_list.iterator(); // アイテム用イテレータ
		while (ite_item.hasNext()) {
			BuyItem buyitem = (BuyItem) ite_item.next();	// 次のアイテム
			item_id = buyitem.getId();						// [取得]アイテムID
			CheckBox check = buyitem.getCheckbox();			// [取得]チェックボックス
			if (check.isChecked() == true) {				// [取得]チェック有無
				check_flag = 1;
			} else {
				check_flag = 0;
			}
			TextView edit_text = buyitem.getEdit_text();	// [取得]メモ編集情報
			String memo = edit_text.getText().toString();	// [取得]メモ

			// [取得]表示順
    		try{
    			ContentValues values = new ContentValues();
    			values.put("check_flag", Integer.toString(check_flag));	// [設定]チェック
    			values.put("memo", memo);								// [設定]メモ
    			int cnt=db.update("item_table", values, "id=?", new String[] { String.valueOf(item_id) });	// UPDATE
    			if (cnt != 1){
    				Log.e(function_name, "[ERROR][UPDATE] id[" + item_id + "] memo[" + memo + "]");
        			saveErrorMessage("id[" + item_id + "] check[" + check_flag + "]  memo[" + memo + "]");
        			cnt_err++;
    			}
    		}catch(SQLiteException e){
    			saveErrorMessage("SQLiteException id[" + item_id + "] check[" + check_flag + "]  memo[" + memo + "]");
    			Log.e(function_name, "SQLiteException:" + e.getMessage());
    			e.printStackTrace();
    			cnt_err++;

    		}catch(SQLException e){
    			saveErrorMessage("SQLiteException id[" + item_id + "] check[" + check_flag + "]  memo[" + memo + "]");
    			Log.e(function_name, "SQLException:" + e.getMessage());
    			e.printStackTrace();
    			cnt_err++;

    		}catch(Exception  e){
    			saveErrorMessage("SQLiteException id[" + item_id + "] check[" + check_flag + "]  memo[" + memo + "]");
    			Log.e(function_name, "Exception:" + e.getMessage());
    			e.printStackTrace();
    			cnt_err++;
    		}
		}
		db.close();
		return cnt_err;
	}

	/* =====================================
	 * [UPDATE][ITEM]すべてのチェックをOFFにする
	 * -------------------------------------
	 * 引数１:アイテム更新情報
	 * 返却値：エラー項目数
	 * ====================================*/
	public int AllCheckOff(){
    	SQLiteDatabase db = this.getReadableDatabase();
    	final SQLiteStatement statement = db.compileStatement("UPDATE item_table SET check_flag=0");
    	try {
    		statement.execute();
    	}catch(Exception e){
    		Log.d("AllCheckOff", "Exception:" + e.getMessage());
    		e.printStackTrace();
    		return -1;
    	}
		return 0;
	}

	/* 買物中画面 */
	/* =====================================
	 * [UPDATE][ITEM]買物済みアイテムを更新
	 * -------------------------------------
	 * 引数１:(List<BuyItem>)アイテム更新情報
	 * 返却値：エラー項目数
	 * ====================================*/
	public int updateShoppingItem(List<BuyItem> buy_item_list){
		SQLiteDatabase db = this.getReadableDatabase();
		String function_name = "updateShoppingItem";
		int cnt_err = 0;
		int item_id;

		/* [取得]本日の日付 */
		Calendar calendar = Calendar.getInstance();
		String s_today = calendar.get(Calendar.YEAR) + "/"
					  + (calendar.get(Calendar.MONTH) + 1) + "/"
					   + calendar.get(Calendar.DATE);

		Iterator<BuyItem> ite_item = buy_item_list.iterator(); // アイテム用イテレータ
		while (ite_item.hasNext()) {
			BuyItem buy_item = (BuyItem) ite_item.next();	// 次のアイテム
			CheckBox check = buy_item.getCheckbox(); 	// [取得]チェックボックス
			if (check.isChecked() == false) {			// チェックなしアイテムは更新しない
				continue;
			}
			item_id = buy_item.getId();					// [取得]アイテムID
    		try{
    			ContentValues values = new ContentValues();
    			values.put("check_flag", Integer.toString(0));		// [設定]チェック ( => 0)
    			values.put("memo", "");								// [設定]メモ( => null)
    			values.put("buy_date", s_today); 					// [設定]購入日( => today)
    			int cnt=db.update("item_table", values, "id=?", new String[] { String.valueOf(item_id) });	// UPDATE
    			if (cnt != 1){
    				Log.e(function_name, "[SUCCESS][UPDATE] id[" + item_id + "]");
        			saveErrorMessage("id[" + item_id + "] day[" + s_today + "] ");
        			cnt_err++;
    			}//else{
    				//Log.d(function_name, "[SUCCESS][UPDATE] id[" + item_id + "]");
    			//}
    		}catch(SQLiteException e){
    			Log.e(function_name, "SQLiteException:" + e.getMessage());
    			saveErrorMessage("SQLiteException id[" + item_id + "] day[" + s_today + "] ");
    			e.printStackTrace();
    			cnt_err++;

    		}catch(SQLException e){
    			Log.e(function_name, "SQLException:" + e.getMessage());
    			saveErrorMessage("SQLiteException id[" + item_id + "] day[" + s_today + "] ");
    			e.printStackTrace();
    			cnt_err++;

    		}catch(Exception  e){
    			Log.e(function_name, "Exception:" + e.getMessage());
    			saveErrorMessage("SQLiteException id[" + item_id + "] day[" + s_today + "] ");
    			e.printStackTrace();
    			cnt_err++;
    		}
		}
		db.close();
		return cnt_err;
	}

	/* カテゴリ並び換え画面 */
	/* =====================================
	 * [UPDATE][CATEGORY]カテゴリの並び換え
	 * -------------------------------------
	 * 引数１：(List<BuyItem>)並び換え済のアイテムのリスト
	 * return:0(正常終了)/1↑(エラー数)
	 * ====================================*/
	public int saveRearrangeCategory(List<BuyCategory> buy_category_list ){
		String function_name = "saveRearrangeCategory";
        SQLiteDatabase db = this.getReadableDatabase();
		int id;
		int show_no = 0;
		int cnt_err = 0;
		String err_log = "";

		Iterator<BuyCategory> ite_category = buy_category_list.iterator(); // アイテム用イテレータ
		while (ite_category.hasNext()) {
			final BuyCategory buy_category = (BuyCategory) ite_category.next(); // 次の行を取得
			id               = buy_category.getId();		// [取得]カテゴリID
            show_no++;										// [取得]表示順
    		try{
    			/* update */
    			ContentValues values = new ContentValues();
    			values.put("show_no", show_no);				// [設定]表示順
    			String[] args = {Integer.toString(id)};		// [対象]カテゴリID
    			int cnt = db.update("category_table", values, "id=?", args);
    			/* log */
    			if ( cnt != 1 ){	// 失敗
    				Log.e(function_name, "[ERROR] (" + cnt + ") UPDATE fin /  id[" + id + "] show_no[" + show_no + "] name[" + buy_category.getCategory_name() + "]");
        			err_log += id + ",";
    				cnt_err++;

    			} //else {			// 成功
    				//Log.d(function_name, "[SUCCESS] id[" + id + "] show_no[" + show_no + "] name[" + buy_category.getCategory_name() + "]");
    			//}
    		}catch(SQLiteException e){
    			Log.e(function_name, "SQLiteException:" + e.getMessage());
    			e.printStackTrace();
    			err_log += id + ",";
    			cnt_err++;

    		}catch(SQLException e){
    			Log.e(function_name, "SQLException:" + e.getMessage());
    			e.printStackTrace();
    			err_log += id + ",";
    			cnt_err++;

    		}catch(Exception  e){
    			Log.e(function_name, "Exception:" + e.getMessage());
    			e.printStackTrace();
    			err_log += id + ",";
    			cnt_err++;
    		}
		}
		db.close();
		if (cnt_err > 0){
			saveErrorMessage(err_log);
		}
		return cnt_err;
	}

	/* 買物順並び換え画面 */
	/* =====================================
	 * [UPDATE][CATEGORY]買物順の並び換え
	 * -------------------------------------
	 * 引数１：(List<BuyItem>)並び換え済のアイテムのリスト
	 * return:0(正常終了)/1↑(エラー数)
	 * ====================================*/
	public int saveRearrangeShopping(List<BuyCategory> buy_category_list ){
		String function_name = "saveRearrangeShopping";
        SQLiteDatabase db = this.getReadableDatabase();
		int id;
		int buy_no = 0;
		int cnt_err = 0;
		String err_log = "";

		Iterator<BuyCategory> ite_category = buy_category_list.iterator(); // アイテム用イテレータ
		while (ite_category.hasNext()) {
			final BuyCategory buy_category = (BuyCategory) ite_category.next(); // 次の行を取得
			id               = buy_category.getId();		// [取得]カテゴリID
			buy_no++;										// [取得]表示順
    		try{
    			/* update */
    			ContentValues values = new ContentValues();
    			values.put("buy_no", buy_no);				// [設定]表示順
    			String[] args = {Integer.toString(id)};		// [対象]カテゴリID
    			int cnt = db.update("category_table", values, "id=?", args);
    			/* log */
    			if ( cnt != 1 ){	// 失敗
    				Log.e(function_name, "[ERROR] (" + cnt + ") UPDATE fin /  id[" + id + "] show_no[" + buy_no + "] name[" + buy_category.getCategory_name() + "]");
        			err_log += id + ",";
        			cnt_err++;

    			} //else {			// 成功
    				//Log.d(function_name, "[SUCCESS] id[" + id + "] buy_no[" + buy_no + "] name[" + buy_category.getCategory_name() + "]");

    			//}
    		}catch(SQLiteException e){
    			Log.e(function_name, "SQLiteException:" + e.getMessage());
    			e.printStackTrace();
    			err_log += id + ",";
    			cnt_err++;

    		}catch(SQLException e){
    			Log.e(function_name, "SQLException:" + e.getMessage());
    			e.printStackTrace();
    			err_log += id + ",";
    			cnt_err++;

    		}catch(Exception  e){
    			Log.e(function_name, "Exception:" + e.getMessage());
    			e.printStackTrace();
    			err_log += id + ",";
    			cnt_err++;
    		}
		}
		db.close();
		if (cnt_err > 0){
			saveErrorMessage(err_log);
		}
		return cnt_err;
	}

	/* アイテム並び換え画面 */
	/* =====================================
	 * [UPDATE][ITEM]アイテムの並び換え
	 * -------------------------------------
	 * 引数１：(List<BuyItem>)並び換え済のアイテムのリスト
	 * return:0(正常終了)/1↑(エラー数)
	 * ====================================*/
	public int saveRearrangeItem(List<BuyItem> buy_item_list ){
		String function_name = "saveRearrangeItem";
        SQLiteDatabase db = this.getReadableDatabase();
		int id;
		int show_no = 0;
		int cnt_err = 0;
		String err_log = "";

		Iterator<BuyItem> ite_item = buy_item_list.iterator(); // アイテム用イテレータ
		while (ite_item.hasNext()) {
			final BuyItem buy_item = (BuyItem) ite_item.next(); // 次の行を取得
			id               = buy_item.getId();			// [取得]アイテムID
            show_no++;										// [取得]表示順
    		try{
    			/* update */
    			ContentValues values = new ContentValues();
    			values.put("show_no", show_no);				// [設定]表示順
    			String[] args = {Integer.toString(id)};		// [対象]アイテムID
    			int cnt = db.update("item_table", values, "id=?", args);
    			/* log */
    			if ( cnt != 1 ){	// 失敗
    				Log.e(function_name, "[ERROR] (" + cnt + ") UPDATE fin /  id[" + id + "] show_no[" + show_no + "] name[" + buy_item.getItem_name() + "]");
        			err_log += id + ",";
    				cnt_err++;
    			} //else {			// 成功
    				//Log.d(function_name, "[SUCCESS] id[" + id + "] show_no[" + show_no + "] name[" + buy_item.getItem_name() + "]");

    			//}
    		}catch(SQLiteException e){
    			Log.e(function_name, "SQLiteException:" + e.getMessage());
    			e.printStackTrace();
    			err_log += id + ",";
    			cnt_err++;

    		}catch(SQLException e){
    			Log.e(function_name, "SQLException:" + e.getMessage());
    			e.printStackTrace();
    			err_log += id + ",";
    			cnt_err++;

    		}catch(Exception  e){
    			Log.e(function_name, "Exception:" + e.getMessage());
    			e.printStackTrace();
    			err_log += id + ",";
    			cnt_err++;
    		}
		}
		if (cnt_err > 0){
			saveErrorMessage(err_log);
		}
		db.close();
		return cnt_err;
	}

	/* -+-+-+-+-+-+-+-+-+ INSERT -+-+-+-+-+-+-+-+-+ */

	/* クイック追加画面 */
	public int insertQuickAddItem(BuyItem buy_item){
		final String function_name = "quick_add_item";
		Log.d(function_name, "start");
		int id = -1;
		SQLiteDatabase db = getReadableDatabase();
		final SQLiteStatement statement = db.compileStatement("INSERT INTO item_table(category_id, item_name, show_no, check_flag) VALUES (?, ?, ?, 0)");
		try {
			TextView text_view   = buy_item.getCheckbox();			// [取得]アイテム名(EditText)
			String item_name     = text_view.getText().toString();	// [取得]アイテム名
			int category_id      = buy_item.getCategory_id();		// [取得]カテゴリID
			int show_no          = buy_item.getShow_no();			// [取得]表示順

			if ( item_name.length() == 0 ){
				return id;											// アイテム名をNULLにした場合は保存しない
			}

			statement.bindLong  (1, category_id);					// [設定]カテゴリID
			statement.bindString(2, item_name);						// [設定]アイテム名
			statement.bindLong  (3, show_no);						// [設定]表示順

			id = (int)statement.executeInsert();
			if ( id < 0 ){	// エラーログ
				Log.e(function_name, "[INSERT][FAILED]  id[" + id + "] name[" + item_name + "] category_id[" + category_id + "] show_no[" + show_no + "]");
			}else{			// 成功ログ
				Log.d(function_name, "[INSERT][SUCCESS]  id[" + id + "] name[" + item_name + "] category_id[" + category_id + "] show_no[" + show_no + "]");
			}
		} catch (Exception e) { // 例外発生
			Log.e(function_name, "Exception:" + e.getMessage());
			e.printStackTrace();
		} finally {
			statement.close();
			db.close();
		}
		Log.d(function_name, "finish");
		return id;
	}

	/* -+-+-+-+-+-+-+-+-+ 買物編集画面 -+-+-+-+-+-+-+-+-+ */
	/* =====================================
	 * 編集画面の保存
	 * -------------------------------------
	 *
	 * 引数１：(INT)ジャンルID
	 * 引数２：(List<BuyCategory>)保存対象となるカテゴリのリスト
	 * 引数３：(List<BuyItem>)保存対象となるアイテムのリスト
	 * 返却値:エラー件数
	 * ====================================*/
	public int save(int junle_id, List<BuyCategory> buy_category_list, List<BuyItem> buy_item_list ){
        SQLiteDatabase db = this.getReadableDatabase();
        int err_cnt = 0;
        String function_name = "save";

		/* カテゴリ保存 */
        err_cnt = saveCategory(db, junle_id, buy_category_list);

        /* [追加]カテゴリ&[追加]アイテムのひもづけ */
		Iterator<BuyCategory> iterator_category = buy_category_list.iterator();
		while (iterator_category.hasNext()) {	// カテゴリリストをループ
			final BuyCategory buycategory = (BuyCategory) iterator_category.next();
			if ( buycategory.getMod_type() != 1 ){	// [変更フラグ]="1"(追加)以外
				continue;
			}
			int category_id = buycategory.getId();
			int category_no = buycategory.getCategory_no();
			Iterator<BuyItem> iterator_item = buy_item_list.iterator();
			while (iterator_item.hasNext()) {
				final BuyItem buy_item = (BuyItem) iterator_item.next(); // 次の行を取得
				if ( buy_item.getCategory_no() != category_no ){ // [カテゴリNo.]がカテゴリとアイテムで一致しない
					continue;
				}
				buy_item.setCategory_id(category_id);	// 追加したアイテムにカテゴリIDを付加
				Log.d(function_name, "category　　id[" + category_id + "] no[" + category_no + "]");
			}
		}

		/* アイテム保存 */
        err_cnt += saveItem(db, buy_item_list);
		db.close();
		return err_cnt;
	}

	/* =====================================
	 * 【SUB】[CATEGORY]編集画面の保存
	 * -------------------------------------
	 * 機能　　：編集画面のカテゴリ全てを保存する
	 * 引数１：(SQLiteDatabase)open済みのDB
	 * 引数２：(int)ジャンルID
	 * 引数３：(List<BuyCategory>)カテゴリ編集情報
	 * 返却値 ：(0)正常終了/(1↑)失敗件数
	 * ====================================*/
	private int saveCategory(SQLiteDatabase db, int junle, List<BuyCategory> buy_category_list) {
		int cnt_err = 0;

		/* 表示順番 */
		int show_no = getMax(db, "category_table","show_no");
		if ( show_no < 0 ){
			return -1;
		}
		/* 買物順 */
		int buy_no  = getMax(db, "category_table","buy_no");
		if ( buy_no < 0 ){
			return -1;
		}

		Iterator<BuyCategory> iterator = buy_category_list.iterator();
		while (iterator.hasNext()) {
			final BuyCategory buycategory = (BuyCategory) iterator.next(); // 次の行を取得
			int mod_type = buycategory.getMod_type();				// [取得]変更タイプ
			EditText edit_text   = buycategory.getName_mod();		// [取得]カテゴリ名(EditText)
			String category_name = edit_text.getText().toString();	// [取得]カテゴリ名


			int category_id;
			switch(mod_type){
			/* 追加 */
			case 1:
				if ( category_name.length() == 0 ){
					buycategory.setId(-1);						// [設定]カテゴリID
					continue;	// 未入力なら追加しない
				}
				show_no++;											// [取得]表示順
				buy_no++;											// [取得]買物順
				category_id = (int) insertCategory(db, junle, category_name, show_no, buy_no);	// [INSERT][CATEGORY]
				if (category_id == -1){
					cnt_err++;
					break;
				}
				buycategory.setId(category_id);						// [設定]カテゴリID
				break;

			/* 変更 */
			// 未変更でも、全項目変更ありとみなして更新をかける
			case 2:
				if ( category_name.length() == 0 ){
					continue;	// 未入力なら変更しない
				}
				category_id = buycategory.getId();					// [取得]カテゴリID
				if (updateCategoryName(db, category_id, category_name) != 0){  // [UPDATE]
					cnt_err++;
				}
				break;

			/* 削除 */
			case 3: // カテゴリ削除機能は現在未実装のため現在は処理を行わない。
			default:
				break;
			}
		}
		return cnt_err;
	}

	/* =====================================
	 * 【SUB】[ITEM]編集画面の保存
	 * -------------------------------------
	 * 機能　　：編集画面のアイテム全てを保存する
	 * 引数１：open済みのDB
	 * 引数２：(List<BuyItem>)アイテム編集情報
	 * 返却値 ：(0)正常終了/(1↑)失敗件数
	 * ====================================*/
	public int saveItem(SQLiteDatabase db, List<BuyItem> buy_item_list){
		Iterator<BuyItem> iterator = buy_item_list.iterator();
		int cnt_err = 0;
		int item_id;
		int mod_type;

		while (iterator.hasNext()) {
			final BuyItem buy_item = (BuyItem) iterator.next(); // 次の行を取得
			mod_type = buy_item.getMod_type();		// [取得]変更タイプ
			item_id  = buy_item.getId();			// [取得]アイテムID

			/* 変更 */
			if ( mod_type == 2 ){
				TextView edit_text   = buy_item.getEdit_text();			// [取得]アイテム名(TextView)
				String item_name     = edit_text.getText().toString();	// [取得]アイテム名
				if (updateItemName(db, item_id, item_name) != 0){		// [UPDATE]
	    			saveErrorMessage("mod item (" + item_id + ")" + item_name + "]");
					cnt_err++;
				}
			/* 削除 */
			}else if ( mod_type == 3 ){
				if (deleteItem(db, item_id) != 0){  				// [DELETE]
	    			saveErrorMessage("del item (" + item_id + ")");
					cnt_err++;
				}
			}
		}

		/* 追加 */
		insertItem(db,buy_item_list);
		return cnt_err;
	}

	/* =====================================
	 * 【SUB】[INSERT][CATEGORY]カテゴリ追加
	 * -------------------------------------
	 * 引数１：open済みのDB
	 * 引数２：ジャンルID
	 * 引数３：カテゴリ名
	 * 返却値:(long)挿入したカテゴリID/異常時-1
	 * ====================================*/
	public long insertCategory(SQLiteDatabase db, int junle_id, String category_name, int show_no, int buy_no){
		String function_name = "insertCategory";
		long id = -1;
        try{
        	ContentValues cv=new ContentValues();
        	cv.put("category_name", category_name);	// [設定]カテゴリ名
        	cv.put("show_flag", 1);					// [設定]表示フラグ = 1
        	cv.put("show_no", show_no);				// [設定]表示順番
        	cv.put("junle_id", junle_id);			// [設定]ジャンルID
        	cv.put("buy_no", buy_no);				// [設定]買物順
        	id=db.insert("category_table", null, cv);

		}catch(SQLiteException e){
			saveErrorMessage("add category err[1] [" + category_name + "](" + show_no + ")(" + junle_id + ")(" + buy_no + ")(" + id + ")");
			Log.e(function_name, "SQLiteException:" + e.getMessage());
			e.printStackTrace();

		}catch(SQLException e){
			saveErrorMessage("add category err[2] [" + category_name + "](" + show_no + ")(" + junle_id + ")(" + buy_no + ")(" + id + ")");
			Log.e(function_name, "SQLException:" + e.getMessage());
			e.printStackTrace();

		}catch(Exception  e){
			saveErrorMessage("add category err[3] [" + category_name + "](" + show_no + ")(" + junle_id + ")(" + buy_no + ")(" + id + ")");
			Log.e(function_name, "Exception:" + e.getMessage());
			e.printStackTrace();

        }finally{
            //db.close();
    	}
        // ログ
        if ( id == -1 ){	// エラー
    		Log.e(function_name,"[FAILED][INSERT]  id[" + id + "] name[" + category_name + "]　show_no[" + show_no + "] buy_no[" + buy_no + "]");
			saveErrorMessage("add category err[4] [" + category_name + "](" + show_no + ")(" + junle_id + ")(" + buy_no + ")(" + id + ")");
        }else{				// 成功
    		Log.d(function_name,"[SUCCESS][INSERT] id[" + id + "] name[" + category_name + "] show_no[" + show_no + "] buy_no[" + buy_no + "]");
        }
		return id;
	}

	/* =====================================
	 * 【SUB】[UPDATE][CATEGORY]カテゴリ名変更
	 * -------------------------------------
	 * 引数１：open済みのDB
	 * 引数２:変更対象となるカテゴリID
	 * 引数３：変更後のカテゴリ名
	 * 返却値:0(正常終了)/-1(異常終了)
	 * ====================================*/
	public int updateCategoryName(SQLiteDatabase db, int category_id, String category_name){
		String function_name = "update_category_name";
        //SQLiteDatabase db = this.getReadableDatabase();
		try{
			ContentValues values = new ContentValues();
			values.put("category_name", category_name);
			String[] args = {Integer.toString(category_id)};
			int cnt = db.update("category_table", values, "id=?", args);
			if ( cnt != 1 ){
				Log.e(function_name, "[ERROR] (" + cnt + ") UPDATE fin /  id[" + category_id + "] name[" + category_name + "]");
				return -1;
			}
		}catch(SQLiteException e){
			Log.e(function_name, "SQLiteException:" + e.getMessage());
			saveErrorMessage("mod category err[1] (" + category_id + ")[" + category_name + "]");
			e.printStackTrace();
			return -1;

		}catch(SQLException e){
			Log.e(function_name, "SQLException:" + e.getMessage());
			saveErrorMessage("mod category err[2] (" + category_id + ")[" + category_name + "]");
			e.printStackTrace();
			return -1;

		}catch(Exception  e){
			Log.e(function_name, "Exception:" + e.getMessage());
			saveErrorMessage("mod category err[3] (" + category_id + ")[" + category_name + "]");
			e.printStackTrace();
			return -1;
		}finally{
            //db.close();
    	}
		Log.d("update_category","[SUCCESS][UPDATE]  id[" + category_id + "] name[" + category_name + "]");
		return 0;
	}

	/* =====================================
	 * 【SUB】[INSERT][ITEM]アイテム追加
	 * -------------------------------------
	 * 引数１：open済みのDB
	 * 引数2：変更後のアイテム名
	 * return:(0)正常終了/(-1)異常発生により追加を行わず終了/(1↑)最後まで処理を行ったが整数値ぶんinsert失敗
	 * ====================================*/
	public int insertItem(SQLiteDatabase db, List<BuyItem> buy_item_list){
		String function_name = "insert_item";
		int cnt_err = 0;

		/* [取得]表示順番 */
		int show_no = getMax(db,"item_table","show_no");
		if ( show_no < 0 ){
			return -1;
		}

		// 【DB】トランザクション開始
		db.beginTransaction();
		try {
			final SQLiteStatement statement = db.compileStatement("INSERT INTO item_table(category_id, item_name, show_no, check_flag) VALUES (?, ?, ?, 0)");
			try {
				Iterator<BuyItem> iterator = buy_item_list.iterator();
				while (iterator.hasNext()) {
					final BuyItem buy_item = (BuyItem) iterator.next(); // 次の行を取得
					int mod_type = buy_item.getMod_type();		// [取得]変更タイプ
					if ( mod_type != 1 ){	// [変更フラグ] = 1(追加)のみ対象
						continue;
					}
					TextView edit_text   = buy_item.getEdit_text();			// [取得]アイテム名(EditText)
					String item_name     = edit_text.getText().toString();	// [取得]アイテム名
					if ( item_name.length() == 0 ){
						continue;											// アイテム名をNULLにした場合は保存しない
					}
					int category_id      = buy_item.getCategory_id();		// [取得]カテゴリID
					if ( category_id < 0 ){
						continue;											// カテゴリ追加失敗している場合保存しない
					}
					show_no++;												// [取得]表示順
					statement.bindLong  (1, category_id);					// [設定]カテゴリID
					statement.bindString(2, item_name);						// [設定]アイテム名
					statement.bindLong  (3, show_no);						// [設定]表示順
					long id = statement.executeInsert();					// [INSERT]
					if ( id < 0 ){	// エラーログ
						Log.e(function_name, "[INSERT][FAILED]  id[" + id + "] name[" + item_name + "] category_id[" + category_id + "] show_no[" + show_no + "]");
						saveErrorMessage("add item err[1] (" + id + ")[" + item_name + "](" + show_no + ")");
						cnt_err++;
					}else{			// 成功ログ
						Log.d(function_name, "[INSERT][SUCCESS]  id[" + id + "] name[" + item_name + "] category_id[" + category_id + "] show_no[" + show_no + "]");
					}
				}

			} catch (SQLiteException e) {
				Log.e(function_name, "SQLiteException:" + e.getMessage());
				saveErrorMessage("add item err[2]");
				e.printStackTrace();
				return -1;

			} catch (SQLException e) {
				Log.e(function_name, "SQLException:" + e.getMessage());
				saveErrorMessage("add item err[3]");
				e.printStackTrace();
				return -1;

			} catch (Exception e) {
				Log.e(function_name, "Exception:" + e.getMessage());
				saveErrorMessage("add item err[4]");
				e.printStackTrace();
				return -1;

			} finally {
				statement.close();
			}
			// 【DB】成功(-> トランザクション終了時にcommitを行う)
			db.setTransactionSuccessful();

		} catch (SQLiteException e) {
			Log.e(function_name, "SQLiteException:" + e.getMessage());
			saveErrorMessage("add item err[5]");
			e.printStackTrace();
			return -1;

		} catch (SQLException e) {
			Log.e(function_name, "SQLException:" + e.getMessage());
			saveErrorMessage("add item err[6]");
			e.printStackTrace();
			return -1;

		} catch (Exception e) {
			Log.e(function_name, "Exception:" + e.getMessage());
			saveErrorMessage("add item err[7]");
			e.printStackTrace();
			return -1;

		} finally {
			// 【DB】トランザクション終了
			db.endTransaction();
		}
		return cnt_err;
	}

	/* =====================================
	 * 【SUB】[UPDATE][ITEM]アイテム名変更
	 * -------------------------------------
	 * 引数１：open済みのDB
	 * 引数２:変更対象となるアイテムID
	 * 引数３：変更後のアイテム名
	 * return:0(正常終了)/-1(異常終了)
	 * ====================================*/
	public int updateItemName(SQLiteDatabase db, int item_id, String item_name){
		String function_name = "updateItemName";
		if ( item_name.length() == 0 ){
			return 0;											// アイテム名をNULLにした場合は保存しない
		}
		try{
			ContentValues values = new ContentValues();
			values.put("item_name", item_name);
			String[] args = {Integer.toString(item_id)};
			int cnt = db.update("item_table", values, "id=?", args);
			if ( cnt != 1 ){	// エラーログ
				Log.e(function_name, "[UPDATE][ERROR]   (" + cnt + ") UPDATE fin /  id[" + item_id + "] name[" + item_name + "]");
				saveErrorMessage("mod item err[1] (" + item_id + ")[" + item_name + "](" + cnt + ")");
				return -1;
			}//else{				// 成功ログ
				//Log.d(function_name, "[UPDATE][SUCCESS] (" + cnt + ") UPDATE fin /  id[" + item_id + "] name[" + item_name + "]");
			//}
		}catch(SQLiteException e){
			Log.e(function_name, "SQLiteException:" + e.getMessage());
			saveErrorMessage("mod item err[2] (" + item_id + ")[" + item_name + "]");
			e.printStackTrace();
			return -1;

		}catch(SQLException e){
			Log.e(function_name, "SQLException:" + e.getMessage());
			saveErrorMessage("mod item err[3] (" + item_id + ")[" + item_name + "]");
			e.printStackTrace();
			return -1;

		}catch(Exception  e){
			Log.e(function_name, "Exception:" + e.getMessage());
			saveErrorMessage("mod item err[4] (" + item_id + ")[" + item_name + "]");
			e.printStackTrace();
			return -1;

		}
		return 0;
	}

	/* =====================================
	 * 【SUB】[DELETE][ITEM]アイテム削除
	 * -------------------------------------
	 * 引数１：open済みのDB
	 * 引数２:削除対象となるアイテムID
	 * return:0(正常終了)/-1(異常終了)
	 * ====================================*/
	public int deleteItem(SQLiteDatabase db, int item_id){
		String function_name = "deleteItem";
		if ( item_id == 0 ){
			Log.d(function_name, "id[" + item_id + "] DB未登録のため消去せず");
			return 0;
		}

		try{
			String[] args = {Integer.toString(item_id)};
			int cnt = db.delete("item_table","id=?", args);
			if ( cnt != 1 ){	// エラーログ
				Log.e(function_name, "[DELETE][ERROR]   (" + cnt + ") UPDATE fin /  id[" + item_id + "]");
				saveErrorMessage("del item err[1] (" + item_id + ")(" + cnt + ")");
				return -1;
			}else{				// 成功ログ
				Log.d(function_name, "[DELETE][SUCCESS] (" + cnt + ") UPDATE fin /  id[" + item_id + "]");
			}
		}catch(SQLiteException e){
			Log.e(function_name, "SQLiteException:" + e.getMessage());
			saveErrorMessage("del item err[2] (" + item_id + ")");
			e.printStackTrace();
			return -1;

		}catch(SQLException e){
			Log.e(function_name, "SQLException:" + e.getMessage());
			saveErrorMessage("del item err[3] (" + item_id + ")");
			e.printStackTrace();
			return -1;

		}catch(Exception  e){
			Log.e(function_name, "Exception:" + e.getMessage());
			saveErrorMessage("del item err[4] (" + item_id + ")");
			e.printStackTrace();
			return -1;

		}

		return 0;
	}

	/* -+-+-+-+-+-+-+-+-+ CSV取り込み -+-+-+-+-+-+-+-+-+ */
	public int getTableFromCsv(CSVReader csv_category, CSVReader csv_item){
		String function_name = "getItemTableFromCsv";
		SQLiteDatabase db = openDB();
		if (  db == null ){
			return -1;
		}
		db.beginTransaction(); // トランザクション開始

		/* カテゴリテーブル削除 */
		if ( deleteTable(db,"category_table") != 0){
			db.endTransaction();	// トランザクション終了
			return -1;
		}

		/* アイテムテーブル削除 */
		if ( deleteTable(db,"item_table") != 0){
			db.endTransaction();	// トランザクション終了
			return -1;
		}

		/* カテゴリテーブル作成 */
		if ( createCategoryTable(db) != 0){
			db.endTransaction();	// トランザクション終了
			return -1;
		}

		/* アイテムテーブル作成 */
		if ( createItemTable(db) != 0){
			db.endTransaction();	// トランザクション終了
			return -1;
		}

		int cnt_err = 0;

		/* カテゴリテーブルをCSVから読込 */
		SQLiteStatement statement = compileStatement(db,"INSERT INTO category_table(id, category_name, show_flag, junle_id, show_no, buy_no) VALUES (?,?,1,?,?,?)");
		if( statement == null){
			db.endTransaction();	// トランザクション終了
			return -1;
		}
		int id;
		int show_no;
		int jnle_id;
		int buy_no;
		try {
			    String [] next_line;
			    csv_category.readNext(); // １行目をスキップ
			    while ((next_line = csv_category.readNext()) != null) {
			    	id = Integer.valueOf(next_line[0]).intValue();		// [取得]カテゴリID
			    	String category = next_line[1];						// [取得]カテゴリ名
					if ( category.length() == 0 ){
						continue;										// アイテム名をNULLにした場合は保存しない
					}
					jnle_id = Integer.valueOf(next_line[2]).intValue();	// [取得]ジャンルID
					show_no = Integer.valueOf(next_line[3]).intValue();	// [取得]表示順
					buy_no  = Integer.valueOf(next_line[4]).intValue();	// [取得]買物順

					statement.bindLong  (1, id);						// [設定]カテゴリID
					statement.bindString(2, category);					// [設定]カテゴリ名
					statement.bindLong  (3, jnle_id);					// [設定]ジャンルID
					statement.bindLong  (4, show_no);					// [設定]表示順
					statement.bindLong  (5, buy_no);					// [設定]買物順
					long ret = statement.executeInsert();				// [INSERT]
					if ( ret < 0 ){	// エラーログ
						Log.e(function_name, "[INSERT][FAILED]  id[" + id + "] name[" + category + "] jnle_id[" + jnle_id + "] show_no[" + show_no + "]");
						cnt_err++;
					} else{			// 成功ログ
						Log.d(function_name, "[INSERT][SUCCESS]  id[" + id + "] name[" + category + "] jnle_id[" + jnle_id + "] show_no[" + show_no + "]");
					}
					show_no++;													// [更新]カテゴリID
			    }

		} catch (Exception e) {
			Log.e(function_name, "Exception:" + e.getMessage());
			e.printStackTrace();
			db.endTransaction();	// トランザクション終了
			return -1;

		}

		/* アイテムテーブルをCSVから読込 */
		/* 行追加 */
		long item_id;
		show_no = 1;
		int category_id;
		statement = compileStatement(db,"INSERT INTO item_table(category_id, item_name, show_no, check_flag) VALUES (?, ?, ?, 0)");	//これいけるのかな
		if( statement == null){
			db.endTransaction();	// トランザクション終了
			return -1;
		}

		try {
			    String [] next_line;
			    csv_item.readNext(); // １行目をスキップ
			    while ((next_line = csv_item.readNext()) != null) {
			    	category_id = Integer.valueOf(next_line[0]).intValue();	// [取得]カテゴリID
			    	String item = next_line[1];							// [取得]アイテム名
					if ( item.length() == 0 ){
						continue;										// アイテム名をNULLにした場合は保存しない
					}
					statement.bindLong  (1, category_id);				// [設定]カテゴリID
					statement.bindString(2, item);						// [設定]アイテム名
					statement.bindLong  (3, show_no);					// [設定]表示順
					item_id = statement.executeInsert();						// [INSERT]
					if ( item_id < 0 ){	// エラーログ
						Log.e(function_name, "[INSERT][FAILED]  id[" + item_id + "] name[" + item + "] category_id[" + category_id + "] show_no[" + show_no + "]");
						cnt_err++;
					} else{			// 成功ログ
						Log.d(function_name, "[INSERT][SUCCESS]  id[" + item_id + "] name[" + item + "] category_id[" + category_id + "] show_no[" + show_no + "]");
					}
					show_no++;													// [更新]カテゴリID
			    }

		} catch (Exception e) {
			Log.e(function_name, "Exception:" + e.getMessage());
			e.printStackTrace();
			db.endTransaction();	// トランザクション終了
			return -1;

		} finally {
			statement.close();
		}


		// 【DB】成功(-> トランザクション終了時にcommitを行う)
		db.setTransactionSuccessful();

		// トランザクション終了
		db.endTransaction();

		return cnt_err;
	}

	/* =====================================
	 * 【SUB】[CREATE][TABLE]カテゴリテーブル作成
	 * -------------------------------------
	 * return:0(正常終了)/-1(異常終了)
	 * ====================================*/
	private int createCategoryTable(SQLiteDatabase db){
		String sql = "CREATE TABLE category_table("
				+ "id INTEGER PRIMARY KEY, "   		// id
				+ "category_name TEXT NOT NULL,"   	// カテゴリ名
				+ "show_flag INTEGER NOT NULL, " 	// 0:非表示,1:表示
				+ "show_no INTEGER NOT NULL, " 		// 表示順番
				+ "junle_id INTEGER NOT NULL, " 	// 0:その他,1:食材,2:消耗品
				+ "buy_no INTEGER NOT NULL);";		// 買物順
		return execSql(db, sql);
	}

	/* =====================================
	 * 【SUB】[CREATE][TABLE]アイテムテーブル作成
	 * -------------------------------------
	 * return:0(正常終了)/-1(異常終了)
	 * ====================================*/
	private int createItemTable(SQLiteDatabase db){
		// アイテム
		String sql = "CREATE TABLE item_table("
				+ "id INTEGER PRIMARY KEY,"         // id
				+ "category_id INTEGER NOT NULL,"   // カテゴリID
				+ "item_name TEXT NOT NULL,"        // アイテム名
				+ "show_no INTEGER NOT NULL," 		// 表示順番
				+ "check_flag INTEGER NOT NULL,"    // チェックボックス
				+ "buy_date TEXT,"                  // 購入日
				+ "memo TEXT);";                    // メモ
		return execSql(db, sql);
	}

	/* =====================================
	 * 【SUB】[DELETE][TABLE]テーブル削除
	 * -------------------------------------
	 * 引数１：テーブル名
	 * return:0(正常終了)/-1(異常終了)
	 * ====================================*/
	private int deleteTable(SQLiteDatabase db, String table){
		String function_name = "deleteTable";
    	Log.d(function_name, "delete table[" + table + "]");
		String sql = "drop table " + table + ";";
		return execSql(db, sql);
	}

	/* =====================================
	 * 【SUB】SQL文を実行する
	 * -------------------------------------
	 * return:(SQLiteDatabase)opn済みデータベース/(異常)null
	 * ====================================*/
	private int execSql(SQLiteDatabase db, String sql){
		String function_name = "execSql";
        try{
		    db.execSQL(sql);
        }catch(SQLiteException e){
			Log.e(function_name, "SQL[" + sql + "]");
			Log.e(function_name, "SQLiteException:" + e.getMessage());
			e.printStackTrace();
			return -1;

		}catch(SQLException e){
			Log.e(function_name, "SQLException:" + e.getMessage());
			e.printStackTrace();
			return -1;

		}catch(Exception  e){
			Log.e(function_name, "Exception:" + e.getMessage());
			e.printStackTrace();
			return -1;
		}
		return 0;
	}

	/* =====================================
	 * 【SUB】データベースをオープンする
	 * -------------------------------------
	 * return:(SQLiteDatabase)opn済みデータベース/(異常)null
	 * ====================================*/
	private SQLiteDatabase openDB(){
		String function_name = "openDB";
		SQLiteDatabase db = null;
		try {
	        db = this.getReadableDatabase();
		}catch(SQLiteException e){
			Log.e(function_name, "SQLiteException:" + e.getMessage());
			e.printStackTrace();

		}catch(SQLException e){
			Log.e(function_name, "SQLException:" + e.getMessage());
			e.printStackTrace();

		}catch(Exception  e){
			Log.e(function_name, "Exception:" + e.getMessage());
			e.printStackTrace();
		}
		return db;
	}


	/* =====================================
	 * 【SUB】SQLiteStatement初期化
	 * -------------------------------------
	 * 引数１：open済みのDB
	 * 引数２：SQL文
	 * return:(SQLiteStatement)ステートメント(異常時null)
	 * ====================================*/
	private SQLiteStatement compileStatement(SQLiteDatabase db, String statement){
		SQLiteStatement sqlite_statement = null;
		String function_name = "compileStatement";
		try {
			sqlite_statement = db.compileStatement(statement);

		} catch (SQLiteException e) {
			String msg = "[" + function_name + "]SQLiteException:" + e.getMessage();
			saveErrorMessage(msg);
			Log.e(function_name, msg);
			e.printStackTrace();
			return null;

		} catch (SQLException e) {
			String msg = "[" + function_name + "]SQLException:" + e.getMessage();
			saveErrorMessage(msg);
			Log.e(function_name, msg);
			e.printStackTrace();
			return null;

		} catch (Exception e) {
			String msg = "[" + function_name + "]Exception:" + e.getMessage();
			saveErrorMessage(msg);
			Log.e(function_name, msg);
			e.printStackTrace();
			return null;

		}
		return sqlite_statement;
	}

	/* エラーメッセージ保存「dbError」 */
	private void saveErrorMessage(String message){
		pref = ctxt.getSharedPreferences("preference", Activity.MODE_PRIVATE);
		editor = pref.edit();
		editor.putString("dbError", message);
		editor.commit();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO 自動生成されたメソッド・スタブ

	}


}
