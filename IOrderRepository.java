
import java.util.*;
public interface IOrderRepository {
    void save(IOrder order);
    Optional<IOrder> findById(String id);
    List<IOrder> getAll();
    void deleteById(String id);
}
