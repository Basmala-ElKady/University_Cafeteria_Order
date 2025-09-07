

import java.time.LocalDate;
import java.util.Map;
import java.util.LinkedHashMap;
public interface IReport {

    long getOrdersCountByDay(LocalDate day);
    long getOrdersCountByYear(int year);
    Map<String, Integer> getItemOrdersByDay(LocalDate day);
    LinkedHashMap<String, Integer> getTopSellingItems(int limit);
    double getSalesTotalByPeriod(LocalDate start, LocalDate endInclusive);
}
