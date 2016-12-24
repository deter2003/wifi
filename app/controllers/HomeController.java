package controllers;

import play.Configuration;
import play.Play;
import play.libs.F;
import play.libs.ws.*;
import play.mvc.*;

import views.html.*;

import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    @Inject WSClient ws;

    @Inject WebJarAssets webJarAssets;

    private static Configuration configuration;

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */

    final String m_appid;
    final String m_body;
    final String m_mch_id;
    final String m_notify_url;
    final String m_trade_type;
    final String m_key;
    final String m_redirect;
    final String m_secret;

    @Inject
    public HomeController(Configuration configuration) {
        this.configuration = configuration;

        m_appid = configuration.getString("weixin.appid");
        m_body = configuration.getString("weixin.body");
        m_mch_id = configuration.getString("weixin.mch_id");
        m_notify_url = configuration.getString("weixin.notify_url");
        m_trade_type = configuration.getString("weixin.trade_type");
        m_key = configuration.getString("weixin.key");
        m_redirect = configuration.getString("weixin.redirect_uri");
        m_secret = configuration.getString("weixin.secret");
    }

    public Result index() {
        return ok(order.render());
    }

    private String getOrder(String openid, Integer fee) {
        Map<String, String> map = new HashMap<>();

        final String nonce_str = getRandom();
        final String out_trade_no = getRandom();

        map.put("appid", m_appid);
        map.put("body", m_body);
        map.put("mch_id", m_mch_id);
        map.put("nonce_str", getRandom());
        map.put("notify_url", m_notify_url);
        map.put("openid", openid);
        map.put("out_trade_no", getRandom());
        map.put("total_fee", fee.toString());
        map.put("trade_type", m_trade_type);

        return Tools.getSignXmlString(map, m_key);
    }

    public String getBody(WSResponse body) {

        try {
            String string = new String(body.asByteArray(), "utf-8");
            play.Logger.info(string);
            return string;
        } catch (IOException e) {

        }

        return "deter";
    }

    private static String getXmlByMap(Map<String, String> map) {
        String xml = "<xml>";

        for (Map.Entry<String, String> entry : map.entrySet()) {
            final String key = entry.getKey();
            xml = xml + "<" + key + ">" + entry.getValue() + "</" + key + ">";
        }
        return xml + "</xml>";
    }

    private String getSignString() {
        return "";
    }

    public F.Promise<Result> h5(String id, Integer fee, String user) {
        play.Logger.info("h5-------");
        WSRequest request = ws.url("https://api.mch.weixin.qq.com/pay/unifiedorder");

        request.setRequestTimeout(1000);

        return request.post(getOrder(id, fee)).map(response ->
                getPayPage(response, user, fee)
        );
    }

    private Result getPayPage(WSResponse response, String user, Integer fee) {
        play.Logger.info("getPayPage-------");
        Map<String, String> map = Tools.xmlToMap(response.asXml());

        Long rd = System.currentTimeMillis() / 1000;

        final String timeStamp = rd.toString();
        final String nonceStr = getRandom();

        String preay = map.get("prepay_id").toString();

        Map<String, String> pay = new HashMap<>();
        pay.put("appId", m_appid);
        pay.put("nonceStr", getRandom());
        pay.put("package", "prepay_id=" + preay);
        pay.put("signType", "MD5");
        pay.put("timeStamp", timeStamp);

        final String sign = Tools.getSignStr(pay, m_key);

        return ok(h5.render(timeStamp, nonceStr, preay, sign, user, fee, fee / 100));
    }

    public F.Promise<Result> test() {
        WSRequest request = ws.url("https://api.mch.weixin.qq.com/pay/unifiedorder");

        request.setRequestTimeout(1000);

        return request.post(getOrder("oxSMzwRg09eU3usEwmHHpisVwt8c",1)).map(response ->
            {
                String out = response.getBody();

                out = Tools.xmlToMap(response.asXml()).get("return_msg").toString();

                return ok(out);
            }
        );
    }

    private static String getRandom() {
        Long rd = System.currentTimeMillis();

        return rd.toString();
    }

    private String getSign(String xml) {
        final String key = Play.application().configuration().getString("");
        return "";
    }

    public Result pay(String id) {
        return ok(pay.render(id));
    }

    public F.Promise<Result> queryUser(String user) {
        play.Logger.info("queryUser-------");
        WSRequest request = ws.url("http://jnhy602.imwork.net:7788/interface.php?username=weixin&password=bbbccc&action=queryuserinfo&account=" + user);

        request.setRequestTimeout(1000);

        return request.get().map(response ->
        {
            String body = response.getBody();

            if (body == null || body.isEmpty())
            {
                return ok("用户不存在");
            }
            String code = body.substring(0, body.indexOf("&"));

            if (code.equals("errcode=0")) {
                return ok("ok");
            }

            return ok("用户不存在");
        }
        );
    }

    public Result notify_url() {
        play.Logger.info("notify_url-------");
        Http.RequestBody body = request().body();
        String str = body.asText();

        if (body.asXml() == null)
        {
            return ok("FAIL");
        }
        play.Logger.info(str);

        // check
        Map<String, String> map =  Tools.xmlToMap(body.asXml());
        boolean check = Tools.checkSign(map, m_key);

        play.Logger.info(check ? "SUCCESS" : "FAIL");

        return ok(str);
    }

    public F.Promise<Result> redirect() {
        play.Logger.info("redirect-------");

        final String code = request().getQueryString("code");

        if (code == null || code.isEmpty()) {
            return F.Promise.pure(ok(error.render()));
        }

        StringBuilder url = new StringBuilder();

        url.append("https://api.weixin.qq.com/sns/oauth2/access_token?")
           .append("appid=").append(m_appid)
           .append("&secret=").append(m_secret)
           .append("&code=").append(code)
           .append("&grant_type=authorization_code");
        play.Logger.info(url.toString());

        WSRequest wsRequest = ws.url("https://api.weixin.qq.com/sns/oauth2/access_token");
        wsRequest.setQueryParameter("appid", m_appid)
                .setQueryParameter("secret", m_secret)
                .setQueryParameter("code", code)
                .setQueryParameter("grant_type", "authorization_code");

        return wsRequest.get().map(response -> {
            final String openid = response.asJson().findPath("openid").asText();

            if (openid == null || openid.isEmpty()) {
                return ok(error.render());
            }

            return ok(pay.render(openid));
        });
    }
}
