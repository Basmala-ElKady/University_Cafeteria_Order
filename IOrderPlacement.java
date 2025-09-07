


import java.util.List;

public interface IOrderPlacement {
    IOrder placeOrder(IStudentAuthentication student, List<IMenuItem> items, IPointsAdder pointsAdder);
}
