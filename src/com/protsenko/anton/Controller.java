package com.protsenko.anton;
public class Controller implements DatesToCronConverter{
    @Override
    public String convert(List<String> a){
        List<UserDate> dates = new ArrayList<UserDate>();
        //Раскидываем или в класс каждую дату или в Мапу
        for(String value : a){
            //Валидация даты
            if(!value.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}")){
                throw new DatesToCronConvertException("Ошибка...");
            }
            else{
                String[] args = value.split("T");
                String[] argsDate = args[0].split("-");
                String[] argsTime = args[1].split(":");
                dates.add(new UserDate(argsDate,argsTime));
            }
        }
        //Получаем список паттернов по секундам
        Map<String,List<UserDate>> patternsOfSeconds = UserDate.getPatterns(dates);
        Map.Entry<String,List<UserDate>> highestSec = filterMap(patternsOfSeconds);
        //Отфильтруем данный список
        //patternsOfSeconds.

        UserDate.setRules((UserDate val_1)->{return val_1.min;});
        Map<String,List<UserDate>> patternsOfMinutes = UserDate.getPatterns(dates);
        Map.Entry<String,List<UserDate>> highesMin = filterMap(patternsOfMinutes);


        UserDate.setRules((UserDate val_1)->{return val_1.hour;});
        Map<String,List<UserDate>> patternsOfHours = UserDate.getPatterns(dates);
        Map.Entry<String,List<UserDate>> highesHours = filterMap(patternsOfHours);

        UserDate.setRules((UserDate val_1)->{return val_1.dayOfTheMonth;});
        Map<String,List<UserDate>> patternsOfDayTheMonth = UserDate.getPatterns(dates);
        Map.Entry<String,List<UserDate>> highesDayTheMonth = filterMap(patternsOfDayTheMonth);

        UserDate.setRules((UserDate val_1)->{return val_1.month;});
        Map<String,List<UserDate>> patternsOfmonth = UserDate.getPatterns(dates);
        Map.Entry<String,List<UserDate>> highesMonth = filterMap(patternsOfmonth);

        /*
        UserDate.setRules((UserDate val_1)->{return val_1.dayOfWeek;});
        Map<String,List<UserDate>> patternsOfDayOfWeek = UserDate.getPatterns(dates);
        Map.Entry<String,List<UserDate>> highesDayOfWeek = filterMap(patternsOfmonth);
        */

        String secPattern = "*";
        String minPattern = "*";

        String hourPattern = "*";
        String dayOfTheMonthPattern = "*";
        String monthPattern = "*";
        List<UserDate> intersectionList = dates;
        boolean test = false;
        int minCountOfElements = (dates.size() + 1) / 2;
        if(highesMin != null){
            intersectionList.retainAll(highesMin.getValue());
            secPattern = highestSec.getKey();
            test = true;
        }
        if(highesMonth != null){
            highesMin.getValue().retainAll(intersectionList);
            if(highesMin.getValue().size() >= minCountOfElements){
                intersectionList = highesMin.getValue();
                minPattern = highesMin.getKey();
                test = true;
            }
        }
        if(highesHours != null){
            highesHours.getValue().retainAll(intersectionList);
            if(highesHours.getValue().size() >= minCountOfElements){
                intersectionList = highesHours.getValue();
                hourPattern = highesHours.getKey();
                test = true;
            }
        }
        if(highesDayTheMonth != null){
            highesDayTheMonth.getValue().retainAll(intersectionList);
            if(highesDayTheMonth.getValue().size() >= minCountOfElements){
                intersectionList = highesDayTheMonth.getValue();
                dayOfTheMonthPattern = highesDayTheMonth.getKey();
                test = true;
            }
        }
        if(highesMonth != null){
            highesMonth.getValue().retainAll(intersectionList);
            if(highesMonth.getValue().size() >= minCountOfElements){
                //intersectionList = highesMonth.getValue();
                monthPattern = highesMonth.getKey();
                test = true;
            }

        }
        if(!test){
            throw new DatesToCronConvertException("ОШИБКА!");
        }

