package com.example.myapplication.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DataBase extends SQLiteOpenHelper {
	private final static String TAG = "DataBase";

	private final static String TABLE_NAME = "EquipmentInfo";
	private final static String DATABASE_NAME = "targetEquipments.db";
	private final static String ID = "id"; //
	private SQLiteDatabase db = null;

	public DataBase(Context context,  int DATABASE_VERSION) {
        super(context,DATABASE_NAME, null, DATABASE_VERSION); 
    }
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		String sql = "CREATE TABLE " + TABLE_NAME +
				" (" +
				ID + " INTEGER primary key autoincrement, " +"name TEXT, num TEXT, pic TEXT, region, TEXT)";
				db.execSQL(sql); 
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		String sql = "DROP TABLE IF EXISTS " + TABLE_NAME;
		db.execSQL(sql); 
		onCreate(db); 
	}
	
	
	/**
	 * insert data
	 * @param equipment
	 * @return
	 */
	public long insert(EquipmentBean equipment)
	{ 
		db = getWritableDatabase();

		/* ContentValues */ 
		ContentValues cv = new ContentValues();
		cv.put("name", equipment.getName());
		cv.put("num", equipment.getId());
		cv.put("pic", equipment.getPicture());
		cv.put("region", equipment.getRegion());

		long row = db.insert(TABLE_NAME, null, cv); 
		db.close();
		return row; 
	} 
	
	/**
	 * delete data
	 * @param equipmentID
	 * @return
	 */
	public long delete(String equipmentID)
	{
		db = getWritableDatabase();
		String where = "num = ?";
		String[] whereValue ={equipmentID};
		long row = db.delete(TABLE_NAME, where, whereValue);
		db.close();
		return row;
	}

	/**
	 * query data
	 * @param QueryContent 要查询的字段名
	 * @return  返回时一个cursor 也就是一个结果集
	 */
	public Cursor query(String QueryContent)
	{
		db = getWritableDatabase();
		Cursor cursor = null;
		String str = null;

		str = "select "+QueryContent +" from "+ TABLE_NAME;
		cursor = db.rawQuery(str,null);

		return cursor;
	}

	/*
	 * 查询记录
	 * @return 获取到的行记录
	 */
	public List<EquipmentBean> queryEquipments(){
		Cursor c = null;
		SQLiteDatabase db = null;
		List<EquipmentBean> result = new ArrayList<>();

		db = this.getReadableDatabase();
		c = db.query(TABLE_NAME, null,    null,null,null,null,null,null);

		c.moveToFirst();
		while(!c.isAfterLast()){
			EquipmentBean EquipmentBean = new EquipmentBean();

			EquipmentBean.setName(c.getString(1));
			EquipmentBean.setId(c.getString(2));
			EquipmentBean.setPicture(c.getString(3));
			EquipmentBean.setRegion(c.getString(4));

			if ((result.indexOf(c.getString(2))==-1)&&(c.getString(2) != null)){
				result.add(EquipmentBean);;   // 将从数据库查出来的数据放到list集合中
			}

			c.moveToNext();
		}
		if(!c.isClosed()){
			c.close();
		}
		if(db.isOpen()){
			db.close();
		}
		return result;
	}

	/*
	 * 删除一条用户记录
	 * @return 是否删除成功
	 */
	public long delete(EquipmentBean equipment)
	{
		SQLiteDatabase db=null;
		db=this.getWritableDatabase();
		String where= "num=?";
		String[] whereValue={equipment.getId()};
		long result = db.delete(TABLE_NAME, where, whereValue);

		db.close();
		return result;
	}

}
