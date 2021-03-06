/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.transaction.utils;

import io.nuls.core.log.logback.LoggerBuilder;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.model.bo.Chain;

/**
 * @author: Charlie
 * @date: 2019/2/28
 */
public class LoggerUtil {

    public static final NulsLogger LOG = LoggerBuilder.getLogger( "tx");

    private static final String FOLDER_PREFIX = "chain-";

    public static void init(Chain chain){
        NulsLogger txLogger = LoggerBuilder.getLogger(FOLDER_PREFIX + String.valueOf(chain.getConfig().getChainId()), TxConstant.LOG_TX);
        chain.getLoggerMap().put(TxConstant.LOG_TX, txLogger);
        NulsLogger txProcessLogger = LoggerBuilder.getLogger(FOLDER_PREFIX + String.valueOf(chain.getConfig().getChainId()), TxConstant.LOG_NEW_TX_PROCESS);
        chain.getLoggerMap().put(TxConstant.LOG_NEW_TX_PROCESS, txProcessLogger);
        NulsLogger txMessageLogger = LoggerBuilder.getLogger(FOLDER_PREFIX + String.valueOf(chain.getConfig().getChainId()), TxConstant.LOG_TX_MESSAGE);
        chain.getLoggerMap().put(TxConstant.LOG_TX_MESSAGE, txMessageLogger);
    }
}
