package com.common.lib.activity.db;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


import com.common.lib.bean.ChainBean;
import com.common.lib.utils.LogUtil;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context context, String strDBName) {
        super(context, strDBName, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        LogUtil.LogE("databaseHelper: onCreate");
        createMsgTable(db);
        String sql = "create table chain(" +   //公链表
                "chainId integer primary key," +  //链Id
                "chainName text," +   //
                "rpcUrl text," +
                "symbol text," + //货币符号
                "explore text" +  //区块浏览器 URL
                ")";
        db.execSQL(sql);
        sql = "create table wallet(" +   //钱包表
                "id integer primary key autoincrement," +
                "walletName varchar(50)," +  //钱包名字
                "chainId integer," +  //所属公链
                "mnemonic text," +  //助记词
                "privateKey text," +//私钥
                "publicKey text," +//公钥
                "address text," +//钱包地址
                "keystorePath text," +  //keystore存的路径
                "keystore text," +
                "password varchar(50)," +//密码
                "walletType text," +//钱包类型 例如 utg eth
                "money text," +//余额
                "createTime long" +
                ")";
        db.execSQL(sql);
        sql = "create table 'transaction'(" +
                "hash varchar(50) primary key," +
                "blockNumber text," +
                "'from' text," +
                "gas text," +
                "gasPrice text," +
                "gasUsed text," +
                "cumulativeGasUsed text," +
                "'to' text," +
                "value text," +
                "timeStamp long" +
                ")";
        db.execSQL(sql);
        sql = "create table token(" +   //代币表
                "id integer primary key autoincrement," +
                "chainId integer," +  //代币所属公链
                "contractAddress text," +  //代币合约地址
                "symbol varchar(50)," +  //代币符号
                "tokenPrecision integer," +//代币精度
                "walletAddress text," +  //钱包地址
                "balance text" +  //代币余额
                ")";
        db.execSQL(sql);
        initData(db);
        LogUtil.LogE("databaseHelper: initData");
    }

    private void initData(SQLiteDatabase db) {
        // insert(db, new ChainBean(188, "Glow Testnet", "https://rpc.glowtest.net", "UTG", ""));
        //  insert(db, new ChainBean(188, "UltronGlow Mainnet", "https://rpc.ultronglow.io", "UTG", ""));
        insert(db, new ChainBean(97, "Binance Smart Chain Testnet", "https://data-seed-prebsc-1-s1.binance.org:8545", "BNB", "https://api-testnet.bscscan.com"));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("DatabaseHelper", "onUpgrade: " + oldVersion + ", " + newVersion);
    }

    public void createMsgTable(SQLiteDatabase db) {
        String sql = "create table message(" +   //消息表
                "messageId varchar(100)," +
                "cmd int," +
                "msgType int," +
                "fromId long," +
                "toId long," +
                "sendStatus int," +
                "receiveStatus int," +
                "content text," +
                "translate text," +
                "url text," +
                "createTime long," +
                "expire long," +
                "isDel byte," +
                "isRead byte," +
                "fileProgress int," +
                "extra text," +
                "owerId long," +  //消息所属id
                "tag varchar(100)," +  //两者id组合，用来做group by
                "primary key(messageId,owerId)" +
                ")";
        db.execSQL(sql);
        sql = "create table group_message(" +   //消息表
                "messageId varchar(100)," +
                "cmd int," +
                "msgType int," +
                "fromId long," +
                "groupId long," +
                "sendStatus int," +
                "receiveStatus int," +
                "content text," +
                "translate text," +
                "url text," +
                "createTime long," +
                "expire long," +
                "isDel byte," +
                "isRead byte," +
                "fileProgress int," +
                "owerId long," +  //消息所属id
                "extra text," +
                "primary key(messageId,owerId)" +
                ")";
        db.execSQL(sql);
    }

    /**
     * 插入一条数据 IDBItemOperation
     *
     * @return
     */
    private long insert(SQLiteDatabase db, IDBItemOperation dbItem) {
        long result = db.insert(dbItem.getTableName(), null, dbItem.getValues());
        return result;
    }


    private <T extends IDBItemOperation> ArrayList<T> getAll(SQLiteDatabase db, String tableName, Class cls) {
        String sql = "select * from " + tableName + " where isdel=0";
        ArrayList<T> list = getList(db, sql, cls);
        if (list == null) {
            list = new ArrayList<>();
        }
        return list;
    }

    private <T extends IDBItemOperation> ArrayList<T> getList(SQLiteDatabase db, String strSql, Class<T> clz) {
        Cursor cursor = db.rawQuery(strSql, null);
        if (cursor == null) {
            return null;
        }
        ArrayList<T> dbObjectList = new ArrayList<T>();
        try {
            T dbObject;
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
                    .moveToNext()) {
                dbObject = clz.newInstance();
                dbObject.setDataByCursor(cursor);
                dbObjectList.add(dbObject);
            }
            if (dbObjectList.size() > 0) {
                return dbObjectList;
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            cursor.close();
        }
        return null;
    }


}
