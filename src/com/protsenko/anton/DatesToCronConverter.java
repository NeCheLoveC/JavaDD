package com.protsenko.anton;

import java.util.List;

public interface DatesToCronConverter {
    public String convert(List<String> inputDate);
    public String getImplementationInfo();
}
