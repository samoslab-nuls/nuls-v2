package io.nuls.crosschain.nuls;

import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.Log;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.core.rpc.info.HostInfo;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.modulebootstrap.Module;
import io.nuls.core.rpc.modulebootstrap.NulsRpcModuleBootstrap;
import io.nuls.core.rpc.modulebootstrap.RpcModuleState;
import io.nuls.crosschain.base.BaseCrossChainBootStrap;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConfig;
import io.nuls.crosschain.nuls.model.bo.Chain;
import io.nuls.crosschain.nuls.rpc.call.NetWorkCall;
import io.nuls.crosschain.nuls.utils.manager.ChainManager;

import java.lang.reflect.Field;
import java.nio.charset.Charset;

import static io.nuls.crosschain.nuls.constant.NulsCrossChainConstant.*;
import static java.nio.charset.StandardCharsets.UTF_8;


/**
 * 跨链模块启动类
 * Cross Chain Module Startup and Initialization Management
 * @author tag
 * 2019/4/10
 */
@Component
public class NulsCrossChainBootStrap extends BaseCrossChainBootStrap {
    @Autowired
    private NulsCrossChainConfig nulsCrossChainConfig;

    @Autowired
    private ChainManager chainManager;

    public static void main(String[] args){
        if (args == null || args.length == 0) {
            args = new String[]{"ws://" + HostInfo.getLocalIP() + ":7771"};
        }
        NulsRpcModuleBootstrap.run(CONTEXT_PATH, args);
    }
    /**
     * 初始化模块，比如初始化RockDB等，在此处初始化后，可在其他bean的afterPropertiesSet中使用
     * 在onStart前会调用此方法
     *
     */
    @Override
    public void init() {
        try {
            super.init();
            initSys();
            initDB();
            /**
             * 注册本链跨链交易类型
             * Registered Chain Cross-Chain Transaction Types
             * */
            registerCrossTxType(nulsCrossChainConfig.getCrossCtxType());
            /**
             * 添加RPC接口目录
             * Add RPC Interface Directory
             * */
            registerRpcPath(RPC_PATH);
            chainManager.initChain();
        }catch (Exception e){
            Log.error(e);
        }
    }

    @Override
    public Module[] declareDependent() {
        if (nulsCrossChainConfig.getMainChainId() == nulsCrossChainConfig.getChainId()) {
            return new Module[]{
                    new Module(ModuleE.NW.abbr, VERSION),
                    new Module(ModuleE.TX.abbr, VERSION),
                    new Module(ModuleE.CM.abbr, VERSION),
                    new Module(ModuleE.AC.abbr, VERSION),
                    new Module(ModuleE.CS.abbr, VERSION),
                    new Module(ModuleE.LG.abbr, VERSION)
            };
        }else{
            return new Module[]{
                    new Module(ModuleE.NW.abbr, VERSION),
                    new Module(ModuleE.TX.abbr, VERSION),
                    new Module(ModuleE.AC.abbr, VERSION),
                    new Module(ModuleE.CS.abbr, VERSION),
                    new Module(ModuleE.LG.abbr, VERSION)
            };
        }
    }

    @Override
    public boolean doStart() {
        try {
            while (!isDependencieReady(ModuleE.NW.abbr) || !isDependencieReady(ModuleE.TX.abbr)){
                Log.debug("wait depend modules ready");
                Thread.sleep(2000L);
            }
            chainManager.runChain();
            return true;
        }catch (Exception e){
            Log.error(e);
            return false;
        }
    }

    @Override
    public void onDependenciesReady(Module module){
        try {
            /*
             * 注册交易
             * Registered transactions
             */
            if(module.getName().equals(ModuleE.TX.abbr)){
                for (Integer chainId:chainManager.getChainMap().keySet()) {
                    registerTx(null, chainId);
                }
            }
            /*
             * 注册协议,如果为非主网则需激活跨链网络
             */
            if (ModuleE.NW.abbr.equals(module.getName())) {
                NetWorkCall.register();
                for (Chain chain:chainManager.getChainMap().values()) {
                    if(!chain.isMainChain()){
                        NetWorkCall.activeCrossNet(chain.getChainId(), chain.getConfig().getMaxNodeAmount(), chain.getConfig().getMaxInNode(), chain.getConfig().getCrossSeedIps());
                    }
                }
            }
        }catch (Exception e){
            Log.error(e);
        }
    }

    @Override
    public RpcModuleState onDependenciesReady() {
        Log.debug("cc onDependenciesReady");
        return RpcModuleState.Running;
    }

    @Override
    public RpcModuleState onDependenciesLoss(Module dependenciesModule) {
        return RpcModuleState.Ready;
    }

    /**
     * 初始化系统编码
     * Initialization System Coding
     */
    private void initSys() throws Exception {
        System.setProperty(SYS_FILE_ENCODING, UTF_8.name());
        Field charset = Charset.class.getDeclaredField("defaultCharset");
        charset.setAccessible(true);
        charset.set(null, UTF_8);
    }

    /**
     * 初始化数据库
     * Initialization database
     */
    private void initDB() throws Exception {
        RocksDBService.init(nulsCrossChainConfig.getDataFolder());
        RocksDBService.createTable(DB_NAME_CONSUME_LANGUAGE);
        RocksDBService.createTable(DB_NAME_CONSUME_CONGIF);
    }

}
