package controllers;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by hu on 2016/12/21.
 */
public class Tools {

    public static Map<String, String> xmlToMap(org.w3c.dom.Document document) {
        Map<String, String> map = new HashMap<>();
        NodeList nodeList = document.getDocumentElement().getChildNodes();

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                NodeList nodeList1 = node.getChildNodes();

                for (int j = 0; j < nodeList1.getLength(); j++) {
                    Node item = nodeList1.item(j);

                    map.put(node.getNodeName(), item.getNodeValue());
                    play.Logger.info(node.getNodeName() + " -> " + item.getNodeValue());
                }
            }
        }

        return map;
    }

    public static String getSignXmlString(Map<String, String> map, String key) {
        Map<String, String> treeMap = new TreeMap<String, String>(new Comparator<String>() {
            @Override
            public int compare(String o, String t1) {
                return o.compareTo(t1);
            }
        });

        treeMap.putAll(map);

        // get string url
        String url = "";
        for (Map.Entry<String, String> entry : treeMap.entrySet()) {
            url += entry.getKey() + "=" + entry.getValue() + "&";
        }

        url += "key=" + key;

        play.Logger.info(url);

        final String md5 = getMD5(url);
        treeMap.put("sign", md5);

        return getXmlByMap(treeMap);
    }

    public static String getSignStr(Map<String, String> map, String key) {
        Map<String, String> treeMap = new TreeMap<String, String>(new Comparator<String>() {
            @Override
            public int compare(String o, String t1) {
                return o.compareTo(t1);
            }
        });

        treeMap.putAll(map);

        // get string url
        String url = "";
        for (Map.Entry<String, String> entry : treeMap.entrySet()) {
            url += entry.getKey() + "=" + entry.getValue() + "&";
        }

        url += "key=" + key;

        return getMD5(url);
    }

    private static String getMD5(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());

            return new BigInteger(1, md.digest()).toString(16).toUpperCase();
        } catch (Exception e) {

        }

        return "";
    }

    private static String getXmlByMap(Map<String, String> map) {
        String xml = "<xml>";

        for (Map.Entry<String, String> entry : map.entrySet()) {
            final String key = entry.getKey();
            xml = xml + "<" + key + ">" + entry.getValue() + "</" + key + ">";
        }
        return xml + "</xml>";
    }

    public static boolean checkSign(Map<String, String> map, String key) {
        String sign = map.get("sign").toString();

        if (sign == null || sign.isEmpty()) {
            return false;
        }

        play.Logger.info(sign);

        map.remove("sign");

        return sign.equals(getSignStr(map, key));
    }
}
