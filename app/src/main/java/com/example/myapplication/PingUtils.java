package com.example.myapplication;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class PingUtils {

    //ip地址正则表达式
    private static final String ipRegex =
            "((?:(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d))))";

    //默认超时时间
    private static final int TIME_OUT = 100;
    //默认ping的次数
    private static final int DEFAULT_COUNT = 1;

    //最小RTT
    private static final int TYPE_MIN = 1;
    //平均RTT
    private static final int TYPE_AVG = 2;
    //最大RTT
    private static final int TYPE_MAX = 3;
    //RTT偏差
    private static final int TYPE_MDEV = 4;

    //网络ping的结果
    private static final String RESULT_BAD = "bad";
    private static final String RESULT_NORMAL = "normal";
    private static final String RESULT_GOOD = "good";

    /**
     * 解析数据，延迟小于50为good，延迟在50~100之间或者丢包率为0为normal，
     * 延迟在100以上或者返回失败或丢包率大于0，则返回bad
     *
     * @param url ping的地址
     * @return
     */
    public static String parseRTT(String url) {
        int pingRtt = getRTT(url);
        float pingLostRate = getPacketLossFloat(url);
        if (pingRtt > 0 && pingRtt < 50) {
            return RESULT_GOOD;
        } else if ((pingRtt <= 100 && pingRtt > 50) || pingLostRate == 0) {
            return RESULT_NORMAL;
        } else if (pingRtt > 100 || pingRtt < 0 || pingLostRate > 0) {
            return RESULT_BAD;
        }
        return RESULT_BAD;
    }

    /**
     * 解析数据，延迟小于50为good，延迟在50~100之间或者丢包率为0为normal，
     * 延迟在100以上或者返回失败或丢包率大于0，则返回bad
     *
     * @param url     ping的地址
     * @param type    ping的类型
     * @param count   ping的次数
     * @param timeout ping的超时时间
     * @return
     */
    public static String parseRTT(String url, int type, int count, int timeout) {
        int pingRtt = getRTT(url, type, count, timeout);
        float pingLostRate = getPacketLossFloat(url, count, timeout);
        if (pingRtt > 0 && pingRtt < 50) {
            return RESULT_GOOD;
        } else if ((pingRtt <= 100 && pingRtt > 50) || pingLostRate == 0) {
            return RESULT_NORMAL;
        } else if (pingRtt > 100 || pingRtt < 0 || pingLostRate > 0) {
            return RESULT_BAD;
        }
        return RESULT_BAD;
    }

    /**
     * 获取pingPTT值，默认为平均RTT
     *
     * @param url 需要ping的url地址
     * @return RTT值，单位 ms 注意：-1是默认值，返回-1表示获取失败
     */
    public static int getRTT(String url) {
        return getRTT(url, TYPE_AVG);
    }

    /**
     * 获取ping的RTT值
     *
     * @param url  需要ping的url地址
     * @param type ping的类型
     * @return RTT值，单位 ms 注意：-1是默认值，返回-1表示获取失败
     */
    public static int getRTT(String url, int type) {
        return getRTT(url, type, DEFAULT_COUNT, TIME_OUT);
    }

    /**
     * 获取ping url的RTT
     *
     * @param url     需要ping的url地址
     * @param type    ping的类型
     * @param count   需要ping的次数
     * @param timeout 需要ping的超时时间，单位 ms
     * @return RTT值，单位 ms 注意：-1是默认值，返回-1表示获取失败
     */
    public static int getRTT(String url, int type, int count, int timeout) {
        String domain = getDomain(url);
        if(null == domain){
            return -1;
        }
        String pingString = ping(createSimplePingCommand(count, timeout, domain));
        Log.d("Ping result: ",pingString);
        if (null != pingString) {
            try {
                //获取以"min/avg/max/mdev"为头的文本，分别获取此次的ping参数
                String tempInfo = pingString.substring(pingString.indexOf("min/avg/max/mdev") + 19);
                String[] temps = tempInfo.split("/");
                switch (type) {
                    case TYPE_MIN: {
                        return Math.round(Float.parseFloat(temps[0]));
                    }
                    case TYPE_AVG: {
                        return Math.round(Float.parseFloat(temps[1]));
                    }
                    case TYPE_MAX: {
                        return Math.round(Float.parseFloat(temps[2]));
                    }
                    case TYPE_MDEV: {
                        return Math.round(Float.parseFloat(temps[3]));
                    }
                    default: {
                        return -1;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    /**
     * 获取ping url的丢包率，浮点型
     *
     * @param url 需要ping的url地址
     * @return 丢包率 如50%可得 50，注意：-1是默认值，返回-1表示获取失败
     */
    public static float getPacketLossFloat(String url) {
        String packetLossInfo = getPacketLoss(url);
        if (null != packetLossInfo) {
            try {
                return Float.parseFloat(packetLossInfo.replace("%", ""));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    /**
     * 获取ping url的丢包率，浮点型
     *
     * @param url     需要ping的url地址
     * @param count   需要ping的次数
     * @param timeout 需要ping的超时时间，单位 ms
     * @return 丢包率 如50%可得 50，注意：-1是默认值，返回-1表示获取失败
     */
    public static float getPacketLossFloat(String url, int count, int timeout) {
        String packetLossInfo = getPacketLoss(url, count, timeout);
        if (null != packetLossInfo) {
            try {
                return Float.parseFloat(packetLossInfo.replace("%", ""));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    /**
     * 获取ping url的丢包率
     *
     * @param url 需要ping的url地址
     * @return 丢包率 x%
     */
    public static String getPacketLoss(String url) {
        return getPacketLoss(url, DEFAULT_COUNT, TIME_OUT);
    }

    /**
     * 获取ping url的丢包率
     *
     * @param url     需要ping的url地址
     * @param count   需要ping的次数
     * @param timeout 需要ping的超时时间，单位ms
     * @return 丢包率 x%
     */
    public static String getPacketLoss(String url, int count, int timeout) {
        String domain = getDomain(url);
        if (null == domain) {
            return null;
        }
        String pingString = ping(createSimplePingCommand(count, timeout, domain));
        if (null != pingString) {
            try {
                String tempInfo = pingString.substring(pingString.indexOf("received,"));
                return tempInfo.substring(9, tempInfo.indexOf("packet"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    // ********************以下是一些辅助方法，设为private********************//

    /**
     * 域名获取
     *
     * @param url 网址
     * @return
     */
    private static String getDomain(String url) {
        String domain = null;
        try {
            domain = URI.create(url).getHost();
            if (null == domain && isMatch(ipRegex,url)) {
                domain = url;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return domain;
    }

    private static boolean isMatch(String regex, String string) {
        return Pattern.matches(regex, string);
    }

    /**
     * ping方法，调用ping指令
     *
     * @param command ping指令文本
     * @return
     */
    private static String ping(String command) {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(command);       //执行ping指令
            InputStream is = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while (null != (line = reader.readLine())) {
                sb.append(line);
                sb.append("\n");
            }
            reader.close();
            is.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != process) {
                process.destroy();
            }
        }
        return null;
    }

    /**
     * ping指令格式文本
     *
     * @param count   调用次数
     * @param timeout 超时时间
     * @param domain  地址
     * @return
     */
    private static String createSimplePingCommand(int count, int timeout, String domain) {
        return "/system/bin/ping -c " + count + " -w " + timeout + " " + domain;
    }
}

