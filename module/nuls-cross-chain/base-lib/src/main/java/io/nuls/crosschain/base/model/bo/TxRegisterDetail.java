package io.nuls.crosschain.base.model.bo;
import io.nuls.crosschain.nuls.utils.enumeration.TxProperty;

/**
 * 交易注册类
 * Transaction registration class
 *
 * @author tag
 * 2018/11/30
 * */
public class TxRegisterDetail {
    /**
    * 交易类型
    * Transaction type
    * */
    private int txType;
    /**
    * 交易验证方法
    * Transaction verification method
    * */
    private String validator;
    /**
     * 是否是系统交易
     * Is it a system transaction
     * */
    private boolean systemTx;
    /**
     * 是否是解锁交易
     * Is it a system transaction
     * */
    private boolean unlockTx;
    /**
     * 交易是否需要签名
     * Is it a system transaction
     * */
    private boolean verifySignature;

    public TxRegisterDetail(int txType){
        this.txType = txType;
        this.systemTx = false;
        this.unlockTx = false;
        this.verifySignature = true;
        this.validator = "";
    }

    public TxRegisterDetail(TxProperty txProperty){
        this.txType = txProperty.txType;
        this.systemTx = txProperty.systemTx;
        this.unlockTx = txProperty.unlockTx;
        this.verifySignature = txProperty.verifySignature;
        this.validator = "";
    }

    public TxRegisterDetail(int txType,String validator){
        this.txType = txType;
        this.systemTx = false;
        this.unlockTx = false;
        this.verifySignature = true;
        this.validator = validator;
    }

    public TxRegisterDetail(int txType,boolean systemTx,boolean unlockTx,boolean verifySignature,String validator){
        this.txType = txType;
        this.systemTx = systemTx;
        this.unlockTx = unlockTx;
        this.verifySignature = verifySignature;
        this.validator = validator;
    }

    public int getTxType() {
        return txType;
    }

    public void setTxType(int txType) {
        this.txType = txType;
    }

    public String getValidator() {
        return validator;
    }

    public void setValidator(String validator) {
        this.validator = validator;
    }

    public boolean isSystemTx() {
        return systemTx;
    }

    public void setSystemTx(boolean systemTx) {
        this.systemTx = systemTx;
    }

    public boolean isUnlockTx() {
        return unlockTx;
    }

    public void setUnlockTx(boolean unlockTx) {
        this.unlockTx = unlockTx;
    }

    public boolean isVerifySignature() {
        return verifySignature;
    }

    public void setVerifySignature(boolean verifySignature) {
        this.verifySignature = verifySignature;
    }
}
