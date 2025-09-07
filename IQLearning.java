

import java.util.List;

public interface IQLearning {
    void update(String state, Action action, double reward, String nextState);
    List<String> prioritize(List<IOrder> orders, Action action);
    String encodeState(List<IOrder> orders);
    Action chooseAction(String state);
}