        return secPattern +  " " + minPattern + " " + hourPattern + " " + dayOfTheMonthPattern + " " + monthPattern + " " + "*" ;
    }
    private Map.Entry<String,List<UserDate>> filterMap(Map<String,List<UserDate>> patterns){
        int size = 0;
        Map.Entry<String,List<UserDate>> highest = null;
        for(Map.Entry<String,List<UserDate>> b : patterns.entrySet()){
            if(b.getValue().size() > size){
                size = b.getValue().size();
                highest = b;
            }
        }
        return highest;
    }
    @Override
    public String getImplementationInfo(){

        return null;
    }

}
@FunctionalInterface
interface Ruleable<T extends UserDate>{
    int compare(T val_1);
}
class UserDate implements Comparable<UserDate>
{
    int second;
    int min;
    int hour;
    int dayOfTheMonth;
    int month;
    int dayOfWeek;
    private static Ruleable<UserDate> rules = ((UserDate val_1) -> {return val_1.second;});
    public UserDate(){}
    public UserDate(int second,int min,int hour,int dayOfTheMonth,int month,int dayOfWeek){
        this.second = second;
        this.min = min;
        this.hour = hour;
        this.dayOfTheMonth = dayOfTheMonth;
        this.month = month;
        this.dayOfWeek = dayOfWeek;
    }

    public static void setRules(Ruleable rules) {
        UserDate.rules = rules;
    }

    public UserDate(String[] inputDate,String[] inputTime){
        //Добавить id для чего-то???
        for(int i = 0;i < inputDate.length;i++){
            inputDate[i] = inputDate[i].replaceAll("\\b0+\\B","");
            inputDate[i] = inputDate[i].replaceAll("\\b0+\\b", "0");
        }
        for(int i = 0;i < inputTime.length;i++){
            inputDate[i] = inputDate[i].replaceAll("\\b0+\\B","");
            inputDate[i] = inputDate[i].replaceAll("\\b0+\\b", "0");
        }
        this.second = Integer.parseInt(inputTime[2]);
        this.min = Integer.parseInt(inputTime[1]);
        this.hour = Integer.parseInt(inputTime[0]);

        this.dayOfTheMonth = Integer.parseInt(inputDate[2]);
        this.month = Integer.parseInt(inputDate[1]);
    }

    public static Map<String, List<UserDate>> getPatterns(List<UserDate> dates){
        Collections.sort(dates);
        int startPosition;
        Map<String, List<UserDate>> patterns = new HashMap<String, List<UserDate>>();
        if(dates.size() == 1){
            List<UserDate> ls = new ArrayList<>();
            ls.add(dates.get(0));
            patterns.put(Integer.toString(rules.compare(dates.get(0))),ls);
            return patterns;
        }
        List<Integer[]> addedPatternsStep = new ArrayList<>();
        List<Integer> addedPatternsConst = new ArrayList<>();
        int countEqualence = 0;
        Integer lastValue= null;
        for(int i = 0;i < (dates.size() - 1) && (i < (dates.size() / 2 + 1));i++){
            //Где [0] - это начальный элемент (startPosition), [1] - step, т.е приращение
            if(lastValue == null || lastValue.intValue() != rules.compare(dates.get(i))){
                lastValue = rules.compare(dates.get(i));
                countEqualence = 1;
            }
            else{
                countEqualence++;
            }


            List<UserDate> listOfRelated = new ArrayList<UserDate>();
            startPosition = rules.compare(dates.get(i));
            UserDate after = dates.get(i + 1);
            int step = rules.compare(after) - startPosition;
            boolean isContinue = false;
            //Фильтруем подшаблоны
            if(step == 0){
                for(Integer k : addedPatternsConst){
                    if(k.intValue() == startPosition){
                        isContinue = true;
                        break;
                    }
                }
            }
            else{
                for(Integer[] k : addedPatternsStep){
                    if(step == k[1]){
                        if((k[0] - k[startPosition]) % step == 0){
                            isContinue = true;
                            break;
                        }
                    }
                }
            }
            if(isContinue) {
                continue;
            }
            if(step == 0){
                addedPatternsConst.add(startPosition);
            }
            else{
                addedPatternsStep.add(new Integer[]{Integer.valueOf(startPosition),Integer.valueOf(step)});
            }
            listOfRelated.add(dates.get(i));
            listOfRelated.add(dates.get(i + 1));
            for(int b = i + 2 - countEqualence; b < dates.size();b++){
                if(step == 0){
                    if(startPosition == rules.compare(dates.get(b))){
                        listOfRelated.add(dates.get(b));
                    }
                }
                else{
                    if((rules.compare(dates.get(b)) - startPosition) % step == 0){
                        listOfRelated.add(dates.get(b));
                    }
                }
            }
            if(listOfRelated.size() >= (Math.ceil(dates.size() / 2.0d))){
                StringBuilder str = new StringBuilder();
                if(step == 0){
                    str.append(startPosition);
                }
                else{
                    str.append(startPosition).append("/").append(step);
                }
                patterns.put(str.toString(),listOfRelated);

            }
        }
        return patterns;
    }
    @Override
    public int compareTo(UserDate o) {
        return rules.compare(this) - rules.compare(o);
    }
}
