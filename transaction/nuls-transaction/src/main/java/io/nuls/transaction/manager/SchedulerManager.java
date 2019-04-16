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

package io.nuls.transaction.manager;

import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.thread.ThreadUtils;
import io.nuls.tools.thread.commom.NulsThreadFactory;
import io.nuls.transaction.constant.TxConfig;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.task.UnconfirmedTxProcessTask;
import io.nuls.transaction.task.VerifyTxProcessTask;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author: Charlie
 * @date: 2018-12-19
 */
@Component
public class SchedulerManager {

    @Autowired
    private TxConfig txConfig;

    public boolean createTransactionScheduler(Chain chain) {
        ScheduledThreadPoolExecutor localTxExecutor = ThreadUtils.createScheduledThreadPool(1,
                new NulsThreadFactory(txConfig.getModuleCode()));
        localTxExecutor.scheduleAtFixedRate(new VerifyTxProcessTask(chain),
                TxConstant.TX_TASK_INITIALDELAY, TxConstant.TX_TASK_PERIOD, TimeUnit.SECONDS);
        chain.setScheduledThreadPoolExecutor(localTxExecutor);

        ScheduledThreadPoolExecutor crossTxExecutor = ThreadUtils.createScheduledThreadPool(1,
                new NulsThreadFactory(txConfig.getModuleCode()));

        ScheduledThreadPoolExecutor unconfirmedTxExecutor = ThreadUtils.createScheduledThreadPool(1,
                new NulsThreadFactory(txConfig.getModuleCode()));
        //固定延迟时间
        crossTxExecutor.scheduleWithFixedDelay(new UnconfirmedTxProcessTask(chain),
                TxConstant.CLEAN_TASK_INITIALDELAY, TxConstant.CLEAN_TASK_PERIOD, TimeUnit.MINUTES);
        chain.setScheduledThreadPoolExecutor(unconfirmedTxExecutor);

        return true;
    }
}
