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
package io.nuls.rpc.model.message;

import io.nuls.rpc.info.Constants;
import io.nuls.tools.data.DateUtils;
import io.nuls.tools.thread.TimeService;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tangyi
 * @date 2018/12/4
 * @description
 */
public class MessageUtil {

    /**
     * 默认Message对象
     * Default Message object
     */
    public static Message basicMessage(MessageType messageType) {
        Message message = new Message();
        message.setMessageId(Constants.nextSequence());
        message.setMessageType(messageType.name());
        message.setTimestamp(TimeService.currentTimeMillis() + "");
        message.setTimezone(DateUtils.getTimeZone() + "");
        return message;
    }

    /**
     * 默认握手对象
     * Default NegotiateConnection object
     */
    public static NegotiateConnection defaultNegotiateConnection() {
        NegotiateConnection negotiateConnection = new NegotiateConnection();
        negotiateConnection.setProtocolVersion("1.0");
        negotiateConnection.setCompressionAlgorithm("zlib");
        negotiateConnection.setCompressionRate("0");
        return negotiateConnection;
    }

    /**
     * 构造默认Request对象
     * Constructing a default Request object
     */
    public static Request defaultRequest() {
        Request request = new Request();
        request.setRequestAck("0");
        request.setSubscriptionEventCounter("0");
        request.setSubscriptionPeriod("0");
        request.setSubscriptionRange("0");
        request.setResponseMaxSize("0");
        request.setRequestMethods(new HashMap<>(1));
        return request;
    }

    /**
     * 根据参数构造Request对象，然后发送Request
     * Construct the Request object according to the parameters, and then send the Request
     */
    public static Request newRequest(String cmd, Map params, String ack, String subscriptionPeriod, String subscriptionEventCounter) {
        Request request = defaultRequest();
        request.setRequestAck(ack);
        request.setSubscriptionPeriod(subscriptionPeriod);
        request.setSubscriptionEventCounter(subscriptionEventCounter);
        request.getRequestMethods().put(cmd, params);
        return request;
    }

    /**
     * 构造一个Response对象
     * Constructing a new Response object
     */
    public static Response newResponse(String requestId, String status, String comment) {
        Response response = new Response();
        response.setRequestId(requestId);
        response.setResponseStatus(status);
        response.setResponseComment(comment);
        response.setResponseMaxSize("0");
        return response;
    }
}
