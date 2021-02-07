package se.skltp.ei.updateprocess;

import java.util.HashMap;
import java.util.Map;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;

public abstract class AbstractMapAggregationStrategy <T, V> implements AggregationStrategy {

  public static final String CAMEL_GROUPED_EXCHANGE = "CamelGroupedExchange";

  public abstract Object getCompletedBody(Map<T, V> map);

  public abstract Object updateMap(Map<T, V> map, Exchange exhange);

  public boolean isStoreAsBodyOnCompletion() {
    return true;
  }

  @Override
  public void onCompletion(Exchange exchange) {
    if (exchange != null && this.isStoreAsBodyOnCompletion()) {

      Map<T, V> map = (Map) exchange.removeProperty(CAMEL_GROUPED_EXCHANGE);
      if (map != null) {
        exchange.getIn().setBody(this.getCompletedBody(map));
      } else {
        exchange.getIn().setBody(null);
      }
    }

  }

  @Override
  public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
    Map<T, V> map = this.getMap(oldExchange == null ? newExchange : oldExchange);

    if (newExchange != null) {
      this.updateMap(map, newExchange);
    }

    return oldExchange != null ? oldExchange : newExchange;
  }

  private Map<T, V> getMap(Exchange exchange) {
    Map<T, V> map = (Map) exchange.getProperty(CAMEL_GROUPED_EXCHANGE, Map.class);
    if (map == null) {
      map = new HashMap<>();
      exchange.setProperty(CAMEL_GROUPED_EXCHANGE, map);
    }

    return map;
  }


}
