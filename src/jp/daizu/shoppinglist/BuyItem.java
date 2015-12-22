
package jp.daizu.shoppinglist;

import java.io.Serializable;

import android.widget.CheckBox;
import android.widget.EditText;

@SuppressWarnings("serial")
public class BuyItem implements Serializable {

    // アイテム
    private int id = 0;					// アイテムID
    private int category_id = 0;		// カテゴリID
    private int category_no = 0;		// カテゴリ表示順
    private String item_name = null;	// アイテム名
    private int show_no = 0;			// アイテム表示順
    private boolean check_flag;			// チェック有無
    private String buy_date;			// 購入日
    private CheckBox checkbox;			// チェックボックス
    private EditText edit_text;			// 編集用
    private String memo;				// メモ
    private int mod_type = 2; 			// 0(変更なし)/1(追加)/2(修正)/3(削除)

    // setter / getter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getItem_name() {
        return item_name;
    }

    public void setItem_name(String item_name) {
        this.item_name = item_name;
    }

    public int getShow_no() {
        return show_no;
    }

    public void setShow_no(int show_no) {
        this.show_no = show_no;
    }

    public String getBuy_date() {
        return buy_date;
    }

    public void setBuy_date(String buy_date) {
        this.buy_date = buy_date;
    }

    // コンストラクタ
    public BuyItem() {

    }

	public int getCategory_id() {
		return category_id;
	}

	public void setCategory_id(int category_id) {
		this.category_id = category_id;
	}

	public CheckBox getCheckbox() {
		return checkbox;
	}

	public void setCheckbox(CheckBox checkbox) {
		this.checkbox = checkbox;
	}

	public boolean getCheck_flag() {
		return check_flag;
	}

	public void setCheck_flag(boolean check_flag) {
		this.check_flag = check_flag;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public EditText getEdit_text() {
		return edit_text;
	}

	public void setEdit_text(EditText edit_text) {
		this.edit_text = edit_text;
	}

	public int getMod_type() {
		return mod_type;
	}

	public void setMod_type(int mod_type) {
		this.mod_type = mod_type;
	}

	public int getCategory_no() {
		return category_no;
	}

	public void setCategory_no(int category_no) {
		this.category_no = category_no;
	}

}
