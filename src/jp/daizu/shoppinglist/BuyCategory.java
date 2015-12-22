
package jp.daizu.shoppinglist;

import java.io.Serializable;

import android.widget.EditText;

@SuppressWarnings("serial")
public class BuyCategory implements Serializable {

    // カテゴリ
    private int id = 0;						// カテゴリID
    private int junle_id=0;					// ジャンルID
    private int category_no = 0;			// カテゴリ表示順
    private String category_name = null;	// カテゴリ名
    private int show_no = 0;				// 表示順
    private EditText name_mod;				// カテゴリ名編集用
    private int mod_type = 2; 				// 1(追加)/2(修正)/3(削除)

    // setter / getter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public int getShow_no() {
        return show_no;
    }

    public void setShow_no(int show_no) {
        this.show_no = show_no;
    }


    // コンストラクタ
    public BuyCategory() {

    }

	public EditText getName_mod() {
		return name_mod;
	}

	public void setName_mod(EditText name_mod) {
		this.name_mod = name_mod;
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


	public int getJunle_id() {
		return junle_id;
	}

	public void setJunle_id(int junle_id) {
		this.junle_id = junle_id;
	}

}
