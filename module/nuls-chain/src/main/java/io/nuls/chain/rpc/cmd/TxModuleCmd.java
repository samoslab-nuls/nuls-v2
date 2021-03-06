/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.chain.rpc.cmd;

import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import io.nuls.chain.info.ChainTxConstants;
import io.nuls.chain.info.CmRuntimeInfo;
import io.nuls.chain.info.RpcConstants;
import io.nuls.chain.model.dto.ChainEventResult;
import io.nuls.chain.model.po.Asset;
import io.nuls.chain.model.po.BlockChain;
import io.nuls.chain.model.po.BlockHeight;
import io.nuls.chain.model.po.CacheDatas;
import io.nuls.chain.service.AssetService;
import io.nuls.chain.service.CacheDataService;
import io.nuls.chain.service.ChainService;
import io.nuls.chain.service.ValidateService;
import io.nuls.chain.util.LoggerUtil;
import io.nuls.chain.util.TxUtil;
import io.nuls.core.rpc.model.CmdAnnotation;
import io.nuls.core.rpc.model.Parameter;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.util.RPCUtil;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.model.ObjectUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lan
 * @date 2018/11/22
 */
@Component
public class TxModuleCmd extends BaseChainCmd {
    @Autowired
    private ChainService chainService;
    @Autowired
    private AssetService assetService;
    @Autowired
    private CacheDataService cacheDataService;
    @Autowired
    private ValidateService validateService;

