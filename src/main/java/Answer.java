

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Answer {


    public static void main(String[] args) throws IOException, ParseException {
        BufferedReader fr=new BufferedReader(new FileReader(new File("C:\\Users\\Administrator\\Downloads\\wds-contact-master\\answer\\src\\main\\resources\\2013-05-30.txt")));
        String content=null;
        MultiValueMap<String, String> map = new LinkedMultiValueMap() ;
        while((content=fr.readLine())!=null){
            String[] result = content.split(" - - \\[");
            List<String> values = map.get(result[0]);
            if(values==null){
                List<String > list=new ArrayList<String>();
                list.add(result[1]);
                map.put(result[0],list);
            }else {
                values.add(result[1]);
            }
        }
        fr.close();
        // 首先执行排序操作,按照时间进行排序操作处理
        sort(map);
        // 生成最终的计算结果
        JSONArray jsonArray=getFinalResults(map);
        jsonArray.stream().forEachOrdered(x->{
            JSONObject cont=(JSONObject)x;
            String start = (String) cont.get("start");
            String stop = (String) cont.get("stop");
            Long duration = (Long) cont.get("duration");
            System.out.println(start+"             " +stop+"             "+duration);
        });
    }

    /**
     *  对应的一个ip地址对应的存在多个访问记录操作的，需要进行遍历操作管理
     * */
    private static JSONArray getFinalResults(MultiValueMap<String, String> map) throws ParseException {
        Set<String> keys = map.keySet();
        Iterator<String> iterator = keys.iterator();
        JSONArray result=new JSONArray();
        while (iterator.hasNext()){
            String key = iterator.next();
            List<String> values = map.get(key);
            // 对应的需要针对于单个ip形成多个数据记录操作
            JSONArray res=createJsonObject(values);
            if (res.size()>0){
                result.addAll(res);
            }
        }
        return result;
    }

    /**
     *  判断和对应的生成jsonObject对象
     *
     * @param values*/
    private static JSONArray createJsonObject(List<String> values) throws ParseException {
        // 作为记录的起点数据
        JSONArray jsonArray=new JSONArray();
        String start=values.get(0);
        if (values.size()>=2){
            for(int i=1;i<values.size();i++){
                String time = values.get(i);
                String startTime = getDestFormatValue(start);
                String endTime=getDestFormatValue(time);
                // 超过了30分钟的话，对应的需要生成一个新的记录
                Long duration = pass30Minute(startTime, endTime);
                if (duration>=30*60*1000){
                    JSONObject content=new JSONObject();
                    content.put("start",start);
                    content.put("stop",time);
                    content.put("duration",duration);
                    start = time;
                    jsonArray.add(content);
                }
            }
       }else {
           System.out.println("记录时间太短了，无法生成相关的数据记录");
       }
        return  jsonArray;
    }

    /**
     *  记录超过了30分钟
     * */
    private static Long pass30Minute(String startTime, String endTime) {
        Long start = Long.valueOf(startTime);
        Long end = Long.valueOf(endTime);
        return end-start;
    }

    /**
     *  排序操作实现
     * */
    private static void sort(MultiValueMap<String, String> map) {
        Set<String> keys = map.keySet();
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext()){
            String key = iterator.next();
            List<String> values = map.get(key);
            sortAllValues(values);
            map.replace(key,values);
        }
    }

    /**
     *  排序指定key下面所有的value的
     * */
    private static void sortAllValues(List<String> values) {
        values.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                try {
                    String d1 = getDestFormatValue(o1);
                    String d2 = getDestFormatValue(o2);
                    return d1.compareTo(d2);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
               return 0;
            }
        });
    }

    /**
     *  获取符合要求的时间格式数据
     * */
    private static String getDestFormatValue(String time) throws ParseException {
        String[] time_prepare = time.split("/");
        String[] dayMonthYear = time_prepare[2].split(":");
        String day=getDay(time_prepare);
        String month=getMonth(time_prepare);
        String year=getYear(dayMonthYear);
        long timeFinal=getFinalTime(year,month,day,dayMonthYear[1],dayMonthYear[2],dayMonthYear[3]);
        return timeFinal+"";
    }

    private static Long getFinalTime(String year, String month, String day, String hour, String minute, String second) throws ParseException {
        DateFormat format=new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        StringBuilder sb=new StringBuilder();
        sb.append(year).append("-").append(month).append("-").append(day).append("-").append(hour)
                .append("-").append(minute).append("-").append(second);
        Date time = format.parse(sb.toString());
        return time.getTime();
    }

    private static String getYear(String[] time) {
        return time[0];
    }

    private static String getMonth(String[] time) {
        switch (time[1]){
            case "May":
                return "5";
            default:
                return "0";
        }
    }

    private static String getDay(String[] time) {
        return time[0];
    }
}
