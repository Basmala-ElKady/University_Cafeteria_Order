

import java.util.function.Supplier;
import java.util.stream.Collectors;


import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
public class ReportController extends AbstractRepoController implements IReport {
    public ReportController(Supplier<List<IOrder>> ordersProvider) { super(ordersProvider); }

    @Override
    public long getOrdersCountByDay(java.time.LocalDate day) {
        
        return allOrders().stream() .filter(o -> o.getCreatedAt().toLocalDate().equals(day)) .count(); 
    }

    @Override
    public long getOrdersCountByYear(int year) {
        
        return allOrders().stream() .filter(o -> o.getCreatedAt().getYear() == year) .count(); 
    }

    @Override
    public java.util.Map<String, Integer> getItemOrdersByDay(java.time.LocalDate day) {
    
        Map<String, Integer> map = new HashMap<>(); 
        allOrders().stream() 
        .filter(o -> o.getCreatedAt().toLocalDate().equals(day)) 
        .forEach(o -> o.getItems().forEach(oi -> { 
            String name = oi.getItem().getName(); 
            map.merge(name, oi.getQuantity(), Integer::sum); })); 
            return map;
    }

    @Override
    public java.util.LinkedHashMap<String, Integer> getTopSellingItems(int limit) {
       
        Map<String, Integer> counts = new HashMap<>(); 
        allOrders().forEach(o -> o.getItems().forEach(oi -> { 
            String name = oi.getItem().getName(); 
            counts.merge(name, oi.getQuantity(), Integer::sum); })); 
            return counts.entrySet().stream() 
            .sorted((a,b) -> Integer.compare(b.getValue(), a.getValue())) 
            .limit(limit) 
            .collect(Collectors.toMap( 
                Map.Entry::getKey, Map.Entry::getValue, (x,y) -> x, LinkedHashMap::new )); 
        }
                
    @Override
    public double getSalesTotalByPeriod(java.time.LocalDate start, java.time.LocalDate endInclusive) {
       LocalDateTime from = start.atStartOfDay(); 
       LocalDateTime to = endInclusive.plusDays(1).atStartOfDay().minusSeconds(1); 
       return allOrders().stream() 
       .filter(o -> !o.getCreatedAt().isBefore(from) && !o.getCreatedAt().isAfter(to)) .mapToDouble(IOrder::getTotalPrice) .sum(); 
    }

  
    
}
