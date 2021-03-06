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

package io.nuls.transaction.threadpool;

import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.model.bo.Chain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author: Charlie
 * @date: 2019/5/5
 */
public class NetTxThreadPoolExecutor implements NetTxThreadPool {

    private final LinkedList<NetTxProcessJob> jobs = new LinkedList<>();
    private final List<Worker> workers = Collections.synchronizedList(new ArrayList<>());
    private AtomicLong threadNum = new AtomicLong();
    private Chain chain;

    public NetTxThreadPoolExecutor(Chain chain) {
        this.chain = chain;
        initializeWorker();
    }

    private void initializeWorker() {
        Worker worker = new Worker(chain);
        workers.add(worker);
        Thread thread = new Thread(worker, "chainId-" + chain.getChainId() + "-worker-" + threadNum.incrementAndGet());
        thread.start();
    }

    @Override
    public void execute(NetTxProcessJob job) {
        if(job != null && jobs.size() < TxConstant.NET_NEW_TX_LIST_MAX_LENGTH) {
            synchronized (jobs) {
                jobs.addLast(job);
                jobs.notify();
            }
        }
    }

    class Worker implements Runnable{

        private volatile boolean running = true;

        private NetTxProcess netTxProcess = SpringLiteContext.getBean(NetTxProcess.class);

        private Chain chain;

        public Worker(Chain chain) {
            this.chain = chain;
        }

        @Override
        public void run() {
//            String name;
            while (running) {
//                name = Thread.currentThread().getName();
//                chain.getLoggerMap().get(TxConstant.LOG_NEW_TX_PROCESS).debug( name + "-待处理新交易线程准备获取任务.");
                NetTxProcessJob job = null;
                synchronized (jobs) {
                    while (jobs.isEmpty()) {
                        try {
//                            chain.getLoggerMap().get(TxConstant.LOG_NEW_TX_PROCESS).debug( name + "-待处理新交易线程等待任务.");
                            netTxProcess.process(chain);
                            jobs.wait();
                        } catch (Exception e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                    job = jobs.removeFirst();
                }
                if(job != null) {
                    try {
//                        chain.getLoggerMap().get(TxConstant.LOG_NEW_TX_PROCESS).debug( name + "-待处理新交易线程执行任务.");
                        job.run();
                    } catch (Exception e) {}
                }
            }
        }

        public void shutdown() {
            running = false;
        }
    }
}
