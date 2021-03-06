package com.tony.util;


import com.tony.entity.Session;
import com.tony.entity.SessionType;
import com.tony.entity.Talk;
import com.tony.entity.Track;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 工具类(拆分talkStr以及装入talklist的算法)
 * Created by ton on 16-4-23.
 */
public class Tool {
    private static final String regxLetter = "[a-zA-Z]+";
    private static final String regxLightning = "lightning";
    private static final String regxNum = "[0-9]";

    /**
     * 根据传入的字符串来拆分并且创建talk
     * @param str 根据配置文件读入的字符串
     * @return 组装的talk
     */
    public static Talk createTalk(String str) throws Exception{
        String titleName = str.substring(0, str.lastIndexOf(" "));  //拆分titlename
        String last = str.substring(str.lastIndexOf(" ") + 1);   //将时间那个字段拆出来
        int last_int = 0;

        Pattern p = Pattern.compile(regxNum);
        Matcher m = p.matcher(last);

        if (m.find()) {//先判断是否含有数字，否则就是“lightning”
            p = Pattern.compile(regxLetter);
            m = p.matcher(last);
            last_int = Integer.parseInt(m.replaceAll("").trim());   //将min过滤掉，然后提取出其中的数字
        } else {
            p = Pattern.compile(regxLightning);
            m = p.matcher(last);
            if (m.find()) {
                last_int = 5; //将last置为lightning
            }else{
                throw new Exception("Invalid Duration");
            }
        }

        Talk talk = new Talk(titleName,last_int,str);
        return talk;
    }

    /**
     * 根据传入的talkList和定义的track，来实现schedule（核心方法）
     * @param talkList
     * @param track
     * @return  返回一个schedule后的Track
     */
    public static Track getTrackAfterTalkScheduled(List<Talk> talkList, Track track) throws Exception{
        List<Session> sessions = track.getSessionList();
        List<Talk> talksOfPerSession = null;

        //session中的剩余的时间
        int remainingMinutes = 0;

        for(Session _session: sessions){
            if(!_session.getSessionType().isScheduleable()){    //如果是lunch或者networking，就跳过
                continue;
            }
            talksOfPerSession = new ArrayList<Talk>();//初始化session中的talk集合
            for(Talk _talk : talkList){
                if(_talk.isScheduled()){//是否已经被计划过了
                    continue;
                }
                remainingMinutes = _session.getRemainingTimeOfMinutes();//检查session还剩多少时间

                //如果时间没了，那么跳出，循环下一个session
                if(remainingMinutes == 0){
                    break;
                }

                //如果这个talk的持续时间在session的剩余时间范围之内,那么安排在此session中
                if(_talk.getLast() <= remainingMinutes ){
                    _talk.setIsScheduled(true);//设置已被schedule过的标志
                    _talk.setStart(_session.getRightNow());//根据session现在时间设置talk开始的时间
                    _session.rightNowAfterScheduled(_talk.getLast());//session时间相应的减去

                    talksOfPerSession.add(_talk);        //talk加进集合中去
                }
            }

            _session.setTalkList(talksOfPerSession);
        }

        track.setSessionList(sessions);
        return track;
    }

    /**
     * 装载每个Track所需要的Session
     * @return   list of session
     */
    public static List<Session> getSessionPerTrack(){
        List<Session> sessionList = new ArrayList<Session>();

        //morningSession
        Calendar start= Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        start.clear();
        end.clear();
        start.set(Calendar.HOUR_OF_DAY, 9);
        end.set(Calendar.HOUR_OF_DAY,12);

        SessionType sessionType = new SessionType("morningSession",start.getTime(),end.getTime(),true);
        Session morningSession = new Session(sessionType,null,start.getTime());

        //afternoonSession
        start =Calendar.getInstance();
        end = Calendar.getInstance();
        start.clear();
        end.clear();
        start.set(Calendar.HOUR_OF_DAY,13);
        end.set(Calendar.HOUR_OF_DAY,17);

        sessionType = new SessionType("afternoonSession",start.getTime(),end.getTime(),true);
        Session afternoonSession = new Session(sessionType,null,start.getTime());

        //lunch session
        start  = Calendar.getInstance();
        end = Calendar.getInstance();
        start.clear();
        end.clear();
        start.set(Calendar.HOUR_OF_DAY,12);

        sessionType = new SessionType("lunch",start.getTime(),null,false);
        Session lunch = new Session(sessionType,null,start.getTime());

        //network event
        start = Calendar.getInstance();
        end = Calendar.getInstance();
        start.clear();
        end.clear();
        start.set(Calendar.HOUR_OF_DAY,17);

        sessionType = new SessionType("Networking Event",start.getTime(),null,false);
        Session networking = new Session(sessionType,null,start.getTime());

        sessionList.add(morningSession);
        sessionList.add(lunch);
        sessionList.add(afternoonSession);
        sessionList.add(networking);

        return sessionList;
    }
}
