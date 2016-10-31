package com.encryption.biz;

/**
 * Created by cy002 on 2016/10/7.
 */
public interface AsyncHandleCallback {

    /** 操作开始 */
    public void onStart();

    /** 操作成功 */
    public void onSucc(Object obj);

    /** 操作异常 */
    public void onErr(int errCode, String errMsg);

    /** 操作结束 */
    public void onFinish(Object obj);
}
