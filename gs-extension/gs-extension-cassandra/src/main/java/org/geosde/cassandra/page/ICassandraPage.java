package org.geosde.cassandra.page;

import java.util.List;
import com.datastax.driver.core.Row;

public interface ICassandraPage<T> {
	List<T> nextPage();
	T parse(Row row);
}
