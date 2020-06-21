package uk.dioxic.wfmt.repository.fragments;

import uk.dioxic.wfmt.model.Activity;
import uk.dioxic.wfmt.model.Order;

public interface OrderCustomizedRepository<T, ID> {

    /**
     * Saves a given entity. Use the returned instance for further operations as the save operation might have changed the
     * entity instance completely.
     *
     * @param entity must not be {@literal null}.
     * @return the saved entity; will never be {@literal null}.
     * @throws IllegalArgumentException in case the given {@literal entity} is {@literal null}.
     */
    <S extends T> S save(S entity);

    Order addActivity(Activity activity);

    void removeActivity(Activity activity);

    void removeActivity(Activity activity, Order.OrderPk orderPk);

    void updateActivity(Activity activity);

    void removeAllActivities();

}
