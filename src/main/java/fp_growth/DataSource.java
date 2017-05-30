package fp_growth;

import java.util.Map;

/**
 * Created by yuan.wei on 5/11/17.
 */
public interface DataSource {// 数据集接口，产生用于构造FPTree的原始数据
    Map<String, Object> get_nextData();// 数据集的迭代器，迭代结束结束返回null(返回值key="data"对应的是事务，key="counter"对应事物出现的次数)
    void refresh_dataSource();// 重置迭代器
    void clear_cache();// 清空数据源中的缓存
}