    /**
     * chainModuleTxValidate
     * 批量校验
     */
    @CmdAnnotation(cmd = RpcConstants.TX_MODULE_VALIDATE_CMD_VALUE, version = 1.0,
            description = "chainModuleTxValidate")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]")
    @Parameter(parameterName = "txList", parameterType = "String")
    public Response chainModuleTxValidate(Map params) {
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"));
            ObjectUtils.canNotEmpty(params.get("txList"));
            Integer chainId = (Integer) params.get("chainId");
            List<String> txHexList = (List) params.get("txList");
            List<Transaction> txList = new ArrayList<>();
            List<String> errorList = new ArrayList<>();
            Response parseResponse = parseTxs(txHexList, txList);
            if (!parseResponse.isSuccess()) {
                return parseResponse;
            }
            //1获取交易类型
            //2进入不同验证器里处理
            //3封装失败交易返回
            Map<String, Integer> chainMap = new HashMap<>();
            Map<String, Integer> assetMap = new HashMap<>();
            BlockChain blockChain = null;
            Asset asset = null;
            ChainEventResult chainEventResult = ChainEventResult.getResultSuccess();
            for (Transaction tx : txList) {
                switch (tx.getType()) {
                    case ChainTxConstants.TX_TYPE_REGISTER_CHAIN_AND_ASSET:
                        blockChain = TxUtil.buildChainWithTxData(tx, false);
                        asset = TxUtil.buildAssetWithTxChain(tx);
                        String assetKey = CmRuntimeInfo.getAssetKey(asset.getChainId(), asset.getAssetId());
                        chainEventResult = validateService.batchChainRegValidator(blockChain, asset, chainMap, assetMap);
                        if (chainEventResult.isSuccess()) {
                            chainMap.put(String.valueOf(blockChain.getChainId()), 1);
                            assetMap.put(assetKey, 1);
                            LoggerUtil.logger().debug("txHash = {},chainId={} reg batchValidate success!", tx.getHash().toString(), blockChain.getChainId());
                        } else {
                            LoggerUtil.logger().error("txHash = {},chainId={} reg batchValidate fail!", tx.getHash().toString(), blockChain.getChainId());
                            errorList.add(tx.getHash().toString());
//                            return failed(chainEventResult.getErrorCode());
                        }
                        break;
                    case ChainTxConstants.TX_TYPE_DESTROY_ASSET_AND_CHAIN:
                        blockChain = TxUtil.buildChainWithTxData(tx, true);
                        chainEventResult = validateService.chainDisableValidator(blockChain);
                        if (chainEventResult.isSuccess()) {
                            LoggerUtil.logger().debug("txHash = {},chainId={} destroy batchValidate success!", tx.getHash().toString(), blockChain.getChainId());
                        } else {
                            LoggerUtil.logger().error("txHash = {},chainId={} destroy batchValidate fail!", tx.getHash().toString(), blockChain.getChainId());
                            errorList.add(tx.getHash().toString());
//                            return failed(chainEventResult.getErrorCode());
                        }
                        break;

                    case ChainTxConstants.TX_TYPE_ADD_ASSET_TO_CHAIN:
                        asset = TxUtil.buildAssetWithTxChain(tx);
                        String assetKey2 = CmRuntimeInfo.getAssetKey(asset.getChainId(), asset.getAssetId());
                        chainEventResult = validateService.batchAssetRegValidator(asset, assetMap);
                        if (chainEventResult.isSuccess()) {
                            assetMap.put(assetKey2, 1);
                            LoggerUtil.logger().debug("txHash = {},assetKey={} reg batchValidate success!", tx.getHash().toString(), assetKey2);
                        } else {
                            LoggerUtil.logger().error("txHash = {},assetKey={} reg batchValidate fail!", tx.getHash().toString(), assetKey2);
                            errorList.add(tx.getHash().toString());
//                            return failed(chainEventResult.getErrorCode());
                        }
                        break;
                    case ChainTxConstants.TX_TYPE_REMOVE_ASSET_FROM_CHAIN:
                        asset = TxUtil.buildAssetWithTxChain(tx);
                        chainEventResult = validateService.assetDisableValidator(asset);
                        if (chainEventResult.isSuccess()) {
                            LoggerUtil.logger().debug("txHash = {},assetKey={} disable batchValidate success!", tx.getHash().toString(), CmRuntimeInfo.getAssetKey(asset.getChainId(), asset.getAssetId()));
                        } else {
                            LoggerUtil.logger().error("txHash = {},assetKey={} disable batchValidate fail!", tx.getHash().toString(), CmRuntimeInfo.getAssetKey(asset.getChainId(), asset.getAssetId()));
                            errorList.add(tx.getHash().toString());
                            //                            return failed(chainEventResult.getErrorCode());
                        }
                        break;
                    default:
                        break;
                }
            }
            Map<String,Object> rtMap = new HashMap<>(1);
            rtMap.put("list",errorList);
            return success(rtMap);
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
            return failed(e.getMessage());
        }
    }

    /**
     * moduleTxsRollBack
     * 回滚
     */
    @CmdAnnotation(cmd = RpcConstants.TX_ROLLBACK_CMD_VALUE, version = 1.0,
            description = "moduleTxsRollBack")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]")
    @Parameter(parameterName = "txList", parameterType = "array")
    @Parameter(parameterName = "blockHeader", parameterType = "array")
    public Response moduleTxsRollBack(Map params) {
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"));
            ObjectUtils.canNotEmpty(params.get("blockHeader"));
            Integer chainId = (Integer) params.get("chainId");
            List<String> txHexList = (List) params.get("txList");
            String blockHeaderStr = (String) params.get("blockHeader");
            BlockHeader blockHeader = new BlockHeader();
            blockHeader.parse(RPCUtil.decode(blockHeaderStr),0);
            long commitHeight = blockHeader.getHeight();
            List<Transaction> txList = new ArrayList<>();
            Response parseResponse = parseTxs(txHexList, txList);
            if (!parseResponse.isSuccess()) {
                return parseResponse;
            }
            //高度先回滚
            CacheDatas moduleTxDatas = cacheDataService.getCacheDatas(commitHeight);
            if (null == moduleTxDatas) {
                //这里存在该高度 可能在TxCirculateCmd中已经回滚过了
                BlockHeight blockHeight = cacheDataService.getBlockHeight(chainId);
                if (blockHeight.getLatestRollHeight() == commitHeight) {
                    LoggerUtil.logger().debug("chain module height ={} bak datas is null,maybe had rolled", commitHeight);
                    return success();
                } else {
                    LoggerUtil.logger().error("chain module height ={} bak datas is null", commitHeight);
                    return failed("chain module height = " + commitHeight + " bak datas is null");
                }
            }

            //通知远程调用回滚
            chainService.rpcBlockChainRollback(txList);
            //进行数据回滚
            cacheDataService.rollBlockTxs(chainId, commitHeight);
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
            return failed(e.getMessage());
        }
        Map<String, Boolean> resultMap = new HashMap<>();
        resultMap.put("value", true);
        return success(resultMap);
    }

    /**
     * moduleTxsCommit
     * 批量提交
     */
    @CmdAnnotation(cmd = RpcConstants.TX_COMMIT_CMD_VALUE, version = 1.0,
            description = "moduleTxsCommit")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]")
    @Parameter(parameterName = "txList", parameterType = "array")
    @Parameter(parameterName = "blockHeader", parameterType = "array")
    public Response moduleTxsCommit(Map params) {
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"));
            ObjectUtils.canNotEmpty(params.get("txList"));
            ObjectUtils.canNotEmpty(params.get("blockHeader"));
            Integer chainId = (Integer) params.get("chainId");
            List<String> txHexList = (List) params.get("txList");
            String blockHeaderStr = (String) params.get("blockHeader");
            BlockHeader blockHeader = new BlockHeader();
            blockHeader.parse(RPCUtil.decode(blockHeaderStr),0);
            long commitHeight = blockHeader.getHeight();
            List<Transaction> txList = new ArrayList<>();
            Response parseResponse = parseTxs(txHexList, txList);
            if (!parseResponse.isSuccess()) {
                return parseResponse;
            }
            /*begin bak datas*/
            BlockHeight dbHeight = cacheDataService.getBlockHeight(chainId);
            cacheDataService.bakBlockTxs(chainId, dbHeight.getBlockHeight(), commitHeight, txList, false);
            /*end bak datas*/
            /*begin bak height*/
            cacheDataService.beginBakBlockHeight(chainId, commitHeight);
            /*end bak height*/
            BlockChain blockChain = null;
            Asset asset = null;
            try {
                for (Transaction tx : txList) {
                    switch (tx.getType()) {
                        case ChainTxConstants.TX_TYPE_REGISTER_CHAIN_AND_ASSET:
                            blockChain = TxUtil.buildChainWithTxData(tx, false);
                            asset = TxUtil.buildAssetWithTxChain(tx);
                            chainService.registerBlockChain(blockChain, asset);
                            //通知网络模块创建链
                            break;
                        case ChainTxConstants.TX_TYPE_DESTROY_ASSET_AND_CHAIN:
                            blockChain = TxUtil.buildChainWithTxData(tx, true);
                            chainService.destroyBlockChain(blockChain);
                            break;
                        case ChainTxConstants.TX_TYPE_ADD_ASSET_TO_CHAIN:
                            asset = TxUtil.buildAssetWithTxChain(tx);
                            assetService.registerAsset(asset);
                            break;
                        case ChainTxConstants.TX_TYPE_REMOVE_ASSET_FROM_CHAIN:
                            asset = TxUtil.buildAssetWithTxChain(tx);
                            assetService.deleteAsset(asset);
                            break;
                        default:
                            break;
                    }
                }
                LoggerUtil.logger().debug("moduleTxsCommit end");
                /*begin bak height*/
                cacheDataService.endBakBlockHeight(chainId, commitHeight);
                /*end bak height*/
            } catch (Exception e) {
                LoggerUtil.logger().error(e);
                //通知远程调用回滚
                chainService.rpcBlockChainRollback(txList);
                //进行回滚
                cacheDataService.rollBlockTxs(chainId, commitHeight);
                return failed(e.getMessage());
            }

        } catch (Exception e) {
            LoggerUtil.logger().error(e);
            return failed(e.getMessage());
        }
        Map<String, Boolean> resultMap = new HashMap<>();
        resultMap.put("value", true);
        return success(resultMap);
    }
}
