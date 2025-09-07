

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class MenuManagerController {
    private List<IMenuItem> items;

    public MenuManagerController() {
        this.items = new ArrayList<>();
    }

    public IMenuItem addItem(IMenuItem item) {
        if (item != null) items.add(item);
        return item;
    }

    public IMenuItem removeItem(String itemId) {
        Optional<IMenuItem> item = items.stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst();
        item.ifPresent(items::remove);
        return item.orElse(null);
    }

    public IMenuItem updateItem(String itemId, IMenuItem updated) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(itemId)) {
                items.set(i, updated);
                return updated;
            }
        }
        return null;
    }

    public List<IMenuItem> getAllItems() {
        return new ArrayList<>(items);
    }

    public Optional<IMenuItem> findItem(String itemId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findItem'");
    }
}
