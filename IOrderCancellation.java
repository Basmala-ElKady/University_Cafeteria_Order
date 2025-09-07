import java.util.List;

public interface IOrderCancellation {

    boolean cancelOrder(String orderId);

    /** إنشاء الطلب + احتساب النقاط */
    IOrder placeOrder(IStudentAuthentication student, List<IMenuItem> items, StudentManagement studentCtrl);
}
