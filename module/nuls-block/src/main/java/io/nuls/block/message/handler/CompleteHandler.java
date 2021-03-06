/*
 * MIT License
 * Copyright (c) 2017-2019 nuls.io
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.block.message.handler;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.block.cache.BlockCacher;
import io.nuls.block.constant.BlockErrorCode;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.message.CompleteMessage;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.CmdAnnotation;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.protocol.MessageHandler;
import io.nuls.core.rpc.util.RPCUtil;
import io.nuls.core.core.annotation.Service;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.logback.NulsLogger;

import java.util.Map;

import static io.nuls.block.constant.CommandConstant.COMPLETE_MESSAGE;


/**
 * 处理收到的{@link CompleteMessage},用于区块的同步
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 下午4:23
 */
@Service
public class CompleteHandler extends BaseCmd {

    @CmdAnnotation(cmd = COMPLETE_MESSAGE, version = 1.0, scope = Constants.PUBLIC, description = "")
    @MessageHandler(message = CompleteMessage.class)
    public Response process(Map map) {
        int chainId = Integer.parseInt(map.get("chainId").toString());
        String nodeId = map.get("nodeId").toString();
        CompleteMessage message = new CompleteMessage();
        NulsLogger messageLog = ContextManager.getContext(chainId).getMessageLog();
        try {
            byte[] decode = RPCUtil.decode(map.get("messageBody").toString());
            message.parse(new NulsByteBuffer(decode));
        } catch (NulsException e) {
            e.printStackTrace();
            messageLog.error(e);
            return failed(BlockErrorCode.PARAMETER_ERROR);
        }
        messageLog.debug("recieve CompleteMessage from node-" + nodeId + ", chainId:" + chainId);
        BlockCacher.batchComplete(chainId, message);
        return success();
    }

}
