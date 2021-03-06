package top.cadecode.sra.common.datasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.Map;

/**
 * @author Cade Li
 * @date 2021/12/3
 * @description 动态数据源类
 */
public class DynamicDS extends AbstractRoutingDataSource {

    /**
     * 设置数据源 key
     */
    @Override
    protected Object determineCurrentLookupKey() {
        return DynamicDSHolder.getDataSourceKey();
    }

    /**
     * 设置全部数据源
     */
    @Override
    public void setTargetDataSources(Map<Object, Object> targetDataSources) {
        super.setTargetDataSources(targetDataSources);
        // 存储数据源 key
        DynamicDSHolder.addDataSourceKeys(targetDataSources.keySet());
    }

    /**
     * 设置默认数据源
     */
    @Override
    public void setDefaultTargetDataSource(Object defaultTargetDataSource) {
        super.setDefaultTargetDataSource(defaultTargetDataSource);
    }

    /**
     * 切换数据源
     */
    @Override
    protected DataSource determineTargetDataSource() {
        return super.determineTargetDataSource();
    }
}
