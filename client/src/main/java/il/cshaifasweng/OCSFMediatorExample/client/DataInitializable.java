package il.cshaifasweng.OCSFMediatorExample.client;

import com.mysql.cj.xdevapi.Client;

public interface DataInitializable {
    void initData(Object data);

    void setClient(SimpleClient client);
}
