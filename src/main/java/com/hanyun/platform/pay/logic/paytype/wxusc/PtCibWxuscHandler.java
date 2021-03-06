/**
 * 
 */
package com.hanyun.platform.pay.logic.paytype.wxusc;

import java.util.Date;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.hanyun.ground.util.date.DateCalcUtil;
import com.hanyun.ground.util.date.DateFormatUtil;
import com.hanyun.platform.pay.adapt.cib.weixin.CibWeiXinUnifiedorderAdapter;
import com.hanyun.platform.pay.adapt.cib.weixin.consts.CibWeiXinConsts;
import com.hanyun.platform.pay.adapt.cib.weixin.protocol.UnifiedorderReq;
import com.hanyun.platform.pay.adapt.cib.weixin.protocol.UnifiedorderRes;
import com.hanyun.platform.pay.consts.PaymentConsts;
import com.hanyun.platform.pay.context.PayProcessContext;
import com.hanyun.platform.pay.dao.CibWeixinOrderDao;
import com.hanyun.platform.pay.domain.CibWeixinOrder;
import com.hanyun.platform.pay.domain.PayTransaction;
import com.hanyun.platform.pay.domain.Payment;
import com.hanyun.platform.pay.logic.paytype.base.PtCibWxBaseHandler;
import com.hanyun.platform.pay.vo.trade.TradePreCreateReq;
import com.hanyun.platform.pay.vo.trade.TradePreCreateWeixinExtData;

/**
 * 兴业银行微信用户扫码处理
 * 
 * @author liyinglong@hanyun.com
 * @date 2016年8月31日 上午11:53:25
 */
@Component
public class PtCibWxuscHandler extends PtCibWxBaseHandler {

    @Resource
    private CibWeixinOrderDao cibWeixinOrderDao;
    @Resource
    private CibWeiXinUnifiedorderAdapter cibWeiXinUnifiedorderAdapter;

    @Override
    public String getPayType() {
        return PaymentConsts.PAYTYPE_WXPAY_USC;
    }

    @Override
    public Object doPreCreate(TradePreCreateReq req) {
        Payment payment = PayProcessContext.getPayment();
        PayTransaction trans = PayProcessContext.getTransation();
        TradePreCreateWeixinExtData extData = new TradePreCreateWeixinExtData();
        // 检测已存在的
        CibWeixinOrder wxorder = cibWeixinOrderDao.selectByOutTradeNo(trans.getTransId());
        if (wxorder != null && StringUtils.isNotBlank(wxorder.getCodeUrl())
                && StringUtils.isNotBlank(wxorder.getTimeExpire())) {
            Date expire = DateFormatUtil.parseDateTimeNoSep(wxorder.getTimeExpire());
            // 过期前2分钟之前，不需要请求接口
            Date preExpire = DateCalcUtil.addMinutes(expire, -2);
            if (preExpire.after(new Date())) {
                extData.setCodeUrl(wxorder.getCodeUrl());
                return extData;
            }
        }
        // 组装业务参数
        UnifiedorderReq mthreq = new UnifiedorderReq();
        mthreq.setTrade_type(CibWeiXinConsts.TRADE_TYPE_NATIVE);
        mthreq.setOut_trade_no(trans.getTransId());
        mthreq.setProduct_id(payment.getPayId());
        mthreq.setBody(req.getOrderDesc());
        mthreq.setTotal_fee(req.getCurPayAmount());
        mthreq.setDevice_info(req.getTerminalDeviceNo());
        mthreq.setSpbill_create_ip(req.getTerminalIp());
        mthreq.setWx_appid(req.getWxAppid());
        mthreq.setOpenid(req.getWxOpenid());

        UnifiedorderRes mthres = cibWeiXinUnifiedorderAdapter.request(mthreq);
        extData.setCodeUrl(mthres.getCode_url());

        return extData;
    }

}
